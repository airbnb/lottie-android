package com.airbnb.lotte.animation;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

// TODO: potentially delete these completely
public class LotteAnimatableProperty {
    @IntDef({OPACITY, POSITION, ANCHOR_POINT, TRANSFORM, SUBLAYER_TRANSFORM, STROKE_COLOR, LINE_WIDTH, RECT_SIZE,
            RECT_POSITION, RECT_CORNER_RADIUS, BACKGROUND_COLOR, HIDDEN, TRIM_PATH_START, TRIM_PATH_END,
            TRIM_PATH_OFFSET, PATH, CIRCLE_POSITION, CIRCLE_SIZE, DASH_PATTERN, DASH_PATTERN_OFFSET, DASH_PATTERN_GAP})
    @Retention(RetentionPolicy.SOURCE)
    public @interface AnimatableProperty {}
    public static final int OPACITY = 1;
    public static final int POSITION = 2;
    public static final int ANCHOR_POINT = 3;
    public static final int TRANSFORM = 4;
    public static final int SUBLAYER_TRANSFORM = 5;
    public static final int STROKE_COLOR = 6;
    public static final int LINE_WIDTH = 7;
    public static final int RECT_SIZE = 8;
    public static final int RECT_POSITION = 9;
    public static final int RECT_CORNER_RADIUS = 10;
    public static final int BACKGROUND_COLOR = 11;
    public static final int HIDDEN = 12;
    public static final int TRIM_PATH_START = 13;
    public static final int TRIM_PATH_END = 14;
    public static final int TRIM_PATH_OFFSET = 15;
    public static final int PATH = 16;
    public static final int CIRCLE_POSITION = 17;
    public static final int CIRCLE_SIZE = 18;
    public static final int DASH_PATTERN = 19;
    public static final int DASH_PATTERN_OFFSET = 20;
    public static final int DASH_PATTERN_GAP = 21;
}
