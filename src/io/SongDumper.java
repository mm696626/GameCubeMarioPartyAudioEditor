package io;

import javax.swing.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;

public class SongDumper {

    //This code is largely derived from Yoshimaster96's C PDT dumping code, so huge credit and kudos to them!
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

    // Method to extract a specific song based on the index and input file
    public static void extractSong(File selectedFile, int songIndex) {
        try (RandomAccessFile raf = new RandomAccessFile(selectedFile, "r")) {
            int unk00 = BinaryIO.readU16BE(raf);
            int numFiles = BinaryIO.readU16BE(raf);
            long unk04 = BinaryIO.readU32BE(raf);
            long unk08 = BinaryIO.readU32BE(raf);
            long unk0C = BinaryIO.readU32BE(raf);
            long entryOffs = BinaryIO.readU32BE(raf);
            long coeffOffs = BinaryIO.readU32BE(raf);
            long headerOffs = BinaryIO.readU32BE(raf);
            long streamOffs = BinaryIO.readU32BE(raf);

            if (songIndex < 0 || songIndex >= numFiles) {
                JOptionPane.showMessageDialog(null, "Invalid song index.");
                return;
            }

            // Seek to the specific song entry
            raf.seek(entryOffs + (songIndex << 2));
            long thisHeaderOffs = BinaryIO.readU32BE(raf);
            if (thisHeaderOffs == 0) {
                JOptionPane.showMessageDialog(null, "No song data found for this index.");
                return;
            }

            raf.seek(thisHeaderOffs);
            long flags = BinaryIO.readU32BE(raf);
            long sampleRate = BinaryIO.readU32BE(raf);
            long nibbleCount = BinaryIO.readU32BE(raf);
            long loopStart = BinaryIO.readU32BE(raf);
            long ch1Start = BinaryIO.readU32BE(raf);
            int ch1CoefEntry = BinaryIO.readU16BE(raf);
            int unk116 = BinaryIO.readU16BE(raf);
            long ch1CoefOffs = coeffOffs + (ch1CoefEntry << 5);

            long ch2Start = ch1Start;
            int ch2CoefEntry = ch1CoefEntry;
            long ch2CoefOffs = coeffOffs + (ch2CoefEntry << 5);
            int chanCount = 1;

            if ((flags & 0x01000000) != 0) {
                ch2Start = BinaryIO.readU32BE(raf);
                ch2CoefEntry = BinaryIO.readU16BE(raf);
                int unk11A = BinaryIO.readU16BE(raf);
                ch2CoefOffs = coeffOffs + (ch2CoefEntry << 5);
                chanCount = 2;
            }

            int loopFlag = ((flags & 0x02000000) != 0) ? 1 : 0;

            for (int j = 0; j < chanCount; j++) {

                String fname;


                if (chanCount == 2) {
                    if (j == 0) {
                        fname = String.format("%04d_L.dsp", songIndex);
                    } else {
                        fname = String.format("%04d_R.dsp", songIndex);
                    }
                } else {
                    fname = String.format("%04d.dsp", songIndex);
                }


                try (FileOutputStream out = new FileOutputStream(fname)) {
                    BinaryIO.writeU32BE(out, nibblesToSamples(nibbleCount));
                    BinaryIO.writeU32BE(out, nibbleCount);
                    BinaryIO.writeU32BE(out, sampleRate);
                    BinaryIO.writeU16BE(out, loopFlag);
                    BinaryIO.writeU16BE(out, 0);
                    BinaryIO.writeU32BE(out, loopStart);
                    BinaryIO.writeU32BE(out, nibbleCount - 1);
                    BinaryIO.writeU32BE(out, 0);

                    if (j == 0) {
                        raf.seek(ch1CoefOffs);
                        for (int k = 0; k < 16; k++) {
                            BinaryIO.writeU16BE(out, BinaryIO.readU16BE(raf));
                        }

                        BinaryIO.writeU16BE(out, 0);
                        raf.seek(ch1Start);
                        BinaryIO.writeU16BE(out, BinaryIO.readU8BE(raf));
                        for (int k = 0; k < 5; k++) {
                            BinaryIO.writeU16BE(out, 0);
                        }

                        for (int k = 0; k < 11; k++) {
                            BinaryIO.writeU16BE(out, 0);
                        }

                        raf.seek(ch1Start);
                        for (int k = 0; k < (nibbleCount << 1); k++) {
                            out.write(raf.readUnsignedByte());
                        }
                    }

                    else {
                        raf.seek(ch2CoefOffs);
                        for (int k = 0; k < 16; k++) {
                            BinaryIO.writeU16BE(out, BinaryIO.readU16BE(raf));
                        }

                        BinaryIO.writeU16BE(out, 0);
                        raf.seek(ch2Start);
                        BinaryIO.writeU16BE(out, BinaryIO.readU8BE(raf));
                        for (int k = 0; k < 5; k++) {
                            BinaryIO.writeU16BE(out, 0);
                        }

                        for (int k = 0; k < 11; k++) {
                            BinaryIO.writeU16BE(out, 0);
                        }

                        raf.seek(ch2Start);
                        for (int k = 0; k < (nibbleCount << 1); k++) {
                            out.write(raf.readUnsignedByte());
                        }
                    }
                }
            }

            JOptionPane.showMessageDialog(null, "Finished extracting DSP file for song index: " + songIndex);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "An error occurred: " + e.getMessage());
        }
    }
}
