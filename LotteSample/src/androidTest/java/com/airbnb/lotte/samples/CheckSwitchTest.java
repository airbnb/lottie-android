package com.airbnb.lotte.samples;

import android.test.ActivityInstrumentationTestCase2;

import com.airbnb.lotte.LotteAnimationView;
import com.facebook.testing.screenshot.Screenshot;
import com.facebook.testing.screenshot.ViewHelpers;

import org.junit.Test;

public class CheckSwitchTest extends ActivityInstrumentationTestCase2<MainActivity> {

    public CheckSwitchTest() {
        super(MainActivity.class);
    }

    @Test
    public void testCheckSwitch0() {
        LotteAnimationView view = new LotteAnimationView(getActivity());
        view.setAnimationSync("CheckSwitch.json");
        ViewHelpers.setupView(view)
                .layout();

        Screenshot.snap(view)
                .setGroup("CheckSwitch")
                .setName("CheckSwitch 0")
                .record();
    }

    @Test
    public void testCheckSwitch10() {
        LotteAnimationView view = new LotteAnimationView(getActivity());
        view.setAnimationSync("CheckSwitch.json");
        view.setProgress(0.10f);
        ViewHelpers.setupView(view)
                .layout();

        Screenshot.snap(view)
                .setGroup("CheckSwitch")
                .setName("CheckSwitch 10")
                .record();
    }

    @Test
    public void testCheckSwitch15() {
        LotteAnimationView view = new LotteAnimationView(getActivity());
        view.setAnimationSync("CheckSwitch.json");
        view.setProgress(0.15f);
        ViewHelpers.setupView(view)
                .layout();

        Screenshot.snap(view)
                .setGroup("CheckSwitch")
                .setName("CheckSwitch 15")
                .record();
    }

    @Test
    public void testCheckSwitch20() {
        LotteAnimationView view = new LotteAnimationView(getActivity());
        view.setAnimationSync("CheckSwitch.json");
        view.setProgress(0.20f);
        ViewHelpers.setupView(view)
                .layout();

        Screenshot.snap(view)
                .setGroup("CheckSwitch")
                .setName("CheckSwitch 20")
                .record();
    }

    @Test
    public void testCheckSwitch25() {
        LotteAnimationView view = new LotteAnimationView(getActivity());
        view.setAnimationSync("CheckSwitch.json");
        view.setProgress(0.25f);
        ViewHelpers.setupView(view)
                .layout();

        Screenshot.snap(view)
                .setGroup("CheckSwitch")
                .setName("CheckSwitch 25")
                .record();
    }

    @Test
    public void testCheckSwitch50() {
        LotteAnimationView view = new LotteAnimationView(getActivity());
        view.setAnimationSync("CheckSwitch.json");
        view.setProgress(0.50f);
        ViewHelpers.setupView(view)
                .layout();

        Screenshot.snap(view)
                .setGroup("CheckSwitch")
                .setName("CheckSwitch 50")
                .record();
    }

    @Test
    public void testCheckSwitch75() {
        LotteAnimationView view = new LotteAnimationView(getActivity());
        view.setAnimationSync("CheckSwitch.json");
        view.setProgress(0.75f);
        ViewHelpers.setupView(view)
                .layout();

        Screenshot.snap(view)
                .setGroup("CheckSwitch")
                .setName("CheckSwitch 75")
                .record();
    }

    @Test
    public void testCheckSwitch100() {
        LotteAnimationView view = new LotteAnimationView(getActivity());
        view.setAnimationSync("CheckSwitch.json");
        view.setProgress(1f);
        ViewHelpers.setupView(view)
                .layout();

        Screenshot.snap(view)
                .setGroup("CheckSwitch")
                .setName("CheckSwitch 100")
                .record();
    }
}