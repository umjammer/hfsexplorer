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

package org.catacombae.storage.ps.container.hfs;

import org.catacombae.io.ReadableRandomAccessStream;
import org.catacombae.storage.io.DataLocator;
import org.catacombae.storage.fs.FileSystemMajorType;
import org.catacombae.storage.fs.hfscommon.HFSCommonFileSystemRecognizer;
import org.catacombae.storage.ps.PartitionSystemType;
import org.catacombae.storage.ps.container.ContainerHandler;
import org.catacombae.storage.ps.container.ContainerType;


/**
 * @author <a href="https://catacombae.org" target="_top">Erik Larsson</a>
 */
public class HFSContainerHandler extends ContainerHandler {

    private static final short SIGNATURE_HFS = (short) 0x4244; // ASCII: 'BD'
    private static final short SIGNATURE_HFS_PLUS = (short) 0x482B; // ASCII: 'H+'
    private static final short SIGNATURE_HFSX = (short) 0x4858; // ASCII: 'HX'

    private final DataLocator partitionData;

    public HFSContainerHandler(DataLocator partitionData) {
        this.partitionData = partitionData;
    }

    @Override
    public boolean containsFileSystem() {
        return true;
    }

    @Override
    public boolean containsPartitionSystem() {
        return false;
    }

    @Override
    public boolean containsContainer() {
        return false;
    }

    /**
     * The file system type of an HFS container will be one of HFS or HFS+. If
     * no signature at all can be found, UNKNOWN is returned and the file system
     * type is undetermined.
     */
    @Override
    public FileSystemMajorType detectFileSystemType() {
        ReadableRandomAccessStream bitstream = partitionData.createReadOnlyFile();
        return switch (HFSCommonFileSystemRecognizer.detectFileSystem(bitstream, 0)) {
            case HFS -> FileSystemMajorType.APPLE_HFS;
            case HFS_PLUS, HFS_WRAPPED_HFS_PLUS -> FileSystemMajorType.APPLE_HFS_PLUS;
            case HFSX -> FileSystemMajorType.APPLE_HFSX; // Not possible, or probable.
            default -> FileSystemMajorType.UNKNOWN;
        };
    }

    @Override
    public PartitionSystemType detectPartitionSystemType() {
        throw new UnsupportedOperationException("An HFS container does not contain partition systems.");
    }

    @Override
    public ContainerType detectContainerType() {
        throw new UnsupportedOperationException("An HFS container does not contain other containers.");
    }
}
