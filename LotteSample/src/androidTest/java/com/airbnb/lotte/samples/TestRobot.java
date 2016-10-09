package com.airbnb.lotte.samples;

import android.content.Context;

import com.airbnb.lotte.LotteAnimationView;
import com.facebook.testing.screenshot.Screenshot;
import com.facebook.testing.screenshot.ViewHelpers;

import java.util.Locale;

class TestRobot {
    private static final float[] DEFAULT_ANIMATED_PROGRESS = {0f, 0.05f, 0.10f, 0.15f, 0.20f, 0.25f, 0.30f, 0.40f, 0.50f, 0.60f, 0.70f, 0.80f, 0.90f, 1f};
    private static final float[] DEFAULT_STATIC_PROGRESS = {0f};

    static void testAnimatedFile(Context context, String fileName) {
        testFile(context, fileName, DEFAULT_ANIMATED_PROGRESS);
    }

    static void testStaticFile(Context context, String fileName) {
        testFile(context, fileName, DEFAULT_STATIC_PROGRESS);
    }

    private static void testFile(Context context, String fileName, float[] progress) {
        LotteAnimationView view = new LotteAnimationView(context);
        view.setAnimationSync(fileName);
        ViewHelpers.setupView(view)
                .layout();

        String nameWithoutExtension = fileName.substring(0, fileName.indexOf('.'));
        for (float p : progress) {
            view.setProgress(p);
            Screenshot.snap(view)
                    .setGroup(fileName)
                    .setName(String.format(Locale.US, "%s %d", nameWithoutExtension, (int) (p * 100)))
                    .record();
        }
        view.recycleBitmaps();
    }
}
