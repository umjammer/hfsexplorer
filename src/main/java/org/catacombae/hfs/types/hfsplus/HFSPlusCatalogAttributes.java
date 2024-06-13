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

package org.catacombae.hfs.types.hfsplus;

import java.util.Date;


/**
 * Generalization of the common attributes of HFSPlusCatalogFolder and HFSPlusCatalogFile.
 *
 * @author <a href="https://catacombae.org" target="_top">Erik Larsson</a>
 */
public interface HFSPlusCatalogAttributes {

    short kHFSFileLockedBit = 0x0000;
    short kHFSFileLockedMask = 0x0001;

    short kHFSThreadExistsBit = 0x0001;
    short kHFSThreadExistsMask = 0x0002;

    short kHFSHasAttributesBit = 0x0002;
    short kHFSHasAttributesMask = 0x0004;

    short kHFSHasSecurityBit = 0x0003;
    short kHFSHasSecurityMask = 0x0008;

    short kHFSHasFolderCountBit = 0x0004;
    short kHFSHasFolderCountMask = 0x0010;

    short kHFSHasLinkChainBit = 0x0005;
    short kHFSHasLinkChainMask = 0x0020;

    short kHFSHasChildLinkBit = 0x0006;
    short kHFSHasChildLinkMask = 0x0040;

    short kHFSHasDateAddedBit = 0x0007;
    short kHFSHasDateAddedMask = 0x0080;

    short getRecordType();

    short getFlags();

    int getCreateDate();

    int getContentModDate();

    int getAttributeModDate();

    int getAccessDate();

    int getBackupDate();

    HFSPlusBSDInfo getPermissions();

    int getTextEncoding();

    Date getCreateDateAsDate();

    Date getContentModDateAsDate();

    Date getAttributeModDateAsDate();

    Date getAccessDateAsDate();

    Date getBackupDateAsDate();
}
