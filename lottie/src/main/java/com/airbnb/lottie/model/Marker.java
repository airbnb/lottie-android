package com.airbnb.lottie.model;

public class Marker {

  public final String name;
  public final float startFrame;
  public final float durationFrames;

  public Marker(String name, float startFrame, float durationFrames) {
    this.name = name;
    this.durationFrames = durationFrames;
    this.startFrame = startFrame;
  }
}
