/*-
 * Copyright (C) 2007-2014 Erik Larsson
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

package org.catacombae.hfsexplorer.tools;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.Arrays;
import java.util.Date;

import org.catacombae.dmg.encrypted.ReadableCEncryptedEncodingStream;
import org.catacombae.dmg.sparsebundle.ReadableSparseBundleStream;
import org.catacombae.dmg.sparseimage.ReadableSparseImageStream;
import org.catacombae.dmg.sparseimage.SparseImageRecognizer;
import org.catacombae.dmg.udif.UDIFDetector;
import org.catacombae.dmg.udif.UDIFRandomAccessStream;
import org.catacombae.hfsexplorer.HFSExplorer;
import org.catacombae.hfsexplorer.IOUtil;
import org.catacombae.hfsexplorer.Java7Util;
import org.catacombae.hfsexplorer.fs.AppleSingleBuilder;
import org.catacombae.hfsexplorer.fs.AppleSingleBuilder.AppleSingleVersion;
import org.catacombae.hfsexplorer.fs.AppleSingleBuilder.FileSystem;
import org.catacombae.hfsexplorer.fs.AppleSingleBuilder.FileType;
import org.catacombae.storage.io.win32.ReadableWin32FileStream;
import org.catacombae.io.ReadableFileStream;
import org.catacombae.io.ReadableRandomAccessStream;
import org.catacombae.io.RuntimeIOException;
import org.catacombae.storage.io.DataLocator;
import org.catacombae.storage.io.ReadableStreamDataLocator;
import org.catacombae.storage.io.SubDataLocator;
import org.catacombae.storage.fs.FSEntry;
import org.catacombae.storage.fs.FSFile;
import org.catacombae.storage.fs.FSFolder;
import org.catacombae.storage.fs.FSFork;
import org.catacombae.storage.fs.FSForkType;
import org.catacombae.storage.fs.FSLink;
import org.catacombae.storage.fs.FileSystemDetector;
import org.catacombae.storage.fs.FileSystemHandler;
import org.catacombae.storage.fs.FileSystemHandlerFactory;
import org.catacombae.storage.fs.FileSystemHandlerFactory.CustomAttribute;
import org.catacombae.storage.fs.FileSystemMajorType;
import org.catacombae.storage.ps.Partition;
import org.catacombae.storage.ps.PartitionSystemDetector;
import org.catacombae.storage.ps.PartitionSystemHandler;
import org.catacombae.storage.ps.PartitionSystemHandlerFactory;
import org.catacombae.storage.ps.PartitionSystemType;
import org.catacombae.storage.ps.PartitionType;

import static java.lang.System.getLogger;


/**
 * Command line program which extracts all or part of the contents of a
 * HFS/HFS+/HFSX file system to a specified path.
 *
 * @author <a href="https://catacombae.org" target="_top">Erik Larsson</a>
 */
public class UnHFS {

    private static final Logger logger = getLogger(UnHFS.class.getName());

    private static final int RETVAL_NEED_PASSWORD = 10;
    private static final int RETVAL_INCORRECT_PASSWORD = 11;

    /**
     * Prints program usage instructions to the PrintStream <code>ps</code>.
     *
     * @param ps the PrintStream to print usage instruction to.
     */
    private static void printUsage(PrintStream ps) {
        //     80 <-------------------------------------------------------------------------------->
        ps.println("unhfs " + HFSExplorer.VERSION);
        ps.println(HFSExplorer.COPYRIGHT.replaceAll("©", "(C)"));
        for (String s : HFSExplorer.NOTICES) {
            ps.println(s.replaceAll("©", "(C)"));
        }
        ps.println();
        ps.println("usage: unhfs [options...] <input file>");
        ps.println("  Input file can be in raw, UDIF (.dmg) and/or encrypted format.");
        ps.println("  Options:");
        ps.println("    -o <output dir>");
        ps.println("      The target directory in the local file system where all extracted files");
        ps.println("      should go.");
        ps.println("      When this option is omitted, all files go to the currect working");
        ps.println("      directory.");
        ps.println("    -fsroot <path to extract>");
        ps.println("      A POSIX path in the HFS file system that should be extracted.");
        ps.println("      Example which extracts all the contents of joe's user dir from a backup");
        ps.println("      disk image to the current directory:");
        ps.println("        unhfs -o . -fsroot /Users/joe FullBackup.dmg");
        ps.println("      When this option is omitted, all the contents of the file system is");
        ps.println("      extracted.");
        ps.println("    -create");
        ps.println("      If the -fsroot path refers to a folder, create that folder inside");
        ps.println("      the output directory, rather than extracting into the output directory");
        ps.println("      itself.");
        ps.println("    -resforks NONE|APPLEDOUBLE");
        ps.println("      Determines whether resource forks should be extracted, and in what");
        ps.println("      format. Currently only the APPLEDOUBLE format, which puts each resource");
        ps.println("      fork in its own file with the '._' prefix, is supported.");
        ps.println("      When this option is omitted, no resource forks are extracted.");
        ps.println("    -partition <partition number>");
        ps.println("      If the input file is partitioned, extracts files from the specified HFS");
        ps.println("      partition. Partitions are numbered from 0 and up.");
        ps.println("      When this options is omitted, the application chooses the first");
        ps.println("      available HFS partition.");
        ps.println("    -password <password>");
        ps.println("      Specifies the password for an encrypted image. The special marker \"-\" ");
        ps.println("      causes the password to be read from stdin.");
        ps.println("    -sfm-substitutions");
        ps.println("      Translates the filenames to a format that is more compatible with Windows");
        ps.println("      filesystems, using the translation scheme that was used by the now defunct");
        ps.println("      Services for Mac component in Windows Server.");
        ps.println("    -v");
        ps.println("      Verbose mode. Prints the POSIX path of every extracted file to stdout.");
        ps.println("    --");
        ps.println("      Signals that there are no more option arguments. Useful for accessing");
        ps.println("      input files with names identical to an option signature.");
    }

    /**
     * UnHFS entry point. The main method's only responsibility is to parse and
     * validate program arguments. It then passes them on to the static method
     * unhfs(...), which contains the actual program logic.
     *
     * @param args program arguments.
     */
    public static void main(String[] args) {
        String outputDirname = ".";
        String fsRoot = "/";
        boolean extractFolderDirectly = true;
        boolean extractResourceForks = false;
        boolean verbose = false;
        boolean sfmSubstitutions = false;
        int partitionNumber = -1; // -1 means search for first supported partition
        char[] password = null;

        int i;
label:
        for (i = 0; i < args.length; ++i) {
            String curArg = args[i];

            switch (curArg) {
                case "-o":
                    if (i + 1 < args.length)
                        outputDirname = args[++i];
                    else {
                        printUsage(System.err);
                        System.exit(1);
                    }
                    break;
                case "-fsroot":
                    if (i + 1 < args.length)
                        fsRoot = args[++i];
                    else {
                        printUsage(System.err);
                        System.exit(1);
                    }
                    break;
                case "-create":
                    extractFolderDirectly = false;
                    break;
                case "-resforks":
                    if (i + 1 < args.length) {
                        String value = args[++i];
                        if (value.equalsIgnoreCase("NONE")) {
                            extractResourceForks = false;
                        } else if (value.equalsIgnoreCase("APPLEDOUBLE")) {
                            extractResourceForks = true;
                        } else {
                            logger.log(Level.DEBUG, "Error: Invalid value \"" + value +
                                    "\" for -resforks!");
                            printUsage(System.err);
                            System.exit(1);
                        }
                    } else {
                        printUsage(System.err);
                        System.exit(1);
                    }
                    break;
                case "-partition":
                    if (i + 1 < args.length) {
                        try {
                            partitionNumber = Integer.parseInt(args[++i]);
                        } catch (NumberFormatException nfe) {
                            logger.log(Level.DEBUG, "Error: Invalid partition number \"" +
                                    args[i] + "\"!");
                            printUsage(System.err);
                            System.exit(1);
                        }
                    } else {
                        printUsage(System.err);
                        System.exit(1);
                    }
                    break;
                case "-password":
                    if (i + 1 < args.length) {
                        password = args[++i].toCharArray();

                        if (password.length == 1 && password[0] == '-') {
                            // Read password from stdin.
                            InputStreamReader r = new InputStreamReader(System.in);
                            char[] tmp = new char[4096];
                            int offset = 0;
                            int readLength = 0;
                            try {
                                while ((readLength = r.read(tmp, offset, tmp.length - offset)) > 0) {
                                    logger.log(Level.DEBUG, "readLength: " + readLength);

                                    char[] newTmp = new char[tmp.length * 2];
                                    System.arraycopy(tmp, 0, newTmp, 0, tmp.length);
                                    Arrays.fill(tmp, '\0');
                                    offset += readLength;
                                    tmp = newTmp;
                                }
                            } catch (IOException ex) {
                                logger.log(Level.DEBUG, "Got IOException while reading password from stdin:");
                                logger.log(Level.ERROR, ex.getMessage(), ex);
                            }

                            int passwordLength = offset;
                            char[] lineSeparator = System.getProperty("line.separator").toCharArray();
                            boolean trailingLineSeparator = true;
                            for (int j = 0; j < lineSeparator.length; ++j) {
                                int lineSeparatorIndex = lineSeparator.length - 1 - j;
                                int tmpIndex = passwordLength - 1 - j;

                                if (tmp[tmpIndex] != lineSeparator[lineSeparatorIndex]) {
                                    trailingLineSeparator = false;
                                    break;
                                }
                            }

                            if (trailingLineSeparator) {
                                passwordLength -= lineSeparator.length;
                            }

                            password = new char[passwordLength];
                            System.arraycopy(tmp, 0, password, 0, passwordLength);
                            Arrays.fill(tmp, '\0');
                        }
                    } else {
                        printUsage(System.err);
                        System.exit(1);
                    }
                    break;
                case "-sfm-substitutions":
                    sfmSubstitutions = true;
                    break;
                case "-v":
                    verbose = true;
                    break;
                case "--":
                    ++i;
                    break label;
                default:
                    break label;
            }
        }

        if (i != args.length - 1) {
            printUsage(System.err);
            System.exit(1);
        }

        String inputFilename = args[i];
        File inputFile = new File(inputFilename);
        if (!inputFile.isDirectory() && !(inputFile.exists() && inputFile.canRead())) {
            logger.log(Level.DEBUG, "Error: Input file \"" + inputFilename + "\" can not be read!");
            printUsage(System.err);
            System.exit(1);
        }

        File outputDir = new File(outputDirname);
        if (!(outputDir.exists() && outputDir.isDirectory())) {
            logger.log(Level.DEBUG, "Error: Invalid output directory \"" + outputDirname + "\"!");
            printUsage(System.err);
            System.exit(1);
        }

        ReadableRandomAccessStream inputStream;
        if (inputFile.isDirectory()) {
            inputStream = new ReadableSparseBundleStream(inputFile);
        } else if (ReadableWin32FileStream.isSystemSupported())
            inputStream = new ReadableWin32FileStream(inputFilename);
        else
            inputStream = new ReadableFileStream(inputFilename);

        try {
            unhfs(System.out, inputStream, outputDir, fsRoot, password,
                    extractFolderDirectly, extractResourceForks,
                    partitionNumber, verbose, sfmSubstitutions);
            System.exit(0);
        } catch (RuntimeIOException e) {
            logger.log(Level.DEBUG, "Exception while executing main routine:");
            logger.log(Level.ERROR, e.getMessage(), e);
            System.exit(1);
        }
    }

    /**
     * The main routine in the program, which gets invoked after arguments
     * parsing is complete. The routine expects all arguments to be fully parsed
     * and valid.
     *
     * @param outputStream          the PrintStream where all the messages will go
     *                              (should normally be System.out).
     * @param inFileStream          the stream containing the file system data.
     * @param outputDir
     * @param fsRoot
     * @param password              the password used to unlock an encrypted image.
     * @param extractFolderDirectly if fsRoot is a folder, extract directly into outputDir?
     * @param extractResourceForks
     * @param partitionNumber
     * @param verbose
     * @throws org.catacombae.io.RuntimeIOException
     */
    public static void unhfs(PrintStream outputStream,
                             ReadableRandomAccessStream inFileStream, File outputDir,
                             String fsRoot, char[] password, boolean extractFolderDirectly,
                             boolean extractResourceForks, int partitionNumber, boolean verbose,
                             boolean sfmSubstitutions)
            throws RuntimeIOException {

        // First detect any outer layers of UDIF and/or encryption.
        logDebug("Trying to detect encrypted structure...");
        if (ReadableCEncryptedEncodingStream.isCEncryptedEncoding(inFileStream)) {
            if (password != null) {
                try {
                    ReadableCEncryptedEncodingStream stream =
                            new ReadableCEncryptedEncodingStream(inFileStream, password);
                    inFileStream = stream;
                } catch (Exception e) {
                    // TODO: Differentiate between exceptions...
                    logger.log(Level.DEBUG, "Incorrect password for encrypted image.");
                    System.exit(RETVAL_INCORRECT_PASSWORD);
                }
            } else {
                logger.log(Level.DEBUG, "Image is encrypted, and no password was specified.");
                System.exit(RETVAL_NEED_PASSWORD);
            }
        }

        logDebug("Trying to detect sparseimage structure...");
        if (SparseImageRecognizer.isSparseImage(inFileStream)) {
            try {
                ReadableSparseImageStream stream = new ReadableSparseImageStream(inFileStream);
                inFileStream = stream;
            } catch (Exception e) {
                logger.log(Level.DEBUG, "Exception while creating readable sparseimage stream:");
                logger.log(Level.ERROR, e.getMessage(), e);
                System.exit(1);
            }
        }

        logDebug("Trying to detect UDIF structure...");
        if (UDIFDetector.isUDIFEncoded(inFileStream)) {
            UDIFRandomAccessStream stream = null;
            try {
                stream = new UDIFRandomAccessStream(inFileStream);
                inFileStream = stream;
            } catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                logger.log(Level.DEBUG, "Unhandled exception while trying to load UDIF wrapper.");
                System.exit(1);
            }
        }

        DataLocator inputDataLocator = new ReadableStreamDataLocator(inFileStream);

        PartitionSystemType[] psTypes =
                PartitionSystemDetector.detectPartitionSystem(inputDataLocator, false);
        if (psTypes.length >= 1) {

outer:
            for (PartitionSystemType chosenType : psTypes) {

                PartitionSystemHandlerFactory fact = chosenType.createDefaultHandlerFactory();
                PartitionSystemHandler psHandler = fact.createHandler(inputDataLocator);

                if (psHandler.getPartitionCount() > 0) {
                    Partition[] partitionsToProbe;
                    if (partitionNumber >= 0) {
                        if (partitionNumber < psHandler.getPartitionCount()) {
                            partitionsToProbe = new Partition[] {psHandler.getPartition(partitionNumber)};
                        } else {
                            break;
                        }
                    } else if (partitionNumber == -1) {
                        partitionsToProbe = psHandler.getPartitions();
                    } else {
                        logger.log(Level.DEBUG, "Invalid partition number: " + partitionNumber);
                        System.exit(1);
                        return;
                    }

                    for (Partition p : partitionsToProbe) {
                        if (p.getType() == PartitionType.APPLE_HFS_CONTAINER) {
//                            DataLocator subDataLocator =
//                                    new SubDataLocator(inputDataLocator, p.getStartOffset(), p.getLength());
//                            ContainerHandlerFactory chFact =
//                                    p.getType().getAssociatedContainerType().createDefaultHandlerFactory();
//                            ContainerHandler ch = chFact.createHandler(subDataLocator);
//                            if (ch.containsFileSystem()) {
//                                FileSystemMajorType fsType = ch.detectFileSystemType();
//                                switch (fsType) {
//                                    case APPLE_HFS:
//                                    case APPLE_HFS_PLUS:
//                                    case APPLE_HFSX:
//                                        inputDataLocator = subDataLocator;
//                                        break outer;
//                                    default:
//                                }
//                            }
                            inputDataLocator = new SubDataLocator(inputDataLocator, p.getStartOffset(), p.getLength());
                            break outer;
                        } else if (p.getType() == PartitionType.APPLE_HFSX) {
                            inputDataLocator = new SubDataLocator(inputDataLocator, p.getStartOffset(), p.getLength());
                            break outer;
                        }
                    }
                }
            }
        }

        FileSystemMajorType[] fsTypes = FileSystemDetector.detectFileSystem(inputDataLocator);

        FileSystemHandlerFactory fact = null;
outer:
        for (FileSystemMajorType type : fsTypes) {
            switch (type) {
                case APPLE_HFS:
                case APPLE_HFS_PLUS:
                case APPLE_HFSX:
                    fact = type.createDefaultHandlerFactory();
                    break outer;
                default:
            }
        }

        if (fact == null) {
            logger.log(Level.DEBUG, "No HFS file system found.");
            System.exit(1);
        }

        CustomAttribute posixFilenamesAttribute = fact.getCustomAttribute("POSIX_FILENAMES");
        if (posixFilenamesAttribute == null) {
            logger.log(Level.DEBUG, "Unexpected: HFS-ish file system handler does not support POSIX_FILENAMES attribute.");
            System.exit(1);
            return;
        }

        fact.getCreateAttributes().setBooleanAttribute(posixFilenamesAttribute,
                true);

        CustomAttribute sfmSubstitutionsAttribute = fact.getCustomAttribute("SFM_SUBSTITUTIONS");
        if (sfmSubstitutionsAttribute == null) {
            logger.log(Level.DEBUG, "Unexpected: HFS-ish file system handler does not support SFM_SUBSTITUTIONS attribute.");
            System.exit(1);
            return;
        }

        fact.getCreateAttributes().setBooleanAttribute(sfmSubstitutionsAttribute, sfmSubstitutions);

        FileSystemHandler fsHandler = fact.createHandler(inputDataLocator);

        logDebug("Getting entry by posix path: \"" + fsRoot + "\"");
        FSEntry entry = fsHandler.getEntryByPosixPath(fsRoot);
        if (entry instanceof FSFolder folder) {
            File dirForFolder;
            String folderName = folder.getName();
            if (extractFolderDirectly || folderName.equals("/") || folderName.isEmpty()) {
                dirForFolder = outputDir;
            } else {
                dirForFolder = getFileForFolder(outputDir, folder, verbose);
            }
            if (dirForFolder != null) {
                extractFolder(folder, dirForFolder, extractResourceForks, verbose);
            }
        } else if (entry instanceof FSFile file) {
            extractFile(file, outputDir, extractResourceForks, verbose);
        } else {
            logger.log(Level.DEBUG, "Requested path is not a folder or a file!");
            System.exit(1);
        }
    }

    private static void setFileTimes(File file, FSEntry entry, String fileType) {
        Long createdTime = null;
        Long lastAccessedTime = null;
        Long lastModifiedTime = null;

        if (entry.getAttributes().hasCreateDate()) {
            createdTime = entry.getAttributes().getCreateDate().getTime();
        }

        if (entry.getAttributes().hasAccessDate()) {
            lastAccessedTime = entry.getAttributes().getAccessDate().getTime();
        }

        if (entry.getAttributes().hasModifyDate()) {
            lastModifiedTime = entry.getAttributes().getModifyDate().getTime();
        }

        boolean fileTimesSet = false;
        if (Java7Util.isJava7OrHigher()) {
            try {
                Java7Util.setFileTimes(file.getPath(),
                        createdTime != null ? new Date(createdTime) : null,
                        lastAccessedTime != null ? new Date(lastAccessedTime) : null,
                        lastModifiedTime != null ? new Date(lastModifiedTime) : null);
                fileTimesSet = true;
            } catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
            }
        }

        if (!fileTimesSet && lastModifiedTime != null) {
            boolean setLastModifiedResult;

            if (lastModifiedTime < 0) {
                logger.log(Level.DEBUG, "Warning: Can not set " + fileType + "'s " +
                        "last modified timestamp to pre-1970 date " + new Date(lastModifiedTime) + " (raw: " +
                        lastModifiedTime + "). Setting to earliest possible timestamp (" + new Date(0) + ").");

                lastModifiedTime = (long) 0;
            }

            setLastModifiedResult = file.setLastModified(lastModifiedTime);
            if (!setLastModifiedResult) {
                logger.log(Level.DEBUG, "Warning: Failed to set last modified timestamp (" + lastModifiedTime + ") for " +
                        fileType + " \"" + file.getPath() + "\" after extraction.");
            }
        }
    }

    private static void extractFolder(FSFolder folder, File targetDir, boolean extractResourceForks, boolean verbose) {
        boolean wasEmpty = targetDir.list().length == 0;
        for (FSEntry e : folder.listEntries()) {
            if (e instanceof FSFile file) {
                extractFile(file, targetDir, extractResourceForks, verbose);
            } else if (e instanceof FSFolder subFolder) {
                File subFolderFile = getFileForFolder(targetDir, subFolder, verbose);
                if (subFolderFile != null) {
                    extractFolder(subFolder, subFolderFile, extractResourceForks, verbose);
                }
            } else if (e instanceof FSLink) {
                // We don't currently handle links.
            }
        }
        if (wasEmpty) {
            setFileTimes(targetDir, folder, "folder");
        }
    }

    private static void extractFile(FSFile file, File targetDir, boolean extractResourceForks, boolean verbose)
            throws RuntimeIOException {
        File dataFile = new File(targetDir, scrub(file.getName()));
        if (!extractRawForkToFile(file.getMainFork(), dataFile)) {
            logger.log(Level.DEBUG, "Failed to extract data fork to " + dataFile.getPath());
        } else {
            if (verbose) {
                System.out.println(dataFile.getPath());
            }

            setFileTimes(dataFile, file, "data file");
        }

        if (extractResourceForks) {
            FSFork resourceFork = file.getForkByType(FSForkType.MACOS_RESOURCE);

            if (resourceFork != null) {
                File resFile = new File(targetDir, "._" + scrub(file.getName()));
                if (!extractResourceForkToAppleDoubleFile(resourceFork, resFile)) {
                    logger.log(Level.DEBUG, "Failed to extract resource fork to " + resFile.getPath());
                } else {
                    if (verbose) {
                        System.out.println(resFile.getPath());
                    }

                    setFileTimes(resFile, file, "resource fork AppleDouble file");
                }
            }
        }
    }

    private static File getFileForFolder(File targetDir, FSFolder folder, boolean verbose) {
        File folderFile = new File(targetDir, scrub(folder.getName()));
        if (folderFile.isDirectory() || folderFile.mkdir()) {
            if (verbose)
                System.out.println(folderFile.getPath());
        } else {
            logger.log(Level.DEBUG, "Failed to create directory " + folderFile.getPath());
            folderFile = null;
        }
        return folderFile;
    }

    private static boolean extractRawForkToFile(FSFork fork, File targetFile) throws RuntimeIOException {

        ReadableRandomAccessStream in = null;
        try (FileOutputStream os = new FileOutputStream(targetFile)) {

            in = fork.getReadableRandomAccessStream();

            long extractedBytes = IOUtil.streamCopy(in, os, 128 * 1024);
            if (extractedBytes != fork.getLength()) {
                logger.log(Level.DEBUG, "WARNING: Did not extract intended number of bytes to \"" +
                        targetFile.getPath() + "\"! Intended: " + fork.getLength() + " Extracted: " + extractedBytes);
            }

            return true;
        } catch (FileNotFoundException fnfe) {
            return false;
        } catch (Exception ioe) {
            logger.log(Level.ERROR, ioe.getMessage(), ioe);
            return false;
//            throw new RuntimeIOException(ioe);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception e) {
                }
            }
        }
    }

    private static boolean extractResourceForkToAppleDoubleFile(FSFork resourceFork, File targetFile) {
        FileOutputStream os = null;
        ReadableRandomAccessStream in = null;
        try {
            AppleSingleBuilder builder = new AppleSingleBuilder(FileType.APPLEDOUBLE,
                    AppleSingleVersion.VERSION_2_0, FileSystem.MACOS_X);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            in = resourceFork.getReadableRandomAccessStream();
            long extractedBytes = IOUtil.streamCopy(in, baos, 128 * 1024);
            if (extractedBytes != resourceFork.getLength()) {
                logger.log(Level.DEBUG, "WARNING: Did not extract intended number of bytes to \"" +
                        targetFile.getPath() + "\"! Intended: " + resourceFork.getLength() +
                        " Extracted: " + extractedBytes);
            }

            builder.addResourceFork(baos.toByteArray());

            os = new FileOutputStream(targetFile);
            os.write(builder.getResult());
            return true;
        } catch (FileNotFoundException fnfe) {
            return false;
        } catch (Exception ioe) {
            logger.log(Level.ERROR, ioe.getMessage(), ioe);
            return false;
//            throw new RuntimeIOException(ioe);
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (Exception e) {
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (Exception e) {
                }
            }
        }
    }

    /**
     * Scrubs away all control characters from a string and replaces them with '_'.
     *
     * @param s the string to be processed.
     * @return a scrubbed string.
     */
    private static String scrub(String s) {
        char[] cdata = s.toCharArray();
        for (int i = 0; i < cdata.length; ++i) {
            if ((cdata[i] >= 0 && cdata[i] <= 31) ||
                    (cdata[i] == 127)) {
                cdata[i] = '_';
            }
        }
        return new String(cdata);
    }

    private static void logDebug(String s) {
        logger.log(Level.DEBUG, "DEBUG: " + s);
    }
}
