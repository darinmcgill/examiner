package com.x5e.examiner;

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
}
