/*-
 * Copyright (C) 2009 Erik Larsson
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

package org.catacombae.hfs;

import java.util.LinkedList;

import org.catacombae.hfs.io.ForkFilter;
import org.catacombae.hfs.plus.HFSPlusVolume;
import org.catacombae.hfs.types.hfscommon.CommonBTHeaderNode;
import org.catacombae.hfs.types.hfscommon.CommonBTIndexRecord;
import org.catacombae.hfs.types.hfscommon.CommonBTNode;
import org.catacombae.hfs.types.hfscommon.CommonHFSAttributesIndexNode;
import org.catacombae.hfs.types.hfscommon.CommonHFSAttributesKey;
import org.catacombae.hfs.types.hfscommon.CommonHFSAttributesLeafNode;
import org.catacombae.hfs.types.hfscommon.CommonHFSAttributesLeafRecord;
import org.catacombae.hfs.types.hfscommon.CommonHFSCatalogNodeID;
import org.catacombae.hfs.types.hfscommon.CommonHFSCatalogNodeID.ReservedID;
import org.catacombae.hfs.types.hfscommon.CommonHFSVolumeHeader;
import org.catacombae.hfs.types.hfsplus.HFSPlusAttributesKey;
import org.catacombae.io.ReadableRandomAccessStream;


/**
 * @author <a href="https://catacombae.org" target="_top">Erik Larsson</a>
 */
public class AttributesFile extends BTreeFile<CommonHFSAttributesKey, CommonHFSAttributesLeafRecord> {

    private final HFSPlusVolume view;

    private class Session extends BTreeFileSession {

        ReadableRandomAccessStream attributesFileStream;

        @Override
        protected ReadableRandomAccessStream getBTreeStream(CommonHFSVolumeHeader header) {
            if (!(header instanceof CommonHFSVolumeHeader.HFSPlusImplementation)) {
                throw new RuntimeException("Illegal CommonHFSVolumeHeader " +
                        "flavour (expected HFSPlusImplementation, got " + header.getClass() + ").");
            }

            if (this.attributesFileStream == null) {
                this.attributesFileStream = getAttributesFileStream((CommonHFSVolumeHeader.HFSPlusImplementation) header);
            }

            return this.attributesFileStream;
        }
    }

    public AttributesFile(HFSPlusVolume view) {
        super(view);
        this.view = view;
    }

    private ReadableRandomAccessStream getAttributesFileStream(CommonHFSVolumeHeader.HFSPlusImplementation header) {

        return new ForkFilter(ForkFilter.ForkType.DATA,
                vol.getCommonHFSCatalogNodeID(ReservedID.ATTRIBUTES_FILE).toLong(),
                header.getAttributesFile(),
                vol.extentsOverflowFile,
                view.createFSStream(),
                0,
                header.getAllocationBlockSize(),
                header.getAllocationBlockStart() * view.getPhysicalBlockSize());
    }

    @Override
    BTreeFileSession openSession() {
        return new Session();
    }

    @Override
    protected CommonHFSAttributesIndexNode createIndexNode(byte[] nodeData, int offset, int nodeSize) {
        return CommonHFSAttributesIndexNode.createHFSPlus(nodeData, 0, nodeSize);
    }

    @Override
    protected CommonHFSAttributesLeafNode createLeafNode(byte[] nodeData, int offset, int nodeSize) {
        return CommonHFSAttributesLeafNode.createHFSPlus(nodeData, 0, nodeSize);
    }

    public CommonBTHeaderNode getHeaderNode() {
        CommonBTNode<?> firstNode = getNode(0);
        if (firstNode instanceof CommonBTHeaderNode) {
            return (CommonBTHeaderNode) firstNode;
        } else {
            throw new RuntimeException("Unexpected node type at catalog node 0: " + firstNode.getClass());
        }
    }

    public String[] listAttributeNames(CommonHFSCatalogNodeID nodeID) {
        LinkedList<String> list = new LinkedList<>();

        listAttributeNames(nodeID, list);

        return list.toArray(String[]::new);
    }

    public void listAttributeNames(CommonHFSCatalogNodeID nodeID, LinkedList<String> list) {
        LinkedList<CommonHFSAttributesLeafRecord> recordList = new LinkedList<>();

        listAttributeRecords(nodeID, recordList);

        for (CommonHFSAttributesLeafRecord r : recordList) {
            CommonHFSAttributesKey k = r.getKey();
            if (k.getStartBlock() == 0) {
                list.add(new String(k.getAttrName()));
            }
        }
    }

    public CommonHFSAttributesLeafRecord[] listAttributeRecords(CommonHFSCatalogNodeID nodeID) {
        LinkedList<CommonHFSAttributesLeafRecord> list = new LinkedList<>();

        listAttributeRecords(nodeID, list);

        return list.toArray(CommonHFSAttributesLeafRecord[]::new);
    }

    public void listAttributeRecords(CommonHFSCatalogNodeID nodeID, LinkedList<CommonHFSAttributesLeafRecord> list) {
        CommonBTNode<?> rootNode = getRootNode();
        if (rootNode != null) {
            listAttributeRecords(rootNode, nodeID, list);
        }
    }

    private void listAttributeRecords(CommonBTNode<?> curNode,
                                      CommonHFSCatalogNodeID nodeID,
                                      LinkedList<CommonHFSAttributesLeafRecord> list) {
        CommonHFSAttributesKey searchKey = CommonHFSAttributesKey.create(new HFSPlusAttributesKey(
                        ((CommonHFSCatalogNodeID.HFSPlusImplementation) nodeID).
                                getHFSCatalogNodeID(), 0, new char[0]));
        CommonHFSAttributesKey endKey = CommonHFSAttributesKey.create(new HFSPlusAttributesKey(
                        ((CommonHFSCatalogNodeID.HFSPlusImplementation)
                                nodeID.add(1)).getHFSCatalogNodeID(), 0, new char[0]));

        if (curNode instanceof CommonHFSAttributesLeafNode leafNode) {

            int listSizeBefore = list.size();
            if (!findLEKeys(leafNode, searchKey, endKey, true, list)) {
                while (list.size() > listSizeBefore) {
                    list.removeLast();
                }
            }
        } else if (curNode instanceof CommonHFSAttributesIndexNode indexNode) {
            LinkedList<CommonBTIndexRecord<CommonHFSAttributesKey>> recList = new LinkedList<>();

            // Search for all keys in index node between search key (inclusive)
            // and end key (exclusive).
            findLEKeys(indexNode, searchKey, endKey, false, recList);

            for (CommonBTIndexRecord<CommonHFSAttributesKey> rec : recList) {
                listAttributeRecords(getNode(rec.getIndex()), nodeID, list);
            }
        } else {
            throw new RuntimeException("Unexpected node type: " + curNode.getClass());
        }
    }
}
