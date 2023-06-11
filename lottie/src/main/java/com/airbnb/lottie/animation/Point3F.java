package com.airbnb.lottie.animation;

public class Point3F {
  public float x;
  public float y;
  public float z;

  public Point3F() {
  }

  public Point3F(float x, float y, float z) {
    this.x = x;
    this.y = y;
    this.z = z;
  }

  public void set(float x, float y, float z) {
    this.x = x;
    this.y = y;
    this.z = z;
  }

  /**
   * Returns true if the point's coordinates equal (x,y)
   */
  public final boolean equals(float x, float y, float z) {
    return this.x == x && this.y == y && this.z == z;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Point3F point3F = (Point3F) o;

    if (Float.compare(point3F.x, x) != 0) {
      return false;
    }
    if (Float.compare(point3F.y, y) != 0) {
      return false;
    }
    return Float.compare(point3F.z, z) == 0;
  }

  @Override
  public int hashCode() {
    int result = (x != 0.0f ? Float.floatToIntBits(x) : 0);
    result = 31 * result + (y != 0.0f ? Float.floatToIntBits(y) : 0);
    result = 31 * result + (z != 0.0f ? Float.floatToIntBits(z) : 0);
    return result;
  }

  @Override
  public String toString() {
    return "PointF(" + x + ", " + y + ", " + z + ")";
  }
}

