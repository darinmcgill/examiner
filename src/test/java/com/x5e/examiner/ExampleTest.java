package com.x5e.examiner;
import org.testng.annotations.Test;
import java.io.*;
import java.nio.ByteBuffer;

import static org.testng.Assert.*;

class Bucket implements java.io.Serializable {
    int abc;
}


public class ExampleTest {

    public static void writeFile(String fn, byte[] data) throws IOException {
        FileOutputStream fos = new FileOutputStream(fn);
        fos.write(data);
        fos.close();
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

    @Test
    public void arrayTest() throws Exception {
        int[] ints = new int[2];
        ints[0] = 17;
        ints[1] = 19;
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(o);
        out.writeObject(ints);
        out.flush();
        out.close();
        byte[] bytes = o.toByteArray();
        //writeFile("simpleTest.ser",bytes);
        //dumpBytes(bytes);
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        Item.start(bb);
        State state = new State();
        Item item = Item.read(bb,state);
        System.out.println(item.toPyon());
        assertEquals(item.payload[0],17);
        assertEquals(item.payload[1],19);
    }

    @Test
    public void arrayTest2() throws Exception {
        Integer[] ints = new Integer[2];
        ints[0] = 17;
        ints[1] = 19;
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(o);
        out.writeObject(ints);
        out.flush();
        out.close();
        byte[] bytes = o.toByteArray();
        //writeFile("simpleTest.ser",bytes);
        //dumpBytes(bytes);
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        Item.start(bb);
        State state = new State();
        Item item = Item.read(bb,state);
        System.out.println(item.toPyon());
        assertEquals(((Item)item.payload[0]).payload[0],17);
        assertEquals(((Item)item.payload[1]).payload[0],19);
        assertTrue(bb.remaining() == 0);
    }

}
