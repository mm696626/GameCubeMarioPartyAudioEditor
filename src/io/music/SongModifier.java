package io.music;

import constants.DSPFileConstants;
import io.FileIO;

import javax.swing.*;
import java.io.*;
import java.util.Scanner;

public class SongModifier {

    //This code is largely derived from Yoshimaster96's C PDT dumping code, so huge credit and kudos to them!
    //Code: https://github.com/Yoshimaster96/mpgc-sound-tools

    public static boolean modifySong(File pdtFile, File leftChannel, File rightChannel, int songIndex, String songName, String selectedGame) {
        try (RandomAccessFile pdtRaf = new RandomAccessFile(pdtFile, "rw")) {
            int unk00 = FileIO.readU16BE(pdtRaf);
            int numFiles = FileIO.readU16BE(pdtRaf);
            long unk04 = FileIO.readU32BE(pdtRaf);
            long unk08 = FileIO.readU32BE(pdtRaf);
            long unk0C = FileIO.readU32BE(pdtRaf);
            long entryOffs = FileIO.readU32BE(pdtRaf);
            long coeffOffs = FileIO.readU32BE(pdtRaf);
            long headerOffs = FileIO.readU32BE(pdtRaf);
            long streamOffs = FileIO.readU32BE(pdtRaf);

            if (songIndex < 0 || songIndex >= numFiles) {
                return false;
            }

            pdtRaf.seek(entryOffs + (songIndex << 2));
            long thisHeaderOffs = FileIO.readU32BE(pdtRaf);
            if (thisHeaderOffs == 0) {
                return false;
            }

            pdtRaf.seek(thisHeaderOffs);

            long flags = FileIO.readU32BE(pdtRaf);
            long sampleRate = FileIO.readU32BE(pdtRaf);
            long nibbleCount = FileIO.readU32BE(pdtRaf);
            long loopStart = FileIO.readU32BE(pdtRaf);
            long ch1Start = FileIO.readU32BE(pdtRaf);
            int ch1CoefEntry = FileIO.readU16BE(pdtRaf);
            int unk116 = FileIO.readU16BE(pdtRaf);
            long ch1CoefOffs = coeffOffs + (ch1CoefEntry << 5);

            long ch2Start = ch1Start;
            int ch2CoefEntry = ch1CoefEntry;
            long ch2CoefOffs = coeffOffs + (ch2CoefEntry << 5);
            int chanCount = 1;

            if ((flags & 0x01000000) != 0) {
                ch2Start = FileIO.readU32BE(pdtRaf);
                ch2CoefEntry = FileIO.readU16BE(pdtRaf);
                int unk11A = FileIO.readU16BE(pdtRaf);
                ch2CoefOffs = coeffOffs + (ch2CoefEntry << 5);
                chanCount = 2;
            }

            //read song info from left DSP channel (same for right, so only have to read from the left channel)
            byte[] newDSPSampleRate = new byte[DSPFileConstants.SAMPLE_RATE_LENGTH_IN_BYTES];
            try (RandomAccessFile leftChannelRaf = new RandomAccessFile(leftChannel, "r")) {
                leftChannelRaf.seek(DSPFileConstants.SAMPLE_RATE_OFFSET);
                leftChannelRaf.read(newDSPSampleRate);
            }

            byte[] newDSPNibbleCount = new byte[DSPFileConstants.NIBBLE_COUNT_LENGTH_IN_BYTES];
            try (RandomAccessFile leftChannelRaf = new RandomAccessFile(leftChannel, "r")) {
                leftChannelRaf.seek(DSPFileConstants.NIBBLE_COUNT_OFFSET);
                leftChannelRaf.read(newDSPNibbleCount);
            }

            byte[] newDSPLoopStart = new byte[DSPFileConstants.LOOP_START_LENGTH_IN_BYTES];
            try (RandomAccessFile leftChannelRaf = new RandomAccessFile(leftChannel, "r")) {
                leftChannelRaf.seek(DSPFileConstants.LOOP_START_OFFSET);
                leftChannelRaf.read(newDSPLoopStart);
            }

            //read left decode coeffs data
            byte[] newDSPLeftChannelDecodingCoeffs = new byte[DSPFileConstants.DECODE_COEFFS_LENGTH_IN_BYTES];
            try (RandomAccessFile leftChannelRaf = new RandomAccessFile(leftChannel, "r")) {
                leftChannelRaf.seek(DSPFileConstants.DECODE_COEFFS_OFFSET);
                leftChannelRaf.read(newDSPLeftChannelDecodingCoeffs);
            }

            //read right decode coeffs data
            byte[] newDSPRightChannelDecodingCoeffs = new byte[DSPFileConstants.DECODE_COEFFS_LENGTH_IN_BYTES];
            try (RandomAccessFile rightChannelRaf = new RandomAccessFile(rightChannel, "r")) {
                rightChannelRaf.seek(DSPFileConstants.DECODE_COEFFS_OFFSET);
                rightChannelRaf.read(newDSPRightChannelDecodingCoeffs);
            }

            //read left channel audio data
            byte[] newDSPLeftChannelAudio;

            try (RandomAccessFile raf = new RandomAccessFile(leftChannel, "r")) {
                raf.seek(DSPFileConstants.AUDIO_DATA_OFFSET);
                long remainingBytes = raf.length() - DSPFileConstants.AUDIO_DATA_OFFSET;
                newDSPLeftChannelAudio = new byte[(int) remainingBytes];
                raf.readFully(newDSPLeftChannelAudio);
            }

            //read right channel audio data
            byte[] newDSPRightChannelAudio;

            try (RandomAccessFile raf = new RandomAccessFile(rightChannel, "r")) {
                raf.seek(DSPFileConstants.AUDIO_DATA_OFFSET);
                long remainingBytes = raf.length() - DSPFileConstants.AUDIO_DATA_OFFSET;
                newDSPRightChannelAudio = new byte[(int) remainingBytes];
                raf.readFully(newDSPRightChannelAudio);
            }

            //modify song

            //read the original nibble count (which was read from header) if the song has been replaced before (for edge case)
            //use that to compare instead of the header value
            long originalNibbleCount = readDSPNibbleCountFromFile(songName, selectedGame);

            if (originalNibbleCount != -1) {
                nibbleCount = originalNibbleCount;
            }

            if (isInvalidSize(newDSPNibbleCount, nibbleCount, songName)) return false;

            //if the nibble count here is -1, then the song hasn't been replaced yet, so write it
            if (originalNibbleCount == -1) {
                writeDSPNibbleCountToFile(songName, nibbleCount, selectedGame);
            }

            long newDSPSampleRateOffset = thisHeaderOffs + 4;
            long newDSPNibbleCountOffset = thisHeaderOffs + 8;
            long newDSPLoopStartOffset = thisHeaderOffs + 12;

            writeDSPToPDT(pdtRaf, newDSPSampleRateOffset, newDSPSampleRate, newDSPNibbleCountOffset, newDSPNibbleCount, newDSPLoopStartOffset, newDSPLoopStart, ch1CoefOffs, newDSPLeftChannelDecodingCoeffs, ch2CoefOffs, newDSPRightChannelDecodingCoeffs, ch1Start, newDSPLeftChannelAudio, ch2Start, newDSPRightChannelAudio);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "An error occurred: " + e.getMessage());
            return false;
        }
    }

    private static long readDSPNibbleCountFromFile(String songName, String selectedGame) {
        Scanner inputStream;

        try {
            inputStream = new Scanner(new FileInputStream("original_sizes/" + selectedGame + ".txt"));
        }
        catch (FileNotFoundException e) {
            return -1;
        }

        while (inputStream.hasNextLine()) {
            String line = inputStream.nextLine();
            if (line.split(":")[0].equals(songName)) {
                return Long.parseLong(line.split(":")[1]);
            }
        }

        return -1;
    }

    private static void writeDSPNibbleCountToFile(String songName, long nibbleCount, String selectedGame) {
        PrintWriter outputStream;

        try {
            File originalSizesFolder = new File("original_sizes");
            if (!originalSizesFolder.exists()) {
                originalSizesFolder.mkdirs();
            }
            outputStream = new PrintWriter(new FileOutputStream(new File(originalSizesFolder, selectedGame + ".txt"), true));
        }
        catch (FileNotFoundException f) {
            return;
        }

        outputStream.println(songName + ":" + nibbleCount);
        outputStream.close();
    }

    private static boolean isInvalidSize(byte[] dspNibbleCount, long nibbleCount, String songName) {
        //size check
        long newDSPSize = ((long) ((dspNibbleCount[0] & 0xFF) << 24)
                | ((dspNibbleCount[1] & 0xFF) << 16)
                | ((dspNibbleCount[2] & 0xFF) << 8)
                | (dspNibbleCount[3] & 0xFF));

        if (newDSPSize > nibbleCount) {
            JOptionPane.showMessageDialog(null, String.format("Your replacement for %s is %,d nibbles and the original is %,d nibbles! Try again!", songName, newDSPSize, nibbleCount));
            return true;
        }
        return false;
    }

    private static void writeDSPToPDT(RandomAccessFile pdtRaf, long newDSPSampleRateOffset, byte[] newDSPSampleRate, long newDSPNibbleCountOffset, byte[] newDSPNibbleCount, long newDSPLoopStartOffset, byte[] newDSPLoopStart, long ch1CoefOffs, byte[] newDSPLeftChannelDecodingCoeffs, long ch2CoefOffs, byte[] newDSPRightChannelDecodingCoeffs, long ch1Start, byte[] newDSPLeftChannelAudio, long ch2Start, byte[] newDSPRightChannelAudio) throws IOException {
        pdtRaf.seek(newDSPSampleRateOffset);
        pdtRaf.write(newDSPSampleRate);

        pdtRaf.seek(newDSPNibbleCountOffset);
        pdtRaf.write(newDSPNibbleCount);

        pdtRaf.seek(newDSPLoopStartOffset);
        pdtRaf.write(newDSPLoopStart);

        pdtRaf.seek(ch1CoefOffs);
        pdtRaf.write(newDSPLeftChannelDecodingCoeffs);

        pdtRaf.seek(ch2CoefOffs);
        pdtRaf.write(newDSPRightChannelDecodingCoeffs);

        pdtRaf.seek(ch1Start);
        pdtRaf.write(newDSPLeftChannelAudio);

        pdtRaf.seek(ch2Start);
        pdtRaf.write(newDSPRightChannelAudio);
    }
}