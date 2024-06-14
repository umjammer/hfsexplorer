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

package org.catacombae.storage.ps.ebr;

import org.catacombae.io.ReadableRandomAccessStream;
import org.catacombae.storage.ps.legacy.PartitionSystem;
import org.catacombae.storage.io.DataLocator;
import org.catacombae.storage.ps.Partition;
import org.catacombae.storage.ps.PartitionSystemHandler;


/**
 * @author <a href="https://catacombae.org" target="_top">Erik Larsson</a>
 */
public class EBRHandler extends PartitionSystemHandler {

    private final DataLocator partitionData;

    public EBRHandler(DataLocator partitionData) {
        this.partitionData = partitionData;
    }

    @Override
    public long getPartitionCount() {
        PartitionSystem ps = readPartitionTable();
        return ps.getUsedPartitionCount();
    }

    @Override
    public Partition[] getPartitions() {
        EBRPartitionSystem partitionTable = readPartitionTable();
        return partitionTable.getUsedPartitionEntries();
    }

    @Override
    public void close() {
        partitionData.close();
    }

    private EBRPartitionSystem readPartitionTable() {
        ReadableRandomAccessStream llf = null;
        try {
            llf = partitionData.createReadOnlyFile();

            byte[] firstBlock = new byte[512];

            llf.readFully(firstBlock);

            EBRPartitionSystem ebs = new EBRPartitionSystem(llf, 0, 512);
            if (ebs.isValid())
                return ebs;

        } finally {
            if (llf != null) {
                llf.close();
            }
        }

        return null;
    }
}
