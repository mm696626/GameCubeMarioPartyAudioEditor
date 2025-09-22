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

    GridBagConstraints gridBagConstraints = null;

    public MarioPartyMusicEditorUI() {
        setTitle("Mario Party GameCube Music Editor");
        generateUI();
    }

    private void generateUI() {

        pickLeftChannel = new JButton("Select Left DSP Channel");
        pickLeftChannel.addActionListener(this);

        pickRightChannel = new JButton("Select Right DSP Channel");
        pickRightChannel.addActionListener(this);

        dumpSong = new JButton("Dump Selected Song");
        dumpSong.addActionListener(this);

        dumpAllSongs = new JButton("Dump All Songs");
        dumpAllSongs.addActionListener(this);

        modifySong = new JButton("Modify Selected Song");
        modifySong.addActionListener(this);

        selectGame = new JButton("Select PDT and Game");
        selectGame.addActionListener(this);

        songNames = new JComboBox<>();

        songLabel = new JLabel("Chosen Song");

        leftChannelLabel = new JLabel("No file selected");
        rightChannelLabel = new JLabel("No file selected");

        pdtFilePathLabel = new JLabel("No PDT file selected");
        selectedGameLabel = new JLabel("No game selected");

        setLayout(new GridBagLayout());
        gridBagConstraints = new GridBagConstraints();

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        add(songLabel, gridBagConstraints);

        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        add(songNames, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        add(pickLeftChannel, gridBagConstraints);

        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        add(leftChannelLabel, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        add(pickRightChannel, gridBagConstraints);

        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        add(rightChannelLabel, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        add(dumpSong, gridBagConstraints);

        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        add(dumpAllSongs, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        add(modifySong, gridBagConstraints);

        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        add(selectGame, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        add(pdtFilePathLabel, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 2;
        add(selectedGameLabel, gridBagConstraints);

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