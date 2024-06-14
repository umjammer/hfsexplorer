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
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.charset.Charset;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.border.BevelBorder;
import javax.swing.table.DefaultTableModel;

import org.catacombae.hfsexplorer.Resources;

import static java.lang.System.getLogger;
import static org.jdesktop.layout.GroupLayout.BASELINE;
import static org.jdesktop.layout.GroupLayout.DEFAULT_SIZE;
import static org.jdesktop.layout.GroupLayout.LEADING;
import static org.jdesktop.layout.GroupLayout.PREFERRED_SIZE;
import static org.jdesktop.layout.GroupLayout.TRAILING;
import static org.jdesktop.layout.LayoutStyle.RELATED;


/**
 * @author <a href="https://catacombae.org" target="_top">Erik Larsson</a>
 */
public class FilesystemBrowserPanel extends javax.swing.JPanel {

    private static final Logger logger = getLogger(FilesystemBrowserPanel.class.getName());

    private static final ImageIcon FORWARD_ICON = new ImageIcon(Resources.FORWARD_ICON);
    private static final ImageIcon EXTRACT_ICON = new ImageIcon(Resources.EXTRACT_ICON);
    private static final ImageIcon BACK_ICON = new ImageIcon(Resources.BACK_ICON);
    private static final ImageIcon UP_ICON = new ImageIcon(Resources.UP_ICON);
    private static final ImageIcon INFO_ICON = new ImageIcon(Resources.INFO_ICON);

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
            if (Charset.isSupported(encoding) || Charset.isSupported("x-" + encoding)) {
                logger.log(Level.DEBUG, "Charset is supported: " + encoding);
                encodingComboBox.addItem(encoding);
            } else {
                logger.log(Level.DEBUG, "Charset is not supported: " + encoding);
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

        addressField = new JTextField();
        pathLabel = new JLabel();
        goButton = new JButton();
        extractButton = new JButton();
        upButton = new JButton();
        infoButton = new JButton();
        boxPanel = new JPanel();
        treeTablePanel = new JPanel();
        treeTableSplit = new JSplitPane();
        dirTreeScroller = new JScrollPane();
        dirTree = new JTree();
        fileTableScroller = new JScrollPane();
        fileTable = new JTable();
        statusLabelPanel = new JPanel();
        statusLabel = new JLabel();
        encodingLabel = new JLabel();
        encodingComboBox = new JComboBox<>();

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

        fileTable.setModel(new DefaultTableModel(
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
            final boolean[] canEdit = new boolean[] {
                    false, false, false, false
            };

            @Override
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
        statusLabel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        statusLabelPanel.add(statusLabel, java.awt.BorderLayout.CENTER);

        boxPanel.add(statusLabelPanel, java.awt.BorderLayout.SOUTH);

        encodingLabel.setText("Encoding:");

        encodingComboBox.setModel(new DefaultComboBoxModel<>(new String[] {"MacRoman", "MacJapanese"}));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(LEADING)
                        .add(TRAILING, layout.createSequentialGroup()
                                .addContainerGap()
                                .add(pathLabel)
                                .addPreferredGap(RELATED)
                                .add(addressField, DEFAULT_SIZE, 650, Short.MAX_VALUE)
                                .addPreferredGap(RELATED)
                                .add(goButton)
                                .addContainerGap())
                        .add(boxPanel, DEFAULT_SIZE, 812, Short.MAX_VALUE)
                        .add(layout.createSequentialGroup()
                                .addContainerGap()
                                .add(upButton)
                                .addPreferredGap(RELATED)
                                .add(extractButton)
                                .addPreferredGap(RELATED)
                                .add(infoButton)
                                .addPreferredGap(RELATED, 295, Short.MAX_VALUE)
                                .add(encodingLabel)
                                .addPreferredGap(RELATED)
                                .add(encodingComboBox, PREFERRED_SIZE, 166, PREFERRED_SIZE)
                                .addContainerGap())
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(LEADING)
                        .add(layout.createSequentialGroup()
                                .addContainerGap()
                                .add(layout.createParallelGroup(BASELINE)
                                        .add(upButton)
                                        .add(extractButton)
                                        .add(infoButton)
                                        .add(encodingComboBox, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
                                        .add(encodingLabel))
                                .addPreferredGap(RELATED)
                                .add(layout.createParallelGroup(BASELINE)
                                        .add(addressField, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
                                        .add(goButton)
                                        .add(pathLabel))
                                .addPreferredGap(RELATED)
                                .add(boxPanel, DEFAULT_SIZE, 429, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    public JTextField addressField;
    private JPanel boxPanel;
    public JTree dirTree;
    private JScrollPane dirTreeScroller;
    private JComboBox<String> encodingComboBox;
    private JLabel encodingLabel;
    public JButton extractButton;
    public JTable fileTable;
    public JScrollPane fileTableScroller;
    public JButton goButton;
    public JButton infoButton;
    private JLabel pathLabel;
    public JLabel statusLabel;
    private JPanel statusLabelPanel;
    private JPanel treeTablePanel;
    private JSplitPane treeTableSplit;
    public JButton upButton;
    // End of variables declaration//GEN-END:variables

//    public static void main(String[] args) {
//        JFrame jf = new JFrame("Test");
//        jf.add(new FilesystemBrowserPanel());
//        jf.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
//        jf.pack();
//        jf.setLocationRelativeTo(null);
//        jf.setVisible(true);
//    }
}
