/*-
 * Copyright (C) 2007 Erik Larsson
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

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

import org.catacombae.hfsexplorer.fs.ResourceForkReader;
import org.catacombae.hfsexplorer.gui.FileInfoPanel;
import org.catacombae.hfsexplorer.gui.FSEntrySummaryPanel;
import org.catacombae.hfsexplorer.gui.FolderInfoPanel;
import org.catacombae.hfsexplorer.gui.ResourceForkViewPanel;
import org.catacombae.hfsexplorer.gui.StructViewPanel;
import org.catacombae.hfs.types.hfscommon.CommonHFSCatalogFile;
import org.catacombae.hfs.types.hfscommon.CommonHFSCatalogFolder;
import org.catacombae.hfsexplorer.fs.ResourceForkReader.
        MalformedResourceForkException;
import org.catacombae.hfsexplorer.gui.HFSExplorerJFrame;
import org.catacombae.io.FileStream;
import org.catacombae.io.ReadableRandomAccessStream;
import org.catacombae.storage.fs.FSEntry;
import org.catacombae.storage.fs.FSFile;
import org.catacombae.storage.fs.FSFork;
import org.catacombae.storage.fs.FSForkType;
import org.catacombae.storage.fs.hfscommon.HFSCommonFSFile;
import org.catacombae.storage.fs.hfscommon.HFSCommonFSFolder;
import org.catacombae.storage.fs.hfscommon.HFSCommonFSLink;

import static java.lang.System.getLogger;
import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED;


/**
 * @author <a href="https://catacombae.org" target="_top">Erik Larsson</a>
 */
public class FileInfoWindow extends HFSExplorerJFrame {

    private static final Logger logger = getLogger(FileInfoWindow.class.getName());

    public FileInfoWindow(FSEntry fsEntry, String[] parentPath) {
        super("Info - " + fsEntry.getName());

        JScrollPane summaryPanelScroller = null;
        JScrollPane infoPanelScroller = null;

        JTabbedPane tabs = new JTabbedPane();

        // Summary panel
        try {
            FSEntrySummaryPanel summaryPanel = new FSEntrySummaryPanel(this, fsEntry, parentPath);
            summaryPanelScroller = new JScrollPane(summaryPanel, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_NEVER);

            tabs.addTab("Summary", summaryPanelScroller);
        } catch (Exception e) {
            GUIUtil.displayExceptionDialog(e, 20, this, "Exception while " +
                    "creating FSEntrySummaryPanel.");
            logger.log(Level.ERROR, e.getMessage(), e);
        }

        // Details panel
        try {
            JPanel infoPanel = null;
            if (fsEntry instanceof HFSCommonFSFile || fsEntry instanceof HFSCommonFSLink) {
                CommonHFSCatalogFile hfsFile;
                if (fsEntry instanceof HFSCommonFSFile)
                    hfsFile = ((HFSCommonFSFile) fsEntry).getInternalCatalogFile();
                else if (fsEntry instanceof HFSCommonFSLink)
                    hfsFile = ((HFSCommonFSLink) fsEntry).getInternalCatalogFileRecord().getData();
                else
                    throw new RuntimeException();

                if (hfsFile instanceof CommonHFSCatalogFile.HFSPlusImplementation) {
                    FileInfoPanel fip = new FileInfoPanel();
                    fip.setFields(((CommonHFSCatalogFile.HFSPlusImplementation) hfsFile).getUnderlying());
                    infoPanel = fip;
                } else {
                    StructViewPanel svp = new StructViewPanel("File", hfsFile.getStructElements());
                    infoPanel = svp;
                }
            } else if (fsEntry instanceof HFSCommonFSFolder) {
                CommonHFSCatalogFolder fld = ((HFSCommonFSFolder) fsEntry).getInternalCatalogFolder();
                if (fld instanceof CommonHFSCatalogFolder.HFSPlusImplementation) {
                    FolderInfoPanel fip = new FolderInfoPanel();
                    fip.setFields(((CommonHFSCatalogFolder.HFSPlusImplementation) fld).getUnderlying());
                    infoPanel = fip;
                } else {
                    StructViewPanel svp = new StructViewPanel("Folder", fld.getStructElements());
                    infoPanel = svp;
                }
            }

            if (infoPanel != null) {
                infoPanelScroller = new JScrollPane(infoPanel, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_NEVER);

                tabs.addTab("Detailed", infoPanelScroller);
            }

        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }

        // Resource fork panel
        JPanel resffPanel = null;
        ResourceForkReader resffReader = null;
        try {
            if (fsEntry instanceof FSFile fsFile) {
                FSFork resourceFork = fsFile.getForkByType(FSForkType.MACOS_RESOURCE);
                if (resourceFork != null && resourceFork.getLength() > 0) {
                    ReadableRandomAccessStream s =
                            resourceFork.getReadableRandomAccessStream();
                    try {
                        resffReader = new ResourceForkReader(s);
                        resffPanel = new ResourceForkViewPanel(resffReader);
                    } catch (Exception e) {
                        if (resffReader != null) {
                            resffReader.close();
                        } else if (s != null) {
                            s.close();
                        }

                        throw e;
                    }
                }
            }
        } catch (MalformedResourceForkException e) {
            logger.log(Level.DEBUG, "Malformed resource fork:");
            e.printStackTrace(System.err);

            resffPanel = new JPanel();
            resffPanel.setLayout(new BorderLayout());

            resffPanel.add(new JLabel("Invalid resource fork data", SwingConstants.CENTER), BorderLayout.CENTER);

            JButton saveDataButton = new JButton("Save data...");
            saveDataButton.addActionListener(e1 -> {
                try {
                    JFileChooser fc = new JFileChooser();
                    if (fc.showSaveDialog(fc) == JFileChooser.APPROVE_OPTION) {
                        ReadableRandomAccessStream rs =
                                fsEntry.getForkByType(FSForkType.MACOS_RESOURCE).
                                        getReadableRandomAccessStream();
                        FileStream fs = new FileStream(fc.getSelectedFile());

                        IOUtil.streamCopy(rs, fs, 1024 * 1024);
                    }
                } catch (Throwable t) {
                    logger.log(Level.DEBUG, "Exception while extracting resource fork to file:");
                    t.printStackTrace(System.err);

                    GUIUtil.displayExceptionDialog(t, 20,
                            FileInfoWindow.this,
                            "Exception while extracting resource fork to file:");
                }
            });
            resffPanel.add(saveDataButton, BorderLayout.SOUTH);
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            GUIUtil.displayExceptionDialog(e, 20, this, "Exception while creating ResourceForkViewPanel.");
        }

        if (resffPanel != null) {
            tabs.addTab("Resource fork", resffPanel);
        }

        add(tabs, BorderLayout.CENTER);

        if (summaryPanelScroller != null)
            summaryPanelScroller.getVerticalScrollBar().setUnitIncrement(10);
        if (infoPanelScroller != null)
            infoPanelScroller.getVerticalScrollBar().setUnitIncrement(10);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        pack();
        int width = getSize().width;
        int height = getSize().height;
        int adjustedHeight = width + width / 2;

        if (adjustedHeight < height)
            setSize(width, adjustedHeight);

        setLocationRelativeTo(null);

        ResourceForkReader resffReaderFinal = resffReader;
        addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent we) {
                // We know that this window won't be reused. It's recreated
                // every time, so under that assumption we can close the
                // ResourceForkReader passed to the ResourceForkViewerPanel.
                if (resffReaderFinal != null) {
                    resffReaderFinal.close();
                }
            }
        });
    }

//    public void setFields(FSFile file) {
//        if (file instanceof HFSCommonFSFile) {
//            CommonHFSCatalogFile hfsFile = ((HFSCommonFSFile) file).getInternalCatalogFile();
//            if (hfsFile instanceof CommonHFSCatalogFile.HFSPlusImplementation) {
//                FileInfoPanel infoPanel = new FileInfoPanel();
//                infoPanel.setFields(((CommonHFSCatalogFile.HFSPlusImplementation) hfsFile).getUnderlying());
//                infoPanelScroller.setViewportView(infoPanel);
//            } else {
//                StructViewPanel svp = new StructViewPanel("Folder:", hfsFile.getStructElements());
//                infoPanelScroller.setViewportView(svp);
//            }
//        } else
//            throw new RuntimeException("FSFolder type " + file.getClass() + " not yet supported!");
//    }

//    public void setFields(HFSPlusCatalogFile vh) {
//        infoPanel.setFields(vh);
//    }

//    public void setJournalFields(JournalInfoBlock jib) {
//        journalInfoPanel.setFields(jib);
//    }
}
