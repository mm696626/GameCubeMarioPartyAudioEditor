package uihelpers;

import io.FileIO;

import javax.swing.*;
import java.io.File;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;

public class BuildSongNameMapForOtherGames {

    public static Map<Integer, String> buildSongNameMap(File pdtFile) {
        Map<Integer, String> songNameMap = new HashMap<>();

        try (RandomAccessFile raf = new RandomAccessFile(pdtFile, "r")) {
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

                songNameMap.put(i, "Song " + String.format("%04d", i));
            }

            return songNameMap;

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "An error occurred: " + e.getMessage());
        }

        return null;
    }
}
