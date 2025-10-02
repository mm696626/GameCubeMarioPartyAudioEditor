package io.sound;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class DSPSoundPadder {

    public static boolean padSoundDSP(File oldDSPFile, File newDSPFile) {
        if (oldDSPFile.getName().equals(newDSPFile.getName())) {
            long oldSize = oldDSPFile.length();
            long newSize = newDSPFile.length();

            if (newSize < oldSize) {
                long sizeDifference = oldSize - newSize;

                try (RandomAccessFile raf = new RandomAccessFile(newDSPFile, "rw")) {
                    raf.seek(newDSPFile.length());

                    byte[] padding = new byte[1024];
                    while (sizeDifference > 0) {
                        int bytesToWrite = (int) Math.min(sizeDifference, padding.length);
                        raf.write(padding, 0, bytesToWrite);
                        sizeDifference -= bytesToWrite;
                    }

                    return true;
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }

        return false;
    }

    public static boolean padSoundDSPs(File[] oldFiles, File[] newFiles) {
        boolean paddedAFile = false;
        boolean paddedFile;

        for (int i = 0; i < oldFiles.length; i++) {
            for (int j = 0; j < newFiles.length; j++) {
                if (oldFiles[i].getName().equals(newFiles[j].getName())) {
                    paddedFile = padSoundDSP(oldFiles[i], newFiles[j]);

                    if (paddedFile && !paddedAFile) {
                        paddedAFile = true;
                    }
                }
            }
        }

        return paddedAFile;
    }
}
