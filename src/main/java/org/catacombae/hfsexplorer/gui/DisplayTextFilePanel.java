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

import java.awt.Frame;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Set;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.catacombae.io.InputStreamReadable;
import org.catacombae.io.Readable;

import static java.lang.System.getLogger;
import static javax.swing.JOptionPane.showMessageDialog;
import static org.jdesktop.layout.GroupLayout.BASELINE;
import static org.jdesktop.layout.GroupLayout.DEFAULT_SIZE;
import static org.jdesktop.layout.GroupLayout.LEADING;
import static org.jdesktop.layout.GroupLayout.PREFERRED_SIZE;
import static org.jdesktop.layout.LayoutStyle.RELATED;


/**
 * @author <a href="https://catacombae.org" target="_top">Erik Larsson</a>
 */
public class DisplayTextFilePanel extends javax.swing.JPanel {

    private static final Logger logger = getLogger(DisplayTextFilePanel.class.getName());

    private static final String[] sortingPrefixes = {"US-ASCII", "UTF-8", "ISO-8859", "UTF", "IBM4", "IBM8", "IBM"};
    private byte[] fileData = new byte[] {0};
    private final Frame parentFrame;
    private final String baseTitle;

    /** Creates new form DisplayTextFilePanel. */
    public DisplayTextFilePanel() {
        this(null);
    }

    /**
     * Creates new form DisplayTextFilePanel. Supplying a parent frame will lead to the frame's
     * title changing to reflect the currently displayed file.
     */
    public DisplayTextFilePanel(Frame parentFrame) {
        this.parentFrame = parentFrame;

        if (parentFrame != null)
            this.baseTitle = parentFrame.getTitle();
        else
            this.baseTitle = "";

        initComponents();

        textPaneScroller.getVerticalScrollBar().setMinimum(0);
        textPaneScroller.getVerticalScrollBar().setMaximum(Integer.MAX_VALUE);
        textPaneScroller.getHorizontalScrollBar().setMinimum(0);
        textPaneScroller.getHorizontalScrollBar().setMaximum(Integer.MAX_VALUE);

        Set<String> keySet = Charset.availableCharsets().keySet();
        ArrayList<String> charsets = new ArrayList<>(keySet);
        LinkedList<String> listItems = new LinkedList<>();
        for (String prefix : sortingPrefixes) {
            for (int i = 0; i < charsets.size(); ) {
                String curCharset = charsets.get(i);
                if (curCharset.startsWith(prefix)) {
                    listItems.add(curCharset);
                    charsets.remove(i);
                } else
                    ++i;
            }
        }
        listItems.addAll(charsets);

        encodingBox.removeAllItems();
        for (String curItem : listItems)
            encodingBox.addItem(curItem);
        if (encodingBox.getItemCount() > 0)
            encodingBox.setSelectedIndex(0);

        encodingBox.addActionListener(e -> refreshView());
    }

//    public void addFileDropListener() {
//        new FileDrop(textPaneScroller, new FileDrop.Listener() {
//            public void filesDropped(java.io.File[] files) {
//                // handle file drop
//                if (files.length == 1) {
//                    if (files[0].isFile()) {
//                        loadFile(files[0]);
//                    } else
//                        JOptionPane.showMessageDialog(DisplayTextFilePanel.this, "You can only view files.",
//                                "Error", JOptionPane.ERROR_MESSAGE);
//                } else if (files.length > 1) {
//                    JOptionPane.showMessageDialog(DisplayTextFilePanel.this, "You can only view one file at a time.",
//                            "Error", JOptionPane.ERROR_MESSAGE);
//                }
//            }
//        });
//    }

    public void loadFile(File file) {
        if (file.length() < Integer.MAX_VALUE) {
            try {
                FileInputStream fis = new FileInputStream(file);
                loadStream(fis);
                fis.close();
                if (parentFrame != null)
                    parentFrame.setTitle(baseTitle + " - [" + file.getName() + "]");
            } catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                showMessageDialog(this, "Exception while loading file:\n  " + e + "\"");
            }
        } else
            showMessageDialog(this, "File too large for memory address space! (" +
                    file.length() + "bytes)");
    }

    public void loadStream(InputStream is) {
        loadStream(new InputStreamReadable(is));
    }

    public void loadStream(Readable is) {
        try {
            byte[] tmp = new byte[65536];
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            int bytesRead;
            while ((bytesRead = is.read(tmp)) > 0)
                baos.write(tmp, 0, bytesRead);

            fileData = baos.toByteArray();
            baos = null;

            refreshView();
            SwingUtilities.invokeLater(() -> {
                int vMin = textPaneScroller.getVerticalScrollBar().getMinimum();
                int hMin = textPaneScroller.getHorizontalScrollBar().getMinimum();
                textPaneScroller.getVerticalScrollBar().setValue(vMin);
                textPaneScroller.getHorizontalScrollBar().setValue(hMin);
            });

        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            showMessageDialog(this, "Exception while loading file:\n  " + e + "\"");
        }
    }

    private void refreshView() {
        try {
            int vValue = textPaneScroller.getVerticalScrollBar().getValue();
            int hValue = textPaneScroller.getHorizontalScrollBar().getValue();
            textPane.setText(new String(fileData, getSelectedEncoding()));
            SwingUtilities.invokeLater(() -> {
                textPaneScroller.getVerticalScrollBar().setValue(vValue);
                textPaneScroller.getHorizontalScrollBar().setValue(hValue);
            });
        } catch (UnsupportedEncodingException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            showMessageDialog(this, "Exception while decoding file data:\n  " + e + "\"");
        }
    }

    private String getSelectedEncoding() {
        return encodingBox.getSelectedItem().toString();
    }

    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        textPaneScroller = new JScrollPane();
        textPane = new JTextArea();
        encodingLabel = new JLabel();
        encodingBox = new JComboBox<>();

        textPane.setColumns(20);
        textPane.setEditable(false);
        textPane.setRows(5);
        textPaneScroller.setViewportView(textPane);

        encodingLabel.setText("Encoding:");

        encodingBox.setModel(new DefaultComboBoxModel<>(new String[] {"Item 1", "Item 2", "Item 3", "Item 4"}));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(LEADING)
                        .add(layout.createSequentialGroup()
                                .addContainerGap()
                                .add(encodingLabel)
                                .addPreferredGap(RELATED)
                                .add(encodingBox, 0, 650, Short.MAX_VALUE)
                                .addContainerGap())
                        .add(textPaneScroller, DEFAULT_SIZE, 721, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(LEADING)
                        .add(layout.createSequentialGroup()
                                .addContainerGap()
                                .add(layout.createParallelGroup(BASELINE)
                                        .add(encodingLabel)
                                        .add(encodingBox, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE))
                                .addPreferredGap(RELATED)
                                .add(textPaneScroller, DEFAULT_SIZE, 445, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JComboBox<String> encodingBox;
    private JLabel encodingLabel;
    private JTextArea textPane;
    private JScrollPane textPaneScroller;
    // End of variables declaration//GEN-END:variables
}
