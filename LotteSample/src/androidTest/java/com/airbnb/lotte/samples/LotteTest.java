package com.airbnb.lotte.samples;


import android.test.ActivityInstrumentationTestCase2;

import com.airbnb.lotte.LotteAnimationView;
import com.facebook.testing.screenshot.Screenshot;
import com.facebook.testing.screenshot.ViewHelpers;

import org.junit.Test;

public class LotteTest extends ActivityInstrumentationTestCase2<MainActivity> {

    public LotteTest() {
        super(MainActivity.class);
    }

    @Test

    public void testSquishy() {
        LotteAnimationView view = new LotteAnimationView(getActivity());
        view.setAnimation("twoShapes.json");
        ViewHelpers.setupView(view)
                .layout();

        Screenshot.snap(view)
                .setName("Two Shapes")
                .record();

    }
}
