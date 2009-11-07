/*
Copyright 2008 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.visualization.options;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.gephi.visualization.api.VizConfig;
import org.openide.util.NbPreferences;

final class OpenGLPanel extends javax.swing.JPanel {

    private final OpenGLOptionsPanelController controller;

    //Settings
    private int antiAliasing = 0;

    OpenGLPanel(OpenGLOptionsPanelController controller) {
        this.controller = controller;
        initComponents();

        antialisaingCombobox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (antialisaingCombobox.getSelectedIndex() > 0) {
                    antiAliasing = (int) Math.pow(2, antialisaingCombobox.getSelectedIndex());
                }
            }
        });
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jXTitledSeparator1 = new org.jdesktop.swingx.JXTitledSeparator();
        labelAntialiasing = new javax.swing.JLabel();
        antialisaingCombobox = new javax.swing.JComboBox();
        jXTitledSeparator2 = new org.jdesktop.swingx.JXTitledSeparator();
        labelShow = new javax.swing.JLabel();
        fpsCheckbox = new javax.swing.JCheckBox();

        jXTitledSeparator1.setTitle(org.openide.util.NbBundle.getMessage(OpenGLPanel.class, "OpenGLPanel.jXTitledSeparator1.title")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(labelAntialiasing, org.openide.util.NbBundle.getMessage(OpenGLPanel.class, "OpenGLPanel.labelAntialiasing.text")); // NOI18N

        antialisaingCombobox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "0x", "2x", "4x", "8x", "16x" }));

        jXTitledSeparator2.setTitle(org.openide.util.NbBundle.getMessage(OpenGLPanel.class, "OpenGLPanel.jXTitledSeparator2.title")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(labelShow, org.openide.util.NbBundle.getMessage(OpenGLPanel.class, "OpenGLPanel.labelShow.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(fpsCheckbox, org.openide.util.NbBundle.getMessage(OpenGLPanel.class, "OpenGLPanel.fpsCheckbox.text")); // NOI18N
        fpsCheckbox.setMargin(new java.awt.Insets(2, 0, 2, 2));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(labelShow)
                        .addGap(38, 38, 38)
                        .addComponent(fpsCheckbox))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(labelAntialiasing)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(antialisaingCombobox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jXTitledSeparator1, javax.swing.GroupLayout.DEFAULT_SIZE, 369, Short.MAX_VALUE)
                    .addComponent(jXTitledSeparator2, javax.swing.GroupLayout.DEFAULT_SIZE, 369, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jXTitledSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelAntialiasing)
                    .addComponent(antialisaingCombobox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jXTitledSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(fpsCheckbox)
                    .addComponent(labelShow))
                .addContainerGap(135, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    void load() {
        antiAliasing = NbPreferences.forModule(VizConfig.class).getInt(VizConfig.ANTIALIASING, VizConfig.DEFAULT_ANTIALIASING);
        antialisaingCombobox.setSelectedIndex(antiAliasing == 0 ? 0 : Math.round((float) (Math.log(antiAliasing) / Math.log(2))));
        fpsCheckbox.setSelected(NbPreferences.forModule(VizConfig.class).getBoolean(VizConfig.SHOW_FPS, VizConfig.DEFAULT_SHOW_FPS));
    }

    void store() {
        NbPreferences.forModule(VizConfig.class).putInt(VizConfig.ANTIALIASING, antiAliasing);
        NbPreferences.forModule(VizConfig.class).putBoolean(VizConfig.SHOW_FPS, fpsCheckbox.isSelected());
    }

    boolean valid() {
        // TODO check whether form is consistent and complete
        return true;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox antialisaingCombobox;
    private javax.swing.JCheckBox fpsCheckbox;
    private org.jdesktop.swingx.JXTitledSeparator jXTitledSeparator1;
    private org.jdesktop.swingx.JXTitledSeparator jXTitledSeparator2;
    private javax.swing.JLabel labelAntialiasing;
    private javax.swing.JLabel labelShow;
    // End of variables declaration//GEN-END:variables
}