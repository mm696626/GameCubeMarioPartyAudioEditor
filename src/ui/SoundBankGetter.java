package ui;

import io.FileIO;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

public class SoundBankGetter {

    public static ArrayList<String> getBanks(File msmFile) {

        ArrayList<String> banks = new ArrayList<>();

        try (RandomAccessFile msmRaf = new RandomAccessFile(msmFile, "r")) {

            msmRaf.seek(0x20);
            long chk2Offs = FileIO.readU32BE(msmRaf);
            long chk2Size = FileIO.readU32BE(msmRaf);

            for (int i = 1; i < (chk2Size >> 5); i++) {
                msmRaf.seek(chk2Offs + (i << 5));
                long groupId = FileIO.readU16BE(msmRaf);
                banks.add(String.format("%04X", groupId));
            }

            return banks;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
