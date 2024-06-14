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

import org.catacombae.hfs.types.hfsplus.HFSPlusCatalogKey;


/**
 * @author <a href="https://catacombae.org" target="_top">Erik Larsson</a>
 */
public abstract class CommonHFSCatalogLeafNode extends CommonBTKeyedNode<CommonHFSCatalogLeafRecord> {

    protected CommonHFSCatalogLeafNode(byte[] data, int offset, int nodeSize, FSType type) {
        super(data, offset, nodeSize, type);
    }

    public CommonHFSCatalogLeafRecord[] getLeafRecords() {
        return ic.records.toArray(CommonHFSCatalogLeafRecord[]::new);
    }

    public static CommonHFSCatalogLeafNode createHFSPlus(byte[] data, int offset, int nodeSize) {
        return new HFSPlusImplementation(data, offset, nodeSize);
    }

    public static CommonHFSCatalogLeafNode createHFSX(byte[] data, int offset, int nodeSize, byte keyCompareType) {
        return new HFSXImplementation(data, offset, nodeSize, keyCompareType).
                getInternal();
    }

    public static CommonHFSCatalogLeafNode createHFS(byte[] data, int offset, int nodeSize) {
        return new HFSImplementation(data, offset, nodeSize);
    }

    private static class HFSPlusImplementation extends CommonHFSCatalogLeafNode {

        public HFSPlusImplementation(byte[] data, int offset, int nodeSize) {
            super(data, offset, nodeSize, FSType.HFS_PLUS);
        }

        protected HFSPlusCatalogKey createKey(byte[] data, int offset, int length) {
            return new HFSPlusCatalogKey(data, offset);
        }

        @Override
        protected CommonHFSCatalogLeafRecord createBTRecord(int recordNumber, byte[] data, int offset, int length) {
            return CommonHFSCatalogLeafRecord.createHFSPlus(data, offset, length);
        }
    }

    public static class HFSXImplementation {

        private final byte keyCompareType;
        private final Internal internal;

        private class Internal extends CommonHFSCatalogLeafNode {

            public Internal(byte[] data, int offset, int nodeSize) {
                super(data, offset, nodeSize, FSType.HFS_PLUS);
            }

            @Override
            protected CommonHFSCatalogLeafRecord createBTRecord(int recordNumber, byte[] data, int offset, int length) {
                return CommonHFSCatalogLeafRecord.createHFSX(data, offset, length, keyCompareType);
            }
        }

        public HFSXImplementation(byte[] data, int offset, int nodeSize, byte keyCompareType) {
            this.keyCompareType = keyCompareType;
            this.internal = new Internal(data, offset, nodeSize);
        }

        private Internal getInternal() {
            return internal;
        }
    }

    public static class HFSImplementation extends CommonHFSCatalogLeafNode {

        public HFSImplementation(byte[] data, int offset, int nodeSize) {
            super(data, offset, nodeSize, FSType.HFS);
        }

        @Override
        protected CommonHFSCatalogLeafRecord createBTRecord(int recordNumber, byte[] data, int offset, int length) {
            return CommonHFSCatalogLeafRecord.createHFS(data, offset, length);
        }
    }
}
