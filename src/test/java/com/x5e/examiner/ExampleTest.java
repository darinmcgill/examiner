package com.x5e.examiner;
import org.testng.annotations.Test;

import java.io.*;
import java.nio.ByteBuffer;

import static org.testng.Assert.*;

class Bucket implements java.io.Serializable {
    int abc;
}

class List implements java.io.Serializable {
    int value;
    List next;
}

public class ExampleTest {
    @Test
    public void specTest() throws Exception {
        List list1 = new List();
        List list2 = new List();
        list1.value = 17;
        list1.next = list2;
        list2.value = 19;
        list2.next = null;

        ByteArrayOutputStream o = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(o);
        out.writeObject(list1);
        out.writeObject(list2);
        out.flush();
        out.close();
        byte[] bytes = o.toByteArray();
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        State state = new State();
        Item item = Item.read(bb,state);
    }

    public static void writeFile(String fn, byte[] data) throws IOException {
        FileOutputStream fos = new FileOutputStream(fn);
        fos.write(data);
        fos.close();
    }

    public static void dumpBytes(byte[] bytes) {
        for (int i=0;i<bytes.length;i++) {
            char c = ' ';
            if (bytes[i] < 127 && bytes[i] > 0) c = (char) bytes[i];
            System.out.println(String.format("%03d | 0x%02X = %03d = %c", i, bytes[i], 0xFF & bytes[i], c));
        }
    }

    @Test
    public void simpleTest() throws Exception {
        Bucket bucket = new Bucket();
        bucket.abc = 17;
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(o);
        out.writeObject(bucket);
        Bucket b2 = new Bucket();
        //out.writeObject(b2);
        out.flush();
        out.close();
        byte[] bytes = o.toByteArray();
        //writeFile("simpleTest.ser",bytes);
        //dumpBytes(bytes);
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        Item.start(bb);
        State state = new State();
        Item item = Item.read(bb,state);
        //System.out.println(item);
        assertEquals(item.payload[0],17);
    }
    /*
     */
}
