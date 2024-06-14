/*-
 * Copyright (C) 2011 Erik Larsson
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.catacombae.hfsexplorer.tools;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.Arrays;
import java.util.LinkedList;

import org.catacombae.storage.ps.apm.types.APMPartition;
import org.catacombae.storage.ps.apm.types.DriverDescriptorRecord;
import org.catacombae.util.Util;

import static java.lang.System.getLogger;


/**
 * @author <a href="https://catacombae.org" target="_top">Erik Larsson</a>
 */
public class MkAPM {

    private static final Logger logger = getLogger(MkAPM.class.getName());

    private static class PartitionSpec {

        Long partitionByteStart = null;
        Long partitionByteLength = null;
        String partitionName = null;
        String partitionType = null;
    }

    /* Options. */
    private static class Options {

        Integer blockSize = null;
//        Long deviceBlockCount = null;
        final LinkedList<PartitionSpec> partitions = new LinkedList<>();
        RandomAccessFile file = null;
    }

    private static final Options options = new Options();

    private static boolean parseOptions(String[] args) {
        for (int i = 0; i < args.length - 1; ++i) {
            String curArg = args[i];
            logger.log(Level.DEBUG, "Processing argument " + i + ": " + curArg);
            if (curArg.equals("--sector-size")) {
                if (i + 1 == args.length - 1) {
                    logger.log(Level.DEBUG, "Incomplete sector size specification.");
                    return false;
                }

                try {
                    options.blockSize = Integer.parseInt(args[++i]);
                } catch (Exception e) {
                    logger.log(Level.DEBUG, "Error: Invalid sector size \"" + args[i] + "\".");
                    return false;
                }
            }
//            else if (curArg.equals("--byte-count")) {
//                try {
//                    byteCount = Long.parseLong(args[++i]);
//                } catch (Exception e) {
//                    logger.log(Level.DEBUG, "Error: Invalid byte count \"" + args[i] + "\".");
//                    return false;
//                }
//            }
            else if (curArg.equals("--part")) {
                // Add partition.

                if (i + 4 == args.length - 1) {
                    logger.log(Level.DEBUG, "Incomplete part specification.");
                    return false;
                }

                PartitionSpec p = new PartitionSpec();
                try {
                    p.partitionByteStart = Long.parseLong(args[++i]);
                    p.partitionByteLength = Long.parseLong(args[++i]);
                    p.partitionName = args[++i];
                    p.partitionType = args[++i];
                } catch (Exception e) {
                    logger.log(Level.DEBUG, "Error: Invalid partition specification.");
                    return false;
                }

                options.partitions.add(p);
            } else {
                logger.log(Level.DEBUG, "Unrecognized argument: \"" + curArg + "\"");
                return false;
            }
        }

        String filename = args[args.length - 1];
        try {
            options.file = new RandomAccessFile(filename, "r");
        } catch (FileNotFoundException ex) {
            logger.log(Level.DEBUG, "Error: Failed to open file \"" + filename + "\".");
            return false;
        }

        if (options.blockSize == null) {
            logger.log(Level.DEBUG, "Error: No block size specified.");
            return false;
        }

        {
            int i = 0;
            for (PartitionSpec ps : options.partitions) {
                if ((ps.partitionByteStart % options.blockSize) != 0) {
                    logger.log(Level.DEBUG, "Error: Start of partition " + i + " is not a multiple of sector size.");
                    return false;
                } else if ((ps.partitionByteLength % options.blockSize) != 0) {
                    logger.log(Level.DEBUG, "Error: Length of partition " + i + " is not a multiple of sector size.");
                    return false;
                } else if (ps.partitionName.length() > 32) {
                    logger.log(Level.DEBUG, "Error: Name of partition " + i + " is too long.");
                    return false;
                } else if (ps.partitionType.length() > 32) {
                    logger.log(Level.DEBUG, "Error: Type of partition " + i + " is too long.");
                    return false;
                }

                try {
                    Util.encodeASCIIString(ps.partitionName);
                } catch (IllegalArgumentException e) {
                    logger.log(Level.DEBUG, "Error: Name of partition " + i + " contains invalid characters.");
                    return false;
                }

                try {
                    Util.encodeASCIIString(ps.partitionType);
                } catch (IllegalArgumentException e) {
                    logger.log(Level.DEBUG, "Error: Type of partition " + i + " contains invalid characters.");
                    return false;
                }
            }
        }

        // Re-open file in write mode.
        try {
            options.file.close();
            options.file = new RandomAccessFile(filename, "rw");
        } catch (FileNotFoundException ex) {
            logger.log(Level.DEBUG, "Error: Failed to open file \"" + filename + "\".");
            return false;
        } catch (IOException ex) {
            logger.log(Level.ERROR, ex.getMessage(), ex);
            return false;
        }

        return true;
    }

    private static void printUsage() {
        logger.log(Level.DEBUG, "usage: mkapm --sector-size <sector size> --part " +
                "<start offset> <length> <partition name> <partition type>");
    }

    private static void writeWithPadding(RandomAccessFile file, byte[] data, int paddedSize) throws IOException {
        byte[] fullBlock = new byte[paddedSize];
        System.arraycopy(data, 0, fullBlock, 0, data.length);
        Arrays.fill(fullBlock, data.length, fullBlock.length, (byte) 0);
        file.write(fullBlock);
    }

    public static void main(String[] args) {
        logger.log(Level.DEBUG, "YO! args.length=" + args.length);
        if (args.length == 0 || !parseOptions(args)) {
            printUsage();
            System.exit(1);
            return;
        }

        long deviceByteCount;
        try {
            deviceByteCount = options.file.length();
        } catch (IOException ioe) {
            logger.log(Level.ERROR, ioe.getMessage(), ioe);
            System.exit(1);
            return;
        }

        if ((deviceByteCount % options.blockSize) != 0) {
            logger.log(Level.DEBUG, "Error: File size is not a multiple of sector size.");
            printUsage();
            System.exit(1);
            return;
        }

        long deviceBlockCount = deviceByteCount / options.blockSize;

        DriverDescriptorRecord ddr = new DriverDescriptorRecord(options.blockSize, deviceBlockCount);
        LinkedList<APMPartition> partitions = new LinkedList<>();

        /* First partition always describes the partition map itself. */
        partitions.add(new APMPartition(2, 1, 15, "Apple",
                "Apple_partition_map", 0x0, options.blockSize));

        for (PartitionSpec ps : options.partitions) {
            APMPartition curPart = new APMPartition(2,
                    ps.partitionByteStart / options.blockSize,
                    ps.partitionByteLength / options.blockSize,
                    ps.partitionName, ps.partitionType, 0x40000077,
                    options.blockSize);
            partitions.add(curPart);
        }

        try {
            writeWithPadding(options.file, ddr.getData(), options.blockSize);
            for (APMPartition p : partitions) {
                logger.log(Level.DEBUG, "Writing out partition:");
                p.print(System.err, "\t");
                writeWithPadding(options.file, p.getData(), options.blockSize);
            }
            writeWithPadding(options.file, new byte[0], 15 * options.blockSize -
                    (partitions.size() + 1) * options.blockSize);
            options.file.close();
        } catch (IOException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
    }
}
