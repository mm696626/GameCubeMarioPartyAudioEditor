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

    public static void dumpSong(File selectedFile, int songIndex, String songName, File defaultDumpOutputFolder) {
        File outputDir;

        if (defaultDumpOutputFolder == null) {
            outputDir = promptForOutputFolder();
            if (outputDir == null) return;
        }
        else {
            outputDir = defaultDumpOutputFolder;
        }

        try (RandomAccessFile raf = new RandomAccessFile(selectedFile, "r")) {
            int unk00 = FileIO.readU16BE(raf);
            int numFiles = FileIO.readU16BE(raf);
            long unk04 = FileIO.readU32BE(raf);
            long unk08 = FileIO.readU32BE(raf);
            long unk0C = FileIO.readU32BE(raf);
            long entryOffs = FileIO.readU32BE(raf);
            long coeffOffs = FileIO.readU32BE(raf);
            long headerOffs = FileIO.readU32BE(raf);
            long streamOffs = FileIO.readU32BE(raf);

            if (songIndex < 0 || songIndex >= numFiles) {
                return;
            }

            raf.seek(entryOffs + (songIndex << 2));
            long thisHeaderOffs = FileIO.readU32BE(raf);
            if (thisHeaderOffs == 0) {
                return;
            }

            raf.seek(thisHeaderOffs);
            long flags = FileIO.readU32BE(raf);
            long sampleRate = FileIO.readU32BE(raf);
            long nibbleCount = FileIO.readU32BE(raf);
            long loopStart = FileIO.readU32BE(raf);
            long ch1Start = FileIO.readU32BE(raf);
            int ch1CoefEntry = FileIO.readU16BE(raf);
            int unk116 = FileIO.readU16BE(raf);
            long ch1CoefOffs = coeffOffs + (ch1CoefEntry << 5);

            long ch2Start = ch1Start;
            int ch2CoefEntry = ch1CoefEntry;
            long ch2CoefOffs = coeffOffs + (ch2CoefEntry << 5);
            int chanCount = 1;

            if ((flags & 0x01000000) != 0) {
                ch2Start = FileIO.readU32BE(raf);
                ch2CoefEntry = FileIO.readU16BE(raf);
                int unk11A = FileIO.readU16BE(raf);
                ch2CoefOffs = coeffOffs + (ch2CoefEntry << 5);
                chanCount = 2;
            }

            int loopFlag = ((flags & 0x02000000) != 0) ? 1 : 0;

            for (int j = 0; j < chanCount; j++) {
                String fileName = getFileName(songName, chanCount, j);
                File outputFile = new File(outputDir, fileName);
                writeDSP(outputFile.getAbsolutePath(), nibbleCount, sampleRate, loopFlag, loopStart, j, raf, ch1CoefOffs, ch1Start, ch2CoefOffs, ch2Start);
            }

            JOptionPane.showMessageDialog(null, "Finished dumping DSP file for " + songName);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "An error occurred: " + e.getMessage());
        }
    }

    private static File promptForOutputFolder() {
        return chooseOutputDirectory();
    }

    private static void writeDSP(String fileName, long nibbleCount, long sampleRate, int loopFlag, long loopStart, int j, RandomAccessFile raf, long ch1CoefOffs, long ch1Start, long ch2CoefOffs, long ch2Start) throws IOException {
        try (FileOutputStream out = new FileOutputStream(fileName)) {
            FileIO.writeU32BE(out, nibblesToSamples(nibbleCount));
            FileIO.writeU32BE(out, nibbleCount);
            FileIO.writeU32BE(out, sampleRate);
            FileIO.writeU16BE(out, loopFlag);
            FileIO.writeU16BE(out, 0);
            FileIO.writeU32BE(out, loopStart);
            FileIO.writeU32BE(out, nibbleCount - 1);
            FileIO.writeU32BE(out, 0);

            if (j == 0) {
                raf.seek(ch1CoefOffs);
                for (int k = 0; k < 16; k++) {
                    FileIO.writeU16BE(out, FileIO.readU16BE(raf));
                }

                FileIO.writeU16BE(out, 0);
                raf.seek(ch1Start);
                FileIO.writeU16BE(out, FileIO.readU8BE(raf));

                for (int k = 0; k < 16; k++) {
                    FileIO.writeU16BE(out, 0);
                }

                raf.seek(ch1Start);
                writeDSPAudioData(nibbleCount, raf, out);
            }

            else {
                raf.seek(ch2CoefOffs);
                for (int k = 0; k < 16; k++) {
                    FileIO.writeU16BE(out, FileIO.readU16BE(raf));
                }

                FileIO.writeU16BE(out, 0);
                raf.seek(ch2Start);
                FileIO.writeU16BE(out, FileIO.readU8BE(raf));

                for (int k = 0; k < 16; k++) {
                    FileIO.writeU16BE(out, 0);
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

    public static void dumpAllSongs(File selectedFile, File defaultDumpOutputFolder) {
        File outputDir;

        if (defaultDumpOutputFolder == null) {
            outputDir = promptForOutputFolder();
            if (outputDir == null) return;
        }
        else {
            outputDir = defaultDumpOutputFolder;
        }

        try (RandomAccessFile raf = new RandomAccessFile(selectedFile, "r")) {
            int unk00 = FileIO.readU16BE(raf);
            int numFiles = FileIO.readU16BE(raf);
            long unk04 = FileIO.readU32BE(raf);
            long unk08 = FileIO.readU32BE(raf);
            long unk0C = FileIO.readU32BE(raf);
            long entryOffs = FileIO.readU32BE(raf);
            long coeffOffs = FileIO.readU32BE(raf);
            long headerOffs = FileIO.readU32BE(raf);
            long streamOffs = FileIO.readU32BE(raf);


            for (int i=0; i<numFiles; i++) {
                raf.seek(entryOffs + (i << 2));
                long thisHeaderOffs = FileIO.readU32BE(raf);
                if (thisHeaderOffs == 0) {
                    continue;
                }

                raf.seek(thisHeaderOffs);
                long flags = FileIO.readU32BE(raf);
                long sampleRate = FileIO.readU32BE(raf);
                long nibbleCount = FileIO.readU32BE(raf);
                long loopStart = FileIO.readU32BE(raf);
                long ch1Start = FileIO.readU32BE(raf);
                int ch1CoefEntry = FileIO.readU16BE(raf);
                int unk116 = FileIO.readU16BE(raf);
                long ch1CoefOffs = coeffOffs + (ch1CoefEntry << 5);

                long ch2Start = ch1Start;
                int ch2CoefEntry = ch1CoefEntry;
                long ch2CoefOffs = coeffOffs + (ch2CoefEntry << 5);
                int chanCount = 1;

                if ((flags & 0x01000000) != 0) {
                    ch2Start = FileIO.readU32BE(raf);
                    ch2CoefEntry = FileIO.readU16BE(raf);
                    int unk11A = FileIO.readU16BE(raf);
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