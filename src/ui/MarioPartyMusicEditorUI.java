package ui;

import constants.MarioPartySongNames;
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
import java.util.Date;

public class MarioPartyMusicEditorUI extends JFrame implements ActionListener {

    private JButton pickLeftChannel, pickRightChannel, modifySong;
    private String pdtPath = "";
    private String leftChannelPath = "";
    private String rightChannelPath = "";
    private JComboBox<String> songNames;
    private JComboBox<String> gameSelect;

    private JLabel leftChannelLabel, rightChannelLabel;
    private JLabel pdtFilePathLabel; // New label for PDT path

    GridBagConstraints gridBagConstraints = null;

    public MarioPartyMusicEditorUI() {
        setTitle("Mario Party GameCube Music Editor");
        generateUI();
        initPDTPath();
    }

    private void generateUI() {

        pickLeftChannel = new JButton("Select Left DSP Channel");
        pickLeftChannel.addActionListener(this);

        pickRightChannel = new JButton("Select Right DSP Channel");
        pickRightChannel.addActionListener(this);

        modifySong = new JButton("Modify Selected Song");
        modifySong.addActionListener(this);

        songNames = new JComboBox<>();

        leftChannelLabel = new JLabel("No file selected for Left Channel");
        rightChannelLabel = new JLabel("No file selected for Right Channel");

        pdtFilePathLabel = new JLabel("No PDT file selected"); // Initialize label for PDT file path

        setLayout(new GridBagLayout());
        gridBagConstraints = new GridBagConstraints();

        // Adding components to the layout
        gridBagConstraints.gridx = 0;
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

        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        add(modifySong, gridBagConstraints);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4; // Place the PDT path label below the buttons
        gridBagConstraints.gridwidth = 2;
        add(pdtFilePathLabel, gridBagConstraints);
    }

    private void initPDTPath() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select PDT file");

        FileNameExtensionFilter pdtFilter = new FileNameExtensionFilter("PDT Files", "pdt");
        fileChooser.setFileFilter(pdtFilter);

        int userSelection = fileChooser.showOpenDialog(null);

        if (userSelection != JFileChooser.APPROVE_OPTION) {
            System.out.println("No file selected. Exiting.");
            return;
        }

        pdtPath = fileChooser.getSelectedFile().getAbsolutePath();

        pdtFilePathLabel.setText("Selected PDT: " + pdtPath);

        String selectedGame = null;
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

        updateSongList(selectedGame);
    }

    private void chooseLeftChannelPath() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select DSP Left Channel");

        FileNameExtensionFilter dspFilter = new FileNameExtensionFilter("DSP Files", "dsp");
        fileChooser.setFileFilter(dspFilter);

        int userSelection = fileChooser.showOpenDialog(null);

        if (userSelection != JFileChooser.APPROVE_OPTION) {
            System.out.println("No file selected. Exiting.");
        } else {
            leftChannelPath = fileChooser.getSelectedFile().getAbsolutePath();
            String leftChannelName = fileChooser.getSelectedFile().getName();
            leftChannelLabel.setText("Left Channel: " + leftChannelName);
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
        } else {
            rightChannelPath = fileChooser.getSelectedFile().getAbsolutePath();
            String rightChannelName = fileChooser.getSelectedFile().getName();
            rightChannelLabel.setText("Right Channel: " + rightChannelName);
        }
    }

    private void updateSongList(String selectedGame) {
        switch (selectedGame) {
            case "Mario Party 4":
                songNames.setModel(new DefaultComboBoxModel<>(MarioPartySongNames.MARIO_PARTY_4_TRACK_NAMES));
                break;
            case "Mario Party 5":
                songNames.setModel(new DefaultComboBoxModel<>(MarioPartySongNames.MARIO_PARTY_5_TRACK_NAMES));
                break;
            case "Mario Party 6":
                songNames.setModel(new DefaultComboBoxModel<>(MarioPartySongNames.MARIO_PARTY_6_TRACK_NAMES));
                break;
            case "Mario Party 7":
                songNames.setModel(new DefaultComboBoxModel<>(MarioPartySongNames.MARIO_PARTY_7_TRACK_NAMES));
                break;
            default:
                songNames.setModel(new DefaultComboBoxModel<>(new String[]{}));
                break;
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

        if (e.getSource() == modifySong) {
            if (leftChannelPath.isEmpty() || rightChannelPath.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Either the left or right channel wasn't chosen!");
                return;
            }

            int response = JOptionPane.showConfirmDialog(null, "Do you want to make a backup of the PDT file?", "Backup PDT", JOptionPane.YES_NO_OPTION);

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

            SongModifier.modifySong(new File(pdtPath), new File(leftChannelPath), new File(rightChannelPath), songNames.getSelectedIndex());
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