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

package org.catacombae.storage.ps.gpt;

import org.catacombae.storage.ps.PartitionSystemRecognizer;
import org.catacombae.io.ReadableRandomAccessStream;
import org.catacombae.storage.ps.gpt.types.GPTHeader;


/**
 * @author <a href="https://catacombae.org" target="_top">Erik Larsson</a>
 */
public class GPTRecognizer implements PartitionSystemRecognizer {

    @Override
    public boolean detect(ReadableRandomAccessStream fsStream, long offset, long length) {
        try {
//            ReadableRandomAccessStream llf = data.createReadOnlyFile();
            byte[] secondBlock = new byte[512];
            fsStream.seek(512);
            fsStream.readFully(secondBlock);

            // Look for GPT
            // Let's assume that blocks are always 512 bytes in size with MBR and GPT. I don't know
            // how to detect the actual block size (at least when reading from a file, otherwise I
            // guess there are system specific ways)...
            GPTHeader gh = new GPTHeader(secondBlock, 0, 512);
            if (gh.isValid()) {
                return true;
            }
        } catch (Exception e) {
        }

        return false;
    }
}
