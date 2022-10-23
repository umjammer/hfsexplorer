/*-
 * Copyright (C) 2006-2009 Erik Larsson
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

package org.catacombae.hfs.plus;

import org.catacombae.hfs.io.ForkFilter;
import org.catacombae.io.ReadableRandomAccessSubstream;
import org.catacombae.io.SynchronizedReadableRandomAccess;
import org.catacombae.hfs.types.hfscommon.CommonBTHeaderNode;
import org.catacombae.hfs.types.hfscommon.CommonBTHeaderRecord;
import org.catacombae.hfs.types.hfscommon.CommonBTNodeDescriptor;
import org.catacombae.hfs.types.hfscommon.CommonHFSCatalogIndexNode;
import org.catacombae.hfs.types.hfscommon.CommonHFSCatalogKey;
import org.catacombae.hfs.types.hfscommon.CommonHFSCatalogLeafNode;
import org.catacombae.hfs.types.hfscommon.CommonHFSCatalogLeafRecord;
import org.catacombae.hfs.types.hfscommon.CommonHFSCatalogNodeID;
import org.catacombae.hfs.types.hfscommon.CommonHFSCatalogNodeID.ReservedID;
import org.catacombae.hfs.types.hfscommon.CommonHFSCatalogString;
import org.catacombae.hfs.types.hfscommon.CommonHFSExtentIndexNode;
import org.catacombae.hfs.types.hfscommon.CommonHFSExtentKey;
import org.catacombae.hfs.types.hfscommon.CommonHFSExtentLeafNode;
import org.catacombae.hfs.types.hfscommon.CommonHFSForkData;
import org.catacombae.hfs.types.hfscommon.CommonHFSForkType;
import org.catacombae.hfs.types.hfscommon.CommonHFSVolumeHeader;
import org.catacombae.hfs.types.hfsplus.BTHeaderRec;
import org.catacombae.hfs.types.hfsplus.BTNodeDescriptor;
import org.catacombae.hfs.types.hfsplus.HFSCatalogNodeID;
import org.catacombae.hfs.types.hfsplus.HFSPlusCatalogKey;
import org.catacombae.hfs.types.hfsplus.HFSPlusExtentKey;
import org.catacombae.hfs.types.hfsplus.HFSPlusVolumeHeader;
import org.catacombae.hfs.types.hfsplus.HFSUniStr255;
import org.catacombae.io.Readable;
import org.catacombae.io.ReadableRandomAccessStream;
import org.catacombae.hfs.AllocationFile;
import org.catacombae.hfs.AttributesFile;
import org.catacombae.hfs.HFSVolume;
import org.catacombae.hfs.HotFilesFile;
import org.catacombae.hfs.Journal;
import org.catacombae.util.Util;

/**
 * @author <a href="https://catacombae.org" target="_top">Erik Larsson</a>
 */
public class HFSPlusVolume extends HFSVolume {
    private static final CommonHFSCatalogString EMPTY_STRING =
            CommonHFSCatalogString.createHFSPlus(new HFSUniStr255(""));

    private final HFSPlusAllocationFile allocationFile;
    private final HFSPlusJournal journal;
    private final AttributesFile attributesFile;

    public HFSPlusVolume(ReadableRandomAccessStream hfsFile,
            boolean cachingEnabled) {
        this(hfsFile, cachingEnabled, HFSPlusVolumeHeader.SIGNATURE_HFS_PLUS);
    }

    protected HFSPlusVolume(ReadableRandomAccessStream hfsFile,
            boolean cachingEnabled, short volumeHeaderSignature)
    {
        super(hfsFile, cachingEnabled);

        final HFSPlusVolumeHeader volumeHeader = getHFSPlusVolumeHeader();
        if(volumeHeader.getSignature() != volumeHeaderSignature) {
            throw new RuntimeException("Invalid volume header signature " +
                    "(expected: 0x" +
                    Util.toHexStringBE(volumeHeaderSignature) + " actual: 0x" +
                    Util.toHexStringBE(volumeHeader.getSignature()) + ").");
        }

        this.allocationFile = createAllocationFile();
        this.journal = new HFSPlusJournal(this);

        if(volumeHeader.getAttributesFile().getExtents().
                getExtentDescriptors()[0].getBlockCount() == 0)
        {
            /* TODO: Is this even valid? */
            this.attributesFile = null;
        }
        else {
            this.attributesFile = new AttributesFile(this);
        }
    }

    SynchronizedReadableRandomAccess getBackingStream() {
        return hfsFile;
    }

    public final HFSPlusVolumeHeader getHFSPlusVolumeHeader() {
        //System.err.println("getHFSPlusVolumeHeader()");
	byte[] currentBlock = new byte[512];
        //System.err.println("  hfsFile.seek(" + (fsOffset + 1024) + ")");
        //System.err.println("  hfsFile.read(byte[" + currentBlock.length +
        //        "])");
	hfsFile.readFrom(1024, currentBlock);
        return new HFSPlusVolumeHeader(currentBlock);
    }

    @Override
    public CommonHFSVolumeHeader getVolumeHeader() {
        return CommonHFSVolumeHeader.create(getHFSPlusVolumeHeader());
    }

    private HFSPlusAllocationFile createAllocationFile() {
        HFSPlusVolumeHeader vh = getHFSPlusVolumeHeader();

        CommonHFSForkData allocationFileFork =
                CommonHFSForkData.create(vh.getAllocationFile());

        ForkFilter allocationFileStream = new ForkFilter(
                ForkFilter.ForkType.DATA,
                getCommonHFSCatalogNodeID(ReservedID.ALLOCATION_FILE).toLong(),
                allocationFileFork,
                extentsOverflowFile,
                new ReadableRandomAccessSubstream(hfsFile),
                0, Util.unsign(vh.getBlockSize()), 0);

        return new HFSPlusAllocationFile(this, allocationFileStream);
    }

    public AllocationFile getAllocationFile() {
        return allocationFile;
    }

    @Override
    public boolean hasAttributesFile() {
        return attributesFile != null;
    }

    @Override
    public AttributesFile getAttributesFile() {
        return attributesFile;
    }

    @Override
    public boolean hasJournal() {
        return getHFSPlusVolumeHeader().getAttributeVolumeJournaled();
    }

    @Override
    public Journal getJournal() {
        if(hasJournal())
            return journal;
        else
            return null;
    }

    @Override
    public boolean hasHotFilesFile() {
        return false; // TODO
    }

    @Override
    public HotFilesFile getHotFilesFile() {
        return null; // TODO
    }

    @Override
    public CommonHFSCatalogNodeID getCommonHFSCatalogNodeID(
            ReservedID requestedNodeID) {

        return CommonHFSCatalogNodeID.getHFSPlusReservedID(requestedNodeID);
    }

    @Override
    public CommonHFSCatalogNodeID createCommonHFSCatalogNodeID(int cnid)
    {
        return CommonHFSCatalogNodeID.create(new HFSCatalogNodeID(cnid));
    }

    @Override
    public CommonHFSExtentKey createCommonHFSExtentKey(boolean isResource,
            int cnid, long startBlock)
    {
        if(startBlock > 0xFFFFFFFFL) {
            throw new IllegalArgumentException("Value of 'startBlock' is too " +
                    "large for an HFS+ extent key.");
        }

        return CommonHFSExtentKey.create(new HFSPlusExtentKey(
                isResource ? HFSPlusExtentKey.RESOURCE_FORK :
                    HFSPlusExtentKey.DATA_FORK,
                new HFSCatalogNodeID(cnid),
                (int) startBlock));
    }

    @Override
    public CommonHFSCatalogString getEmptyString() {
        return EMPTY_STRING;
    }

    @Override
    public String decodeString(CommonHFSCatalogString str) {
        if(str instanceof CommonHFSCatalogString.HFSPlusImplementation) {
            CommonHFSCatalogString.HFSPlusImplementation hStr =
                    (CommonHFSCatalogString.HFSPlusImplementation)str;
            return new String(hStr.getInternal().getUnicode());
        }
        else
            throw new RuntimeException("Invalid string type: " +
                    str.getClass());
    }

    @Override
    public CommonHFSCatalogString encodeString(String str) {
        return CommonHFSCatalogString.HFSPlusImplementation.createHFSPlus(
                new HFSUniStr255(str));
    }

    @Override
    public void close() {
        allocationFile.close();
        super.close();
    }

    public CommonBTHeaderNode createCommonBTHeaderNode(byte[] currentNodeData,
            int offset, int nodeSize)
    {
        return CommonBTHeaderNode.createHFSPlus(currentNodeData, offset,
                nodeSize);
    }

    public CommonBTNodeDescriptor readNodeDescriptor(Readable rd) {
        byte[] data = new byte[BTNodeDescriptor.length()];
        rd.readFully(data);
        final BTNodeDescriptor btnd = new BTNodeDescriptor(data, 0);

        return CommonBTNodeDescriptor.create(btnd);
    }

    public CommonBTHeaderRecord readHeaderRecord(Readable rd) {
        byte[] data = new byte[BTHeaderRec.length()];
        rd.readFully(data);
        BTHeaderRec bthr = new BTHeaderRec(data, 0);

        return CommonBTHeaderRecord.create(bthr);
    }

    public CommonBTNodeDescriptor createCommonBTNodeDescriptor(
            byte[] currentNodeData, int offset)
    {
        final BTNodeDescriptor btnd =
                new BTNodeDescriptor(currentNodeData, offset);
        return CommonBTNodeDescriptor.create(btnd);
    }

    public CommonHFSCatalogIndexNode newCatalogIndexNode(byte[] data,
            int offset, int nodeSize)
    {
        return CommonHFSCatalogIndexNode.createHFSPlus(data, offset, nodeSize);
    }

    public CommonHFSCatalogKey newCatalogKey(CommonHFSCatalogNodeID nodeID,
            CommonHFSCatalogString searchString)
    {
        return CommonHFSCatalogKey.create(new HFSPlusCatalogKey(
                new HFSCatalogNodeID((int)nodeID.toLong()),
                new HFSUniStr255(searchString.getStructBytes(), 0)));
    }

    public CommonHFSCatalogLeafNode newCatalogLeafNode(byte[] data, int offset,
            int nodeSize)
    {
        return CommonHFSCatalogLeafNode.createHFSPlus(data, offset, nodeSize);
    }

    public CommonHFSCatalogLeafRecord newCatalogLeafRecord(byte[] data,
            int offset)
    {
        return CommonHFSCatalogLeafRecord.createHFSPlus(data, offset,
                data.length - offset);
    }

    public CommonHFSExtentIndexNode createCommonHFSExtentIndexNode(
            byte[] currentNodeData, int offset, int nodeSize)
    {
        return CommonHFSExtentIndexNode.createHFSPlus(currentNodeData, offset,
                nodeSize);
    }

    public CommonHFSExtentLeafNode createCommonHFSExtentLeafNode(
            byte[] currentNodeData, int offset, int nodeSize)
    {
        return CommonHFSExtentLeafNode.createHFSPlus(currentNodeData, offset,
                nodeSize);
    }

    public CommonHFSExtentKey createCommonHFSExtentKey(
            CommonHFSForkType forkType, CommonHFSCatalogNodeID fileID,
            int startBlock) {

        final byte forkTypeByte;
        switch(forkType) {
            case DATA_FORK:
                forkTypeByte = HFSPlusExtentKey.DATA_FORK;
                break;
            case RESOURCE_FORK:
                forkTypeByte = HFSPlusExtentKey.RESOURCE_FORK;
                break;
            default:
                throw new RuntimeException("Invalid fork type");
        }

        HFSPlusExtentKey key = new HFSPlusExtentKey(forkTypeByte,
                new HFSCatalogNodeID((int) fileID.toLong()), startBlock);
        return CommonHFSExtentKey.create(key);
    }
}
