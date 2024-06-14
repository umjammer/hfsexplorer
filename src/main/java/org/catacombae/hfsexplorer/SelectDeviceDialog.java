/*-
 * Copyright (C) 2006-2008 Erik Larsson
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

package org.catacombae.hfsexplorer;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ItemEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.catacombae.io.ReadableRandomAccessStream;
import org.catacombae.io.ReadableConcatenatedStream;
import org.catacombae.storage.io.win32.ReadableWin32FileStream;
import org.catacombae.hfsexplorer.gui.SelectDevicePanel;
import org.catacombae.io.ReadableFileStream;
import org.catacombae.io.RuntimeIOException;
import org.catacombae.storage.fs.FileSystemDetector;
import org.catacombae.storage.fs.FileSystemHandler;
import org.catacombae.storage.fs.FileSystemHandlerFactory;
import org.catacombae.storage.fs.FileSystemMajorType;
import org.catacombae.storage.ps.Partition;
import org.catacombae.storage.fs.hfscommon.HFSCommonFileSystemRecognizer;
import org.catacombae.storage.fs.hfscommon.HFSCommonFileSystemRecognizer.FileSystemType;
import org.catacombae.storage.io.DataLocator;
import org.catacombae.storage.io.ReadableStreamDataLocator;
import org.catacombae.storage.io.SubDataLocator;
import org.catacombae.storage.ps.PartitionSystemDetector;
import org.catacombae.storage.ps.PartitionSystemHandler;
import org.catacombae.storage.ps.PartitionSystemHandlerFactory;
import org.catacombae.storage.ps.PartitionSystemType;
import org.catacombae.storage.ps.PartitionType;
import org.catacombae.storage.ps.PartitionType.ContentType;
import org.catacombae.storage.ps.gpt.GPTRecognizer;
import org.catacombae.util.ObjectContainer;

import static java.lang.System.getLogger;


/**
 * @author <a href="https://catacombae.org" target="_top">Erik Larsson</a>
 */
public abstract class SelectDeviceDialog extends JDialog {

    private static final Logger logger = getLogger(SelectDeviceDialog.class.getName());

    private final SelectDevicePanel guiPanel;

    // Shortcuts to variables in guiPanel
    private final JButton autodetectButton;
    private final JRadioButton selectDeviceButton;
    private final JRadioButton specifyDeviceNameButton;
    private final JButton loadButton;
    private final JButton cancelButton;
    private final JTextField specifyDeviceNameField;
    private final JComboBox<String> detectedDevicesCombo;

    // Additional gui stuff
    private final ButtonGroup selectSpecifyGroup;

    private ReadableRandomAccessStream result = null;
    private String resultCreatePath = null;
    private String[] detectedDeviceNames = null;

    private long lastRefreshTimestamp = 0;

    private interface SelectDeviceDialogFactory {

        boolean isSystemSupported();

        SelectDeviceDialog createDeviceDialog(Frame owner, boolean modal, String title);
    }

    private static final WindowsNT4Factory windowsNt4Factory = new WindowsNT4Factory();

    private static final SelectDeviceDialogFactory[] factories = {
            new WindowsFactory(),
            windowsNt4Factory, /* Must come immediately after WindowsFactory. */
            new LinuxFactory(),
            new MacOSXFactory(),
            new FreeBSDFactory(),
            new SolarisFactory(),
    };

    protected SelectDeviceDialog(Frame owner, boolean modal, String title) {
        super(owner, modal);
        setTitle(title);

        this.guiPanel = new SelectDevicePanel(getExampleDeviceName());
        this.autodetectButton = guiPanel.autodetectButton;
        this.selectDeviceButton = guiPanel.selectDeviceButton;
        this.specifyDeviceNameButton = guiPanel.specifyDeviceNameButton;
        this.loadButton = guiPanel.loadButton;
        this.cancelButton = guiPanel.cancelButton;
        this.specifyDeviceNameField = guiPanel.specifyDeviceNameField;
        this.detectedDevicesCombo = guiPanel.detectedDevicesCombo;

        this.selectSpecifyGroup = new ButtonGroup();
        selectSpecifyGroup.add(selectDeviceButton);
        selectSpecifyGroup.add(specifyDeviceNameButton);

        refreshDevices();

        autodetectButton.addActionListener(ae -> autodetectFilesystems());
        detectedDevicesCombo.addItemListener(ie -> {
            if (ie.getStateChange() == ItemEvent.SELECTED)
                specifyDeviceNameField.setText(getDevicePrefix() + ie.getItem().toString());
        });
        selectDeviceButton.addActionListener(ae -> {
            detectedDevicesCombo.setEnabled(true);
            specifyDeviceNameField.setEnabled(false);
            specifyDeviceNameField.setText(getDevicePrefix() + detectedDevicesCombo.getSelectedItem().toString());
        });
        specifyDeviceNameButton.addActionListener(ae -> {
            detectedDevicesCombo.setEnabled(false);
            specifyDeviceNameField.setEnabled(true);
        });
        loadButton.addActionListener(ae -> {
            resultCreatePath = specifyDeviceNameField.getText();
            result = createStream(resultCreatePath);
            dispose();
        });
        cancelButton.addActionListener(ae -> dispose());
        addWindowFocusListener(new WindowAdapter() {
            @Override
            public void windowGainedFocus(WindowEvent we) {
                refreshDevices();
            }
        });

//        JPanel container = new JPanel();
//        container.setLayout(new BorderLayout());
//        container.setBorder(new EmptyBorder(5, 5, 5, 5));
//        container.add(guiPanel, BorderLayout.CENTER);

        add(guiPanel, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(null);
        setResizable(false);
    }

    private void refreshDevices() {
        long refreshTimestamp = System.currentTimeMillis();
        if (refreshTimestamp - lastRefreshTimestamp < 100) {
            // Prevent a flood of requests.
            return;
        }
        lastRefreshTimestamp = refreshTimestamp;

        int selectedIndex = detectedDevicesCombo.getSelectedIndex();
        detectedDevicesCombo.removeAllItems();

        String[] newDeviceNames = detectDevices();
        for (String name : newDeviceNames) {
            detectedDevicesCombo.addItem(name);
        }

        if (newDeviceNames.length > 0) {
            // Calculate new selected index.
            int i_limit = (detectedDeviceNames == null) ? 0 : detectedDeviceNames.length;
            int j_limit = newDeviceNames.length;

            int i, j;
            for (i = 0, j = 0; i < i_limit && j < j_limit; ) {
                if (detectedDeviceNames[i].equals(newDeviceNames[j])) {
                    if (i == selectedIndex) {
                        break;
                    }

                    ++i;
                    ++j;
                } else if (j <= i) {
                    ++i;
                }
            }

            if (j >= j_limit) {
                j = j_limit - 1;
            }

            detectedDevicesCombo.setSelectedIndex(j);

            specifyDeviceNameField.setText(getDevicePrefix() + detectedDevicesCombo.getSelectedItem().toString());
        } else {
            detectedDevicesCombo.setSelectedIndex(-1);
            specifyDeviceNameField.setText("");
        }

        detectedDeviceNames = newDeviceNames;
    }

    public ReadableRandomAccessStream getPartitionStream() {
        if (this instanceof WindowsNT4 && result instanceof ReadableWin32FileStream) {
            // Check if we need to offset the stream to handle a GPT layout
            // inside a protective MBR partition.
            int sectorSize = ((ReadableWin32FileStream) result).getSectorSize();
            ReadableRandomAccessStream gptStream;

            // Try detecting GPT partitions inside MBR protective
            // partition by creating a sector-sized hole preceding the
            // device for the MBR.
            gptStream = new ReadableConcatenatedStream(result, -sectorSize, result.length() + sectorSize);

            GPTRecognizer gptRecognizer = new GPTRecognizer();
            if (gptRecognizer.detect(gptStream, 0, gptStream.length())) {
                return gptStream;
            }
        }
        return result;
    }

    /** Could include an identifier of a partitioning scheme. This should only be used to display a descriptive locator. */
    public String getPathName() {
        return resultCreatePath;
    }

    protected ReadableRandomAccessStream createStream(String path) {
        return new ReadableFileStream(path);
    }

    protected abstract String getDevicePrefix();

    protected abstract String getExampleDeviceName();

    protected abstract boolean isPartition(String deviceName);

    protected abstract String[] detectDevices();

    public static boolean isSystemSupported() {
        boolean supported = false;

        for (SelectDeviceDialogFactory factory : factories) {
            if (factory.isSystemSupported()) {
                supported = true;
                break;
            }
        }

        return supported;
    }

    public static SelectDeviceDialog createSelectDeviceDialog(Frame owner, boolean modal, String title) {
        SelectDeviceDialog dialog = null;

        for (SelectDeviceDialogFactory factory : factories) {
            if (factory.isSystemSupported()) {
                dialog = factory.createDeviceDialog(owner, modal, title);
                if (dialog instanceof SelectDeviceDialog.Windows && dialog.detectedDeviceNames.length == 0) {
                    SelectDeviceDialog nt4dialog =
                            windowsNt4Factory.createDeviceDialog(owner, modal, title);
                    if (nt4dialog.detectedDeviceNames.length != 0) {
                        dialog = nt4dialog;
                    }
                }
                break;
            }
        }

        return dialog;
    }

    private static String getFilesystemInfo(ReadableRandomAccessStream deviceStream, String deviceDescription) {
        String fsInfo = null;
        DataLocator inputDataLocator = null;

        try {
            inputDataLocator = new ReadableStreamDataLocator(deviceStream);
            FileSystemMajorType[] fsTypes = FileSystemDetector.detectFileSystem(inputDataLocator);
            FileSystemHandlerFactory fsFactory = null;

            for (FileSystemMajorType type : fsTypes) {
                FileSystemHandler fsHandler = null;
                try {
                    switch (type) {
                        case APPLE_HFS:
                        case APPLE_HFS_PLUS:
                        case APPLE_HFSX:
                            fsFactory = type.createDefaultHandlerFactory();
                            break;
                        default:
                            break;
                    }

                    if (fsFactory != null) {
                        fsHandler = fsFactory.createHandler(inputDataLocator);
                        fsInfo = "\"" + fsHandler.getRoot().getName() + "\" (" + deviceDescription + ")";
                        break;
                    }
                } catch (Exception e) {
                    logger.log(Level.DEBUG, "Exception while getting file system " +
                            "label for filesystem major type " + type + ":");
                    logger.log(Level.ERROR, e.getMessage(), e);
                } finally {
                    if (fsHandler != null) {
                        fsHandler.close();
                    }
                }
            }
        } catch (Exception e) {
            logger.log(Level.DEBUG, "Exception while getting file system label:");
            logger.log(Level.ERROR, e.getMessage(), e);
        } finally {
            if (inputDataLocator != null) {
                inputDataLocator.close();
            }
        }

        if (fsInfo == null) {
            fsInfo = deviceDescription;
        }

        return fsInfo;
    }

    private String getFilesystemInfoString(String deviceName) {
        ReadableRandomAccessStream fsStream = null;
        try {
            fsStream = createStream(getDevicePrefix() + deviceName);
            return getFilesystemInfo(fsStream, deviceName);
        } finally {
        }
    }

    private String getFilesystemInfoString(EmbeddedPartitionEntry pe) {
        ReadableRandomAccessStream fsStream = null;
        try {
            fsStream = new ReadableConcatenatedStream(
                            createStream(getDevicePrefix() + pe.deviceName),
                            pe.psOffset + pe.partition.getStartOffset(),
                            pe.partition.getLength());
            return getFilesystemInfo(fsStream, pe.toString());
        } finally {
        }
    }

    protected void autodetectFilesystems() {
        LinkedList<String> plainFileSystems = new LinkedList<>();
        LinkedList<EmbeddedPartitionEntry> embeddedFileSystems = new LinkedList<>();
//        String skipPrefix = null;

        refreshDevices();

        // Look for file systems that sit inside partition systems unsupported by Windows.
        for (String deviceName : detectedDeviceNames) {
            //            System.out.println("Checking if \"" + name + "\" might be an unsupported partition system...");
//            System.out.println("  name.startsWith(\"CdRom\") == " + name.startsWith(DEVICE_PREFIX + "CdRom"));
//            System.out.println("  name.endsWith(\"Partition0\") == " + name.endsWith("Partition0"));

//            if (name.startsWith("CdRom") ||
//                    (name.endsWith("Partition0") &&
//                            !(i + 1 < detectedDeviceNames.length && detectedDeviceNames[i + 1].endsWith("Partition1")))) {
            // We have an unidentifed partition system at "name"
//            System.out.println("TRUE!");
//            if (skipPrefix != null && deviceName.startsWith(skipPrefix))
//                continue;
//            else
//                skipPrefix = null;

            ReadableRandomAccessStream llf = null;
            int psOffset = 0;
            ReadableStreamDataLocator llfLocator = null;
            PartitionSystemHandler partSys = null;
            try {
                llf = createStream(getDevicePrefix() + deviceName);

                PartitionSystemType[] detectedTypes = PartitionSystemDetector.detectPartitionSystem(llf, false);

                if (detectedTypes.length == 0 && this instanceof WindowsNT4 &&
                        llf instanceof ReadableWin32FileStream) {
                    int sectorSize = ((ReadableWin32FileStream) llf).getSectorSize();
                    ReadableRandomAccessStream gptStream;

                    // Try detecting GPT partitions inside MBR protective
                    // partition by creating a sector-sized hole preceding the
                    // device for the MBR.
                    psOffset = -sectorSize;
                    gptStream = new ReadableConcatenatedStream(llf, -sectorSize, llf.length() + sectorSize);

                    GPTRecognizer a = new GPTRecognizer();
                    if (a.detect(gptStream, 0, gptStream.length())) {
                        detectedTypes = new PartitionSystemType[] {
                                PartitionSystemType.GPT,
                        };
                        llf = gptStream;
                    }
                }

                PartitionSystemType pst;
                if (detectedTypes.length == 1)
                    pst = detectedTypes[0];
                else if (detectedTypes.length == 0)
                    pst = null;
                else {
                    StringBuilder msg = new StringBuilder(detectedTypes.length + " partition types detected: { ");
                    for (PartitionSystemType t : detectedTypes)
                        msg.append(t).append(" ");
                    msg.append("} Cannot continue.");
                    throw new RuntimeException(msg.toString());
                }

                boolean fileSystemFound = false;

                if (pst != null) {
                    PartitionSystemHandlerFactory fact = pst.createDefaultHandlerFactory();

                    llfLocator = new ReadableStreamDataLocator(llf);
                    partSys = fact.createHandler(llfLocator);

                    ArrayList<Partition> parts = new ArrayList<>(Arrays.asList(partSys.getPartitions()));
                    for (int j = 0; j < parts.size(); ++j) {
                        Partition part = parts.get(j);
                        PartitionType pt = part.getType();

                        if (pt.getContentType() == ContentType.PARTITION_SYSTEM) {
                            PartitionSystemType epst = pt.getAssociatedPartitionSystemType();
                            PartitionSystemHandler ph =
                                    epst.createDefaultHandlerFactory().createHandler(new SubDataLocator(llfLocator,
                                            part.getStartOffset(), part.getLength()));
                            EmbeddedPartitionEntry outerPartitionEntry =
                                    new EmbeddedPartitionEntry(deviceName, j, pst, part, psOffset);
                            Partition[] embeddedPartitions = ph.getPartitions();
                            for (int k = 0; k < embeddedPartitions.length; ++k) {
                                Partition embeddedPart = embeddedPartitions[k];

                                PartitionType ept = embeddedPart.getType();
                                if (ept != PartitionType.APPLE_HFS_CONTAINER && ept != PartitionType.APPLE_HFSX) {
                                    continue;
                                }

                                FileSystemType fsType = HFSCommonFileSystemRecognizer.detectFileSystem(llf,
                                        part.getStartOffset() + embeddedPart.getStartOffset());

                                if (HFSCommonFileSystemRecognizer.isTypeSupported(fsType)) {
                                    fileSystemFound = true;
                                    embeddedFileSystems.add(new EmbeddedPartitionEntry(
                                            outerPartitionEntry, k, epst, embeddedPart, psOffset));
                                }
                            }

                            ph.close();
                        } else if (pt == PartitionType.APPLE_HFS_CONTAINER || pt == PartitionType.APPLE_HFSX) {
                            FileSystemType fsType = HFSCommonFileSystemRecognizer.
                                    detectFileSystem(llf, part.getStartOffset());

                            if (HFSCommonFileSystemRecognizer.isTypeSupported(fsType)) {
                                fileSystemFound = true;
                                embeddedFileSystems.add(new EmbeddedPartitionEntry(deviceName, j, pst, part, psOffset));
                            }
                        }
                    }
                }

                if (!fileSystemFound && !isPartition(deviceName)) {
                    FileSystemType fsType = HFSCommonFileSystemRecognizer.detectFileSystem(llf, 0);

                    if (HFSCommonFileSystemRecognizer.isTypeSupported(fsType))
                        plainFileSystems.add(deviceName);
                }
                // If we found file systems in embedded partition systems,
                // ignore windows-detected partitions if the embedded partition
                // system is at Partition0. */
//                else if(deviceName.endsWith("Partition0"))
//                    skipPrefix = deviceName.substring(0, deviceName.length()-1);
            } catch (Exception e) {
                System.out.println("INFO: Non-critical exception while " +
                        "detecting partition system at \"" + getDevicePrefix() + deviceName + "\": " + e);

                if (llf != null) {
                    FileSystemType fsType = HFSCommonFileSystemRecognizer.detectFileSystem(llf, 0);

                    if (HFSCommonFileSystemRecognizer.isTypeSupported(fsType))
                        plainFileSystems.add(deviceName);
                }
            } finally {
                if (partSys != null) {
                    partSys.close(); // Will also close llfLocator, llf.
                } else if (llfLocator != null) {
                    llfLocator.close(); // Will also close llf.
                } else if (llf != null) {
                    llf.close();
                }
            }
        }

        if (!plainFileSystems.isEmpty() || !embeddedFileSystems.isEmpty()) {
            int i;

            String[] plainStrings = new String[plainFileSystems.size()];
            i = 0;
            for (String cur : plainFileSystems) {
                plainStrings[i++] = getFilesystemInfoString(cur);
            }

            String[] embeddedStrings = new String[embeddedFileSystems.size()];
            i = 0;
            for (EmbeddedPartitionEntry cur : embeddedFileSystems) {
                embeddedStrings[i++] = getFilesystemInfoString(cur);
            }

            String[] allOptions = new String[plainStrings.length + embeddedStrings.length];
            for (i = 0; i < plainStrings.length; ++i)
                allOptions[i] = plainStrings[i];
            for (i = 0; i < embeddedStrings.length; ++i)
                allOptions[plainStrings.length + i] = embeddedStrings[i];

            Object selectedValue = JOptionPane.showInputDialog(this,
                    "Autodetection complete! Found " + allOptions.length + " " +
                            "HFS+ file systems.\n" +
                            "Please choose which one to load:",
                    "Load HFS+ file system", JOptionPane.QUESTION_MESSAGE,
                    null, allOptions, allOptions[0]);

            if (selectedValue != null) {
                int selectedIndex = -1;
                for (i = 0; i < allOptions.length; ++i) {
                    if (selectedValue.equals(allOptions[i])) {
                        selectedIndex = i;
                        break;
                    }
                }

                if (selectedIndex == -1) {
                    throw new RuntimeException("selectedIndex == -1");
                } else {
                    if (selectedIndex >= plainStrings.length) {
                        // We have an embedded FS
                        selectedIndex -= plainStrings.length;
                        EmbeddedPartitionEntry embeddedInfo = embeddedFileSystems.get(selectedIndex);
                        if (embeddedInfo == null)
                            throw new RuntimeException("embeddedInfo == null");

                        switch (embeddedInfo.psType) {
                            case APM:
                            case GPT:
                            case MBR:
                            case DOS_EXTENDED:
                                ReadableRandomAccessStream llf =
                                        createStream(getDevicePrefix() + embeddedInfo.deviceName);

                                Partition p = embeddedInfo.partition;
                                resultCreatePath = getDevicePrefix() + embeddedInfo;
                                result = new ReadableConcatenatedStream(llf,
                                        embeddedInfo.psOffset + p.getStartOffset(), p.getLength());
                                dispose();
                                break;
                            default:
                                throw new RuntimeException("Unexpected partition system: " + embeddedInfo.psType);
                        }
                    } else {
                        String plainInfo = plainFileSystems.get(selectedIndex);
                        if (plainInfo == null) {
                            throw new RuntimeException("plainInfo == null");
                        }

                        resultCreatePath = getDevicePrefix() + plainInfo;
                        result = createStream(resultCreatePath);
                        dispose();
                    }
                }
            }
        }
//        else if (plainFileSystems.size() > 0) {
//            int res = JOptionPane.showConfirmDialog(this, "Autodetection complete! Found an " +
//                            "HFS+ file system at \"" +
//                            plainFileSystems.getFirst() + "\".\n" +
//                            "Do you want to load it?",
//                    "Load HFS+ file system",
//                    JOptionPane.YES_NO_OPTION,
//                    JOptionPane.QUESTION_MESSAGE);
//            if (res == JOptionPane.YES_OPTION) {
//                result = DEVICE_PREFIX + plainFileSystems.getFirst();
//                dispose();
//            }
//        }
        else
            JOptionPane.showMessageDialog(this, "No HFS+ file systems found...",
                    "Result",
                    JOptionPane.INFORMATION_MESSAGE);
    }

    private static class WindowsFactory implements SelectDeviceDialogFactory {

        @Override
        public boolean isSystemSupported() {
            return System.getProperty("os.name").toLowerCase().
                    startsWith("windows");
        }

        @Override
        public SelectDeviceDialog createDeviceDialog(Frame owner, boolean modal, String title) {
            return new SelectDeviceDialog.Windows(owner, modal, title);
        }
    }

    private static class Windows extends SelectDeviceDialog {

        public Windows(Frame owner, boolean modal, String title) {
            super(owner, modal, title);
        }

        @Override
        protected ReadableRandomAccessStream createStream(String path) {
            return new ReadableWin32FileStream(path);
        }

        @Override
        protected String getDevicePrefix() {
            return "\\\\?\\GLOBALROOT\\Device\\";
        }

        @Override
        protected String getExampleDeviceName() {
            return "\\\\?\\GLOBALROOT\\Device\\Harddisk0\\Partition1";
        }

        @Override
        protected boolean isPartition(String deviceName) {
            return !deviceName.endsWith("Partition0") && deviceName.matches("Harddisk[0-9]+\\\\Partition[0-9]+$");
        }

        /**
         * This method is only tested with Windows XP (SP2, x86). Also, it won't
         * work with devices that are not mounted using the Windows XP standard
         * names. For example, Bo Brantén's filedisk creates a device with
         * another name. However, if your file system is on a file, this method
         * is not needed.
         *
         * @return a list of the names of the detected devices
         */
        @Override
        protected String[] detectDevices() {
            LinkedList<String> activeDeviceNames = new LinkedList<>();

            //
            // Since I've been too lazy to figure out how to implement a native
            // method for reading the contents of the device tree, I'll just
            // make up names for at least 20 harddrives, with at least 20
            // partitions in each and check for existence.
            //

            // 20 hard drives minimum...
            for (int i = 0; true; ++i) {
                boolean anyFound = false;
                // 20 partitions each minimum...
                for (int j = 0; true; ++j) {
                    try {
                        // Should I add Partition0 to the list? It really means
                        // "the whole drive". Partition1 is the first
                        // partition...
                        String currentDevice = "Harddisk" + i + "\\Partition" + j;
                        ReadableRandomAccessStream curFile = createStream(getDevicePrefix() + currentDevice);
                        curFile.close();
                        activeDeviceNames.addLast(currentDevice);
                        anyFound = true;
                    } catch (Exception e) {
                        if (j >= 20)
                            break;
                    }
                }
                if (!anyFound && i >= 20)
                    break;
            }

            /* ...and 20 CD-ROMs minimum */
            for (int i = 0; true; ++i) {
                try {
                    String currentDevice = "CdRom" + i;
                    ReadableRandomAccessStream curFile = createStream(getDevicePrefix() + currentDevice);
                    curFile.close();
                    activeDeviceNames.addLast(currentDevice);
                } catch (Exception e) {
                    if (i >= 20) {
                        break;
                    }
                }
            }

            /* Check for TrueCrypt volumes 'A'-'Z', using their special naming
             * scheme. */
            for (char c = 'A'; c <= 'Z'; ++c) {
                try {
                    String currentDevice = "TrueCryptVolume" + c;
                    ReadableRandomAccessStream curFile = createStream(getDevicePrefix() + currentDevice);
                    curFile.close();
                    activeDeviceNames.addLast(currentDevice);
                } catch (Exception e) {
                }
            }

            return activeDeviceNames.toArray(String[]::new);
        }
    }

    private static class WindowsNT4Factory implements SelectDeviceDialogFactory {

        @Override
        public boolean isSystemSupported() {
            return System.getProperty("os.name").toLowerCase().startsWith("windows");
        }

        @Override
        public SelectDeviceDialog createDeviceDialog(Frame owner, boolean modal, String title) {
            return new SelectDeviceDialog.WindowsNT4(owner, modal, title);
        }
    }

    private static class WindowsNT4 extends SelectDeviceDialog {

        public WindowsNT4(Frame owner, boolean modal, String title) {
            super(owner, modal, title);
        }

        @Override
        protected ReadableRandomAccessStream createStream(String path) {
            return new ReadableWin32FileStream(path);
        }

        @Override
        protected String getDevicePrefix() {
            return "\\\\.\\";
        }

        @Override
        protected String getExampleDeviceName() {
            return "\\\\.\\E:";
        }

        @Override
        protected boolean isPartition(String deviceName) {
            // Technically this is true, however this is used only to determine
            // if we should ignore it in favour of the internal partition
            // system parser, and we can't use it if we don't have access to
            // the whole devices. So return false (maybe this method should be
            // renamed).
            return false;
        }

        /**
         * This method is only tested with Windows NT 4.0. (SP6a, x86). Also,
         * it won't work with devices that are not mounted using the Windows NT
         * standard names.
         *
         * @return a list of the names of the detected devices
         */
        @Override
        protected String[] detectDevices() {
            LinkedList<String> activeDeviceNames = new LinkedList<>();

            //
            // Since I've been too lazy to figure out how to implement a native
            // method for reading the contents of the device tree I'll just
            // iterate over all possible drive letters.
            //

            // If all else fails, use drive letters.
            if (activeDeviceNames.isEmpty()) {
                for (char c = 'A'; c <= 'Z'; ++c) {
                    try {
                        String currentDevice = c + ":";
                        ReadableRandomAccessStream curFile = createStream(getDevicePrefix() + currentDevice);
                        curFile.close();
                        activeDeviceNames.addLast(currentDevice);
                    } catch (Exception e) {
                    }
                }
            }

            return activeDeviceNames.toArray(String[]::new);
        }
    }

    private static abstract class CommonUNIX extends SelectDeviceDialog {

        public CommonUNIX(Frame owner, boolean modal, String title) {
            super(owner, modal, title);
        }

        protected abstract FilenameFilter getDiskDeviceFileNameFilter();

        @Override
        protected String getDevicePrefix() {
            return "/dev/";
        }

        @Override
        protected String[] detectDevices() {
            File devDirFile = new File(getDevicePrefix());
            File[] diskDevices = devDirFile.listFiles(getDiskDeviceFileNameFilter());
            ArrayList<String> deviceNames = new ArrayList<>(diskDevices.length);

            for (File diskDevice : diskDevices) {
                String curName = diskDevice.getName();
                ObjectContainer<Boolean> canRead = new ObjectContainer<>(null);

                Thread t = new Thread(() -> {
                    FileInputStream is = null;
                    try {
                        is = new FileInputStream(getDevicePrefix() + curName);
                        canRead.o = true;
                    } catch (IOException ex) {
                        canRead.o = false;
                    } finally {
                        if (is != null) {
                            try {
                                is.close();
                            } catch (IOException ex) {
                                logger.log(Level.ERROR, ex.getMessage(), ex);
                            }
                        }
                    }
                });

                t.start();
                try {
                    // We wait 5 seconds for the thread to finish.
                    t.join(5000);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }

                t.interrupt();

                if (canRead.o == null) {
                    logger.log(Level.DEBUG, "Timeout while detecting device at: " + getDevicePrefix() + curName);
                } else if (canRead.o) {
                    deviceNames.add(curName);
                } else {
                    // Just ignore device if we can't open it for reading.
                }
            }

            String[] deviceNamesArray = deviceNames.toArray(String[]::new);

            // Sorted output is nice.
            Arrays.sort(deviceNamesArray);

            return deviceNamesArray;
        }
    }

    private static class LinuxFactory implements SelectDeviceDialogFactory {

        @Override
        public boolean isSystemSupported() {
            return System.getProperty("os.name").toLowerCase().startsWith("linux");
        }

        @Override
        public SelectDeviceDialog createDeviceDialog(Frame owner, boolean modal, String title) {
            return new SelectDeviceDialog.Linux(owner, modal, title);
        }
    }

    private static class Linux extends CommonUNIX {

        private static class LinuxDeviceFilenameFilter implements FilenameFilter {

            @Override
            public boolean accept(File dir, String name) {
                return name.matches("sd[a-z]+([0-9]+)?$") ||
                        name.matches("hd[a-z]+([0-9]+)?$") ||
                        name.matches("sr[0-9]+$") ||
                        name.matches("fd[0-9]+$") ||
                        name.matches("dm-[0-9]+$") ||
                        name.matches("mmcblk[0-9]+(p[0-9]+)?$");
            }
        }

        private static final FilenameFilter diskDeviceFileNameFilter = new LinuxDeviceFilenameFilter();

        public Linux(Frame owner, boolean modal, String title) {
            super(owner, modal, title);
        }

        @Override
        protected FilenameFilter getDiskDeviceFileNameFilter() {
            return diskDeviceFileNameFilter;
        }

        @Override
        protected String getExampleDeviceName() {
            return "/dev/sda1";
        }

        @Override
        protected boolean isPartition(String deviceName) {
            return deviceName.matches("[0-9]+$");
        }

        @Override
        protected String[] detectDevices() {
            // Special case for Linux: We read /proc/partitions in order to get
            // a list of all block devices.

            LinkedList<String> deviceNames = new LinkedList<>();

            BufferedReader r = null;
            try {
                try {
                    r = new BufferedReader(new InputStreamReader(
                            new FileInputStream("/proc/partitions"), StandardCharsets.UTF_8));
                } catch (IOException e) {
                    logger.log(Level.DEBUG, "Unable to open /proc/partitions for " +
                            "reading! Falling back on common UNIX method for " +
                            "device detection.");
                    return super.detectDevices();
                }

                // Skip first line, which is just a description.
                r.readLine();

                for (String curLine; (curLine = r.readLine()) != null; ) {
                    if (curLine.trim().isEmpty()) {
                        // Ignore whitespace.
                        continue;
                    }

                    // Parse line.
                    char[] curLineChars = curLine.toCharArray();
                    int i = 0;

                    // Whitespace.
                    for (; i < curLineChars.length; ++i) {
                        if (!Character.isWhitespace(curLineChars[i])) {
                            break;
                        }
                    }

                    // Major device number.
                    for (; i < curLineChars.length; ++i) {
                        if (Character.isWhitespace(curLineChars[i])) {
                            break;
                        }
                    }

                    // Whitespace.
                    for (; i < curLineChars.length; ++i) {
                        if (!Character.isWhitespace(curLineChars[i])) {
                            break;
                        }
                    }

                    // Minor device number.
                    for (; i < curLineChars.length; ++i) {
                        if (Character.isWhitespace(curLineChars[i])) {
                            break;
                        }
                    }

                    // Whitespace.
                    for (; i < curLineChars.length; ++i) {
                        if (!Character.isWhitespace(curLineChars[i])) {
                            break;
                        }
                    }

                    // Block count.
                    for (; i < curLineChars.length; ++i) {
                        if (Character.isWhitespace(curLineChars[i])) {
                            break;
                        }
                    }

                    // Whitespace.
                    for (; i < curLineChars.length; ++i) {
                        if (!Character.isWhitespace(curLineChars[i])) {
                            break;
                        }
                    }

                    // Device name.
                    if (i < curLineChars.length) {
                        String curName = new String(curLineChars, i, curLineChars.length - i).trim();
                        try {
                            FileInputStream is = new FileInputStream(getDevicePrefix() + curName);
                            is.close();

                            deviceNames.add(curName);
                        } catch (IOException ex) {
                            // Just ignore device if we can't open it for
                            // reading.
                        }
                    } else {
                        logger.log(Level.DEBUG, "Error while parsing /proc/partitions line \"" + curLine + "\".");
                    }
                }

                String[] deviceNamesArray = deviceNames.toArray(String[]::new);

                // Sorted output is nice.
                Arrays.sort(deviceNamesArray);

                return deviceNamesArray;
            } catch (IOException ex) {
                throw new RuntimeIOException(ex);
            }
        }
    }

    private static class MacOSXFactory implements SelectDeviceDialogFactory {

        @Override
        public boolean isSystemSupported() {
            return System.getProperty("os.name").toLowerCase().startsWith("mac os x");
        }

        @Override
        public SelectDeviceDialog createDeviceDialog(Frame owner, boolean modal, String title) {
            return new SelectDeviceDialog.MacOSX(owner, modal, title);
        }
    }

    private static class MacOSX extends CommonUNIX {

        private static class MacOSXDeviceFilenameFilter implements FilenameFilter {

            @Override
            public boolean accept(File dir, String name) {
                return name.matches("disk[0-9]+(s[0-9]+)?$");
            }
        }

        private static final FilenameFilter diskDeviceFileNameFilter = new MacOSXDeviceFilenameFilter();

        public MacOSX(Frame owner, boolean modal, String title) {
            super(owner, modal, title);
        }

        @Override
        protected FilenameFilter getDiskDeviceFileNameFilter() {
            return diskDeviceFileNameFilter;
        }

        @Override
        protected String getExampleDeviceName() {
            return "/dev/disk0s1";
        }

        @Override
        protected boolean isPartition(String deviceName) {
            return deviceName.matches("s[0-9]+$");
        }
    }

    private static class FreeBSDFactory implements SelectDeviceDialogFactory {

        @Override
        public boolean isSystemSupported() {
            return System.getProperty("os.name").toLowerCase().startsWith("freebsd");
        }

        @Override
        public SelectDeviceDialog createDeviceDialog(Frame owner, boolean modal, String title) {
            return new SelectDeviceDialog.FreeBSD(owner, modal, title);
        }
    }

    private static class FreeBSD extends CommonUNIX {

        private static class FreeBSDDeviceFilenameFilter implements FilenameFilter {

            public final String[] knownDevices;

            public FreeBSDDeviceFilenameFilter(String[] knownDevices) {
                this.knownDevices = knownDevices;
            }

            @Override
            public boolean accept(File dir, String name) {
                if (knownDevices != null) {
                    return acceptSpecific(dir, name);
                } else {
                    return acceptGeneric(dir, name);
                }
            }

            private boolean acceptSpecific(File dir, String name) {
                boolean acceptResult = false;

                for (String device : knownDevices) {
                    if (name.matches("^" + device + "+([sp][0-9]+([a-z]+)?)?$")) {
                        acceptResult = true;
                        break;
                    }
                }

                return acceptResult;
            }

            private static boolean acceptGeneric(File dir, String name) {
                // Device naming info retrieved 2014-08-22 from:
                // https://www.freebsd.org/doc/handbook/disk-organization.html
                //
                return name.matches("ada[0-9]+([sp][0-9]+([a-z]+)?)?$") ||
                        name.matches("ad[0-9]+([sp][0-9]+([a-z]+)?)?$") ||
                        name.matches("da[0-9]+([sp][0-9]+([a-z]+)?)?$") ||
                        name.matches("cd[0-9]+([sp][0-9]+([a-z]+)?)?$") ||
                        name.matches("acd[0-9]+([sp][0-9]+([a-z]+)?)?$") ||
                        name.matches("fd[0-9]+([sp][0-9]+([a-z]+)?)?$") ||
                        name.matches("mcd[0-9]+([sp][0-9]+([a-z]+)?)?$") ||
                        name.matches("scd[0-9]+([sp][0-9]+([a-z]+)?)?$") ||
                        name.matches("sa[0-9]+([sp][0-9]+([a-z]+)?)?$") ||
                        name.matches("ast[0-9]+([sp][0-9]+([a-z]+)?)?$") ||
                        name.matches("aacd[0-9]+([sp][0-9]+([a-z]+)?)?$") ||
                        name.matches("mlxd[0-9]+([sp][0-9]+([a-z]+)?)?$") ||
                        name.matches("mlyd[0-9]+([sp][0-9]+([a-z]+)?)?$") ||
                        name.matches("amrd[0-9]+([sp][0-9]+([a-z]+)?)?$") ||
                        name.matches("idad[0-9]+([sp][0-9]+([a-z]+)?)?$") ||
                        name.matches("twed[0-9]+([sp][0-9]+([a-z]+)?)?$");
            }
        }

        private static final FilenameFilter genericdiskDeviceFileNameFilter =
                new FreeBSDDeviceFilenameFilter(null);

        public FreeBSD(Frame owner, boolean modal, String title) {
            super(owner, modal, title);
        }

        @Override
        protected FilenameFilter getDiskDeviceFileNameFilter() {
            String[] devices = null;
            Process sysctlProcess = null;
            BufferedReader sysctlStdoutReader = null;

            try {
                sysctlProcess = Runtime.getRuntime().exec(new String[] {"/sbin/sysctl", "kern.disks"});

                sysctlStdoutReader =
                        new BufferedReader(new InputStreamReader(sysctlProcess.getInputStream(), StandardCharsets.UTF_8));
                String disksString = sysctlStdoutReader.readLine();

                int retval = sysctlProcess.waitFor();
                if (retval != 0) {
                    logger.log(Level.DEBUG, "sysctl returned error value (" +
                            retval + "). Falling back on exhaustive " +
                            "detection method.");
                } else if (disksString.startsWith("kern.disks: ")) {
                    devices = disksString.substring("kern.disks: ".length()).split("\\s");
                    if (devices.length == 0) {
                        // We should definitely have at least one disk. This
                        // must be an error. */
                        logger.log(Level.DEBUG, "No disks returned from sysctl. " +
                                "Falling back on exhaustive detection method...");
                        devices = null;
                    }
                } else {
                    logger.log(Level.DEBUG, "Unexpected output from sysctl " +
                            "command: \"" + disksString + "\" Falling back " +
                            "on exhaustive detection method...");
                }
            } catch (IOException ex) {
                logger.log(Level.DEBUG, "Exception while issuing sysctl command:");
                logger.log(Level.ERROR, ex.getMessage(), ex);
                logger.log(Level.DEBUG, "Falling back on exhaustive detection method...");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                if (sysctlStdoutReader != null) {
                    try {
                        sysctlStdoutReader.close();
                    } catch (IOException ex) {
                        logger.log(Level.ERROR, ex.getMessage(), ex);
                    }
                }
            }

            FilenameFilter filter;
            if (devices != null) {
                filter = new FreeBSDDeviceFilenameFilter(devices);
            } else {
                filter = genericdiskDeviceFileNameFilter;
            }

            return filter;
        }

        @Override
        protected String getExampleDeviceName() {
            return "/dev/da0";
        }

        @Override
        protected boolean isPartition(String deviceName) {
            return deviceName.matches("s[0-9]+$");
        }
    }

    private static class SolarisFactory implements SelectDeviceDialogFactory {

        @Override
        public boolean isSystemSupported() {
            return System.getProperty("os.name").toLowerCase().startsWith("sunos");
        }

        @Override
        public SelectDeviceDialog createDeviceDialog(Frame owner, boolean modal, String title) {
            return new SelectDeviceDialog.Solaris(owner, modal, title);
        }
    }

    private static class Solaris extends CommonUNIX {

        private static class SolarisDeviceFilenameFilter implements FilenameFilter {

            @Override
            public boolean accept(File dir, String name) {
                // All the files in /dev/dsk should be valid block device names.
                // Alternatively we could use the regexp:
                //     c[0-9]+(t[0-9]+)?d[0-9]+([ps][0-9]+)?$
                //

                return true;
            }
        }

        private static final FilenameFilter diskDeviceFileNameFilter = new SolarisDeviceFilenameFilter();

        public Solaris(Frame owner, boolean modal, String title) {
            super(owner, modal, title);
        }

        @Override
        protected FilenameFilter getDiskDeviceFileNameFilter() {
            return diskDeviceFileNameFilter;
        }

        @Override
        protected String getDevicePrefix() {
            return "/dev/dsk/";
        }

        @Override
        protected String getExampleDeviceName() {
            return "/dev/dsk/c0t0d0s0";
        }

        @Override
        protected boolean isPartition(String deviceName) {
            return deviceName.matches("[ps][0-9]+$");
        }
    }

    private static final class EmbeddedPartitionEntry {

        public final String deviceName;
        public final EmbeddedPartitionEntry outerPartitionEntry;
        public final long partitionNumber;
        public final PartitionSystemType psType;
        public final Partition partition;
        public final long psOffset;

        public EmbeddedPartitionEntry(String deviceName, long partitionNumber,
                                      PartitionSystemType psType, Partition partition, int psOffset) {
            this.deviceName = deviceName;
            this.outerPartitionEntry = null;
            this.partitionNumber = partitionNumber;
            this.psType = psType;
            this.partition = partition;
            this.psOffset = psOffset;
        }

        public EmbeddedPartitionEntry(
                EmbeddedPartitionEntry outerPartitionEntry,
                long partitionNumber, PartitionSystemType psType,
                Partition partition, int psOffset) {
            this.deviceName = outerPartitionEntry.deviceName;
            this.outerPartitionEntry = outerPartitionEntry;
            this.partitionNumber = partitionNumber;
            this.psType = psType;
            this.partition = partition;
            this.psOffset = psOffset + outerPartitionEntry.partition.getStartOffset();
        }

        private String getPartitionSystemString() {
            return switch (psType) {
                case MBR -> "MBR";
                case GPT -> "GPT";
                case APM -> "APM";
                case DOS_EXTENDED -> "EBR";
            };
        }

        private String getPartitionString() {
            return ((outerPartitionEntry != null) ?
                    outerPartitionEntry.getPartitionString() : "") +
                    "[" + getPartitionSystemString() + ":Partition" +
                    partitionNumber + "]";
        }

        @Override
        public String toString() {
            return deviceName + getPartitionString();
        }
    }
}
