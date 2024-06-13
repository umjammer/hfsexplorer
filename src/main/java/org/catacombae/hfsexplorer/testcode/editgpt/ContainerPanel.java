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

package org.catacombae.hfsexplorer.testcode.editgpt;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JOptionPane;

import org.catacombae.csjc.structelements.Array;
import org.catacombae.csjc.structelements.Dictionary;
import org.catacombae.csjc.structelements.StringRepresentableField;
import org.catacombae.csjc.structelements.StructElement;


/**
 * @author <a href="https://catacombae.org" target="_top">Erik Larsson</a>
 */
public class ContainerPanel extends javax.swing.JPanel {

    private LinkedList<ContainerPanel> subPanels = new LinkedList<ContainerPanel>();
    private LinkedList<EditStringValuePanel> fields = new LinkedList<EditStringValuePanel>();

    public ContainerPanel() {
        this(null);
    }

    /** Creates new form ContainerPanel */
    public ContainerPanel(String label) {
        initComponents();

        if (label != null) {
            descriptionLabel.setText(label);
            saveButton.setVisible(false);
        } else {
            descriptionLabel.setVisible(false);
            saveButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    actionSave();
                }
            });
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

        contentsPanel = new javax.swing.JPanel();
        descriptionLabel = new javax.swing.JLabel();
        saveButton = new javax.swing.JButton();

        contentsPanel.setLayout(new javax.swing.BoxLayout(contentsPanel, javax.swing.BoxLayout.PAGE_AXIS));

        descriptionLabel.setText("jLabel1");

        saveButton.setText("Save");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(layout.createSequentialGroup()
                                .addContainerGap()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                        .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                                                .add(10, 10, 10)
                                                .add(contentsPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 219, Short.MAX_VALUE))
                                        .add(layout.createSequentialGroup()
                                                .add(descriptionLabel)
                                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 109, Short.MAX_VALUE)
                                                .add(saveButton)))
                                .addContainerGap())
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(layout.createSequentialGroup()
                                .addContainerGap()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                        .add(descriptionLabel)
                                        .add(saveButton))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(contentsPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 107, Short.MAX_VALUE)
                                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    public void setFields(Dictionary rootDict) {
        contentsPanel.removeAll();
        String[] keys = rootDict.getKeys();

        for (String key : keys) {
            System.err.println("setFields processing key \"" + key + "\"...");
            StructElement curElem = rootDict.getElement(key);
            System.err.println("  curElem = " + curElem);
            if (curElem instanceof StringRepresentableField) {
                StringRepresentableField curField = (StringRepresentableField) curElem;
                EditStringValuePanel panel = new EditStringValuePanel();
                panel.setDecription(key + " (" + curField.getTypeName() + ")");
                panel.setValue(curField.getValueAsString());
                panel.setUserData(curField);
                System.err.println("  (1)adding " + panel + " to containerpanel");
                addComponent(panel);
                fields.add(panel);
            } else if (curElem instanceof Dictionary) {
                Dictionary curDict = (Dictionary) curElem;
                ContainerPanel panel = new ContainerPanel(key + " (" + curDict.getTypeName() + ")");
                panel.setFields(curDict);
                System.err.println("  (2)adding " + panel + " to containerpanel");
                addComponent(panel);
                subPanels.add(panel);
            } else if (curElem instanceof Array) {
                Array curArray = (Array) curElem;
                ContainerPanel panel = new ContainerPanel(key + " (" + curArray.getTypeName() + ")");
                panel.setFields(curArray);
                System.err.println("  (2)adding " + panel + " to containerpanel");
                addComponent(panel);
                subPanels.add(panel);
            } else
                throw new RuntimeException("Unknown StructElement type: " + curElem.getClass());
        }
        contentsPanel.add(Box.createVerticalGlue());
    }

    public void setFields(Array rootArray) {
        contentsPanel.removeAll();
        StructElement[] elements = rootArray.getElements();

        for (int i = 0; i < elements.length; ++i) {
            StructElement curElem = elements[i];
            System.err.println("setFields processing array element...");
            System.err.println("  curElem = " + curElem);
            if (curElem instanceof StringRepresentableField) {
                StringRepresentableField curField = (StringRepresentableField) curElem;
                EditStringValuePanel panel = new EditStringValuePanel();
                panel.setDecription("[" + i + "] (" + curField.getTypeName() + ")");
                panel.setValue(curField.getValueAsString());
                panel.setUserData(curField);
                System.err.println("  (1)adding " + panel + " to containerpanel");
                addComponent(panel);
            } else if (curElem instanceof Dictionary) {
                Dictionary curDict = (Dictionary) curElem;
                ContainerPanel panel = new ContainerPanel("[" + i + "] (" + curDict.getTypeName() + ")");
                panel.setFields(curDict);
                System.err.println("  (2)adding " + panel + " to containerpanel");
                addComponent(panel);
            } else if (curElem instanceof Array) {
                Array curArray = (Array) curElem;
                ContainerPanel panel = new ContainerPanel("[" + i + "] (" + curArray.getTypeName() + ")");
                panel.setFields(curArray);
                System.err.println("  (3)adding " + panel + " to containerpanel");
                addComponent(panel);
            } else
                throw new RuntimeException("Unknown StructElement type: " + curElem.getClass());
        }
        contentsPanel.add(Box.createVerticalGlue());
    }


    private void actionSave() {
        // Gather all the modified components
        List<EditStringValuePanel> modifiedFields = getModifiedFields();
        if (modifiedFields.size() == 0) {
            JOptionPane.showMessageDialog(this, "Nothing to save.", "Information",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Validate their data
        StringBuilder messageBuilder = new StringBuilder();
        for (EditStringValuePanel vp : modifiedFields) {
            StringRepresentableField field = vp.getUserData();
            String validateRes = field.validateStringValue(vp.getValue());
            if (validateRes != null) {
                messageBuilder.append(vp.getDescription()).append(": ").append(vp.getValue());
                messageBuilder.append(" [").append(validateRes).append("]\n");
            }
        }

        if (messageBuilder.length() != 0) {
            JOptionPane.showMessageDialog(this, "The following fields failed to validate:\n\n" +
                    messageBuilder.toString(), "Error", JOptionPane.ERROR_MESSAGE);
        } else {
            messageBuilder.append("The following modifications were made:\n\n");
            for (EditStringValuePanel vp : modifiedFields) {
                messageBuilder.append(vp.getDescription()).append(": \"").append(vp.getValue()).append("\"\n");
            }
            messageBuilder.append("\nCarry on with save?");

            JOptionPane.showConfirmDialog(this, messageBuilder.toString(), "Confirm save",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        }
    }

    private List<EditStringValuePanel> getModifiedFields() {
        LinkedList<EditStringValuePanel> tmpList = new LinkedList<EditStringValuePanel>();
        for (EditStringValuePanel field : fields) {
            if (field.isModified())
                tmpList.add(field);
        }

        for (ContainerPanel cp : subPanels)
            tmpList.addAll(cp.getModifiedFields());

        return tmpList;
    }

    private void addComponent(JComponent jc) {

        contentsPanel.add(jc);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel contentsPanel;
    private javax.swing.JLabel descriptionLabel;
    private javax.swing.JButton saveButton;
    // End of variables declaration//GEN-END:variables
}
