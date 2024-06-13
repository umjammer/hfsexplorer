/*-
 * Copyright (C) 2015 Erik Larsson
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

package org.catacombae.hfsexplorer.types.applesingle;

import java.io.PrintStream;
import java.lang.reflect.Field;

import org.catacombae.csjc.DynamicStruct;
import org.catacombae.csjc.PrintableStruct;
import org.catacombae.csjc.structelements.Dictionary;
import org.catacombae.csjc.structelements.DictionaryBuilder;
import org.catacombae.util.Util;


/**
 * This class was generated by CStructToJavaClass.
 *
 * @author <a href="https://catacombae.org" target="_top">Erik Larsson</a>
 */
public class AttributeEntry implements DynamicStruct, PrintableStruct {
    /*
     * struct AttributeEntry
     * size: 12 bytes
     * description:
     *
     * BP  Size   Type     Identifier  Description
     * -------------------------------------------
     * 0   4      ube32    offset
     * 4   4      ube32    length
     * 8   2      be16     flags
     * 10  1      u8       nameLength
     * 11  1 * ?  char[?]  name
     */

    public static final int STATIC_STRUCTSIZE = 11;

    private static final long MAX_U32 = 0xFFFFFFFFL;
    private static final int MAX_U8 = 0xFF;

    private final int offset;
    private final int length;
    private final short flags;
    private final byte nameLength;
    private final byte[] name;

    public AttributeEntry(byte[] data, int offset) {
        this.offset = Util.readIntBE(data, offset + 0);
        this.length = Util.readIntBE(data, offset + 4);
        this.flags = Util.readShortBE(data, offset + 8);
        this.nameLength = Util.readByteBE(data, offset + 10);

        this.name = new byte[getNameLength()];
        System.arraycopy(data, offset + 11, this.name, 0, this.name.length);
    }

    public AttributeEntry(long offset, long length, short flags, byte[] name, int nameOffset, short nameLength) {
        if (offset < 0 || offset > MAX_U32) {
            throw new IllegalArgumentException("Illegal value for offset (" + offset + ").");
        }

        if (length < 0 || length > MAX_U32) {
            throw new IllegalArgumentException("Illegal value for length (" + length + ").");
        }

        if (nameLength < 0 || nameLength > MAX_U8) {
            throw new IllegalArgumentException("Illegal value for nameLength (" + length + ").");
        }

        this.offset = (int) offset;
        this.length = (int) length;
        this.flags = flags;
        this.nameLength = (byte) nameLength;
        this.name = new byte[nameLength];
        System.arraycopy(name, nameOffset, this.name, 0, nameLength);
    }

    @Override
    public int occupiedSize() {
        return STATIC_STRUCTSIZE + getNameLength();
    }

    @Override
    public int maxSize() {
        return STATIC_STRUCTSIZE + 255;
    }

    /** */
    public final long getOffset() {
        return Util.unsign(getRawOffset());
    }

    /** */
    public final long getLength() {
        return Util.unsign(getRawLength());
    }

    /** */
    public final short getFlags() {
        return this.flags;
    }

    /** */
    public final short getNameLength() {
        return Util.unsign(getRawNameLength());
    }

    /** */
    public final byte[] getName() {
        return Util.createCopy(this.name);
    }

    /**
     * <b>Note that the return value from this function should be interpreted as
     * an unsigned integer, for instance using Util.unsign(...).</b>
     */
    public final int getRawOffset() {
        return this.offset;
    }

    /**
     * <b>Note that the return value from this function should be interpreted as
     * an unsigned integer, for instance using Util.unsign(...).</b>
     */
    public final int getRawLength() {
        return this.length;
    }

    /**
     * <b>Note that the return value from this function should be interpreted as
     * an unsigned integer, for instance using Util.unsign(...).</b>
     */
    public final byte getRawNameLength() {
        return this.nameLength;
    }

    @Override
    public void printFields(PrintStream ps, String prefix) {
        ps.println(prefix + " offset: " + getOffset());
        ps.println(prefix + " length: " + getLength());
        ps.println(prefix + " flags: " + getFlags());
        ps.println(prefix + " nameLength: " + getNameLength());
        ps.println(prefix + " name: \"" +
                Util.readString(getName(), 0, getNameLength() - 1, "UTF-8") + "\"");
    }

    @Override
    public void print(PrintStream ps, String prefix) {
        ps.println(prefix + "AttributeEntry:");
        printFields(ps, prefix);
    }

    @Override
    public byte[] getBytes() {
        byte[] result = new byte[occupiedSize()];
        getBytes(result, 0);
        return result;
    }

    public int getBytes(byte[] result, int offset) {
        int startOffset = offset;

        Util.arrayPutBE(result, offset, this.offset);
        offset += 4;

        Util.arrayPutBE(result, offset, this.length);
        offset += 4;

        Util.arrayPutBE(result, offset, this.flags);
        offset += 2;

        Util.arrayPutBE(result, offset, this.nameLength);
        offset += 1;

        System.arraycopy(this.name, 0, result, offset, this.name.length);
        offset += this.name.length;

        return offset - startOffset;
    }

    private Field getPrivateField(String name) throws NoSuchFieldException {
        Field f = getClass().getDeclaredField(name);
        f.setAccessible(true);
        return f;
    }

    public Dictionary getStructElements() {
        DictionaryBuilder db = new DictionaryBuilder(AttributeEntry.class.getSimpleName());

        try {
            db.addUIntBE("offset", getPrivateField("offset"), this);
            db.addUIntBE("length", getPrivateField("length"), this);
            db.addUIntBE("flags", getPrivateField("flags"), this);
            db.addUIntBE("nameLength", getPrivateField("nameLength"), this);
            db.addByteArray("name", this.name);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }

        return db.getResult();
    }
}
