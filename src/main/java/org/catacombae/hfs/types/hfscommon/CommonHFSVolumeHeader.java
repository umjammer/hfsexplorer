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
import java.util.Date;

import org.catacombae.csjc.PrintableStruct;
import org.catacombae.csjc.StructElements;
import org.catacombae.csjc.structelements.Dictionary;
import org.catacombae.util.Util;
import org.catacombae.hfs.types.hfsplus.HFSPlusVolumeHeader;
import org.catacombae.hfs.types.hfs.MasterDirectoryBlock;


/**
 * This class acts as a generalization of the common data properties of the
 * volume headers of all versions of HFS.
 *
 * @author <a href="https://catacombae.org" target="_top">Erik Larsson</a>
 */
public abstract class CommonHFSVolumeHeader implements PrintableStruct, StructElements {

    public abstract short getSignature();

    /**
     * Returns the <b>physical</b> block number of the first allocation block
     * (allocation block 0). Since it's a physical block number, it has to be
     * multiplied with the physical block size (usually 512) and <b>not</b>
     * the allocation block size.
     *
     * @return the physical block number of the first allocation block.
     */
    public abstract long getAllocationBlockStart();

    public abstract long getAllocationBlockSize();

    /**
     * Returns a byte pointer (not block) to the end of the volume, that is,
     * a pointer to byte following the last byte of the volume. You could call
     * this method getFileSystemLength if you wanted to.
     *
     * @return a byte pointer to the end of the volume.
     */
    public abstract long getFileSystemEnd();

    public abstract long getTotalBlocks();

    public abstract long getFreeBlocks();

    public abstract Date getCreateDate();

    public abstract Date getModifyDate();

    public abstract Date getBackupDate();

    public abstract CommonHFSCatalogNodeID getNextCatalogNodeID();

//    public abstract long getCatalogFileSize();

    public abstract CommonHFSForkData getCatalogFile();

//    public abstract long getExtentsOverflowFileSize();

    public abstract CommonHFSForkData getExtentsOverflowFile();

    /**
     * Get the fork data of the allocation file.
     * <p>
     * The allocation file only exists in HFS+ and HFSX. For HFS file systems
     * this function will return <code>null</code>.
     *
     * @return the initial fork data of the allocation file if the allocation
     * file exists, or <code>null</code> otherwise.
     */
    public abstract CommonHFSForkData getAllocationFile();

    /**
     * Get the fork data of the attributes file.
     * <p>
     * The attributes file only exists in HFS+ and HFSX. For HFS file systems
     * this function will return <code>null</code>.
     *
     * @return the initial fork data of the attributes file if the attributes
     * file exists, or <code>null</code> otherwise.
     */
    public abstract CommonHFSForkData getAttributesFile();

    /**
     * Get the fork data of the startup file.
     * <p>
     * The startup file only exists in HFS+ and HFSX. For HFS file systems this
     * function will return <code>null</code>.
     *
     * @return the initial fork data of the startup file if the startup file
     * exists, or <code>null</code> otherwise.
     */
    public abstract CommonHFSForkData getStartupFile();

    public static CommonHFSVolumeHeader create(HFSPlusVolumeHeader hdr) {
        return new HFSPlusImplementation(hdr);
    }

    public static CommonHFSVolumeHeader create(MasterDirectoryBlock hdr) {
        return new HFSImplementation(hdr);
    }

    /**
     * Validates the data of this volume header.
     *
     * @return whether or not this volume header has valid data.
     */
    public abstract boolean isValid();

    public static class HFSPlusImplementation extends CommonHFSVolumeHeader {

        private final HFSPlusVolumeHeader hdr;

        public HFSPlusImplementation(HFSPlusVolumeHeader hdr) {
            this.hdr = hdr;
        }

        @Deprecated
        public HFSPlusVolumeHeader getUnderlying() {
            return hdr;
        }

        @Override
        public short getSignature() {
            return hdr.getSignature();
        }

        @Override
        public long getAllocationBlockSize() {
            return Util.unsign(hdr.getBlockSize());
        }

        @Override
        public long getTotalBlocks() {
            return Util.unsign(hdr.getTotalBlocks());
        }

        @Override
        public long getFreeBlocks() {
            return Util.unsign(hdr.getFreeBlocks());
        }

        @Override
        public Date getCreateDate() {
            return hdr.getCreateDateAsDate();
        }

        @Override
        public Date getModifyDate() {
            return hdr.getModifyDateAsDate();
        }

        @Override
        public Date getBackupDate() {
            return hdr.getBackupDateAsDate();
        }

        @Override
        public CommonHFSCatalogNodeID getNextCatalogNodeID() {
            return CommonHFSCatalogNodeID.create(hdr.getNextCatalogID());
        }

        @Override
        public CommonHFSForkData getCatalogFile() {
            return CommonHFSForkData.create(hdr.getCatalogFile());
        }

        @Override
        public CommonHFSForkData getExtentsOverflowFile() {
            return CommonHFSForkData.create(hdr.getExtentsFile());
        }

        @Override
        public CommonHFSForkData getAllocationFile() {
            return CommonHFSForkData.create(hdr.getAllocationFile());
        }

        @Override
        public CommonHFSForkData getAttributesFile() {
            return CommonHFSForkData.create(hdr.getAttributesFile());
        }

        @Override
        public CommonHFSForkData getStartupFile() {
            return CommonHFSForkData.create(hdr.getStartupFile());
        }

        @Override
        public void print(PrintStream err, String prefix) {
            hdr.print(err, prefix);
        }

        @Override
        public void printFields(PrintStream err, String prefix) {
            hdr.printFields(err, prefix);
        }

        @Override
        public long getAllocationBlockStart() {
            /*
             * HFS+ volumes are completely mapped by allocation blocks, from the
             * start of the volume. Thus the first allocation block starts at
             * the first physical block (block 0).
             */
            return 0;
        }

        @Override
        public Dictionary getStructElements() {
            return hdr.getStructElements();
        }

        @Override
        public long getFileSystemEnd() {
            // In HFS+, the entire volume is mapped by allocation blocks.
            return getTotalBlocks() * getAllocationBlockSize();
        }

        @Override
        public boolean isValid() {
            return hdr.isValid();
        }

        public long getJournalInfoBlock() {
            return Util.unsign(hdr.getJournalInfoBlock());
        }
    }

    public static class HFSImplementation extends CommonHFSVolumeHeader {

        private final MasterDirectoryBlock hdr;

        public HFSImplementation(MasterDirectoryBlock hdr) {
            this.hdr = hdr;
        }

        @Override
        public short getSignature() {
            return hdr.getDrSigWord();
        }

        @Override
        public long getAllocationBlockSize() {
            return Util.unsign(hdr.getDrAlBlkSiz());
        }

        @Override
        public long getTotalBlocks() {
            return Util.unsign(hdr.getDrNmAlBlks());
        }

        @Override
        public long getFreeBlocks() {
            return Util.unsign(hdr.getDrFreeBks());
        }

        @Override
        public Date getCreateDate() {
            return hdr.getDrCrDateAsDate();
        }

        @Override
        public Date getModifyDate() {
            return hdr.getDrLsModAsDate();
        }

        @Override
        public Date getBackupDate() {
            return hdr.getDrVolBkUpAsDate();
        }

        @Override
        public CommonHFSCatalogNodeID getNextCatalogNodeID() {
            return CommonHFSCatalogNodeID.create(hdr.getDrNxtCNID());
        }

        @Override
        public CommonHFSForkData getCatalogFile() {
            return CommonHFSForkData.create(hdr.getDrCTExtRec(),
                    Util.unsign(hdr.getDrCTFlSize()));
        }

        @Override
        public CommonHFSForkData getExtentsOverflowFile() {
            return CommonHFSForkData.create(hdr.getDrXTExtRec(),
                    Util.unsign(hdr.getDrXTFlSize()));
        }

        @Override
        public CommonHFSForkData getAllocationFile() {
            return null;
        }

        @Override
        public CommonHFSForkData getAttributesFile() {
            return null;
        }

        @Override
        public CommonHFSForkData getStartupFile() {
            return null;
        }

        @Override
        public void print(PrintStream err, String prefix) {
            hdr.print(err, prefix);
        }

        @Override
        public void printFields(PrintStream err, String prefix) {
            hdr.printFields(err, prefix);
        }

        @Override
        public long getAllocationBlockStart() {
            return Util.unsign(hdr.getDrAlBlSt());
        }

        @Override
        public Dictionary getStructElements() {
            return hdr.getStructElements();
        }

        @Override
        public long getFileSystemEnd() {
            // In HFS, only the "data" part of the volume is mapped by allocation blocks.
            return getAllocationBlockStart() * 512 + getTotalBlocks() * getAllocationBlockSize() + 2 * 512;
        }

        @Override
        public boolean isValid() {
            return hdr.isValid();
        }
    }
}
