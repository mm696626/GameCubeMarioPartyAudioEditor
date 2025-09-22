package io;

import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;

public class PDTFileIO {

    public static int readU8BE(RandomAccessFile raf) throws IOException {
        return raf.readUnsignedByte();
    }

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

    public static void writeU16BE(OutputStream out, int val) throws IOException {
        out.write((val >> 8) & 0xFF);
        out.write(val & 0xFF);
    }

    public static void writeU32BE(OutputStream out, long val) throws IOException {
        out.write((int) ((val >> 24) & 0xFF));
        out.write((int) ((val >> 16) & 0xFF));
        out.write((int) ((val >> 8) & 0xFF));
        out.write((int) (val & 0xFF));
    }
}
