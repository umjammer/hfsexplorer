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

package org.catacombae.hfs.types.hfs;

import org.catacombae.util.Util;
import java.io.PrintStream;
import org.catacombae.csjc.PrintableStruct;
import org.catacombae.csjc.StructElements;
import org.catacombae.csjc.structelements.ArrayBuilder;
import org.catacombae.csjc.structelements.Dictionary;

/**
 * This class was generated by CStructToJavaClass.
 *
 * @author <a href="https://catacombae.org" target="_top">Erik Larsson</a>
 */
public class ExtDataRec implements PrintableStruct, StructElements {
    /*
     * struct ExtDataRec
     * size: 12 bytes
     * description:
     *
     * BP  Size  Type              Identifier   Description
     * -----------------------------------------------------------
     * 0   4*3   ExtDescriptor[3]  extDataRecs  extent data record
     */

    public static final int STRUCTSIZE = 12;

    private final ExtDescriptor[] extDataRecs = new ExtDescriptor[3];

    public ExtDataRec(byte[] data, int offset) {
        int curOff = offset;
        for(int i = 0; i < extDataRecs.length; ++i) {
            extDataRecs[i] = new ExtDescriptor(data, curOff);
            curOff += ExtDescriptor.length();
        }
    }

    public static int length() { return STRUCTSIZE; }

    /** extent data record */
    public ExtDescriptor[] getExtDataRecs() { return Util.arrayCopy(extDataRecs, new ExtDescriptor[extDataRecs.length]); }

    public void printFields(PrintStream ps, String prefix) {
	ps.println(prefix + " extDataRecs: ");
        for(int i = 0; i < extDataRecs.length; ++i) {
            ps.println(prefix + "  [" + i + "]: ");
            extDataRecs[i].print(ps, prefix+"   ");
        }
    }

    public void print(PrintStream ps, String prefix) {
	ps.println(prefix + "ExtDataRec:");
	printFields(ps, prefix);
    }

    public byte[] getBytes() {
	byte[] result = new byte[length()];
	byte[] tempData;
	int offset = 0;

        for(ExtDescriptor extDataRec : extDataRecs) {
            tempData = extDataRec.getBytes();
            System.arraycopy(tempData, 0, result, offset, tempData.length); offset += tempData.length;
        }

	return result;
    }

    public Dictionary getStructElements() {
        DictionaryBuilder db = new DictionaryBuilder(ExtDataRec.class.getSimpleName());

        {
            ArrayBuilder ab = new ArrayBuilder("ExtDescriptor[" + extDataRecs.length + "]");
            for(int i = 0; i < extDataRecs.length; i += 4)
                ab.add(extDataRecs[i].getStructElements());
            db.add("extDataRecs", ab.getResult());
        }

        return db.getResult();
    }
}
