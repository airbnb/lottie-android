package com.airbnb.lotte.samples;

import android.test.ActivityInstrumentationTestCase2;

import com.airbnb.lotte.LotteAnimationView;
import com.facebook.testing.screenshot.Screenshot;
import com.facebook.testing.screenshot.ViewHelpers;

import org.junit.Test;

public class AlarmTest extends ActivityInstrumentationTestCase2<MainActivity> {

    public AlarmTest() {
        super(MainActivity.class);
    }

    @Test
    public void testAlarm0() {
        LotteAnimationView view = new LotteAnimationView(getActivity());
        view.setAnimationSync("Alarm.json");
        ViewHelpers.setupView(view)
                .layout();

        Screenshot.snap(view)
                .setGroup("Alarm")
                .setName("Alarm 0")
                .record();
    }

    @Test
    public void testAlarm10() {
        LotteAnimationView view = new LotteAnimationView(getActivity());
        view.setAnimationSync("Alarm.json");
        view.setProgress(0.10f);
        ViewHelpers.setupView(view)
                .layout();

        Screenshot.snap(view)
                .setGroup("Alarm")
                .setName("Alarm 10")
                .record();
    }

    @Test
    public void testAlarm15() {
        LotteAnimationView view = new LotteAnimationView(getActivity());
        view.setAnimationSync("Alarm.json");
        view.setProgress(0.15f);
        ViewHelpers.setupView(view)
                .layout();

        Screenshot.snap(view)
                .setGroup("Alarm")
                .setName("Alarm 15")
                .record();
    }

    @Test
    public void testAlarm20() {
        LotteAnimationView view = new LotteAnimationView(getActivity());
        view.setAnimationSync("Alarm.json");
        view.setProgress(0.20f);
        ViewHelpers.setupView(view)
                .layout();

        Screenshot.snap(view)
                .setGroup("Alarm")
                .setName("Alarm 20")
                .record();
    }

    @Test
    public void testAlarm25() {
        LotteAnimationView view = new LotteAnimationView(getActivity());
        view.setAnimationSync("Alarm.json");
        view.setProgress(0.25f);
        ViewHelpers.setupView(view)
                .layout();

        Screenshot.snap(view)
                .setGroup("Alarm")
                .setName("Alarm 25")
                .record();
    }

    @Test
    public void testAlarm50() {
        LotteAnimationView view = new LotteAnimationView(getActivity());
        view.setAnimationSync("Alarm.json");
        view.setProgress(0.50f);
        ViewHelpers.setupView(view)
                .layout();

        Screenshot.snap(view)
                .setGroup("Alarm")
                .setName("Alarm 50")
                .record();
    }

    @Test
    public void testAlarm75() {
        LotteAnimationView view = new LotteAnimationView(getActivity());
        view.setAnimationSync("Alarm.json");
        view.setProgress(0.75f);
        ViewHelpers.setupView(view)
                .layout();

        Screenshot.snap(view)
                .setGroup("Alarm")
                .setName("Alarm 75")
                .record();
    }

    @Test
    public void testAlarm100() {
        LotteAnimationView view = new LotteAnimationView(getActivity());
        view.setAnimationSync("Alarm.json");
        view.setProgress(1f);
        ViewHelpers.setupView(view)
                .layout();

        Screenshot.snap(view)
                .setGroup("Alarm")
                .setName("Alarm 100")
                .record();
    }
}