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

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import org.catacombae.hfs.types.hfscommon.CommonHFSCatalogFileRecord;
import org.catacombae.io.ReadableRandomAccessStream;
import org.catacombae.storage.fs.FSAttributes;
import org.catacombae.storage.fs.FSEntry;
import org.catacombae.storage.fs.FSLink;
import org.catacombae.util.IOUtil;
import org.catacombae.util.Util;

import static java.lang.System.getLogger;


/**
 * @author <a href="https://catacombae.org" target="_top">Erik Larsson</a>
 */
public class HFSCommonFSLink extends HFSCommonAbstractFile implements FSLink {

    private static final Logger logger = getLogger(HFSCommonFSLink.class.getName());

    private final CommonHFSCatalogFileRecord linkRecord;

    public HFSCommonFSLink(HFSCommonFileSystemHandler fsHandler,
                           CommonHFSCatalogFileRecord linkRecord) {
        super(fsHandler, linkRecord);

        this.linkRecord = linkRecord;

        if (!linkRecord.getData().isSymbolicLink())
            throw new IllegalArgumentException("linkRecord is no symbolic link!");
    }

    public String getLinkTargetPosixPath() {
        // Read the data associated with the link.
        ReadableRandomAccessStream linkDataStream = fsHandler.getReadableDataForkStream(linkRecord);
        byte[] linkBytes = IOUtil.readFully(linkDataStream);
        linkDataStream.close();

        return Util.readString(linkBytes, "UTF-8");
    }

//    String[] getLinkTargetPath() {
//        // Read the data associated with the link.
//        ReadableRandomAccessStream linkDataStream = fsHandler.getReadableDataForkStream(linkRecord);
//        byte[] linkBytes = new byte[(int) linkDataStream.length()];
//        linkDataStream.readFully(linkBytes);
//        linkDataStream.close();
//
//        return HFSCommonFileSystemHandler.splitPOSIXUTF8Path(linkBytes);
//    }

    @Override
    public FSEntry getLinkTarget(String[] parentDir) {
//        String prefix = parentFileSystem.globalPrefix;
//        parentFileSystem.globalPrefix += "   ";
//        try {
//        parentFileSystem.log(prefix + "getLinkTarget(" + Util.concatenateStrings(parentDir, "/") + ");");
//        logger.log(Level.DEBUG, );
        String posixPath = getLinkTargetPosixPath();
//        parentFileSystem.log(prefix + "  getLinkTarget(): posixPath=\"" + posixPath + "\"");
//        logger.log(Level.DEBUG, "getLinkTarget(): " + linkRecord.getKey().getParentID().toLong() + ":\"" +
//                fsHandler.getProperNodeName(linkRecord) + "\" getting true path for \"" + posixPath + "\"...");
        String[] targetPath = fsHandler.getTruePathFromPosixPath(posixPath, parentDir);
        FSEntry res;
        if (targetPath != null) {
//            parentFileSystem.log(prefix + "getLinkTarget(): got true path \"" + Util.concatenateStrings(targetPath, "/") + "\"");
            res = fsHandler.getEntry(targetPath);
            if (res != null && res instanceof FSLink) {
//                String[] targetParentDir = Util.arrayCopy(targetPath, 0, new String[targetPath.length-1], 0, targetPath.length-1);
//                logger.log(Level.DEBUG, "getLinkTarget(): trying to resolve inner link using link path \"" + Util.concatenateStrings(targetPath, "/") + "\"");
                res = fsHandler.resolveLinks(targetPath, (FSLink) res);
                if (res == null) {
                    logger.log(Level.DEBUG, "\ngetLinkTarget(): Could not resolve inner link \"" +
                            Util.concatenateStrings(targetPath, "/") + "\"");
                }
            } else if (res == null) {
                logger.log(Level.DEBUG, "\ngetLinkTarget(): Could not get entry for true path \"" +
                        Util.concatenateStrings(targetPath, "/") + "\"");
            }

            if (res != null && res instanceof FSLink)
                throw new RuntimeException("res still instanceof FSLink!");
        } else {
            logger.log(Level.DEBUG, "\ngetLinkTarget(): Could not get true path!");

            res = null;
        }

        if (res == null) {
            logger.log(Level.DEBUG, "getLinkTarget(): FAILED to get entry by posix path for link " +
                    linkRecord.getKey().getParentID().toLong() + ":\"" +
                    fsHandler.getProperNodeName(linkRecord) + "\":");
            logger.log(Level.DEBUG, "getLinkTarget():   posixPath=\"" + posixPath + "\"");
            logger.log(Level.DEBUG, "getLinkTarget():   parentDir=\"" +
                    Util.concatenateStrings(parentDir, "/") + "\"");
        }

        return res;
//        } finally {
//            parentFileSystem.log(prefix + "Returning from getLinkTarget.");
//            parentFileSystem.globalPrefix = prefix;
//        }
    }

    @Override
    public FSAttributes getAttributes() {
        return new HFSCommonFSAttributes(this, linkRecord.getData());
    }

    @Override
    public String getName() {
        return fsHandler.getProperNodeName(linkRecord);
    }

//    @Override
//    public FSFolder getParent() {
//        return fsHandler.lookupParentFolder(linkRecord);
//    }

    public CommonHFSCatalogFileRecord getInternalCatalogFileRecord() {
        return linkRecord;
    }

    @Override
    public String getLinkTargetString() {
        return getLinkTargetPosixPath();
    }

    @Override
    public HFSCommonFileSystemHandler getFileSystemHandler() {
        return fsHandler;
    }
}
