/*-
 * Copyright (C) 2008-2009 Erik Larsson
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

package org.catacombae.storage.fs.hfscommon;

import java.util.Date;

import org.catacombae.hfs.types.hfscommon.CommonHFSCatalogAttributes;
import org.catacombae.hfs.types.hfscommon.CommonHFSCatalogFileRecord;
import org.catacombae.hfs.types.hfsplus.HFSPlusBSDInfo;
import org.catacombae.storage.fs.FSAttributes;
import org.catacombae.storage.fs.WindowsFileAttributes;
import org.catacombae.util.Util;


/**
 * @author <a href="https://catacombae.org" target="_top">Erik Larsson</a>
 */
class HFSCommonFSAttributes extends FSAttributes {

    private final HFSCommonFSEntry parentEntry;
    private final CommonHFSCatalogAttributes attributes;
    private POSIXFileAttributes posixAttributes = null;

    public HFSCommonFSAttributes(HFSCommonFSEntry parentEntry, CommonHFSCatalogAttributes attributes) {
        this.parentEntry = parentEntry;
        this.attributes = attributes;
    }

    @Override
    public boolean hasPOSIXFileAttributes() {
        return attributes.hasPermissions();
    }

    @Override
    public POSIXFileAttributes getPOSIXFileAttributes() {
        if (attributes.hasPermissions()) {
            if (posixAttributes == null) {
                HFSPlusBSDInfo permissions = attributes.getPermissions();

                posixAttributes = new DefaultPOSIXFileAttributes(
                        Util.unsign(permissions.getOwnerID()),
                        Util.unsign(permissions.getGroupID()),
                        permissions.getFileMode(),
                        attributes.getCatalogNodeID().toLong());
            }
            return posixAttributes;
        } else
            throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public WindowsFileAttributes getWindowsFileAttributes() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public Date getModifyDate() {
        return attributes.getContentModDateAsDate();
    }


    @Override
    public boolean hasWindowsFileAttributes() {
        return false;
    }

    @Override
    public boolean hasCreateDate() {
        return attributes.hasCreateDate();
    }

    @Override
    public Date getCreateDate() {
        return attributes.getCreateDateAsDate();
    }

    @Override
    public boolean hasModifyDate() {
        return attributes.hasContentModDate();
    }

    @Override
    public boolean hasAttributeModifyDate() {
        return attributes.hasAttributeModDate();
    }

    @Override
    public boolean hasAccessDate() {
        return attributes.hasAccessDate();
    }

    @Override
    public Date getAccessDate() {
        return attributes.getAccessDateAsDate();
    }

    @Override
    public boolean hasBackupDate() {
        return attributes.hasBackupDate();
    }

    @Override
    public Date getBackupDate() {
        return attributes.getBackupDateAsDate();
    }

    @Override
    public Date getAttributeModifyDate() {
        return attributes.getAttributeModDateAsDate();
    }

    @Override
    public boolean hasLinkCount() {
        if (attributes instanceof CommonHFSCatalogFileRecord fr) {
            if (fr.getData().isHardFileLink() /* || fr.getData().isHardDirectoryLink() */)
                return true;
        }

        return false;
    }

    @Override
    public Long getLinkCount() {
        if (attributes instanceof CommonHFSCatalogFileRecord) {
            return parentEntry.getFileSystemHandler().getLinkCount(
                    (CommonHFSCatalogFileRecord) attributes);
        }

        return null;
    }
}
