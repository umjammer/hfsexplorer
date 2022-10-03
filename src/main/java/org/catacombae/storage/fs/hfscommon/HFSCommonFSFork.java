/*-
 * Copyright (C) 2008-2009 Erik Larsson
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

package org.catacombae.storage.fs.hfscommon;

import java.io.InputStream;
import java.io.OutputStream;
import org.catacombae.io.ReadableRandomAccessInputStream;
import org.catacombae.io.SynchronizedReadableRandomAccessStream;
import org.catacombae.hfs.types.hfscommon.CommonHFSForkData;
import org.catacombae.io.RandomAccessStream;
import org.catacombae.io.ReadableRandomAccessStream;
import org.catacombae.io.TruncatableRandomAccessStream;
import org.catacombae.io.WritableRandomAccessStream;
import org.catacombae.storage.fs.FSFork;
import org.catacombae.storage.fs.FSForkType;

/**
 * @author <a href="https://catacombae.org" target="_top">Erik Larsson</a>
 */
public class HFSCommonFSFork implements FSFork {
    private static final String RESOURCE_XATTR_NAME = "com.apple.ResourceFork";
    private final HFSCommonAbstractFile parent;
    private final FSForkType type;
    private final CommonHFSForkData forkData;

    protected HFSCommonFSFork(HFSCommonAbstractFile iParent, FSForkType iType,
        CommonHFSForkData iForkData)
    {
        // Input check
        if(iParent == null)
            throw new IllegalArgumentException("iParent must not be null!");
        if(iType == null)
            throw new IllegalArgumentException("iType must not be null!");
        else if(iType != FSForkType.DATA && iType != FSForkType.MACOS_RESOURCE)
            throw new IllegalArgumentException("iType is unsupported!");
        if(iForkData == null)
            throw new IllegalArgumentException("iForkData must not be null!");

        this.parent = iParent;
        this.type = iType;
        this.forkData = iForkData;
    }

    /* @Override */
    public FSForkType getType() {
        return type;
    }

    /* @Override */
    public long getLength() {
        return forkData.getLogicalSize();
    }

    public long getOccupiedSize() {
        final long blockSize =
                parent.fsHandler.getFSView().getVolumeHeader().
                getAllocationBlockSize();
        final long forkBlocks;
        if(forkData.hasTotalBlocks()) {
            forkBlocks = forkData.getTotalBlocks();
        }
        else {
            forkBlocks = (forkData.getLogicalSize() + blockSize - 1) /
                    blockSize;
        }

        return forkBlocks * blockSize;
    }

    /* @Override */
    public boolean isWritable() {
        return false; // Will be implemented in the future
    }

    /* @Override */
    public boolean isTruncatable() {
        return false; // Will be implemented in the future
    }

    /* @Override */
    public boolean isCompressed() {
        return false;
    }

    /* @Override */
    public String getForkIdentifier() {
        switch(type) {
            case DATA:
                return "Data fork";
            case MACOS_RESOURCE:
                return "Resource fork";
            default:
                throw new RuntimeException("INTERNAL ERROR: Incorrect fork " +
                        "type: " + type);
        }
    }

    /* @Override */
    public InputStream getInputStream() {
        return new ReadableRandomAccessInputStream(
                new SynchronizedReadableRandomAccessStream(
                        getReadableRandomAccessStream()));
    }

    /* @Override */
    public ReadableRandomAccessStream getReadableRandomAccessStream() {
        switch(type) {
            case DATA:
                return parent.getReadableDataForkStream();
            case MACOS_RESOURCE:
                return parent.getReadableResourceForkStream();
            default:
                throw new RuntimeException("INTERNAL ERROR: Incorrect fork " +
                        "type: " + type);
        }
    }

    /* @Override */
    public WritableRandomAccessStream getWritableRandomAccessStream() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /* @Override */
    public RandomAccessStream getRandomAccessStream() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /* @Override */
    public OutputStream getOutputStream() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /* @Override */
    public TruncatableRandomAccessStream getForkStream() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /* @Override */
    public boolean hasXattrName() {
        if(type == FSForkType.MACOS_RESOURCE)
            return true;
        else
            return false;
    }

    /* @Override */
    public String getXattrName() {
        if(type == FSForkType.MACOS_RESOURCE)
            return RESOURCE_XATTR_NAME;
        else
            return null;
    }

}
