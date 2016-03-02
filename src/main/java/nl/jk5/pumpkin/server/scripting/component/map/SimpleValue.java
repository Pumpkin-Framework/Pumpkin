package nl.jk5.pumpkin.server.scripting.component.map;

import nl.jk5.pumpkin.server.scripting.AbstractValue;

public abstract class SimpleValue<T> implements AbstractValue {

    private final T value;

    public SimpleValue(T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }
}
