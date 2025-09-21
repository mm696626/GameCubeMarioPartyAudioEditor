// GameCube Mario Party Music Editor by Matt McCullough
// This is to edit the in game music for Mario Parties 5-7

import ui.MarioPartyMusicEditorUI;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        MarioPartyMusicEditorUI marioPartyMusicEditorUI = new MarioPartyMusicEditorUI();
        marioPartyMusicEditorUI.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        marioPartyMusicEditorUI.pack();
        marioPartyMusicEditorUI.setVisible(true);
    }
}