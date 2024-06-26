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

import org.catacombae.csjc.PrintableStruct;


/**
 * @author <a href="https://catacombae.org" target="_top">Erik Larsson</a>
 */
public abstract class BTNode implements PrintableStruct {

    protected final BTNodeDescriptor nodeDescriptor;

    public BTNode(byte[] data, int offset, int nodeSize) {
        nodeDescriptor = new BTNodeDescriptor(data, offset);
    }

    public BTNodeDescriptor getNodeDescriptor() {
        return nodeDescriptor;
    }

    @Override
    public void printFields(PrintStream ps, String prefix) {
        ps.println(prefix + " nodeDescriptor:");
        nodeDescriptor.printFields(ps, prefix + "  ");
    }

    @Override
    public void print(PrintStream ps, String prefix) {
        ps.println(prefix + "BTNode:");
        printFields(ps, prefix);
    }
}
