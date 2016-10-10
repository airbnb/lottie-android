package com.airbnb.lotte.model;

public interface RemapInterface<T extends Number> {

    T remap(T inValue);

}
