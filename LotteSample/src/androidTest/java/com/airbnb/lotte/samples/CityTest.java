package com.airbnb.lotte.samples;


import android.test.ActivityInstrumentationTestCase2;

import com.airbnb.lotte.LotteAnimationView;
import com.facebook.testing.screenshot.Screenshot;
import com.facebook.testing.screenshot.ViewHelpers;

import org.junit.Test;

public class CityTest extends ActivityInstrumentationTestCase2<MainActivity> {

    public CityTest() {
        super(MainActivity.class);
    }

    @Test
    public void testCity0() {
        LotteAnimationView view = new LotteAnimationView(getActivity());
        view.setAnimationSync("City.json");
        ViewHelpers.setupView(view)
                .layout();

        Screenshot.snap(view)
                .setGroup("City")
                .setName("City 0")
                .record();
    }

    @Test
    public void testCity10() {
        LotteAnimationView view = new LotteAnimationView(getActivity());
        view.setAnimationSync("City.json");
        view.setProgress(0.10f);
        ViewHelpers.setupView(view)
                .layout();

        Screenshot.snap(view)
                .setGroup("City")
                .setName("City 10")
                .record();
    }

    @Test
    public void testCity15() {
        LotteAnimationView view = new LotteAnimationView(getActivity());
        view.setAnimationSync("City.json");
        view.setProgress(0.15f);
        ViewHelpers.setupView(view)
                .layout();

        Screenshot.snap(view)
                .setGroup("City")
                .setName("City 15")
                .record();
    }

    @Test
    public void testCity20() {
        LotteAnimationView view = new LotteAnimationView(getActivity());
        view.setAnimationSync("City.json");
        view.setProgress(0.20f);
        ViewHelpers.setupView(view)
                .layout();

        Screenshot.snap(view)
                .setGroup("City")
                .setName("City 20")
                .record();
    }

    @Test
    public void testCity25() {
        LotteAnimationView view = new LotteAnimationView(getActivity());
        view.setAnimationSync("City.json");
        view.setProgress(0.25f);
        ViewHelpers.setupView(view)
                .layout();

        Screenshot.snap(view)
                .setGroup("City")
                .setName("City 25")
                .record();
    }

    @Test
    public void testCity50() {
        LotteAnimationView view = new LotteAnimationView(getActivity());
        view.setAnimationSync("City.json");
        view.setProgress(0.50f);
        ViewHelpers.setupView(view)
                .layout();

        Screenshot.snap(view)
                .setGroup("City")
                .setName("City 50")
                .record();
    }

    @Test
    public void testCity75() {
        LotteAnimationView view = new LotteAnimationView(getActivity());
        view.setAnimationSync("City.json");
        view.setProgress(0.75f);
        ViewHelpers.setupView(view)
                .layout();

        Screenshot.snap(view)
                .setGroup("City")
                .setName("City 75")
                .record();
    }

    @Test
    public void testCity100() {
        LotteAnimationView view = new LotteAnimationView(getActivity());
        view.setAnimationSync("City.json");
        view.setProgress(1f);
        ViewHelpers.setupView(view)
                .layout();

        Screenshot.snap(view)
                .setGroup("City")
                .setName("City 100")
                .record();
    }
}