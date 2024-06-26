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

package org.catacombae.hfsexplorer.gui;

import org.catacombae.hfs.types.hfsplus.JournalInfoBlock;
import org.catacombae.util.Util;


/**
 * @author <a href="https://catacombae.org" target="_top">Erik Larsson</a>
 */
public class JournalInfoBlockPanel extends javax.swing.JPanel {

    /** Creates new form JournalInfoBlockPanel */
    public JournalInfoBlockPanel() {
        initComponents();
    }

    public void setFields(JournalInfoBlock jib) {
        journalInFSBox.setSelected(jib.getFlagJournalInFS());
        journalOnOtherDeviceBox.setSelected(jib.getFlagJournalOnOtherDevice());
        journalNeedInitBox.setSelected(jib.getFlagJournalNeedInit());

        int[] deviceSignature = jib.getDeviceSignature();
        deviceSignatureField0.setText(Util.toHexStringBE(deviceSignature[0]) +
                " " + Util.toHexStringBE(deviceSignature[1]));
        deviceSignatureField1.setText(Util.toHexStringBE(deviceSignature[2]) +
                " " + Util.toHexStringBE(deviceSignature[3]));
        deviceSignatureField2.setText(Util.toHexStringBE(deviceSignature[4]) +
                " " + Util.toHexStringBE(deviceSignature[5]));
        deviceSignatureField3.setText(Util.toHexStringBE(deviceSignature[6]) +
                " " + Util.toHexStringBE(deviceSignature[7]));

        journalOffsetField.setText(jib.getOffset() + " bytes");
        journalSizeField.setText(jib.getSize() + " bytes");
        int[] reserved = jib.getReserved();
        reservedField0.setText(Util.toHexStringBE(reserved[0]) + " " +
                Util.toHexStringBE(reserved[1]));
        reservedField1.setText(Util.toHexStringBE(reserved[2]) + " " +
                Util.toHexStringBE(reserved[3]));
        reservedField2.setText(Util.toHexStringBE(reserved[4]) + " " +
                Util.toHexStringBE(reserved[5]));
        reservedField3.setText(Util.toHexStringBE(reserved[6]) + " " +
                Util.toHexStringBE(reserved[7]));
        reservedField4.setText(Util.toHexStringBE(reserved[8]) + " " +
                Util.toHexStringBE(reserved[9]));
        reservedField5.setText(Util.toHexStringBE(reserved[10]) + " " +
                Util.toHexStringBE(reserved[11]));
        reservedField6.setText(Util.toHexStringBE(reserved[12]) + " " +
                Util.toHexStringBE(reserved[13]));
        reservedField7.setText(Util.toHexStringBE(reserved[14]) + " " +
                Util.toHexStringBE(reserved[15]));
        reservedField8.setText(Util.toHexStringBE(reserved[16]) + " " +
                Util.toHexStringBE(reserved[17]));
        reservedField9.setText(Util.toHexStringBE(reserved[18]) + " " +
                Util.toHexStringBE(reserved[19]));
        reservedField10.setText(Util.toHexStringBE(reserved[20]) + " " +
                Util.toHexStringBE(reserved[21]));
        reservedField11.setText(Util.toHexStringBE(reserved[22]) + " " +
                Util.toHexStringBE(reserved[23]));
        reservedField12.setText(Util.toHexStringBE(reserved[24]) + " " +
                Util.toHexStringBE(reserved[25]));
        reservedField13.setText(Util.toHexStringBE(reserved[26]) + " " +
                Util.toHexStringBE(reserved[27]));
        reservedField14.setText(Util.toHexStringBE(reserved[28]) + " " +
                Util.toHexStringBE(reserved[29]));
        reservedField15.setText(Util.toHexStringBE(reserved[30]) + " " +
                Util.toHexStringBE(reserved[31]));
    }

    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        flagsLabel = new javax.swing.JLabel();
        journalInFSBox = new javax.swing.JCheckBox();
        journalOnOtherDeviceBox = new javax.swing.JCheckBox();
        journalNeedInitBox = new javax.swing.JCheckBox();
        journalInFSLabel = new javax.swing.JLabel();
        journalOnOtherDeviceLabel = new javax.swing.JLabel();
        journalNeedInitLabel = new javax.swing.JLabel();
        deviceSignatureLabel = new javax.swing.JLabel();
        journalOffsetLabel = new javax.swing.JLabel();
        journalSizeLabel = new javax.swing.JLabel();
        reservedLabel = new javax.swing.JLabel();
        deviceSignatureField0 = new javax.swing.JLabel();
        journalOffsetField = new javax.swing.JLabel();
        journalSizeField = new javax.swing.JLabel();
        reservedField0 = new javax.swing.JLabel();
        deviceSignatureField1 = new javax.swing.JLabel();
        deviceSignatureField2 = new javax.swing.JLabel();
        deviceSignatureField3 = new javax.swing.JLabel();
        reservedField1 = new javax.swing.JLabel();
        reservedField2 = new javax.swing.JLabel();
        reservedField3 = new javax.swing.JLabel();
        reservedField4 = new javax.swing.JLabel();
        reservedField5 = new javax.swing.JLabel();
        reservedField6 = new javax.swing.JLabel();
        reservedField7 = new javax.swing.JLabel();
        reservedField8 = new javax.swing.JLabel();
        reservedField9 = new javax.swing.JLabel();
        reservedField10 = new javax.swing.JLabel();
        reservedField11 = new javax.swing.JLabel();
        reservedField12 = new javax.swing.JLabel();
        reservedField13 = new javax.swing.JLabel();
        reservedField14 = new javax.swing.JLabel();
        reservedField15 = new javax.swing.JLabel();

        flagsLabel.setText("Flags:");

        journalInFSBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        journalInFSBox.setEnabled(false);
        journalInFSBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        journalOnOtherDeviceBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        journalOnOtherDeviceBox.setEnabled(false);
        journalOnOtherDeviceBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        journalNeedInitBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        journalNeedInitBox.setEnabled(false);
        journalNeedInitBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        journalInFSLabel.setText("Journal is located in file system");

        journalOnOtherDeviceLabel.setText("Journal is located on other device");

        journalNeedInitLabel.setText("Journal needs to be initialized");

        deviceSignatureLabel.setText("Device signature:");

        journalOffsetLabel.setText("Journal offset:");

        journalSizeLabel.setText("Journal size:");

        reservedLabel.setText("Reserved:");

        deviceSignatureField0.setText("ÖÖ ÖÖ ÖÖ ÖÖ ÖÖ ÖÖ ÖÖ ÖÖ");

        journalOffsetField.setText("0 bytes");

        journalSizeField.setText("0 bytes");

        reservedField0.setText("ÖÖ ÖÖ ÖÖ ÖÖ ÖÖ ÖÖ ÖÖ ÖÖ");

        deviceSignatureField1.setText("ÖÖ ÖÖ ÖÖ ÖÖ ÖÖ ÖÖ ÖÖ ÖÖ");

        deviceSignatureField2.setText("ÖÖ ÖÖ ÖÖ ÖÖ ÖÖ ÖÖ ÖÖ ÖÖ");

        deviceSignatureField3.setText("ÖÖ ÖÖ ÖÖ ÖÖ ÖÖ ÖÖ ÖÖ ÖÖ");

        reservedField1.setText("ÖÖ ÖÖ ÖÖ ÖÖ ÖÖ ÖÖ ÖÖ ÖÖ");

        reservedField2.setText("ÖÖ ÖÖ ÖÖ ÖÖ ÖÖ ÖÖ ÖÖ ÖÖ");

        reservedField3.setText("ÖÖ ÖÖ ÖÖ ÖÖ ÖÖ ÖÖ ÖÖ ÖÖ");

        reservedField4.setText("ÖÖ ÖÖ ÖÖ ÖÖ ÖÖ ÖÖ ÖÖ ÖÖ");

        reservedField5.setText("ÖÖ ÖÖ ÖÖ ÖÖ ÖÖ ÖÖ ÖÖ ÖÖ");

        reservedField6.setText("ÖÖ ÖÖ ÖÖ ÖÖ ÖÖ ÖÖ ÖÖ ÖÖ");

        reservedField7.setText("ÖÖ ÖÖ ÖÖ ÖÖ ÖÖ ÖÖ ÖÖ ÖÖ");

        reservedField8.setText("ÖÖ ÖÖ ÖÖ ÖÖ ÖÖ ÖÖ ÖÖ ÖÖ");

        reservedField9.setText("ÖÖ ÖÖ ÖÖ ÖÖ ÖÖ ÖÖ ÖÖ ÖÖ");

        reservedField10.setText("ÖÖ ÖÖ ÖÖ ÖÖ ÖÖ ÖÖ ÖÖ ÖÖ");

        reservedField11.setText("ÖÖ ÖÖ ÖÖ ÖÖ ÖÖ ÖÖ ÖÖ ÖÖ");

        reservedField12.setText("ÖÖ ÖÖ ÖÖ ÖÖ ÖÖ ÖÖ ÖÖ ÖÖ");

        reservedField13.setText("ÖÖ ÖÖ ÖÖ ÖÖ ÖÖ ÖÖ ÖÖ ÖÖ");

        reservedField14.setText("ÖÖ ÖÖ ÖÖ ÖÖ ÖÖ ÖÖ ÖÖ ÖÖ");

        reservedField15.setText("ÖÖ ÖÖ ÖÖ ÖÖ ÖÖ ÖÖ ÖÖ ÖÖ");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(layout.createSequentialGroup()
                                .addContainerGap()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                        .add(flagsLabel)
                                        .add(layout.createSequentialGroup()
                                                .add(21, 21, 21)
                                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                                        .add(layout.createSequentialGroup()
                                                                .add(journalOnOtherDeviceBox)
                                                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                                                .add(journalOnOtherDeviceLabel))
                                                        .add(layout.createSequentialGroup()
                                                                .add(journalInFSBox)
                                                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                                                .add(journalInFSLabel))
                                                        .add(layout.createSequentialGroup()
                                                                .add(journalNeedInitBox)
                                                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                                                .add(journalNeedInitLabel))))
                                        .add(layout.createSequentialGroup()
                                                .add(journalOffsetLabel)
                                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                                .add(journalOffsetField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 308, Short.MAX_VALUE))
                                        .add(layout.createSequentialGroup()
                                                .add(journalSizeLabel)
                                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                                .add(journalSizeField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 320, Short.MAX_VALUE))
                                        .add(layout.createSequentialGroup()
                                                .add(deviceSignatureLabel)
                                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                                        .add(deviceSignatureField3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 288, Short.MAX_VALUE)
                                                        .add(deviceSignatureField0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 288, Short.MAX_VALUE)
                                                        .add(deviceSignatureField1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 288, Short.MAX_VALUE)
                                                        .add(deviceSignatureField2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 288, Short.MAX_VALUE)))
                                        .add(layout.createSequentialGroup()
                                                .add(reservedLabel)
                                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                                        .add(reservedField0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 337, Short.MAX_VALUE)
                                                        .add(reservedField1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 337, Short.MAX_VALUE)
                                                        .add(reservedField2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 337, Short.MAX_VALUE)
                                                        .add(reservedField3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 337, Short.MAX_VALUE)
                                                        .add(reservedField4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 337, Short.MAX_VALUE)
                                                        .add(reservedField5, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 337, Short.MAX_VALUE)
                                                        .add(reservedField6, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 337, Short.MAX_VALUE)
                                                        .add(reservedField7, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 337, Short.MAX_VALUE)
                                                        .add(reservedField8, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 337, Short.MAX_VALUE)
                                                        .add(reservedField9, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 337, Short.MAX_VALUE)
                                                        .add(reservedField10, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 337, Short.MAX_VALUE)
                                                        .add(reservedField11, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 337, Short.MAX_VALUE)
                                                        .add(reservedField12, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 337, Short.MAX_VALUE)
                                                        .add(reservedField13, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 337, Short.MAX_VALUE)
                                                        .add(reservedField14, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 337, Short.MAX_VALUE)
                                                        .add(reservedField15, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 337, Short.MAX_VALUE))))
                                .addContainerGap())
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(layout.createSequentialGroup()
                                .addContainerGap()
                                .add(flagsLabel)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                        .add(journalInFSBox)
                                        .add(journalInFSLabel))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                        .add(journalOnOtherDeviceBox)
                                        .add(journalOnOtherDeviceLabel))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                        .add(journalNeedInitBox)
                                        .add(journalNeedInitLabel))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                        .add(deviceSignatureLabel)
                                        .add(deviceSignatureField0))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(deviceSignatureField1)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(deviceSignatureField2)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(deviceSignatureField3)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                        .add(journalOffsetLabel)
                                        .add(journalOffsetField))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                        .add(journalSizeLabel)
                                        .add(journalSizeField))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                        .add(reservedLabel)
                                        .add(reservedField0))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(reservedField1)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(reservedField2)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(reservedField3)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(reservedField4)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(reservedField5)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(reservedField6)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(reservedField7)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(reservedField8)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(reservedField9)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(reservedField10)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(reservedField11)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(reservedField12)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(reservedField13)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(reservedField14)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(reservedField15)
                                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel deviceSignatureField0;
    private javax.swing.JLabel deviceSignatureField1;
    private javax.swing.JLabel deviceSignatureField2;
    private javax.swing.JLabel deviceSignatureField3;
    private javax.swing.JLabel deviceSignatureLabel;
    private javax.swing.JLabel flagsLabel;
    private javax.swing.JCheckBox journalInFSBox;
    private javax.swing.JLabel journalInFSLabel;
    private javax.swing.JCheckBox journalNeedInitBox;
    private javax.swing.JLabel journalNeedInitLabel;
    private javax.swing.JLabel journalOffsetField;
    private javax.swing.JLabel journalOffsetLabel;
    private javax.swing.JCheckBox journalOnOtherDeviceBox;
    private javax.swing.JLabel journalOnOtherDeviceLabel;
    private javax.swing.JLabel journalSizeField;
    private javax.swing.JLabel journalSizeLabel;
    private javax.swing.JLabel reservedField0;
    private javax.swing.JLabel reservedField1;
    private javax.swing.JLabel reservedField10;
    private javax.swing.JLabel reservedField11;
    private javax.swing.JLabel reservedField12;
    private javax.swing.JLabel reservedField13;
    private javax.swing.JLabel reservedField14;
    private javax.swing.JLabel reservedField15;
    private javax.swing.JLabel reservedField2;
    private javax.swing.JLabel reservedField3;
    private javax.swing.JLabel reservedField4;
    private javax.swing.JLabel reservedField5;
    private javax.swing.JLabel reservedField6;
    private javax.swing.JLabel reservedField7;
    private javax.swing.JLabel reservedField8;
    private javax.swing.JLabel reservedField9;
    private javax.swing.JLabel reservedLabel;
    // End of variables declaration//GEN-END:variables

}
