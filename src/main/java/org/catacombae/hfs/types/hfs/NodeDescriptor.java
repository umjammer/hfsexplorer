/*-
 * Copyright (C) 2008 Erik Larsson
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

package org.catacombae.hfs.types.hfs;

import java.io.PrintStream;
import java.lang.System.Logger.Level;

import org.catacombae.csjc.PrintableStruct;
import org.catacombae.util.Util;


/**
 * This class was generated by CStructToJavaClass.
 *
 * @author <a href="https://catacombae.org" target="_top">Erik Larsson</a>
 */
public class NodeDescriptor implements PrintableStruct {
    /*
     * struct NodeDescriptor
     * size: 14 bytes
     * description:
     *
     * BP  Size  Type    Identifier  Description
     * -----------------------------------------------------------------
     * 0   4     SInt32  ndFLink     forward link (LongInt)
     * 4   4     SInt32  ndBLink     backward link (LongInt)
     * 8   1     SInt8   ndType      node type (SignedByte)
     * 9   1     SInt8   ndNHeight   node level (SignedByte)
     * 10  2     SInt16  ndNRecs     number of records in node (Integer)
     * 12  2     SInt16  ndResv2     reserved (Integer)
     */

    public static final int STRUCTSIZE = 14;
    public static final byte ndIndxNode = (byte) 0x00; // index node
    public static final byte ndHdrNode = (byte) 0x01; // header node
    public static final byte ndMapNode = (byte) 0x02; // map node
    public static final byte ndLeafNode = (byte) 0xFF; // leaf node
    private final byte[] ndFLink = new byte[4];
    private final byte[] ndBLink = new byte[4];
    private final byte[] ndType = new byte[1];
    private final byte[] ndNHeight = new byte[1];
    private final byte[] ndNRecs = new byte[2];
    private final byte[] ndResv2 = new byte[2];

    public NodeDescriptor(byte[] data, int offset) {
//        logger.log(Level.DEBUG, "NodeDescriptor(byte[" + data.length + "], " + offset + ");");
        System.arraycopy(data, offset + 0, ndFLink, 0, 4);
        System.arraycopy(data, offset + 4, ndBLink, 0, 4);
        System.arraycopy(data, offset + 8, ndType, 0, 1);
        System.arraycopy(data, offset + 9, ndNHeight, 0, 1);
        System.arraycopy(data, offset + 10, ndNRecs, 0, 2);
        System.arraycopy(data, offset + 12, ndResv2, 0, 2);
    }

    public static int length() {
        return STRUCTSIZE;
    }

    /** forward link (LongInt) */
    public int getNdFLink() {
        return Util.readIntBE(ndFLink);
    }

    /** backward link (LongInt) */
    public int getNdBLink() {
        return Util.readIntBE(ndBLink);
    }

    /** node type (SignedByte) */
    public byte getNdType() {
        return Util.readByteBE(ndType);
    }

    /** node level (SignedByte) */
    public byte getNdNHeight() {
        return Util.readByteBE(ndNHeight);
    }

    /** number of records in node (Integer) */
    public short getNdNRecs() {
        return Util.readShortBE(ndNRecs);
    }

    /** reserved (Integer) */
    public short getNdResv2() {
        return Util.readShortBE(ndResv2);
    }

    @Override
    public void printFields(PrintStream ps, String prefix) {
        ps.println(prefix + " ndFLink: " + getNdFLink());
        ps.println(prefix + " ndBLink: " + getNdBLink());
        ps.println(prefix + " ndType: " + getNdType());
        ps.println(prefix + " ndNHeight: " + getNdNHeight());
        ps.println(prefix + " ndNRecs: " + getNdNRecs());
        ps.println(prefix + " ndResv2: " + getNdResv2());
    }

    @Override
    public void print(PrintStream ps, String prefix) {
        ps.println(prefix + "NodeDescriptor:");
        printFields(ps, prefix);
    }

    public byte[] getBytes() {
        byte[] result = new byte[STRUCTSIZE];
        int offset = 0;
        System.arraycopy(ndFLink, 0, result, offset, ndFLink.length);
        offset += ndFLink.length;
        System.arraycopy(ndBLink, 0, result, offset, ndBLink.length);
        offset += ndBLink.length;
        System.arraycopy(ndType, 0, result, offset, ndType.length);
        offset += ndType.length;
        System.arraycopy(ndNHeight, 0, result, offset, ndNHeight.length);
        offset += ndNHeight.length;
        System.arraycopy(ndNRecs, 0, result, offset, ndNRecs.length);
        offset += ndNRecs.length;
        System.arraycopy(ndResv2, 0, result, offset, ndResv2.length);
        offset += ndResv2.length;
        return result;
    }
}
