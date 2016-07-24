package com.x5e.examiner;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.*;

class Field {
    public byte typecode;
    String fieldName;
    Item className1;
    public String toPyon() {
        String out = "Field('"+ fieldName + "','" + (char) typecode + "'";
        if (className1 != null)
            out += "," + className1.toPyon();
        out += ")";
        return out;
    }
}


public class Item {

    byte tag;
    ByteBuffer stuff;
    int size=0;
    int handle=0;
    Item classDesc=null;
    long serialVersionUID_=0;
    String string=null;
    byte classDescFlags=0;
    Field[] fields=null;
    Object[] payload=null;

    public static String toPyon(Object obj) {
        if (obj instanceof Item) {
            return ((Item) obj).toPyon();
        }
        if (obj instanceof Number) {
            return obj.toString();
        }
        if (obj instanceof Boolean) {
            return obj.toString();
        }
        if (obj instanceof Character || obj instanceof String) {
            return "'" + obj.toString() + "'";
        }
        if (obj instanceof Field) return ((Field) obj).toPyon();
        throw new RuntimeException("don't know how to show: " + obj.getClass().toString());
    }

    public String toPyon() {
        StringBuilder builder = new StringBuilder();
        builder.append(names.getOrDefault(tag,"UNKNOWN"));
        builder.append("(");
        if (size !=0)
            builder.append(" size=" + size);
        if (handle != 0)
            builder.append(" handle=" + "0x" + Integer.toHexString(handle));
        if (string != null)
            builder.append(" string='" + string + "'");
        if (serialVersionUID_ != 0)
            builder.append(" serialVersionUID=0x" + Long.toHexString(serialVersionUID_).toUpperCase());
        if (classDesc != null)
            builder.append(" classDesc=" + classDesc.toPyon() + "");
        if (classDescFlags != 0)
            builder.append(" classDescFlags=" + classDescFlags);
        if (fields != null) {
            builder.append(" fields=[");
            for (Field field : fields) builder.append(toPyon(field) + " ");
            builder.append(" ]");
        }
        if (payload != null) {
            builder.append(" payload=[");
            for (Object obj : payload) builder.append(toPyon(obj) + " ");
            builder.append(" ]");
        }
        builder.append(')');
        return builder.toString();
    }

    public static boolean verbose = false;

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
    private static final Charset UTF8_CHARSET = Charset.forName("UTF-8");

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

    public void readFields(ByteBuffer bb,State state) {
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
                    fields[i].className1 = read(bb,state);
                    break;
                default:
                    throw new RuntimeException("unexpected field typecode:" + fields[i].typecode);
            }
        }
    }

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
        Item classDesc = this.classDesc;
        if (classDesc == null) throw new RuntimeException("null class desc?");
        if (classDesc.tag == TC_REFERENCE) {
            classDesc = state.get(classDesc.handle);
        }
        assert classDesc.fields != null;
        int n = classDesc.fields.length;
        Field[] theFields = classDesc.fields;
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
                case 'L':
                    payload[i] = read(bb,state);
                    break;
                    //throw new RuntimeException("need to read object at:" + bb.position());
                default:
                    throw new RuntimeException("unexpected kind:" + theFields[i].typecode);
            }
        }
    }

    private void readArrayPrimitives(ByteBuffer bb, Character kind) {
        // not very efficient
        payload = new Object[size];
        for (int i=0;i<size;i++) {
            switch (kind) {
                case 'I': payload[i] = bb.getInt(); break;
                case 'J': payload[i] = bb.getLong(); break;
                case 'C': payload[i] = bb.getChar(); break;
                case 'D': payload[i] = bb.getDouble(); break;
                case 'F': payload[i] = bb.getFloat(); break;
                case 'B': payload[i] = bb.get(); break;
                case 'Z': payload[i] = (bb.get() != 0); break;
                case 'S': payload[i] = bb.getShort(); break;
                default:
                    throw new RuntimeException("unexpected array type:" + kind);
            }
        }
    }

    private void readArrayObjects(ByteBuffer bb, State state) {
        payload = new Object[size];
        for (int i=0;i<size;i++) {
            payload[i] = read(bb,state);
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
                if (out.classDesc.string.length() == 2) {
                    out.readArrayPrimitives(bb,out.classDesc.string.charAt(1));
                } else {
                    out.readArrayObjects(bb,state);
                }
                break;
            case TC_CLASSDESC:
                // TC_CLASSDESC className serialVersionUID newHandle classDescInfo
                // classDescInfo:
                // classDescFlags fields classAnnotation superClassDesc
                out.string = readUtf(bb);
                out.serialVersionUID_ = readLong(bb);
                out.handle = state.put(out);
                out.classDescFlags = bb.get();
                out.readFields(bb,state);
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
