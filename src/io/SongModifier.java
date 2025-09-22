package io;

import constants.DSPFileConstants;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class SongModifier {

    //This code is largely derived from Yoshimaster96's C PDT dumping code, so huge credit and kudos to them!
    //Code: https://github.com/Yoshimaster96/mpgc-sound-tools

    public static void modifySong(File pdtFile, File leftChannel, File rightChannel, int songIndex, String songName) {
        try (RandomAccessFile pdtRaf = new RandomAccessFile(pdtFile, "rw")) {
            int unk00 = PDTFileIO.readU16BE(pdtRaf);
            int numFiles = PDTFileIO.readU16BE(pdtRaf);
            long unk04 = PDTFileIO.readU32BE(pdtRaf);
            long unk08 = PDTFileIO.readU32BE(pdtRaf);
            long unk0C = PDTFileIO.readU32BE(pdtRaf);
            long entryOffs = PDTFileIO.readU32BE(pdtRaf);
            long coeffOffs = PDTFileIO.readU32BE(pdtRaf);
            long headerOffs = PDTFileIO.readU32BE(pdtRaf);
            long streamOffs = PDTFileIO.readU32BE(pdtRaf);

            if (songIndex < 0 || songIndex >= numFiles) {
                JOptionPane.showMessageDialog(null, "Invalid song index.");
                return;
            }

            pdtRaf.seek(entryOffs + (songIndex << 2));
            long thisHeaderOffs = PDTFileIO.readU32BE(pdtRaf);
            if (thisHeaderOffs == 0) {
                JOptionPane.showMessageDialog(null, "No song data found for this index.");
                return;
            }

            pdtRaf.seek(thisHeaderOffs);

            long flags = PDTFileIO.readU32BE(pdtRaf);
            long sampleRate = PDTFileIO.readU32BE(pdtRaf);
            long nibbleCount = PDTFileIO.readU32BE(pdtRaf);
            long loopStart = PDTFileIO.readU32BE(pdtRaf);
            long ch1Start = PDTFileIO.readU32BE(pdtRaf);
            int ch1CoefEntry = PDTFileIO.readU16BE(pdtRaf);
            int unk116 = PDTFileIO.readU16BE(pdtRaf);
            long ch1CoefOffs = coeffOffs + (ch1CoefEntry << 5);

            long ch2Start = ch1Start;
            int ch2CoefEntry = ch1CoefEntry;
            long ch2CoefOffs = coeffOffs + (ch2CoefEntry << 5);
            int chanCount = 1;

            if ((flags & 0x01000000) != 0) {
                ch2Start = PDTFileIO.readU32BE(pdtRaf);
                ch2CoefEntry = PDTFileIO.readU16BE(pdtRaf);
                int unk11A = PDTFileIO.readU16BE(pdtRaf);
                ch2CoefOffs = coeffOffs + (ch2CoefEntry << 5);
                chanCount = 2;
            }

            if (chanCount == 1) {
                int response = JOptionPane.showConfirmDialog(
                        null,
                        "The song you're replacing isn't stereo. Do you want to continue?",
                        "Mono DSP Found",
                        JOptionPane.YES_NO_OPTION
                );

                if (response != JOptionPane.YES_OPTION) {
                    return;
                }
            }

            //read song info from left DSP channel (same for right, so only have to read from the left channel)
            byte[] dspSampleRate = new byte[DSPFileConstants.SAMPLE_RATE_LENGTH_IN_BYTES];
            try (RandomAccessFile leftChannelRaf = new RandomAccessFile(leftChannel, "r")) {
                leftChannelRaf.seek(DSPFileConstants.SAMPLE_RATE_OFFSET);
                leftChannelRaf.read(dspSampleRate);
            }

            byte[] dspNibbleCount = new byte[DSPFileConstants.NIBBLE_COUNT_LENGTH_IN_BYTES];
            try (RandomAccessFile leftChannelRaf = new RandomAccessFile(leftChannel, "r")) {
                leftChannelRaf.seek(DSPFileConstants.NIBBLE_COUNT_OFFSET);
                leftChannelRaf.read(dspNibbleCount);
            }

            byte[] dspLoopStart = new byte[DSPFileConstants.LOOP_START_LENGTH_IN_BYTES];
            try (RandomAccessFile leftChannelRaf = new RandomAccessFile(leftChannel, "r")) {
                leftChannelRaf.seek(DSPFileConstants.LOOP_START_OFFSET);
                leftChannelRaf.read(dspLoopStart);
            }

            //read left decode coeffs data
            byte[] leftChannelDecodingCoeffs = new byte[DSPFileConstants.DECODE_COEFFS_LENGTH_IN_BYTES];
            try (RandomAccessFile leftChannelRaf = new RandomAccessFile(leftChannel, "r")) {
                leftChannelRaf.seek(DSPFileConstants.DECODE_COEFFS_OFFSET);
                leftChannelRaf.read(leftChannelDecodingCoeffs);
            }

            //read right decode coeffs data
            byte[] rightChannelDecodingCoeffs = new byte[DSPFileConstants.DECODE_COEFFS_LENGTH_IN_BYTES];
            try (RandomAccessFile rightChannelRaf = new RandomAccessFile(rightChannel, "r")) {
                rightChannelRaf.seek(DSPFileConstants.DECODE_COEFFS_OFFSET);
                rightChannelRaf.read(rightChannelDecodingCoeffs);
            }

            //read left channel audio data
            byte[] leftChannelAudio;

            try (RandomAccessFile raf = new RandomAccessFile(leftChannel, "r")) {
                raf.seek(DSPFileConstants.AUDIO_DATA_OFFSET);
                long remainingBytes = raf.length() - DSPFileConstants.AUDIO_DATA_OFFSET;
                leftChannelAudio = new byte[(int) remainingBytes];
                raf.readFully(leftChannelAudio);
            }

            //read right channel audio data
            byte[] rightChannelAudio;

            try (RandomAccessFile raf = new RandomAccessFile(rightChannel, "r")) {
                raf.seek(DSPFileConstants.AUDIO_DATA_OFFSET);
                long remainingBytes = raf.length() - DSPFileConstants.AUDIO_DATA_OFFSET;
                rightChannelAudio = new byte[(int) remainingBytes];
                raf.readFully(rightChannelAudio);
            }

            //modify song

            if (isInvalidSize(dspNibbleCount, nibbleCount)) return;

            long newSampleRateOffset = thisHeaderOffs + 4;
            long newNibbleCount = thisHeaderOffs + 8;
            long newLoopStartOffset = thisHeaderOffs + 12;

            writeDSPToPDT(pdtRaf, newSampleRateOffset, dspSampleRate, newNibbleCount, dspNibbleCount, newLoopStartOffset, dspLoopStart, ch1CoefOffs, leftChannelDecodingCoeffs, ch2CoefOffs, rightChannelDecodingCoeffs, ch1Start, leftChannelAudio, ch2Start, rightChannelAudio);
            pdtRaf.close();

            JOptionPane.showMessageDialog(null, "Finished modifying PDT file for " + songName);

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "An error occurred: " + e.getMessage());
        }
    }

    private static boolean isInvalidSize(byte[] dspNibbleCount, long nibbleCount) {
        //size check
        long newDSPSize = ((long) ((dspNibbleCount[0] & 0xFF) << 24)
                | ((dspNibbleCount[1] & 0xFF) << 16)
                | ((dspNibbleCount[2] & 0xFF) << 8)
                | (dspNibbleCount[3] & 0xFF));

        if (newDSPSize > nibbleCount) {
            JOptionPane.showMessageDialog(null, "Your song is too big! Try again!");
            return true;
        }
        return false;
    }

    private static void writeDSPToPDT(RandomAccessFile pdtRaf, long newSampleRateOffset, byte[] dspSampleRate, long newNibbleCount, byte[] dspNibbleCount, long newLoopStartOffset, byte[] dspLoopStart, long ch1CoefOffs, byte[] leftChannelDecodingCoeffs, long ch2CoefOffs, byte[] rightChannelDecodingCoeffs, long ch1Start, byte[] leftChannelAudio, long ch2Start, byte[] rightChannelAudio) throws IOException {
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
    }
}