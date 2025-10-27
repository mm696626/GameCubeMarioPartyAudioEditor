// GameCube Mario Party Audio Editor by Matt McCullough
// This is to edit the in game music and sounds for Mario Parties 4-7

import ui.GamecubeMarioPartyAudioEditorUI;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        GamecubeMarioPartyAudioEditorUI gamecubeMarioPartyAudioEditorUI = new GamecubeMarioPartyAudioEditorUI();
        gamecubeMarioPartyAudioEditorUI.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gamecubeMarioPartyAudioEditorUI.pack();
        gamecubeMarioPartyAudioEditorUI.setVisible(true);
    }
}