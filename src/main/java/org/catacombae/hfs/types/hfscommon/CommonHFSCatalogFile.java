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
import org.catacombae.csjc.StaticStruct;
import org.catacombae.csjc.StructElements;
import org.catacombae.csjc.structelements.Dictionary;
import org.catacombae.hfs.types.hfs.CdrFilRec;
import org.catacombae.hfs.types.hfsplus.HFSPlusBSDInfo;
import org.catacombae.hfs.types.hfsplus.HFSPlusCatalogFile;
import org.catacombae.hfs.types.hfsplus.HFSPlusDate;


/**
 * @author <a href="https://catacombae.org" target="_top">Erik Larsson</a>
 */
public abstract class CommonHFSCatalogFile
        implements StaticStruct, PrintableStruct, StructElements, CommonHFSCatalogAttributes {

    public abstract CommonHFSCatalogNodeID getFileID();

    public abstract CommonHFSForkData getDataFork();

    public abstract CommonHFSForkData getResourceFork();

    public abstract byte[] getBytes();

    public abstract boolean isHardFileLink();

    public abstract boolean isHardDirectoryLink();

    public abstract boolean isSymbolicLink();

    public abstract int getHardLinkInode();

    public CommonHFSCatalogNodeID getCatalogNodeID() {
        return getFileID();
    }

    public void print(PrintStream ps, String prefix) {
        ps.println(prefix + CommonHFSCatalogFile.class.getSimpleName() + ":");
        printFields(ps, prefix + " ");
    }

    public abstract void printFields(PrintStream ps, String string);

    public static CommonHFSCatalogFile create(HFSPlusCatalogFile data) {
        return new HFSPlusImplementation(data);
    }

    public static CommonHFSCatalogFile create(CdrFilRec data) {
        return new HFSImplementation(data);
    }

    public static class HFSPlusImplementation extends CommonHFSCatalogFile {

        private static final int HARD_FILE_LINK_FILE_TYPE = 0x686C6E6B; // "hlnk"
        private static final int HARD_FILE_LINK_CREATOR = 0x6866732B; // "hfs+"
        private static final int HARD_DIRECTORY_LINK_FILE_TYPE = 0x66647270; // "fdrp"
        private static final int HARD_DIRECTORY_LINK_CREATOR = 0x4d414353; // "MACS"
        private HFSPlusCatalogFile data;

        private HFSPlusImplementation(HFSPlusCatalogFile data) {
            this.data = data;
        }

        public HFSPlusCatalogFile getUnderlying() {
            return data;
        }

        @Override
        public CommonHFSCatalogNodeID getFileID() {
            return CommonHFSCatalogNodeID.create(data.getFileID());
        }

        @Override
        public CommonHFSForkData getDataFork() {
            return CommonHFSForkData.create(data.getDataFork());
        }

        @Override
        public CommonHFSForkData getResourceFork() {
            return CommonHFSForkData.create(data.getResourceFork());
        }

        public int size() {
            return data.size();
        }

        @Override
        public byte[] getBytes() {
            return data.getBytes();
        }

        public short getRecordType() {
            return data.getRecordType();
        }

        public short getFlags() {
            return data.getFlags();
        }

        public int getCreateDate() {
            return data.getCreateDate();
        }

        public int getContentModDate() {
            return data.getContentModDate();
        }

        public int getAttributeModDate() {
            return data.getAttributeModDate();
        }

        public int getAccessDate() {
            return data.getAccessDate();
        }

        public int getBackupDate() {
            return data.getBackupDate();
        }

        public Date getCreateDateAsDate() {
            return data.getCreateDateAsDate();
        }

        public Date getContentModDateAsDate() {
            return data.getContentModDateAsDate();
        }

        public Date getAttributeModDateAsDate() {
            return data.getAttributeModDateAsDate();
        }

        public Date getAccessDateAsDate() {
            return data.getAccessDateAsDate();
        }

        public Date getBackupDateAsDate() {
            return data.getBackupDateAsDate();
        }

        @Override
        public void printFields(PrintStream ps, String prefix) {
            ps.println(prefix + "data:");
            data.print(ps, prefix + " ");
        }

        @Override
        public boolean isSymbolicLink() {
            return data.getPermissions().getFileModeFileType() == HFSPlusBSDInfo.FILETYPE_SYMBOLIC_LINK;
        }

        public Dictionary getStructElements() {
            return data.getStructElements();
        }

        public boolean hasPermissions() {
            return true;
        }

        public HFSPlusBSDInfo getPermissions() {
            return data.getPermissions();
        }

        @Override
        public boolean isHardFileLink() {
            int fileType = data.getUserInfo().getFileType().getOSType().getFourCharCode();
            int creator = data.getUserInfo().getFileCreator().getOSType().getFourCharCode();
            return fileType == HARD_FILE_LINK_FILE_TYPE && creator == HARD_FILE_LINK_CREATOR;
        }

        @Override
        public boolean isHardDirectoryLink() {
            int fileType = data.getUserInfo().getFileType().getOSType().getFourCharCode();
            int creator = data.getUserInfo().getFileCreator().getOSType().getFourCharCode();
            return fileType == HARD_DIRECTORY_LINK_FILE_TYPE &&
                    creator == HARD_DIRECTORY_LINK_CREATOR &&
                    data.getHasLinkChainFlag();
        }

        @Override
        public int getHardLinkInode() {
            return data.getPermissions().getSpecial();
        }

        public boolean hasCreateDate() {
            return true;
        }

        public boolean hasContentModDate() {
            return true;
        }

        public boolean hasAttributeModDate() {
            return true;
        }

        public boolean hasAccessDate() {
            return true;
        }

        public boolean hasBackupDate() {
            return true;
        }

        public CommonHFSFinderInfo getFinderInfo() {
            return CommonHFSFinderInfo.create(data);
        }
    }

    public static class HFSImplementation extends CommonHFSCatalogFile {

        private CdrFilRec data;

        private HFSImplementation(CdrFilRec data) {
            this.data = data;
        }

        @Override
        public CommonHFSCatalogNodeID getFileID() {
            return CommonHFSCatalogNodeID.create(data.getFilFlNum());
        }

        @Override
        public CommonHFSForkData getDataFork() {
            return CommonHFSForkData.create(data.getFilExtRec(), data.getFilLgLen());
        }

        @Override
        public CommonHFSForkData getResourceFork() {
            return CommonHFSForkData.create(data.getFilRExtRec(), data.getFilRLgLen());
        }

        public int size() {
            return data.size();
        }

        @Override
        public byte[] getBytes() {
            return data.getBytes();
        }

        public short getRecordType() {
            return data.getCdrType();
        }

        public short getFlags() {
            return data.getFilFlags();
        }

        public int getCreateDate() {
            return data.getFilCrDat();
        }

        public int getContentModDate() {
            return data.getFilMdDat();
        }

        public int getAttributeModDate() {
            return data.getFilMdDat();
        }

        public int getAccessDate() {
            return data.getFilMdDat();
        }

        public int getBackupDate() {
            return data.getFilBkDat();
        }

        public Date getCreateDateAsDate() {
            return HFSPlusDate.localTimestampToDate(getCreateDate());
        }

        public Date getContentModDateAsDate() {
            return HFSPlusDate.localTimestampToDate(getContentModDate());
        }

        public Date getAttributeModDateAsDate() {
            return HFSPlusDate.localTimestampToDate(getAttributeModDate());
        }

        public Date getAccessDateAsDate() {
            return HFSPlusDate.localTimestampToDate(getAccessDate());
        }

        public Date getBackupDateAsDate() {
            return HFSPlusDate.localTimestampToDate(getBackupDate());
        }

        @Override
        public void printFields(PrintStream ps, String prefix) {
            ps.println(prefix + "data:");
            data.print(ps, prefix + " ");
        }

        @Override
        public boolean isSymbolicLink() {
            // HFS doesn't support symbolic links.
            return false;
        }

        public Dictionary getStructElements() {
            return data.getStructElements();
        }

        public boolean hasPermissions() {
            return false;
        }

        public HFSPlusBSDInfo getPermissions() {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public boolean isHardFileLink() {
            return false; // No such thing in HFS.
        }

        @Override
        public boolean isHardDirectoryLink() {
            return false; // No such thing in HFS.
        }

        @Override
        public int getHardLinkInode() {
            throw new UnsupportedOperationException("Not supported for HFS.");
        }

        public boolean hasCreateDate() {
            return true;
        }

        public boolean hasContentModDate() {
            return true;
        }

        public boolean hasAttributeModDate() {
            return false;
        }

        public boolean hasAccessDate() {
            return false;
        }

        public boolean hasBackupDate() {
            return true;
        }

        public CommonHFSFinderInfo getFinderInfo() {
            return CommonHFSFinderInfo.create(data);
        }
    }
}
