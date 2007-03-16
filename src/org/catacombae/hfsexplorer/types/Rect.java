/*-
 * Copyright (C) 2006 Erik Larsson
 * 
 * All rights reserved.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 */

package org.catacombae.hfsexplorer.types;

import org.catacombae.hfsexplorer.Util;
import org.catacombae.hfsexplorer.Util2;
import java.io.PrintStream;

/** This class was generated by CStructToJavaClass. */
public class Rect {
    /*
     * struct Rect
     * size: 8 bytes
     * description: 
     * 
     * BP  Size  Type    Identifier  Description
     * -----------------------------------------
     * 0   2     SInt16  top                    
     * 2   2     SInt16  left                   
     * 4   2     SInt16  bottom                 
     * 6   2     SInt16  right                  
     */
    
    private final byte[] top = new byte[2];
    private final byte[] left = new byte[2];
    private final byte[] bottom = new byte[2];
    private final byte[] right = new byte[2];
    
    public Rect(byte[] data, int offset) {
	System.arraycopy(data, offset+0, top, 0, 2);
	System.arraycopy(data, offset+2, left, 0, 2);
	System.arraycopy(data, offset+4, bottom, 0, 2);
	System.arraycopy(data, offset+6, right, 0, 2);
    }
    
    public static int length() { return 8; }
    
    public short getTop() { return Util.readShortBE(top); }
    public short getLeft() { return Util.readShortBE(left); }
    public short getBottom() { return Util.readShortBE(bottom); }
    public short getRight() { return Util.readShortBE(right); }
    
    public String toString() { return "(" + getTop() + "," + getLeft() + "," + getBottom() + "," + getRight() + ")"; }
    
    public void printFields(PrintStream ps, String prefix) {
	ps.println(prefix + " top: " + getTop());
	ps.println(prefix + " left: " + getLeft());
	ps.println(prefix + " bottom: " + getBottom());
	ps.println(prefix + " right: " + getRight());
    }
    
    public void print(PrintStream ps, String prefix) {
	ps.println(prefix + "Rect:");
	printFields(ps, prefix);
    }
}
