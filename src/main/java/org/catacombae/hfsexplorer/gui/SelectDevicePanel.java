/*-
 * Copyright (C) 2006 Erik Larsson
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

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import static org.jdesktop.layout.GroupLayout.BASELINE;
import static org.jdesktop.layout.GroupLayout.DEFAULT_SIZE;
import static org.jdesktop.layout.GroupLayout.LEADING;
import static org.jdesktop.layout.GroupLayout.PREFERRED_SIZE;
import static org.jdesktop.layout.LayoutStyle.RELATED;


/**
 * @author <a href="https://catacombae.org" target="_top">Erik Larsson</a>
 */
public class SelectDevicePanel extends javax.swing.JPanel {

    /** Creates new form SelectWindowsDevicePanel */
    public SelectDevicePanel(String exampleDeviceName) {
        initComponents();
        selectSpecifyGroup.add(selectDeviceButton);
        selectSpecifyGroup.add(specifyDeviceNameButton);

        if (exampleDeviceName != null) {
            specifyDeviceNameButton.setText(specifyDeviceNameButton.getText() +
                    " (example: " + exampleDeviceName + ")");
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

        selectSpecifyGroup = new ButtonGroup();
        partitionGroup = new ButtonGroup();
        selectDeviceButton = new JRadioButton();
        specifyDeviceNameButton = new JRadioButton();
        specifyDeviceNameField = new JTextField();
        detectedDevicesCombo = new JComboBox<>();
        detectedDevicesLabel = new JLabel();
        loadButton = new JButton();
        cancelButton = new JButton();
        warningLabel = new JLabel();
        autodetectButton = new JButton();
        autodetectLabel = new JLabel();

        selectDeviceButton.setSelected(true);
        selectDeviceButton.setText("Select a device");
        selectDeviceButton.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        selectDeviceButton.setMargin(new java.awt.Insets(0, 0, 0, 0));

        specifyDeviceNameButton.setText("Specify device name");
        specifyDeviceNameButton.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        specifyDeviceNameButton.setMargin(new java.awt.Insets(0, 0, 0, 0));

        specifyDeviceNameField.setEnabled(false);

        detectedDevicesCombo.setModel(new DefaultComboBoxModel<>(new String[] {"Harddisk0\\Partition1", "Harddisk0\\Partition2", "Harddisk1\\Partition1", "CdRom0", "CdRom1"}));

        detectedDevicesLabel.setText("Detected devices:");

        loadButton.setText("Load");

        cancelButton.setText("Cancel");

        warningLabel.setText("(hybrid CD-ROMs with both HFS/+/X and ISO filesystems won't work)");

        autodetectButton.setText("Autodetect...");

        autodetectLabel.setText("Automatically detects HFS/HFS+/HFSX partitions on your system");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(LEADING)
                        .add(layout.createSequentialGroup()
                                .addContainerGap()
                                .add(layout.createParallelGroup(LEADING)
                                        .add(layout.createSequentialGroup()
                                                .add(autodetectButton)
                                                .addPreferredGap(RELATED)
                                                .add(autodetectLabel))
                                        .add(layout.createSequentialGroup()
                                                .add(detectedDevicesLabel)
                                                .addPreferredGap(RELATED)
                                                .add(detectedDevicesCombo, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
                                                .addPreferredGap(RELATED)
                                                .add(warningLabel))
                                        .add(selectDeviceButton)
                                        .add(specifyDeviceNameButton)
                                        .add(layout.createSequentialGroup()
                                                .add(loadButton)
                                                .addPreferredGap(RELATED)
                                                .add(cancelButton))
                                        .add(specifyDeviceNameField, DEFAULT_SIZE, 569, Short.MAX_VALUE))
                                .addContainerGap())
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(LEADING)
                        .add(layout.createSequentialGroup()
                                .addContainerGap()
                                .add(layout.createParallelGroup(BASELINE)
                                        .add(autodetectButton)
                                        .add(autodetectLabel))
                                .addPreferredGap(RELATED)
                                .add(selectDeviceButton)
                                .addPreferredGap(RELATED)
                                .add(layout.createParallelGroup(BASELINE)
                                        .add(detectedDevicesLabel)
                                        .add(detectedDevicesCombo, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
                                        .add(warningLabel))
                                .addPreferredGap(RELATED)
                                .add(specifyDeviceNameButton)
                                .addPreferredGap(RELATED)
                                .add(specifyDeviceNameField, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
                                .addPreferredGap(RELATED)
                                .add(layout.createParallelGroup(BASELINE)
                                        .add(loadButton)
                                        .add(cancelButton)))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    public JButton autodetectButton;
    private JLabel autodetectLabel;
    public JButton cancelButton;
    public JComboBox<String> detectedDevicesCombo;
    private JLabel detectedDevicesLabel;
    public JButton loadButton;
    private ButtonGroup partitionGroup;
    public JRadioButton selectDeviceButton;
    private ButtonGroup selectSpecifyGroup;
    public JRadioButton specifyDeviceNameButton;
    public JTextField specifyDeviceNameField;
    private JLabel warningLabel;
    // End of variables declaration//GEN-END:variables
}
