package com.airbnb.lottie.animatable;

import java.util.ArrayList;
import java.util.List;

public class Observable<T> {
    public interface OnChangedListener {
        void onChanged();
    }

    private final List<OnChangedListener> listeners = new ArrayList<>(1);
    private T value;

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
        this.value = value;
        for (int i = 0; i < listeners.size(); i++) {
            listeners.get(i).onChanged();
        }
    }

    public T getValue() {
        return value;
    }
}
