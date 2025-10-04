package ui;

import constants.MarioPartySongNames;
import io.music.SNGLoopAdder;
import io.music.SongDumper;
import io.music.SongModifier;
import io.sound.*;
import uihelpers.DSPPair;
import uihelpers.ModifyJob;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.*;

public class MarioPartyAudioEditorUI extends JFrame implements ActionListener {

    private JButton pickLeftChannel, pickRightChannel, modifySong, dumpAllSongs, dumpAllMP4SequencedSongs, modifyMP4SequencedSong, dumpSoundBank, dumpAllSoundBanks, modifySoundBank, fixSoundDSPHeader, fixSoundDSPHeaderFolder, padSoundDSP, padSoundDSPs, selectGame;
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
    private JLabel defaultMSMFileLabel;
    private JLabel defaultDumpFolderLabel;

    private File defaultSavedDSPFolder = null;
    private File defaultPDTFile = null;
    private File defaultMSMFile = null;
    private File defaultDumpOutputFolder = null;

    private DefaultListModel<ModifyJob> jobQueueModel;
    private JList<ModifyJob> jobQueueList;
    private JButton addToQueueButton, removeQueueButton, clearQueueButton, runBatchButton;

    private JCheckBox dumpProjPool = null;
    private JCheckBox padSoundOnModify = null;


    public MarioPartyAudioEditorUI() {
        setTitle("Mario Party GameCube Audio Editor");
        initSettingsFile();
        loadSettingsFile();
        generateUI();
    }

    private void generateUI() {
        JTabbedPane tabbedPane = new JTabbedPane();

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

        songSearchField.getDocument().addDocumentListener(new DocumentListener() {
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
            public void insertUpdate(DocumentEvent e) {
                filterSongs();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filterSongs();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filterSongs();
            }
        });

        JPanel songPanel = new JPanel(new GridBagLayout());
        songPanel.setBorder(BorderFactory.createTitledBorder("Dump/Modify Song"));
        GridBagConstraints songGBC = new GridBagConstraints();
        songGBC.insets = new Insets(5, 5, 5, 5);
        songGBC.fill = GridBagConstraints.HORIZONTAL;

        pickLeftChannel = new JButton("Select Left DSP Channel");
        pickLeftChannel.addActionListener(this);
        leftChannelLabel = new JLabel("No file selected");

        pickRightChannel = new JButton("Select Right DSP Channel");
        pickRightChannel.addActionListener(this);
        rightChannelLabel = new JLabel("No file selected");

        dumpAllSongs = new JButton("Dump All Songs");
        dumpAllSongs.addActionListener(this);

        dumpAllMP4SequencedSongs = new JButton("Dump All Sequenced Songs");
        dumpAllMP4SequencedSongs.addActionListener(this);

        modifyMP4SequencedSong = new JButton("Modify Sequenced Song");
        modifyMP4SequencedSong.addActionListener(this);

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

        songGBC.gridx = 0; songGBC.gridy = 3;
        songGBC.gridwidth = 2;
        songPanel.add(dumpAllSongs, songGBC);

        selectGame = new JButton("Select PDT and Game");
        selectGame.addActionListener(this);

        if (defaultPDTFile == null || !defaultPDTFile.exists()) {
            pdtFilePathLabel = new JLabel("No PDT file selected");
            selectedGameLabel = new JLabel("No game selected");
        }

        songGBC.gridx = 0; songGBC.gridy = 4;
        songGBC.gridwidth = 2;
        songPanel.add(selectGame, songGBC);

        songGBC.gridy = 5;
        songPanel.add(pdtFilePathLabel, songGBC);

        songGBC.gridy = 6;
        songPanel.add(selectedGameLabel, songGBC);

        songToolsPanel.add(songSelectionPanel);
        songToolsPanel.add(Box.createVerticalStrut(10));
        songToolsPanel.add(songPanel);

        tabbedPane.addTab("Streamed Audio Tools (PDT)", songToolsPanel);

        JPanel queuePanel = new JPanel(new BorderLayout());
        queuePanel.setBorder(BorderFactory.createTitledBorder("Batch Modification Job Queue"));

        jobQueueModel = new DefaultListModel<>();
        jobQueueList = new JList<>(jobQueueModel);
        jobQueueList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(jobQueueList);

        JPanel queueButtonPanel = new JPanel(new GridLayout(1, 4, 5, 5));
        addToQueueButton = new JButton("Add");
        removeQueueButton = new JButton("Remove");
        clearQueueButton = new JButton("Clear All");
        runBatchButton = new JButton("Run Batch");

        addToQueueButton.addActionListener(this);
        removeQueueButton.addActionListener(this);
        clearQueueButton.addActionListener(this);
        runBatchButton.addActionListener(this);

        queueButtonPanel.add(addToQueueButton);
        queueButtonPanel.add(removeQueueButton);
        queueButtonPanel.add(clearQueueButton);
        queueButtonPanel.add(runBatchButton);

        queuePanel.add(scrollPane, BorderLayout.CENTER);
        queuePanel.add(queueButtonPanel, BorderLayout.SOUTH);

        songToolsPanel.add(Box.createVerticalStrut(10));
        songToolsPanel.add(queuePanel);

        JPanel mp4SeqAudioPanel = new JPanel();
        mp4SeqAudioPanel.setLayout(new BoxLayout(mp4SeqAudioPanel, BoxLayout.Y_AXIS));

        JPanel mp4Panel = new JPanel(new GridBagLayout());
        mp4Panel.setBorder(BorderFactory.createTitledBorder("Dump/Modify Sequenced Song"));
        GridBagConstraints mp4GBC = new GridBagConstraints();
        mp4GBC.insets = new Insets(5, 5, 5, 5);
        mp4GBC.fill = GridBagConstraints.HORIZONTAL;

        mp4GBC.gridx = 0; mp4GBC.gridy = 0;
        mp4GBC.gridwidth = 1;
        mp4Panel.add(modifyMP4SequencedSong, mp4GBC);

        songGBC.gridx = 0; songGBC.gridy = 1;
        songGBC.gridwidth = 1;
        mp4Panel.add(dumpAllMP4SequencedSongs, songGBC);

        mp4SeqAudioPanel.add(mp4Panel);
        tabbedPane.addTab("Sequenced Audio Tools (MSM) (Mario Party 4 only)", mp4SeqAudioPanel);

        JPanel soundToolsPanel = new JPanel();
        soundToolsPanel.setLayout(new BoxLayout(soundToolsPanel, BoxLayout.Y_AXIS));

        JPanel soundBankPanel = new JPanel(new GridBagLayout());
        soundBankPanel.setBorder(BorderFactory.createTitledBorder("Dump/Modify Sound Bank"));
        GridBagConstraints soundBankGBC = new GridBagConstraints();
        soundBankGBC.insets = new Insets(5, 5, 5, 5);
        soundBankGBC.fill = GridBagConstraints.HORIZONTAL;

        soundBankGBC.gridx = 0;
        soundBankGBC.gridy = 0;
        dumpSoundBank = new JButton("Dump Sound Bank");
        dumpSoundBank.addActionListener(this);
        soundBankPanel.add(dumpSoundBank, soundBankGBC);

        soundBankGBC.gridy = 1;
        dumpAllSoundBanks = new JButton("Dump All Sound Banks");
        dumpAllSoundBanks.addActionListener(this);
        soundBankPanel.add(dumpAllSoundBanks, soundBankGBC);

        soundBankGBC.gridy = 2;
        modifySoundBank = new JButton("Modify Sound Bank");
        modifySoundBank.addActionListener(this);
        soundBankPanel.add(modifySoundBank, soundBankGBC);

        soundBankGBC.gridy = 3;
        dumpProjPool = new JCheckBox("Dump .proj and .pool files (not needed for modding)");
        soundBankPanel.add(dumpProjPool, soundBankGBC);

        soundBankGBC.gridy = 4;
        padSoundOnModify = new JCheckBox("Pad .samp and .sdir to original file size on modify (this just writes padding bytes after the replacements in the MSM to match the original file size)");
        padSoundOnModify.setSelected(true);
        soundBankPanel.add(padSoundOnModify, soundBankGBC);

        JPanel soundDSPPanel = new JPanel(new GridBagLayout());
        soundDSPPanel.setBorder(BorderFactory.createTitledBorder("Fix Sound DSP"));
        GridBagConstraints soundDSPGBC = new GridBagConstraints();
        soundDSPGBC.insets = new Insets(5, 5, 5, 5);
        soundDSPGBC.fill = GridBagConstraints.HORIZONTAL;

        soundDSPGBC.gridx = 0;
        soundDSPGBC.gridy = 0;
        fixSoundDSPHeader = new JButton("Fix Nonlooping Sound DSP Header");
        fixSoundDSPHeader.addActionListener(this);
        soundDSPPanel.add(fixSoundDSPHeader, soundDSPGBC);

        soundDSPGBC.gridy = 1;
        fixSoundDSPHeaderFolder = new JButton("Fix Nonlooping Sound DSP Headers (Folder)");
        fixSoundDSPHeaderFolder.addActionListener(this);
        soundDSPPanel.add(fixSoundDSPHeaderFolder, soundDSPGBC);

        soundDSPGBC.gridy = 2;
        padSoundDSP = new JButton("Pad Sound DSP Filesize");
        padSoundDSP.addActionListener(this);
        soundDSPPanel.add(padSoundDSP, soundDSPGBC);

        soundDSPGBC.gridy = 3;
        padSoundDSPs = new JButton("Pad Sound DSP Filesizes (Folder)");
        padSoundDSPs.addActionListener(this);
        soundDSPPanel.add(padSoundDSPs, soundDSPGBC);

        soundToolsPanel.add(soundBankPanel);
        soundToolsPanel.add(Box.createVerticalStrut(10));
        soundToolsPanel.add(soundDSPPanel);

        tabbedPane.addTab("Sound Tools (MSM)", soundToolsPanel);


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

        settingsGBC.gridx = 0;
        settingsGBC.gridy = 1;
        settingsPanel.add(new JLabel("Default Dump Output Folder:"), settingsGBC);

        defaultDumpFolderLabel = new JLabel(defaultDumpOutputFolder != null ? defaultDumpOutputFolder.getAbsolutePath() : "None");
        settingsGBC.gridx = 1;
        settingsPanel.add(defaultDumpFolderLabel, settingsGBC);

        JButton chooseDefaultDumpButton = new JButton("Change");
        chooseDefaultDumpButton.addActionListener(e -> chooseDefaultDumpFolder());
        settingsGBC.gridx = 2;
        settingsPanel.add(chooseDefaultDumpButton, settingsGBC);

        defaultPDTFileLabel = new JLabel(defaultPDTFile != null ? defaultPDTFile.getAbsolutePath() : "None");

        settingsGBC.gridx = 0;
        settingsGBC.gridy = 2;
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
                    case "mpgcstr.pdt": selectedGame = "Mario Party 4"; break;
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

        defaultMSMFileLabel = new JLabel(defaultMSMFile != null ? defaultMSMFile.getAbsolutePath() : "None");

        settingsGBC.gridx = 0;
        settingsGBC.gridy = 3;
        settingsGBC.gridwidth = 1;
        settingsPanel.add(new JLabel("Default MSM File:"), settingsGBC);

        settingsGBC.gridx = 1;
        settingsPanel.add(defaultMSMFileLabel, settingsGBC);

        JButton chooseDefaultMSMButton = new JButton("Change");
        chooseDefaultMSMButton.addActionListener(e -> {
            JFileChooser msmChooser = new JFileChooser();
            msmChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            msmChooser.setDialogTitle("Select Default MSM File");
            FileNameExtensionFilter pdtFilter = new FileNameExtensionFilter("MSM Files", "msm");
            msmChooser.setFileFilter(pdtFilter);
            msmChooser.setAcceptAllFileFilterUsed(false);

            int result = msmChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                defaultMSMFile = msmChooser.getSelectedFile();
                defaultMSMFileLabel.setText(defaultMSMFile.getAbsolutePath());
                saveSettingsToFile();
            }
        });
        settingsGBC.gridx = 2;
        settingsPanel.add(chooseDefaultMSMButton, settingsGBC);

        JButton resetSettingsButton = new JButton("Reset Settings");
        resetSettingsButton.addActionListener(e -> resetSettings());
        settingsGBC.gridx = 0;
        settingsGBC.gridy = 4;
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

            outputStream.println("defaultSavedDSPFolder:None");
            outputStream.println("defaultPDTFile:None");
            outputStream.println("defaultMSMFile:None");
            outputStream.println("defaultDumpOutputFolder:None");
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
                    case "defaultMSMFile":
                        if (!value.equals("None")) defaultMSMFile = new File(value);
                        break;
                    case "defaultDumpOutputFolder":
                        if (!value.equals("None")) defaultDumpOutputFolder = new File(value);
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
                    case "mpgcstr.pdt": selectedGame = "Mario Party 4"; break;
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

    private void chooseDefaultDumpFolder() {
        JFileChooser defaultDumpFolderChooser = new JFileChooser();
        defaultDumpFolderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        defaultDumpFolderChooser.setAcceptAllFileFilterUsed(false);
        int result = defaultDumpFolderChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            defaultDumpOutputFolder = defaultDumpFolderChooser.getSelectedFile();
            defaultDumpFolderLabel.setText(defaultDumpOutputFolder.getAbsolutePath());
            saveSettingsToFile();
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
            writer.println("defaultMSMFile:" + (defaultMSMFile != null ? defaultMSMFile.getAbsolutePath() : "None"));
            writer.println("defaultDumpOutputFolder:" + (defaultDumpOutputFolder != null ? defaultDumpOutputFolder.getAbsolutePath() : "None"));
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

        defaultMSMFile = null;

        if (defaultMSMFileLabel != null) {
            defaultMSMFileLabel.setText("None");
        }

        defaultDumpOutputFolder = null;

        if (defaultDumpFolderLabel != null) {
            defaultDumpFolderLabel.setText("None");
        }

        try (PrintWriter writer = new PrintWriter(new FileOutputStream("settings.txt"))) {
            writer.println("defaultSavedDSPFolder:None");
            writer.println("defaultPDTFile:None");
            writer.println("defaultMSMFile:None");
            writer.println("defaultDumpOutputFolder:None");
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

        if (pdtFileName.equals("mpgcstr.pdt")) {
            selectedGame = "Mario Party 4";
        } else if (pdtFileName.equals("mp5_str.pdt")) {
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
                        new String[]{"Mario Party 4", "Mario Party 5", "Mario Party 6", "Mario Party 7"},
                        "Mario Party 4"
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
            case "Mario Party 4": return MarioPartySongNames.MARIO_PARTY_4_TRACK_NAMES;
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

    private static File getMSMFileName(File msmFile, String timestamp) {
        String baseName = msmFile.getName();
        int extIndex = baseName.lastIndexOf(".");
        if (extIndex != -1) {
            baseName = baseName.substring(0, extIndex);
        }

        String backupFileName = baseName + "_Backup_" + timestamp + ".msm";
        return new File(msmFile.getParent(), backupFileName);
    }

    private void backupPDT(File pdtFile) {
        try {
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            File backupFile = getPDTFileName(pdtFile, timestamp);

            Files.copy(pdtFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Failed to create backup: " + ex.getMessage());
        }
    }

    private void backupMSM(File msmFile) {
        try {
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            File backupFile = getMSMFileName(msmFile, timestamp);

            Files.copy(msmFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Failed to create backup: " + ex.getMessage());
        }
    }

    private boolean isMarioParty4(File msmFile) {
        String msmFileName = msmFile.getName().toLowerCase();
        return msmFileName.equals("mpgcsnd.msm");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == pickLeftChannel) {
            chooseLeftChannelPath();
        }

        if (e.getSource() == pickRightChannel) {
            chooseRightChannelPath();
        }

        if (e.getSource() == dumpSoundBank) {

            File selectedMSM;

            if (defaultMSMFile == null || !defaultMSMFile.exists()) {
                JFileChooser msmFileChooser = new JFileChooser();
                msmFileChooser.setDialogTitle("Select MSM file");
                msmFileChooser.setAcceptAllFileFilterUsed(false);

                FileNameExtensionFilter msmFilter = new FileNameExtensionFilter("MSM Files", "msm");
                msmFileChooser.setFileFilter(msmFilter);

                int userSelection = msmFileChooser.showOpenDialog(null);

                if (userSelection != JFileChooser.APPROVE_OPTION) {
                    return;
                }
                else {
                    selectedMSM = msmFileChooser.getSelectedFile();
                }
            }

            else {
                selectedMSM = defaultMSMFile;
            }

            ArrayList<String> banks = SoundBankGetter.getBanks(selectedMSM);

            if (banks != null) {
                String[] bankArray = banks.toArray(new String[0]);
                JComboBox<String> bankDropdown = new JComboBox<>(bankArray);

                int result = JOptionPane.showConfirmDialog(
                        null,
                        bankDropdown,
                        "Select a Bank",
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                );

                if (result == JOptionPane.OK_OPTION) {
                    String selectedBank = (String) bankDropdown.getSelectedItem();

                    if (selectedBank != null) {
                        SoundDumper.dumpSoundBank(selectedMSM, Long.parseLong(selectedBank), defaultDumpOutputFolder, dumpProjPool.isSelected());
                    }
                    else {
                        return;
                    }
                }
            }
        }

        if (e.getSource() == dumpAllSoundBanks) {
            File selectedMSM;

            if (defaultMSMFile == null || !defaultMSMFile.exists()) {
                JFileChooser msmFileChooser = new JFileChooser();
                msmFileChooser.setDialogTitle("Select MSM file");
                msmFileChooser.setAcceptAllFileFilterUsed(false);

                FileNameExtensionFilter msmFilter = new FileNameExtensionFilter("MSM Files", "msm");
                msmFileChooser.setFileFilter(msmFilter);

                int userSelection = msmFileChooser.showOpenDialog(null);

                if (userSelection != JFileChooser.APPROVE_OPTION) {
                    return;
                }
                else {
                    selectedMSM = msmFileChooser.getSelectedFile();
                }
            }

            else {
                selectedMSM = defaultMSMFile;
            }

            SoundDumper.dumpAllSounds(selectedMSM, defaultDumpOutputFolder, dumpProjPool.isSelected());
        }

        if (e.getSource() == modifySoundBank) {
            File selectedMSM;
            int userSelection;

            if (defaultMSMFile == null || !defaultMSMFile.exists()) {
                JFileChooser msmFileChooser = new JFileChooser();
                msmFileChooser.setDialogTitle("Select MSM file");
                msmFileChooser.setAcceptAllFileFilterUsed(false);

                FileNameExtensionFilter msmFilter = new FileNameExtensionFilter("MSM Files", "msm");
                msmFileChooser.setFileFilter(msmFilter);

                userSelection = msmFileChooser.showOpenDialog(null);

                if (userSelection != JFileChooser.APPROVE_OPTION) {
                    return;
                }
                else {
                    selectedMSM = msmFileChooser.getSelectedFile();
                }
            }

            else {
                selectedMSM = defaultMSMFile;
            }

            JFileChooser sdirFileChooser = new JFileChooser();
            sdirFileChooser.setDialogTitle("Select Modified SDIR file");
            sdirFileChooser.setAcceptAllFileFilterUsed(false);

            FileNameExtensionFilter sdirFilter = new FileNameExtensionFilter("SDIR Files", "sdir");
            sdirFileChooser.setFileFilter(sdirFilter);

            userSelection = sdirFileChooser.showOpenDialog(null);

            if (userSelection != JFileChooser.APPROVE_OPTION) {
                return;
            }

            JFileChooser sampFileChooser = new JFileChooser();
            sampFileChooser.setDialogTitle("Select Modified SAMP file");
            sampFileChooser.setAcceptAllFileFilterUsed(false);

            FileNameExtensionFilter sampFilter = new FileNameExtensionFilter("SAMP Files", "samp");
            sampFileChooser.setFileFilter(sampFilter);

            userSelection = sampFileChooser.showOpenDialog(null);

            if (userSelection != JFileChooser.APPROVE_OPTION) {
                return;
            }

            File selectedSDIR = sdirFileChooser.getSelectedFile();
            File selectedSAMP = sampFileChooser.getSelectedFile();

            ArrayList<String> banks = SoundBankGetter.getBanks(selectedMSM);

            if (banks != null) {
                String[] bankArray = banks.toArray(new String[0]);
                JComboBox<String> bankDropdown = new JComboBox<>(bankArray);

                int result = JOptionPane.showConfirmDialog(
                        null,
                        bankDropdown,
                        "Select a Bank",
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                );

                if (result == JOptionPane.OK_OPTION) {
                    String selectedBank = (String) bankDropdown.getSelectedItem();

                    if (selectedBank != null) {
                        boolean isCorrectBank = selectedSDIR.getName().equals(selectedBank + ".sdir") && selectedSAMP.getName().equals(selectedBank + ".samp");
                        if (isCorrectBank) {
                            int response = JOptionPane.showConfirmDialog(
                                    null,
                                    "Do you want to make a backup of the MSM file?",
                                    "Backup MSM",
                                    JOptionPane.YES_NO_OPTION
                            );

                            if (response == JOptionPane.YES_OPTION) {
                                backupMSM(selectedMSM);
                            }

                            SoundModifier.modifySoundBank(selectedMSM, selectedSDIR, selectedSAMP, Long.parseLong(selectedBank), padSoundOnModify.isSelected());
                        }
                        else {
                            JOptionPane.showMessageDialog(this, "Your modified sdir or samp are not for the correct sound bank");
                        }
                    }

                }
            }
        }

        if (e.getSource() == fixSoundDSPHeader) {
            int response = JOptionPane.showConfirmDialog(
                    null,
                    "Only use this if you have an intended non looping sound that is looping in game.\nAre you sure you want to continue?",
                    "Continue?",
                    JOptionPane.YES_NO_OPTION
            );

            if (response != JOptionPane.YES_OPTION) {
                return;
            }

            JFileChooser dspFileChooser = new JFileChooser();
            dspFileChooser.setDialogTitle("Select Sound DSP file");
            dspFileChooser.setAcceptAllFileFilterUsed(false);

            FileNameExtensionFilter dspFilter = new FileNameExtensionFilter("DSP Files", "dsp");
            dspFileChooser.setFileFilter(dspFilter);

            int userSelection = dspFileChooser.showOpenDialog(null);

            if (userSelection != JFileChooser.APPROVE_OPTION) {
                return;
            }

            File selectedDSP = dspFileChooser.getSelectedFile();

            FixDSPSoundHeader.fixHeader(selectedDSP);
            JOptionPane.showMessageDialog(this, "Header has been fixed!");
        }

        if (e.getSource() == fixSoundDSPHeaderFolder) {
            int response = JOptionPane.showConfirmDialog(
                    null,
                    "Only use this if you have a folder of intended non looping sounds that are looping in game.\nAre you sure you want to continue?",
                    "Continue?",
                    JOptionPane.YES_NO_OPTION
            );

            if (response != JOptionPane.YES_OPTION) {
                return;
            }

            JFileChooser dspFolderChooser = new JFileChooser();
            dspFolderChooser.setDialogTitle("Select Sound DSP folder");
            dspFolderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            dspFolderChooser.setAcceptAllFileFilterUsed(false);

            int userSelection = dspFolderChooser.showOpenDialog(null);

            if (userSelection != JFileChooser.APPROVE_OPTION) {
                return;
            }

            File selectedDSPFolder = dspFolderChooser.getSelectedFile();

            File[] files = selectedDSPFolder.listFiles((_, name) -> name.toLowerCase().endsWith(".dsp"));
            if (files == null) return;

            for (int i=0; i<files.length; i++) {
                FixDSPSoundHeader.fixHeader(files[i]);
            }

            JOptionPane.showMessageDialog(this, "Headers have been fixed!");
        }

        if (e.getSource() == padSoundDSP) {
            int response = JOptionPane.showConfirmDialog(
                    null,
                    "Only use this if you're padding a sound DSP. Music has its own system that is taken care of already.\nThis is to prevent issues (haven't seen any with Mario Party, but it's here in case)\nThis will pad your replacement to the exact size as the original.\nThus, you will have to rename your file to what the original name was at this point.\nAre you sure you want to continue?",
                    "Continue?",
                    JOptionPane.YES_NO_OPTION
            );

            if (response != JOptionPane.YES_OPTION) {
                return;
            }

            JFileChooser oldDSPFileChooser = new JFileChooser();
            oldDSPFileChooser.setDialogTitle("Select unmodified sound DSP file");
            oldDSPFileChooser.setAcceptAllFileFilterUsed(false);

            FileNameExtensionFilter dspFilter = new FileNameExtensionFilter("DSP Files", "dsp");
            oldDSPFileChooser.setFileFilter(dspFilter);

            int userSelection = oldDSPFileChooser.showOpenDialog(null);

            if (userSelection != JFileChooser.APPROVE_OPTION) {
                return;
            }

            JFileChooser newDSPFileChooser = new JFileChooser();
            newDSPFileChooser.setDialogTitle("Select modified sound DSP file");
            newDSPFileChooser.setAcceptAllFileFilterUsed(false);

            newDSPFileChooser.setFileFilter(dspFilter);

            userSelection = newDSPFileChooser.showOpenDialog(null);

            if (userSelection != JFileChooser.APPROVE_OPTION) {
                return;
            }

            File oldDSPFile = oldDSPFileChooser.getSelectedFile();
            File newDSPFile = newDSPFileChooser.getSelectedFile();

            if (oldDSPFile == null || newDSPFile == null) {
                return;
            }

            boolean fixHeader = false;

            response = JOptionPane.showConfirmDialog(
                    null,
                    "Do you also want to fix the header?\nOnly say yes to this if your sound isn't meant to loop.\nAre you sure you want to continue?",
                    "Continue?",
                    JOptionPane.YES_NO_OPTION
            );

            if (response == JOptionPane.YES_OPTION) {
                fixHeader = true;
            }

            boolean paddedFile = DSPSoundPadder.padSoundDSP(oldDSPFile, newDSPFile, fixHeader);

            if (paddedFile) {
                JOptionPane.showMessageDialog(this, "DSP file has been padded!");
            }
        }

        if (e.getSource() == padSoundDSPs) {
            int response = JOptionPane.showConfirmDialog(
                    null,
                    "Only use this if you're padding sound DSPs. Music has its own system that is taken care of already.\nThis is to prevent issues (haven't seen any with Mario Party, but it's here in case)\nThis will pad your replacements to the exact size as the originals.\nThus, you will have to rename your files to what the original name was at this point.\nAre you sure you want to continue?",
                    "Continue?",
                    JOptionPane.YES_NO_OPTION
            );

            if (response != JOptionPane.YES_OPTION) {
                return;
            }

            JFileChooser oldDSPFolderChooser = new JFileChooser();
            oldDSPFolderChooser.setDialogTitle("Select the unmodified sound DSP folder");
            oldDSPFolderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            oldDSPFolderChooser.setAcceptAllFileFilterUsed(false);

            int userSelection = oldDSPFolderChooser.showOpenDialog(null);

            if (userSelection != JFileChooser.APPROVE_OPTION) {
                return;
            }

            JFileChooser newDSPFolderChooser = new JFileChooser();
            newDSPFolderChooser.setDialogTitle("Select the modified sound DSP folder");
            newDSPFolderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            newDSPFolderChooser.setAcceptAllFileFilterUsed(false);

            userSelection = newDSPFolderChooser.showOpenDialog(null);

            if (userSelection != JFileChooser.APPROVE_OPTION) {
                return;
            }

            File oldDSPFolder = oldDSPFolderChooser.getSelectedFile();
            File newDSPFolder = newDSPFolderChooser.getSelectedFile();

            File[] oldFiles = oldDSPFolder.listFiles((_, name) -> name.toLowerCase().endsWith(".dsp"));
            File[] newFiles = newDSPFolder.listFiles((_, name) -> name.toLowerCase().endsWith(".dsp"));

            if (oldFiles == null || newFiles == null) {
                return;
            }

            boolean fixHeaders = false;

            response = JOptionPane.showConfirmDialog(
                    null,
                    "Do you also want to fix the headers?\nOnly say yes to this if your sounds aren't meant to loop.\nAre you sure you want to continue?",
                    "Continue?",
                    JOptionPane.YES_NO_OPTION
            );

            if (response == JOptionPane.YES_OPTION) {
                fixHeaders = true;
            }

            boolean paddedFiles = DSPSoundPadder.padSoundDSPs(oldFiles, newFiles, fixHeaders);

            if (paddedFiles) {
                JOptionPane.showMessageDialog(this, "DSP files have been padded!");
            }
        }

        if (e.getSource() == dumpAllSongs) {

            File pdtFile;

            if (pdtPath.isEmpty()) {
                JFileChooser pdtFileChooser = new JFileChooser();
                pdtFileChooser.setDialogTitle("Select PDT file");
                pdtFileChooser.setAcceptAllFileFilterUsed(false);

                FileNameExtensionFilter pdtFilter = new FileNameExtensionFilter("PDT Files", "pdt");
                pdtFileChooser.setFileFilter(pdtFilter);

                int userSelection = pdtFileChooser.showOpenDialog(null);

                if (userSelection != JFileChooser.APPROVE_OPTION) {
                    return;
                }

                pdtFile = pdtFileChooser.getSelectedFile();
            }
            else {
                pdtFile = new File(pdtPath);
            }

            if (!pdtFile.exists()) {
                JOptionPane.showMessageDialog(this, "The chosen PDT file doesn't exist!");
                return;
            }

            if (defaultDumpOutputFolder != null && !defaultDumpOutputFolder.exists()) {
                defaultDumpOutputFolder = null;
            }

            SongDumper.dumpAllSongs(pdtFile, defaultDumpOutputFolder);
        }

        if (e.getSource() == dumpAllMP4SequencedSongs) {
            JFileChooser msmFileChooser = new JFileChooser();
            msmFileChooser.setDialogTitle("Select MSM file");
            msmFileChooser.setAcceptAllFileFilterUsed(false);

            FileNameExtensionFilter msmFilter = new FileNameExtensionFilter("MSM Files", "msm");
            msmFileChooser.setFileFilter(msmFilter);

            int userSelection = msmFileChooser.showOpenDialog(null);

            if (userSelection != JFileChooser.APPROVE_OPTION) {
                return;
            }

            File msmFile = msmFileChooser.getSelectedFile();

            if (defaultDumpOutputFolder != null && !defaultDumpOutputFolder.exists()) {
                defaultDumpOutputFolder = null;
            }

            if (!isMarioParty4(msmFile)) {
                JOptionPane.showMessageDialog(this, "This option is only designed for Mario Party 4! Please provide a MSM from that game!");
            }

            if (msmFile.exists()) {
                SongDumper.dumpMarioParty4SequencedSongs(msmFile, defaultDumpOutputFolder);
            }
        }

        if (e.getSource() == modifyMP4SequencedSong) {
            JFileChooser msmFileChooser = new JFileChooser();
            msmFileChooser.setDialogTitle("Select MSM file");
            msmFileChooser.setAcceptAllFileFilterUsed(false);

            FileNameExtensionFilter msmFilter = new FileNameExtensionFilter("MSM Files", "msm");
            msmFileChooser.setFileFilter(msmFilter);

            int userSelection = msmFileChooser.showOpenDialog(null);

            if (userSelection != JFileChooser.APPROVE_OPTION) {
                return;
            }

            JFileChooser sngFileChooser = new JFileChooser();
            sngFileChooser.setDialogTitle("Select SNG file");
            sngFileChooser.setAcceptAllFileFilterUsed(false);

            FileNameExtensionFilter sngFilter = new FileNameExtensionFilter("SNG Files", "sng");
            sngFileChooser.setFileFilter(sngFilter);

            userSelection = sngFileChooser.showOpenDialog(null);

            if (userSelection != JFileChooser.APPROVE_OPTION) {
                return;
            }

            File msmFile = msmFileChooser.getSelectedFile();
            File sngFile = sngFileChooser.getSelectedFile();

            String[] songOptions = MarioPartySongNames.MARIO_PARTY_4_SEQUENCED_TRACK_NAMES.values().toArray(new String[0]);
            Arrays.sort(songOptions);

            String selectedSongName = (String) JOptionPane.showInputDialog(
                    null,
                    "Select a song to replace:",
                    "Choose Song",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    songOptions,
                    songOptions[0]
            );

            if (selectedSongName == null) {
                return;
            }

            int actualSongIndex = -1;

            for (Map.Entry<Integer, String> entry : MarioPartySongNames.MARIO_PARTY_4_SEQUENCED_TRACK_NAMES.entrySet()) {
                if (selectedSongName.equals(entry.getValue())) {
                    actualSongIndex = entry.getKey();
                    break;
                }
            }

            if (actualSongIndex == -1) {
                JOptionPane.showMessageDialog(this, "Could not determine song index.");
                return;
            }

            if (!isMarioParty4(msmFile)) {
                JOptionPane.showMessageDialog(this, "This option is only designed for Mario Party 4! Please provide a MSM from that game!");
            }

            int response = JOptionPane.showConfirmDialog(
                    null,
                    "Do you want to make a backup of the MSM file?",
                    "Backup MSM",
                    JOptionPane.YES_NO_OPTION
            );

            if (response == JOptionPane.YES_OPTION) {
                backupMSM(msmFile);
            }

            response = JOptionPane.showConfirmDialog(
                    null,
                    "Do you want to make your SNG file loop?\nNote that I haven't figured out how to make it loop 100% correctly, so one of the tracks has to drop after a full loop and it loop the song start to finish\nAre you sure you want to add looping?",
                    "SNG File Loop",
                    JOptionPane.YES_NO_OPTION
            );

            if (response == JOptionPane.YES_OPTION) {
                SNGLoopAdder.loopSNG(sngFile);
            }

            if (msmFile.exists() && sngFile.exists()) {
                SongModifier.replaceMarioParty4SequencedSong(msmFile, sngFile, actualSongIndex);
            }
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
                backupPDT(pdtFile);
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

            boolean modifySuccessful = SongModifier.modifySong(
                    pdtFile,
                    leftChannelFile,
                    rightChannelFile,
                    actualSongIndex,
                    selectedSongName,
                    selectedGame
            );

            if (modifySuccessful) {
                JOptionPane.showMessageDialog(null, "Finished modifying PDT file for " + selectedSongName);
            }
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
                backupPDT(pdtFile);
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
                    JOptionPane.showMessageDialog(this, modifyJob.getSongName() + " not found in song map.");
                    continue;
                }

                File leftDSP = new File(modifyJob.getLeftDSP());
                File rightDSP = new File(modifyJob.getRightDSP());

                if (!leftDSP.exists() || !rightDSP.exists()) {
                    JOptionPane.showMessageDialog(this, "DSP files for " + modifyJob.getSongName() + " not found. Skipping.");
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