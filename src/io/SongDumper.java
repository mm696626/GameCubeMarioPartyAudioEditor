package io;

import javax.swing.*;
import java.io.File;
import java.io.FileOutputStream;
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

    public static void dumpSong(File selectedFile, int songIndex, String songName) {
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
                JOptionPane.showMessageDialog(null, "Invalid song index.");
                return;
            }

            // Seek to the specific song entry
            raf.seek(entryOffs + (songIndex << 2));
            long thisHeaderOffs = PDTFileIO.readU32BE(raf);
            if (thisHeaderOffs == 0) {
                JOptionPane.showMessageDialog(null, "No song data found for this index.");
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
                        for (int k = 0; k < 5; k++) {
                            PDTFileIO.writeU16BE(out, 0);
                        }

                        for (int k = 0; k < 11; k++) {
                            PDTFileIO.writeU16BE(out, 0);
                        }

                        raf.seek(ch1Start);
                        for (int k = 0; k < nibbleCount/2; k++) {
                            out.write(raf.readUnsignedByte());
                        }
                    }

                    else {
                        raf.seek(ch2CoefOffs);
                        for (int k = 0; k < 16; k++) {
                            PDTFileIO.writeU16BE(out, PDTFileIO.readU16BE(raf));
                        }

                        PDTFileIO.writeU16BE(out, 0);
                        raf.seek(ch2Start);
                        PDTFileIO.writeU16BE(out, PDTFileIO.readU8BE(raf));
                        for (int k = 0; k < 5; k++) {
                            PDTFileIO.writeU16BE(out, 0);
                        }

                        for (int k = 0; k < 11; k++) {
                            PDTFileIO.writeU16BE(out, 0);
                        }

                        raf.seek(ch2Start);
                        for (int k = 0; k < nibbleCount/2; k++) {
                            out.write(raf.readUnsignedByte());
                        }
                    }
                }
            }

            JOptionPane.showMessageDialog(null, "Finished extracting DSP file for " + songName);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "An error occurred: " + e.getMessage());
        }
    }

    public static void dumpAllSongs(File selectedFile) {
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
                // Seek to the specific song entry
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
                            for (int k = 0; k < 5; k++) {
                                PDTFileIO.writeU16BE(out, 0);
                            }

                            for (int k = 0; k < 11; k++) {
                                PDTFileIO.writeU16BE(out, 0);
                            }

                            raf.seek(ch1Start);
                            for (int k = 0; k < nibbleCount/2; k++) {
                                out.write(raf.readUnsignedByte());
                            }
                        }

                        else {
                            raf.seek(ch2CoefOffs);
                            for (int k = 0; k < 16; k++) {
                                PDTFileIO.writeU16BE(out, PDTFileIO.readU16BE(raf));
                            }

                            PDTFileIO.writeU16BE(out, 0);
                            raf.seek(ch2Start);
                            PDTFileIO.writeU16BE(out, PDTFileIO.readU8BE(raf));
                            for (int k = 0; k < 5; k++) {
                                PDTFileIO.writeU16BE(out, 0);
                            }

                            for (int k = 0; k < 11; k++) {
                                PDTFileIO.writeU16BE(out, 0);
                            }

                            raf.seek(ch2Start);
                            for (int k = 0; k < nibbleCount/2; k++) {
                                out.write(raf.readUnsignedByte());
                            }
                        }
                    }
                }
            }

            JOptionPane.showMessageDialog(null, "Finished all songs from PDT file");
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "An error occurred: " + e.getMessage());
        }
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