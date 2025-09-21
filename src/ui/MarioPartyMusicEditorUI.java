package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MarioPartyMusicEditorUI extends JFrame implements ActionListener {


    private JButton pickLeftChannel, pickRightChannel, dumpSong, modifySong;
    private String pdtPath = "";
    private String leftChannelPath = "";
    private String rightChannelPath = "";
    GridBagConstraints gridBagConstraints = null;

    public MarioPartyMusicEditorUI()
    {
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

        modifySong = new JButton("Modify Selected Song");
        modifySong.addActionListener(this);

        setLayout(new GridBagLayout());
        gridBagConstraints = new GridBagConstraints();


        gridBagConstraints.gridx=0;
        gridBagConstraints.gridy=0;
        add(pickLeftChannel, gridBagConstraints);

        gridBagConstraints.gridx=1;
        gridBagConstraints.gridy=0;
        add(pickRightChannel, gridBagConstraints);

        gridBagConstraints.gridx=0;
        gridBagConstraints.gridy=1;
        add(dumpSong, gridBagConstraints);

        gridBagConstraints.gridx=1;
        gridBagConstraints.gridy=1;
        add(modifySong, gridBagConstraints);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == pickLeftChannel) {

        }
    }
}