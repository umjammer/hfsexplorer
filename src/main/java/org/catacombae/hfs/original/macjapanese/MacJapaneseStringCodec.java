/*-
 * Copyright (C) 2021 Erik Larsson
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

package org.catacombae.hfs.original.macjapanese;

import java.io.ByteArrayOutputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.HashMap;
import java.util.Map.Entry;

import org.catacombae.hfs.original.SingleByteCodepageStringCodec;
import org.catacombae.hfs.original.StringCodec;
import org.catacombae.util.Util;

import static java.lang.System.getLogger;


/**
 * StringCodec that decodes Mac OS Japanese.
 *
 * @author <a href="https://catacombae.org" target="_top">Erik Larsson</a>
 */
public class MacJapaneseStringCodec implements StringCodec {

    private static final Logger logger = getLogger(MacJapaneseStringCodec.class.getName());

    private static final HashMap<Short, String> macJapaneseToUnicodeMap;

    private static final HashMap<String, Short> unicodeToMacJapaneseMap;

    private final SingleByteCodepageStringCodec fallbackCodec;

    /**
     * Creates a new {@link MacJapaneseStringCodec}.
     *
     * @param fallbackCodec (optional) The fully mapped 8-bit charset codec to use for byte
     *                      sequences that don't exist in the MacJapanese encoding. This can be
     *                      <code>null</code> in which case an exception is thrown when
     *                      encountering such sequences.
     */
    public MacJapaneseStringCodec(SingleByteCodepageStringCodec fallbackCodec) {
        this.fallbackCodec = fallbackCodec;
    }

    /**
     * Creates a new {@link MacJapaneseStringCodec} without a fallback codec.
     */
    public MacJapaneseStringCodec() {
        this(null);
    }

    @Override
    public String decode(byte[] data) {
        return decode(data, 0, data.length);
    }

    @Override
    public String decode(byte[] data, int off, int len) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < len; ) {
            byte firstByte = data[off + i];
            Byte secondByte = (i + 1 < len) ? data[off + i + 1] : null;
            short sequence = 0;
            String replacement = null;

            logger.log(Level.DEBUG, "data[" + (off + i) + "]: 0x" + Util.toHexStringBE(firstByte));

            if (secondByte != null && (firstByte & 0xFF) >= 0xF0 &&
                    (firstByte & 0xFF) <= 0xFC && (secondByte & 0xFF) >= 0x40 &&
                    (secondByte & 0xFF) <= 0xFC && (secondByte & 0xFF) != 0x7F) {
                // Shift-JIS user-defined range.
                //
                // This is mapped a bit weirdly as the mapping starts at offset
                // 0xF040 and goes on for 0x3F (63) entries, skips over entry
                // 0x40 (64), continues for 0x3D (61) entries, then skips over
                // 0x43 (67) entries and resumes mapping at 0xF140, repeating
                // this pattern until the end of the range at 0xFCFC. */
                char replacementChar =
                        (char) (0xE000 + ((firstByte & 0xFF) - 0xF0) * 0xBC +
                                ((secondByte & 0xFF) - ((secondByte & 0xFF) >= 0x80 ? 0x41 : 0x40)));

                replacement = Character.valueOf(replacementChar).toString();
                i += 2;
            }

            if (replacement == null && secondByte != null) {
                //
                // First check if there's a match for the 2-byte sequence
                // (greedy matching).
                // Note: Big endian
                //

                sequence =
                        (short) (((firstByte & 0xFF) << 8) |
                                (secondByte & 0xFF));
                replacement = macJapaneseToUnicodeMap.get(sequence);
                if (replacement != null) {
                    logger.log(Level.DEBUG, "data[" + (off + i) + "]: " +
                            "0x" + Util.toHexStringBE(firstByte));
                    i += 2;
                }
            }

            if (replacement == null) {
                sequence = (short) (firstByte & 0xFF);
                if (sequence < 0x20) {
                    replacement = String.valueOf((char) sequence);
                } else {
                    replacement = macJapaneseToUnicodeMap.get(sequence);
                }

                if (replacement != null) {
                    ++i;
                }
            }

            if (replacement == null && fallbackCodec != null) {
                sequence = (short) (firstByte & 0xFF);
                logger.log(Level.DEBUG, "Querying fallback codec for missing replacement " +
                        "for 0x" + Util.toHexStringBE((byte) sequence) + "...");
                replacement = fallbackCodec.decode(data, off + i - 1, 1);

                if (replacement != null) {
                    ++i;
                }
            }

            if (replacement == null) {
                throw new StringCodecException("Unable to decode sequence at " +
                        "byte " + i + ": 0x" + Util.toHexStringBE(firstByte) +
                        ((secondByte != null) ? Util.toHexStringBE(secondByte) : ""));
            }

            if (logger.isLoggable(Level.DEBUG)) {
                StringBuilder messageBuilder = new StringBuilder();

                messageBuilder.append("Found replacement: 0x").append(Util.toHexStringBE(sequence)).append(" ->");
                for (int j = 0; j < replacement.length(); ++j) {
                    messageBuilder.append(" 0x").append(Util.toHexStringBE(replacement.charAt(j)));
                }
                messageBuilder.append(" (\"").append(replacement).append("\")");

                logger.log(Level.DEBUG, messageBuilder.toString());
            }

            sb.append(replacement);
        }

        return sb.toString();
    }

    @Override
    public byte[] encode(String str) {
        return encode(str, 0, str.length());
    }

    @Override
    public byte[] encode(String str, int off, int len) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        for (int i = 0; i < len; ) {
            int remaining = len - i;
            char firstChar;
            Short replacement = null;

            firstChar = str.charAt(off + i);
            if (firstChar < 0x20) {
                replacement = (short) firstChar;
                ++i;
            } else if (firstChar >= 0xE000 && firstChar <= 0xE98B) {
                // Shift-JIS user-defined range.
                //
                // This is mapped a bit weirdly as the mapping starts at offset
                // 0xF040 and goes on for 0x3F (63) entries, skips over entry
                // 0x40 (64), continues for 0x3D (61) entries, then skips over
                // 0x43 (67) entries and resumes mapping at 0xF140, repeating
                // this pattern until the end of the range at 0xFCFC. */
                int rangeIndex = firstChar - 0xE000;

                // Which 'chunk' of the repeating pattern are we at? For every
                // chunk we must add 0x44 to compensate for the gap between the
                // chunks (0x43 bytes) and the 1-byte gap between 0x7E and 0x80
                // (avoiding the 0x7F / DEL control character). */
                int rangeChunk = rangeIndex / 0xBC;
                int indexInChunk = rangeIndex % 0xBC;

                logger.log(Level.TRACE, "rangeIndex: " + rangeIndex);
                logger.log(Level.TRACE, "rangeChunk: " + rangeChunk);
                logger.log(Level.TRACE, "indexInChunk: " + indexInChunk);
                replacement = (short) ((0xF040 + rangeChunk * 0x44 + (indexInChunk > 0x3E ? 1 : 0) + rangeIndex) & 0xFFFF);
                logger.log(Level.TRACE, "replacement: 0x" + Util.toHexStringBE(replacement));
                ++i;
            } else {
                for (int j = 5; j > 0; --j) {
                    if (remaining >= j) {
                        // Check if the j-character substring has a match.
                        replacement = unicodeToMacJapaneseMap.get(str.substring(off + i, j));
                        if (replacement != null) {
                            i += j;
                            break;
                        }
                    }
                }
            }

            if (replacement == null && fallbackCodec != null) {
                byte[] data = fallbackCodec.encode(str.substring(off + i, 1));
                if (data == null || data.length != 1) {
                    throw new StringCodecException("Unexpected data length " +
                            "from fallback codec: " + (data != null ? data.length : 0));
                }

                replacement = (short) (data[0] & 0xFF);
            }

            if (replacement == null) {
                throw new StringCodecException("Unable to encode sequence at " +
                        "character " + i + ": 0x" + Util.toHexStringBE(firstChar));
            }

            if ((replacement & 0xFFFF) > 0xFF) {
                os.write((replacement >>> 8) & 0xFF);
            }

            os.write(replacement & 0xFF);
        }

        return os.toByteArray();
    }

    /**
     * Returns the charset name as it was passed to the constructor.
     *
     * @return the charset name as it was passed to the constructor.
     */
    @Override
    public String getCharsetName() {
        return "MacJapanese";
    }

    static {
        macJapaneseToUnicodeMap = new HashMap<>();
        for (int i = 0; i < MacJapaneseStringCodecData.mappingTable.length; ++i) {
            short key = (short) MacJapaneseStringCodecData.mappingTable[i][0];
            String value = new String(MacJapaneseStringCodecData.mappingTable[i],
                    1, MacJapaneseStringCodecData.mappingTable[i].length - 1);
            macJapaneseToUnicodeMap.put(key, value);
        }

        for (int i = 0; i < MacJapaneseStringCodecData2.mappingTable.length; ++i) {
            short key = (short) MacJapaneseStringCodecData2.mappingTable[i][0];
            String value = new String(MacJapaneseStringCodecData2.mappingTable[i],
                    1, MacJapaneseStringCodecData2.mappingTable[i].length - 1);
            macJapaneseToUnicodeMap.put(key, value);
        }

        for (int i = 0; i < MacJapaneseStringCodecData3.mappingTable.length; ++i) {
            short key = (short) MacJapaneseStringCodecData3.mappingTable[i][0];
            String value = new String(MacJapaneseStringCodecData3.mappingTable[i],
                    1, MacJapaneseStringCodecData3.mappingTable[i].length - 1);
            macJapaneseToUnicodeMap.put(key, value);
        }

        unicodeToMacJapaneseMap = new HashMap<>();
        for (Entry<Short, String> entry : macJapaneseToUnicodeMap.entrySet()) {
            unicodeToMacJapaneseMap.put(entry.getValue(), entry.getKey());
        }
    }

    static boolean testShiftJisReservedRange() {
        final int rangeFirst = 0xE000;
        final int rangeEnd = 0xE98B + 1;
        int contiguous = 0;
        byte[] prevEncoded = null;

        MacJapaneseStringCodec c = new MacJapaneseStringCodec();
        for (int i = rangeFirst; i <= rangeEnd; ++i) {
            String original;
            byte[] encoded;

            if (i != rangeEnd) {
                original = Character.valueOf((char) i).toString();
                encoded = c.encode(original);
                logger.log(Level.DEBUG, "original: " + original);
            } else {
                original = null;
                encoded = null;
            }

            if (prevEncoded != null && (encoded == null || encoded.length != prevEncoded.length ||
                    encoded[encoded.length - 1] != prevEncoded[encoded.length - 1] + contiguous)) {
                logger.log(Level.DEBUG, "0x");
                if (contiguous > 1) {
                    for (int j = 0; j < prevEncoded.length - 1; ++j) {
                        logger.log(Level.DEBUG, String.format("%02X", prevEncoded[j] & 0xFF));
                    }
                    logger.log(Level.DEBUG, String.format("%02X-0x", prevEncoded[prevEncoded.length - 1] & 0xFF));
                }
                for (int j = 0; j < prevEncoded.length - 1; ++j) {
                    logger.log(Level.DEBUG, String.format("%02X", prevEncoded[j] & 0xFF));
                }
                logger.log(Level.DEBUG, String.format("%02X -> ", (prevEncoded[prevEncoded.length - 1] & 0xFF) + (contiguous - 1)));
                if (contiguous > 1) {
                    logger.log(Level.DEBUG, String.format("0x%04X-", i - contiguous));
                }
                logger.log(Level.DEBUG, String.format("0x%04X", i - 1));
                logger.log(Level.DEBUG, "\n");

                contiguous = 1;
            } else {
                ++contiguous;
            }

            if (contiguous == 1) {
                prevEncoded = encoded;
            }

            if (original == null || encoded == null) {
                break;
            }

            String decoded = c.decode(encoded);
            if (!decoded.equals(original)) {
                logger.log(Level.DEBUG, "FAIL: Round-trip conversion does not yield identical result!");
                logger.log(Level.DEBUG, "      Original: \"" + original + "\" (");
                for (int j = 0; j < original.length(); ++j) {
                    logger.log(Level.DEBUG, (j != 0 ? " " : "") + Util.toHexStringBE(original.charAt(j)));
                }
                logger.log(Level.DEBUG, ")");
                logger.log(Level.DEBUG, "      Round-trip: \"" + decoded + "\" (");
                for (int j = 0; j < decoded.length(); ++j) {
                    logger.log(Level.DEBUG, (j != 0 ? " " : "") + Util.toHexStringBE(decoded.charAt(j)));
                }
                logger.log(Level.DEBUG, ")");
                return false;
            }
        }

        return true;
    }

//    public static void main(String[] args) {
//        boolean b;
//
//        logger.log(Level.DEBUG, "Testing Shift-JIS reserved range...");
//        try {
//            b = testShiftJisReservedRange();
//        } catch (Throwable t) {
//            t.printStackTrace(System.err);
//            b = false;
//        }
//        logger.log(Level.DEBUG, "Testing Shift-JIS reserved range: " +
//                (b ? "PASS" : "FAIL"));
//
//        System.exit(b ? 1 : 0);
//    }
}
