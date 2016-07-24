package com.x5e.examiner;


public class Field {
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

