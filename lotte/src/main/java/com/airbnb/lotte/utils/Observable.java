package com.airbnb.lotte.utils;

import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class Observable<T> {
    public interface OnChangedListener {
        void onChanged();
    }

    private final List<OnChangedListener> listeners = new ArrayList<>(1);
    private T value;
    @Nullable private T lastValue;

    public Observable() {
    }

    public Observable(T value) {
        this.value = value;
    }

    public void addChangeListener(OnChangedListener listener) {
        if (listeners.contains(listener)) {
            throw new IllegalArgumentException("Listener already added.");
        }
        listeners.add(listener);
    }

    public void removeChangeListener(OnChangedListener listener) {
        if (!listeners.remove(listener)) {
            throw new IllegalArgumentException("Listener not added.");
        }
    }

    public void setValue(T value) {
        if (lastValue != null && lastValue.equals(value)) {
            return;
        }
        this.value = value;
        for (int i = 0; i < listeners.size(); i++) {
            listeners.get(i).onChanged();
        }
    }

    public T getValue() {
        return value;
    }
}
