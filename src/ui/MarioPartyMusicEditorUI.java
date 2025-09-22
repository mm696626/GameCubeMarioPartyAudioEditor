package ui;

import constants.MarioPartySongNames;
import io.SongDumper;
import io.SongModifier;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

public class MarioPartyMusicEditorUI extends JFrame implements ActionListener {

    private JButton pickLeftChannel, pickRightChannel, dumpSong, dumpAllSongs, modifySong, selectGame;
    private String pdtPath = "";
    private String leftChannelPath = "";
    private String rightChannelPath = "";
    private String selectedGame = "";
    private JComboBox<String> songNames;

    private JLabel songLabel, leftChannelLabel, rightChannelLabel, pdtFilePathLabel, selectedGameLabel;

    public MarioPartyMusicEditorUI() {
        setTitle("Mario Party GameCube Music Editor");
        generateUI();
    }

    private void generateUI() {

        JTabbedPane tabbedPane = new JTabbedPane();

        //song selection tab
        JPanel songSelectionPanel = new JPanel(new GridBagLayout());
        GridBagConstraints songSelectionGBC = new GridBagConstraints();
        songSelectionGBC.insets = new Insets(5, 5, 5, 5);
        songSelectionGBC.fill = GridBagConstraints.HORIZONTAL;

        songLabel = new JLabel("Chosen Song:");
        songNames = new JComboBox<>();

        songSelectionGBC.gridx = 0; songSelectionGBC.gridy = 0;
        songSelectionPanel.add(songLabel, songSelectionGBC);
        songSelectionGBC.gridx = 1;
        songSelectionPanel.add(songNames, songSelectionGBC);

        //dumping tab
        JPanel dumpPanel = new JPanel(new GridBagLayout());
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

        //modify tab
        JPanel modifyPanel = new JPanel(new GridBagLayout());
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

        //choose game/PDT tab
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

        chooseGameGBC.gridx = 0; chooseGameGBC.gridy = 1;
        chooseGamePanel.add(pdtFilePathLabel, chooseGameGBC);

        chooseGameGBC.gridx = 0; chooseGameGBC.gridy = 2;
        chooseGamePanel.add(selectedGameLabel, chooseGameGBC);

        tabbedPane.addTab("Choose PDT/Game", chooseGamePanel);
        tabbedPane.addTab("Song Selection", songSelectionPanel);
        tabbedPane.addTab("Song Dumping", dumpPanel);
        tabbedPane.addTab("Modify Song", modifyPanel);

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

        updateSongList(selectedGame);
        selectedGameLabel.setText("Selected Game: " + selectedGame);
    }

    private void chooseLeftChannelPath() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select DSP Left Channel");

        FileNameExtensionFilter dspFilter = new FileNameExtensionFilter("DSP Files", "dsp");
        fileChooser.setFileFilter(dspFilter);

        int userSelection = fileChooser.showOpenDialog(null);

        if (userSelection != JFileChooser.APPROVE_OPTION) {
            System.out.println("No file selected. Exiting.");
            return;
        }

        File selectedFile = fileChooser.getSelectedFile();
        leftChannelPath = selectedFile.getAbsolutePath();
        leftChannelLabel.setText(selectedFile.getName());

        File other = detectOtherChannel(selectedFile, true);
        if (other != null) {
            rightChannelPath = other.getAbsolutePath();
            rightChannelLabel.setText(other.getName());
        }
    }

    private void chooseRightChannelPath() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select DSP Right Channel");

        FileNameExtensionFilter dspFilter = new FileNameExtensionFilter("DSP Files", "dsp");
        fileChooser.setFileFilter(dspFilter);

        int userSelection = fileChooser.showOpenDialog(null);

        if (userSelection != JFileChooser.APPROVE_OPTION) {
            System.out.println("No file selected. Exiting.");
            return;
        }

        File selectedFile = fileChooser.getSelectedFile();
        rightChannelPath = selectedFile.getAbsolutePath();
        rightChannelLabel.setText(selectedFile.getName());

        File other = detectOtherChannel(selectedFile, false);
        if (other != null) {
            leftChannelPath = other.getAbsolutePath();
            leftChannelLabel.setText(other.getName());
        }
    }

    private File detectOtherChannel(File selectedFile, boolean isLeftSelected) {
        String name = selectedFile.getName();
        File parentDir = selectedFile.getParentFile();

        String counterpartName = null;

        if (name.endsWith("_L.dsp") && isLeftSelected) {
            counterpartName = name.replace("_L.dsp", "_R.dsp");
        } else if (name.endsWith("_R.dsp") && !isLeftSelected) {
            counterpartName = name.replace("_R.dsp", "_L.dsp");
        } else if (name.endsWith("(channel 0).dsp") && isLeftSelected) {
            counterpartName = name.replace("(channel 0).dsp", "(channel 1).dsp");
        } else if (name.endsWith("(channel 1).dsp") && !isLeftSelected) {
            counterpartName = name.replace("(channel 1).dsp", "(channel 0).dsp");
        }

        if (counterpartName != null) {
            File counterpartFile = new File(parentDir, counterpartName);
            if (counterpartFile.exists()) {
                return counterpartFile;
            }
        }

        return null;
    }

    private void updateSongList(String selectedGame) {
        Map<Integer, String> trackMap = null;

        if ("Mario Party 4".equals(selectedGame)) {
            trackMap = MarioPartySongNames.MARIO_PARTY_4_TRACK_NAMES;
        } else if ("Mario Party 5".equals(selectedGame)) {
            trackMap = MarioPartySongNames.MARIO_PARTY_5_TRACK_NAMES;
        } else if ("Mario Party 6".equals(selectedGame)) {
            trackMap = MarioPartySongNames.MARIO_PARTY_6_TRACK_NAMES;
        } else if ("Mario Party 7".equals(selectedGame)) {
            trackMap = MarioPartySongNames.MARIO_PARTY_7_TRACK_NAMES;
        }

        if (trackMap != null) {
            ArrayList<Integer> sortedKeys = new ArrayList<>(trackMap.keySet());
            Collections.sort(sortedKeys);

            ArrayList<String> songList = new ArrayList<>();
            for (Integer key : sortedKeys) {
                songList.add(trackMap.get(key));
            }

            songNames.setModel(new DefaultComboBoxModel<>(songList.toArray(new String[0])));
        } else {
            songNames.setModel(new DefaultComboBoxModel<>(new String[]{}));
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

            String selectedSongName = (String) songNames.getSelectedItem();

            Map<Integer, String> trackMap;

            if ("Mario Party 4".equals(selectedGame)) {
                trackMap = MarioPartySongNames.MARIO_PARTY_4_TRACK_NAMES;
            } else if ("Mario Party 5".equals(selectedGame)) {
                trackMap = MarioPartySongNames.MARIO_PARTY_5_TRACK_NAMES;
            } else if ("Mario Party 6".equals(selectedGame)) {
                trackMap = MarioPartySongNames.MARIO_PARTY_6_TRACK_NAMES;
            } else if ("Mario Party 7".equals(selectedGame)) {
                trackMap = MarioPartySongNames.MARIO_PARTY_7_TRACK_NAMES;
            } else {
                JOptionPane.showMessageDialog(this, "No game is selected! Please select one!");
                return;
            }

            int actualSongIndex = -1;

            if (trackMap != null && selectedSongName != null) {
                for (Map.Entry<Integer, String> entry : trackMap.entrySet()) {
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
                    new File(pdtPath),
                    actualSongIndex,
                    selectedSongName
            );
        }

        if (e.getSource() == dumpAllSongs) {
            if (pdtPath.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No PDT file was chosen!");
                return;
            }

            int response = JOptionPane.showConfirmDialog(
                    null,
                    "Are you sure you want to dump all songs from the PDT?",
                    "Dump All Songs",
                    JOptionPane.YES_NO_OPTION
            );

            if (response != JOptionPane.YES_OPTION) {
                return;
            }

            SongDumper.dumpAllSongs(new File(pdtPath));
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

            int response = JOptionPane.showConfirmDialog(
                    null,
                    "Do you want to make a backup of the PDT file?",
                    "Backup PDT",
                    JOptionPane.YES_NO_OPTION
            );

            if (response == JOptionPane.YES_OPTION) {
                try {
                    String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                    File pdtFile = new File(pdtPath);
                    File backupFile = getPDTFileName(pdtFile, timestamp);

                    Files.copy(pdtFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(null, "Failed to create backup: " + ex.getMessage());
                }
            }

            String selectedSongName = (String) songNames.getSelectedItem();

            Map<Integer, String> trackMap;

            if ("Mario Party 4".equals(selectedGame)) {
                trackMap = MarioPartySongNames.MARIO_PARTY_4_TRACK_NAMES;
            } else if ("Mario Party 5".equals(selectedGame)) {
                trackMap = MarioPartySongNames.MARIO_PARTY_5_TRACK_NAMES;
            } else if ("Mario Party 6".equals(selectedGame)) {
                trackMap = MarioPartySongNames.MARIO_PARTY_6_TRACK_NAMES;
            } else if ("Mario Party 7".equals(selectedGame)) {
                trackMap = MarioPartySongNames.MARIO_PARTY_7_TRACK_NAMES;
            } else {
                JOptionPane.showMessageDialog(this, "No game is selected! Please select one!");
                return;
            }

            int actualSongIndex = -1;

            if (trackMap != null && selectedSongName != null) {
                for (Map.Entry<Integer, String> entry : trackMap.entrySet()) {
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
                    new File(pdtPath),
                    new File(leftChannelPath),
                    new File(rightChannelPath),
                    actualSongIndex,
                    selectedSongName
            );
        }

        if (e.getSource() == selectGame) {
            initPDTPath();
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
}