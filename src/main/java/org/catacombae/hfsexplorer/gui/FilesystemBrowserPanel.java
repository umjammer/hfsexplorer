/*-
 * Copyright (C) 2006-2021 Erik Larsson
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

import java.awt.event.ActionListener;
import java.nio.charset.Charset;
import java.util.logging.Logger;
import javax.swing.ImageIcon;

import org.catacombae.hfsexplorer.Resources;


/**
 * @author <a href="https://catacombae.org" target="_top">Erik Larsson</a>
 */
public class FilesystemBrowserPanel extends javax.swing.JPanel {

    private static final ImageIcon FORWARD_ICON =
            new ImageIcon(Resources.FORWARD_ICON);
    private static final ImageIcon EXTRACT_ICON =
            new ImageIcon(Resources.EXTRACT_ICON);
    private static final ImageIcon BACK_ICON =
            new ImageIcon(Resources.BACK_ICON);
    private static final ImageIcon UP_ICON =
            new ImageIcon(Resources.UP_ICON);
    private static final ImageIcon INFO_ICON =
            new ImageIcon(Resources.INFO_ICON);

    private static final String[] optionalEncodings = {
            "MacChineseTrad",
            "MacKorean",
            "MacArabic",
            "MacHebrew",
            "MacGreek",
            "MacCyrillic",
            "MacDevanagari",
            "MacGurmukhi",
            "MacGujarati",
            "MacOriya",
            "MacBengali",
            "MacTamil",
            "MacTelugu",
            "MacKannada",
            "MacMalayalam",
            "MacSinhalese",
            "MacBurmese",
            "MacKhmer",
            "MacThai",
            "MacLaotian",
            "MacGeorgian",
            "MacArmenian",
            "MacChineseSimp",
            "MacTibetan",
            "MacMongolian",
            "MacEthiopic",
            "MacCentralEurRoman",
            "MacVietnamese",
            "MacExtArabic",
            "MacSymbol",
            "MacDingbats",
            "MacTurkish",
            "MacCroatian",
            "MacIcelandic",
            "MacRomanian",
            "MacFarsi",
            "MacUkrainian",
    };

    private static final Logger log =
            Logger.getLogger(FilesystemBrowserPanel.class.getName());

    /** Creates new form FilesystemBrowserPanel */
    public FilesystemBrowserPanel() {
        initComponents();
        fileTableScroller.getViewport().setBackground(fileTable.getBackground()); // To remove the grey area below the actual table
        setHFSFieldsVisible(false);

        /*
         * Add optional encodings to the encoding combo box based on
         * availability.
         */
        for (String encoding : optionalEncodings) {
            if (Charset.isSupported(encoding) ||
                    Charset.isSupported("x-" + encoding)) {
                log.fine("Charset is supported: " + encoding);
                encodingComboBox.addItem(encoding);
            } else {
                log.fine("Charset is not supported: " + encoding);
            }
        }
    }

    public void setHFSFieldsVisible(boolean b) {
        encodingLabel.setVisible(b);
        encodingComboBox.setVisible(b);
    }

    /**
     * Get the selected encoding in the encodings combo box.
     *
     * @return the selected encoding in the encodings combo box.
     */
    public String getSelectedHFSEncoding() {
        return encodingComboBox.getSelectedItem().toString();
    }

    /**
     * Set the selected encoding in the encodings combo box to the item matching
     * the supplied string.
     *
     * @param encoding The encoding to select.
     * @return true if the encoding existed in the combo box, false otherwise.
     */
    public boolean setSelectedHFSEncoding(String encoding) {
        for (int i = 0; i < encodingComboBox.getItemCount(); ++i) {
            Object item = encodingComboBox.getItemAt(i);
            if (item.toString().equals(encoding)) {
                encodingComboBox.setSelectedIndex(i);
                return true;
            }
        }

        return false;
    }

    /**
     * Register a listener for when the user changes the selection in the
     * encodings combo box.
     *
     * @param al An {@link ActionListener} that will be notified.
     */
    public void registerHFSEncodingChangedListener(ActionListener al) {
        encodingComboBox.addActionListener(al);
    }

    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        addressField = new javax.swing.JTextField();
        pathLabel = new javax.swing.JLabel();
        goButton = new javax.swing.JButton();
        extractButton = new javax.swing.JButton();
        upButton = new javax.swing.JButton();
        infoButton = new javax.swing.JButton();
        boxPanel = new javax.swing.JPanel();
        treeTablePanel = new javax.swing.JPanel();
        treeTableSplit = new javax.swing.JSplitPane();
        dirTreeScroller = new javax.swing.JScrollPane();
        dirTree = new javax.swing.JTree();
        fileTableScroller = new javax.swing.JScrollPane();
        fileTable = new javax.swing.JTable();
        statusLabelPanel = new javax.swing.JPanel();
        statusLabel = new javax.swing.JLabel();
        encodingLabel = new javax.swing.JLabel();
        encodingComboBox = new javax.swing.JComboBox();

        pathLabel.setText("Path:");

        goButton.setIcon(FORWARD_ICON);
        goButton.setText("Go");

        extractButton.setIcon(EXTRACT_ICON);
        extractButton.setText("Extract");

        upButton.setIcon(UP_ICON);
        upButton.setText("Up");

        infoButton.setIcon(INFO_ICON);
        infoButton.setText("Info");

        boxPanel.setLayout(new java.awt.BorderLayout());

        treeTablePanel.setLayout(new java.awt.BorderLayout());

        treeTableSplit.setDividerLocation(200);
        treeTableSplit.setContinuousLayout(true);

        dirTreeScroller.setViewportView(dirTree);

        treeTableSplit.setLeftComponent(dirTreeScroller);

        fileTable.setModel(new javax.swing.table.DefaultTableModel(
                new Object[][] {
                        {"yada.txt", "1 KiB", "File", "2006-06-11 14:34"},
                        {null, null, null, null},
                        {null, null, null, null},
                        {null, null, null, null}
                },
                new String[] {
                        "Name", "Size", "Type", "Date modified"
                }
        ) {
            boolean[] canEdit = new boolean[] {
                    false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
            }
        });
        fileTable.setIntercellSpacing(new java.awt.Dimension(4, 0));
        fileTable.setShowHorizontalLines(false);
        fileTable.setShowVerticalLines(false);
        fileTableScroller.setViewportView(fileTable);

        treeTableSplit.setRightComponent(fileTableScroller);

        treeTablePanel.add(treeTableSplit, java.awt.BorderLayout.CENTER);

        boxPanel.add(treeTablePanel, java.awt.BorderLayout.CENTER);

        statusLabelPanel.setLayout(new java.awt.BorderLayout());

        statusLabel.setText("No file system loaded");
        statusLabel.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        statusLabelPanel.add(statusLabel, java.awt.BorderLayout.CENTER);

        boxPanel.add(statusLabelPanel, java.awt.BorderLayout.SOUTH);

        encodingLabel.setText("Encoding:");

        encodingComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] {"MacRoman", "MacJapanese"}));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                                .addContainerGap()
                                .add(pathLabel)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(addressField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 650, Short.MAX_VALUE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(goButton)
                                .addContainerGap())
                        .add(boxPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 812, Short.MAX_VALUE)
                        .add(layout.createSequentialGroup()
                                .addContainerGap()
                                .add(upButton)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(extractButton)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(infoButton)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 295, Short.MAX_VALUE)
                                .add(encodingLabel)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(encodingComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 166, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap())
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(layout.createSequentialGroup()
                                .addContainerGap()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                        .add(upButton)
                                        .add(extractButton)
                                        .add(infoButton)
                                        .add(encodingComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .add(encodingLabel))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                        .add(addressField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .add(goButton)
                                        .add(pathLabel))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(boxPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 429, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    public javax.swing.JTextField addressField;
    private javax.swing.JPanel boxPanel;
    public javax.swing.JTree dirTree;
    private javax.swing.JScrollPane dirTreeScroller;
    private javax.swing.JComboBox encodingComboBox;
    private javax.swing.JLabel encodingLabel;
    public javax.swing.JButton extractButton;
    public javax.swing.JTable fileTable;
    public javax.swing.JScrollPane fileTableScroller;
    public javax.swing.JButton goButton;
    public javax.swing.JButton infoButton;
    private javax.swing.JLabel pathLabel;
    public javax.swing.JLabel statusLabel;
    private javax.swing.JPanel statusLabelPanel;
    private javax.swing.JPanel treeTablePanel;
    private javax.swing.JSplitPane treeTableSplit;
    public javax.swing.JButton upButton;
    // End of variables declaration//GEN-END:variables

    /*
    public static void main(String[] args) {
        JFrame jf = new JFrame("Test");
        jf.add(new FilesystemBrowserPanel());
        jf.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        jf.pack();
        jf.setLocationRelativeTo(null);
        jf.setVisible(true);
    }
    */
}
