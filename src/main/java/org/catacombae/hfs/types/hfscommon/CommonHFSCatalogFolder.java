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
import org.catacombae.hfs.types.hfs.CdrDirRec;
import org.catacombae.hfs.types.hfs.HFSDate;
import org.catacombae.hfs.types.hfsplus.HFSPlusBSDInfo;
import org.catacombae.hfs.types.hfsplus.HFSPlusCatalogFolder;
import org.catacombae.util.Util;


/**
 * @author <a href="https://catacombae.org" target="_top">Erik Larsson</a>
 */
public abstract class CommonHFSCatalogFolder implements CommonHFSCatalogAttributes, PrintableStruct, StructElements {

    public abstract CommonHFSCatalogNodeID getFolderID();

    public static CommonHFSCatalogFolder create(HFSPlusCatalogFolder data) {
        return new HFSPlusImplementation(data);
    }

    public static CommonHFSCatalogFolder create(CdrDirRec data) {
        return new HFSImplementation(data);
    }

    public abstract long getValence();

    public abstract int length();

    public abstract byte[] getBytes();

    public CommonHFSCatalogNodeID getCatalogNodeID() {
        return getFolderID();
    }

    public void print(PrintStream ps, String prefix) {
        ps.println(prefix + CommonHFSCatalogFolder.class.getSimpleName() + ":");
        printFields(ps, prefix + " ");
    }

    public static class HFSPlusImplementation extends CommonHFSCatalogFolder {

        private HFSPlusCatalogFolder data;

        public HFSPlusImplementation(HFSPlusCatalogFolder data) {
            this.data = data;
        }

        //@Deprecated
        public HFSPlusCatalogFolder getUnderlying() {
            return data;
        }

        @Override
        public CommonHFSCatalogNodeID getFolderID() {
            return CommonHFSCatalogNodeID.create(data.getFolderID());
        }

        @Override
        public long getValence() {
            return Util.unsign(data.getValence());
        }

        @Override
        public int length() {
            return data.length();
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

        public void printFields(PrintStream ps, String prefix) {
            ps.println(prefix + "data:");
            data.print(ps, prefix + " ");
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

    public static class HFSImplementation extends CommonHFSCatalogFolder {

        private CdrDirRec data;

        public HFSImplementation(CdrDirRec data) {
            this.data = data;
        }

        @Override
        public CommonHFSCatalogNodeID getFolderID() {
            return CommonHFSCatalogNodeID.create(data.getDirDirID());
        }

        @Override
        public long getValence() {
            return Util.unsign(data.getDirVal());
        }

        @Override
        public int length() {
            return data.length();
        }

        @Override
        public byte[] getBytes() {
            return data.getBytes();
        }


        public short getRecordType() {
            return data.getCdrType();
        }

        public short getFlags() {
            return data.getDirFlags();
        }

        public int getCreateDate() {
            return data.getDirCrDat();
        }

        public int getContentModDate() {
            return data.getDirMdDat();
        }

        public int getAttributeModDate() {
            return data.getDirMdDat();
        }

        public int getAccessDate() {
            return data.getDirMdDat();
        }

        public int getBackupDate() {
            return data.getDirBkDat();
        }

        public Date getCreateDateAsDate() {
            return HFSDate.localTimestampToDate(getCreateDate());
        }

        public Date getContentModDateAsDate() {
            return HFSDate.localTimestampToDate(getContentModDate());
        }

        public Date getAttributeModDateAsDate() {
            return HFSDate.localTimestampToDate(getAttributeModDate());
        }

        public Date getAccessDateAsDate() {
            return HFSDate.localTimestampToDate(getAccessDate());
        }

        public Date getBackupDateAsDate() {
            return HFSDate.localTimestampToDate(getBackupDate());
        }

        public void printFields(PrintStream ps, String prefix) {
            ps.println(prefix + "data:");
            data.print(ps, prefix + " ");
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

        public boolean hasAccessDate() {
            return false;
        }

        public boolean hasBackupDate() {
            return true;
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

        public CommonHFSFinderInfo getFinderInfo() {
            return CommonHFSFinderInfo.create(data);
        }
    }
}
