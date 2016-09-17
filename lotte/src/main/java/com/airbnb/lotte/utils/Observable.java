package com.airbnb.lotte.utils;

import java.util.ArrayList;
import java.util.List;

public class Observable<T> {
    public interface OnChangedListener<T> {
        void onChanged(T value);
    }

    private final List<OnChangedListener<T>> listeners = new ArrayList<>(1);
    private T value;

    public void addChangeListener(OnChangedListener<T> listener) {
        if (listeners.contains(listener)) {
            throw new IllegalArgumentException("Listener already added.");
        }
        listeners.add(listener);
    }

    public void removeChangeListemer(OnChangedListener<T> listener) {
        if (!listeners.remove(listener)) {
            throw new IllegalArgumentException("Listener not added.");
        }
    }

    public void setValue(T value) {
        this.value = value;
        for (OnChangedListener<T> listener : listeners) {
            listener.onChanged(value);
        }
    }

    public T getValue() {
        return value;
    }
}
