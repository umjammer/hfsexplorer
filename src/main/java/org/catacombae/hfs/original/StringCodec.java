/*-
 * Copyright (C) 2008-2021 Erik Larsson
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

package org.catacombae.hfs.original;

/**
 * Transforms data between Java strings and their respective encoded byte[] forms.
 *
 * @author <a href="https://catacombae.org" target="_top">Erik Larsson</a>
 */
public interface StringCodec {

    /**
     * Decodes the specified data into a string.
     *
     * @param data the data to decode.
     * @return the decoded string.
     */
    String decode(byte[] data);

    /**
     * Decodes the specified data into a string.
     *
     * @param data the data to decode.
     * @param off  the offset in <code>data</code> to start reading at.
     * @param len  the amount of data to process.
     * @return the decoded string.
     */
    String decode(byte[] data, int off, int len);

    /**
     * Encodes the specified string into bytes.
     *
     * @param str the string to encode.
     * @return encoded data.
     */
    byte[] encode(String str);

    /**
     * Encodes the specified string into bytes.
     *
     * @param str the string to encode.
     * @param off the position to start reading in the string.
     * @param len the number of bytes to read from the string.
     * @return encoded data.
     */
    byte[] encode(String str, int off, int len);

    /**
     * Returns the charset name as a string.
     *
     * @return the charset name as a string.
     */
    String getCharsetName();

    /**
     * Exception which should be thrown only when a conversion between binary
     * data and Unicode fails.
     */
    class StringCodecException extends RuntimeException {

        public StringCodecException(String message) {
            super(message);
        }

        public StringCodecException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
