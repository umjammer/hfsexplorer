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

import org.catacombae.csjc.structelements.FlagField;


/**
 * @author <a href="https://catacombae.org" target="_top">Erik Larsson</a>
 */
public class FlagViewPanel extends javax.swing.JPanel {

    /** Creates new form FlagViewPanel */
    public FlagViewPanel(String label, FlagField data) {
        initComponents();

        if (false) {
            flagBox.addActionListener(e -> flagBox.setSelected(data.getValueAsBoolean()));
        } else
            flagBox.setEnabled(false);

        flagBox.setSelected(data.getValueAsBoolean());
        flagLabel.setText(label);
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

        flagBox = new javax.swing.JCheckBox();
        flagLabel = new javax.swing.JLabel();

        flagBox.setBorder(null);
        flagBox.setFocusable(false);

        flagLabel.setText("jLabel1");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(layout.createSequentialGroup()
                                .add(flagBox)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(flagLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 66, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                .add(flagBox)
                                .add(flagLabel))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox flagBox;
    private javax.swing.JLabel flagLabel;
    // End of variables declaration//GEN-END:variables

}
