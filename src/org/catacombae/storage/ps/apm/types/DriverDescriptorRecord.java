/*-
 * Copyright (C) 2006-2011 Erik Larsson
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
import org.catacombae.util.Util;
import java.io.PrintStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import org.catacombae.io.RuntimeIOException;

/** This class was generated by CStructToJavaClass. (Modified afterwards) */
public class DriverDescriptorRecord {
    public static final short DDR_SIGNATURE = 0x4552;
    /*
     * struct DriverDescriptorRecord
     * size: >= 18 bytes
     * description:
     *
     * BP   Size  Type                      Identifier   Description
     * --------------------------------------------------------------------------------------
     * 0    2     be16                      sbSig        Device signature.
     * 2    2     be16                      sbBlkSize    Block size of the device.
     * 4    4     be32                      sbBlkCount   Number of blocks on the device.
     * 8    2     be16                      sbDevType    Reserved.
     * 10   2     be16                      sbDevId      Reserved.
     * 12   4     be32                      sbData       Reserved.
     * 16   2     be16                      sbDrvrCount  Number of driver descriptor entries.
     * 18   ?     DriverDescriptorEntry[?]  entries      Drivers, if any.
     * ?    ?     u8[?]                     ddPad        Reserved.
     */

    private final byte[] sbSig = new byte[2];
    private final byte[] sbBlkSize = new byte[2];
    private final byte[] sbBlkCount = new byte[4];
    private final byte[] sbDevType = new byte[2];
    private final byte[] sbDevId = new byte[2];
    private final byte[] sbData = new byte[4];
    private final byte[] sbDrvrCount = new byte[2];
    private final DriverDescriptorEntry[] entries;
    private final byte[] ddPad;

    public DriverDescriptorRecord(ReadableRandomAccessStream llf, long offset) {
	this(readData(llf, offset), 0);
    }
    public DriverDescriptorRecord(byte[] data, int offset) {
        if((data.length - offset) < 18) {
            throw new RuntimeException("Insufficient remaining data in " +
                    "buffer for a " + this.getClass().getName() + " " +
                    "(remaining: " + (data.length - offset) + " bytes, " +
                    "required: 18 bytes).");
        }

	System.arraycopy(data, offset+0, sbSig, 0, 2);
	System.arraycopy(data, offset+2, sbBlkSize, 0, 2);
	System.arraycopy(data, offset+4, sbBlkCount, 0, 4);
        System.arraycopy(data, offset+8, sbDevType, 0, 2);
        System.arraycopy(data, offset+10, sbDevId, 0, 2);
        System.arraycopy(data, offset+12, sbData, 0, 4);
	System.arraycopy(data, offset+16, sbDrvrCount, 0, 2);

	int numEntries = Util.unsign(getSbDrvrCount());
        if(numEntries * DriverDescriptorEntry.length() >
                (data.length - offset - 18))
        {
            numEntries = (data.length - offset - 18) /
                    DriverDescriptorEntry.length();
        }

	entries = new DriverDescriptorEntry[numEntries];
	int i;
	for(i = 0; i < entries.length; ++i)
	    entries[i] = new DriverDescriptorEntry(data, offset+18 + DriverDescriptorEntry.length()*i);

	int padOffset = offset+18 + DriverDescriptorEntry.length()*i;
        int padLength = getSbBlkSize() - padOffset;
        if(((data.length - offset) - padOffset) < padLength) {
            padLength = (data.length - offset) - padOffset;
        }

        ddPad = new byte[padLength];
        System.arraycopy(data, offset + padOffset, ddPad, 0, ddPad.length);
    }

    /**
     * Creates a new DriverDescriptorRecord from the supplied parameters.
     *
     * @param blockSize the block size of the volume. Commonly 512 or 4096 for
     * hard disks and 2048 for optical drives.
     * @param blockCount the size of the volume in blockSize-sized blocks.
     */
    public DriverDescriptorRecord(int blockSize, long blockCount) {
        if(blockSize < 0 || blockSize > 0xFFFF)
            throw new IllegalArgumentException("Invalid value for " +
                    "'blockSize': " + blockSize);
        if(blockCount < 0 || blockCount > 0xFFFFFFFFL)
            throw new IllegalArgumentException("Invalid value for " +
                    "'blockCount': " + blockCount);

        Util.arrayPutBE(this.sbSig, 0, DDR_SIGNATURE);
        Util.arrayPutBE(this.sbBlkSize, 0, (short) blockSize);
        Util.arrayPutBE(this.sbBlkCount, 0, (int) blockCount);
        Util.arrayPutBE(this.sbDevType, 0, (short) 0);
        Util.arrayPutBE(this.sbDevId, 0, (short) 0);
        Util.arrayPutBE(this.sbData, 0, (int) 0);
        Util.arrayPutBE(this.sbDrvrCount, 0, (short) 0);
        this.entries = new DriverDescriptorEntry[0];
        this.ddPad = new byte[blockSize - 18];
        Arrays.fill(ddPad, (byte) 0);
    }

    private static byte[] readData(ReadableRandomAccessStream llf, long offset)
    {
        byte[] data = null;
        int curBlockSize = 512;
        RuntimeIOException mostRecentException = null;

        do {
            data = new byte[curBlockSize];
            try {
                llf.seek(offset);
                llf.readFully(data);
            } catch(RuntimeIOException ex) {
                /* It's possible that an exception is thrown if the device
                 * requires aligned access. In that case ignore and increase the
                 * block size until we hit the required alignment block size. */

                mostRecentException = ex;
                data = null;
            }
        } while(data == null && (curBlockSize *= 2) <= 4096);

        if(data == null) {
            /* We got I/O errors when attempting to read using all attempted
             * block sizes. This must be something unrelated to alignment, and
             * we cannot continue so throw the most recent exception. */

            throw mostRecentException;
        }

        DriverDescriptorRecord ddrTmp =
                new DriverDescriptorRecord(data, 0);
        if(ddrTmp.isValid() && ddrTmp.getSbBlkSize() > data.length) {
            /* Logical block size is different than our current block size.
             * Re-read with logical block size to get entire driver descriptor
             * record. */

            data = new byte[ddrTmp.getSbBlkSize()];
            llf.seek(offset);
            llf.readFully(data);
        }

        return data;
    }

    public int length() {
        return 18 + entries.length * DriverDescriptorEntry.length() +
                ddPad.length;
    }

    /** Device signature. (Should be "ER"...) */
    public short getSbSig() { return Util.readShortBE(sbSig); }
    /** Block size of the device. */
    public short getSbBlkSize() { return Util.readShortBE(sbBlkSize); }
    /** Number of blocks on the device. */
    public int getSbBlkCount() { return Util.readIntBE(sbBlkCount); }
    /** Reserved. */
    public short getSbDevType() { return Util.readShortBE(sbDevType); }
    /** Reserved. */
    public short getSbDevId() { return Util.readShortBE(sbDevId); }
    /** Reserved. */
    public int getSbData() { return Util.readIntBE(sbData); }
    /** Number of driver descriptor entries. Won't be more than 31 in a valid structure. */
    public short getSbDrvrCount() { return Util.readShortBE(sbDrvrCount); }
    public DriverDescriptorEntry[] getDriverDecriptorEntries() {
	DriverDescriptorEntry[] result = new DriverDescriptorEntry[entries.length];
	System.arraycopy(entries, 0, result, 0, entries.length);
	return result;
    }
    /** Reserved. */
    public byte[] getDdPad() { return Util.createCopy(ddPad); }

    /** Returns a String representation of the device signature. */
    public String getSbSigAsString() { return Util.toASCIIString(sbSig); }

    public boolean isValid() {
	int driverCount = Util.unsign(getSbDrvrCount());
	return getSbSig() == DDR_SIGNATURE && driverCount <= 31 && entries.length == driverCount;
    }

    public byte[] getData() {
	byte[] result = new byte[length()];
	int offset = 0;
	System.arraycopy(sbSig, 0, result, offset, sbSig.length); offset += sbSig.length;
	System.arraycopy(sbBlkSize, 0, result, offset, sbBlkSize.length); offset += sbBlkSize.length;
	System.arraycopy(sbBlkCount, 0, result, offset, sbBlkCount.length); offset += sbBlkCount.length;

        System.arraycopy(sbDevType, 0, result, offset, sbDevType.length);
        offset += sbDevType.length;

        System.arraycopy(sbDevId, 0, result, offset, sbDevId.length);
        offset += sbDevId.length;

        System.arraycopy(sbData, 0, result, offset, sbData.length); 
        offset += sbData.length;

	System.arraycopy(sbDrvrCount, 0, result, offset, sbDrvrCount.length); offset += sbDrvrCount.length;
	for(DriverDescriptorEntry dde : entries) {
	    byte[] tmp = dde.getData();
	    System.arraycopy(tmp, 0, result, offset, tmp.length); offset += tmp.length;
	}
	System.arraycopy(ddPad, 0, result, offset, ddPad.length); offset += ddPad.length;
	//System.arraycopy(, 0, result, offset, .length); offset += .length;
	if(offset != length())
	    throw new RuntimeException("Internal miscalculation...");
	else
	    return result;
    }

    public void printFields(PrintStream ps, String prefix) {
	ps.println(prefix + " sbSig: \"" + getSbSigAsString() + "\"");
	ps.println(prefix + " sbBlkSize: " + getSbBlkSize());
	ps.println(prefix + " sbBlkCount: " + getSbBlkCount());
        ps.println(prefix + " sbDevType: " + getSbDevType());
        ps.println(prefix + " sbDevId: " + getSbDevId());
        ps.println(prefix + " sbData: " + getSbData());
	ps.println(prefix + " sbDrvrCount: " + getSbDrvrCount());
	ps.println(prefix + " entries (" + entries.length + " elements):");
	for(int i = 0; i < entries.length; ++i) {
	    ps.println(prefix + "  entries[" + i + "]: ");
	    entries[i].print(ps, prefix + "   ");
	}
	if(entries.length == 0)
	    ps.println(prefix + "  <empty>");
	ps.println(prefix + " ddPad:");
        ps.print(prefix + "  byte[" + ddPad.length + "] {");
        for(int i = 0; i < ddPad.length; ++i) {
            if(i % 16 == 0) {
                ps.println();
                ps.print(prefix + "  ");
            }
            ps.print(" " + Util.toHexStringBE(ddPad[i]));
        }
        ps.println();
        ps.println(prefix + "  }");

        try {
            byte[] md5sum = MessageDigest.getInstance("MD5").digest(ddPad);
            ps.println(prefix + "  MD5: " + Util.byteArrayToHexString(md5sum));
        } catch(NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public void print(PrintStream ps, String prefix) {
	ps.println(prefix + "DriverDescriptorRecord:");
	printFields(ps, prefix);
    }
}
