package io.music;

import constants.DSPFileConstants;
import io.FileIO;

import javax.swing.*;
import java.io.*;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

public class SongModifier {

    //This code is largely derived from Yoshimaster96's C PDT dumping code, so huge credit and kudos to them!
    //Code: https://github.com/Yoshimaster96/mpgc-sound-tools

    public static boolean modifySong(File pdtFile, File leftChannel, File rightChannel, int songIndex, String songName, String selectedGame, boolean deleteDSPAfterModify) {
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

            //grab the pointer to the start of the left channel
            long ch1Pointer = pdtRaf.getFilePointer();
            long ch1Start = FileIO.readU32BE(pdtRaf);

            int ch1CoefEntry = FileIO.readU16BE(pdtRaf);
            int unk116 = FileIO.readU16BE(pdtRaf);
            long ch1CoefOffs = coeffOffs + (ch1CoefEntry << 5);

            long ch2Pointer = ch1Pointer;

            long ch2Start = ch1Start;
            int ch2CoefEntry = ch1CoefEntry;
            long ch2CoefOffs = coeffOffs + (ch2CoefEntry << 5);
            int chanCount = 1;

            if ((flags & 0x01000000) != 0) {
                //grab the pointer to the start of the right channel
                ch2Pointer = pdtRaf.getFilePointer();
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
            long originalNibbleCount = readDSPNibbleCountFromFile(songName, selectedGame, pdtFile.getName());

            if (originalNibbleCount != -1) {
                nibbleCount = originalNibbleCount;
            }

            boolean writeToEOF = isInvalidSize(newDSPNibbleCount, nibbleCount, songName);

            //if the nibble count here is -1, then the song hasn't been replaced yet, so write it
            if (originalNibbleCount == -1) {
                writeDSPNibbleCountToFile(songName, nibbleCount, selectedGame, pdtFile.getName());
            }

            long newDSPSampleRateOffset = thisHeaderOffs + 4;
            long newDSPNibbleCountOffset = thisHeaderOffs + 8;
            long newDSPLoopStartOffset = thisHeaderOffs + 12;

            String doesSongExist = checkIfSongExists(leftChannel, rightChannel, selectedGame, pdtFile.getAbsolutePath());

            //if the song has already been used in the PDT, just write its header data to the new location and point to the audio data
            if (!doesSongExist.isEmpty()) {
                writeDSPHeaderDataToPDT(pdtRaf, newDSPSampleRateOffset, newDSPSampleRate, newDSPNibbleCountOffset, newDSPNibbleCount, newDSPLoopStartOffset, newDSPLoopStart, ch1CoefOffs, newDSPLeftChannelDecodingCoeffs, ch2CoefOffs, newDSPRightChannelDecodingCoeffs);

                String[] doesSongExistParts = doesSongExist.split("\\|");

                //string is split into song name|left channel name|right channel name|left channel pointer|right channel pointer
                String leftPointer = doesSongExistParts[3];
                String rightPointer = doesSongExistParts[4];

                pdtRaf.seek(ch1Pointer);
                pdtRaf.writeInt(Integer.parseInt(leftPointer));

                pdtRaf.seek(ch2Pointer);
                pdtRaf.writeInt(Integer.parseInt(rightPointer));
            }
            else {
                if (writeToEOF) {
                    writeDSPToPDTEOF(pdtRaf, newDSPSampleRateOffset, newDSPSampleRate, newDSPNibbleCountOffset, newDSPNibbleCount, newDSPLoopStartOffset, newDSPLoopStart, ch1CoefOffs, newDSPLeftChannelDecodingCoeffs, ch2CoefOffs, newDSPRightChannelDecodingCoeffs, newDSPLeftChannelAudio, newDSPRightChannelAudio, ch1Pointer, ch2Pointer);
                }
                else {
                    writeDSPToPDT(pdtRaf, newDSPSampleRateOffset, newDSPSampleRate, newDSPNibbleCountOffset, newDSPNibbleCount, newDSPLoopStartOffset, newDSPLoopStart, ch1CoefOffs, newDSPLeftChannelDecodingCoeffs, ch2CoefOffs, newDSPRightChannelDecodingCoeffs, ch1Start, newDSPLeftChannelAudio, ch2Start, newDSPRightChannelAudio);
                }
            }

            //write new pointers to log file
            pdtRaf.seek(ch1Pointer);
            long newLeftChannelPointer = FileIO.readU32BE(pdtRaf);

            pdtRaf.seek(ch2Pointer);
            long newRightChannelPointer = FileIO.readU32BE(pdtRaf);

            logSongReplacement(songName, leftChannel, rightChannel, selectedGame, pdtFile.getAbsolutePath(), newLeftChannelPointer, newRightChannelPointer);

            if (deleteDSPAfterModify) {
                leftChannel.delete();
                rightChannel.delete();
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "An error occurred: " + e.getMessage());
            return false;
        }
    }

    private static long readDSPNibbleCountFromFile(String songName, String selectedGame, String pdtFileName) {
        Scanner inputStream;

        try {
            if (!selectedGame.equals("Other")) {
                inputStream = new Scanner(new FileInputStream("original_song_sizes/" + selectedGame + ".txt"));
            }
            else {
                String baseFileName = pdtFileName.substring(0, pdtFileName.length() - 4);
                inputStream = new Scanner(new FileInputStream("original_song_sizes/" + baseFileName + ".txt"));
            }
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

    private static void writeDSPNibbleCountToFile(String songName, long nibbleCount, String selectedGame, String pdtFileName) {
        PrintWriter outputStream;

        try {
            File originalSizesFolder = new File("original_song_sizes");
            if (!originalSizesFolder.exists()) {
                originalSizesFolder.mkdirs();
            }

            if (!selectedGame.equals("Other")) {
                outputStream = new PrintWriter(new FileOutputStream(new File(originalSizesFolder, selectedGame + ".txt"), true));
            }
            else {
                String baseFileName = pdtFileName.substring(0, pdtFileName.length() - 4);
                outputStream = new PrintWriter(new FileOutputStream(new File(originalSizesFolder, baseFileName + ".txt"), true));
            }
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
            return true;
        }
        return false;
    }

    private static void writeDSPToPDT(RandomAccessFile pdtRaf, long newDSPSampleRateOffset, byte[] newDSPSampleRate, long newDSPNibbleCountOffset, byte[] newDSPNibbleCount, long newDSPLoopStartOffset, byte[] newDSPLoopStart, long ch1CoefOffs, byte[] newDSPLeftChannelDecodingCoeffs, long ch2CoefOffs, byte[] newDSPRightChannelDecodingCoeffs, long ch1Start, byte[] newDSPLeftChannelAudio, long ch2Start, byte[] newDSPRightChannelAudio) throws IOException {
        writeDSPHeaderDataToPDT(pdtRaf, newDSPSampleRateOffset, newDSPSampleRate, newDSPNibbleCountOffset, newDSPNibbleCount, newDSPLoopStartOffset, newDSPLoopStart, ch1CoefOffs, newDSPLeftChannelDecodingCoeffs, ch2CoefOffs, newDSPRightChannelDecodingCoeffs);

        pdtRaf.seek(ch1Start);
        pdtRaf.write(newDSPLeftChannelAudio);

        pdtRaf.seek(ch2Start);
        pdtRaf.write(newDSPRightChannelAudio);
    }

    private static void writeDSPToPDTEOF(RandomAccessFile pdtRaf, long newDSPSampleRateOffset, byte[] newDSPSampleRate, long newDSPNibbleCountOffset, byte[] newDSPNibbleCount, long newDSPLoopStartOffset, byte[] newDSPLoopStart, long ch1CoefOffs, byte[] newDSPLeftChannelDecodingCoeffs, long ch2CoefOffs, byte[] newDSPRightChannelDecodingCoeffs, byte[] newDSPLeftChannelAudio, byte[] newDSPRightChannelAudio, long ch1Pointer, long ch2Pointer) throws IOException {
        //if file size isn't divisible by 0x10, then make it so before writing to EOF
        if (pdtRaf.length() % 0x10 != 0) {
            padFileSize(pdtRaf);
        }

        writeDSPHeaderDataToPDT(pdtRaf, newDSPSampleRateOffset, newDSPSampleRate, newDSPNibbleCountOffset, newDSPNibbleCount, newDSPLoopStartOffset, newDSPLoopStart, ch1CoefOffs, newDSPLeftChannelDecodingCoeffs, ch2CoefOffs, newDSPRightChannelDecodingCoeffs);

        pdtRaf.seek(ch1Pointer);
        pdtRaf.writeInt((int)pdtRaf.length());

        pdtRaf.seek(pdtRaf.length());
        pdtRaf.write(newDSPLeftChannelAudio);

        //make sure file size is even (idk why, but the game will freak out otherwise)
        //make sure file size is divisible by 0x10 (idk why, but the game will freak out otherwise)
        makePDTFileSizeEven(pdtRaf);
        padFileSize(pdtRaf);

        pdtRaf.seek(ch2Pointer);
        pdtRaf.writeInt((int)pdtRaf.length());

        pdtRaf.seek(pdtRaf.length());
        pdtRaf.write(newDSPRightChannelAudio);

        //do the same here
        makePDTFileSizeEven(pdtRaf);
        padFileSize(pdtRaf);
    }

    private static void writeDSPHeaderDataToPDT(RandomAccessFile pdtRaf, long newDSPSampleRateOffset, byte[] newDSPSampleRate, long newDSPNibbleCountOffset, byte[] newDSPNibbleCount, long newDSPLoopStartOffset, byte[] newDSPLoopStart, long ch1CoefOffs, byte[] newDSPLeftChannelDecodingCoeffs, long ch2CoefOffs, byte[] newDSPRightChannelDecodingCoeffs) throws IOException {
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
    }

    private static void padFileSize(RandomAccessFile pdtRaf) throws IOException {
        pdtRaf.seek(pdtRaf.length());
        int paddingNeeded = (int) (0x10 - (pdtRaf.length() % 0x10));

        for (int i=0; i < paddingNeeded; i++) {
            pdtRaf.write(0);
        }
    }

    private static void makePDTFileSizeEven(RandomAccessFile pdtRaf) throws IOException {
        pdtRaf.seek(pdtRaf.length());

        if (pdtRaf.length() % 2 == 0) {
            pdtRaf.write(0);
            pdtRaf.write(0);
        }
        else {
            pdtRaf.write(0);
        }
    }

    private static void logSongReplacement(String songName, File leftChannel, File rightChannel, String selectedGame, String pdtFilePath, long leftChannelPointer, long rightChannelPointer) {
        File songReplacementsFolder = new File("song_replacements");
        if (!songReplacementsFolder.exists()) {
            songReplacementsFolder.mkdirs();
        }

        File logFile;

        String hash = Integer.toHexString(pdtFilePath.hashCode());
        String baseFileName = selectedGame + "_" + hash;
        logFile = new File("song_replacements", baseFileName + ".txt");

        Map<String, String> songMap = new TreeMap<>();

        if (logFile.exists()) {
            try (Scanner inputStream = new Scanner(new FileInputStream(logFile))) {
                while (inputStream.hasNextLine()) {
                    String line = inputStream.nextLine();
                    String[] parts = line.split("\\|");

                    if (parts.length >= 5) {
                        String existingSongName = parts[0];
                        String left = parts[1];
                        String right = parts[2];
                        String leftPointer = parts[3];
                        String rightPointer = parts[4];
                        songMap.put(existingSongName, left + "|" + right + "|" + leftPointer + "|" + rightPointer);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        songMap.put(songName, leftChannel.getName() + "|" + rightChannel.getName() + "|" + leftChannelPointer + "|" + rightChannelPointer);

        try (PrintWriter outputStream = new PrintWriter(new FileOutputStream(logFile))) {
            for (Map.Entry<String, String> entry : songMap.entrySet()) {
                outputStream.println(entry.getKey() + "|" + entry.getValue());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String checkIfSongExists(File leftChannel, File rightChannel, String selectedGame, String pdtFilePath) {
        File songReplacementsFolder = new File("song_replacements");
        if (!songReplacementsFolder.exists()) {
            songReplacementsFolder.mkdirs();
        }

        File logFile;

        String hash = Integer.toHexString(pdtFilePath.hashCode());
        String baseFileName = selectedGame + "_" + hash;
        logFile = new File("song_replacements", baseFileName + ".txt");

        if (logFile.exists()) {
            try (Scanner inputStream = new Scanner(new FileInputStream(logFile))) {
                while (inputStream.hasNextLine()) {
                    String line = inputStream.nextLine();
                    String[] parts = line.split("\\|");

                    //string is split into song name|left channel name|right channel name|left channel pointer|right channel pointer
                    if (parts[1].equals(leftChannel.getName()) && parts[2].equals(rightChannel.getName())) {
                        return line;
                    }
                }

                return "";
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return "";
    }
}