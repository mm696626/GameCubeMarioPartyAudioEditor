package io;

import javax.swing.*;
import java.io.*;

public class SoundDumper {

    //This code is derived from Yoshimaster96's C MSM sound dumping code, so huge credit and kudos to them!
    //This code is a C to Java translation
    //Code: https://github.com/Yoshimaster96/mpgc-sound-tools

    public static void dumpSoundBank(File msmFile, long soundBankDumped, File defaultDumpOutputFolder) {
        File outputDir;

        if (defaultDumpOutputFolder == null) {
            outputDir = promptForOutputFolder();
            if (outputDir == null) return;
        }
        else {
            outputDir = defaultDumpOutputFolder;
        }

        try (RandomAccessFile file = new RandomAccessFile(msmFile, "r")) {

            // Seek to 0x20 to read chk2 offset and size
            file.seek(0x20);
            long chk2Offs = FileIO.readU32BE(file);
            long chk2Size = FileIO.readU32BE(file);

            // Skip 0x10 bytes
            file.skipBytes(0x10);

            // Read chk5 and chk6 offsets and sizes
            long chk5Offs = FileIO.readU32BE(file);
            long chk5Size = FileIO.readU32BE(file);
            long chk6Offs = FileIO.readU32BE(file);
            long chk6Size = FileIO.readU32BE(file);

            // Iterate through entries in chk2
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

                // Read offsets from group data
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
                    // Dump .sdir
                    dumpToFile(file, sdirOffs, sdirSize, new File(outputDir, String.format("%04X.sdir", groupId)));

                    // Dump .samp
                    dumpToFile(file, sampOffs, sampSize, new File(outputDir, String.format("%04X.samp", groupId)));

                    file.close();

                    return;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void dumpAllSounds(File msmFile, File defaultDumpOutputFolder) {
        File outputDir;

        if (defaultDumpOutputFolder == null) {
            outputDir = promptForOutputFolder();
            if (outputDir == null) return;
        }
        else {
            outputDir = defaultDumpOutputFolder;
        }

        try (RandomAccessFile file = new RandomAccessFile(msmFile, "r")) {

            // Seek to 0x20 to read chk2 offset and size
            file.seek(0x20);
            long chk2Offs = FileIO.readU32BE(file);
            long chk2Size = FileIO.readU32BE(file);

            // Skip 0x10 bytes
            file.skipBytes(0x10);

            // Read chk5 and chk6 offsets and sizes
            long chk5Offs = FileIO.readU32BE(file);
            long chk5Size = FileIO.readU32BE(file);
            long chk6Offs = FileIO.readU32BE(file);
            long chk6Size = FileIO.readU32BE(file);

            // Iterate through entries in chk2
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

                // Read offsets from group data
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

                // Dump .sdir
                dumpToFile(file, sdirOffs, sdirSize, new File(outputDir, String.format("%04X.sdir", groupId)));

                // Dump .samp
                dumpToFile(file, sampOffs, sampSize, new File(outputDir, String.format("%04X.samp", groupId)));
            }

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
        JFileChooser dspOutputFolderChooser = new JFileChooser();
        dspOutputFolderChooser.setDialogTitle("Select the DSP output folder");
        dspOutputFolderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        dspOutputFolderChooser.setAcceptAllFileFilterUsed(false);

        int result = dspOutputFolderChooser.showSaveDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            return dspOutputFolderChooser.getSelectedFile();
        } else {
            return null;
        }
    }
}