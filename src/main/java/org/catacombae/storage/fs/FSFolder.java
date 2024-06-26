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

package org.catacombae.storage.fs;

/**
 * This class represents an entry corresponding to a folder (directory) in the
 * file system presented by a FileSystemHandler. A folder is an entry which
 * holds subentries, like other files, folders or special files. A folder has no
 * associated data, other than the file system metadata such as attributes.
 *
 * @author <a href="https://catacombae.org" target="_top">Erik Larsson</a>
 */
public interface FSFolder extends FSEntry {

    /**
     * Lists the contents of this folder as an array of the names of its subentries.
     *
     * @return the contents of this folder as an array of the names of its subentries.
     */
    String[] list();

    /**
     * Returns the contents of this folder as an array of FSEntries. This method is more expensive
     * than <code>list()</code> since all the attributes of each subentry is also retrieved and
     * put into an FSEntry object.
     *
     * @return the contents of this folder as an array of FSEntries.
     */
    FSEntry[] listEntries();

    /**
     * Returns a named child entry in this directory, or <code>null</code> if no
     * entry by the name <code>childName</code> was found.
     *
     * @param childName the name of the child entry to fetch.
     * @return the requested child entry if existent, or <code>null</code>
     * otherwise.
     */
    FSEntry getChild(String childName);

    /**
     * Returns the valence of this folder, i.e. how many subentries this folder
     * holds.
     *
     * @return the valence of this folder.
     */
    long getValence();
}
