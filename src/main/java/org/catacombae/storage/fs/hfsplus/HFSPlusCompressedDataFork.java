/*-
 * Copyright (C) 2014 Erik Larsson
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

package org.catacombae.storage.fs.hfsplus;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import org.catacombae.hfs.types.decmpfs.DecmpfsHeader;
import org.catacombae.hfsexplorer.IOUtil;
import org.catacombae.hfsexplorer.fs.ResourceForkReader;
import org.catacombae.hfsexplorer.types.resff.ReferenceListEntry;
import org.catacombae.hfsexplorer.types.resff.ResourceMap;
import org.catacombae.hfsexplorer.types.resff.ResourceType;
import org.catacombae.io.BasicReadableRandomAccessStream;
import org.catacombae.io.RandomAccessStream;
import org.catacombae.io.ReadableByteArrayStream;
import org.catacombae.io.ReadableRandomAccessInputStream;
import org.catacombae.io.ReadableRandomAccessStream;
import org.catacombae.io.RuntimeIOException;
import org.catacombae.io.SynchronizedReadableRandomAccessStream;
import org.catacombae.io.TruncatableRandomAccessStream;
import org.catacombae.io.WritableRandomAccessStream;
import org.catacombae.storage.fs.FSFork;
import org.catacombae.storage.fs.FSForkType;
import org.catacombae.util.Util;

import static java.lang.System.getLogger;


/**
 * @author <a href="https://catacombae.org" target="_top">Erik Larsson</a>
 */
public class HFSPlusCompressedDataFork implements FSFork {

    private static final Logger logger = getLogger(HFSPlusCompressedDataFork.class.getName());

    private final FSFork decmpfsFork;
    private final FSFork resourceFork;

    private DecmpfsHeader decmpfsHeader = null;

    private boolean lengthValid = false;
    private long length = 0;

    private boolean occupiedSizeValid = false;
    private long occupiedSize = 0;

    HFSPlusCompressedDataFork(FSFork decmpfsFork, FSFork resourceFork) {
        this.decmpfsFork = decmpfsFork;
        this.resourceFork = resourceFork;
    }

    @Override
    public FSForkType getType() {
        return FSForkType.DATA;
    }

    synchronized DecmpfsHeader getDecmpfsHeader() {
        if (decmpfsHeader == null) {
            ReadableRandomAccessStream stream = decmpfsFork.getReadableRandomAccessStream();
            try {
                byte[] headerData = new byte[DecmpfsHeader.STRUCTSIZE];

                stream.readFully(headerData);

                DecmpfsHeader header = new DecmpfsHeader(headerData, 0);
                if (header.getMagic() != DecmpfsHeader.MAGIC) {
                    throw new RuntimeException("Invalid magic for decmpfs header: \"" +
                            Util.toASCIIString(header.getRawMagic()) + "\"");
                }

                decmpfsHeader = header;
            } finally {
                if (stream != null) {
                    stream.close();
                }
            }
        }

        return decmpfsHeader;
    }

    @Override
    public synchronized long getLength() {
        if (!lengthValid) {
            DecmpfsHeader header = getDecmpfsHeader();

            length = header.getRawFileSize();
            lengthValid = true;
        }

        return length;
    }

    @Override
    public synchronized long getOccupiedSize() {
        if (!occupiedSizeValid) {
            DecmpfsHeader header = getDecmpfsHeader();
            long tmpOccupiedSize;

            switch (header.getRawCompressionType()) {
                case DecmpfsHeader.COMPRESSION_TYPE_INLINE:
                    tmpOccupiedSize = decmpfsFork.getLength() - DecmpfsHeader.STRUCTSIZE;
                    break;
                case DecmpfsHeader.COMPRESSION_TYPE_RESOURCE:
                    ReadableRandomAccessStream resourceForkStream = null;
                    ResourceForkReader r = null;
                    try {
                        resourceForkStream = resourceFork.getReadableRandomAccessStream();
                        r = new ResourceForkReader(resourceForkStream);

                        ResourceMap m = r.getResourceMap();
                        Long resourceDataLength = null;

                        for (ResourceType t : m.getResourceTypeList()) {
                            if (!Util.toASCIIString(t.getType()).equals("cmpf")) {
                                continue;
                            }

                            ReferenceListEntry[] entries = m.getReferencesByType(t);
                            if (entries.length != 1) {
                                throw new RuntimeException("More than one instance (" + entries.length + ") " +
                                        "of resource type 'cmpf'.");
                            }

                            resourceDataLength = r.getDataLength(entries[0]);
                        }

                        if (resourceDataLength == null) {
                            throw new RuntimeException("No 'cmpf' resource found in resource fork.");
                        }

                        tmpOccupiedSize = resourceDataLength;
                    } finally {
                        if (r != null) {
                            r.close();
                        } else if (resourceForkStream != null) {
                            resourceForkStream.close();
                        }
                    }
                    break;
                default:
                    throw new RuntimeException("Unsupported compression type " + header.getCompressionType());
            }

            occupiedSize = tmpOccupiedSize;
            occupiedSizeValid = true;
        }

        return occupiedSize;
    }

    @Override
    public boolean isWritable() {
        return false;
    }

    @Override
    public boolean isTruncatable() {
        return false;
    }

    @Override
    public boolean isCompressed() {
        return true;
    }

    @Override
    public String getForkIdentifier() {
        return "Data fork";
    }

    @Override
    public boolean hasXattrName() {
        return false;
    }

    @Override
    public String getXattrName() {
        return null;
    }

    @Override
    public InputStream getInputStream() {
        return new ReadableRandomAccessInputStream(
                new SynchronizedReadableRandomAccessStream(getReadableRandomAccessStream()));
    }

    @Override
    public synchronized ReadableRandomAccessStream getReadableRandomAccessStream() {
        ReadableRandomAccessStream decmpfsForkStream = null;
        ReadableRandomAccessStream resourceForkStream = null;
        try {
            DecmpfsHeader header = getDecmpfsHeader();

            decmpfsForkStream = decmpfsFork.getReadableRandomAccessStream();

            ReadableRandomAccessStream dataForkStream;
            long compressionType = header.getCompressionType();

            if (compressionType == DecmpfsHeader.COMPRESSION_TYPE_INLINE) {
                // Compressed file data is stored within the decmpfs fork
                // itself.
                long fileSize = header.getRawFileSize();
                if (fileSize < 0 || fileSize > Integer.MAX_VALUE) {
                    logger.log(Level.DEBUG, "Decompressed data is too large to be stored in memory.");
                    return null;
                }

                decmpfsForkStream.seek(DecmpfsHeader.STRUCTSIZE);
                byte compressionFlags = decmpfsForkStream.readFully();

                if ((compressionFlags & 0x0F) == 0x0F) {
                    // Data is stored as an uncompressed blob in the attributes
                    // file.
                    final int uncompressedDataOffset =
                            DecmpfsHeader.STRUCTSIZE + 1;
                    long uncompressedDataLength =
                            decmpfsForkStream.length() - uncompressedDataOffset;
                    if (uncompressedDataLength < 0 ||
                            uncompressedDataLength > Integer.MAX_VALUE) {
                        logger.log(Level.DEBUG, "Uncompressed data is too large to be stored in memory.");
                        return null;
                    }

                    if (uncompressedDataLength != fileSize) {
                        logger.log(Level.DEBUG, "[WARNING] decmpfs compression type 3 uncompressed data length " +
                                "(" + uncompressedDataLength + " doesn't match file size (" + fileSize + ").");
                    }

                    byte[] uncompressedData =
                            IOUtil.readFully(decmpfsForkStream, uncompressedDataOffset, (int) fileSize);

                    dataForkStream =
                            new ReadableByteArrayStream(uncompressedData);
                } else {
                    // Data is stored as a compressed blob in the attributes
                    // file.

                    // Decompress data in memory and return a stream for reading
                    // from the resulting memory buffer.
                    final int compressedDataOffset = DecmpfsHeader.STRUCTSIZE;
                    long compressedDataLength = decmpfsForkStream.length() - compressedDataOffset;
                    if (compressedDataLength < 0 || compressedDataLength > Integer.MAX_VALUE) {
                        logger.log(Level.DEBUG, "Compressed data is too large to be stored in memory.");
                        return null;
                    }

                    byte[] compressedData =
                            IOUtil.readFully(decmpfsForkStream, compressedDataOffset, (int) compressedDataLength);

                    Inflater inflater = new Inflater(false);
                    inflater.setInput(compressedData);

                    byte[] outBuffer = new byte[(int) fileSize];
                    try {
                        inflater.inflate(outBuffer);
                    } catch (DataFormatException ex) {
                        logger.log(Level.DEBUG, "Invalid compressed data in decmpfs attribute. Exception stack trace:");
                        logger.log(Level.ERROR, ex.getMessage(), ex);
                        return null;
                    }

                    boolean inflaterFinished = inflater.finished();
                    inflater.end();

                    if (!inflaterFinished) {
                        logger.log(Level.DEBUG, "Decompression failed. All input was not processed.");
                        return null;
                    }

                    dataForkStream = new ReadableByteArrayStream(outBuffer);
                }
            } else if (compressionType == DecmpfsHeader.COMPRESSION_TYPE_RESOURCE) {
                // Compressed file data is stored in the resource fork.
                resourceForkStream = resourceFork.getReadableRandomAccessStream();

                ResourceForkReader resReader = new ResourceForkReader(resourceForkStream);
                ResourceMap map = resReader.getResourceMap();

                ResourceType cmpfType = null;
                for (ResourceType curType : map.getResourceTypeList()) {
                    if (Util.toASCIIString(curType.getType()).equals("cmpf")) {
                        if (curType.getInstanceCount() > 0) {
                            logger.log(Level.DEBUG, "Resource fork har more than 1 instance of \"cmpf\" resource (" +
                                    (curType.getInstanceCount() + 1) + " instances). Don't know how to handle " +
                                    "this...");
                            return null;
                        }

                        cmpfType = curType;
                        break;
                    }
                }

                if (cmpfType == null) {
                    logger.log(Level.DEBUG, "No \"cmpf\" resource found in resource fork.");
                    return null;
                }

                ReferenceListEntry[] referenceListEntries = map.getReferencesByType(cmpfType);

                if (referenceListEntries.length != 1) {
                    logger.log(Level.DEBUG, "Unexpected length of returned reference list entry array (expected: 1, " +
                            "actual: " + referenceListEntries.length);
                    return null;
                }

                dataForkStream = new CompressedResourceStream(
                        resReader.getResourceStream(referenceListEntries[0]), header.getRawFileSize());
            } else {
                logger.log(Level.DEBUG, "Unknown decmpfs compression type: " + compressionType);

                return null;
            }

            return dataForkStream;
        } finally {
            if (resourceForkStream != null) {
                resourceForkStream.close();
            }

            if (decmpfsForkStream != null) {
                decmpfsForkStream.close();
            }
        }
    }

    @Override
    public WritableRandomAccessStream getWritableRandomAccessStream() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public RandomAccessStream getRandomAccessStream() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public OutputStream getOutputStream() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public TruncatableRandomAccessStream getForkStream() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    boolean isUsingResourceFork() {
        return getDecmpfsHeader().getCompressionType() == DecmpfsHeader.COMPRESSION_TYPE_RESOURCE;
    }

    private static class CompressedResourceStream extends BasicReadableRandomAccessStream {
        /* The compressed stream is divided into blocks, where each block is a
         * separate compression unit and can be individually decompressed.
         * The structure of the stream is as follows:
         *     le32 blockCount;
         *     struct {
         *         le32 blockOffset;
         *         le32 blockLength;
         *     } blockTable[blockCount];
         *     u8[...] compressedData;
         *
         * It is unclear if the whole resource stream must be rewritten when
         * data is updated (if not, then there's something about the compressed
         * format that we do not yet understand).
         * It is also unclear if the blocks always have the same uncompressed
         * size, i.e. if we can rely on this for seeking and in this case if the
         * block size is always equal to the file system block size. For now we
         * decompress every block when seeking, which is slow. */

        private final ReadableRandomAccessStream resourceStream;
        private final long uncompressedSize;
        private final int blockCount;
        private final byte[] blockTableData;
        private final Inflater inflater = new Inflater(true);

        private long fp = 0;

        private int processedBlocks = 0;
        private int fixedBlockSize = 0;
        private long[] nextBlockOffsets = null;

        public CompressedResourceStream(ReadableRandomAccessStream resourceStream, long uncompressedSize) {
            this.resourceStream = resourceStream;
            this.uncompressedSize = uncompressedSize;

            byte[] blockCountData = new byte[4];
            this.resourceStream.seek(0);
            this.resourceStream.readFully(blockCountData);
            this.blockCount = Util.readIntLE(blockCountData);

            logger.log(Level.DEBUG, "[CompressedResourceStream.<init>] blockCount=" + blockCount);

            this.blockTableData = new byte[this.blockCount * (2 * 4)];
            this.resourceStream.readFully(this.blockTableData);
            logger.log(Level.DEBUG, "[CompressedResourceStream.<init>] Block table data:");
            for (int i = 0; i < blockCount; ++i) {
                logger.log(Level.DEBUG, "[CompressedResourceStream.<init>]     " + i + ": offset=" +
                        Util.readIntLE(blockTableData, 2 * 4 * i) + ", length=" +
                        Util.readIntLE(blockTableData, 2 * 4 * i + 4));
            }
        }

        @Override
        public synchronized void close() throws RuntimeIOException {
            resourceStream.close();
        }

        @Override
        public synchronized void seek(long pos) throws RuntimeIOException {
            if (pos < 0) {
                throw new RuntimeIOException("Negative seek offset: " + pos);
            }

            fp = pos;
        }

        @Override
        public long length() throws RuntimeIOException {
            return uncompressedSize;
        }

        @Override
        public synchronized long getFilePointer() throws RuntimeIOException {
            return fp;
        }

        @Override
        public synchronized int read(byte[] data, int pos, int len) throws RuntimeIOException {
            logger.log(Level.TRACE, "[CompressedResourceStream.read(byte[], " +
                    "int, int)] Called with data=" + data + ", pos=" + pos + ", len=" + len + "...");

            // Input check.
            if (data == null) {
                throw new IllegalArgumentException("data == null");
            } else if (pos < 0) {
                throw new IllegalArgumentException("pos < 0");
            } else if (len < 0) {
                throw new IllegalArgumentException("len < 0");
            }

            // Read is completely beyond end of file => -1 (EOF).
            if (fp >= uncompressedSize) {
                return -1;
            }

            // Read is partially beyond end of file => truncate len.
            if (len > uncompressedSize || fp > (uncompressedSize - len)) {
                len = (int) (uncompressedSize - fp);
            }

            int curBlock;
            long curFp;
            long curBlockOffset;

            if (processedBlocks == 0 || fixedBlockSize == 0) {
                // Either no blocks have been processed previously, or the
                // uncompressed block size is not fixed. In both cases we must
                // start from the beginning (though if we have processed some
                // blocks before fp previously we will still be able to use
                // cached data to speed up the seek).
                curBlock = 0;
                curFp = 0;
                curBlockOffset = 0;
            } else {
                // We have previously processed some blocks, and the
                // uncompressed block size has been identical so far, so we can
                // calculate the uncompressed offset with one simple
                // operation.
                long requestedBlock = fp / fixedBlockSize;

                if (requestedBlock > processedBlocks) {
                    curBlock = processedBlocks + 1;
                    curFp = (long) curBlock * fixedBlockSize;
                    curBlockOffset = curFp;
                } else {
                    curBlock = (int) requestedBlock;
                    curFp = fp;
                    curBlockOffset = (long) curBlock * fixedBlockSize;
                }
            }

            long endFp = fp + len;
            if (endFp > uncompressedSize) {
                endFp = uncompressedSize;
            }

            byte[] compressedBuffer = null;
            byte[] decompressedBuffer = new byte[16 * 1024];
            int bytesRead = 0;

            while (curFp < endFp) {
                boolean skip;

                logger.log(Level.DEBUG, "[CompressedResourceStream.read(byte[], int, int)]     Iterating... curFp " +
                        "(" + curFp + ") < endFp (" + endFp + ")");

                if (curBlock < processedBlocks) {
                    long nextBlockOffset;

                    // We have cached data from a previous access.
                    if (fixedBlockSize != 0) {
                        // Fixed block size => we can easily calculate the
                        // offset of the next block.
                        nextBlockOffset = curBlockOffset + fixedBlockSize;
                    } else if (nextBlockOffsets == null) {
                        throw new RuntimeException("Unexpected: fixedBlockSize == 0 but no blockOffsets array!");
                    } else {
                        // Variable block size => we must look up the offset of
                        // the next block in nextBlockOffsets.
                        nextBlockOffset = nextBlockOffsets[curBlock];
                    }

                    // Now that we have the offset of the next block, we can
                    // determine whether this block can be skipped i.e. whether
                    // it is fully located before 'fp'.
                    skip = nextBlockOffset <= fp;
                } else {
                    // We do not know anything about this block, so we cannot
                    // skip past it.
                    skip = false;
                }

                if (!skip) {
                    // We cannot skip over this block since it might be within
                    // the range of data that we are requesting. So read the
                    // compressed data from disk and decompress it.
                    int curOffset = Util.readIntLE(blockTableData, curBlock * (2 * 4));
                    int curLength = Util.readIntLE(blockTableData, curBlock * (2 * 4) + 4);

                    int curDecompressedOffsetInBlock = 0;

                    resourceStream.seek(curOffset);
                    byte compressionFlags = resourceStream.readFully();

                    if ((compressionFlags & 0x0F) == 0x0F) {
                        // Block is not compressed... just copy from input to
                        // output.
                        int rawDataOffset = curOffset + 1;
                        int rawDataLength = curLength - 1;

                        logger.log(Level.DEBUG, "[CompressedResourceStream.read(byte[], int, int)]     Copying " +
                                "raw data at logical offset " + curFp + ": [offset=" + rawDataOffset +
                                ", length=" + rawDataLength + "]");

                        int remainingLen = len - bytesRead;
                        int remainingInBlock = rawDataLength;
                        int curBytesToRead = Math.min(remainingInBlock, remainingLen);

                        // If the raw data is requested by the caller, i.e. if
                        // the file pointer is within the current block as we
                        // know it so far (file pointer is greater than the
                        // start of the block and less than the current
                        // decompressed end offset, then copy this data to the
                        // destination array.
                        if (fp >= curBlockOffset &&
                                fp < (curBlockOffset + curLength)) {
                            resourceStream.readFully(data, pos + bytesRead,
                                    curBytesToRead);
                        }

                        curDecompressedOffsetInBlock = curLength;
                    } else {
                        // Block is compressed. Decompression is necessary.

                        // Read compressed block into memory. We assume that it
                        // will not be too large to fit in memory. If this
                        // assumption breaks we can restructure the code to read
                        // the compressed data in chunks, but let's not do that
                        // now since it will only complicate things.

                        int compressedDataOffset = curOffset + 2;
                        int compressedDataLength = curLength - 2;

                        logger.log(Level.DEBUG, "[CompressedResourceStream.read(byte[], int, int)]     " +
                                "Decompressing compressed data at logical offset " + curFp + ": [offset=" +
                                compressedDataOffset + ", length=" + compressedDataLength + "]");

                        if (compressedBuffer == null ||
                                compressedBuffer.length < compressedDataLength) {
                            if (compressedBuffer != null) {
                                // Explicitly let GC reclaim old buffer when we
                                // are allocating the new one to avoid running
                                // out of memory due to unnecessary references
                                // to old allocations (the VM is probably smart
                                // enough so that this isn't necessary, but just
                                // in case).
                                compressedBuffer = null;
                            }

                            compressedBuffer = new byte[compressedDataLength];
                        }

                        resourceStream.seek(compressedDataOffset);
                        resourceStream.readFully(compressedBuffer, 0, compressedDataLength);

                        // Decompress data in current block.

                        inflater.reset();
                        inflater.setInput(compressedBuffer, 0,
                                compressedDataLength);

                        while (!inflater.finished()) {
                            int inflatedBytes;
                            try {
                                inflatedBytes = inflater.inflate(decompressedBuffer);
                            } catch (DataFormatException ex) {
                                throw new RuntimeException("Invalid compressed data in resource fork " +
                                        "(" + ex + ").", ex);
                            }

                            logger.log(Level.DEBUG, "Inflated " + inflatedBytes +
                                    " to decompressedBuffer (length: " + decompressedBuffer.length + ").");

                            if (inflatedBytes <= 0) {
                                throw new RuntimeIOException("No (" + inflatedBytes + ") inflated " +
                                        "bytes. inflater.needsInput()=" + inflater.needsInput() + " " +
                                        "inflater.needsDictionary()=" + inflater.needsDictionary());
                            }

                            // If the decompressed data is requested by the
                            // caller, i.e. if the file pointer is within the
                            // current block as we know it so far (file pointer
                            // is greater than the start of the block and less
                            // than the current decompressed end offset, then
                            // copy this data to the destination array.
                            long curOffsetInBlock = fp - curBlockOffset;
                            if (curOffsetInBlock >= curDecompressedOffsetInBlock &&
                                    curOffsetInBlock < (curDecompressedOffsetInBlock + inflatedBytes)) {
                                int inOffset = (int) (curOffsetInBlock - curDecompressedOffsetInBlock);
                                int remainingBytes = len - bytesRead;
                                int copyLength = Math.min(remainingBytes, inflatedBytes);

                                logger.log(Level.DEBUG, "Copying " + copyLength +
                                        " bytes from decompressedBuffer @ " + inOffset + " to data @ " +
                                        (pos + bytesRead) + " (curDecompressedOffsetInBlock=" +
                                        curDecompressedOffsetInBlock + ", fp=" + fp + ", curBlockOffset=" +
                                        curBlockOffset + ")...");

                                System.arraycopy(decompressedBuffer, inOffset, data, pos + bytesRead, copyLength);

                                fp += copyLength;
                                bytesRead += copyLength;
                            } else {
                                logger.log(Level.DEBUG, "Skipping copy of data outside bounds of read. " +
                                        "fp=" + fp + " curBlockOffset=" + curBlockOffset + " " +
                                        "curOffsetInBlock=" + curOffsetInBlock + " " +
                                        "curDecompressedOffsetInBlock=" + curDecompressedOffsetInBlock + " " +
                                        "inflatedBytes=" + inflatedBytes);
                            }

                            curDecompressedOffsetInBlock += inflatedBytes;
                            curFp += inflatedBytes;
                        }

                        logger.log(Level.DEBUG, "Inflater is finished.");
                    }

                    if (curBlock == processedBlocks) {
                        // Current block has been decompressed. Update info
                        // about this block since we haven't visited it
                        // before.
                        if (processedBlocks == 0) {
                            fixedBlockSize = curDecompressedOffsetInBlock;
                        } else if (fixedBlockSize == 0 || curDecompressedOffsetInBlock != fixedBlockSize) {
                            if (nextBlockOffsets == null || nextBlockOffsets.length < (processedBlocks + 1)) {
                                long[] oldNextBlockOffsets = nextBlockOffsets;

                                nextBlockOffsets = new long[processedBlocks + 1];
                                if (oldNextBlockOffsets != null) {
                                    System.arraycopy(oldNextBlockOffsets, 0,
                                            nextBlockOffsets, 0, oldNextBlockOffsets.length);
                                } else {
                                    for (int i = 0; i < processedBlocks; ++i) {
                                        nextBlockOffsets[i] = (long) (i + 1) * fixedBlockSize;
                                    }
                                }
                            }

                            nextBlockOffsets[processedBlocks] =
                                    nextBlockOffsets[processedBlocks - 1] + curDecompressedOffsetInBlock;
                            fixedBlockSize = 0;
                        }

                        ++processedBlocks;
                    } else if (curBlock > processedBlocks) {
                        throw new RuntimeException("Internal error: Went beyond processed blocks.");
                    }
                }

                curBlockOffset += fixedBlockSize != 0 ? fixedBlockSize :
                        nextBlockOffsets[curBlock] - (curBlock == 0 ? 0 : nextBlockOffsets[curBlock - 1]);
                ++curBlock;
            }

            logger.log(Level.DEBUG, "[CompressedResourceStream.read(byte[], int, int)] Leaving with " +
                    (bytesRead == 0 ? -1 : bytesRead) + ".");

            return bytesRead == 0 ? -1 : bytesRead;
        }
    }
}
