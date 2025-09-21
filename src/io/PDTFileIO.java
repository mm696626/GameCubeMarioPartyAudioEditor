package io;

import java.io.IOException;
import java.io.RandomAccessFile;

public class PDTFileIO {

    public static int readU16BE(RandomAccessFile raf) throws IOException {
        int b0 = raf.readUnsignedByte();
        int b1 = raf.readUnsignedByte();
        return (b0 << 8) | b1;
    }

    public static long readU32BE(RandomAccessFile raf) throws IOException {
        long b0 = raf.readUnsignedByte();
        long b1 = raf.readUnsignedByte();
        long b2 = raf.readUnsignedByte();
        long b3 = raf.readUnsignedByte();
        return (b0 << 24) | (b1 << 16) | (b2 << 8) | b3;
    }
}
