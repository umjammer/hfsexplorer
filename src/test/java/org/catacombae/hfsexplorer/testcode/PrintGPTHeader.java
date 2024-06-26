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

package org.catacombae.hfsexplorer.testcode;

import java.io.File;
import java.io.RandomAccessFile;

import org.catacombae.storage.ps.gpt.types.GPTHeader;


/**
 * @author <a href="https://catacombae.org" target="_top">Erik Larsson</a>
 */
public class PrintGPTHeader {

    public static void main(String[] args) throws Exception {
        long offset = 0;
        File sourceFile;
        if (args.length > 0)
            sourceFile = new File(args[0]);
        else {
            System.out.println("usage: PrintGPTHeader <sourceFile> [<offset>]");
            System.exit(1);
            return;
        }
        if (args.length > 1)
            offset = Long.parseLong(args[1]);

        byte[] data = new byte[512];
        RandomAccessFile sourceRaf = new RandomAccessFile(sourceFile, "r");
        sourceRaf.seek(offset);
        sourceRaf.read(data);

        GPTHeader gph = new GPTHeader(data, 0, 512);
        gph.print(System.out, "");
    }
}
