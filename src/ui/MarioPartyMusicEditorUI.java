package ui;

import constants.MarioPartySongNames;
import io.SongDumper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class MarioPartyMusicEditorUI extends JFrame implements ActionListener {


    private JButton pickLeftChannel, pickRightChannel, dumpSong, modifySong;
    private String pdtPath = "";
    private String leftChannelPath = "";
    private String rightChannelPath = "";
    private JComboBox<String> songNames = new JComboBox<>(MarioPartySongNames.MARIO_PARTY_5_TRACK_NAMES);
    GridBagConstraints gridBagConstraints = null;

    public MarioPartyMusicEditorUI()
    {
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

        setLayout(new GridBagLayout());
        gridBagConstraints = new GridBagConstraints();

        gridBagConstraints.gridx=0;
        gridBagConstraints.gridy=0;
        add(songNames, gridBagConstraints);

        gridBagConstraints.gridx=0;
        gridBagConstraints.gridy=1;
        add(pickLeftChannel, gridBagConstraints);

        gridBagConstraints.gridx=1;
        gridBagConstraints.gridy=1;
        add(pickRightChannel, gridBagConstraints);

        gridBagConstraints.gridx=0;
        gridBagConstraints.gridy=2;
        add(dumpSong, gridBagConstraints);

        gridBagConstraints.gridx=1;
        gridBagConstraints.gridy=2;
        add(modifySong, gridBagConstraints);
    }

    private void initPDTPath() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select PDT file");
        int userSelection = fileChooser.showOpenDialog(null);

        if (userSelection != JFileChooser.APPROVE_OPTION) {
            System.out.println("No file selected. Exiting.");
            return;
        }

        else {
            pdtPath = fileChooser.getSelectedFile().getAbsolutePath();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == pickLeftChannel) {

        }

        if (e.getSource() == dumpSong) {
            SongDumper.extractSong(new File(pdtPath), songNames.getSelectedIndex());
        }
    }
}