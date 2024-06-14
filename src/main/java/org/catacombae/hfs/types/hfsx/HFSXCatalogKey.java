/*-
 * Copyright (C) 2007 Erik Larsson
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

package org.catacombae.hfs.types.hfsx;

import org.catacombae.hfs.FastUnicodeCompare;
import org.catacombae.hfs.types.hfsplus.BTHeaderRec;
import org.catacombae.hfs.types.hfsplus.BTKey;
import org.catacombae.hfs.types.hfsplus.HFSCatalogNodeID;
import org.catacombae.hfs.types.hfsplus.HFSPlusCatalogKey;
import org.catacombae.hfs.types.hfsplus.HFSUniStr255;
import org.catacombae.util.Util;


/**
 * @author <a href="https://catacombae.org" target="_top">Erik Larsson</a>
 */
public class HFSXCatalogKey extends HFSPlusCatalogKey {

    private final byte keyCompareType;

    public HFSXCatalogKey(byte[] data, int offset, byte keyCompareType) {
        super(data, offset);

        this.keyCompareType = keyCompareType;
        if (keyCompareType != BTHeaderRec.kHFSBinaryCompare && keyCompareType != BTHeaderRec.kHFSCaseFolding)
            throw new IllegalArgumentException("Illegal key compare type: " + keyCompareType);
    }

    public HFSXCatalogKey(HFSCatalogNodeID parentID, HFSUniStr255 nodeName, byte keyCompareType) {
        super(parentID, nodeName);

        this.keyCompareType = keyCompareType;
        if (keyCompareType != BTHeaderRec.kHFSBinaryCompare && keyCompareType != BTHeaderRec.kHFSCaseFolding)
            throw new IllegalArgumentException("Illegal key compare type: " + keyCompareType);
    }

    public HFSXCatalogKey(int parentIDInt, String nodeNameString, byte keyCompareType) {
        super(parentIDInt, nodeNameString);

        this.keyCompareType = keyCompareType;
        if (keyCompareType != BTHeaderRec.kHFSBinaryCompare && keyCompareType != BTHeaderRec.kHFSCaseFolding)
            throw new IllegalArgumentException("Illegal key compare type: " + keyCompareType);
    }

    @Override
    public int compareTo(BTKey btk) {
        if (btk instanceof HFSPlusCatalogKey catKey) {
            if (Util.unsign(getParentID().toInt()) == Util.unsign(catKey.getParentID().toInt())) {
                return switch (keyCompareType) {
                    case BTHeaderRec.kHFSCaseFolding ->
                            FastUnicodeCompare.compare(getNodeName().getUnicode(), catKey.getNodeName().getUnicode());
                    case BTHeaderRec.kHFSBinaryCompare ->
                            Util.unsignedArrayCompareLex(getNodeName().getUnicode(), catKey.getNodeName().getUnicode());
                    default -> throw new RuntimeException("Invalid value in file system structure! keyCompareType = " +
                            keyCompareType);
                };
            } else return super.compareTo(btk);
        } else
            return super.compareTo(btk);
    }
}
