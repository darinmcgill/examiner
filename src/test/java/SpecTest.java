import com.x5e.examiner.Item;
import com.x5e.examiner.State;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import static com.x5e.examiner.Statics.*;

import static org.testng.Assert.assertEquals;

class List implements java.io.Serializable {
    int value;
    List next;
}

public class SpecTest {

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
        //dumpBytes(bytes);
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        DataInputStream din = new DataInputStream(in);
        State state = new State();
        //Item.verbose = true;
        Item.start(din);
        Item item1 = Item.read(din,state);
        Item item2 = Item.read(din,state);
        System.err.println(item1.toPyon());
        System.err.println(item2.toPyon());
    }

}
