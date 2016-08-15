package com.airbnb.lotte.utils;

import android.graphics.PointF;

public class MiscUtils {

    public static PointF addPoints(PointF p1, PointF p2) {
        return new PointF(p1.x + p2.x, p1.y + p2.y);
    }

}
