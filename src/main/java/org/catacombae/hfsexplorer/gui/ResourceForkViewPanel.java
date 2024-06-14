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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.LinkedList;
import java.util.List;
import javax.swing.AbstractListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import org.catacombae.hfsexplorer.GUIUtil;
import org.catacombae.hfsexplorer.IOUtil;
import org.catacombae.hfsexplorer.fs.ResourceForkReader;
import org.catacombae.hfsexplorer.types.resff.ReferenceListEntry;
import org.catacombae.hfsexplorer.types.resff.ResourceMap;
import org.catacombae.hfsexplorer.types.resff.ResourceName;
import org.catacombae.hfsexplorer.types.resff.ResourceType;
import org.catacombae.io.ReadableRandomAccessStream;
import org.catacombae.util.Util;
import org.catacombae.util.Util.Pair;
import org.jdesktop.layout.GroupLayout;

import static java.lang.System.getLogger;
import static org.jdesktop.layout.GroupLayout.BASELINE;
import static org.jdesktop.layout.GroupLayout.DEFAULT_SIZE;
import static org.jdesktop.layout.GroupLayout.LEADING;
import static org.jdesktop.layout.GroupLayout.PREFERRED_SIZE;
import static org.jdesktop.layout.GroupLayout.TRAILING;


/**
 * @author <a href="https://catacombae.org" target="_top">Erik Larsson</a>
 */
public class ResourceForkViewPanel extends javax.swing.JPanel {

    private static final Logger logger = getLogger(ResourceForkViewPanel.class.getName());

    private ResourceForkReader reader = null;

    /**
     * An item as it is displayed in the list view over available resources.
     * Its toString method decides how it is displayed to the user.
     */
    private static class ListItem {

        final ResourceType type;
        final ReferenceListEntry entry;
        final ResourceName name;
        final long size;

        public ListItem(ResourceType type,
                        ReferenceListEntry entry,
                        ResourceName name,
                        long size) {
            this.type = type;
            this.entry = entry;
            this.name = name;
            this.size = size;
        }

        @Override
        public String toString() {
            try {
                StringBuilder sb = new StringBuilder();
                sb.append(new String(type.getType(), "MacRoman"));

                if (name != null)
                    sb.append(" \"").append(new String(name.getName(), "MacRoman")).append("\"");
                return sb.toString();
            } catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                return "{" + e.getClass().getSimpleName() + " in resource id " + entry.getResourceID() + "}";
            }
        }
    }

    /** Creates new form ResourceForkViewPanel */
    public ResourceForkViewPanel(ResourceForkReader startupReader) {
        initComponents();
        resourceList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        loadResourceFork(startupReader);

        resourceList.addListSelectionListener(e -> {
            Object o = resourceList.getSelectedValue();
            if (o instanceof ListItem)
                setSelectedItem((ListItem) o);
            else if (o != null)
                JOptionPane.showMessageDialog(resourceList, "Unexpected type in list: " + o.getClass());
        });

        viewButton.addActionListener(e -> {
            Object selection = resourceList.getSelectedValue();
            if (selection != null && selection instanceof ListItem selectedItem) {
                JDialog d = new JDialog(JOptionPane.getFrameForComponent(ResourceForkViewPanel.this),
                        selection.toString(), true);

                DisplayTextFilePanel dtfp = new DisplayTextFilePanel();
                dtfp.loadStream(reader.getResourceStream(selectedItem.entry));

                d.add(dtfp);
                d.pack();
                d.setLocationRelativeTo(null);
                d.setVisible(true);
            }
        });

        extractButton.addActionListener(new ActionListener() {
            private final JFileChooser fileChooser = new JFileChooser();

            {
                fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                fileChooser.setMultiSelectionEnabled(false);
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                Object selection = resourceList.getSelectedValue();
                if (selection != null && selection instanceof ListItem selectedItem) {

                    if (fileChooser.showSaveDialog(ResourceForkViewPanel.this) == JFileChooser.APPROVE_OPTION) {
                        File saveFile = fileChooser.getSelectedFile();
                        if (saveFile.exists()) {
                            int res = JOptionPane.showConfirmDialog(ResourceForkViewPanel.this,
                                    "The file already exists. Do you want to overwrite?",
                                    "Confirm overwrite", JOptionPane.YES_NO_OPTION,
                                    JOptionPane.QUESTION_MESSAGE);
                            if (res != JOptionPane.YES_OPTION)
                                return;
                        }

                        ReadableRandomAccessStream in = null;
                        FileOutputStream fos = null;
                        try {
                            in = reader.getResourceStream(selectedItem.entry);
                            fos = new FileOutputStream(saveFile);

                            IOUtil.streamCopy(in, fos, 65536);
                        } catch (FileNotFoundException fnfe) {
                            JOptionPane.showMessageDialog(ResourceForkViewPanel.this,
                                    "Could not open file \"" + saveFile.getPath() + "\" for writing...",
                                    "Error", JOptionPane.ERROR_MESSAGE);
                        } catch (IOException ioe) {
                            logger.log(Level.ERROR, ioe.getMessage(), ioe);
                            GUIUtil.displayExceptionDialog(ioe, ResourceForkViewPanel.this);
                        } finally {
                            if (in != null)
                                in.close();
                            if (fos != null) {
                                try {
                                    fos.close();
                                } catch (IOException ex) {
                                    logger.log(Level.ERROR, ex.getMessage(), ex);
                                    GUIUtil.displayExceptionDialog(ex, ResourceForkViewPanel.this);
                                }
                            }
                        }
                    }
                }
            }

        });
    }

    public final void loadResourceFork(ResourceForkReader reader) {
        if (reader != null) {
            ListItem[] allItems = listAllItems(reader);
            resourceList.setEnabled(true);
            resourceList.setListData(allItems);
            resourceListLabel.setText("Resource list (" + allItems.length + " items):");
        } else {
            resourceList.setEnabled(false);
            resourceList.setListData(new Object[0]);
            resourceListLabel.setText("Resource list:");
        }
        setSelectedItem(null);
        this.reader = reader;
    }

    private static ListItem[] listAllItems(ResourceForkReader reader) {
//        logger.log(Level.DEBUG, "listAllItems(): getting resource map");
        ResourceMap resMap = reader.getResourceMap();

        LinkedList<ListItem> result = new LinkedList<>();

//        logger.log(Level.DEBUG, "listAllItems(): getting reference list for " + resMap);
        List<Pair<ResourceType, ReferenceListEntry[]>> refList = resMap.getReferenceList();
        for (Pair<ResourceType, ReferenceListEntry[]> p : refList) {
            ResourceType type = p.getA();
            for (ReferenceListEntry entry : p.getB()) {
//                logger.log(Level.DEBUG, "listAllItems(): getting name by reflist entry " + entry);
                ResourceName name = resMap.getNameByReferenceListEntry(entry);
                long size = reader.getDataLength(entry);

                result.add(new ListItem(type, entry, name, size));
            }
        }

        return result.toArray(ListItem[]::new);
    }

    private void setSelectedItem(ListItem li) {
        boolean enabled;
        if (li == null)
            enabled = false;
        else
            enabled = true;

        extractButton.setEnabled(enabled);
        viewButton.setEnabled(enabled);
        nameField.setEnabled(enabled);
        typeField.setEnabled(enabled);
        idField.setEnabled(enabled);
        sizeField.setEnabled(enabled);
        attributesField.setEnabled(enabled);

        if (!enabled) {
            nameField.setText("");
            typeField.setText("");
            idField.setText("");
            sizeField.setText("");
            attributesField.setText("");
        } else {
            String nameString;
            if (li.name != null) {
                try {
                    nameString = new String(li.name.getName(), "MacRoman");
                } catch (Exception e) {
                    logger.log(Level.ERROR, e.getMessage(), e);
                    nameString = "[Could not decode: " + e + "]";
                }
            } else {
                nameString = null;
            }

            String typeString;
            try {
                typeString = new String(li.type.getType(), "MacRoman");
            } catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
                typeString = "[Could not decode: " + e + "]";
            }

            if (nameField == null) {
                nameField.setEnabled(false);
                nameField.setName("");
            } else
                nameField.setText(nameString);

            typeField.setText(typeString);
            idField.setText("" + li.entry.getResourceID());
            sizeField.setText(li.size + " bytes");
            attributesField.setText("0x" + Util.toHexStringBE(li.entry.getResourceAttributes()));
        }
    }

    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        resourceListLabel = new JLabel();
        resourceListScroller = new JScrollPane();
        resourceList = new JList<>();
        fieldsPanel = new JPanel();
        nameLabel = new JLabel();
        nameField = new JTextField();
        typeLabel = new JLabel();
        typeField = new JTextField();
        idLabel = new JLabel();
        idField = new JTextField();
        sizeLabel = new JLabel();
        sizeField = new JTextField();
        attributesLabel = new JLabel();
        attributesField = new JTextField();
        extractButton = new JButton();
        viewButton = new JButton();

        resourceListLabel.setText("[This label is set programmatically]");

        resourceList.setModel(new AbstractListModel<>() {
            final String[] strings = {"Item 1", "Item 2", "Item 3", "Item 4", "Item 5"};

            @Override
            public int getSize() {
                return strings.length;
            }

            @Override
            public Object getElementAt(int i) {
                return strings[i];
            }
        });
        resourceListScroller.setViewportView(resourceList);

        nameLabel.setText("Name:");

        nameField.setEditable(false);
        nameField.setText("jTextField1");
        nameField.setOpaque(false);

        typeLabel.setText("Type:");

        typeField.setEditable(false);
        typeField.setText("jTextField2");
        typeField.setOpaque(false);

        idLabel.setText("ID:");

        idField.setEditable(false);
        idField.setText("jTextField3");
        idField.setOpaque(false);

        sizeLabel.setText("Size:");

        sizeField.setEditable(false);
        sizeField.setText("jTextField4");
        sizeField.setOpaque(false);

        attributesLabel.setText("Attributes:");

        attributesField.setEditable(false);
        attributesField.setText("jTextField5");
        attributesField.setOpaque(false);

        GroupLayout fieldsPanelLayout = new GroupLayout(fieldsPanel);
        fieldsPanel.setLayout(fieldsPanelLayout);
        fieldsPanelLayout.setHorizontalGroup(
                fieldsPanelLayout.createParallelGroup(LEADING)
                        .add(fieldsPanelLayout.createSequentialGroup()
                                .add(fieldsPanelLayout.createParallelGroup(LEADING)
                                        .add(nameLabel)
                                        .add(typeLabel)
                                        .add(idLabel)
                                        .add(sizeLabel)
                                        .add(attributesLabel))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(fieldsPanelLayout.createParallelGroup(LEADING)
                                        .add(attributesField, DEFAULT_SIZE, 227, Short.MAX_VALUE)
                                        .add(sizeField, DEFAULT_SIZE, 227, Short.MAX_VALUE)
                                        .add(idField, DEFAULT_SIZE, 227, Short.MAX_VALUE)
                                        .add(nameField, DEFAULT_SIZE, 227, Short.MAX_VALUE)
                                        .add(typeField, DEFAULT_SIZE, 227, Short.MAX_VALUE)))
        );
        fieldsPanelLayout.setVerticalGroup(
                fieldsPanelLayout.createParallelGroup(LEADING)
                        .add(fieldsPanelLayout.createSequentialGroup()
                                .add(fieldsPanelLayout.createParallelGroup(BASELINE)
                                        .add(nameLabel)
                                        .add(nameField, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(fieldsPanelLayout.createParallelGroup(BASELINE)
                                        .add(typeLabel)
                                        .add(typeField, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(fieldsPanelLayout.createParallelGroup(BASELINE)
                                        .add(idLabel)
                                        .add(idField, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(fieldsPanelLayout.createParallelGroup(BASELINE)
                                        .add(sizeLabel)
                                        .add(sizeField, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(fieldsPanelLayout.createParallelGroup(BASELINE)
                                        .add(attributesLabel)
                                        .add(attributesField, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)))
        );

        extractButton.setText("Save to file...");

        viewButton.setText("View as text");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(LEADING)
                        .add(TRAILING, layout.createSequentialGroup()
                                .addContainerGap()
                                .add(layout.createParallelGroup(TRAILING)
                                        .add(LEADING, resourceListScroller, DEFAULT_SIZE, 283, Short.MAX_VALUE)
                                        .add(LEADING, resourceListLabel)
                                        .add(layout.createSequentialGroup()
                                                .add(viewButton)
                                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                                .add(extractButton))
                                        .add(LEADING, fieldsPanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE))
                                .addContainerGap())
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(LEADING)
                        .add(layout.createSequentialGroup()
                                .addContainerGap()
                                .add(resourceListLabel)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(resourceListScroller, DEFAULT_SIZE, 99, Short.MAX_VALUE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(fieldsPanel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(BASELINE)
                                        .add(extractButton)
                                        .add(viewButton))
                                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JTextField attributesField;
    private JLabel attributesLabel;
    private JButton extractButton;
    private JPanel fieldsPanel;
    private JTextField idField;
    private JLabel idLabel;
    private JTextField nameField;
    private JLabel nameLabel;
    private JList<Object> resourceList;
    private JLabel resourceListLabel;
    private JScrollPane resourceListScroller;
    private JTextField sizeField;
    private JLabel sizeLabel;
    private JTextField typeField;
    private JLabel typeLabel;
    private JButton viewButton;
    // End of variables declaration//GEN-END:variables
}
