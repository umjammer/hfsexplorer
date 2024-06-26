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

package org.catacombae.hfsexplorer.gui;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.text.DecimalFormat;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.catacombae.util.ObjectContainer;
import org.catacombae.hfsexplorer.SpeedUnitUtils;
import org.catacombae.util.Util;
import org.catacombae.storage.fs.FSAttributes;
import org.catacombae.storage.fs.FSEntry;
import org.catacombae.storage.fs.FSFile;
import org.catacombae.storage.fs.FSFolder;
import org.catacombae.storage.fs.FSLink;

import static java.lang.System.getLogger;


/**
 * @author <a href="https://catacombae.org" target="_top">Erik Larsson</a>
 */
public class FSEntrySummaryPanel extends javax.swing.JPanel implements ChainedPanel {

    private static final Logger logger = getLogger(FSEntrySummaryPanel.class.getName());

    private volatile boolean cancelSignaled = false;
    private final DecimalFormat sizeFormatter = new DecimalFormat("0.00");

    FSEntrySummaryPanel() {
        initComponents();
    }

    /* NOTES
     *
     * An FSEntry always has:
     * - Name (every FSEntry must have a name, even if it may be blank)
     * - Location (the parent to the FSEntry). A POSIX pathname (possibly translated) must also
     *   exist.
     * - Type (Folder, Link or File)
     * - Size can be derived from all FSEntries (through recursive search or by looking up a file
     *   entry) and should be considered a must-have field.
     * - Size on disk.
     *
     * An FSEntry may optionally have:
     * - Various date variables.
     * - POSIX permissions.
     * - Global attributes such as "hidden", "write protected", "bundle"...
     *
     * An FSFile always has:
     * - A number of forks (possibly 0), where each fork has
     *   - Identifier
     *   - Length
     *
     * An FSFolder always has:
     * - Valence
     *
     * An FSLink always has:
     * - Link target
     *
     * Note to self: The file system model does not support forks tied to folders. This is a
     * limitation. Some file system out there may be using this method to store metadata about
     * folders.
     */

    /** Creates new form FSEntrySummaryPanel */
    public FSEntrySummaryPanel(Window window, FSEntry entry, String[] parentPath) {
        this();

        window.addWindowListener(new WindowAdapter() {

//            @Override
//            public void windowOpened(WindowEvent e) {
//                throw new UnsupportedOperationException("Not supported yet.");
//            }

            @Override
            public void windowClosing(WindowEvent e) {
//                logger.log(Level.DEBUG, "Window closing. Signaling any calculate process to stop.");
                cancelSignaled = true;
            }

//            @Override
//            public void windowClosed(WindowEvent e) {
//                throw new UnsupportedOperationException("Not supported yet.");
//            }
//
//            @Override
//            public void windowIconified(WindowEvent e) {
//                throw new UnsupportedOperationException("Not supported yet.");
//            }
//
//            @Override
//            public void windowDeiconified(WindowEvent e) {
//                throw new UnsupportedOperationException("Not supported yet.");
//            }
//
//            @Override
//            public void windowActivated(WindowEvent e) {
//                throw new UnsupportedOperationException("Not supported yet.");
//            }
//
//            @Override
//            public void windowDeactivated(WindowEvent e) {
//                throw new UnsupportedOperationException("Not supported yet.");
//            }
        });

        nameField.setText(entry.getName());
        String typeString;
        String sizeString;
        String occupiedSizeString;
        if (entry instanceof FSFile file) {
            typeString = "File";
            sizeString = getSizeString(file.getMainFork().getLength());
            occupiedSizeString = getSizeString(file.getMainFork().getOccupiedSize());
        } else if (entry instanceof FSFolder folder) {
            typeString = "Folder";
            sizeString = "Calculating...";
            occupiedSizeString = "Calculating...";
            startFolderSizeCalculation(folder);
        } else if (entry instanceof FSLink link) {
            FSEntry linkTarget = link.getLinkTarget(parentPath);
            if (linkTarget == null) {
                typeString = "Symbolic link (broken)";
                sizeString = "- (broken link)";
                occupiedSizeString = "- (broken link)";
            } else if (linkTarget instanceof FSFile file) {
                typeString = "Symbolic link (file)";
                sizeString = getSizeString(file.getMainFork().getLength());
                occupiedSizeString = getSizeString(file.getMainFork().getOccupiedSize());
            } else if (linkTarget instanceof FSFolder folder) {
                typeString = "Symbolic link (folder)";
                sizeString = "Calculating...";
                occupiedSizeString = "Calculating...";
                startFolderSizeCalculation(folder);
            } else {
                typeString = "Symbolic link (unknown [" + linkTarget.getClass() + "])";
                sizeString = "- (unknown type)";
                occupiedSizeString = "- (unknown type)";
            }
        } else {
            typeString = "Unknown [" + entry.getClass() + "]";
            sizeString = "- (unknown type)";
            occupiedSizeString = "- (unknown type)";
        }
        typeField.setText(typeString);
        sizeField.setText(sizeString);
        occupiedSizeField.setText(occupiedSizeString);

        FSAttributes attrs = entry.getAttributes();

        ChainedPanel currentChain = this;

        if (entry instanceof FSLink) {
            LinkTargetPanel ltp = new LinkTargetPanel((FSLink) entry);
            currentChain.setChainedContents(ltp);
            currentChain = ltp;
        }

        DateSummaryPanel dsp = new DateSummaryPanel(attrs);
        currentChain.setChainedContents(dsp);
        currentChain = dsp;

        if (attrs.hasPOSIXFileAttributes()) {
            POSIXAttributesPanel attributesPanel = new POSIXAttributesPanel(attrs.getPOSIXFileAttributes());
            currentChain.setChainedContents(attributesPanel);
            currentChain = attributesPanel;
        }
    }

    @Override
    public void setChainedContents(Component c) {
        extendedInfoStackPanel.removeAll();
        extendedInfoStackPanel.add(c);
    }

    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        nameField = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        typeField = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        sizeField = new javax.swing.JTextField();
        occupiedSizeLabel = new javax.swing.JLabel();
        occupiedSizeField = new javax.swing.JTextField();
        jSeparator1 = new javax.swing.JSeparator();
        extendedInfoStackPanel = new javax.swing.JPanel();

        jLabel1.setText("Name:");

        nameField.setEditable(false);
        nameField.setText("jTextField1");
        nameField.setOpaque(false);

        jLabel2.setText("Type:");

        typeField.setEditable(false);
        typeField.setText("jTextField2");
        typeField.setBorder(null);
        typeField.setOpaque(false);

        jLabel3.setText("Size:");

        sizeField.setEditable(false);
        sizeField.setText("jTextField3");
        sizeField.setBorder(null);
        sizeField.setOpaque(false);

        occupiedSizeLabel.setText("Size on disk:");

        occupiedSizeField.setEditable(false);
        occupiedSizeField.setText("occupiedSizeField");
        occupiedSizeField.setBorder(null);
        occupiedSizeField.setOpaque(false);

        extendedInfoStackPanel.setLayout(new javax.swing.BoxLayout(extendedInfoStackPanel, javax.swing.BoxLayout.PAGE_AXIS));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                                .addContainerGap()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                        .add(org.jdesktop.layout.GroupLayout.LEADING, extendedInfoStackPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 340, Short.MAX_VALUE)
                                        .add(org.jdesktop.layout.GroupLayout.LEADING, jSeparator1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 340, Short.MAX_VALUE)
                                        .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                                        .add(jLabel1)
                                                        .add(jLabel2)
                                                        .add(jLabel3)
                                                        .add(occupiedSizeLabel))
                                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                                        .add(occupiedSizeField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 305, Short.MAX_VALUE)
                                                        .add(sizeField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 305, Short.MAX_VALUE)
                                                        .add(typeField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 305, Short.MAX_VALUE)
                                                        .add(nameField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 305, Short.MAX_VALUE))))
                                .addContainerGap())
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(layout.createSequentialGroup()
                                .addContainerGap()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                        .add(jLabel1)
                                        .add(nameField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                        .add(jLabel2)
                                        .add(typeField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                        .add(jLabel3)
                                        .add(sizeField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                        .add(occupiedSizeLabel)
                                        .add(occupiedSizeField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(extendedInfoStackPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 119, Short.MAX_VALUE)
                                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel extendedInfoStackPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTextField nameField;
    private javax.swing.JTextField occupiedSizeField;
    private javax.swing.JLabel occupiedSizeLabel;
    private javax.swing.JTextField sizeField;
    private javax.swing.JTextField typeField;
    // End of variables declaration//GEN-END:variables

    private String getSizeString(long result) {
        String baseString = Long.toString(result);
        if (result >= 1000) {
            String spacedString = Util.addUnitSpaces(baseString, 3);

            if (result >= 1024) {
                return SpeedUnitUtils.bytesToBinaryUnit(result, sizeFormatter) + " (" + spacedString + " bytes)";
            } else
                return spacedString + " bytes";
        } else
            return baseString + " bytes";
    }

    private void startFolderSizeCalculation(FSFolder folder) {
        Runnable r = () -> {
            String sizeResultString;
            String occupiedSizeResultString;
            try {
                ObjectContainer<Long> sizeResult = new ObjectContainer<>((long) 0);
                ObjectContainer<Long> occupiedSizeResult = new ObjectContainer<>((long) 0);
                calculateFolderSize(folder, sizeResult, occupiedSizeResult);
                sizeResultString = getSizeString(sizeResult.o);
                occupiedSizeResultString = getSizeString(occupiedSizeResult.o);
            } catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                sizeResultString = "Exception while calculating! See debug console " + "for info...";
                occupiedSizeResultString = "Exception while calculating! See debug console " + "for info...";
            }

            String finalSizeResultString;
            String finalOccupiedSizeResultString;
            if (!cancelSignaled) {
                finalSizeResultString = sizeResultString;
                finalOccupiedSizeResultString = occupiedSizeResultString;
            } else {
                finalSizeResultString = "Canceled";
                finalOccupiedSizeResultString = "Canceled";
            }

            SwingUtilities.invokeLater(() -> {
                sizeField.setText(finalSizeResultString);
                occupiedSizeField.setText(finalOccupiedSizeResultString);
            });
        };

        new Thread(r).start();
    }

    private void calculateFolderSize(FSFolder folder,
                                     ObjectContainer<Long> sizeResult,
                                     ObjectContainer<Long> occupiedSizeResult) {
        if (cancelSignaled) {
            logger.log(Level.DEBUG, "Calculate process stopping for folder " + "\"" + folder.getName() + "\"");

            return;
        }

        for (FSEntry entry : folder.listEntries()) {
            if (cancelSignaled) {
                logger.log(Level.DEBUG, "Calculate process stopping for " + "folder \"" + folder.getName() + "\", entry " +
                        "\"" + entry.getName() + "\"");

                return;
            }

            if (entry instanceof FSFile) {
                sizeResult.o = sizeResult.o + ((FSFile) entry).getMainFork().getLength();
                occupiedSizeResult.o = occupiedSizeResult.o + ((FSFile) entry).getMainFork().getOccupiedSize();
            } else if (entry instanceof FSFolder) {
                calculateFolderSize((FSFolder) entry, sizeResult, occupiedSizeResult);
            } else if (entry instanceof FSLink) {
                // Do nothing. Symbolic link targets aren't part of the folder.
            } else
                logger.log(Level.DEBUG, "FSEntrySummaryPanel.calculateFolderSize():" + " unexpected type " + entry.getClass());
        }
    }

    public static void main(String[] args) {
        JFrame jf = new JFrame("Test");
        jf.add(new JScrollPane(new FSEntrySummaryPanel()));
        jf.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        jf.pack();
        jf.setLocationRelativeTo(null);
        jf.setVisible(true);
    }
}
