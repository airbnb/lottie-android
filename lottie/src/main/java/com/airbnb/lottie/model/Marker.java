package com.airbnb.lottie.model;

public class Marker {
  private static final String CARRIAGE_RETURN = "\r";

  private final String name;
  public final float startFrame;
  public final float durationFrames;

  public Marker(String name, float startFrame, float durationFrames) {
    this.name = name;
    this.durationFrames = durationFrames;
    this.startFrame = startFrame;
  }

  public String getName() {
    return name;
  }

  public float getStartFrame() {
    return startFrame;
  }

  public float getDurationFrames() {
    return durationFrames;
  }

  public boolean matchesName(String name) {
    if (this.name.equalsIgnoreCase(name)) {
      return true;
    }

    // It is easy for a designer to accidentally include an extra newline which will cause the name to not match what they would
    // expect. This is a convenience to precent unneccesary confusion.
    return this.name.endsWith(CARRIAGE_RETURN) && this.name.substring(0, this.name.length() - 1).equalsIgnoreCase(name);
  }
}
