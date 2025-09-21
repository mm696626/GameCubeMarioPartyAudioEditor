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

public class MarioPartyMusicEditorUI extends JFrame implements ActionListener {

    private JButton pickLeftChannel, pickRightChannel, dumpSong, modifySong;
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

        dumpSong = new JButton("Dump Selected Song");
        dumpSong.addActionListener(this);

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

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        add(dumpSong, gridBagConstraints);

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

        // Now prompt the user to select the game
        String selectedGame = (String) JOptionPane.showInputDialog(
                this,
                "Select Game",
                "Game Selection",
                JOptionPane.QUESTION_MESSAGE,
                null,
                new String[]{"Mario Party 5", "Mario Party 6", "Mario Party 7"},
                "Mario Party 5"  // Default value
        );

        if (selectedGame == null) {
            System.out.println("No game selected. Exiting.");
            return;
        }

        // Update the song list based on the selected game
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

        if (e.getSource() == dumpSong) {
            SongDumper.extractSong(new File(pdtPath), songNames.getSelectedIndex());
        }

        if (e.getSource() == modifySong) {
            if (leftChannelPath.isEmpty() || rightChannelPath.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Either the left or right channel wasn't chosen!");
                return;
            }

            SongModifier.modifySong(new File(pdtPath), new File(leftChannelPath), new File(rightChannelPath), songNames.getSelectedIndex());
        }
    }
}