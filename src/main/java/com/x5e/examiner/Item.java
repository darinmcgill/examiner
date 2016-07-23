package com.x5e.examiner;

import java.nio.ByteBuffer;

public class Item {
    final static short STREAM_MAGIC = (short)0xaced;
    final static short STREAM_VERSION = 5;
    final static byte TC_NULL = (byte)0x70;
    final static byte TC_REFERENCE = (byte)0x71;
    final static byte TC_CLASSDESC = (byte)0x72;
    final static byte TC_OBJECT = (byte)0x73;
    final static byte TC_STRING = (byte)0x74;
    final static byte TC_ARRAY = (byte)0x75;
    final static byte TC_CLASS = (byte)0x76;
    final static byte TC_BLOCKDATA = (byte)0x77;
    final static byte TC_ENDBLOCKDATA = (byte)0x78;
    final static byte TC_RESET = (byte)0x79;
    final static byte TC_BLOCKDATALONG = (byte)0x7A;
    final static byte TC_EXCEPTION = (byte)0x7B;
    final static byte TC_LONGSTRING = (byte) 0x7C;
    final static byte TC_PROXYCLASSDESC = (byte) 0x7D;
    final static byte TC_ENUM = (byte) 0x7E;
    final static  int   baseWireHandle = 0x7E0000;

    public static void start(ByteBuffer bb) {
        short m = bb.getShort();
        if (m != STREAM_MAGIC) throw new RuntimeException("bad magic number");
        short v = bb.getShort();
        if (v != STREAM_VERSION) throw new RuntimeException("bad version");
    }

    public static short getUnsignedByte(ByteBuffer bb) {
        return ((short) (bb.get() & 0xff));
    }

    byte tag;
    ByteBuffer stuff;
    int size;
    int handle;
    Item classDesc;


    public static Item read(ByteBuffer bb) {
        Item out = new Item();
        out.tag = bb.get();

        switch (out.tag) {
            case TC_BLOCKDATA:
                out.size = getUnsignedByte(bb);
                out.stuff = bb.slice();
                bb.position(bb.position() + out.size);
                break;
            case TC_BLOCKDATALONG:
                out.size = bb.getInt();
                out.stuff = bb.slice();
                bb.position(bb.position() + out.size);
                break;
            case TC_ENDBLOCKDATA:
                break;
            case TC_CLASSDESC:
                //TODO
                break;
            case TC_PROXYCLASSDESC:
                //TODO
                break;
            case TC_NULL:
                break;
            case TC_REFERENCE:
                out.handle = bb.getInt();
                break;
            case TC_OBJECT:
                // TC_OBJECT classDesc newHandle classdata[]  // data for each class
                // classDesc is a subset of <object>
                out.classDesc = read(bb);
                out.handle = bb.getInt();
                //TODO
            default:
                throw new RuntimeException("unexpected byte:" + out.tag);
        }
        return out;
    }
}
