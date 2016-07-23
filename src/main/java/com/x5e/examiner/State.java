package com.x5e.examiner;

import java.util.ArrayList;
import java.util.List;

public class State {
    final static  int   baseWireHandle = 0x7E0000;
    public List<Item> assigned = new ArrayList<Item>();
    public int put(Item item) {
        int out = assigned.size() + baseWireHandle;
        assigned.add(item);
        return out;
    }
    public Item get(int itemId) {
        int i = itemId - baseWireHandle;
        return assigned.get(i);
    }
}