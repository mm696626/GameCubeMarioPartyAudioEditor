package ui;

import constants.MarioPartySongNames;
import io.DSPPair;
import io.QueueJob;
import io.SongDumper;
import io.SongModifier;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

public class MarioPartyMusicEditorUI extends JFrame implements ActionListener {

    private JButton pickLeftChannel, pickRightChannel, dumpSong, dumpAllSongs, modifySong, selectGame, clearModifyButton;
    private String pdtPath = "";
    private String leftChannelPath = "";
    private String rightChannelPath = "";
    private String selectedGame = "";
    private JComboBox<String> songNames;

    private JLabel leftChannelLabel;
    private JLabel rightChannelLabel;
    private JLabel pdtFilePathLabel;
    private JLabel selectedGameLabel;

    private java.util.List<QueueJob> jobQueue = new ArrayList<>();
    private DefaultListModel<String> queueListModel = new DefaultListModel<>();
    private JList<String> queueList;
    private JButton addToQueueButton, runQueueButton, clearQueueButton;


    public MarioPartyMusicEditorUI() {
        setTitle("Mario Party GameCube Music Editor");
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

        JPanel dumpPanel = new JPanel(new GridBagLayout());
        dumpPanel.setBorder(BorderFactory.createTitledBorder("Song Dumping"));
        GridBagConstraints dumpingGBC = new GridBagConstraints();
        dumpingGBC.insets = new Insets(5, 5, 5, 5);
        dumpingGBC.fill = GridBagConstraints.HORIZONTAL;

        dumpSong = new JButton("Dump Selected Song");
        dumpSong.addActionListener(this);

        dumpAllSongs = new JButton("Dump All Songs");
        dumpAllSongs.addActionListener(this);

        dumpingGBC.gridx = 0; dumpingGBC.gridy = 0;
        dumpPanel.add(dumpSong, dumpingGBC);
        dumpingGBC.gridx = 1;
        dumpPanel.add(dumpAllSongs, dumpingGBC);

        JPanel modifyPanel = new JPanel(new GridBagLayout());
        modifyPanel.setBorder(BorderFactory.createTitledBorder("Modify Song"));
        GridBagConstraints modifyGBC = new GridBagConstraints();
        modifyGBC.insets = new Insets(5, 5, 5, 5);
        modifyGBC.fill = GridBagConstraints.HORIZONTAL;

        pickLeftChannel = new JButton("Select Left DSP Channel");
        pickLeftChannel.addActionListener(this);
        leftChannelLabel = new JLabel("No file selected");

        pickRightChannel = new JButton("Select Right DSP Channel");
        pickRightChannel.addActionListener(this);
        rightChannelLabel = new JLabel("No file selected");

        modifySong = new JButton("Modify Selected Song");
        modifySong.addActionListener(this);

        modifyGBC.gridx = 0; modifyGBC.gridy = 0;
        modifyPanel.add(pickLeftChannel, modifyGBC);
        modifyGBC.gridx = 1;
        modifyPanel.add(leftChannelLabel, modifyGBC);

        modifyGBC.gridx = 0; modifyGBC.gridy = 1;
        modifyPanel.add(pickRightChannel, modifyGBC);
        modifyGBC.gridx = 1;
        modifyPanel.add(rightChannelLabel, modifyGBC);

        modifyGBC.gridx = 0; modifyGBC.gridy = 2;
        modifyGBC.gridwidth = 2;
        modifyPanel.add(modifySong, modifyGBC);

        clearModifyButton = new JButton("Clear");
        clearModifyButton.addActionListener(this);

        modifyGBC.gridy = 3;
        modifyGBC.gridwidth = 2;
        modifyPanel.add(clearModifyButton, modifyGBC);


        JPanel queuePanel = new JPanel(new BorderLayout());
        queuePanel.setBorder(BorderFactory.createTitledBorder("Job Queue"));

        queueList = new JList<>(queueListModel);
        queueList.setVisibleRowCount(6);
        queuePanel.add(new JScrollPane(queueList), BorderLayout.CENTER);

        JPanel queueButtonsPanel = new JPanel();
        addToQueueButton = new JButton("Add to Queue");
        runQueueButton = new JButton("Run Queue");
        clearQueueButton = new JButton("Clear Queue");

        addToQueueButton.addActionListener(this);
        runQueueButton.addActionListener(this);
        clearQueueButton.addActionListener(this);

        queueButtonsPanel.add(addToQueueButton);
        queueButtonsPanel.add(runQueueButton);
        queueButtonsPanel.add(clearQueueButton);

        queuePanel.add(queueButtonsPanel, BorderLayout.SOUTH);

        songToolsPanel.add(Box.createVerticalStrut(10));
        songToolsPanel.add(queuePanel);


        songToolsPanel.add(songSelectionPanel);
        songToolsPanel.add(Box.createVerticalStrut(10));
        songToolsPanel.add(dumpPanel);
        songToolsPanel.add(Box.createVerticalStrut(10));
        songToolsPanel.add(modifyPanel);

        tabbedPane.addTab("Song Tools", songToolsPanel);

        setLayout(new BorderLayout());
        add(tabbedPane, BorderLayout.CENTER);
    }

    private void initPDTPath() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select PDT file");

        FileNameExtensionFilter pdtFilter = new FileNameExtensionFilter("PDT Files", "pdt");
        fileChooser.setFileFilter(pdtFilter);

        int userSelection = fileChooser.showOpenDialog(null);

        if (userSelection != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File selectedPDT = fileChooser.getSelectedFile();
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
        int response = JOptionPane.showConfirmDialog(this, "Would you like to pick a folder of DSPs to select a song from?", "Choose DSP Folder", JOptionPane.YES_NO_OPTION);
        if (response == JOptionPane.YES_OPTION) {
            chooseDSPPairFolder();

        } else {
            chooseDSP(true);
        }
    }

    private void chooseDSPPairFolder() {
        JFileChooser folderChooser = new JFileChooser();
        folderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        folderChooser.setDialogTitle("Select Folder with DSP Files");

        int folderSelected = folderChooser.showOpenDialog(this);
        if (folderSelected == JFileChooser.APPROVE_OPTION) {
            File selectedFolder = folderChooser.getSelectedFile();
            ArrayList<DSPPair> dspPairs = DSPPair.detectDSPPairs(selectedFolder);

            if (dspPairs.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No matching DSP pairs found in the selected folder.");
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
                leftChannelPath = selectedPair.left.getAbsolutePath();
                rightChannelPath = selectedPair.right.getAbsolutePath();
                leftChannelLabel.setText(selectedPair.left.getName());
                rightChannelLabel.setText(selectedPair.right.getName());
            }
        }
    }

    private void chooseRightChannelPath() {
        int response = JOptionPane.showConfirmDialog(this, "Would you like to pick a folder of DSPs to select a song from?", "Choose DSP Folder", JOptionPane.YES_NO_OPTION);
        if (response == JOptionPane.YES_OPTION) {
            chooseDSPPairFolder();

        } else {
            chooseDSP(false);
        }
    }

    private void chooseDSP(boolean isLeft) {
        JFileChooser fileChooser = new JFileChooser();

        if (isLeft) {
            fileChooser.setDialogTitle("Select DSP Left Channel");
        }
        else {
            fileChooser.setDialogTitle("Select DSP Right Channel");
        }

        FileNameExtensionFilter dspFilter = new FileNameExtensionFilter("DSP Files", "dsp");
        fileChooser.setFileFilter(dspFilter);

        int userSelection = fileChooser.showOpenDialog(this);
        if (userSelection != JFileChooser.APPROVE_OPTION) return;

        File selectedFile = fileChooser.getSelectedFile();

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
            ArrayList<Integer> sortedKeys = new ArrayList<>(songNameMap.keySet());
            Collections.sort(sortedKeys);

            ArrayList<String> songList = new ArrayList<>();
            for (Integer key : sortedKeys) {
                songList.add(songNameMap.get(key));
            }

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

    private static File chooseOutputDirectory() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select the DSP output folder");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);

        int result = chooser.showSaveDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile();
        } else {
            return null;
        }
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
                    false,
                    null
            );
        }

        if (e.getSource() == dumpAllSongs) {
            if (pdtPath.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No PDT file was chosen!");
                return;
            }

            File pdtFile = new File(pdtPath);

            if (!pdtFile.exists()) {
                JOptionPane.showMessageDialog(this, "The chosen PDT file doesn't exist!");
                return;
            }

            SongDumper.dumpAllSongs(pdtFile);
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
            File rightChannelFile = new File(leftChannelPath);

            if (!pdtFile.exists()) {
                JOptionPane.showMessageDialog(this, "The chosen PDT file doesn't exist!");
                return;
            }

            if (!leftChannelFile.exists() || !rightChannelFile.exists()) {
                JOptionPane.showMessageDialog(this, "Either the left or right channel doesn't exist!");
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

            SongModifier.modifySong(
                    pdtFile,
                    leftChannelFile,
                    rightChannelFile,
                    actualSongIndex,
                    selectedSongName,
                    false
            );
        }

        if (e.getSource() == clearModifyButton) {
            leftChannelPath = "";
            rightChannelPath = "";
            leftChannelLabel.setText("No file selected");
            rightChannelLabel.setText("No file selected");
        }

        if (e.getSource() == selectGame) {
            initPDTPath();
        }

        if (e.getSource() == addToQueueButton) {
            String selectedSongName = (String) songNames.getSelectedItem();
            if (selectedSongName == null || pdtPath.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Missing song or PDT file.");
                return;
            }

            Map<Integer, String> songNameMap = getSongNameMapForSelectedGame();
            if (songNameMap == null) return;

            int index = songNameMap.entrySet().stream()
                    .filter(entry -> entry.getValue().equals(selectedSongName))
                    .map(Map.Entry::getKey)
                    .findFirst()
                    .orElse(-1);

            if (index == -1) {
                JOptionPane.showMessageDialog(this, "Could not find song index.");
                return;
            }

            QueueJob.Type type = (leftChannelPath.isEmpty() || rightChannelPath.isEmpty())
                    ? QueueJob.Type.DUMP
                    : QueueJob.Type.MODIFY;

            QueueJob job = new QueueJob(type, selectedSongName, index, leftChannelPath, rightChannelPath);
            jobQueue.add(job);
            queueListModel.addElement(job.toString());
        }

        if (e.getSource() == clearQueueButton) {
            jobQueue.clear();
            queueListModel.clear();
        }

        if (e.getSource() == runQueueButton) {
            if (jobQueue.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Queue is empty!");
                return;
            }

            File pdtFile = new File(pdtPath);
            if (!pdtFile.exists()) {
                JOptionPane.showMessageDialog(this, "PDT file does not exist.");
                return;
            }

            boolean askedForDumpFolder = false;
            File queueDumpFolder = null;
            for (QueueJob job: jobQueue) {
                if (job.getType() == QueueJob.Type.DUMP) {
                    if (!askedForDumpFolder) {
                        queueDumpFolder = chooseOutputDirectory();
                    }
                }
            }

            if (queueDumpFolder == null) {
                return;
            }

            File usedDumpFolder = queueDumpFolder;

            new Thread(() -> {
                for (QueueJob job : jobQueue) {
                    try {
                        if (job.getType() == QueueJob.Type.DUMP) {
                            SongDumper.dumpSong(pdtFile, job.getSongIndex(), job.getSongName(), true, usedDumpFolder);
                        } else {
                            File leftFile = new File(job.getLeftChannel());
                            File rightFile = new File(job.getRightChannel());
                            if (leftFile.exists() && rightFile.exists()) {
                                SongModifier.modifySong(pdtFile, leftFile, rightFile, job.getSongIndex(), job.getSongName(), true);
                            } else {
                                JOptionPane.showMessageDialog(this, "Channel file(s) missing for: " + job.getSongName());
                            }
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this, "Error during job: " + job.getSongName() + "\n" + ex.getMessage());
                    }
                }

                SwingUtilities.invokeLater(() -> {
                    jobQueue.clear();
                    queueListModel.clear();
                    JOptionPane.showMessageDialog(this, "Job queue finished.");
                });
            }).start();
        }
    }
}