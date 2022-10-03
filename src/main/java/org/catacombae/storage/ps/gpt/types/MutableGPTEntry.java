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

package org.catacombae.storage.ps.gpt.types;

import org.catacombae.util.Util;
import java.io.PrintStream;

/**
 * @author <a href="https://catacombae.org" target="_top">Erik Larsson</a>
 */
public class MutableGPTEntry extends GPTEntry {

    private final MutableGUID mutablePartitionTypeGUID;
    private final MutableGUID mutableUniquePartitionGUID;

    public MutableGPTEntry(int blockSize) {
        super(blockSize, new MutableGUID(), new MutableGUID());

        this.mutablePartitionTypeGUID = (MutableGUID) this.partitionTypeGUID;
        this.mutableUniquePartitionGUID =
                (MutableGUID) this.uniquePartitionGUID;
    }

    public MutableGPTEntry(GPTEntry source) {
        super(source.blockSize, new MutableGUID(source.partitionTypeGUID),
                new MutableGUID(source.uniquePartitionGUID));

        this.mutablePartitionTypeGUID = (MutableGUID) this.partitionTypeGUID;
        this.mutableUniquePartitionGUID =
                (MutableGUID) this.uniquePartitionGUID;
    }

    public MutableGUID getMutablePartitionTypeGUID() { return mutablePartitionTypeGUID; }
    public MutableGUID getMutableUniquePartitionGUID() { return mutableUniquePartitionGUID; }
    public void setStartingLBA(long i) { Util.arrayCopy(Util.toByteArrayLE(i), startingLBA); }
    public void setEndingLBA(long i) { Util.arrayCopy(Util.toByteArrayLE(i), endingLBA); }
    public void setAttributeBits(long i) { Util.arrayCopy(Util.toByteArrayBE(i), attributeBits); }
    public void setPartitionName(byte[] data, int off) { copyData(data, off, partitionName); }

    public void setFields(GPTEntry gptEntry) {
	super.copyFields(gptEntry);
    }

    private static void copyData(byte[] data, int off, byte[] dest) {
	copyData(data, off, dest, dest.length);
    }
    private static void copyData(byte[] data, int off, byte[] dest, int len) {
	if(off+len > data.length)
	    throw new IllegalArgumentException("Length of input data must be " + len + ".");
	System.arraycopy(data, off, dest, 0, len);
    }
    public void print(PrintStream ps, String prefix) {
	ps.println(prefix + "MutableGPTEntry:");
	printFields(ps, prefix);
    }
}
