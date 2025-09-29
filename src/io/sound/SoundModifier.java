package io.sound;

import io.FileIO;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

public class SoundModifier {

    //This code is derived from Yoshimaster96's C MSM sound dumping code, so huge credit and kudos to them!
    //Code: https://github.com/Yoshimaster96/mpgc-sound-tools

    public static void modifySoundBank(File msmFile, File sdirFile, File sampFile, long soundBankReplaced) {
        try (RandomAccessFile msmRaf = new RandomAccessFile(msmFile, "rw")) {

            // Seek to 0x20 to read chk2 offset and size
            msmRaf.seek(0x20);
            long chk2Offs = FileIO.readU32BE(msmRaf);
            long chk2Size = FileIO.readU32BE(msmRaf);

            // Skip 0x10 bytes
            msmRaf.skipBytes(0x10);

            // Read chk5 and chk6 offsets and sizes
            long chk5Offs = FileIO.readU32BE(msmRaf);
            long chk5Size = FileIO.readU32BE(msmRaf);
            long chk6Offs = FileIO.readU32BE(msmRaf);
            long chk6Size = FileIO.readU32BE(msmRaf);

            // Iterate through entries in chk2
            for (int i = 1; i < (chk2Size >> 5); i++) {
                msmRaf.seek(chk2Offs + (i << 5));
                long groupId = FileIO.readU16BE(msmRaf);
                msmRaf.skipBytes(2);

                long groupDataOffs = FileIO.readU32BE(msmRaf);
                long groupDataSize = FileIO.readU32BE(msmRaf);
                long sampOffs = FileIO.readU32BE(msmRaf);
                long sampSize = FileIO.readU32BE(msmRaf);

                groupDataOffs += chk5Offs;
                sampOffs += chk6Offs;

                // Read offsets from group data
                msmRaf.seek(groupDataOffs);
                long poolOffs = FileIO.readU32BE(msmRaf);
                long projOffs = FileIO.readU32BE(msmRaf);
                long sdirOffs = FileIO.readU32BE(msmRaf);
                long SNGOffs = FileIO.readU32BE(msmRaf);

                long poolSize = projOffs - poolOffs;
                long projSize = sdirOffs - projOffs;
                long sdirSize = SNGOffs - sdirOffs;

                poolOffs += groupDataOffs;
                projOffs += groupDataOffs;
                sdirOffs += groupDataOffs;


                if (groupId == soundBankReplaced) {
                    if (sdirFile.length() > sdirSize) {
                        JOptionPane.showMessageDialog(null, String.format("Your sdir file replacement is %,d bytes and the original is %,d bytes! Try again!", sdirFile.length(), sdirSize));
                        return;
                    }

                    if (sampFile.length() > sampSize) {
                        JOptionPane.showMessageDialog(null, String.format("Your samp file replacement is %,d bytes and the original is %,d bytes! Try again!", sampFile.length(), sampSize));
                        return;
                    }

                    byte[] newSDirFileBytes;
                    byte[] newSampFileBytes;

                    try (RandomAccessFile raf = new RandomAccessFile(sdirFile, "r")) {
                        raf.seek(0);
                        long remainingBytes = raf.length();
                        newSDirFileBytes = new byte[(int) remainingBytes];
                        raf.readFully(newSDirFileBytes);
                    }

                    try (RandomAccessFile raf = new RandomAccessFile(sampFile, "r")) {
                        raf.seek(0);
                        long remainingBytes = raf.length();
                        newSampFileBytes = new byte[(int) remainingBytes];
                        raf.readFully(newSampFileBytes);
                    }

                    msmRaf.seek(sdirOffs);
                    msmRaf.write(newSDirFileBytes);

                    //pad sdir file to match original size
                    for (int j=0; j<sdirSize-sdirFile.length(); j++) {
                        msmRaf.write(0);
                    }

                    msmRaf.seek(sampOffs);
                    msmRaf.write(newSampFileBytes);

                    //pad samp file to match original size
                    for (int j=0; j<sampSize-sampFile.length(); j++) {
                        msmRaf.write(0);
                    }

                    msmRaf.close();

                    JOptionPane.showMessageDialog(null, "Sound bank has been modified!");

                    return;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}