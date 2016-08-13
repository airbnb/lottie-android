package com.airbnb.lotte.model;

public interface LotteAnimatableValue {

    Object animationForKeyPath(String keyPath);
    boolean hasAnimation();

}
