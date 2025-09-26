package ui;

import constants.MarioPartySongNames;
import io.DSPPair;
import io.SongDumper;
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

    private JButton pickLeftChannel, pickRightChannel, dumpSong, modifySong, selectGame;
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
    private JLabel defaultDumpFolderLabel;

    private File defaultSavedDSPFolder = null;
    private File defaultDumpOutputFolder = null;

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

        pdtFilePathLabel = new JLabel("No PDT file selected");
        selectedGameLabel = new JLabel("No game selected");

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
        songNames = new JComboBox<>();

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

        dumpSong = new JButton("Dump Selected Song");
        dumpSong.addActionListener(this);

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
        songPanel.add(dumpSong, songGBC);

        songToolsPanel.add(songSelectionPanel);
        songToolsPanel.add(Box.createVerticalStrut(10));
        songToolsPanel.add(songPanel);

        tabbedPane.addTab("Song Tools", songToolsPanel);

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
            outputStream.println("defaultDumpOutputFolder" + ":" + "None");
            outputStream.close();
        }
    }

    private void loadSettingsFile() {
        File settingsFile = new File("settings.txt");
        Scanner inputStream;

        try {
            inputStream = new Scanner(new FileInputStream(settingsFile));
        }
        catch (FileNotFoundException e) {
            return;
        }

        while (inputStream.hasNextLine()) {
            String line = inputStream.nextLine();
            String folderPath = line.split(":", 2)[1];
            if (line.split(":")[0].equals("defaultSavedDSPFolder") && !folderPath.equals("None")) {
                defaultSavedDSPFolder = new File(folderPath);
            }
            if (line.split(":")[0].equals("defaultDumpOutputFolder") && !folderPath.equals("None")) {
                defaultDumpOutputFolder = new File(folderPath);
            }
        }

        if (defaultSavedDSPFolder != null) {
            savedDSPFolder = defaultSavedDSPFolder;
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
            saveSettingsToFile();
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

    private void saveSettingsToFile() {
        try (PrintWriter writer = new PrintWriter(new FileOutputStream("settings.txt"))) {
            writer.println("defaultSavedDSPFolder:" + (defaultSavedDSPFolder != null ? defaultSavedDSPFolder.getAbsolutePath() : "None"));
            writer.println("defaultDumpOutputFolder:" + (defaultDumpOutputFolder != null ? defaultDumpOutputFolder.getAbsolutePath() : "None"));
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to save settings: " + e.getMessage());
        }
    }

    private void resetSettings() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to reset all settings?",
                "Confirm Reset",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        defaultSavedDSPFolder = null;
        defaultDumpOutputFolder = null;

        // Update the labels in the UI
        if (defaultDSPFolderLabel != null) {
            defaultDSPFolderLabel.setText("None");
        }
        if (defaultDumpFolderLabel != null) {
            defaultDumpFolderLabel.setText("None");
        }

        try (PrintWriter writer = new PrintWriter(new FileOutputStream("settings.txt"))) {
            writer.println("defaultSavedDSPFolder:None");
            writer.println("defaultDumpOutputFolder:None");
            JOptionPane.showMessageDialog(this, "Settings reset to default.");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to reset settings: " + e.getMessage());
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
                "Would you like to pick a folder of DSPs to select a song from?\n(Your choice will be remembered until closing the program)",
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

        if (e.getSource() == dumpSong) {
            if (pdtPath.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No PDT file was chosen!");
                return;
            }

            File pdtFile = new File(pdtPath);

            if (!pdtFile.exists()) {
                JOptionPane.showMessageDialog(this, "The chosen PDT file doesn't exist!");
                return;
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

            SongDumper.dumpSong(
                    pdtFile,
                    actualSongIndex,
                    selectedSongName,
                    defaultDumpOutputFolder
            );
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
        }

        if (e.getSource() == selectGame) {
            initPDTPath();
        }
    }
}