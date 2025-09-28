package io;

import constants.DSPFileConstants;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class FixDSPSoundHeader {

    public static void fixHeader(File dspFile) {
        try (RandomAccessFile dspRaf = new RandomAccessFile(dspFile, "rw")) {
            dspRaf.seek(DSPFileConstants.LOOP_FLAG_OFFSET);

            for (int i=0; i<DSPFileConstants.LOOP_FLAG_LENGTH_IN_BYTES; i++) {
                dspRaf.write(0);
            }

            dspRaf.seek(DSPFileConstants.LOOP_START_OFFSET);

            for (int i=0; i<DSPFileConstants.LOOP_START_LENGTH_IN_BYTES - 1; i++) {
                dspRaf.write(0);
            }

            dspRaf.write(2);

            dspRaf.seek(DSPFileConstants.LOOP_END_OFFSET);

            for (int i=0; i<DSPFileConstants.LOOP_END_LENGTH_IN_BYTES; i++) {
                dspRaf.write(0);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
