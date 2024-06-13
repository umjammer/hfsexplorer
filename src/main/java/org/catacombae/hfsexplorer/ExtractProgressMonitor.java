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

package org.catacombae.hfsexplorer;

import java.io.File;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.LinkedList;

import org.catacombae.hfs.ProgressMonitor;

import static java.lang.System.getLogger;


/**
 * @author <a href="https://catacombae.org" target="_top">Erik Larsson</a>
 */
public interface ExtractProgressMonitor extends ProgressMonitor {

    void updateCalculateDir(String dirname);

    void updateTotalProgress(double fraction, String message);

    void updateCurrentDir(String dirname);

    void updateCurrentFile(String filename, long fileSize);

    void setDataSize(long totalSize);

//    public boolean confirmOverwriteDirectory(File dir);

//    public boolean confirmSkipDirectory(String... messageLines);

    CreateDirectoryFailedAction createDirectoryFailed(String dirname, File parentDirectory);

    CreateFileFailedAction createFileFailed(String filename, File parentDirectory);

    DirectoryExistsAction directoryExists(File directory);

    FileExistsAction fileExists(File file);

    UnhandledExceptionAction unhandledException(String filename,
                                                Throwable t);

    String displayRenamePrompt(String currentName, File outDir);

    ExtractProperties getExtractProperties();

    interface ExtractPropertiesListener {

        void propertyChanged(Object changedProperty);
    }

    class ExtractProperties {

        private static final Logger logger = getLogger(ExtractProperties.class.getName());

        private final LinkedList<ExtractPropertiesListener> listeners = new LinkedList<>();
        private volatile CreateDirectoryFailedAction createDirAction = CreateDirectoryFailedAction.PROMPT_USER;
        private volatile CreateFileFailedAction createFileAction = CreateFileFailedAction.PROMPT_USER;
        private volatile DirectoryExistsAction dirExistsAction = DirectoryExistsAction.PROMPT_USER;
        private volatile FileExistsAction fileExistsAction = FileExistsAction.PROMPT_USER;
        private volatile UnhandledExceptionAction unhandledExceptionAction = UnhandledExceptionAction.PROMPT_USER;

        public CreateDirectoryFailedAction getCreateDirectoryFailedAction() {
            return createDirAction;
        }

        public CreateFileFailedAction getCreateFileFailedAction() {
            return createFileAction;
        }

        public DirectoryExistsAction getDirectoryExistsAction() {
            return dirExistsAction;
        }

        public FileExistsAction getFileExistsAction() {
            return fileExistsAction;
        }

        public UnhandledExceptionAction getUnhandledExceptionAction() {
            return unhandledExceptionAction;
        }

        public void setCreateDirectoryFailedAction(CreateDirectoryFailedAction action) {
            createDirAction = action;
            notifyListeners(action);
        }

        public void setCreateFileFailedAction(CreateFileFailedAction action) {
            createFileAction = action;
            notifyListeners(action);
        }

        public void setDirectoryExistsAction(DirectoryExistsAction action) {
            dirExistsAction = action;
            notifyListeners(action);
        }

        public void setFileExistsAction(FileExistsAction action) {
            fileExistsAction = action;
            notifyListeners(action);
        }

        public void setUnhandledExceptionAction(UnhandledExceptionAction action) {
            unhandledExceptionAction = action;
            notifyListeners(action);
        }

        public void addListener(ExtractPropertiesListener listener) {
            listeners.addLast(listener);
        }

        private void notifyListeners(Object changedProperty) {
            for (ExtractPropertiesListener listener : listeners) {
                try {
                    listener.propertyChanged(changedProperty);
                } catch (Exception e) {
                    logger.log(Level.ERROR, e.getMessage(), e);
                }
            }
        }
    }

    enum CreateDirectoryFailedAction {PROMPT_USER, SKIP_DIRECTORY, RENAME, AUTO_RENAME, CANCEL}

    enum CreateFileFailedAction {PROMPT_USER, SKIP_FILE, SKIP_DIRECTORY, RENAME, AUTO_RENAME, CANCEL}

    enum DirectoryExistsAction {
        PROMPT_USER,
        CONTINUE,
        ALWAYS_CONTINUE,
        SKIP_DIRECTORY,
        RENAME,
        AUTO_RENAME,
        CANCEL,
    }

    enum FileExistsAction {PROMPT_USER, SKIP_FILE, SKIP_DIRECTORY, OVERWRITE, OVERWRITE_ALL, RENAME, AUTO_RENAME, CANCEL}

    enum UnhandledExceptionAction {
        PROMPT_USER,
        CONTINUE,
        ALWAYS_CONTINUE,
        ABORT,
    }
}
