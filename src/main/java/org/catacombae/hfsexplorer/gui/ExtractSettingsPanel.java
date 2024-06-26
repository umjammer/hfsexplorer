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
import java.lang.System.Logger.Level;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingUtilities;

import org.catacombae.hfsexplorer.ExtractProgressMonitor.CreateDirectoryFailedAction;
import org.catacombae.hfsexplorer.ExtractProgressMonitor.CreateFileFailedAction;
import org.catacombae.hfsexplorer.ExtractProgressMonitor.DirectoryExistsAction;
import org.catacombae.hfsexplorer.ExtractProgressMonitor.ExtractProperties;
import org.catacombae.hfsexplorer.ExtractProgressMonitor.FileExistsAction;
import org.catacombae.hfsexplorer.ExtractProgressMonitor.UnhandledExceptionAction;


/**
 * @author <a href="https://catacombae.org" target="_top">Erik Larsson</a>
 */
public class ExtractSettingsPanel extends javax.swing.JPanel {

    private final ButtonGroup createDirButtonGroup = new ButtonGroup();
    private final ButtonGroup createFileButtonGroup = new ButtonGroup();
    private final ButtonGroup dirExistsButtonGroup = new ButtonGroup();
    private final ButtonGroup fileExistsButtonGroup = new ButtonGroup();

    public ExtractSettingsPanel(ExtractProperties p) {
        this();

        p.addListener(changedProperty -> {
//            logger.log(Level.DEBUG, "Received a propertyChanged for " + changedProperty);
            AbstractButton theButton;
            if (changedProperty instanceof CreateDirectoryFailedAction) {
                theButton = switch ((CreateDirectoryFailedAction) changedProperty) {
                    case PROMPT_USER -> createDirPromptUserButton;
                    case AUTO_RENAME -> createDirAutoRenameButton;
                    case SKIP_DIRECTORY -> createDirSkipDirectoryButton;
                    case CANCEL -> createDirCancelButton;
                    default -> throw new RuntimeException("Unknown property: " + changedProperty);
                };
            } else if (changedProperty instanceof CreateFileFailedAction) {
                theButton = switch ((CreateFileFailedAction) changedProperty) {
                    case PROMPT_USER -> createFilePromptUserButton;
                    case SKIP_FILE -> createFileSkipFileButton;
                    case AUTO_RENAME -> createFileAutoRenameButton;
                    case SKIP_DIRECTORY -> createFileSkipDirectoryButton;
                    case CANCEL -> createFileCancelButton;
                    default -> throw new RuntimeException("Unknown property: " + changedProperty);
                };
            } else if (changedProperty instanceof DirectoryExistsAction) {
                theButton = switch ((DirectoryExistsAction) changedProperty) {
                    case PROMPT_USER -> dirExistsPromptUserButton;
                    case CONTINUE -> dirExistsContinueButton;
                    case AUTO_RENAME -> dirExistsAutoRenameButton;
                    case SKIP_DIRECTORY -> dirExistsSkipDirectoryButton;
                    case CANCEL -> dirExistsCancelButton;
                    default -> throw new RuntimeException("Unknown property: " + changedProperty);
                };
            } else if (changedProperty instanceof FileExistsAction) {
                theButton = switch ((FileExistsAction) changedProperty) {
                    case PROMPT_USER -> fileExistsPromptUserButton;
                    case OVERWRITE -> fileExistsOverwriteButton;
                    case AUTO_RENAME -> fileExistsAutoRenameButton;
                    case SKIP_FILE -> fileExistsSkipFileButton;
                    case SKIP_DIRECTORY -> fileExistsSkipDirectoryButton;
                    case CANCEL -> fileExistsCancelButton;
                    default -> throw new RuntimeException("Unknown property: " + changedProperty);
                };
            } else if (changedProperty instanceof UnhandledExceptionAction) {
                // Ignore for now, until we have implemented this in the
                // GUI.
                theButton = null;
            } else
                throw new RuntimeException("Unknown property: " +
                        (changedProperty != null ? changedProperty.getClass() : "null"));

            if (theButton != null) {
                SwingUtilities.invokeLater(() -> theButton.setSelected(true));
            }
        });

        createDirPromptUserButton.doClick();
        createFilePromptUserButton.doClick();
        dirExistsPromptUserButton.doClick();
        fileExistsPromptUserButton.doClick();

        createDirPromptUserButton.addActionListener(new CreateDirListener(createDirPromptUserButton,
                p, CreateDirectoryFailedAction.PROMPT_USER));
        createDirSkipDirectoryButton.addActionListener(new CreateDirListener(createDirSkipDirectoryButton,
                p, CreateDirectoryFailedAction.SKIP_DIRECTORY));
        createDirAutoRenameButton.addActionListener(new CreateDirListener(createDirAutoRenameButton,
                p, CreateDirectoryFailedAction.AUTO_RENAME));
        createDirCancelButton.addActionListener(new CreateDirListener(createDirCancelButton,
                p, CreateDirectoryFailedAction.CANCEL));

        createFilePromptUserButton.addActionListener(new CreateFileListener(createFilePromptUserButton,
                p, CreateFileFailedAction.PROMPT_USER));
        createFileSkipFileButton.addActionListener(new CreateFileListener(createFileSkipFileButton,
                p, CreateFileFailedAction.SKIP_FILE));
        createFileSkipDirectoryButton.addActionListener(new CreateFileListener(createFileSkipDirectoryButton,
                p, CreateFileFailedAction.SKIP_DIRECTORY));
        createFileAutoRenameButton.addActionListener(new CreateFileListener(createFileAutoRenameButton,
                p, CreateFileFailedAction.AUTO_RENAME));
        createFileCancelButton.addActionListener(new CreateFileListener(createFileCancelButton,
                p, CreateFileFailedAction.CANCEL));

        dirExistsPromptUserButton.addActionListener(new DirExistsListener(dirExistsPromptUserButton,
                p, DirectoryExistsAction.PROMPT_USER));
        dirExistsContinueButton.addActionListener(new DirExistsListener(dirExistsContinueButton,
                p, DirectoryExistsAction.CONTINUE));
        dirExistsSkipDirectoryButton.addActionListener(new DirExistsListener(dirExistsSkipDirectoryButton,
                p, DirectoryExistsAction.SKIP_DIRECTORY));
        dirExistsAutoRenameButton.addActionListener(new DirExistsListener(dirExistsAutoRenameButton,
                p, DirectoryExistsAction.AUTO_RENAME));
        dirExistsCancelButton.addActionListener(new DirExistsListener(dirExistsCancelButton,
                p, DirectoryExistsAction.CANCEL));

        fileExistsPromptUserButton.addActionListener(new FileExistsListener(fileExistsPromptUserButton,
                p, FileExistsAction.PROMPT_USER));
        fileExistsSkipFileButton.addActionListener(new FileExistsListener(fileExistsSkipFileButton,
                p, FileExistsAction.SKIP_FILE));
        fileExistsSkipDirectoryButton.addActionListener(new FileExistsListener(fileExistsSkipDirectoryButton,
                p, FileExistsAction.SKIP_DIRECTORY));
        fileExistsOverwriteButton.addActionListener(new FileExistsListener(fileExistsOverwriteButton,
                p, FileExistsAction.OVERWRITE));
        fileExistsAutoRenameButton.addActionListener(new FileExistsListener(fileExistsAutoRenameButton,
                p, FileExistsAction.AUTO_RENAME));
        fileExistsCancelButton.addActionListener(new FileExistsListener(fileExistsCancelButton,
                p, FileExistsAction.CANCEL));
    }

    /** Creates new form ExtractSettingsPanel */
    private ExtractSettingsPanel() {
        initComponents();

        createDirButtonGroup.add(createDirPromptUserButton);
        createDirButtonGroup.add(createDirSkipDirectoryButton);
        createDirButtonGroup.add(createDirAutoRenameButton);
        createDirButtonGroup.add(createDirCancelButton);

        createFileButtonGroup.add(createFilePromptUserButton);
        createFileButtonGroup.add(createFileSkipFileButton);
        createFileButtonGroup.add(createFileSkipDirectoryButton);
        createFileButtonGroup.add(createFileAutoRenameButton);
        createFileButtonGroup.add(createFileCancelButton);

        dirExistsButtonGroup.add(dirExistsPromptUserButton);
        dirExistsButtonGroup.add(dirExistsContinueButton);
        dirExistsButtonGroup.add(dirExistsSkipDirectoryButton);
        dirExistsButtonGroup.add(dirExistsAutoRenameButton);
        dirExistsButtonGroup.add(dirExistsCancelButton);

        fileExistsButtonGroup.add(fileExistsPromptUserButton);
        fileExistsButtonGroup.add(fileExistsSkipFileButton);
        fileExistsButtonGroup.add(fileExistsSkipDirectoryButton);
        fileExistsButtonGroup.add(fileExistsOverwriteButton);
        fileExistsButtonGroup.add(fileExistsAutoRenameButton);
        fileExistsButtonGroup.add(fileExistsCancelButton);

        quietModeBox.addActionListener(e -> {
            boolean selected = quietModeBox.isSelected();

            if (selected) {
                createDirSkipDirectoryButton.doClick();
                createFileSkipFileButton.doClick();
                dirExistsSkipDirectoryButton.doClick();
                fileExistsSkipFileButton.doClick();
            }

            List<ButtonGroup> buttonGroups = Arrays.asList(createDirButtonGroup,
                    createFileButtonGroup, dirExistsButtonGroup, fileExistsButtonGroup);
            for (ButtonGroup bg : buttonGroups) {
                Enumeration<AbstractButton> buttonEnum = bg.getElements();
                while (buttonEnum.hasMoreElements()) {
                    AbstractButton b = buttonEnum.nextElement();
                    b.setEnabled(!selected);
                }
            }
        });
    }

    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        quietModeBox = new javax.swing.JCheckBox();
        createDirPanel = new javax.swing.JPanel();
        createDirLabel = new javax.swing.JLabel();
        createDirPromptUserButton = new javax.swing.JRadioButton();
        createDirSkipDirectoryButton = new javax.swing.JRadioButton();
        createDirAutoRenameButton = new javax.swing.JRadioButton();
        createDirCancelButton = new javax.swing.JRadioButton();
        createFilePanel = new javax.swing.JPanel();
        createFileLabel = new javax.swing.JLabel();
        createFilePromptUserButton = new javax.swing.JRadioButton();
        createFileSkipFileButton = new javax.swing.JRadioButton();
        createFileSkipDirectoryButton = new javax.swing.JRadioButton();
        createFileAutoRenameButton = new javax.swing.JRadioButton();
        createFileCancelButton = new javax.swing.JRadioButton();
        dirExistsPanel = new javax.swing.JPanel();
        dirExistsLabel = new javax.swing.JLabel();
        dirExistsPromptUserButton = new javax.swing.JRadioButton();
        dirExistsContinueButton = new javax.swing.JRadioButton();
        dirExistsSkipDirectoryButton = new javax.swing.JRadioButton();
        dirExistsAutoRenameButton = new javax.swing.JRadioButton();
        dirExistsCancelButton = new javax.swing.JRadioButton();
        fileExistsPanel = new javax.swing.JPanel();
        fileExistsLabel = new javax.swing.JLabel();
        fileExistsPromptUserButton = new javax.swing.JRadioButton();
        fileExistsSkipFileButton = new javax.swing.JRadioButton();
        fileExistsSkipDirectoryButton = new javax.swing.JRadioButton();
        fileExistsOverwriteButton = new javax.swing.JRadioButton();
        fileExistsAutoRenameButton = new javax.swing.JRadioButton();
        fileExistsCancelButton = new javax.swing.JRadioButton();

        quietModeBox.setText("Quiet mode");
        quietModeBox.setToolTipText("A non-destructive \"No questions asked\" mode");

        createDirLabel.setText("Create directory failed:");

        createDirPromptUserButton.setText("Prompt user");

        createDirSkipDirectoryButton.setText("Skip directory");

        createDirAutoRenameButton.setText("Auto-rename");

        createDirCancelButton.setText("Cancel");

        org.jdesktop.layout.GroupLayout createDirPanelLayout = new org.jdesktop.layout.GroupLayout(createDirPanel);
        createDirPanel.setLayout(createDirPanelLayout);
        createDirPanelLayout.setHorizontalGroup(
                createDirPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(createDirLabel)
                        .add(createDirPanelLayout.createSequentialGroup()
                                .add(10, 10, 10)
                                .add(createDirPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                        .add(createDirPromptUserButton)
                                        .add(createDirSkipDirectoryButton)
                                        .add(createDirAutoRenameButton)
                                        .add(createDirCancelButton)))
        );
        createDirPanelLayout.setVerticalGroup(
                createDirPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(createDirPanelLayout.createSequentialGroup()
                                .add(createDirLabel)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(createDirPromptUserButton)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(createDirSkipDirectoryButton)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(createDirAutoRenameButton)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(createDirCancelButton)
                                .addContainerGap(23, Short.MAX_VALUE))
        );

        createFileLabel.setText("Create file failed:");

        createFilePromptUserButton.setText("Prompt user");

        createFileSkipFileButton.setText("Skip file");

        createFileSkipDirectoryButton.setText("Skip rest of directory");

        createFileAutoRenameButton.setText("Auto-rename");

        createFileCancelButton.setText("Cancel");

        org.jdesktop.layout.GroupLayout createFilePanelLayout = new org.jdesktop.layout.GroupLayout(createFilePanel);
        createFilePanel.setLayout(createFilePanelLayout);
        createFilePanelLayout.setHorizontalGroup(
                createFilePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(createFilePanelLayout.createSequentialGroup()
                                .add(createFilePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                        .add(createFileLabel)
                                        .add(createFilePanelLayout.createSequentialGroup()
                                                .add(10, 10, 10)
                                                .add(createFilePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                                        .add(createFileSkipFileButton)
                                                        .add(createFilePromptUserButton)
                                                        .add(createFileSkipDirectoryButton)
                                                        .add(createFileAutoRenameButton)
                                                        .add(createFileCancelButton))))
                                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        createFilePanelLayout.setVerticalGroup(
                createFilePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(createFilePanelLayout.createSequentialGroup()
                                .add(createFileLabel)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(createFilePromptUserButton)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(createFileSkipFileButton)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(createFileSkipDirectoryButton)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(createFileAutoRenameButton)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(createFileCancelButton))
        );

        dirExistsLabel.setText("Directory already exists:");

        dirExistsPromptUserButton.setText("Prompt user");

        dirExistsContinueButton.setText("Continue");

        dirExistsSkipDirectoryButton.setText("Skip directory");

        dirExistsAutoRenameButton.setText("Auto-rename");

        dirExistsCancelButton.setText("Cancel");

        org.jdesktop.layout.GroupLayout dirExistsPanelLayout = new org.jdesktop.layout.GroupLayout(dirExistsPanel);
        dirExistsPanel.setLayout(dirExistsPanelLayout);
        dirExistsPanelLayout.setHorizontalGroup(
                dirExistsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(dirExistsLabel)
                        .add(dirExistsPanelLayout.createSequentialGroup()
                                .add(10, 10, 10)
                                .add(dirExistsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                        .add(dirExistsContinueButton)
                                        .add(dirExistsPromptUserButton)
                                        .add(dirExistsSkipDirectoryButton)
                                        .add(dirExistsAutoRenameButton)
                                        .add(dirExistsCancelButton)))
        );
        dirExistsPanelLayout.setVerticalGroup(
                dirExistsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(dirExistsPanelLayout.createSequentialGroup()
                                .add(dirExistsLabel)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(dirExistsPromptUserButton)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(dirExistsContinueButton)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(dirExistsSkipDirectoryButton)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(dirExistsAutoRenameButton)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(dirExistsCancelButton))
        );

        fileExistsLabel.setText("File already exists:");

        fileExistsPromptUserButton.setText("Prompt user");

        fileExistsSkipFileButton.setText("Skip file");

        fileExistsSkipDirectoryButton.setText("Skip rest of directory");

        fileExistsOverwriteButton.setText("Overwrite existing file");

        fileExistsAutoRenameButton.setText("Auto-rename");

        fileExistsCancelButton.setText("Cancel");

        org.jdesktop.layout.GroupLayout fileExistsPanelLayout = new org.jdesktop.layout.GroupLayout(fileExistsPanel);
        fileExistsPanel.setLayout(fileExistsPanelLayout);
        fileExistsPanelLayout.setHorizontalGroup(
                fileExistsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(fileExistsPanelLayout.createSequentialGroup()
                                .add(fileExistsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                        .add(fileExistsLabel)
                                        .add(fileExistsPanelLayout.createSequentialGroup()
                                                .add(10, 10, 10)
                                                .add(fileExistsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                                        .add(fileExistsCancelButton)
                                                        .add(fileExistsAutoRenameButton)
                                                        .add(fileExistsSkipFileButton)
                                                        .add(fileExistsPromptUserButton)
                                                        .add(fileExistsSkipDirectoryButton)
                                                        .add(fileExistsOverwriteButton))))
                                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        fileExistsPanelLayout.setVerticalGroup(
                fileExistsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(fileExistsPanelLayout.createSequentialGroup()
                                .add(fileExistsLabel)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(fileExistsPromptUserButton)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(fileExistsSkipFileButton)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(fileExistsSkipDirectoryButton)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(fileExistsOverwriteButton)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(fileExistsAutoRenameButton)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(fileExistsCancelButton))
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                                .addContainerGap()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                        .add(layout.createSequentialGroup()
                                                .add(21, 21, 21)
                                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                                        .add(dirExistsPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                                        .add(createDirPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                                .add(18, 18, 18)
                                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                                        .add(fileExistsPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                                        .add(createFilePanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                                        .add(quietModeBox))
                                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(layout.createSequentialGroup()
                                .addContainerGap()
                                .add(quietModeBox)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                        .add(createDirPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .add(createFilePanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                        .add(fileExistsPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .add(dirExistsPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JRadioButton createDirAutoRenameButton;
    private JRadioButton createDirCancelButton;
    private JLabel createDirLabel;
    private JPanel createDirPanel;
    private JRadioButton createDirPromptUserButton;
    private JRadioButton createDirSkipDirectoryButton;
    private JRadioButton createFileAutoRenameButton;
    private JRadioButton createFileCancelButton;
    private JLabel createFileLabel;
    private JPanel createFilePanel;
    private JRadioButton createFilePromptUserButton;
    private JRadioButton createFileSkipDirectoryButton;
    private JRadioButton createFileSkipFileButton;
    private JRadioButton dirExistsAutoRenameButton;
    private JRadioButton dirExistsCancelButton;
    private JRadioButton dirExistsContinueButton;
    private JLabel dirExistsLabel;
    private JPanel dirExistsPanel;
    private JRadioButton dirExistsPromptUserButton;
    private JRadioButton dirExistsSkipDirectoryButton;
    private JRadioButton fileExistsAutoRenameButton;
    private JRadioButton fileExistsCancelButton;
    private JLabel fileExistsLabel;
    private JRadioButton fileExistsOverwriteButton;
    private JPanel fileExistsPanel;
    private JRadioButton fileExistsPromptUserButton;
    private JRadioButton fileExistsSkipDirectoryButton;
    private JRadioButton fileExistsSkipFileButton;
    private JCheckBox quietModeBox;
    // End of variables declaration//GEN-END:variables

    private abstract static class AbstractListener<A> implements ActionListener {

        protected final AbstractButton button;
        protected final ExtractProperties p;
        protected final A action;

        public AbstractListener(AbstractButton button, ExtractProperties p, A action) {
            this.button = button;
            this.p = p;
            this.action = action;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (button.isSelected()) {
//                logger.log(Level.DEBUG, "Setting action " + action.getClass().getSimpleName() + "." + action);
                setAction(action);
            }
        }

        protected abstract void setAction(A action);
    }

    private static class CreateDirListener extends AbstractListener<CreateDirectoryFailedAction> {

        public CreateDirListener(AbstractButton button, ExtractProperties p, CreateDirectoryFailedAction action) {
            super(button, p, action);
        }

        @Override
        protected void setAction(CreateDirectoryFailedAction action) {
            p.setCreateDirectoryFailedAction(action);
        }
    }

    private static class CreateFileListener extends AbstractListener<CreateFileFailedAction> {

        public CreateFileListener(AbstractButton button, ExtractProperties p, CreateFileFailedAction action) {
            super(button, p, action);
        }

        @Override
        protected void setAction(CreateFileFailedAction action) {
            p.setCreateFileFailedAction(action);
        }
    }

    private static class DirExistsListener extends AbstractListener<DirectoryExistsAction> {

        public DirExistsListener(AbstractButton button, ExtractProperties p, DirectoryExistsAction action) {
            super(button, p, action);
        }

        @Override
        protected void setAction(DirectoryExistsAction action) {
            p.setDirectoryExistsAction(action);
        }
    }

    private static class FileExistsListener extends AbstractListener<FileExistsAction> {

        public FileExistsListener(AbstractButton button, ExtractProperties p, FileExistsAction action) {
            super(button, p, action);
        }

        @Override
        protected void setAction(FileExistsAction action) {
            p.setFileExistsAction(action);
        }
    }

//    public static void main(String[] args) {
//        JFrame jf = new JFrame("Test");
//        jf.add(new ExtractSettingsPanel());
//        jf.pack();
//        jf.setLocationRelativeTo(null);
//        jf.setVisible(true);
//        jf.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
//    }
}
