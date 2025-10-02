package io.sound;

import io.FileIO;

import javax.swing.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

public class SoundDumper {

    //This code is derived from Yoshimaster96's C MSM sound dumping code, so huge credit and kudos to them!
    //This code is a C to Java translation
    //Code: https://github.com/Yoshimaster96/mpgc-sound-tools

    public static void dumpSoundBank(File msmFile, long soundBankDumped, File defaultDumpOutputFolder, boolean dumpProjPool) {
        File outputDir;

        if (defaultDumpOutputFolder == null || !defaultDumpOutputFolder.exists()) {
            outputDir = promptForOutputFolder();
            if (outputDir == null) return;
        }
        else {
            outputDir = defaultDumpOutputFolder;
        }

        try (RandomAccessFile file = new RandomAccessFile(msmFile, "r")) {

            file.seek(0x20);
            long chk2Offs = FileIO.readU32BE(file);
            long chk2Size = FileIO.readU32BE(file);

            file.skipBytes(0x10);

            long chk5Offs = FileIO.readU32BE(file);
            long chk5Size = FileIO.readU32BE(file);
            long chk6Offs = FileIO.readU32BE(file);
            long chk6Size = FileIO.readU32BE(file);

            for (int i = 1; i < (chk2Size >> 5); i++) {
                file.seek(chk2Offs + (i << 5));
                long groupId = FileIO.readU16BE(file);
                file.skipBytes(2);

                long groupDataOffs = FileIO.readU32BE(file);
                long groupDataSize = FileIO.readU32BE(file);
                long sampOffs = FileIO.readU32BE(file);
                long sampSize = FileIO.readU32BE(file);

                groupDataOffs += chk5Offs;
                sampOffs += chk6Offs;

                file.seek(groupDataOffs);
                long poolOffs = FileIO.readU32BE(file);
                long projOffs = FileIO.readU32BE(file);
                long sdirOffs = FileIO.readU32BE(file);
                long SNGOffs = FileIO.readU32BE(file);

                long poolSize = projOffs - poolOffs;
                long projSize = sdirOffs - projOffs;
                long sdirSize = SNGOffs - sdirOffs;

                poolOffs += groupDataOffs;
                projOffs += groupDataOffs;
                sdirOffs += groupDataOffs;


                if (groupId == soundBankDumped) {
                    dumpToFile(file, sdirOffs, sdirSize, new File(outputDir, String.format("%04d.sdir", groupId)));
                    dumpToFile(file, sampOffs, sampSize, new File(outputDir, String.format("%04d.samp", groupId)));

                    if (dumpProjPool) {
                        dumpToFile(file, poolOffs, poolSize, new File(outputDir, String.format("%04d.pool", groupId)));
                        dumpToFile(file, projOffs, projSize, new File(outputDir, String.format("%04d.proj", groupId)));
                    }

                    file.close();

                    JOptionPane.showMessageDialog(null, "Sound bank has been dumped!");

                    return;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void dumpAllSounds(File msmFile, File defaultDumpOutputFolder, boolean dumpProjPool) {
        File outputDir;

        if (defaultDumpOutputFolder == null || !defaultDumpOutputFolder.exists()) {
            outputDir = promptForOutputFolder();
            if (outputDir == null) return;
        }
        else {
            outputDir = defaultDumpOutputFolder;
        }

        try (RandomAccessFile file = new RandomAccessFile(msmFile, "r")) {

            file.seek(0x20);
            long chk2Offs = FileIO.readU32BE(file);
            long chk2Size = FileIO.readU32BE(file);

            file.skipBytes(0x10);

            long chk5Offs = FileIO.readU32BE(file);
            long chk5Size = FileIO.readU32BE(file);
            long chk6Offs = FileIO.readU32BE(file);
            long chk6Size = FileIO.readU32BE(file);

            for (int i = 1; i < (chk2Size >> 5); i++) {
                file.seek(chk2Offs + (i << 5));
                long groupId = FileIO.readU16BE(file);
                file.skipBytes(2);

                long groupDataOffs = FileIO.readU32BE(file);
                long groupDataSize = FileIO.readU32BE(file);
                long sampOffs = FileIO.readU32BE(file);
                long sampSize = FileIO.readU32BE(file);

                groupDataOffs += chk5Offs;
                sampOffs += chk6Offs;

                file.seek(groupDataOffs);
                long poolOffs = FileIO.readU32BE(file);
                long projOffs = FileIO.readU32BE(file);
                long sdirOffs = FileIO.readU32BE(file);
                long SNGOffs = FileIO.readU32BE(file);

                long poolSize = projOffs - poolOffs;
                long projSize = sdirOffs - projOffs;
                long sdirSize = SNGOffs - sdirOffs;

                poolOffs += groupDataOffs;
                projOffs += groupDataOffs;
                sdirOffs += groupDataOffs;

                dumpToFile(file, sdirOffs, sdirSize, new File(outputDir, String.format("%04d.sdir", groupId)));
                dumpToFile(file, sampOffs, sampSize, new File(outputDir, String.format("%04d.samp", groupId)));

                if (dumpProjPool) {
                    dumpToFile(file, poolOffs, poolSize, new File(outputDir, String.format("%04d.pool", groupId)));
                    dumpToFile(file, projOffs, projSize, new File(outputDir, String.format("%04d.proj", groupId)));
                }
            }

            file.close();
            JOptionPane.showMessageDialog(null, "Sound banks have been dumped!");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void dumpToFile(RandomAccessFile file, long offset, long size, File outputFile) throws IOException {
        byte[] buffer = new byte[(int) size];
        file.seek(offset);
        file.readFully(buffer);
        try (FileOutputStream out = new FileOutputStream(outputFile)) {
            out.write(buffer);
        }
    }

    private static File promptForOutputFolder() {
        return chooseOutputDirectory();
    }

    private static File chooseOutputDirectory() {
        JFileChooser soundBankOutputFolderChooser = new JFileChooser();
        soundBankOutputFolderChooser.setDialogTitle("Select the sound bank output folder");
        soundBankOutputFolderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        soundBankOutputFolderChooser.setAcceptAllFileFilterUsed(false);

        int result = soundBankOutputFolderChooser.showSaveDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            return soundBankOutputFolderChooser.getSelectedFile();
        } else {
            return null;
        }
    }
}