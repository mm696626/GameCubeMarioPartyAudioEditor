package io;

import javax.swing.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;

public class SongModifier {

    public static void modifySong(File pdtFile, File leftChannel, File rightChannel, int songIndex) {
        try (RandomAccessFile pdtRaf = new RandomAccessFile(pdtFile, "rw")) {
            int unk00 = BinaryIO.readU16BE(pdtRaf);
            int numFiles = BinaryIO.readU16BE(pdtRaf);
            long unk04 = BinaryIO.readU32BE(pdtRaf);
            long unk08 = BinaryIO.readU32BE(pdtRaf);
            long unk0C = BinaryIO.readU32BE(pdtRaf);
            long entryOffs = BinaryIO.readU32BE(pdtRaf);
            long coeffOffs = BinaryIO.readU32BE(pdtRaf);
            long headerOffs = BinaryIO.readU32BE(pdtRaf);
            long streamOffs = BinaryIO.readU32BE(pdtRaf);

            if (songIndex < 0 || songIndex >= numFiles) {
                JOptionPane.showMessageDialog(null, "Invalid song index.");
                return;
            }

            // Seek to the specific song entry
            pdtRaf.seek(entryOffs + (songIndex << 2));
            long thisHeaderOffs = BinaryIO.readU32BE(pdtRaf);
            if (thisHeaderOffs == 0) {
                JOptionPane.showMessageDialog(null, "No song data found for this index.");
                return;
            }

            pdtRaf.seek(thisHeaderOffs);

            long flags = BinaryIO.readU32BE(pdtRaf);
            long sampleRate = BinaryIO.readU32BE(pdtRaf);
            long nibbleCount = BinaryIO.readU32BE(pdtRaf);
            long loopStart = BinaryIO.readU32BE(pdtRaf);
            long ch1Start = BinaryIO.readU32BE(pdtRaf);
            int ch1CoefEntry = BinaryIO.readU16BE(pdtRaf);
            int unk116 = BinaryIO.readU16BE(pdtRaf);
            long ch1CoefOffs = coeffOffs + (ch1CoefEntry << 5);

            long ch2Start = ch1Start;
            int ch2CoefEntry = ch1CoefEntry;
            long ch2CoefOffs = coeffOffs + (ch2CoefEntry << 5);
            int chanCount = 1;

            if ((flags & 0x01000000) != 0) {
                ch2Start = BinaryIO.readU32BE(pdtRaf);
                ch2CoefEntry = BinaryIO.readU16BE(pdtRaf);
                int unk11A = BinaryIO.readU16BE(pdtRaf);
                ch2CoefOffs = coeffOffs + (ch2CoefEntry << 5);
                chanCount = 2;
            }

            //read song info from left DSP channel (same for right)
            byte[] dspSampleRate = new byte[4];
            try (RandomAccessFile leftChannelRaf = new RandomAccessFile(leftChannel, "r")) {
                leftChannelRaf.seek(0x8);
                leftChannelRaf.read(dspSampleRate);
            }

            byte[] dspNibbleCount = new byte[4];
            try (RandomAccessFile leftChannelRaf = new RandomAccessFile(leftChannel, "r")) {
                leftChannelRaf.seek(0x4);
                leftChannelRaf.read(dspNibbleCount);
            }

            byte[] dspLoopStart = new byte[4];
            try (RandomAccessFile leftChannelRaf = new RandomAccessFile(leftChannel, "r")) {
                leftChannelRaf.seek(0x10);
                leftChannelRaf.read(dspLoopStart);
            }

            //read left decode coeffs data
            byte[] leftChannelDecodingCoeffs = new byte[32];
            try (RandomAccessFile leftChannelRaf = new RandomAccessFile(leftChannel, "r")) {
                leftChannelRaf.seek(0x1C);
                leftChannelRaf.read(leftChannelDecodingCoeffs);
            }

            //read right decode coeffs data
            byte[] rightChannelDecodingCoeffs = new byte[32];
            try (RandomAccessFile rightChannelRaf = new RandomAccessFile(rightChannel, "r")) {
                rightChannelRaf.seek(0x1C);
                rightChannelRaf.read(rightChannelDecodingCoeffs);
            }

            //read left channel audio data
            byte[] leftChannelAudio;

            try (RandomAccessFile raf = new RandomAccessFile(leftChannel, "r")) {
                // Seek to the specified offset
                raf.seek(0x60);

                // Get the remaining bytes from current position to EOF
                long remainingBytes = raf.length() - 0x60;

                // Read the remaining bytes into a byte array
                leftChannelAudio = new byte[(int) remainingBytes];
                raf.readFully(leftChannelAudio);  // Reads the bytes from current position to EOF
            }

            //read right channel audio data
            byte[] rightChannelAudio;

            try (RandomAccessFile raf = new RandomAccessFile(rightChannel, "r")) {
                // Seek to the specified offset
                raf.seek(0x60);

                // Get the remaining bytes from current position to EOF
                long remainingBytes = raf.length() - 0x60;

                // Read the remaining bytes into a byte array
                rightChannelAudio = new byte[(int) remainingBytes];
                raf.readFully(rightChannelAudio);  // Reads the bytes from current position to EOF
            }

            //modify song

            //size check
            SongDumper.extractSong(pdtFile, songIndex, false);
            File songFile = new File("temp.dsp");
            if (leftChannel.length() > songFile.length()) {
                JOptionPane.showMessageDialog(null, "Your song is too big! Try again!");
                deleteTempFile(songFile);
                return;
            }

            deleteTempFile(songFile);

            long newSampleRateOffset = thisHeaderOffs + 4;
            long newNibbleCount = thisHeaderOffs + 8;
            long newLoopStartOffset = thisHeaderOffs + 12;

            pdtRaf.seek(newSampleRateOffset);
            pdtRaf.write(dspSampleRate);

            pdtRaf.seek(newNibbleCount);
            pdtRaf.write(dspNibbleCount);

            pdtRaf.seek(newLoopStartOffset);
            pdtRaf.write(dspLoopStart);

            pdtRaf.seek(ch1CoefOffs);
            pdtRaf.write(leftChannelDecodingCoeffs);

            pdtRaf.seek(ch2CoefOffs);
            pdtRaf.write(rightChannelDecodingCoeffs);

            pdtRaf.seek(ch1Start);
            pdtRaf.write(leftChannelAudio);

            pdtRaf.seek(ch2Start);
            pdtRaf.write(rightChannelAudio);


            pdtRaf.close();

            JOptionPane.showMessageDialog(null, "Finished modifying PDT file for song index: " + songIndex);

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "An error occurred: " + e.getMessage());
        }
    }

    private static void deleteTempFile(File songFile) {
        if (songFile.exists()) {
            songFile.delete();
        }
    }
}
