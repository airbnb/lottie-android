package com.airbnb.lottie.model;

import org.junit.Test;

import static org.junit.Assert.*;

public class MarkerTest {

  @Test
  public void testMarkerWithCarriageReturn() {
    Marker marker = new Marker("Foo\r", 0f, 0f);
    assertTrue(marker.matchesName("foo"));
  }
}