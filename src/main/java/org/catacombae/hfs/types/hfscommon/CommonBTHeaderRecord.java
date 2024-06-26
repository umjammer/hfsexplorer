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

package org.catacombae.hfs.types.hfscommon;

import java.io.PrintStream;

import org.catacombae.csjc.PrintableStruct;
import org.catacombae.csjc.StructElements;
import org.catacombae.csjc.structelements.Dictionary;
import org.catacombae.hfs.types.hfs.BTHdrRec;
import org.catacombae.hfs.types.hfsplus.BTHeaderRec;
import org.catacombae.util.Util;


/**
 * @author <a href="https://catacombae.org" target="_top">Erik Larsson</a>
 */
public abstract class CommonBTHeaderRecord extends CommonBTRecord implements PrintableStruct, StructElements {

    public enum CompareType {
        CASE_FOLDING, BINARY_COMPARE
    }

    public abstract int getTreeDepth();

    public abstract long getRootNodeNumber();

    public abstract long getNumberOfLeafRecords();

    public abstract long getFirstLeafNodeNumber();

    public abstract long getLastLeafNodeNumber();

    public abstract int getNodeSize();

    public abstract int getMaximumKeyLength();

    public abstract long getTotalNodes();

    public abstract long getFreeNodes();

    public abstract CompareType getKeyCompareType();

    @Override
    public abstract byte[] getBytes();

//    public void print(PrintStream ps, String prefix) {
//        ps.println(prefix + getClass().getSimpleName() + ":");
//        printFields(ps, prefix + " ");
//    }

    public static CommonBTHeaderRecord create(BTHeaderRec bthr) {
        return new HFSPlusImplementation(bthr);
    }

    public static CommonBTHeaderRecord create(BTHdrRec bthr) {
        return new HFSImplementation(bthr);
    }

    public static class HFSPlusImplementation extends CommonBTHeaderRecord {

        private final BTHeaderRec bthr;

        public HFSPlusImplementation(BTHeaderRec bthr) {
            this.bthr = bthr;
        }

        @Override
        public int getTreeDepth() {
            return Util.unsign(bthr.getTreeDepth());
        }

        @Override
        public long getRootNodeNumber() {
            return Util.unsign(bthr.getRootNode());
        }

        @Override
        public long getNumberOfLeafRecords() {
            return Util.unsign(bthr.getLeafRecords());
        }

        @Override
        public long getFirstLeafNodeNumber() {
            return Util.unsign(bthr.getFirstLeafNode());
        }

        @Override
        public long getLastLeafNodeNumber() {
            return Util.unsign(bthr.getLastLeafNode());
        }

        @Override
        public int getNodeSize() {
            return Util.unsign(bthr.getNodeSize());
        }

        @Override
        public int getMaximumKeyLength() {
            return Util.unsign(bthr.getMaxKeyLength());
        }

        @Override
        public long getTotalNodes() {
            return Util.unsign(bthr.getTotalNodes());
        }

        @Override
        public long getFreeNodes() {
            return Util.unsign(bthr.getFreeNodes());
        }

        @Override
        public CompareType getKeyCompareType() {
            if (bthr.getKeyCompareType() == BTHeaderRec.kHFSBinaryCompare)
                return CompareType.BINARY_COMPARE;
            else if (bthr.getKeyCompareType() == BTHeaderRec.kHFSCaseFolding)
                return CompareType.CASE_FOLDING;
            else
                throw new RuntimeException("Unknown key compare type!");
        }

        @Override
        public byte[] getBytes() {
            return bthr.getBytes();
        }

        @Override
        public void print(PrintStream ps, String prefix) {
            bthr.print(ps, prefix);
        }

        @Override
        public void printFields(PrintStream ps, String prefix) {
            bthr.printFields(ps, prefix);
        }

        @Override
        public int getSize() {
            return BTHeaderRec.length();
        }

        public BTHeaderRec getInternal() {
            return bthr;
        }

        @Override
        public Dictionary getStructElements() {
            return bthr.getStructElements();
        }
    }

    public static class HFSImplementation extends CommonBTHeaderRecord {

        private final BTHdrRec bthr;

        public HFSImplementation(BTHdrRec bthr) {
            this.bthr = bthr;
        }

        @Override
        public int getTreeDepth() {
            return Util.unsign(bthr.getBthDepth());
        }

        @Override
        public long getRootNodeNumber() {
            return Util.unsign(bthr.getBthRoot());
        }

        @Override
        public long getNumberOfLeafRecords() {
            return Util.unsign(bthr.getBthNRecs());
        }

        @Override
        public long getFirstLeafNodeNumber() {
            return Util.unsign(bthr.getBthFNode());
        }

        @Override
        public long getLastLeafNodeNumber() {
            return Util.unsign(bthr.getBthLNode());
        }

        @Override
        public int getNodeSize() {
            return Util.unsign(bthr.getBthNodeSize());
        }

        @Override
        public int getMaximumKeyLength() {
            return Util.unsign(bthr.getBthKeyLen());
        }

        @Override
        public long getTotalNodes() {
            return Util.unsign(bthr.getBthNNodes());
        }

        @Override
        public long getFreeNodes() {
            return Util.unsign(bthr.getBthFree());
        }

        @Override
        public CompareType getKeyCompareType() {
            // Couldn't find any info, but it seems this is correct.
            return CompareType.BINARY_COMPARE;
        }

        @Override
        public byte[] getBytes() {
            return bthr.getBytes();
        }

        @Override
        public void print(PrintStream ps, String prefix) {
            bthr.print(ps, prefix);
        }

        @Override
        public void printFields(PrintStream ps, String prefix) {
            bthr.printFields(ps, prefix);
        }

        @Override
        public int getSize() {
            return BTHdrRec.length();
        }

        @Override
        public Dictionary getStructElements() {
            return bthr.getStructElements();
        }
    }
}
