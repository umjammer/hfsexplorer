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

import org.catacombae.hfs.types.hfsplus.HFSPlusExtentKey;
import org.catacombae.hfs.types.hfsplus.HFSPlusExtentRecord;
import org.catacombae.hfs.types.hfs.ExtDataRec;
import org.catacombae.hfs.types.hfs.ExtKeyRec;

/**
 * @author <a href="https://catacombae.org" target="_top">Erik Larsson</a>
 */
public abstract class CommonHFSExtentLeafNode
        extends CommonBTKeyedNode<CommonHFSExtentLeafRecord>
{

    protected CommonHFSExtentLeafNode(byte[] data, int offset, int nodeSize, FSType type) {
        super(data, offset, nodeSize, type);
    }

    public CommonHFSExtentLeafRecord[] getLeafRecords() {
        return ic.records.toArray(new CommonHFSExtentLeafRecord[ic.records.size()]);
    }

    public static CommonHFSExtentLeafNode createHFS(byte[] data, int offset, int nodeSize) {
        return new HFSImplementation(data, offset, nodeSize);
    }

    public static CommonHFSExtentLeafNode createHFSPlus(byte[] data, int offset, int nodeSize) {
        return new HFSPlusImplementation(data, offset, nodeSize);
    }

    public static class HFSPlusImplementation extends CommonHFSExtentLeafNode {
        public HFSPlusImplementation(byte[] data, int offset, int nodeSize) {
            super(data, offset, nodeSize, FSType.HFS_PLUS);
        }

        @Override
        protected CommonHFSExtentLeafRecord createBTRecord(int recordNumber, byte[] data, int offset, int length) {
            final HFSPlusExtentKey key = new HFSPlusExtentKey(data, offset);
            final HFSPlusExtentRecord recordData = new HFSPlusExtentRecord(data, offset+key.length());

            return CommonHFSExtentLeafRecord.create(key, recordData);
        }
    }

    public static class HFSImplementation extends CommonHFSExtentLeafNode {
        public HFSImplementation(byte[] data, int offset, int nodeSize) {
            super(data, offset, nodeSize, FSType.HFS);
        }

        @Override
        protected CommonHFSExtentLeafRecord createBTRecord(int recordNumber, byte[] data, int offset, int length) {
            final ExtKeyRec key = new ExtKeyRec(data, offset);
            final ExtDataRec recordData = new ExtDataRec(data, offset+key.length());

            return CommonHFSExtentLeafRecord.create(key, recordData);
        }
    }
}
