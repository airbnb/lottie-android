package com.airbnb.lotte.samples;

import android.app.Application;
import android.support.v4.util.Pair;

public class LotteApplication extends Application implements ILotteApplication {
    @Override
    public void startRecordingDroppedFrames() {
    }

    @Override
    public Pair<Integer, Long> stopRecordingDroppedFrames() {
        return new Pair<>(0, 0L);
    }
}
