package com.airbnb.lotte.samples;


import android.test.ActivityInstrumentationTestCase2;

import com.airbnb.lotte.LotteAnimationView;
import com.facebook.testing.screenshot.Screenshot;
import com.facebook.testing.screenshot.ViewHelpers;

import org.junit.Test;

public class StillsTest extends ActivityInstrumentationTestCase2<MainActivity> {

    public StillsTest() {
        super(MainActivity.class);
    }

    @Test
    public void testHosts() {
        LotteAnimationView view = new LotteAnimationView(getActivity());
        view.setAnimationSync("Hosts Still.json");
        ViewHelpers.setupView(view)
                .layout();

        Screenshot.snap(view)
                .setName("Hosts Still")
                .record();

    }

    @Test
    public void testBusiness1() {
        LotteAnimationView view = new LotteAnimationView(getActivity());
        view.setAnimationSync("BusinessTravel01_Still.json");
        ViewHelpers.setupView(view)
                .layout();

        Screenshot.snap(view)
                .setGroup("Business Travel")
                .setName("1")
                .record();
    }


    @Test
    public void testBusiness2() {
        LotteAnimationView view = new LotteAnimationView(getActivity());
        view.setAnimationSync("BusinessTravel02_Still.json");
        ViewHelpers.setupView(view)
                .layout();

        Screenshot.snap(view)
                .setGroup("Business Travel")
                .setName("2")
                .record();
    }


    @Test
    public void testBusiness3() {
        LotteAnimationView view = new LotteAnimationView(getActivity());
        view.setAnimationSync("BusinessTravel03_Still.json");
        ViewHelpers.setupView(view)
                .layout();

        Screenshot.snap(view)
                .setGroup("Business Travel")
                .setName("3")
                .record();
    }
}
