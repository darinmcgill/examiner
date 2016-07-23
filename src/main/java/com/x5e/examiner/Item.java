package com.x5e.examiner;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Field {
    public byte typecode;
    String fieldName;
    String className1;
}


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


    static Map<Byte,String> names = new HashMap<Byte, String>();
    static {
        names.put(TC_NULL,"TC_NULL");
        names.put(TC_REFERENCE,"TC_REFERENCE");
        names.put(TC_CLASSDESC,"TC_CLASSDESC");
        names.put(TC_OBJECT,"TC_OBJECT");
        names.put(TC_STRING,"TC_STRING");
        names.put(TC_ARRAY,"TC_ARRAY");
        names.put(TC_CLASS,"TC_CLASS");
        names.put(TC_BLOCKDATA,"TC_BLOCKDATA");
        names.put(TC_ENDBLOCKDATA,"TC_ENDBLOCKDATA");
        names.put(TC_RESET,"TC_RESET");
        names.put(TC_BLOCKDATALONG,"TC_BLOCKDATALONG");
        names.put(TC_EXCEPTION,"TC_EXCEPTION");
        names.put(TC_LONGSTRING,"TC_LONGSTRING");
        names.put(TC_PROXYCLASSDESC,"TC_PROXYCLASSDESC");
        names.put(TC_ENUM,"TC_ENUM");
    }

    public static void start(ByteBuffer bb) {
        short m = bb.getShort();
        if (m != STREAM_MAGIC) throw new RuntimeException("bad magic number");
        short v = bb.getShort();
        if (v != STREAM_VERSION) throw new RuntimeException("bad version");
    }

    public static short getUnsignedByte(ByteBuffer bb) {
        return ((short) (bb.get() & 0xff));
    }

    private static final Charset UTF8_CHARSET = Charset.forName("UTF-8");


    public static String readUtf(ByteBuffer bb) {
        short len = bb.getShort();
        if (len < 0) throw new RuntimeException("long string");
        byte[] contents = new byte[len];
        bb.get(contents);
        String out = new String(contents,UTF8_CHARSET);
        if (verbose) {
            System.err.println("red string: \"" + out + "\", pos=" + bb.position());
        }
        return out;
    }

    public void readClassAnnotation(ByteBuffer bb) {
        byte b;
        while (true) {
            b = bb.get();
            if (b == TC_ENDBLOCKDATA) break;
        }
    }

    public void readFields(ByteBuffer bb) {
        if (verbose) {
            System.err.println("about to get fields at pos=" + bb.position());
        }
        short num = bb.getShort();
        if (verbose) {
            System.err.println("got num:" + num + " 0x" + Integer.toHexString(num));
        }
        fields = new Field[num];
        for (short i=0;i<num;i++) {
            fields[i] = new Field();
            fields[i].typecode = bb.get();
            switch (fields[i].typecode) {
                case 'B':
                case 'C':
                case 'D':
                case 'F':
                case 'I':
                case 'J':
                case 'S':
                case 'Z':
                    fields[i].fieldName = readUtf(bb);
                    break;
                case '[':
                case 'L':
                    fields[i].fieldName = readUtf(bb);
                    fields[i].className1 = readUtf(bb);
                    break;
                default:
                    throw new RuntimeException("unexpected field typecode:" + fields[i].typecode);
            }
        }
    }

    byte tag;
    ByteBuffer stuff;
    int size;
    int handle;
    Item classDesc;
    long serialVersionUID_;
    String string;
    byte classDescFlags;
    Field[] fields;
    Object[] payload;
    public static boolean verbose = false;


    static long readLong(ByteBuffer bb) {
        long out = bb.getLong();
        if (verbose) {
            System.err.println("read long: 0x" + Long.toHexString(out) + " " + out + " pos=" + bb.position());
        }
        return out;
    }


    static int readInt(ByteBuffer bb) {
        int out = bb.getInt();
        if (verbose) {
            System.err.println("read int: 0x" + Integer.toHexString(out) + " " + out + " pos=" + bb.position());
        }
        return out;
    }

    public void readPayload(ByteBuffer bb, State state) {
        assert this.classDesc != null;
        assert this.classDesc.fields != null;
        int n = this.classDesc.fields.length;
        Field[] theFields = this.classDesc.fields;
        this.payload = new Object[n];
        for (int i=0; i<n; i++) {
            byte code = theFields[i].typecode;
            switch (code) {
                case 'B':
                    payload[i] = bb.get();
                    break;
                case 'C':
                    payload[i] = bb.getChar();
                    break;
                case 'D':
                    payload[i] = bb.getDouble();
                    break;
                case 'F':
                    payload[i] = bb.getFloat();
                    break;
                case 'I':
                    payload[i] = bb.getInt();
                    break;
                case 'J':
                    payload[i] = bb.getLong();
                    break;
                case 'S':
                    payload[i] = bb.getShort();
                    break;
                case 'Z':
                    payload[i] = (bb.get() != 0);
                    break;
                default:
                    throw new RuntimeException("unexpected kind:" + theFields[i].typecode);
            }
        }
    }

    public static Item read(ByteBuffer bb, State state) {
        Item out = new Item();
        int loc = bb.position();
        out.tag = bb.get();
        if (verbose) {
            String name = names.getOrDefault(out.tag,"Unknown:" + out.tag);
            System.err.println("start read, tag=" + name +" val=" + out.tag + " loc= " + loc);
        }

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
            case TC_CLASS:
                out.classDesc = read(bb,state);
                out.handle = state.put(out);
                break;
            case TC_STRING:
                out.handle = state.put(out);
                out.string = readUtf(bb);
                break;
            case TC_LONGSTRING:
                throw new RuntimeException("TC_LONGSTRING not implemented");
            case TC_ARRAY:
                out.classDesc = read(bb,state);
                out.handle = state.put(out);
                out.size = bb.getInt();
                throw new RuntimeException("TC_ARRAY not implemented");
            case TC_CLASSDESC:
                // TC_CLASSDESC className serialVersionUID newHandle classDescInfo
                // classDescInfo:
                // classDescFlags fields classAnnotation superClassDesc
                out.string = readUtf(bb);
                out.serialVersionUID_ = readLong(bb);
                out.handle = state.put(out);
                out.classDescFlags = bb.get();
                out.readFields(bb);
                out.readClassAnnotation(bb);
                out.classDesc = read(bb,state); // superClassDesc
                break;
            case TC_PROXYCLASSDESC:
                throw new RuntimeException("TC_PROXYCLASSDESC not implemented");
            case TC_NULL:
                break;
            case TC_REFERENCE:
                out.handle = readInt(bb);
                break;
            case TC_OBJECT:
                // TC_OBJECT classDesc newHandle classdata[]  // data for each class
                // classDesc is a subset of <object>
                out.classDesc = read(bb,state);
                out.handle = state.put(out);
                out.readPayload(bb,state);
                break;
            default:
                throw new RuntimeException("unexpected tag:" + Integer.toHexString(0xFF & out.tag) );
        }
        return out;
    }
}
