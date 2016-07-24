package com.x5e.examiner;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import static java.io.ObjectStreamConstants.*;
import java.io.DataInput;

public class Item {

    byte tag;
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
            builder.append(" size=").append(size);
        if (handle != 0)
            builder.append(" handle=" + "0x").append(Integer.toHexString(handle));
        if (string != null)
            builder.append(" string='").append(string).append("'");
        if (serialVersionUID_ != 0)
            builder.append(" serialVersionUID=0x").append(Long.toHexString(serialVersionUID_).toUpperCase());
        if (classDesc != null)
            builder.append(" classDesc=").append(classDesc.toPyon()).append("");
        if (classDescFlags != 0)
            builder.append(" classDescFlags=").append(classDescFlags);
        if (fields != null) {
            builder.append(" fields=[");
            for (Field field : fields) builder.append(toPyon(field)).append(" ");
            builder.append(" ]");
        }
        if (payload != null) {
            builder.append(" payload=[");
            for (Object obj : payload) builder.append(toPyon(obj)).append(" ");
            builder.append(" ]");
        }
        builder.append(')');
        return builder.toString();
    }

    public static boolean verbose = false;

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

    public String toString() {
        return "Item('" + names.getOrDefault(tag,"UNKONWN") + "')";
    }

    public static void start(DataInput bb) throws IOException {
        short m = bb.readShort();
        if (m != STREAM_MAGIC) throw new RuntimeException("bad magic number");
        short v = bb.readShort();
        if (v != STREAM_VERSION) throw new RuntimeException("bad version");
    }

    public static short getUnsignedByte(DataInput bb) throws IOException {
        return ((short) (bb.readByte() & 0xff));
    }

    public static String readUtf(DataInput bb) throws IOException {
        short len = bb.readShort();
        if (len < 0) throw new RuntimeException("long string");
        byte[] contents = new byte[len];
        bb.readFully(contents);
        String out = new String(contents,UTF8_CHARSET);
        if (verbose) {
            System.err.println("red string: \"" + out + "\", pos=");
        }
        return out;
    }

    public void readClassAnnotation(DataInput bb) throws IOException {
        byte b;
        while (true) {
            b = bb.readByte();
            if (b == TC_ENDBLOCKDATA) break;
        }
    }

    public void readFields(DataInput bb,State state) throws IOException {
        if (verbose) {
            System.err.println("about to get fields at pos=");
        }
        short num = bb.readShort();
        if (verbose) {
            System.err.println("got num:" + num + " 0x" + Integer.toHexString(num));
        }
        fields = new Field[num];
        for (short i=0;i<num;i++) {
            fields[i] = new Field();
            fields[i].typecode = bb.readByte();
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

    static long readLong(DataInput bb) throws IOException {
        long out = bb.readLong();
        if (verbose) {
            System.err.println("read long: 0x" + Long.toHexString(out) + " " + out + " pos=");
        }
        return out;
    }

    static int readInt(DataInput bb) throws IOException {
        int out = bb.readInt();
        if (verbose) {
            System.err.println("read int: 0x" + Integer.toHexString(out) + " " + out + " pos=");
        }
        return out;
    }

    public void readPayload(DataInput bb, State state) throws IOException {
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
                    payload[i] = bb.readByte();
                    break;
                case 'C':
                    payload[i] = bb.readChar();
                    break;
                case 'D':
                    payload[i] = bb.readDouble();
                    break;
                case 'F':
                    payload[i] = bb.readFloat();
                    break;
                case 'I':
                    payload[i] = bb.readInt();
                    break;
                case 'J':
                    payload[i] = bb.readLong();
                    break;
                case 'S':
                    payload[i] = bb.readShort();
                    break;
                case 'Z':
                    payload[i] = (bb.readByte() != 0);
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

    private void readArrayPrimitives(DataInput bb, Character kind) throws IOException {
        // not very efficient
        payload = new Object[size];
        for (int i=0;i<size;i++) {
            switch (kind) {
                case 'I': payload[i] = bb.readInt(); break;
                case 'J': payload[i] = bb.readLong(); break;
                case 'C': payload[i] = bb.readChar(); break;
                case 'D': payload[i] = bb.readDouble(); break;
                case 'F': payload[i] = bb.readFloat(); break;
                case 'B': payload[i] = bb.readByte(); break;
                case 'Z': payload[i] = (bb.readByte() != 0); break;
                case 'S': payload[i] = bb.readShort(); break;
                default:
                    throw new RuntimeException("unexpected array type:" + kind);
            }
        }
    }

    private void readArrayObjects(DataInput bb, State state) throws IOException {
        payload = new Object[size];
        for (int i=0;i<size;i++) {
            payload[i] = read(bb,state);
        }
    }

    public static Item read(DataInput bb, State state) throws IOException {
        Item out = new Item();
        out.tag = bb.readByte();
        if (verbose) {
            String name = names.getOrDefault(out.tag,"Unknown:" + out.tag);
            System.err.println("start read, tag=" + name +" val=" + out.tag);
        }

        switch (out.tag) {
            case TC_BLOCKDATA:
                out.size = getUnsignedByte(bb);
                bb.skipBytes(out.size);
                if (verbose) System.err.println("skipped bytes: " + out.size);
                return read(bb,state);
            case TC_BLOCKDATALONG:
                out.size = bb.readInt();
                bb.skipBytes(out.size);
                if (verbose) System.err.println("skipped bytes: " + out.size);
                return read(bb,state);
            case TC_ENDBLOCKDATA:
                return read(bb,state);
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
                out.size = bb.readInt();
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
                out.classDescFlags = bb.readByte();
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
            case TC_ENUM:
                out.classDesc = read(bb,state);
                out.handle = state.put(out);
                out.payload = new Object[1];
                out.payload[0] = read(bb,state);
                break;
            default:
                throw new RuntimeException("unexpected tag:" + Integer.toHexString(0xFF & out.tag) );
        }
        return out;
    }
}
