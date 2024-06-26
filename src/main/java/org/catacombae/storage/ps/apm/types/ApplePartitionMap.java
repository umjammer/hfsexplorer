/*-
 * Copyright (C) 2006-2007 Erik Larsson
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

package org.catacombae.storage.ps.apm.types;

import org.catacombae.io.ReadableRandomAccessStream;
import org.catacombae.io.ReadableByteArrayStream;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.io.PrintStream;

import org.catacombae.storage.ps.Partition;
import org.catacombae.storage.ps.legacy.PartitionSystem;
import org.catacombae.util.Util;

import static java.lang.System.getLogger;


/**
 * @author <a href="https://catacombae.org" target="_top">Erik Larsson</a>
 */
public class ApplePartitionMap implements PartitionSystem {

    private static final Logger logger = getLogger(ApplePartitionMap.class.getName());

    private final APMPartition[] partitions;

    public ApplePartitionMap(ReadableRandomAccessStream isoRaf, long pmOffset, int blockSize) {
        isoRaf.seek(pmOffset);
        byte[] currentBlock = new byte[blockSize];
        ArrayList<APMPartition> partitionList = new ArrayList<>();

        // Redundant fields
        Short pmSig = null;
        Short pmSigPad = null;
        Long pmMapBlkCnt = null;
//        long partitionIndex = 0;

        // Loop while we have data left in the file
        while (((partitionList.isEmpty() && pmMapBlkCnt == null) ||
                (!partitionList.isEmpty() && partitionList.size() < pmMapBlkCnt))
            /* && isoRaf.length() > isoRaf.getFilePointer() + currentBlock.length */) {
            isoRaf.readFully(currentBlock);
            APMPartition p = new APMPartition(currentBlock, 0, blockSize);
//            logger.log(Level.DEBUG, "Processing partition " + partitionIndex + ":");
//            p.printFields(System.err, "  ");
            if (p.isValid()) {
                short curPmSig = p.getPmSig();
                short curPmSigPad = p.getPmSigPad();
                long curPmMapBlkCnt = p.getPmMapBlkCnt();

                if (pmMapBlkCnt != null && pmSigPad != null && pmSig != null) {
                    if (curPmSig != pmSig || curPmSigPad != pmSigPad || curPmMapBlkCnt != pmMapBlkCnt)
                        throw new RuntimeException("Redundant fields mismatch at index: " +
                                partitionList.size() + " (curPmSig=" + curPmSig + " pmSig=" + pmSig +
                                " curPmSigPad=" + curPmSigPad + " pmSigPad=" + pmSigPad +
                                " curPmMapBlkCnt=" + curPmMapBlkCnt + " pmMapBlkCnt=" + pmMapBlkCnt + ")");
                } else {
                    // First partition
                    pmSig = curPmSig;
                    pmSigPad = curPmSigPad;
                    pmMapBlkCnt = curPmMapBlkCnt;
                }

                partitionList.add(p);
            } else {
                logger.log(Level.DEBUG, "Erroneous partition:");
                p.printFields(System.err, "  ");
                throw new RuntimeException("Encountered invalid partition map entry at index: " +
                        partitionList.size() + " pmMapBlkCnt=" + pmMapBlkCnt);
            }

//            ++partitionIndex;
        }
        partitions = partitionList.toArray(APMPartition[]::new);
    }

    public ApplePartitionMap(byte[] data, int off, int blockSize) {
        this(new ReadableByteArrayStream(data, 0, data.length), off, blockSize);
    }

    @Override
    public boolean isValid() {
        if (partitions.length > 0) {
            for (APMPartition p : partitions) {
                if (!p.isValid())
                    return false;
            }
            return true;
        } else // An empty partition system is really not a partition system.
            return false;
    }

    @Override
    public int getPartitionCount() {
        return partitions.length;
    }

    @Override
    public int getUsedPartitionCount() {
        return getPartitionCount();
    }

    /** index must be between 0 and getNumPartitions()-1. */
    public APMPartition getAPMPartition(int index) {
        return partitions[index];
    }

    @Override
    public APMPartition[] getPartitionEntries() {
        APMPartition[] copy = new APMPartition[partitions.length];
        for (int i = 0; i < partitions.length; ++i)
            copy[i] = partitions[i];
        return copy;
    }

    @Override
    public Partition[] getUsedPartitionEntries() {
        return getPartitionEntries();
    }

    public byte[] getData() {
        byte[] result =
                new byte[(partitions != null && partitions.length != 0) ?
                partitions.length * partitions[0].size() : 0];
        int offset = 0;
        for (APMPartition ap : partitions) {
            byte[] tmp = ap.getData();
            System.arraycopy(tmp, 0, result, offset, tmp.length);
            offset += tmp.length;
        }
//        System.arraycopy(, 0, result, offset, .length); offset += .length;
        if (offset != result.length)
            throw new RuntimeException("Internal miscalculation...");
        else
            return result;
    }

    @Override
    public void printFields(PrintStream ps, String prefix) {
        for (int i = 0; i < partitions.length; ++i) {
            ps.println(prefix + " partitions[" + i + "]:");
            partitions[i].print(ps, prefix + "  ");
        }
    }

    @Override
    public void print(PrintStream ps, String prefix) {
        ps.println("Apple Partition Map:");
        printFields(ps, prefix);
    }

    @Override
    public Partition getPartitionEntry(int index) {
        return getAPMPartition(index);
    }

    @Override
    public String getLongName() {
        return "Apple Partition Map";
    }

    @Override
    public String getShortName() {
        return "APM";
    }

//    /** This main method prints the fields of the DDR and APM in the file specified in args[0], offset 0. */
//    public static void main(String[] args) {
//        ReadableFileStream fin = new ReadableFileStream(args[0]);
//        byte[] curBlock = new byte[DriverDescriptorRecord.length()];
//
//        if (fin.read(curBlock) != curBlock.length) throw new RuntimeException("Could not read all...");
//        DriverDescriptorRecord ddr = new DriverDescriptorRecord(curBlock, 0);
//        ddr.print(System.out, "");
//
//        final int blockSize = 512;
//        curBlock = new byte[blockSize];
//
//        ApplePartitionMap apm = new ApplePartitionMap(fin, blockSize, blockSize);
//        apm.print(System.out, "");
//    }
}
