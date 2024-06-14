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

import org.catacombae.csjc.PrintableStruct;
import org.catacombae.csjc.structelements.Dictionary;
import org.catacombae.hfs.types.hfs.CatKeyRec;
import org.catacombae.hfs.types.hfs.CdrThdRec;
import org.catacombae.hfs.types.hfsplus.HFSPlusCatalogKey;
import org.catacombae.hfs.types.hfsplus.HFSPlusCatalogThread;


/**
 * @author <a href="https://catacombae.org" target="_top">Erik Larsson</a>
 */
public class CommonHFSCatalogFolderThreadRecord extends CommonHFSCatalogThreadRecord<CommonHFSCatalogFolderThread>
        implements PrintableStruct {

    private final CommonHFSCatalogKey key;
    private final CommonHFSCatalogFolderThread data;

    private CommonHFSCatalogFolderThreadRecord(CommonHFSCatalogKey key, CommonHFSCatalogFolderThread data) {
        this.key = key;
        this.data = data;
    }

    @Override
    public CommonHFSCatalogKey getKey() {
        return key;
    }

    @Override
    public CommonHFSCatalogFolderThread getData() {
        return data;
    }

    @Override
    public void print(PrintStream ps, String prefix) {
        ps.println(prefix + CommonHFSCatalogFolderThreadRecord.class.getSimpleName() + ":");
        printFields(ps, prefix + " ");
    }

    @Override
    public void printFields(PrintStream ps, String prefix) {
        ps.println(prefix + "key:");
        key.print(ps, prefix + " ");
        ps.println(prefix + "data:");
        data.print(ps, prefix + " ");
    }

    @Override
    public Dictionary getStructElements() {
        DictionaryBuilder db = new DictionaryBuilder(CommonHFSCatalogFolderThreadRecord.class.getSimpleName(),
                        "Folder thread record");

        db.add("key", key.getStructElements(), "Catalog key");
        db.add("data", data.getStructElements(), "Folder thread data");

        return db.getResult();
    }

    public static CommonHFSCatalogFolderThreadRecord create(HFSPlusCatalogKey key, HFSPlusCatalogThread data) {
        return new CommonHFSCatalogFolderThreadRecord(CommonHFSCatalogKey.create(key),
                CommonHFSCatalogFolderThread.create(data));
    }

    public static CommonHFSCatalogFolderThreadRecord create(CatKeyRec key, CdrThdRec data) {
        return new CommonHFSCatalogFolderThreadRecord(CommonHFSCatalogKey.create(key),
                CommonHFSCatalogFolderThread.create(data));
    }

    @Override
    public int getSize() {
        return key.occupiedSize() + data.length();
    }

    @Override
    public byte[] getBytes() {
        byte[] result = new byte[getSize()];
        byte[] tempData;
        int offset = 0;

        tempData = getKey().getBytes();
        System.arraycopy(tempData, 0, result, offset, tempData.length);
        offset += tempData.length;
        tempData = data.getBytes();
        System.arraycopy(tempData, 0, result, offset, tempData.length);
        offset += tempData.length;

        return result;
    }
}
