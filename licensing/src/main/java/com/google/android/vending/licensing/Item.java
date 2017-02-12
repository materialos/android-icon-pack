package com.google.android.vending.licensing;

/**
 * @author Aidan Follestad (afollestad)
 */
public class Item {

    private String name;
    private String value;

    public Item(String n, String v) {
        name = n;
        value = v;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }
}