package org.homework1;

public class MutableClass {
    private int value;

    public MutableClass(int value) {
        this.value = value;
    }

    public MutableClass(MutableClass copy) {
        this.value = copy.value;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}