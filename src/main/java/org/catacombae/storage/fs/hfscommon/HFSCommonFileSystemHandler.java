/*-
 * Copyright (C) 2008-2014 Erik Larsson
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
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;

import org.catacombae.hfs.HFSVolume;
import org.catacombae.hfs.UnicodeNormalizationToolkit;
import org.catacombae.hfs.types.hfscommon.CommonHFSCatalogFileRecord;
import org.catacombae.hfs.types.hfscommon.CommonHFSCatalogFileThreadRecord;
import org.catacombae.hfs.types.hfscommon.CommonHFSCatalogFolderRecord;
import org.catacombae.hfs.types.hfscommon.CommonHFSCatalogFolderThread;
import org.catacombae.hfs.types.hfscommon.CommonHFSCatalogFolderThreadRecord;
import org.catacombae.hfs.types.hfscommon.CommonHFSCatalogLeafRecord;
import org.catacombae.hfs.types.hfscommon.CommonHFSCatalogNodeID;
import org.catacombae.hfs.util.ServicesForMac;
import org.catacombae.io.ReadableRandomAccessStream;
import org.catacombae.storage.fs.FSEntry;
import org.catacombae.storage.fs.FSFile;
import org.catacombae.storage.fs.FSFolder;
import org.catacombae.storage.fs.FSForkType;
import org.catacombae.storage.fs.FSLink;
import org.catacombae.storage.fs.FileSystemCapability;
import org.catacombae.storage.fs.FileSystemHandler;
import org.catacombae.util.Util;

import static java.lang.System.getLogger;


/**
 * HFS+ implementation of a FileSystemHandler. This implementation can be used
 * to access HFS+ file systems.
 *
 * @author <a href="https://catacombae.org" target="_top">Erik Larsson</a>
 */
public abstract class HFSCommonFileSystemHandler extends FileSystemHandler {

    private static final Logger logger = getLogger(HFSCommonFileSystemHandler.class.getName());

    protected final HFSVolume view;
    private final boolean posixNames;
    private final boolean sfmSubstitutions;
    private final boolean doUnicodeFileNameComposition;
    protected boolean hideProtected;

    protected HFSCommonFileSystemHandler(HFSVolume iView,
                                         boolean posixNames,
                                         boolean sfmSubstitutions,
                                         boolean iDoUnicodeFileNameComposition,
                                         boolean hideProtected) {
        if (sfmSubstitutions && !posixNames) {
            throw new IllegalArgumentException("'sfmSubstitutions' requires 'posixNames'.");
        }

        this.view = iView;
        this.posixNames = posixNames;
        this.sfmSubstitutions = sfmSubstitutions;
        this.doUnicodeFileNameComposition = iDoUnicodeFileNameComposition;
        this.hideProtected = hideProtected;
    }

    public static FileSystemCapability[] getStaticCapabilities() {
        return new FileSystemCapability[] {
                FileSystemCapability.CREATE_TIME,
                FileSystemCapability.BACKUP_TIME,
        };
    }

    @Override
    public FileSystemCapability[] getCapabilities() {
        return getStaticCapabilities();
    }

    @Override
    public FSEntry[] list(String... path) {
        CommonHFSCatalogFolderRecord curFolder = view.getCatalogFile().getRootFolder();
        for (String nextFolderName : path) {
            CommonHFSCatalogLeafRecord subRecord = getRecord(curFolder, nextFolderName);

            if (subRecord != null && subRecord instanceof CommonHFSCatalogFolderRecord)
                curFolder = (CommonHFSCatalogFolderRecord) subRecord;
            else
                return null; // Invalid path, no matching child folder was found.
        }
        return listFSEntries(curFolder);
    }

    @Override
    public FSEntry getEntry(String... path) {
        return getEntry(view.getCatalogFile().getRootFolder(), path);
    }

    FSEntry getEntry(CommonHFSCatalogFolderRecord rootRecord, String... path) {
        CommonHFSCatalogLeafRecord rec = getRecord(rootRecord, path);

        if (rec == null)
            return null;
        else if (rec instanceof CommonHFSCatalogFileRecord)
            return entryFromRecord((CommonHFSCatalogFileRecord) rec);
        else if (rec instanceof CommonHFSCatalogFolderRecord)
            return entryFromRecord((CommonHFSCatalogFolderRecord) rec);
        else
            throw new RuntimeException("Did not excpect a " + rec.getClass() + " here!");
    }

    protected abstract String[] getAbsoluteLinkPath(String[] path, int pathLength, CommonHFSCatalogFileRecord rec);

    /**
     * Searches the hierarchy rooted in <code>rootRecord</code> for the record addressed by
     * <code>path</code>. If any symbolic or hard links exist in the path to the requested entry,
     * they will be resolved, but the requested destination will be returned as it is.
     *
     * @param rootRecord (non-null) the root record from which we will begin searching.
     * @param path       the path to our requested entry. May be empty, in which case
     *                   <code>rootRecord</code> is returned.
     * @return the requested entry, or <code>null</code> if it wasn't found.
     */
    protected CommonHFSCatalogLeafRecord getRecord(CommonHFSCatalogFolderRecord rootRecord, String... path) {
        /*
         * Algorithm (variables are prefixed with $):
         *
         * $currentRoot = $root
         * for each path component $pc except the last one:
         *   while currentRoot is a link:
         *     $currentRoot = resolveLink(entry)
         *
         *   if currentRoot is a directory:
         *     $currentRoot = find($pc, $currentRoot)
         *   else:
         *     return null
         *
         * return currentRoot
         */
//        String prefix = globalPrefix;
//        globalPrefix += "    ";
//        log(prefix + "getRecord(" + (rootRecord != null ? rootRecord.getKey().getParentID().toLong() +
//                ":\"" + getProperNodeName(rootRecord) + "\"" : "null") + ", { " +
//                (path != null && path.length > 0 ? "\"" + Util.concatenateStrings(path, "\", \"") +
//                        "\"" : path == null ? "null" : "") + " });");
//        try {
        if (rootRecord == null)
            throw new IllegalArgumentException("rootRecord == null");
        if (path == null)
            throw new IllegalArgumentException("path == null");

        LinkedList<String[]> visitedList = null;
        CommonHFSCatalogLeafRecord currentRoot = rootRecord;

        // We iterate over all records except the last one, which is our target.
        for (int i = 0; i < path.length; ++i) {
            String curPathComponent = getOnDiskName(path[i]);
//            log(prefix + "  getRecord: Processing path element " + (i + 1) + "/" +
//                    path.length + ": \"" + curPathComponent + "\"");

            LinkedList<String[]> curVisitedList = null;

            // Iterate through all links.
            while (currentRoot instanceof CommonHFSCatalogFileRecord) {
                String[] absPath;

                absPath = getAbsoluteLinkPath(path, i,
                        (CommonHFSCatalogFileRecord) currentRoot);
                if (absPath == null) {
                    break;
                }

                // Reset visited list before usage if this is the first time
                if (curVisitedList == null) {
                    if (visitedList == null)
                        visitedList = new LinkedList<>();
                    else
                        visitedList.clear();
                    curVisitedList = visitedList;
                }

                if (absPath == null)
                    throw new RuntimeException("'assertion' failed. absPath shouldn't be null");
                else if (Util.contains(curVisitedList, absPath)) {
                    logger.log(Level.DEBUG, "WARNING: Detected cyclic link structure when resolving link target.");
                    logger.log(Level.DEBUG, "         Resolve stack:");
                    for (String[] sa : curVisitedList) {
                        logger.log(Level.DEBUG, "           " + Util.concatenateStrings(sa, "/"));
                    }
                    logger.log(Level.DEBUG, "           " + Util.concatenateStrings(absPath, "/"));

                    return null; // Circular linking.
                } else {
                    curVisitedList.addLast(absPath);
//                    log(prefix + "  getRecord: Trying to get record for absolute link target...");
                    CommonHFSCatalogLeafRecord linkTarget = getRecord(view.getCatalogFile().getRootFolder(), absPath);
//                    log(prefix + "  getRecord: target record = " + linkTarget);
                    if (linkTarget != null) {
                        currentRoot = linkTarget;
                    }
                }
            }

            CommonHFSCatalogFolderRecord currentRootFolder;
            if (currentRoot instanceof CommonHFSCatalogFolderRecord)
                currentRootFolder = (CommonHFSCatalogFolderRecord) currentRoot;
            else {
//                log(prefix + "  getRecord: Returning with error - currentRoot not instanceof CommonHFSCatalogFolderRecord (" + currentRoot + ")");
                return null; // We encountered a pathname component which wasn't a folder.
            }

//            log(prefix + "  getting record (" + currentRootFolder.getData().getFolderID().toLong() + ":\"" + curPathComponent + "\")");
            CommonHFSCatalogLeafRecord newRoot =
                    view.getCatalogFile().getRecord(currentRootFolder.getData().getFolderID(), view.encodeString(curPathComponent));

            if (newRoot != null)
                currentRoot = newRoot;
            else {
//                log(prefix + "  getRecord: Returning with error - no match was found for \"" + curPathComponent + "\"");
                return null; // Invalid path, no matching child was found.
            }
        }

//        log(prefix + "  getRecord: Returning successfully with " + currentRoot + " (" + currentRoot.getKey().getParentID().toLong() + ":\"" + getProperNodeName(currentRoot) + "\")");
        return currentRoot;
//        } finally {
//            log(prefix + "Returning from getRecord.");
//            globalPrefix = prefix;
//        }
    }

    protected FSFile newFSFile(CommonHFSCatalogFileRecord fileRecord) {
        return new HFSCommonFSFile(this, fileRecord);
    }

    protected FSFile newFSFile(CommonHFSCatalogFileRecord hardLinkRecord, CommonHFSCatalogFileRecord fileRecord) {
        return new HFSCommonFSFile(this, hardLinkRecord, fileRecord);
    }

    protected FSEntry entryFromRecord(CommonHFSCatalogFileRecord fileRecord) {
        return newFSFile(fileRecord);
    }

    protected FSFile createFSFile(CommonHFSCatalogFileRecord fileRecord) {
        return newFSFile(fileRecord);
    }

    protected FSFile createFSFile(CommonHFSCatalogFileRecord hardLinkRecord, CommonHFSCatalogFileRecord fileRecord) {
        return newFSFile(hardLinkRecord, fileRecord);
    }

    protected FSFolder createFSFolder(CommonHFSCatalogFileRecord hardLinkRecord, CommonHFSCatalogFolderRecord folderRecord) {
        return new HFSCommonFSFolder(this, hardLinkRecord, folderRecord);
    }

    private FSEntry entryFromRecord(CommonHFSCatalogFolderRecord folderRecord) {
        return new HFSCommonFSFolder(this, folderRecord);
    }

//    private FSEntry entriFromRecord(CommonHFSCatalogLeafRecord rec) {
//        if (rec instanceof CommonHFSCatalogFileRecord) {
//            return entryFromRecord((CommonHFSCatalogFileRecord) rec);
//        } else if (rec instanceof CommonHFSCatalogFolderRecord) {
//            return entryFromRecord((CommonHFSCatalogFolderRecord) rec);
//        } else
////            throw new RuntimeException("Did not expect a " + rec.getClass() + " here.")
//            return null;
//    }

    @Override
    public FSForkType[] getSupportedForkTypes() {
        return new FSForkType[] {FSForkType.DATA, FSForkType.MACOS_RESOURCE};
    }

    private static char[] posixWrap(char[] nodeNameChars) {
        for (int i = 0; i < nodeNameChars.length; ++i) {
            if (nodeNameChars[i] == '/') {
                nodeNameChars[i] = ':';
            } else if (nodeNameChars[i] == ':') {
                nodeNameChars[i] = '/';
            }
        }

        return nodeNameChars;
    }

    private static String posixWrap(String nodeName) {
        return new String(posixWrap(nodeName.toCharArray()));
    }

    protected String getLogicalName(String onDiskName) {
        String logicalName = onDiskName;

        if (doUnicodeFileNameComposition) {
            logicalName = UnicodeNormalizationToolkit.getDefaultInstance().compose(logicalName);
        }

        if (posixNames) {
            logicalName = posixWrap(logicalName);

            if (sfmSubstitutions) {
                logicalName = ServicesForMac.remap(logicalName, false);
            }
        }

        return logicalName;
    }

    protected String getOnDiskName(String logicalName) {
        String onDiskName = logicalName;

        if (posixNames) {
            if (sfmSubstitutions) {
                onDiskName = ServicesForMac.remap(onDiskName, true);
            }

            onDiskName = posixWrap(onDiskName);
        }

        if (doUnicodeFileNameComposition) {
            onDiskName = UnicodeNormalizationToolkit.getDefaultInstance().decompose(CharBuffer.wrap(onDiskName));
        }

        return onDiskName;
    }

    protected String getProperNodeName(CommonHFSCatalogLeafRecord record) {
        return getLogicalName(view.decodeString(record.getKey().getNodeName()));
    }

    /**
     * Converts a HFS+ POSIX UTF-8 pathname into pathname component strings.
     *
     * @param path the bytes that make up the HFS+ POSIX UTF-8 pathname string.
     * @return the pathname components of the HFS+ POSIX UTF-8 pathname.
     */
    public String[] splitPOSIXUTF8Path(byte[] path) {
        return splitPOSIXUTF8Path(path, 0, path.length);
    }

    /**
     * Converts a HFS+ POSIX UTF-8 pathname into pathname component strings.
     *
     * @param path   the bytes that make up the HFS+ POSIX UTF-8 pathname string.
     * @param offset offset to the beginning of string data in <code>path</code>.
     * @param length length of string data in <code>path</code>.
     * @return the pathname components of the HFS+ POSIX UTF-8 pathname.
     */
    public String[] splitPOSIXUTF8Path(byte[] path, int offset, int length) {
        String s = new String(path, offset, length, StandardCharsets.UTF_8);
        String[] res = s.split("/");

        if (!posixNames) {
            // As per the MacOS <-> POSIX translation semantics, all POSIX
            // ':' characters are really '/' characters in the MacOS
            // world.
            for (int i = 0; i < res.length; ++i) {
                res[i] = posixWrap(res[i]);
            }
        }

        return res;
    }

    protected ReadableRandomAccessStream getReadableDataForkStream(CommonHFSCatalogFileRecord fileRecord) {
        return view.getReadableDataForkStream(fileRecord);
    }

    ReadableRandomAccessStream getReadableResourceForkStream(CommonHFSCatalogFileRecord fileRecord) {
        return view.getReadableResourceForkStream(fileRecord);
    }

//    boolean isUnicodeCompositionEnabled() {
//        return doUnicodeFileNameComposition;
//    }

    protected abstract boolean shouldHide(CommonHFSCatalogLeafRecord rec);

    String[] listNames(CommonHFSCatalogFolderRecord folderRecord) {
        CommonHFSCatalogLeafRecord[] subRecords = view.getCatalogFile().listRecords(folderRecord);
        LinkedList<String> result = new LinkedList<>();
        for (CommonHFSCatalogLeafRecord curRecord : subRecords) {
            if (!shouldHide(curRecord))
                result.add(getProperNodeName(curRecord));
        }
        return result.toArray(String[]::new);
    }

    FSEntry[] listFSEntries(CommonHFSCatalogFolderRecord folderRecord) {
        CommonHFSCatalogLeafRecord[] subRecords = view.getCatalogFile().listRecords(folderRecord);
        LinkedList<FSEntry> result = new LinkedList<>();
        for (CommonHFSCatalogLeafRecord curRecord : subRecords) {
            FSEntry curEntry = null;

            if (shouldHide(curRecord)) ;
            else if (curRecord instanceof CommonHFSCatalogFileRecord)
                curEntry = entryFromRecord((CommonHFSCatalogFileRecord) curRecord);
            else if (curRecord instanceof CommonHFSCatalogFolderRecord)
                curEntry = entryFromRecord((CommonHFSCatalogFolderRecord) curRecord);

            if (curEntry != null)
                result.addLast(curEntry);
        }
        return result.toArray(FSEntry[]::new);
    }

    HFSCommonFSFolder lookupParentFolder(CommonHFSCatalogLeafRecord childRecord) {
        CommonHFSCatalogFolderRecord folderRec = lookupParentFolderRecord(childRecord);
        if (folderRec != null)
            return new HFSCommonFSFolder(this, folderRec);
        else
            return null;
    }

    private CommonHFSCatalogFolderRecord lookupParentFolderRecord(CommonHFSCatalogLeafRecord childRecord) {
        CommonHFSCatalogNodeID parentID = childRecord.getKey().getParentID();

        // Look for the thread record associated with the parent dir
        CommonHFSCatalogLeafRecord parent = view.getCatalogFile().getRecord(parentID, view.getEmptyString());
        if (parent == null) {
            if (parentID.toLong() == 1)
                return null; // There is no parent to root.
            else
                throw new RuntimeException("INTERNAL ERROR: No folder thread found for ID " + parentID.toLong() + "!");
        }

        if (parent instanceof CommonHFSCatalogFolderThreadRecord) {
            CommonHFSCatalogFolderThread data = ((CommonHFSCatalogFolderThreadRecord) parent).getData();
            CommonHFSCatalogLeafRecord rec = view.getCatalogFile().getRecord(data.getParentID(), data.getNodeName());
            if (rec == null)
                return null;
            else if (rec instanceof CommonHFSCatalogFolderRecord)
                return (CommonHFSCatalogFolderRecord) rec;
            else
                throw new RuntimeException("Internal error: rec not instanceof " +
                        "CommonHFSCatalogFolderRecord, but instead:" + rec.getClass());
        } else if (parent instanceof CommonHFSCatalogFileThreadRecord) {
            throw new RuntimeException("Tried to get folder thread record (" +
                    parentID + ",\"\") but found a file thread record!");
        } else {
            throw new RuntimeException("Tried to get folder thread record (" +
                    parentID + ",\"\") but found a " + parent.getClass() + "!");
        }
    }

    /**
     * Returns the underlying BaseHFSFileSystemView that serves the file system
     * handler with data.<br>
     * <b>Don't use this method if you want your code to be file system
     * independent!</b>
     *
     * @return the underlying BaseHFSFileSystemView.
     */
    public HFSVolume getFSView() {
        return view;
    }

    @Override
    public void close() {
        view.close();
    }

    @Override
    public FSFolder getRoot() {
        return new HFSCommonFSFolder(this, view.getCatalogFile().getRootFolder());
    }

    @Override
    public String parsePosixPathnameComponent(String posixPathnameComponent) {
        return posixNames ? posixPathnameComponent : posixWrap(posixPathnameComponent);
    }

    @Override
    public String generatePosixPathnameComponent(String fsPathnameComponent) {
        return posixNames ? fsPathnameComponent : posixWrap(fsPathnameComponent);
    }

    @Override
    public String[] getTargetPath(FSLink link, String[] parentDir) {
        if (link instanceof HFSCommonFSLink hfsLink) {
            return getTruePathFromPosixPath(hfsLink.getLinkTargetPosixPath(), parentDir);
        } else
            throw new RuntimeException("Invalid type: " + link.getClass());
    }

    public void setHideProtected(boolean hideProtected) {
        this.hideProtected = hideProtected;
    }

    protected abstract Long getLinkCount(CommonHFSCatalogFileRecord fr);
}
