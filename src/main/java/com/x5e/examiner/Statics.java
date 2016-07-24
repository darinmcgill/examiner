package com.x5e.examiner;

import sun.nio.ch.FileChannelImpl;

import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Created by darin on 7/23/16.
 */
public class Statics {

    public static void dumpBytes(byte[] bytes) {
        for (int i=0;i<bytes.length;i++) {
            char c = ' ';
            if (bytes[i] < 127 && bytes[i] > 0) c = (char) bytes[i];
            System.out.println(String.format("%03d | 0x%02X = %03d = %c", i, bytes[i], 0xFF & bytes[i], c));
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.err.println("please specify a file");
            System.exit(1);
        }
        System.err.println("got arg: '" + args[0] + "'");
        if (! new File(args[0]).exists()) {
            System.err.println("file doesn't exist");
            System.exit(1);
        }
        long length = new File(args[0]).length();
        RandomAccessFile raf = new RandomAccessFile(args[0],"r");
        Item.verbose = true;
        //BlockDataInputStream din = new BlockDataInputStream(fin);
        Item.start(raf);
        State state = new State();
        Item got = Item.read(raf,state);
        System.err.println("at position:" + raf.getFilePointer() + " out of: " + length);
        System.err.println("done reading!");
    }
}
