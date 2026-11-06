package org.homework1;

public final class ImmutableClass {
    private final MutableClass mutableField;

    public ImmutableClass(MutableClass mutableField) {
        this.mutableField = new MutableClass(mutableField);
    }

    public MutableClass getMutableField() {
        return new MutableClass(this.mutableField);
    }
}