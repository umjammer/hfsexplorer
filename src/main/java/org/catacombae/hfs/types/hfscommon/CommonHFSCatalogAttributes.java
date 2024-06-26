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

import java.util.Date;

import org.catacombae.hfs.types.hfsplus.HFSPlusBSDInfo;


/**
 * Generalization of the common attributes of CommonHFSCatalogFolder and CommonHFSCatalogFile.
 * <p>
 * Please note that this class is NOT designed to cope with subtle differences
 * between HFS and HFS+. For instance HFS does not have the concept of separate
 * content modify and attribute modify dates, and does not at all store access
 * date. This interface should be updated in a clever way some time later.
 *
 * @author <a href="https://catacombae.org" target="_top">Erik Larsson</a>
 */
public interface CommonHFSCatalogAttributes {

    /**
     * Returns the associated Catalog Node ID (File ID or Folder ID).
     *
     * @return the associated Catalog Node ID (File ID or Folder ID).
     */
    CommonHFSCatalogNodeID getCatalogNodeID();

    short getRecordType();

    short getFlags();

    int getCreateDate();

    int getContentModDate();

    int getAttributeModDate();

    int getAccessDate();

    int getBackupDate();


    /**
     * Returns whether or not this attribute set has a defined value for the "entry created"
     * timestamp.
     *
     * @return whether or not this attribute set has a defined value for the "entry created"
     * timestamp.
     */
    boolean hasCreateDate();

    /**
     * Returns whether or not this attribute set has a defined value for the "contents modified"
     * timestamp.
     *
     * @return whether or not this attribute set has a defined value for the "contents modified"
     * timestamp.
     */
    boolean hasContentModDate();

    /**
     * Returns whether or not this attribute set has a defined value for the "attributes modified"
     * timestamp.
     *
     * @return whether or not this attribute set has a defined value for the "attribtues modified"
     * timestamp.
     */
    boolean hasAttributeModDate();

    /**
     * Returns whether or not this attribute set has a defined value for the "entry last accessed"
     * timestamp.
     *
     * @return whether or not this attribute set has a defined value for the "entry last accessed"
     * timestamp.
     */
    boolean hasAccessDate();

    /**
     * Returns whether or not this attribute set has a defined value for the "entry last backuped"
     * timestamp.
     *
     * @return whether or not this attribute set has a defined value for the "entry last backuped"
     * timestamp.
     */
    boolean hasBackupDate();

    /**
     * Returns whether or not this object contains HFS+ permissions.
     *
     * @return whether or not this object contains HFS+ permissions.
     */
    boolean hasPermissions();

    HFSPlusBSDInfo getPermissions();

//    int getTextEncoding();

    CommonHFSFinderInfo getFinderInfo();

    Date getCreateDateAsDate();

    Date getContentModDateAsDate();

    Date getAttributeModDateAsDate();

    Date getAccessDateAsDate();

    Date getBackupDateAsDate();
}
