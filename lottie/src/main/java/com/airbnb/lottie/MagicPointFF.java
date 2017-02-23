package com.airbnb.lottie;


import android.support.annotation.FloatRange;

/**
 * Created by minf on 2017/2/22 0022.
 */

public abstract class MagicPointFF extends CPointF {


    private CPointF point;
    private LottieComposition composition;
    private float progress;
    private String expression;


    public MagicPointFF() {
    }

    void setExpression(String expression) {
        this.expression = expression;
    }

    public MagicPointFF copy(CPointF point) {
        try {
            MagicPointFF mp = this.getClass().newInstance();
            mp.setExpression(expression);
            mp.init(point, composition);

            return mp;

        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public abstract void onExpression(String expression);


    void init(CPointF point, LottieComposition composition) {
        this.point = point;
        this.composition = composition;
        onExpression(expression);
    }

    public void setPoint(CPointF point) {
        this.point = point;
    }

    void setProgress(@FloatRange(from = 0f, to = 1f) float progress) {
        this.progress = progress;
    }

    public long getCurrentTime() {

        return (long) (composition.getDuration() * progress);
    }

    @Override
    public float x() {
        return point.x();
    }

    @Override
    public float y() {
        return point.y();
    }

    @Override
    public void scaleX(float scale) {
        point.scaleX(scale);
    }

    @Override
    public void scaleY(float scale) {
        point.scaleY(scale);
    }

    @Override
    public boolean equals(Object o) {
        return point.equals(o);
    }

    @Override
    public void set(float x, float y) {
        point.set(x, y);
    }

    @Override
    public void set(CPointF p) {
        point.set(p);
    }

    @Override
    public boolean equals(float x, float y) {
        return point.equals(x, y);
    }

    @Override
    public float length() {
        return point.length();
    }
}
