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

import org.catacombae.csjc.PrintableStruct;


/**
 * This class was generated by CStructToJavaClass.
 *
 * @author <a href="https://catacombae.org" target="_top">Erik Larsson</a>
 */
public class HFSPlusExtentLeafRecord implements PrintableStruct {
    /*
     * struct HFSPlusExtentLeafRecord
     * size: 76 bytes
     * description:
     *
     * BP  Size  Type                 Identifier  Description
     * ------------------------------------------------------
     * 0   12    HFSPlusExtentKey     key
     * 12  64    HFSPlusExtentRecord  recordData
     */

    private final HFSPlusExtentKey key;
    private final HFSPlusExtentRecord recordData;

    public HFSPlusExtentLeafRecord(byte[] data, int offset) {
        key = new HFSPlusExtentKey(data, offset + 0);
        recordData = new HFSPlusExtentRecord(data, offset + 12);
    }

    public static int length() {
        return 76;
    }

    public HFSPlusExtentKey getKey() {
        return key;
    }

    public HFSPlusExtentRecord getRecordData() {
        return recordData;
    }

    @Override
    public void printFields(PrintStream ps, String prefix) {
        ps.println(prefix + " key: ");
        getKey().print(ps, prefix + "  ");
        ps.println(prefix + " recordData: ");
        getRecordData().print(ps, prefix + "  ");
    }

    @Override
    public void print(PrintStream ps, String prefix) {
        ps.println(prefix + "HFSPlusExtentLeafRecord:");
        printFields(ps, prefix);
    }
}
