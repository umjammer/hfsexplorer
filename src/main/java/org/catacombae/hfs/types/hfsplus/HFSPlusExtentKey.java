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

import org.catacombae.csjc.StructElements;
import org.catacombae.csjc.structelements.Dictionary;
import org.catacombae.csjc.structelements.IntegerFieldRepresentation;
import org.catacombae.util.Util;


/**
 * This class was generated by CStructToJavaClass.
 *
 * @author <a href="https://catacombae.org" target="_top">Erik Larsson</a>
 */
public class HFSPlusExtentKey extends BTKey implements StructElements {

    /* Fork types */
    public static final byte DATA_FORK = (byte) 0x00;
    public static final byte RESOURCE_FORK = (byte) 0xFF;

    /*
     * struct HFSPlusExtentKey
     * size: 12 bytes
     * description:
     *
     * BP  Size  Type              Identifier  Description
     * ---------------------------------------------------
     * 0   2     UInt16            keyLength
     * 2   1     UInt8             forkType
     * 3   1     UInt8             pad
     * 4   4     HFSCatalogNodeID  fileID
     * 8   4     UInt32            startBlock
     */

    private final byte[] keyLength = new byte[2];
    private final byte[] forkType = new byte[1];
    private final byte[] pad = new byte[1];
    private final HFSCatalogNodeID fileID;
    private final byte[] startBlock = new byte[4];

    public HFSPlusExtentKey(byte[] data, int offset) {
        System.arraycopy(data, offset + 0, keyLength, 0, 2);
        System.arraycopy(data, offset + 2, forkType, 0, 1);
        System.arraycopy(data, offset + 3, pad, 0, 1);
        fileID = new HFSCatalogNodeID(data, offset + 4);
        System.arraycopy(data, offset + 8, startBlock, 0, 4);
    }

    public HFSPlusExtentKey(byte forkType, HFSCatalogNodeID fileID, int startBlock) {
        System.arraycopy(Util.toByteArrayBE((short) 12), 0, this.keyLength, 0, 2);
        this.forkType[0] = forkType;
        this.pad[0] = 0;
        this.fileID = fileID;
        System.arraycopy(Util.toByteArrayBE(startBlock), 0, this.startBlock, 0, 4);
    }

    @Override
    public int length() {
        return 12;
    }

    @Override
    public short getKeyLength() {
        return Util.readShortBE(keyLength);
    }

    public byte getForkType() {
        return Util.readByteBE(forkType);
    }

    public byte getPad() {
        return Util.readByteBE(pad);
    }

    public HFSCatalogNodeID getFileID() {
        return fileID;
    }

    public int getStartBlock() {
        return Util.readIntBE(startBlock);
    }

    public int getUnsignedForkType() {
        return Util.unsign(getForkType());
    }

    public long getUnsignedStartBlock() {
        return Util.unsign(getStartBlock());
    }

    @Override
    public void printFields(PrintStream ps, String prefix) {
        ps.println(prefix + " keyLength: " + getKeyLength());
        ps.println(prefix + " forkType: " + getForkType());
        ps.println(prefix + " pad: " + getPad());
        ps.println(prefix + " fileID: ");
        getFileID().print(ps, prefix + "  ");
        ps.println(prefix + " startBlock: " + getStartBlock());
    }

    @Override
    public void print(PrintStream ps, String prefix) {
        ps.println(prefix + "HFSPlusExtentKey:");
        printFields(ps, prefix);
    }

    @Override
    public int compareTo(BTKey btk) {
        if (btk instanceof HFSPlusExtentKey extKey) {
            // fileID, forkType, startBlock
            if (getFileID().toLong() == extKey.getFileID().toLong()) {
                if (getUnsignedForkType() == extKey.getUnsignedForkType()) {
                    // getStartBlock() > extKey.getStartBlock()
                    return Long.compare(getUnsignedStartBlock(), extKey.getUnsignedStartBlock());
                } else if (getUnsignedForkType() < extKey.getUnsignedForkType())
                    return -1;
                else // getForkType() > extKey.getForkType()
                    return 1;
            } else if (getFileID().toLong() < extKey.getFileID().toLong())
                return -1;
            else // getFileID() > extKey.getFileID()
                return 1;
        } else {
            return super.compareTo(btk);
        }
    }

    @Override
    public byte[] getBytes() {
        byte[] result = new byte[length()];
        System.arraycopy(keyLength, 0, result, 0, 2);
        System.arraycopy(forkType, 0, result, 2, 1);
        System.arraycopy(pad, 0, result, 3, 1);
        System.arraycopy(Util.toByteArrayBE(fileID.toInt()), 0, result, 4, 4);
        System.arraycopy(startBlock, 0, result, 8, 4);
        return result;
    }

    @Override
    public Dictionary getStructElements() {
        DictionaryBuilder db = new DictionaryBuilder("HFSPlusExtentKey", "HFS+ extent key");

        db.addUIntBE("keyLength", keyLength, "Key length", "bytes");
        db.addUIntBE("forkType", forkType, "Fork type");
        db.addUIntBE("pad", pad, "Padding", IntegerFieldRepresentation.HEXADECIMAL);
        db.add("fileID", fileID.getOpaqueStructElement(), "File ID");
        db.addUIntBE("startBlock", startBlock, "Start block number");

        return db.getResult();
    }
}
