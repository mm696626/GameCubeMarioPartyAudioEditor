package io;

import javax.swing.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

public class SongDumper {

    //This code is derived from Yoshimaster96's C PDT dumping code, so huge credit and kudos to them!
    //This code is a C to Java translation
    //Code: https://github.com/Yoshimaster96/mpgc-sound-tools

    // Method to convert nibbles to samples
    private static long nibblesToSamples(long nibbles) {
        long nibInt = nibbles >> 4;
        long nibFrac = nibbles & 0xF;

        if (nibFrac > 0)
            return (nibInt * 14) + nibFrac - 2;
        else
            return nibInt * 14;
    }

    public static void dumpSong(File selectedFile, int songIndex, String songName, boolean runSilently, File queueSelectedFolder) {

        File outputDir;

        if (queueSelectedFolder == null) {
            outputDir = promptForOutputFolder();
            if (outputDir == null) return;
        }

        else {
            outputDir = queueSelectedFolder;
        }

        try (RandomAccessFile raf = new RandomAccessFile(selectedFile, "r")) {
            int unk00 = PDTFileIO.readU16BE(raf);
            int numFiles = PDTFileIO.readU16BE(raf);
            long unk04 = PDTFileIO.readU32BE(raf);
            long unk08 = PDTFileIO.readU32BE(raf);
            long unk0C = PDTFileIO.readU32BE(raf);
            long entryOffs = PDTFileIO.readU32BE(raf);
            long coeffOffs = PDTFileIO.readU32BE(raf);
            long headerOffs = PDTFileIO.readU32BE(raf);
            long streamOffs = PDTFileIO.readU32BE(raf);

            if (songIndex < 0 || songIndex >= numFiles) {
                return;
            }

            raf.seek(entryOffs + (songIndex << 2));
            long thisHeaderOffs = PDTFileIO.readU32BE(raf);
            if (thisHeaderOffs == 0) {
                return;
            }

            raf.seek(thisHeaderOffs);
            long flags = PDTFileIO.readU32BE(raf);
            long sampleRate = PDTFileIO.readU32BE(raf);
            long nibbleCount = PDTFileIO.readU32BE(raf);
            long loopStart = PDTFileIO.readU32BE(raf);
            long ch1Start = PDTFileIO.readU32BE(raf);
            int ch1CoefEntry = PDTFileIO.readU16BE(raf);
            int unk116 = PDTFileIO.readU16BE(raf);
            long ch1CoefOffs = coeffOffs + (ch1CoefEntry << 5);

            long ch2Start = ch1Start;
            int ch2CoefEntry = ch1CoefEntry;
            long ch2CoefOffs = coeffOffs + (ch2CoefEntry << 5);
            int chanCount = 1;

            if ((flags & 0x01000000) != 0) {
                ch2Start = PDTFileIO.readU32BE(raf);
                ch2CoefEntry = PDTFileIO.readU16BE(raf);
                int unk11A = PDTFileIO.readU16BE(raf);
                ch2CoefOffs = coeffOffs + (ch2CoefEntry << 5);
                chanCount = 2;
            }

            int loopFlag = ((flags & 0x02000000) != 0) ? 1 : 0;

            for (int j = 0; j < chanCount; j++) {
                String fileName = getFileName(songName, chanCount, j);
                File outputFile = new File(outputDir, fileName);
                writeDSP(outputFile.getAbsolutePath(), nibbleCount, sampleRate, loopFlag, loopStart, j, raf, ch1CoefOffs, ch1Start, ch2CoefOffs, ch2Start);
            }

            if (!runSilently) {
                JOptionPane.showMessageDialog(null, "Finished dumping DSP file for " + songName);
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (!runSilently) {
                JOptionPane.showMessageDialog(null, "An error occurred: " + e.getMessage());
            }
        }
    }

    private static File promptForOutputFolder() {
        return chooseOutputDirectory();
    }

    private static void writeDSP(String fileName, long nibbleCount, long sampleRate, int loopFlag, long loopStart, int j, RandomAccessFile raf, long ch1CoefOffs, long ch1Start, long ch2CoefOffs, long ch2Start) throws IOException {
        try (FileOutputStream out = new FileOutputStream(fileName)) {
            PDTFileIO.writeU32BE(out, nibblesToSamples(nibbleCount));
            PDTFileIO.writeU32BE(out, nibbleCount);
            PDTFileIO.writeU32BE(out, sampleRate);
            PDTFileIO.writeU16BE(out, loopFlag);
            PDTFileIO.writeU16BE(out, 0);
            PDTFileIO.writeU32BE(out, loopStart);
            PDTFileIO.writeU32BE(out, nibbleCount - 1);
            PDTFileIO.writeU32BE(out, 0);

            if (j == 0) {
                raf.seek(ch1CoefOffs);
                for (int k = 0; k < 16; k++) {
                    PDTFileIO.writeU16BE(out, PDTFileIO.readU16BE(raf));
                }

                PDTFileIO.writeU16BE(out, 0);
                raf.seek(ch1Start);
                PDTFileIO.writeU16BE(out, PDTFileIO.readU8BE(raf));

                for (int k = 0; k < 16; k++) {
                    PDTFileIO.writeU16BE(out, 0);
                }

                raf.seek(ch1Start);
                writeDSPAudioData(nibbleCount, raf, out);
            }

            else {
                raf.seek(ch2CoefOffs);
                for (int k = 0; k < 16; k++) {
                    PDTFileIO.writeU16BE(out, PDTFileIO.readU16BE(raf));
                }

                PDTFileIO.writeU16BE(out, 0);
                raf.seek(ch2Start);
                PDTFileIO.writeU16BE(out, PDTFileIO.readU8BE(raf));

                for (int k = 0; k < 16; k++) {
                    PDTFileIO.writeU16BE(out, 0);
                }

                raf.seek(ch2Start);
                writeDSPAudioData(nibbleCount, raf, out);
            }
        }
    }

    private static void writeDSPAudioData(long nibbleCount, RandomAccessFile raf, FileOutputStream out) throws IOException {
        long bytesToCopy = nibbleCount / 2;

        byte[] buffer = new byte[8192];

        while (bytesToCopy > 0) {
            int chunkSize = (int) Math.min(buffer.length, bytesToCopy);
            int bytesRead = raf.read(buffer, 0, chunkSize);

            if (bytesRead == -1) {
                break;
            }

            out.write(buffer, 0, bytesRead);
            bytesToCopy -= bytesRead;
        }
    }

    public static void dumpAllSongs(File selectedFile) {
        File outputDir = promptForOutputFolder();
        if (outputDir == null) return;

        try (RandomAccessFile raf = new RandomAccessFile(selectedFile, "r")) {
            int unk00 = PDTFileIO.readU16BE(raf);
            int numFiles = PDTFileIO.readU16BE(raf);
            long unk04 = PDTFileIO.readU32BE(raf);
            long unk08 = PDTFileIO.readU32BE(raf);
            long unk0C = PDTFileIO.readU32BE(raf);
            long entryOffs = PDTFileIO.readU32BE(raf);
            long coeffOffs = PDTFileIO.readU32BE(raf);
            long headerOffs = PDTFileIO.readU32BE(raf);
            long streamOffs = PDTFileIO.readU32BE(raf);


            for (int i=0; i<numFiles; i++) {
                raf.seek(entryOffs + (i << 2));
                long thisHeaderOffs = PDTFileIO.readU32BE(raf);
                if (thisHeaderOffs == 0) {
                    continue;
                }

                raf.seek(thisHeaderOffs);
                long flags = PDTFileIO.readU32BE(raf);
                long sampleRate = PDTFileIO.readU32BE(raf);
                long nibbleCount = PDTFileIO.readU32BE(raf);
                long loopStart = PDTFileIO.readU32BE(raf);
                long ch1Start = PDTFileIO.readU32BE(raf);
                int ch1CoefEntry = PDTFileIO.readU16BE(raf);
                int unk116 = PDTFileIO.readU16BE(raf);
                long ch1CoefOffs = coeffOffs + (ch1CoefEntry << 5);

                long ch2Start = ch1Start;
                int ch2CoefEntry = ch1CoefEntry;
                long ch2CoefOffs = coeffOffs + (ch2CoefEntry << 5);
                int chanCount = 1;

                if ((flags & 0x01000000) != 0) {
                    ch2Start = PDTFileIO.readU32BE(raf);
                    ch2CoefEntry = PDTFileIO.readU16BE(raf);
                    int unk11A = PDTFileIO.readU16BE(raf);
                    ch2CoefOffs = coeffOffs + (ch2CoefEntry << 5);
                    chanCount = 2;
                }

                int loopFlag = ((flags & 0x02000000) != 0) ? 1 : 0;

                for (int j = 0; j < chanCount; j++) {
                    String fileName = getFileNameForIndex(chanCount, j, i);
                    File outputFile = new File(outputDir, fileName);
                    writeDSP(outputFile.getAbsolutePath(), nibbleCount, sampleRate, loopFlag, loopStart, j, raf, ch1CoefOffs, ch1Start, ch2CoefOffs, ch2Start);
                }
            }

            JOptionPane.showMessageDialog(null, "Finished dumping all songs from PDT file");
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "An error occurred: " + e.getMessage());
        }
    }

    private static File chooseOutputDirectory() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select the DSP output folder");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);

        int result = chooser.showSaveDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile();
        } else {
            return null;
        }
    }

    private static String getFileNameForIndex(int chanCount, int j, int i) {
        String fileName;

        if (chanCount == 2) {
            if (j == 0) {
                fileName = String.format("%04d_L.dsp", i);
            } else {
                fileName = String.format("%04d_R.dsp", i);
            }
        } else {
            fileName = String.format("%04d.dsp", i);
        }
        return fileName;
    }

    private static String getFileName(String songName, int chanCount, int j) {
        String fileName;

        String sanitizedSongName = songName.replace(" ", "_").replaceAll("[^A-Za-z0-9_]", "");

        if (chanCount == 2) {
            if (j == 0) {
                fileName = String.format("%s_L.dsp", sanitizedSongName);
            } else {
                fileName = String.format("%s_R.dsp", sanitizedSongName);
            }
        } else {
            fileName = String.format("%s.dsp", sanitizedSongName);
        }
        return fileName;
    }
}