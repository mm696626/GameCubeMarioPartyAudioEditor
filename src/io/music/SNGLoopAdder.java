package io.music;

import io.FileIO;

import javax.swing.*;
import java.io.File;
import java.io.RandomAccessFile;
import java.util.ArrayList;

public class SNGLoopAdder {

    public static void loopSNG(File sngFile) {

        ArrayList<Long> regionInfoOffsets = new ArrayList<>();

        try (RandomAccessFile raf = new RandomAccessFile(sngFile, "rw")) {
            long trackIndexOffset = FileIO.readU32BE(raf);
            raf.seek(trackIndexOffset);

            long regionInfoOffset = FileIO.readU32BE(raf);

            while (regionInfoOffset != 0) {
                regionInfoOffsets.add(regionInfoOffset);
                regionInfoOffset = FileIO.readU32BE(raf);
            }

            //do all but the last instrument (track) since one needs to be a terminator (idk how to fix this)
            for (int i=0; i<regionInfoOffsets.size() - 1; i++) {
                raf.seek(regionInfoOffsets.get(i+1) - 3); //terminator for previous track is 3 bytes before the next track
                raf.write(0xFE);
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "An error occurred: " + e.getMessage());
        }
    }
}
