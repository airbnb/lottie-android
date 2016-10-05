package com.airbnb.lotte.samples;

import android.app.Application;
import android.view.Gravity;

import com.codemonkeylabs.fpslibrary.FrameDataCallback;
import com.codemonkeylabs.fpslibrary.TinyDancer;

public class LotteApplication extends Application {

    @Override
    public void onCreate() {
        TinyDancer.create()
                .startingGravity(Gravity.TOP|Gravity.END)
                .startingXPosition(50)
                .startingYPosition(50)
                .addFrameDataCallback(new FrameDataCallback() {
                    @Override
                    public void doFrame(long previousFrameNS, long currentFrameNS, int droppedFrames) {
                        //collect your stats here
                    }
                })
                .show(this);
    }
}
