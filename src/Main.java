// GameCube Mario Party Audio Editor by Matt McCullough
// This is to edit the in game music and sounds for Mario Parties 4-7

import ui.MarioPartyAudioEditorUI;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        MarioPartyAudioEditorUI marioPartyAudioEditorUI = new MarioPartyAudioEditorUI();
        marioPartyAudioEditorUI.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        marioPartyAudioEditorUI.pack();
        marioPartyAudioEditorUI.setVisible(true);
    }
}