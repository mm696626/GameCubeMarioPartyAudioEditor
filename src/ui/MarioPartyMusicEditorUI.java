package ui;

import constants.MarioPartySongNames;
import io.DSPPair;
import io.ModifyJob;
import io.SongModifier;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.*;

public class MarioPartyMusicEditorUI extends JFrame implements ActionListener {

    private JButton pickLeftChannel, pickRightChannel, modifySong, selectGame;
    private String pdtPath = "";
    private String leftChannelPath = "";
    private String rightChannelPath = "";
    private String selectedGame = "";
    private JComboBox<String> songNames;

    private JLabel leftChannelLabel;
    private JLabel rightChannelLabel;
    private JLabel pdtFilePathLabel;
    private JLabel selectedGameLabel;

    private File savedDSPFolder = null;

    private JLabel defaultDSPFolderLabel;
    private JLabel defaultPDTFileLabel;

    private File defaultSavedDSPFolder = null;
    private File defaultPDTFile = null;

    private DefaultListModel<ModifyJob> jobQueueModel;
    private JList<ModifyJob> jobQueueList;
    private JButton addToQueueButton, modifyQueueButton, removeQueueButton, clearQueueButton, runBatchButton;

    public MarioPartyMusicEditorUI() {
        setTitle("Mario Party GameCube Music Editor");
        initSettingsFile();
        loadSettingsFile();
        generateUI();
    }

    private void generateUI() {
        JTabbedPane tabbedPane = new JTabbedPane();

        JPanel chooseGamePanel = new JPanel(new GridBagLayout());
        GridBagConstraints chooseGameGBC = new GridBagConstraints();
        chooseGameGBC.insets = new Insets(5, 5, 5, 5);
        chooseGameGBC.fill = GridBagConstraints.HORIZONTAL;

        selectGame = new JButton("Select PDT and Game");
        selectGame.addActionListener(this);

        if (defaultPDTFile == null || !defaultPDTFile.exists()) {
            pdtFilePathLabel = new JLabel("No PDT file selected");
            selectedGameLabel = new JLabel("No game selected");
        }

        chooseGameGBC.gridx = 0; chooseGameGBC.gridy = 0;
        chooseGamePanel.add(selectGame, chooseGameGBC);

        chooseGameGBC.gridy = 1;
        chooseGamePanel.add(pdtFilePathLabel, chooseGameGBC);

        chooseGameGBC.gridy = 2;
        chooseGamePanel.add(selectedGameLabel, chooseGameGBC);

        tabbedPane.addTab("Choose PDT/Game", chooseGamePanel);

        JPanel songToolsPanel = new JPanel();
        songToolsPanel.setLayout(new BoxLayout(songToolsPanel, BoxLayout.Y_AXIS));

        JPanel songSelectionPanel = new JPanel(new GridBagLayout());
        songSelectionPanel.setBorder(BorderFactory.createTitledBorder("Song Selection"));
        GridBagConstraints songSelectionGBC = new GridBagConstraints();
        songSelectionGBC.insets = new Insets(5, 5, 5, 5);
        songSelectionGBC.fill = GridBagConstraints.HORIZONTAL;

        JLabel filterLabel = new JLabel("Search Songs:");
        JTextField songSearchField = new JTextField();

        JLabel songLabel = new JLabel("Chosen Song:");

        if (songNames == null) {
            songNames = new JComboBox<>();
        }

        songSelectionGBC.gridx = 0;
        songSelectionGBC.gridy = 0;
        songSelectionGBC.gridwidth = 2;
        songSelectionPanel.add(filterLabel, songSelectionGBC);

        songSelectionGBC.gridy = 1;
        songSelectionPanel.add(songSearchField, songSelectionGBC);

        songSelectionGBC.gridwidth = 1;
        songSelectionGBC.gridy = 2;
        songSelectionGBC.gridx = 0;
        songSelectionPanel.add(songLabel, songSelectionGBC);

        songSelectionGBC.gridx = 1;
        songSelectionPanel.add(songNames, songSelectionGBC);

        songSearchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void filterSongs() {
                String filterText = songSearchField.getText().toLowerCase();
                Map<Integer, String> songNameMap = getSongNameMapForSelectedGame();

                if (songNameMap == null) return;

                String currentSelection = (String) songNames.getSelectedItem();

                ArrayList<String> filtered = new ArrayList<>();
                for (String song : songNameMap.values()) {
                    if (song.toLowerCase().contains(filterText)) {
                        filtered.add(song);
                    }
                }

                Collections.sort(filtered);
                DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>(filtered.toArray(new String[0]));
                songNames.setModel(model);

                if (currentSelection != null && filtered.contains(currentSelection)) {
                    songNames.setSelectedItem(currentSelection);
                }
            }

            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                filterSongs();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                filterSongs();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                filterSongs();
            }
        });

        JPanel songPanel = new JPanel(new GridBagLayout());
        songPanel.setBorder(BorderFactory.createTitledBorder("Modify Song"));
        GridBagConstraints songGBC = new GridBagConstraints();
        songGBC.insets = new Insets(5, 5, 5, 5);
        songGBC.fill = GridBagConstraints.HORIZONTAL;

        pickLeftChannel = new JButton("Select Left DSP Channel");
        pickLeftChannel.addActionListener(this);
        leftChannelLabel = new JLabel("No file selected");

        pickRightChannel = new JButton("Select Right DSP Channel");
        pickRightChannel.addActionListener(this);
        rightChannelLabel = new JLabel("No file selected");

        modifySong = new JButton("Modify Selected Song");
        modifySong.addActionListener(this);

        songGBC.gridx = 0; songGBC.gridy = 0;
        songPanel.add(pickLeftChannel, songGBC);
        songGBC.gridx = 1;
        songPanel.add(leftChannelLabel, songGBC);

        songGBC.gridx = 0; songGBC.gridy = 1;
        songPanel.add(pickRightChannel, songGBC);
        songGBC.gridx = 1;
        songPanel.add(rightChannelLabel, songGBC);

        songGBC.gridx = 0; songGBC.gridy = 2;
        songGBC.gridwidth = 2;
        songPanel.add(modifySong, songGBC);

        songToolsPanel.add(songSelectionPanel);
        songToolsPanel.add(Box.createVerticalStrut(10));
        songToolsPanel.add(songPanel);

        tabbedPane.addTab("Modify Songs", songToolsPanel);

        JPanel queuePanel = new JPanel(new BorderLayout());
        queuePanel.setBorder(BorderFactory.createTitledBorder("Batch Job Queue"));

        jobQueueModel = new DefaultListModel<>();
        jobQueueList = new JList<>(jobQueueModel);
        jobQueueList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(jobQueueList);

        JPanel queueButtonPanel = new JPanel(new GridLayout(1, 5, 5, 5));
        addToQueueButton = new JButton("Add");
        modifyQueueButton = new JButton("Modify");
        removeQueueButton = new JButton("Remove");
        clearQueueButton = new JButton("Clear All");
        runBatchButton = new JButton("Run Batch");

        addToQueueButton.addActionListener(this);
        modifyQueueButton.addActionListener(this);
        removeQueueButton.addActionListener(this);
        clearQueueButton.addActionListener(this);
        runBatchButton.addActionListener(this);

        queueButtonPanel.add(addToQueueButton);
        queueButtonPanel.add(modifyQueueButton);
        queueButtonPanel.add(removeQueueButton);
        queueButtonPanel.add(clearQueueButton);
        queueButtonPanel.add(runBatchButton);

        queuePanel.add(scrollPane, BorderLayout.CENTER);
        queuePanel.add(queueButtonPanel, BorderLayout.SOUTH);

        songToolsPanel.add(Box.createVerticalStrut(10));
        songToolsPanel.add(queuePanel);

        setLayout(new BorderLayout());
        add(tabbedPane, BorderLayout.CENTER);

        JPanel settingsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints settingsGBC = new GridBagConstraints();
        settingsGBC.insets = new Insets(5, 5, 5, 5);
        settingsGBC.fill = GridBagConstraints.HORIZONTAL;

        settingsGBC.gridx = 0;
        settingsGBC.gridy = 0;
        settingsPanel.add(new JLabel("Default DSP Folder:"), settingsGBC);

        defaultDSPFolderLabel = new JLabel(defaultSavedDSPFolder != null ? defaultSavedDSPFolder.getAbsolutePath() : "None");
        settingsGBC.gridx = 1;
        settingsPanel.add(defaultDSPFolderLabel, settingsGBC);

        JButton chooseDefaultDSPButton = new JButton("Change");
        chooseDefaultDSPButton.addActionListener(e -> chooseDefaultDSPFolder());
        settingsGBC.gridx = 2;
        settingsPanel.add(chooseDefaultDSPButton, settingsGBC);

        defaultPDTFileLabel = new JLabel(defaultPDTFile != null ? defaultPDTFile.getAbsolutePath() : "None");

        settingsGBC.gridx = 0;
        settingsGBC.gridy = 1;
        settingsGBC.gridwidth = 1;
        settingsPanel.add(new JLabel("Default PDT File:"), settingsGBC);

        settingsGBC.gridx = 1;
        settingsPanel.add(defaultPDTFileLabel, settingsGBC);

        JButton chooseDefaultPDTButton = new JButton("Change");
        chooseDefaultPDTButton.addActionListener(e -> {
            JFileChooser pdtChooser = new JFileChooser();
            pdtChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            pdtChooser.setDialogTitle("Select Default PDT File");
            FileNameExtensionFilter pdtFilter = new FileNameExtensionFilter("PDT Files", "pdt");
            pdtChooser.setFileFilter(pdtFilter);
            pdtChooser.setAcceptAllFileFilterUsed(false);

            int result = pdtChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                defaultPDTFile = pdtChooser.getSelectedFile();
                defaultPDTFileLabel.setText(defaultPDTFile.getAbsolutePath());
                pdtPath = defaultPDTFile.getAbsolutePath();
                pdtFilePathLabel.setText("Selected PDT: " + pdtPath);

                String name = defaultPDTFile.getName().toLowerCase();
                switch (name) {
                    case "mp5_str.pdt": selectedGame = "Mario Party 5"; break;
                    case "mp6_str.pdt": selectedGame = "Mario Party 6"; break;
                    case "mp7_str.pdt": selectedGame = "Mario Party 7"; break;
                }

                selectedGameLabel.setText("Selected Game: " + selectedGame);
                updateSongList();

                saveSettingsToFile();
            }
        });
        settingsGBC.gridx = 2;
        settingsPanel.add(chooseDefaultPDTButton, settingsGBC);

        JButton resetSettingsButton = new JButton("Reset Settings");
        resetSettingsButton.addActionListener(e -> resetSettings());
        settingsGBC.gridx = 0;
        settingsGBC.gridy = 2;
        settingsGBC.gridwidth = 3;
        settingsPanel.add(resetSettingsButton, settingsGBC);

        tabbedPane.addTab("Settings", settingsPanel);
    }

    private void initSettingsFile() {
        File settingsFile = new File("settings.txt");
        PrintWriter outputStream;
        if (!settingsFile.exists()) {
            try {
                outputStream = new PrintWriter(new FileOutputStream(settingsFile));
            }
            catch (FileNotFoundException f) {
                return;
            }

            outputStream.println("defaultSavedDSPFolder" + ":" + "None");
            outputStream.println("defaultPDTFile:None");
            outputStream.close();
        }
    }

    private void loadSettingsFile() {
        File settingsFile = new File("settings.txt");
        try (Scanner inputStream = new Scanner(new FileInputStream(settingsFile))) {
            while (inputStream.hasNextLine()) {
                String line = inputStream.nextLine();
                String[] parts = line.split(":", 2);
                if (parts.length < 2) continue;
                String key = parts[0];
                String value = parts[1];

                switch (key) {
                    case "defaultSavedDSPFolder":
                        if (!value.equals("None")) defaultSavedDSPFolder = new File(value);
                        break;
                    case "defaultPDTFile":
                        if (!value.equals("None")) defaultPDTFile = new File(value);
                        break;
                }
            }

            if (defaultSavedDSPFolder != null && defaultSavedDSPFolder.exists()) {
                savedDSPFolder = defaultSavedDSPFolder;
            }

            if (defaultPDTFile != null && defaultPDTFile.exists()) {
                pdtPath = defaultPDTFile.getAbsolutePath();
                pdtFilePathLabel = new JLabel("No PDT file selected");
                pdtFilePathLabel.setText("Selected PDT: " + pdtPath);

                String pdtFileName = defaultPDTFile.getName().toLowerCase();
                switch (pdtFileName) {
                    case "mp5_str.pdt": selectedGame = "Mario Party 5"; break;
                    case "mp6_str.pdt": selectedGame = "Mario Party 6"; break;
                    case "mp7_str.pdt": selectedGame = "Mario Party 7"; break;
                }

                if (selectedGame != null) {
                    selectedGameLabel = new JLabel("No game selected");
                    selectedGameLabel.setText("Selected Game: " + selectedGame);
                    updateSongList();
                }
            }

        } catch (FileNotFoundException e) {
            return;
        }
    }

    private void chooseDefaultDSPFolder() {
        JFileChooser defaultDSPFolderChooser = new JFileChooser();
        defaultDSPFolderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        defaultDSPFolderChooser.setAcceptAllFileFilterUsed(false);
        int result = defaultDSPFolderChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            defaultSavedDSPFolder = defaultDSPFolderChooser.getSelectedFile();
            defaultDSPFolderLabel.setText(defaultSavedDSPFolder.getAbsolutePath());
            savedDSPFolder = defaultSavedDSPFolder;
            saveSettingsToFile();
        }
    }

    private void saveSettingsToFile() {
        try (PrintWriter writer = new PrintWriter(new FileOutputStream("settings.txt"))) {
            writer.println("defaultSavedDSPFolder:" + (defaultSavedDSPFolder != null ? defaultSavedDSPFolder.getAbsolutePath() : "None"));
            writer.println("defaultPDTFile:" + (defaultPDTFile != null ? defaultPDTFile.getAbsolutePath() : "None"));
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to save settings: " + e.getMessage());
        }
    }

    private void resetSettings() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to reset the settings?",
                "Confirm Reset",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        defaultSavedDSPFolder = null;

        if (defaultDSPFolderLabel != null) {
            defaultDSPFolderLabel.setText("None");
        }

        defaultPDTFile = null;

        if (defaultPDTFileLabel != null) {
            defaultPDTFileLabel.setText("None");
        }

        try (PrintWriter writer = new PrintWriter(new FileOutputStream("settings.txt"))) {
            writer.println("defaultSavedDSPFolder:None");
            writer.println("defaultPDTFile:None");
            JOptionPane.showMessageDialog(this, "Setting reset to default.");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to reset setting: " + e.getMessage());
        }
    }

    private void initPDTPath() {
        JFileChooser pdtFileChooser = new JFileChooser();
        pdtFileChooser.setDialogTitle("Select PDT file");
        pdtFileChooser.setAcceptAllFileFilterUsed(false);

        FileNameExtensionFilter pdtFilter = new FileNameExtensionFilter("PDT Files", "pdt");
        pdtFileChooser.setFileFilter(pdtFilter);

        int userSelection = pdtFileChooser.showOpenDialog(null);

        if (userSelection != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File selectedPDT = pdtFileChooser.getSelectedFile();
        pdtPath = selectedPDT.getAbsolutePath();
        pdtFilePathLabel.setText("Selected PDT: " + pdtPath);

        String pdtFileName = selectedPDT.getName().toLowerCase();

        if (pdtFileName.equals("mp5_str.pdt")) {
            selectedGame = "Mario Party 5";
        } else if (pdtFileName.equals("mp6_str.pdt")) {
            selectedGame = "Mario Party 6";
        } else if (pdtFileName.equals("mp7_str.pdt")) {
            selectedGame = "Mario Party 7";
        } else {
            selectedGame = null;
            while (selectedGame == null) {
                selectedGame = (String) JOptionPane.showInputDialog(
                        this,
                        "Select Game",
                        "Game Selection",
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        new String[]{"Mario Party 5", "Mario Party 6", "Mario Party 7"},
                        "Mario Party 5"
                );

                if (selectedGame == null) {
                    JOptionPane.showMessageDialog(this, "You must select a game. Please choose one.");
                }
            }
        }

        updateSongList();
        selectedGameLabel.setText("Selected Game: " + selectedGame);
    }

    private void chooseLeftChannelPath() {
        if (savedDSPFolder != null) {
            useSavedDSPFolder();
            return;
        }

        int response = JOptionPane.showConfirmDialog(
                this,
                "Would you like to pick a folder of DSPs to select a song from?\n(Your choice will be remembered until closing the program or if you set a default folder)",
                "Choose DSP Folder",
                JOptionPane.YES_NO_OPTION
        );

        if (response == JOptionPane.YES_OPTION) {
            JFileChooser dspFolderChooser = new JFileChooser();
            dspFolderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            dspFolderChooser.setDialogTitle("Select Folder with DSP Files");
            dspFolderChooser.setAcceptAllFileFilterUsed(false);

            int folderSelected = dspFolderChooser.showOpenDialog(this);
            if (folderSelected == JFileChooser.APPROVE_OPTION) {
                savedDSPFolder = dspFolderChooser.getSelectedFile();
                useSavedDSPFolder();
            }
        } else {
            chooseDSP(true);
        }
    }

    private void chooseRightChannelPath() {
        if (savedDSPFolder != null) {
            useSavedDSPFolder();
            return;
        }

        int response = JOptionPane.showConfirmDialog(
                this,
                "Would you like to pick a folder of DSPs to select a song from?\n(Your choice will be remembered until closing the program or if you set a default folder)",
                "Choose DSP Folder",
                JOptionPane.YES_NO_OPTION
        );

        if (response == JOptionPane.YES_OPTION) {
            JFileChooser dspFolderChooser = new JFileChooser();
            dspFolderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            dspFolderChooser.setDialogTitle("Select Folder with DSP Files");
            dspFolderChooser.setAcceptAllFileFilterUsed(false);

            int folderSelected = dspFolderChooser.showOpenDialog(this);
            if (folderSelected == JFileChooser.APPROVE_OPTION) {
                savedDSPFolder = dspFolderChooser.getSelectedFile();
                useSavedDSPFolder();
            }
        } else {
            chooseDSP(false);
        }
    }

    private void useSavedDSPFolder() {
        if (savedDSPFolder == null) return;

        ArrayList<DSPPair> dspPairs = DSPPair.detectDSPPairs(savedDSPFolder);

        if (dspPairs.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No matching DSP pairs found in the saved folder.");
            savedDSPFolder = null;
            return;
        }

        DSPPair selectedPair = (DSPPair) JOptionPane.showInputDialog(
                this,
                "Select DSP Pair:",
                "Select DSP Song",
                JOptionPane.PLAIN_MESSAGE,
                null,
                dspPairs.toArray(),
                dspPairs.getFirst()
        );

        if (selectedPair != null) {
            leftChannelPath = selectedPair.getLeft().getAbsolutePath();
            rightChannelPath = selectedPair.getRight().getAbsolutePath();
            leftChannelLabel.setText(selectedPair.getLeft().getName());
            rightChannelLabel.setText(selectedPair.getRight().getName());
        }
    }

    private void chooseDSP(boolean isLeft) {
        JFileChooser dspFileChooser = new JFileChooser();

        if (isLeft) {
            dspFileChooser.setDialogTitle("Select DSP Left Channel");
        }
        else {
            dspFileChooser.setDialogTitle("Select DSP Right Channel");
        }

        dspFileChooser.setAcceptAllFileFilterUsed(false);
        FileNameExtensionFilter dspFilter = new FileNameExtensionFilter("DSP Files", "dsp");
        dspFileChooser.setFileFilter(dspFilter);

        int userSelection = dspFileChooser.showOpenDialog(this);
        if (userSelection != JFileChooser.APPROVE_OPTION) return;

        File selectedFile = dspFileChooser.getSelectedFile();

        if (isLeft) {
            leftChannelPath = selectedFile.getAbsolutePath();
            leftChannelLabel.setText(selectedFile.getName());
        }

        else {
            rightChannelPath = selectedFile.getAbsolutePath();
            rightChannelLabel.setText(selectedFile.getName());
        }

        File otherChannel = detectOtherChannel(selectedFile, isLeft);
        if (otherChannel != null) {
            if (isLeft) {
                rightChannelPath = otherChannel.getAbsolutePath();
                rightChannelLabel.setText(otherChannel.getName());
            }
            else {
                leftChannelPath = otherChannel.getAbsolutePath();
                leftChannelLabel.setText(otherChannel.getName());
            }
        }
    }

    private File detectOtherChannel(File selectedFile, boolean isLeftSelected) {
        String fileName = selectedFile.getName();
        File parentDir = selectedFile.getParentFile();

        String otherChannelName = null;

        if (fileName.endsWith("_L.dsp") && isLeftSelected) {
            otherChannelName = fileName.replace("_L.dsp", "_R.dsp");
        } else if (fileName.endsWith("_R.dsp") && !isLeftSelected) {
            otherChannelName = fileName.replace("_R.dsp", "_L.dsp");
        } else if (fileName.endsWith("(channel 0).dsp") && isLeftSelected) {
            otherChannelName = fileName.replace("(channel 0).dsp", "(channel 1).dsp");
        } else if (fileName.endsWith("(channel 1).dsp") && !isLeftSelected) {
            otherChannelName = fileName.replace("(channel 1).dsp", "(channel 0).dsp");
        }

        if (otherChannelName != null) {
            File otherChannelFile = new File(parentDir, otherChannelName);
            if (otherChannelFile.exists()) {
                return otherChannelFile;
            }
        }

        return null;
    }

    private void updateSongList() {
        Map<Integer, String> songNameMap = getSongNameMapForSelectedGame();

        if (songNames == null) {
            songNames = new JComboBox<>();
        }

        if (songNameMap != null) {
            ArrayList<String> songList = new ArrayList<>(songNameMap.values());
            Collections.sort(songList);

            songNames.setModel(new DefaultComboBoxModel<>(songList.toArray(new String[0])));
        } else {
            songNames.setModel(new DefaultComboBoxModel<>(new String[]{}));
        }
    }

    private Map<Integer, String> getSongNameMapForSelectedGame() {
        switch (selectedGame) {
            case "Mario Party 5": return MarioPartySongNames.MARIO_PARTY_5_TRACK_NAMES;
            case "Mario Party 6": return MarioPartySongNames.MARIO_PARTY_6_TRACK_NAMES;
            case "Mario Party 7": return MarioPartySongNames.MARIO_PARTY_7_TRACK_NAMES;
            default:
                JOptionPane.showMessageDialog(this, "No game selected.");
                return null;
        }
    }

    private static File getPDTFileName(File pdtFile, String timestamp) {
        String baseName = pdtFile.getName();
        int extIndex = baseName.lastIndexOf(".");
        if (extIndex != -1) {
            baseName = baseName.substring(0, extIndex);
        }

        String backupFileName = baseName + "_Backup_" + timestamp + ".pdt";
        return new File(pdtFile.getParent(), backupFileName);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == pickLeftChannel) {
            chooseLeftChannelPath();
        }

        if (e.getSource() == pickRightChannel) {
            chooseRightChannelPath();
        }

        if (e.getSource() == modifySong) {
            if (leftChannelPath.isEmpty() || rightChannelPath.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Either the left or right channel wasn't chosen!");
                return;
            }

            if (pdtPath.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No PDT file was chosen!");
                return;
            }

            File pdtFile = new File(pdtPath);
            File leftChannelFile = new File(leftChannelPath);
            File rightChannelFile = new File(rightChannelPath);

            if (!pdtFile.exists()) {
                JOptionPane.showMessageDialog(this, "The chosen PDT file doesn't exist!");
                return;
            }

            if (!leftChannelFile.exists() || !rightChannelFile.exists()) {
                JOptionPane.showMessageDialog(this, "Either the left or right channel doesn't exist!");
                return;
            }

            int response = JOptionPane.showConfirmDialog(
                    null,
                    "Do you want to make a backup of the PDT file?",
                    "Backup PDT",
                    JOptionPane.YES_NO_OPTION
            );

            if (response == JOptionPane.YES_OPTION) {
                try {
                    String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                    File backupFile = getPDTFileName(pdtFile, timestamp);

                    Files.copy(pdtFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(null, "Failed to create backup: " + ex.getMessage());
                }
            }

            String selectedSongName = (String) songNames.getSelectedItem();

            Map<Integer, String> songNameMap = getSongNameMapForSelectedGame();

            if (songNameMap == null) {
                JOptionPane.showMessageDialog(this, "No game is selected! Please select one!");
                return;
            }

            int actualSongIndex = -1;

            if (selectedSongName != null) {
                for (Map.Entry<Integer, String> entry : songNameMap.entrySet()) {
                    if (selectedSongName.equals(entry.getValue())) {
                        actualSongIndex = entry.getKey();
                        break;
                    }
                }
            }

            if (actualSongIndex == -1) {
                JOptionPane.showMessageDialog(this, "Could not determine song index.");
                return;
            }

            SongModifier.modifySong(
                    pdtFile,
                    leftChannelFile,
                    rightChannelFile,
                    actualSongIndex,
                    selectedSongName,
                    selectedGame
            );

            JOptionPane.showMessageDialog(null, "Finished modifying PDT file for " + selectedSongName);
        }

        if (e.getSource() == selectGame) {
            initPDTPath();
        }

        if (e.getSource() == addToQueueButton) {
            String songName = (String) songNames.getSelectedItem();
            if (songName == null || leftChannelPath.isEmpty() || rightChannelPath.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please select song and both DSP channels before adding.");
                return;
            }
            jobQueueModel.addElement(new ModifyJob(songName, leftChannelPath, rightChannelPath));
        }

        if (e.getSource() == modifyQueueButton) {
            int selectedIndex = jobQueueList.getSelectedIndex();
            if (selectedIndex == -1) {
                JOptionPane.showMessageDialog(this, "Select a job to modify.");
                return;
            }

            String songName = (String) songNames.getSelectedItem();
            if (songName == null || leftChannelPath.isEmpty() || rightChannelPath.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please select song and both DSP channels before modifying.");
                return;
            }

            jobQueueModel.setElementAt(new ModifyJob(songName, leftChannelPath, rightChannelPath), selectedIndex);
        }

        if (e.getSource() == removeQueueButton) {
            int selectedIndex = jobQueueList.getSelectedIndex();
            if (selectedIndex != -1) {
                jobQueueModel.remove(selectedIndex);
            }
        }

        if (e.getSource() == clearQueueButton) {
            jobQueueModel.clear();
        }

        if (e.getSource() == runBatchButton) {
            if (jobQueueModel.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Queue is empty!");
                return;
            }

            if (pdtPath.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No PDT file was chosen!");
                return;
            }

            File pdtFile = new File(pdtPath);
            if (!pdtFile.exists()) {
                JOptionPane.showMessageDialog(this, "The selected PDT file doesn't exist!");
                return;
            }

            int response = JOptionPane.showConfirmDialog(
                    this,
                    "This will modify the PDT for all jobs in the queue.\nDo you want to back up the PDT file first?",
                    "Backup?",
                    JOptionPane.YES_NO_OPTION
            );

            if (response == JOptionPane.YES_OPTION) {
                try {
                    String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                    File backup = getPDTFileName(pdtFile, timestamp);
                    Files.copy(pdtFile.toPath(), backup.toPath(), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, "Backup failed: " + ex.getMessage());
                }
            }

            Map<Integer, String> songMap = getSongNameMapForSelectedGame();
            if (songMap == null) return;

            for (int i = 0; i < jobQueueModel.size(); i++) {
                ModifyJob modifyJob = jobQueueModel.getElementAt(i);

                int songIndex = -1;
                for (Map.Entry<Integer, String> entry : songMap.entrySet()) {
                    if (entry.getValue().equals(modifyJob.getSongName())) {
                        songIndex = entry.getKey();
                        break;
                    }
                }

                if (songIndex == -1) {
                    JOptionPane.showMessageDialog(this, "Song \"" + modifyJob.getSongName() + "\" not found in song map.");
                    continue;
                }

                File leftDSP = new File(modifyJob.getLeftDSP());
                File rightDSP = new File(modifyJob.getRightDSP());

                if (!leftDSP.exists() || !rightDSP.exists()) {
                    JOptionPane.showMessageDialog(this, "DSP files for \"" + modifyJob.getSongName() + "\" not found. Skipping.");
                    continue;
                }

                SongModifier.modifySong(
                        pdtFile,
                        leftDSP,
                        rightDSP,
                        songIndex,
                        modifyJob.getSongName(),
                        selectedGame
                );
            }

            JOptionPane.showMessageDialog(this, "Batch process completed.");
            jobQueueModel.clear();
        }
    }
}