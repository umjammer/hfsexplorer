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

import org.catacombae.csjc.PrintableStruct;
import org.catacombae.csjc.StructElements;
import org.catacombae.csjc.structelements.Dictionary;
import org.catacombae.util.Util;


/**
 * This class was generated by CStructToJavaClass.
 *
 * @author <a href="https://catacombae.org" target="_top">Erik Larsson</a>
 */
public class BTHdrRec implements PrintableStruct, StructElements {
    /*
     * struct BTHdrRec
     * size: 106 bytes
     * description:
     *
     * BP  Size  Type       Identifier   Description
     * --------------------------------------------------------------------------
     * 0   2     SInt16     bthDepth     current depth of tree (Integer)
     * 2   4     SInt32     bthRoot      number of root node (LongInt)
     * 6   4     SInt32     bthNRecs     number of leaf records in tree (LongInt)
     * 10  4     SInt32     bthFNode     number of first leaf node (LongInt)
     * 14  4     SInt32     bthLNode     number of last leaf node (LongInt)
     * 18  2     SInt16     bthNodeSize  size of a node (Integer)
     * 20  2     SInt16     bthKeyLen    maximum length of a key (Integer)
     * 22  4     SInt32     bthNNodes    total number of nodes in tree (LongInt)
     * 26  4     SInt32     bthFree      number of free nodes (LongInt)
     * 30  1*76  SInt8[76]  bthResv      reserved (ARRAY[1..76] OF SignedByte)
     */

    public static final int STRUCTSIZE = 106;

    private final byte[] bthDepth = new byte[2];
    private final byte[] bthRoot = new byte[4];
    private final byte[] bthNRecs = new byte[4];
    private final byte[] bthFNode = new byte[4];
    private final byte[] bthLNode = new byte[4];
    private final byte[] bthNodeSize = new byte[2];
    private final byte[] bthKeyLen = new byte[2];
    private final byte[] bthNNodes = new byte[4];
    private final byte[] bthFree = new byte[4];
    private final byte[] bthResv = new byte[1 * 76];

    public BTHdrRec(byte[] data, int offset) {
        System.arraycopy(data, offset + 0, bthDepth, 0, 2);
        System.arraycopy(data, offset + 2, bthRoot, 0, 4);
        System.arraycopy(data, offset + 6, bthNRecs, 0, 4);
        System.arraycopy(data, offset + 10, bthFNode, 0, 4);
        System.arraycopy(data, offset + 14, bthLNode, 0, 4);
        System.arraycopy(data, offset + 18, bthNodeSize, 0, 2);
        System.arraycopy(data, offset + 20, bthKeyLen, 0, 2);
        System.arraycopy(data, offset + 22, bthNNodes, 0, 4);
        System.arraycopy(data, offset + 26, bthFree, 0, 4);
        System.arraycopy(data, offset + 30, bthResv, 0, 1 * 76);
    }

    public static int length() {
        return STRUCTSIZE;
    }

    /** current depth of tree (Integer) */
    public short getBthDepth() {
        return Util.readShortBE(bthDepth);
    }

    /** number of root node (LongInt) */
    public int getBthRoot() {
        return Util.readIntBE(bthRoot);
    }

    /** number of leaf records in tree (LongInt) */
    public int getBthNRecs() {
        return Util.readIntBE(bthNRecs);
    }

    /** number of first leaf node (LongInt) */
    public int getBthFNode() {
        return Util.readIntBE(bthFNode);
    }

    /** number of last leaf node (LongInt) */
    public int getBthLNode() {
        return Util.readIntBE(bthLNode);
    }

    /** size of a node (Integer) */
    public short getBthNodeSize() {
        return Util.readShortBE(bthNodeSize);
    }

    /** maximum length of a key (Integer) */
    public short getBthKeyLen() {
        return Util.readShortBE(bthKeyLen);
    }

    /** total number of nodes in tree (LongInt) */
    public int getBthNNodes() {
        return Util.readIntBE(bthNNodes);
    }

    /** number of free nodes (LongInt) */
    public int getBthFree() {
        return Util.readIntBE(bthFree);
    }

    /** reserved (ARRAY[1..76] OF SignedByte) */
    public byte[] getBthResv() {
        return Util.readByteArrayBE(bthResv);
    }

    @Override
    public void printFields(PrintStream ps, String prefix) {
        ps.println(prefix + " bthDepth: " + getBthDepth());
        ps.println(prefix + " bthRoot: " + getBthRoot());
        ps.println(prefix + " bthNRecs: " + getBthNRecs());
        ps.println(prefix + " bthFNode: " + getBthFNode());
        ps.println(prefix + " bthLNode: " + getBthLNode());
        ps.println(prefix + " bthNodeSize: " + getBthNodeSize());
        ps.println(prefix + " bthKeyLen: " + getBthKeyLen());
        ps.println(prefix + " bthNNodes: " + getBthNNodes());
        ps.println(prefix + " bthFree: " + getBthFree());
        ps.println(prefix + " bthResv: " + getBthResv());
    }

    @Override
    public void print(PrintStream ps, String prefix) {
        ps.println(prefix + "BTHdrRec:");
        printFields(ps, prefix);
    }

    public byte[] getBytes() {
        byte[] result = new byte[STRUCTSIZE];
        int offset = 0;
        System.arraycopy(bthDepth, 0, result, offset, bthDepth.length);
        offset += bthDepth.length;
        System.arraycopy(bthRoot, 0, result, offset, bthRoot.length);
        offset += bthRoot.length;
        System.arraycopy(bthNRecs, 0, result, offset, bthNRecs.length);
        offset += bthNRecs.length;
        System.arraycopy(bthFNode, 0, result, offset, bthFNode.length);
        offset += bthFNode.length;
        System.arraycopy(bthLNode, 0, result, offset, bthLNode.length);
        offset += bthLNode.length;
        System.arraycopy(bthNodeSize, 0, result, offset, bthNodeSize.length);
        offset += bthNodeSize.length;
        System.arraycopy(bthKeyLen, 0, result, offset, bthKeyLen.length);
        offset += bthKeyLen.length;
        System.arraycopy(bthNNodes, 0, result, offset, bthNNodes.length);
        offset += bthNNodes.length;
        System.arraycopy(bthFree, 0, result, offset, bthFree.length);
        offset += bthFree.length;
        System.arraycopy(bthResv, 0, result, offset, bthResv.length);
        offset += bthResv.length;
        return result;
    }

    @Override
    public Dictionary getStructElements() {
        DictionaryBuilder db = new DictionaryBuilder(BTHdrRec.class.getSimpleName());

        db.addUIntBE("bthDepth", bthDepth, "Depth");
        db.addUIntBE("bthRoot", bthRoot, "Root node");
        db.addUIntBE("bthNRecs", bthNRecs, "Number of leaf records");
        db.addUIntBE("bthFNode", bthFNode, "First leaf node");
        db.addUIntBE("bthLNode", bthLNode, "Last leaf node");
        db.addUIntBE("bthNodeSize", bthNodeSize, "Node size");
        db.addUIntBE("bthKeyLen", bthKeyLen, "Maximum key length");
        db.addUIntBE("bthNNodes", bthNNodes, "Total number of nodes");
        db.addUIntBE("bthFree", bthFree, "Number of free nodes");

        // No need to add 'bthResv' to dictionary because it's not a field that
        // we want to expose.

        return db.getResult();
    }
}
