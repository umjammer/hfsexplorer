/*-
 * Copyright (C) 2006 Erik Larsson
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

import java.io.PrintStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import org.catacombae.hfs.HFSException;
import org.catacombae.hfs.types.hfsx.HFSXCatalogLeafRecord;
import org.catacombae.util.Util;

import static java.lang.System.getLogger;


/**
 * @author <a href="https://catacombae.org" target="_top">Erik Larsson</a>
 */
public class HFSPlusCatalogLeafNode extends BTLeafNode {

    private static final Logger logger = getLogger(HFSPlusCatalogLeafNode.class.getName());

    protected final HFSPlusCatalogLeafRecord[] leafRecords;

    public HFSPlusCatalogLeafNode(byte[] data, int offset, int nodeSize) {
        this(data, offset, nodeSize, null);
    }

    protected HFSPlusCatalogLeafNode(byte[] data, int offset, int nodeSize, BTHeaderRec catalogHeaderRec) {
        super(data, offset, nodeSize);
        short[] offsets = new short[Util.unsign(nodeDescriptor.getNumRecords()) + 1];
        for (int i = 0; i < offsets.length; ++i) {
            offsets[i] = Util.readShortBE(data, offset + nodeSize - ((i + 1) * 2));
        }
        leafRecords = new HFSPlusCatalogLeafRecord[offsets.length - 1];
        // we loop offsets.length-1 times, since last offset is offset to free space
        int previousOffset = 0;
        for (int i = 0; i < leafRecords.length; ++i) {
            int currentOffset = Util.unsign(offsets[i]);
            if (currentOffset < BTNodeDescriptor.length() ||
                    currentOffset >= nodeSize || (i != 0 && currentOffset <= previousOffset)) {
                logger.log(Level.DEBUG, "Encountered invalid leaf record offset " +
                        "for record " + (i + 1) + ": " + currentOffset + " " +
                        "(node size: " + nodeSize + " current offset: " +
                        currentOffset + ((i != 0) ?
                        (" previous offset: " + previousOffset) : "") + ")");
                logger.log(Level.DEBUG, "Skipping record...");
                leafRecords[i] = null;
            } else {
                try {
                    if (catalogHeaderRec == null) {
                        leafRecords[i] = new HFSPlusCatalogLeafRecord(data, offset + currentOffset);
                    } else {
                        leafRecords[i] = new HFSXCatalogLeafRecord(data, offset + currentOffset, catalogHeaderRec);
                    }
                } catch (HFSException e) {
                    logger.log(Level.DEBUG, "Encountered invalid leaf record data " +
                            "at record " + (i + 1) + "/" + leafRecords.length + ":");
                    logger.log(Level.ERROR, e.getMessage(), e);
                    logger.log(Level.DEBUG, "Skipping record...");

                    leafRecords[i] = null;
                }
            }

            previousOffset = currentOffset;
        }
    }

    public HFSPlusCatalogLeafRecord getLeafRecord(int index) {
        return leafRecords[index];
    }

    public HFSPlusCatalogLeafRecord[] getLeafRecords() {
        HFSPlusCatalogLeafRecord[] copy = new HFSPlusCatalogLeafRecord[leafRecords.length];
        for (int i = 0; i < copy.length; ++i)
            copy[i] = leafRecords[i];
        return copy;
    }

    @Override
    public void printFields(PrintStream ps, String prefix) {
        super.printFields(ps, prefix);
        for (int i = 0; i < leafRecords.length; ++i) {
            ps.println(prefix + " leafRecords[" + i + "]:");
            leafRecords[i].printFields(ps, prefix + "  ");
        }
    }

    @Override
    public void print(PrintStream ps, String prefix) {
        ps.println(prefix + "HFSPlusCatalogLeafNode:");
        printFields(ps, prefix);
    }
}
