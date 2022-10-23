/*-
 * Copyright (C) 2008-2014 Erik Larsson
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

package org.catacombae.storage.fs.hfsx;

import org.catacombae.storage.fs.hfsplus.HFSPlusFileSystemHandler;
import org.catacombae.storage.io.DataLocator;
import org.catacombae.hfs.x.HFSXVolume;

/**
 * HFSX implementation of a FileSystemHandler. This implementation can be used
 * to access HFSX file systems. (HFSX file systems are very similar to HFS+,
 * but with a few extensions, like the ability to treat file names in a case
 * sensitive manner).
 *
 * @author <a href="https://catacombae.org" target="_top">Erik Larsson</a>
 */
public class HFSXFileSystemHandler extends HFSPlusFileSystemHandler {

    public HFSXFileSystemHandler(DataLocator fsLocator, boolean useCaching,
            boolean posixNames, boolean sfmSubstitutions,
            boolean doUnicodeFileNameComposition, boolean hideProtected)
    {
        super(new HFSXVolume(fsLocator.createReadOnlyFile(), useCaching),
                posixNames, sfmSubstitutions, doUnicodeFileNameComposition,
                hideProtected);
    }
}
