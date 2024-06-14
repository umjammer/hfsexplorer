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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.LinkedList;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.catacombae.io.ReadableRandomAccessStream;
import org.catacombae.storage.io.DataLocator;
import org.catacombae.storage.io.RandomAccessFileDataLocator;
import org.catacombae.storage.ps.Partition;
import org.catacombae.storage.ps.PartitionSystemHandler;
import org.catacombae.storage.ps.PartitionSystemHandlerFactory;
import org.catacombae.storage.ps.PartitionSystemRecognizer;
import org.catacombae.storage.ps.PartitionSystemType;
import org.catacombae.storage.ps.apm.types.APMPartition;

import static javax.swing.JOptionPane.INFORMATION_MESSAGE;


/**
 * @author <a href="https://catacombae.org" target="_top">Erik Larsson</a>
 */
public class MainController {

    private final MainWindow mainWindow;
    private final MainPanel mainPanel;

    // Model variables
    LinkedList<PartitionSystemHandler> psHandlers = new LinkedList<>();

    public MainController() {
        this.mainPanel = new MainPanel();
        this.mainWindow = new MainWindow(this.mainPanel);

        mainWindow.setLoadFileItemListener(new LoadFileItemListener());
        mainWindow.setLoadPathItemListener(new LoadPathItemListener());
        mainWindow.setExitItemListener(new ExitItemListener());
        mainWindow.setAboutItemListener(new AboutItemListener());
        mainPanel.setPartitionSystemsBoxListener(new PartitionSystemsBoxListener());
        mainPanel.setSynchronizeButtonListener(new SynchronizeButtonListener());

        mainWindow.setDefaultCloseOperation(MainWindow.EXIT_ON_CLOSE);
    }

    public JPanel getPanel() {
        return mainPanel;
    }

    public void showMainWindow() {
        // Initialize
        mainPanel.setPartitionSystemsBoxContents(new LinkedList<>());
        mainPanel.setPartitionSystemsBoxEnabled(false);
        mainPanel.setSynchronizeButtonEnabled(false);

        mainWindow.setVisible(true);
    }

    private void exitProgram() {
        mainWindow.setVisible(false);
        System.exit(0);
    }

    private void loadPartitionSystem(DataLocator loc) {

        LinkedList<PartitionSystemHandler> detectedPartitionSystems = new LinkedList<>();
        LinkedList<String> detectedPartitionSystemDescriptions = new LinkedList<>();
        for (PartitionSystemType curType : PartitionSystemType.values()) {
            if (curType.isTopLevelCapable()) {
                PartitionSystemHandlerFactory fac = curType.createDefaultHandlerFactory();
                if (fac != null) {
                    PartitionSystemRecognizer recognizer = fac.getRecognizer();
                    ReadableRandomAccessStream stream = loc.createReadOnlyFile();
                    long streamLength = -1;
                    try {
                        streamLength = stream.length();
                    } catch (Exception e) {
                    }
//                    fac.createDetector(loc);
                    if (recognizer.detect(stream, 0, streamLength)) {
                        PartitionSystemHandler handler = fac.createHandler(loc);
                        detectedPartitionSystems.add(fac.createHandler(loc));
                        detectedPartitionSystemDescriptions.add(fac.getInfo().getPartitionSystemName() +
                                        " (" + handler.getPartitionCount() + " partitions)");
                    }
                }
            }
        }

        if (!detectedPartitionSystemDescriptions.isEmpty()) {
            mainPanel.setPartitionSystemsBoxEnabled(true);
            mainPanel.setPartitionSystemsBoxContents(detectedPartitionSystemDescriptions);
        } else {
            mainPanel.setPartitionSystemsBoxEnabled(false);
        }

        mainPanel.clearPartitionList();
        if (!detectedPartitionSystems.isEmpty()) {
            PartitionSystemHandler handler = detectedPartitionSystems.getFirst();
            Partition[] partitions = handler.getPartitions();
            int i = 0;
            for (Partition p : partitions) {
                String name = "";
                if (p instanceof APMPartition)
                    name = ((APMPartition) p).getPmPartNameAsString();

                mainPanel.addPartition("" + ((i++) + 1), p.getType().toString(),
                        name, "" + p.getStartOffset(),
                        (p.getStartOffset() + p.getLength()) + "");
            }
        } else {
            JOptionPane.showMessageDialog(mainPanel,
                    "No partition systems found.", "Info",
                    INFORMATION_MESSAGE);
        }
    }

    private class LoadFileItemListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent evt) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setMultiSelectionEnabled(false);
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            if (fileChooser.showOpenDialog(mainPanel) == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();

                mainWindow.setCurrentFilename(selectedFile.getPath());

                RandomAccessFileDataLocator loc = new RandomAccessFileDataLocator(selectedFile);

                loadPartitionSystem(loc);
            }
        }
    }

    private class LoadPathItemListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent evt) {
            String path = JOptionPane.showInputDialog(mainPanel, "Path:",
                    "Enter path to partition system",
                    JOptionPane.PLAIN_MESSAGE);
            if (path != null) {
                File selectedFile = new File(path);
                if (!selectedFile.exists())
                    JOptionPane.showMessageDialog(mainPanel, "Path:\n    " + path + "\ndoes not seem to exist.");
                else if (!selectedFile.canRead())
                    JOptionPane.showMessageDialog(mainPanel, "Can not read:\n    " + path);
                else {
                    mainWindow.setCurrentFilename(selectedFile.getPath());

                    RandomAccessFileDataLocator loc = new RandomAccessFileDataLocator(selectedFile);

                    loadPartitionSystem(loc);
                }
            }
        }
    }

    private class ExitItemListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent evt) {
            exitProgram();
        }
    }

    private class AboutItemListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent evt) {
            JOptionPane.showMessageDialog(mainPanel, "jParted 0.1", "About", INFORMATION_MESSAGE);
        }
    }

    private static class PartitionSystemsBoxListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent evt) {
        }
    }

    private static class SynchronizeButtonListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent evt) {
        }
    }
}
