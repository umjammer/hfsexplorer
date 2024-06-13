/*-
 * Copyright (C) 2016 Erik Larsson
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

package org.catacombae.hfsexplorer;

import java.net.URL;


/**
 * Interface defining common constants for accessing HFSExplorer's resources.
 *
 * @author <a href="https://catacombae.org" target="_top">Erik Larsson</a>
 */
public interface Resources {

    URL FOLDER_ICON = ClassLoader.getSystemResource("res/folder.png");

    URL EMPTY_DOCUMENT_ICON = ClassLoader.getSystemResource("res/emptydocument.png");

    URL EMPTY_ICON = ClassLoader.getSystemResource("res/nothing.png");

    URL FORWARD_ICON = ClassLoader.getSystemResource("res/forward.png");

    URL EXTRACT_ICON = ClassLoader.getSystemResource("res/extract.png");

    URL BACK_ICON = ClassLoader.getSystemResource("res/back.png");

    URL UP_ICON = ClassLoader.getSystemResource("res/folderup.png");

    URL INFO_ICON = ClassLoader.getSystemResource("res/info.png");

    URL APPLICATION_ICON_16 = ClassLoader.getSystemResource("res/finderdrive_folderback_16.png");

    URL APPLICATION_ICON_32 = ClassLoader.getSystemResource("res/finderdrive_folderback_32.png");

    URL APPLICATION_ICON_48 = ClassLoader.getSystemResource("res/finderdrive_folderback_48.png");
}
