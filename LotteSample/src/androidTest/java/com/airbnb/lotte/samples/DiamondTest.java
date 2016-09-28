package com.airbnb.lotte.samples;

import android.test.ActivityInstrumentationTestCase2;

import com.airbnb.lotte.LotteAnimationView;
import com.facebook.testing.screenshot.Screenshot;
import com.facebook.testing.screenshot.ViewHelpers;

import org.junit.Test;

public class DiamondTest extends ActivityInstrumentationTestCase2<MainActivity> {

    public DiamondTest() {
        super(MainActivity.class);
    }

    @Test
    public void testDiamond0() {
        LotteAnimationView view = new LotteAnimationView(getActivity());
        view.setAnimationSync("Diamond 2.json");
        ViewHelpers.setupView(view)
                .layout();

        Screenshot.snap(view)
                .setGroup("Diamond")
                .setName("Diamond 0")
                .record();
    }

    @Test
    public void testDiamond10() {
        LotteAnimationView view = new LotteAnimationView(getActivity());
        view.setAnimationSync("Diamond 2.json");
        view.setProgress(0.10f);
        ViewHelpers.setupView(view)
                .layout();

        Screenshot.snap(view)
                .setGroup("Diamond")
                .setName("Diamond 10")
                .record();
    }

    @Test
    public void testDiamond15() {
        LotteAnimationView view = new LotteAnimationView(getActivity());
        view.setAnimationSync("Diamond 2.json");
        view.setProgress(0.15f);
        ViewHelpers.setupView(view)
                .layout();

        Screenshot.snap(view)
                .setGroup("Diamond")
                .setName("Diamond 15")
                .record();
    }

    @Test
    public void testDiamond20() {
        LotteAnimationView view = new LotteAnimationView(getActivity());
        view.setAnimationSync("Diamond 2.json");
        view.setProgress(0.20f);
        ViewHelpers.setupView(view)
                .layout();

        Screenshot.snap(view)
                .setGroup("Diamond")
                .setName("Diamond 20")
                .record();
    }

    @Test
    public void testDiamond25() {
        LotteAnimationView view = new LotteAnimationView(getActivity());
        view.setAnimationSync("Diamond 2.json");
        view.setProgress(0.25f);
        ViewHelpers.setupView(view)
                .layout();

        Screenshot.snap(view)
                .setGroup("Diamond")
                .setName("Diamond 25")
                .record();
    }

    @Test
    public void testDiamond50() {
        LotteAnimationView view = new LotteAnimationView(getActivity());
        view.setAnimationSync("Diamond 2.json");
        view.setProgress(0.50f);
        ViewHelpers.setupView(view)
                .layout();

        Screenshot.snap(view)
                .setGroup("Diamond")
                .setName("Diamond 50")
                .record();
    }

    @Test
    public void testDiamond75() {
        LotteAnimationView view = new LotteAnimationView(getActivity());
        view.setAnimationSync("Diamond 2.json");
        view.setProgress(0.75f);
        ViewHelpers.setupView(view)
                .layout();

        Screenshot.snap(view)
                .setGroup("Diamond")
                .setName("Diamond 75")
                .record();
    }

    @Test
    public void testDiamond100() {
        LotteAnimationView view = new LotteAnimationView(getActivity());
        view.setAnimationSync("Diamond 2.json");
        view.setProgress(1f);
        ViewHelpers.setupView(view)
                .layout();

        Screenshot.snap(view)
                .setGroup("Diamond")
                .setName("Diamond 100")
                .record();
    }
}
