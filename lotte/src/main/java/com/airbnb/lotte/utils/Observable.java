package com.airbnb.lotte.utils;

import java.util.ArrayList;
import java.util.List;

public class Observable<T> {
    public interface OnChangedListener {
        void onChanged();
    }

    private final List<OnChangedListener> listeners = new ArrayList<>(1);
    private T value;

    public void addChangeListener(OnChangedListener listener) {
        if (listeners.contains(listener)) {
            throw new IllegalArgumentException("Listener already added.");
        }
        listeners.add(listener);
    }

    public void removeChangeListemer(OnChangedListener listener) {
        if (!listeners.remove(listener)) {
            throw new IllegalArgumentException("Listener not added.");
        }
    }

    public void setValue(T value) {
        this.value = value;
        for (OnChangedListener listener : listeners) {
            listener.onChanged();
        }
    }

    public T getValue() {
        return value;
    }
}
