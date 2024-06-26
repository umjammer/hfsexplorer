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

package org.catacombae.hfs.types.finder;

import java.io.PrintStream;

import org.catacombae.csjc.PrintableStruct;
import org.catacombae.csjc.StructElements;
import org.catacombae.csjc.structelements.Dictionary;
import org.catacombae.util.Util;

import static java.nio.ByteOrder.BIG_ENDIAN;


/**
 * This class was generated by CStructToJavaClass.
 *
 * @author <a href="https://catacombae.org" target="_top">Erik Larsson</a>
 */
public class FXInfo implements PrintableStruct, StructElements {
    /*
     * struct FXInfo
     * size: 16 bytes
     * description:
     *
     * BP  Size  Type       Identifier  Description
     * ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
     * 0   2     SInt16     fdIconID    An ID number for the file's icon; the numbers that identify icons are assigned by the Finder.
     * 2   2*3   SInt16[3]  fdReserved  Reserved.
     * 8   1     SInt8      fdScript    Extended flags. Script code if high-bit is set.
     * 9   1     SInt8      fdXFlags    Extended flags.
     * 10  2     SInt16     fdComment   Reserved (set to 0). If the high-bit is clear, an ID number for the comment that is displayed in the information window when the user selects a file and chooses the Get Info command from the File menu. The numbers that identify comments are assigned by the Finder.
     * 12  4     SInt32     fdPutAway   If the user moves the file onto the desktop, the directory ID of the folder from which the user moves the file.
     */

    public static final int STRUCTSIZE = 16;

    private final byte[] fdIconID = new byte[2];
    private final byte[] fdReserved = new byte[2 * 3];
    private final byte[] fdScript = new byte[1];
    private final byte[] fdXFlags = new byte[1];
    private final byte[] fdComment = new byte[2];
    private final byte[] fdPutAway = new byte[4];

    public FXInfo(byte[] data, int offset) {
        System.arraycopy(data, offset + 0, fdIconID, 0, 2);
        System.arraycopy(data, offset + 2, fdReserved, 0, 2 * 3);
        System.arraycopy(data, offset + 8, fdScript, 0, 1);
        System.arraycopy(data, offset + 9, fdXFlags, 0, 1);
        System.arraycopy(data, offset + 10, fdComment, 0, 2);
        System.arraycopy(data, offset + 12, fdPutAway, 0, 4);
    }

    public static int length() {
        return STRUCTSIZE;
    }

    /** An ID number for the file's icon; the numbers that identify icons are assigned by the Finder. */
    public short getFdIconID() {
        return Util.readShortBE(fdIconID);
    }

    /** Reserved. */
    public short[] getFdReserved() {
        return Util.readShortArrayBE(fdReserved);
    }

    /** Extended flags. Script code if high-bit is set. */
    public byte getFdScript() {
        return Util.readByteBE(fdScript);
    }

    /** Extended flags. */
    public byte getFdXFlags() {
        return Util.readByteBE(fdXFlags);
    }

    /** Reserved (set to 0). If the high-bit is clear, an ID number for the comment that is displayed in the information window when the user selects a file and chooses the Get Info command from the File menu. The numbers that identify comments are assigned by the Finder. */
    public short getFdComment() {
        return Util.readShortBE(fdComment);
    }

    /** If the user moves the file onto the desktop, the directory ID of the folder from which the user moves the file. */
    public int getFdPutAway() {
        return Util.readIntBE(fdPutAway);
    }

    @Override
    public void printFields(PrintStream ps, String prefix) {
        ps.println(prefix + " fdIconID: " + getFdIconID());
        ps.println(prefix + " fdReserved: " + getFdReserved());
        ps.println(prefix + " fdScript: " + getFdScript());
        ps.println(prefix + " fdXFlags: " + getFdXFlags());
        ps.println(prefix + " fdComment: " + getFdComment());
        ps.println(prefix + " fdPutAway: " + getFdPutAway());
    }

    @Override
    public void print(PrintStream ps, String prefix) {
        ps.println(prefix + "FXInfo:");
        printFields(ps, prefix);
    }

    public byte[] getBytes() {
        byte[] result = new byte[STRUCTSIZE];
        int offset = 0;
        System.arraycopy(fdIconID, 0, result, offset, fdIconID.length);
        offset += fdIconID.length;
        System.arraycopy(fdReserved, 0, result, offset, fdReserved.length);
        offset += fdReserved.length;
        System.arraycopy(fdScript, 0, result, offset, fdScript.length);
        offset += fdScript.length;
        System.arraycopy(fdXFlags, 0, result, offset, fdXFlags.length);
        offset += fdXFlags.length;
        System.arraycopy(fdComment, 0, result, offset, fdComment.length);
        offset += fdComment.length;
        System.arraycopy(fdPutAway, 0, result, offset, fdPutAway.length);
        offset += fdPutAway.length;
        return result;
    }

    @Override
    public Dictionary getStructElements() {
        DictionaryBuilder db = new DictionaryBuilder(FXInfo.class.getSimpleName());

        db.addSIntBE("fdIconID", fdIconID);
        db.addIntArray("fdReserved", fdReserved, BITS_16, UNSIGNED, BIG_ENDIAN);
        db.addSIntBE("fdScript", fdScript);
        db.addUIntBE("fdXFlags", fdXFlags);
        db.addSIntBE("fdComment", fdComment);
        db.addSIntBE("fdPutAway", fdPutAway);

        return db.getResult();
    }
}
