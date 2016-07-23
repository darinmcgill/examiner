package com.x5e.examiner;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;


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
    }
}
