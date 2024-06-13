/*-
 * Copyright (C) 2006 Erik Larsson
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

import java.io.File;
import java.io.FileOutputStream;
import java.util.LinkedList;

import org.apache.tools.ant.types.Description;
import org.catacombae.io.ReadableFileStream;
import org.catacombae.io.ReadableRandomAccessStream;

import javax.swing.JOptionPane;

import org.catacombae.hfsexplorer.GUIUtil;
import org.catacombae.hfsexplorer.SelectDeviceDialog;
import org.catacombae.storage.io.ReadableStreamDataLocator;
import org.catacombae.storage.ps.apm.types.ApplePartitionMap;
import org.catacombae.storage.ps.gpt.types.GUIDPartitionTable;
import org.catacombae.storage.ps.mbr.types.MBRPartitionTable;
import org.catacombae.storage.io.win32.ReadableWin32FileStream;
import org.catacombae.storage.ps.Partition;
import org.catacombae.storage.ps.PartitionSystemDetector;
import org.catacombae.storage.ps.PartitionSystemHandler;
import org.catacombae.storage.ps.PartitionSystemHandlerFactory;
import org.catacombae.storage.ps.PartitionSystemType;
import org.catacombae.storage.ps.PartitionType;
import org.catacombae.storage.ps.apm.APMHandler;
import org.catacombae.storage.ps.apm.types.DriverDescriptorRecord;
import org.catacombae.storage.ps.gpt.GPTHandler;
import org.catacombae.storage.ps.mbr.MBRHandler;


/**
 * @author <a href="https://catacombae.org" target="_top">Erik Larsson</a>
 */
public class DumpFSInfo {

    public static void main(String[] args) throws Throwable {
        try {
            javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
            //
            // Description of look&feels:
            // http://java.sun.com/docs/books/tutorial/uiswing/misc/plaf.html
            //
        } catch (Throwable e) {
            // It's ok. Non-critical.
        }
        try {
            dumpInfo(args);
            System.exit(0);
        } catch (Exception e) {
            GUIUtil.displayExceptionDialog(e, 25, null);
        } catch (Throwable t) {
            t.printStackTrace();
            throw t;
        }
        System.exit(1);
    }

    public static void dumpInfo(String[] args) throws Exception {
        long runTimestamp = System.currentTimeMillis();
        ReadableRandomAccessStream fsFile;
        if (args.length == 1) {
            if (ReadableWin32FileStream.isSystemSupported()) {
                fsFile = new ReadableWin32FileStream(args[0]);
            } else {
                fsFile = new ReadableFileStream(args[0]);
            }
        } else if (SelectDeviceDialog.isSystemSupported()) {
            if (args.length == 0) {
                SelectDeviceDialog swdd =
                        SelectDeviceDialog.createSelectDeviceDialog(null, true,
                                "Select device to extract info from");
                swdd.setVisible(true);
                fsFile = swdd.getPartitionStream();
                if (fsFile == null)
                    System.exit(0);
            } else {
                System.out.println("Usage: java DumpFSInfo <filename>");
                System.out.println("        for reading directly from a specified file, or...");
                System.out.println("       java DumpFSInfo");
                System.out.println("        to pop up a device dialog where " +
                        "you can choose which device to read");
                return;
            }
        } else {
            System.out.println("Usage: java DumpFSInfo <filename>");
            return;
        }

        LinkedList<File> generatedFiles = new LinkedList<File>();
        long fsOffset, fsLength;
        int partNum = -1;

        PartitionSystemType[] detectedTypes = PartitionSystemDetector.detectPartitionSystem(fsFile, false);

        PartitionSystemType detectedType;
        PartitionSystemHandler partSys;
        if (detectedTypes.length == 1) {
            detectedType = detectedTypes[0];
            PartitionSystemHandlerFactory fact = detectedType.createDefaultHandlerFactory();
            partSys = fact.createHandler(new ReadableStreamDataLocator(fsFile));
        } else if (detectedTypes.length == 0) {
            detectedType = null;
            partSys = null;
        } else {
            String msg = "Multiple partition system types detected:";
            for (PartitionSystemType t : detectedTypes)
                msg += " " + t;
            throw new RuntimeException(msg);
        }

        if (partSys != null) {
            Partition[] partitions = partSys.getPartitions();
            if (partitions.length == 0) {
                // Proceed to detect file system
                fsOffset = 0;
                try {
                    fsLength = fsFile.length();
                } catch (Exception e) {
                    e.printStackTrace();
                    fsLength = -1;
                }
            } else {
                // Dump partition system to file(s)
                if (partSys instanceof APMHandler) {
                    APMHandler apmHandler = (APMHandler) partSys;
                    File ddrFile = new File("fsdump-" + runTimestamp + "_ddr.dat");
                    FileOutputStream fos = new FileOutputStream(ddrFile);
                    DriverDescriptorRecord ddr = apmHandler.readDriverDescriptorRecord();
                    fos.write(ddr.getData());
                    fos.close();
                    generatedFiles.add(ddrFile);

                    ApplePartitionMap apm = apmHandler.readPartitionMap();
                    if (apm == null)
                        throw new RuntimeException("Failed to read APM data.");

                    File apmFile = new File("fsdump-" + runTimestamp + "_apm.dat");
                    fos = new FileOutputStream(apmFile);
                    fos.write(apm.getData());
                    fos.close();
                    generatedFiles.add(apmFile);
                } else if (partSys instanceof GPTHandler) {
                    GPTHandler gptHandler = (GPTHandler) partSys;
                    File mbrFile = new File("fsdump-" + runTimestamp + "_protectivembr.dat");
                    byte[] mbrData = new byte[512];
                    FileOutputStream fos = new FileOutputStream(mbrFile);
                    fsFile.seek(0);
                    fsFile.readFully(mbrData);
                    fos.write(mbrData);
                    fos.close();
                    generatedFiles.add(mbrFile);

                    GUIDPartitionTable gpt = gptHandler.readPartitionTable();
                    if (gpt == null)
                        throw new RuntimeException("Failed to read GPT data.");

                    File gptBeginFile = new File("fsdump-" + runTimestamp + "_gptprimary.dat");
                    fos = new FileOutputStream(gptBeginFile);
                    fos.write(gpt.getPrimaryTableBytes());
                    fos.close();
                    generatedFiles.add(gptBeginFile);

                    File gptEndFile = new File("fsdump-" + runTimestamp + "_gptbackup.dat");
                    fos = new FileOutputStream(gptEndFile);
                    fos.write(gpt.getBackupTableBytes());
                    fos.close();
                    generatedFiles.add(gptEndFile);
                } else if (partSys instanceof MBRHandler) {
                    MBRHandler mbrHandler = (MBRHandler) partSys;
                    MBRPartitionTable mbr = mbrHandler.readPartitionTable();

                    File mbrFile = new File("fsdump-" + runTimestamp + "_mbr.dat");
                    FileOutputStream fos = new FileOutputStream(mbrFile);
                    fos.write(mbr.getMasterBootRecord().getBytes());
                    fos.close();
                    generatedFiles.add(mbrFile);
                } else
                    throw new RuntimeException("Unknown partition system type!");

                Object selectedValue;
                int firstPreferredPartition = 0;
                for (int i = 0; i < partitions.length; ++i) {
                    Partition p = partitions[i];
                    PartitionType pt = p.getType();
                    if (pt == PartitionType.APPLE_HFS_CONTAINER || pt == PartitionType.APPLE_HFSX) {
                        firstPreferredPartition = i;
                        break;
                    }
                }
                selectedValue = JOptionPane.showInputDialog(null,
                        "Select which partition to read",
                        "Choose " + detectedType.getLongName() + " partition",
                        JOptionPane.QUESTION_MESSAGE,
                        null, partitions, partitions[firstPreferredPartition]);
                for (int i = 0; i < partitions.length; ++i) {
                    if (partitions[i] == selectedValue) {
                        partNum = i;
                        break;
                    }
                }

                if (selectedValue instanceof Partition) {
                    Partition selectedPartition = (Partition) selectedValue;
                    fsOffset = selectedPartition.getStartOffset();
                    fsLength = selectedPartition.getLength();
                } else
                    throw new RuntimeException("Impossible error!");
            }
        } else {
            fsOffset = 0;
            try {
                fsLength = fsFile.length();
            } catch (Exception e) {
                e.printStackTrace();
                fsLength = -1;
            }
        }

        // Dump the first and last 64 KiB from the partition
        byte[] buffer = new byte[65536];
        File first64File;
        File last64File;
        if (partNum == -1) {
            first64File = new File("fsdump-" + runTimestamp + "_first64.dat");
            last64File = new File("fsdump-" + runTimestamp + "_last64.dat");
        } else {
            first64File = new File("fsdump-" + runTimestamp + "_p" + partNum + "_first64.dat");
            last64File = new File("fsdump-" + runTimestamp + "_p" + partNum + "_last64.dat");
        }

        if (extractDataToFile(fsFile, fsOffset, first64File, 65536))
            generatedFiles.add(first64File);

        long pos = fsOffset + fsLength - buffer.length;
        if (pos > fsOffset &&
                extractDataToFile(fsFile, pos, last64File, 65536))
            generatedFiles.add(last64File);

        // Display result
        StringBuilder sb = new StringBuilder();
        sb.append("Dumped FS info to directory:\n    ");
        File firstFile = generatedFiles.getFirst().getAbsoluteFile();
        File firstParent = firstFile.getParentFile();
        sb.append(firstParent.getAbsolutePath());
        sb.append("\nThe following files were generated:\n    ");
        for (File f : generatedFiles)
            sb.append(f.toString() + "\n    ");

        JOptionPane.showMessageDialog(null, sb.toString(), "Result", JOptionPane.INFORMATION_MESSAGE);
    }

    private static boolean extractDataToFile(ReadableRandomAccessStream fsFile, long pos, File outFile, int dataSize) {
        try {
            byte[] buffer = new byte[dataSize];
            fsFile.seek(pos);
            int bytesRead = fsFile.read(buffer);
            FileOutputStream fileOut = new FileOutputStream(outFile);
            fileOut.write(buffer, 0, bytesRead);
            fileOut.close();

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
