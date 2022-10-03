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

package org.catacombae.jparted.app;

import java.awt.event.ActionListener;
import java.util.Collection;
import javax.swing.table.DefaultTableModel;

/**
 * @author <a href="https://catacombae.org" target="_top">Erik Larsson</a>
 */
public class MainPanel extends javax.swing.JPanel {
    private class PartitionTableRow {
        public String number;
        public String type;
        public String name;
        public String start;
        public String end;

        public PartitionTableRow(String number, String type,
                String name, String start, String end) {
            this.number = number;
            this.type = type;
            this.name = name;
            this.start = start;
            this.end = end;
        }
    }
    private class PartitionTableModel extends DefaultTableModel {
        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }
    }

    private final PartitionTableModel partitionTableModel;

    /** Creates new form MainPanel */
    public MainPanel() {
        initComponents();

        partitionTableModel = new PartitionTableModel();
        partitionTableModel.setColumnIdentifiers(new String[] {"Number", "Type", "Name", "Start", "End"});
        partitionTable.setModel(partitionTableModel);

    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        partitionSystemsLabel = new javax.swing.JLabel();
        partitionSystemsBox = new javax.swing.JComboBox();
        synchronizeButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        partitionTable = new javax.swing.JTable();

        partitionSystemsLabel.setText("Detected partition systems:");

        partitionSystemsBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "GUID Partition Table (6 partitions)", "Protective MBR (4 partitions)" }));

        synchronizeButton.setText("Synchronize");

        partitionTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "Number", "Type", "Name", "Start", "End"
            }
        ));
        jScrollPane1.setViewportView(partitionTable);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(partitionSystemsLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(partitionSystemsBox, 0, 339, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(synchronizeButton)
                .addContainerGap())
            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(layout.createSequentialGroup()
                    .addContainerGap()
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 645, Short.MAX_VALUE)
                    .addContainerGap()))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(synchronizeButton)
                    .add(partitionSystemsBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(partitionSystemsLabel))
                .addContainerGap(277, Short.MAX_VALUE))
            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                    .add(43, 43, 43)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 263, Short.MAX_VALUE)
                    .addContainerGap()))
        );
    }// </editor-fold>//GEN-END:initComponents

    public void setPartitionSystemsBoxContents(Collection<String> contents) {
        partitionSystemsBox.removeAllItems();
        for(String currentItem : contents)
            partitionSystemsBox.addItem(currentItem);
    }

    public void setPartitionSystemsBoxEnabled(boolean enabled) {
        partitionSystemsBox.setEnabled(enabled);
    }

    public void setSynchronizeButtonEnabled(boolean enabled) {
        synchronizeButton.setEnabled(enabled);
    }

    public void setPartitionSystemsBoxListener(ActionListener listener) {
        for(ActionListener al : partitionSystemsBox.getActionListeners())
            partitionSystemsBox.removeActionListener(al);
        partitionSystemsBox.addActionListener(listener);
    }

    public void setSynchronizeButtonListener(ActionListener listener) {
        for(ActionListener al : synchronizeButton.getActionListeners())
            synchronizeButton.removeActionListener(al);
        synchronizeButton.addActionListener(listener);
    }

    public void clearPartitionList() {
        int rowCount = partitionTableModel.getRowCount();
        for(int i = rowCount-1; i >= 0; --i)
            partitionTableModel.removeRow(i);
    }

    public void addPartition(String number, String type, String name,
            String start, String end) {
        partitionTableModel.addRow(new String[] { number, type, name, start, end });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JComboBox partitionSystemsBox;
    private javax.swing.JLabel partitionSystemsLabel;
    private javax.swing.JTable partitionTable;
    private javax.swing.JButton synchronizeButton;
    // End of variables declaration//GEN-END:variables

}
