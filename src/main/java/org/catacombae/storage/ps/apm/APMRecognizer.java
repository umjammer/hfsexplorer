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

package org.catacombae.storage.ps.apm;

import org.catacombae.io.ReadableRandomAccessStream;
import org.catacombae.storage.ps.apm.types.ApplePartitionMap;
import org.catacombae.storage.ps.apm.types.DriverDescriptorRecord;
import org.catacombae.storage.ps.PartitionSystemRecognizer;

/**
 * @author <a href="https://catacombae.org" target="_top">Erik Larsson</a>
 */
public class APMRecognizer implements PartitionSystemRecognizer {

    public boolean detect(ReadableRandomAccessStream fsStream, long offset, long length) {
        try {
            //ReadableRandomAccessStream llf = data.createReadOnlyFile();
            byte[] firstBlock = new byte[512];

            fsStream.seek(0);
            fsStream.readFully(firstBlock);

            // Look for APM
            int blockSize = 0;

            try {
                DriverDescriptorRecord ddr =
                        new DriverDescriptorRecord(firstBlock, 0);
                if(ddr.isValid()) {
                    blockSize = ddr.getSbBlkSize();
                }
            } catch(Exception e) {
            }

            if(blockSize == 0) {
                /* Check if the second block has a valid partition signature. */
                byte[] secondBlock = new byte[512];
                fsStream.seek(512);
                fsStream.readFully(secondBlock);
                if(secondBlock[0] == 'P' && secondBlock[1] == 'M') {
                    blockSize = 512;
                }
                else {
                    blockSize = 0;
                }
            }

            if(blockSize > 0) {
                //long numberOfBlocksOnDevice = Util.unsign(ddr.getSbBlkCount());
                //bitStream.seek(blockSize*1); // second block, first partition in list
                ApplePartitionMap apm = new ApplePartitionMap(fsStream, blockSize * 1, blockSize);
                if(apm.getUsedPartitionCount() > 0) {
                    return true;
                }
                else if(blockSize != 512) {
                    /* We may have an APM configured with 512 byte block size
                     * even though the DDR says otherwise. */
                    final ApplePartitionMap backupApm =
                        new ApplePartitionMap(fsStream, 512, 512);
                    if(backupApm.getUsedPartitionCount() > 0) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
        }
        return false;
    }
}
