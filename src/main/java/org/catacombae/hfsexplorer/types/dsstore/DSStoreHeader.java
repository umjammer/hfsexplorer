/*-
 * Copyright (C) 2021 Erik Larsson
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

package org.catacombae.hfsexplorer.types.dsstore;

import java.io.PrintStream;

import org.catacombae.util.Util;


/** This class was generated by CStructToJavaClass. */
public class DSStoreHeader {
    /*
     * struct DSStoreHeader
     * size: 36 bytes
     * description:
     *
     * BP  Size    Type    Identifier        Description
     * ------------------------------------------------------------------------
     * 0   4       be32    alignment
     * 4   1 * 4   u8[4]   signature         Must be 'Bud1' in ASCII.
     * 8   4       be32    rootBlockOffset1  Must be equal to rootBlockOffset2.
     * 12  4       be32    rootBlockSize
     * 16  4       be32    rootBlockOffset2  Must be equal to rootBlockOffset1.
     * 20  4 * 4   be32[4] unknown
     */

    public static final byte[] SIGNATURE = new byte[] {'B', 'u', 'd', '1'};

    public static final int STRUCTSIZE = 36;

    private int alignment;
    private int signature;
    private int rootBlockOffset1;
    private int rootBlockSize;
    private int rootBlockOffset2;
    private final byte[] unknown = new byte[4 * 4];

    public DSStoreHeader(byte[] data, int offset) {
        this.alignment = Util.readIntBE(data, offset + 0);
        this.signature = Util.readIntBE(data, offset + 4);
        this.rootBlockOffset1 = Util.readIntBE(data, offset + 8);
        this.rootBlockSize = Util.readIntBE(data, offset + 12);
        this.rootBlockOffset2 = Util.readIntBE(data, offset + 16);
        System.arraycopy(data, offset + 20, this.unknown, 0, 4 * 4);
    }

    public static int length() {
        return STRUCTSIZE;
    }

    /** */
    public final long getAlignment() {
        return Util.unsign(getRawAlignment());
    }

    /** Must be 'Bud1' in ASCII. */
    public final short[] getSignature() {
        return Util.unsign(getRawSignature());
    }

    /** Must be equal to rootBlockOffset2. */
    public final long getRootBlockOffset1() {
        return Util.unsign(getRawRootBlockOffset1());
    }

    /** */
    public final long getRootBlockSize() {
        return Util.unsign(getRawRootBlockSize());
    }

    /** Must be equal to rootBlockOffset1. */
    public final long getRootBlockOffset2() {
        return Util.unsign(getRawRootBlockOffset2());
    }

    /** */
    public final int[] getUnknown() {
        return Util.readIntArrayBE(this.unknown);
    }

    /**
     * <b>Note that the return value from this function should be interpreted as
     * an unsigned integer, for instance using Util.unsign(...).</b>
     */
    public final int getRawAlignment() {
        return this.alignment;
    }

    /**
     * <b>Note that the return value from this function should be interpreted as
     * an unsigned integer, for instance using Util.unsign(...).</b>
     */
    public final byte[] getRawSignature() {
        return Util.toByteArrayBE(this.signature);
    }

    /**
     * <b>Note that the return value from this function should be interpreted as
     * an unsigned integer, for instance using Util.unsign(...).</b>
     */
    public final int getRawRootBlockOffset1() {
        return this.rootBlockOffset1;
    }

    /**
     * <b>Note that the return value from this function should be interpreted as
     * an unsigned integer, for instance using Util.unsign(...).</b>
     */
    public final int getRawRootBlockSize() {
        return this.rootBlockSize;
    }

    /**
     * <b>Note that the return value from this function should be interpreted as
     * an unsigned integer, for instance using Util.unsign(...).</b>
     */
    public final int getRawRootBlockOffset2() {
        return this.rootBlockOffset2;
    }

    public void printFields(PrintStream ps, String prefix) {
        ps.println(prefix + " alignment: " + getAlignment());
        ps.println(prefix + " signature: " + Util.toASCIIString(getRawSignature()) + " " +
                "(0x" + Util.byteArrayToHexString(getRawSignature()) + ")");
        ps.println(prefix + " rootBlockOffset1: " + getRootBlockOffset1());
        ps.println(prefix + " rootBlockSize: " + getRootBlockSize());
        ps.println(prefix + " rootBlockOffset2: " + getRootBlockOffset2());
        ps.println(prefix + " unknown:");
        {
            int[] _unknown = getUnknown();
            for (int _i = 0; _i < 4; ++_i) {
                ps.println(prefix + "   0x" + Util.toHexStringBE(_unknown[_i]));
            }
        }
    }

    public void print(PrintStream ps, String prefix) {
        ps.println(prefix + "DSStoreHeader:");
        printFields(ps, prefix);
    }

    public byte[] getBytes() {
        byte[] result = new byte[length()];
        getBytes(result, 0);
        return result;
    }

    public int getBytes(byte[] result, int offset) {
        final int startOffset = offset;

        Util.arrayPutBE(result, offset, this.alignment);
        offset += 4;
        Util.arrayPutBE(result, offset, this.signature);
        offset += 4;
        Util.arrayPutBE(result, offset, this.rootBlockOffset1);
        offset += 4;
        Util.arrayPutBE(result, offset, this.rootBlockSize);
        offset += 4;
        Util.arrayPutBE(result, offset, this.rootBlockOffset2);
        offset += 4;
        System.arraycopy(this.unknown, 0, result, offset, this.unknown.length);
        offset += this.unknown.length;

        return offset - startOffset;
    }
}
