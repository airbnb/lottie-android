package com.airbnb.lottie;

/**
 * Created by minf on 2017/2/22 0022.
 */

import android.graphics.Point;
import android.os.Parcel;
import android.os.Parcelable;


/**
 * CPointF holds two float coordinates
 */
public class CPointF implements Parcelable {
    private float x;
    private float y;

    public CPointF() {}

    public CPointF(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public CPointF(Point p) {
        this.x = p.x;
        this.y = p.y;
    }

    /**
     * Set the point's x and y coordinates
     */
    public  void set(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float x() {
        return x;
    }

    public float y() {
        return y;
    }

    /**
     * Set the point's x and y coordinates to the coordinates of p
     */
    public  void set(CPointF p) {
        this.x = p.x;
        this.y = p.y;
    }

    public final void negate() {
        x = -x;
        y = -y;
    }

    public final void offset(float dx, float dy) {
        x += dx;
        y += dy;
    }

    public  void scaleX(float scale)
    {
        x *= scale;
    }
    public  void scaleY(float scale)
    {
        y *= scale;
    }
    /**
     * Returns true if the point's coordinates equal (x,y)
     */
    public  boolean equals(float x, float y) {
        return this.x == x && this.y == y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CPointF CPointF = (CPointF) o;

        if (Float.compare(CPointF.x, x) != 0) return false;
        if (Float.compare(CPointF.y, y) != 0) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (x != +0.0f ? Float.floatToIntBits(x) : 0);
        result = 31 * result + (y != +0.0f ? Float.floatToIntBits(y) : 0);
        return result;
    }

    @Override
    public String toString() {
        return "CPointF(" + x + ", " + y + ")";
    }

    /**
     * Return the euclidian distance from (0,0) to the point
     */
    public  float length() {
        return length(x, y);
    }

    /**
     * Returns the euclidian distance from (0,0) to (x,y)
     */
    public static float length(float x, float y) {
        return (float) Math.hypot(x, y);
    }

    /**
     * Parcelable interface methods
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Write this point to the specified parcel. To restore a point from
     * a parcel, use readFromParcel()
     * @param out The parcel to write the point's coordinates into
     */
    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeFloat(x);
        out.writeFloat(y);
    }

    public static final Creator<CPointF> CREATOR = new Creator<CPointF>() {
        /**
         * Return a new point from the data in the specified parcel.
         */
        public CPointF createFromParcel(Parcel in) {
            CPointF r = new CPointF();
            r.readFromParcel(in);
            return r;
        }

        /**
         * Return an array of rectangles of the specified size.
         */
        public CPointF[] newArray(int size) {
            return new CPointF[size];
        }
    };

    /**
     * Set the point's coordinates from the data stored in the specified
     * parcel. To write a point to a parcel, call writeToParcel().
     *
     * @param in The parcel to read the point's coordinates from
     */
    public void readFromParcel(Parcel in) {
        x = in.readFloat();
        y = in.readFloat();
    }
}
