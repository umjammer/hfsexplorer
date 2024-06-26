/*-
 * Copyright (C) 2006-2009 Erik Larsson
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

package org.catacombae.storage.ps;

import org.catacombae.csjc.PrintableStruct;


/**
 * <pre>
 * A partition is a string of bytes. It is a substring of some possibly larger
 * string of bytes (usually representing a physical device holding the data,
 * such as a hard disk, a memory stick or an optical disc).
 *
 * A partition usually has metadata asssociated with it, facilitating the
 * interpretation of the data inside the partition. This simple abstraction
 * produces three fundamental variables:
 * the start offset, the length of the partition and the partition type.
 *
 * While most partition systems specify their offsets in sectors or blocks, the
 * unit of this general partition will be one byte, so most implementations will
 * need to convert from the native sector number to an actual byte offset/length.
 * </pre>
 *
 * @author <a href="https://catacombae.org" target="_top">Erik Larsson</a>
 */
public interface Partition extends PrintableStruct {

    /** Returns the start offset in bytes. */
    long getStartOffset();

    /** Returns the length of the partition in bytes. */
    long getLength();

    /** Returns the type of the partition. */
    PartitionType getType();
}
