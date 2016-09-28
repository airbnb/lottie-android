package com.airbnb.lotte.samples;

import android.test.ActivityInstrumentationTestCase2;

import com.airbnb.lotte.LotteAnimationView;
import com.facebook.testing.screenshot.Screenshot;
import com.facebook.testing.screenshot.ViewHelpers;

import org.junit.Test;

public class LightbulbTest extends ActivityInstrumentationTestCase2<MainActivity> {

    public LightbulbTest() {
        super(MainActivity.class);
    }

    @Test
    public void testLightbulb0() {
        LotteAnimationView view = new LotteAnimationView(getActivity());
        view.setAnimationSync("Lightbulb.json");
        ViewHelpers.setupView(view)
                .layout();

        Screenshot.snap(view)
                .setGroup("Lightbulb")
                .setName("Lightbulb 0")
                .record();
    }

    @Test
    public void testLightbulb10() {
        LotteAnimationView view = new LotteAnimationView(getActivity());
        view.setAnimationSync("Lightbulb.json");
        view.setProgress(0.10f);
        ViewHelpers.setupView(view)
                .layout();

        Screenshot.snap(view)
                .setGroup("Lightbulb")
                .setName("Lightbulb 10")
                .record();
    }

    @Test
    public void testLightbulb15() {
        LotteAnimationView view = new LotteAnimationView(getActivity());
        view.setAnimationSync("Lightbulb.json");
        view.setProgress(0.15f);
        ViewHelpers.setupView(view)
                .layout();

        Screenshot.snap(view)
                .setGroup("Lightbulb")
                .setName("Lightbulb 15")
                .record();
    }

    @Test
    public void testLightbulb20() {
        LotteAnimationView view = new LotteAnimationView(getActivity());
        view.setAnimationSync("Lightbulb.json");
        view.setProgress(0.20f);
        ViewHelpers.setupView(view)
                .layout();

        Screenshot.snap(view)
                .setGroup("Lightbulb")
                .setName("Lightbulb 20")
                .record();
    }

    @Test
    public void testLightbulb25() {
        LotteAnimationView view = new LotteAnimationView(getActivity());
        view.setAnimationSync("Lightbulb.json");
        view.setProgress(0.25f);
        ViewHelpers.setupView(view)
                .layout();

        Screenshot.snap(view)
                .setGroup("Lightbulb")
                .setName("Lightbulb 25")
                .record();
    }

    @Test
    public void testLightbulb50() {
        LotteAnimationView view = new LotteAnimationView(getActivity());
        view.setAnimationSync("Lightbulb.json");
        view.setProgress(0.50f);
        ViewHelpers.setupView(view)
                .layout();

        Screenshot.snap(view)
                .setGroup("Lightbulb")
                .setName("Lightbulb 50")
                .record();
    }

    @Test
    public void testLightbulb75() {
        LotteAnimationView view = new LotteAnimationView(getActivity());
        view.setAnimationSync("Lightbulb.json");
        view.setProgress(0.75f);
        ViewHelpers.setupView(view)
                .layout();

        Screenshot.snap(view)
                .setGroup("Lightbulb")
                .setName("Lightbulb 75")
                .record();
    }

    @Test
    public void testLightbulb100() {
        LotteAnimationView view = new LotteAnimationView(getActivity());
        view.setAnimationSync("Lightbulb.json");
        view.setProgress(1f);
        ViewHelpers.setupView(view)
                .layout();

        Screenshot.snap(view)
                .setGroup("Lightbulb")
                .setName("Lightbulb 100")
                .record();
    }
}
