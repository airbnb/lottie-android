package com.airbnb.lottie.samples;

import android.graphics.RectF;

import com.airbnb.lottie.MagicPointFF;

/**
 * Created by minf on 2017/2/22 0022.
 */

public class WiggleMagicPointFF extends MagicPointFF {

    private float lastX, lastY;


    private float time;
    private long lastTimeX, lastTimeY;

    private int startTime = Integer.MAX_VALUE;
    private int endTime = Integer.MAX_VALUE;
    private int f = -1;
    private RectF rectF = new RectF(0, 0, 0, 0);
    private float sy, sx;
    private float cx, cy;


    /**
     * var $bm_rt;\nif (time >= 0 && time <= 4) {\n    $bm_rt = wiggle(3, 4);\n} else {\n    $bm_rt = value;\n}
     */
    @Override
    public void onExpression(String expression) {

        String[] line = expression.split("\n");

        if (line.length == 0) {
            wiggle(1, 0);
            this.f = -1;
            return;
        }
        parsTimeExp(line[1]);
        String wiggleEx = line[2];
        if (startTime == Integer.MAX_VALUE || endTime == Integer.MAX_VALUE)
            return;
        int[] args = parsWiggle(wiggleEx);

        wiggle(args[0], args[1]);
    }


    private void wiggle(int time, int f) {
        this.time = 1f / time * 600;
        this.f = f;
        rectF.set(super.x() - f, super.y() - f, super.x() + f, super.y() + f);
        cx = lastX = super.x();
        cy = lastY = super.y();
    }

    private void parsTimeExp(String exp) {
        StringBuilder sb = new StringBuilder(exp);
        try {
            sb.delete(0, sb.indexOf(">=") + 2);
            sb.delete(sb.lastIndexOf(")"), sb.length());
            String startTime = sb.substring(0, sb.indexOf("&&"));
            this.startTime = Integer.valueOf(startTime.trim());
            String endTime = sb.substring(sb.indexOf("<=") + 2, sb.length());
            this.endTime = Integer.valueOf(endTime.trim());

        } catch (NumberFormatException e) {
            e.printStackTrace();
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        } finally {
            sb.setLength(0);
        }
    }

    private int[] parsWiggle(String exp) {
        int[] args = new int[]{0, 0};
        StringBuilder sb = new StringBuilder(exp);
        try {
            String s = sb.substring(sb.indexOf("(") + 1, sb.lastIndexOf(")"));
            String[] argsEx = s.split(",");
            if (argsEx.length == 2) {
                args[0] = Integer.valueOf(argsEx[0].trim());
                args[1] = Integer.valueOf(argsEx[1].trim());
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        } finally {
            sb.setLength(0);
        }
        return args;
    }

    @Override
    public float x() {

        if (canRandomX())
            randomRangeX(f);

        return lastX += sx;
    }

    @Override
    public float y() {
        if (canRandomY())
            randomRangeY(f);

        return lastY += sy;
    }

    private boolean canRandomX() {


        if (!canAnimation())
            return false;


        long time = System.currentTimeMillis();
        if (time - lastTimeX >= this.time) {
            lastTimeX = time;
            return true;
        }
        return false;
    }


    private boolean canRandomY() {


        if (!canAnimation())
            return false;


        long time = System.currentTimeMillis();
        if (time - lastTimeY >= this.time) {
            lastTimeY = time;
            return true;
        }
        return false;
    }

    private boolean canAnimation() {


        long t = getCurrentTime() / 1000;

        return !(t < startTime || t > endTime);
    }

    private void randomRangeX(float f) {

        if (f == -1)
            return;
        float lastX = this.lastX = cx;

        float r = (float) (Math.random() * (2 * f) - f);
        lastX += r;
        if (lastX < rectF.left) lastX = rectF.left;
        else if (lastX > rectF.right) lastX = rectF.right;

        sx = lastX - this.lastX;
        sx /= (this.time/10);

        cx = lastX;
    }

  private  void randomRangeY(float f) {

        if (f == -1)
            return;
        float lastY = this.lastY = cy;

        float r = (float) (Math.random() * (2 * f) - f);
        lastY += r;

        if (lastY < rectF.top) lastY = rectF.top;
        else if (lastY > rectF.bottom) lastY = rectF.bottom;

        sy = lastY - this.lastY;
        sy /= (this.time/10);

        cy = lastY;
    }
}
