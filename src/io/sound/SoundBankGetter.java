package io.sound;

import io.FileIO;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Comparator;

public class SoundBankGetter {

    //This code is derived from Yoshimaster96's C MSM sound dumping code, so huge credit and kudos to them!
    //This code is a C to Java translation
    //Code: https://github.com/Yoshimaster96/mpgc-sound-tools

    public static ArrayList<String> getBanks(File msmFile) {

        ArrayList<String> banks = new ArrayList<>();

        try (RandomAccessFile msmRaf = new RandomAccessFile(msmFile, "r")) {

            msmRaf.seek(0x20);
            long chk2Offs = FileIO.readU32BE(msmRaf);
            long chk2Size = FileIO.readU32BE(msmRaf);

            for (int i = 1; i < (chk2Size >> 5); i++) {
                msmRaf.seek(chk2Offs + (i << 5));
                long groupId = FileIO.readU16BE(msmRaf);
                banks.add(String.format("%04d", groupId));
            }

            banks.sort(Comparator.comparingInt(Integer::parseInt));

            return banks;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
