package com.airbnb.lottie.animation.content;

import android.graphics.Path;

import com.airbnb.lottie.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class CompoundTrimPathContent {
    private List<TrimPathContent> contents = new ArrayList<>();

    void addTrimPath(TrimPathContent trimPath) {
        contents.add(trimPath);
    }

    public void apply(Path path) {
        for (int i = 0; i < contents.size(); i++) {
            Utils.applyTrimPathIfNeeded(path, contents.get(i));
        }
    }
}
