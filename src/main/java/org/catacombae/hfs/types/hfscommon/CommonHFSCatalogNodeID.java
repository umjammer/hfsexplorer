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

import org.catacombae.hfs.types.hfsplus.HFSCatalogNodeID;
import org.catacombae.util.Util;


/**
 * @author <a href="https://catacombae.org" target="_top">Erik Larsson</a>
 */
public abstract class CommonHFSCatalogNodeID {

    public enum ReservedID {
        ROOT_PARENT,
        ROOT_FOLDER,
        EXTENTS_FILE,
        CATALOG_FILE,
        BAD_BLOCKS_FILE,
        ALLOCATION_FILE,
        STARTUP_FILE,
        ATTRIBUTES_FILE,
        REPAIR_CATALOG_FILE,
        BOGUS_EXTENT_FILE,
        FIRST_USER_CATALOG_NODE_ID
    }

//    /**
//     * Returns an <code>int</code> representation of this catalog node ID.
//     * @return an <code>int</code> representation of this catalog node ID.
//     */
//    public abstract int toInt();

    /**
     * Returns an <code>long</code> representation of this catalog node ID.
     *
     * @return an <code>long</code> representation of this catalog node ID.
     */
    public abstract long toLong();

    public abstract CommonHFSCatalogNodeID add(long value);

    /**
     * Returns a CommonHFSCatalogNodeID for a specified reserved ID, if the
     * reserved ID is supported for the implementation (otherwise null is
     * returned). HFS only supports ROOT_PARENT, ROOT_FOLDER, EXTENTS_FILE,
     * CATALOG_FILE, BAD_BLOCKS_FILE and FIRST_USER_CATALOG_NODE_ID.
     *
     * @param id the reserved ID to look up.
     * @return a CommonHFSCatalogNodeID representing the specified reserved ID.
     */
    public static CommonHFSCatalogNodeID getHFSReservedID(ReservedID id) {
        return HFSImplementation.getReservedIDStatic(id);
    }

    /**
     * Returns a CommonHFSCatalogNodeID for a specified reserved ID, if the
     * reserved ID is supported for the implementation (otherwise null is
     * returned). HFS+ supports all ReservedID values.
     *
     * @param id
     * @return a CommonHFSCatalogNodeID representing the specified reserved ID.
     */
    public static CommonHFSCatalogNodeID getHFSPlusReservedID(ReservedID id) {
        return HFSPlusImplementation.getReservedIDStatic(id);
    }

    /**
     * Returns a CommonHFSCatalogNodeID for a specified reserved ID, if the
     * reserved ID is supported for the implementation (otherwise null is
     * returned). HFS only supports ROOT_PARENT, ROOT_FOLDER, EXTENTS_FILE,
     * CATALOG_FILE, BAD_BLOCKS_FILE and FIRST_USER_CATALOG_NODE_ID while
     * HFS+ supports all ReservedID values.
     *
     * @param id
     * @return a CommonHFSCatalogNodeID representing the specified reserved ID.
     */
    public abstract CommonHFSCatalogNodeID getReservedID(ReservedID id);

    @Override
    public boolean equals(Object o) {
        if (o instanceof CommonHFSCatalogNodeID)
            return ((CommonHFSCatalogNodeID) o).toLong() == this.toLong();
        else
            return false;
    }

    @Override
    public int hashCode() {
        return (int) toLong();
    }


    public static CommonHFSCatalogNodeID create(HFSCatalogNodeID fileID) {
        return new HFSPlusImplementation(fileID);
    }

    public static CommonHFSCatalogNodeID create(int filFlNum) {
        return new HFSImplementation(filFlNum);
    }

    public static class HFSPlusImplementation extends CommonHFSCatalogNodeID {

        private static final HFSPlusImplementation ROOT_PARENT_ID = new HFSPlusImplementation(HFSCatalogNodeID.kHFSRootParentID);
        private static final HFSPlusImplementation ROOT_FOLDER_ID = new HFSPlusImplementation(HFSCatalogNodeID.kHFSRootFolderID);
        private static final HFSPlusImplementation EXTENTS_FILE_ID = new HFSPlusImplementation(HFSCatalogNodeID.kHFSExtentsFileID);
        private static final HFSPlusImplementation CATALOG_FILE_ID = new HFSPlusImplementation(HFSCatalogNodeID.kHFSCatalogFileID);
        private static final HFSPlusImplementation BAD_BLOCKS_FILE_ID = new HFSPlusImplementation(HFSCatalogNodeID.kHFSBadBlockFileID);
        private static final HFSPlusImplementation ALLOCATION_FILE_ID = new HFSPlusImplementation(HFSCatalogNodeID.kHFSAllocationFileID);
        private static final HFSPlusImplementation STARTUP_FILE_ID = new HFSPlusImplementation(HFSCatalogNodeID.kHFSStartupFileID);
        private static final HFSPlusImplementation ATTRIBUTES_FILE_ID = new HFSPlusImplementation(HFSCatalogNodeID.kHFSAttributesFileID);
        private static final HFSPlusImplementation REPAIR_CATALOG_FILE_ID = new HFSPlusImplementation(HFSCatalogNodeID.kHFSRepairCatalogFileID);
        private static final HFSPlusImplementation BOGUS_EXTENT_FILE_ID = new HFSPlusImplementation(HFSCatalogNodeID.kHFSBogusExtentFileID);
        private static final HFSPlusImplementation FIRST_USER_CATALOG_NODE_ID = new HFSPlusImplementation(HFSCatalogNodeID.kHFSFirstUserCatalogNodeID);

        private final HFSCatalogNodeID fileID;

        public HFSPlusImplementation(HFSCatalogNodeID fileID) {
            this.fileID = fileID;
        }

//        @Override
//        public int toInt() {
//            return fileID.toInt();
//        }

        public HFSCatalogNodeID getHFSCatalogNodeID() {
            return fileID;
        }

        @Override
        public long toLong() {
            return fileID.toLong();
        }

        @Override
        public CommonHFSCatalogNodeID getReservedID(ReservedID id) {
            return getReservedIDStatic(id);
        }

        public static CommonHFSCatalogNodeID getReservedIDStatic(ReservedID id) {
            return switch (id) {
                case ROOT_PARENT -> ROOT_PARENT_ID;
                case ROOT_FOLDER -> ROOT_FOLDER_ID;
                case EXTENTS_FILE -> EXTENTS_FILE_ID;
                case CATALOG_FILE -> CATALOG_FILE_ID;
                case BAD_BLOCKS_FILE -> BAD_BLOCKS_FILE_ID;
                case ALLOCATION_FILE -> ALLOCATION_FILE_ID;
                case STARTUP_FILE -> STARTUP_FILE_ID;
                case ATTRIBUTES_FILE -> ATTRIBUTES_FILE_ID;
                case REPAIR_CATALOG_FILE -> REPAIR_CATALOG_FILE_ID;
                case BOGUS_EXTENT_FILE -> BOGUS_EXTENT_FILE_ID;
                case FIRST_USER_CATALOG_NODE_ID -> FIRST_USER_CATALOG_NODE_ID;
            };
        }

        @Override
        public CommonHFSCatalogNodeID add(long value) {
            if (value < 0 || value > 0xffff_ffffL) {
                throw new RuntimeException("Invalid value: " + value);
            }

            return new HFSPlusImplementation(new HFSCatalogNodeID((int) (toLong() + value)));
        }
    }

    public static class HFSImplementation extends CommonHFSCatalogNodeID {

        private static final HFSImplementation ROOT_PARENT_ID = new HFSImplementation(1);
        private static final HFSImplementation ROOT_FOLDER_ID = new HFSImplementation(2);
        private static final HFSImplementation EXTENTS_FILE_ID = new HFSImplementation(3);
        private static final HFSImplementation CATALOG_FILE_ID = new HFSImplementation(4);
        private static final HFSImplementation BAD_BLOCKS_FILE_ID = new HFSImplementation(5);
        private static final HFSImplementation FIRST_USER_CATALOG_NODE_ID = new HFSImplementation(16);

        private final int filFlNum;

        public HFSImplementation(int filFlNum) {
            this.filFlNum = filFlNum;
        }

//        @Override
//        public int toInt() {
//            return filFlNum;
//        }

        @Override
        public long toLong() {
            return Util.unsign(filFlNum);
        }

        @Override
        public CommonHFSCatalogNodeID getReservedID(ReservedID id) {
            return getReservedIDStatic(id);
        }

        public static CommonHFSCatalogNodeID getReservedIDStatic(ReservedID id) {
            return switch (id) {
                case ROOT_PARENT -> ROOT_PARENT_ID;
                case ROOT_FOLDER -> ROOT_FOLDER_ID;
                case EXTENTS_FILE -> EXTENTS_FILE_ID;
                case CATALOG_FILE -> CATALOG_FILE_ID;
                case BAD_BLOCKS_FILE -> BAD_BLOCKS_FILE_ID;
                case ALLOCATION_FILE, STARTUP_FILE, ATTRIBUTES_FILE, REPAIR_CATALOG_FILE, BOGUS_EXTENT_FILE ->
                        null; // Not applicable for HFS.
                case FIRST_USER_CATALOG_NODE_ID -> FIRST_USER_CATALOG_NODE_ID;
            };
        }

        @Override
        public CommonHFSCatalogNodeID add(long value) {
            if (value < 0 || value > 0xFFFFFFFFL) {
                throw new RuntimeException("Invalid value: " + value);
            }

            return new HFSImplementation((int) (toLong() + value));
        }
    }
}
