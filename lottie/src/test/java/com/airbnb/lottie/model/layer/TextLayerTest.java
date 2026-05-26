package com.airbnb.lottie.model.layer;

import android.graphics.Color;

import com.airbnb.lottie.BaseTest;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TextLayerTest extends BaseTest {

  @Test public void getTextPaintAlphaPreservesTransparentMissingColor() {
    assertEquals(0, TextLayer.getTextPaintAlpha(Color.TRANSPARENT, 255));
  }

  @Test public void getTextPaintAlphaPreservesOpaqueExplicitColor() {
    assertEquals(128, TextLayer.getTextPaintAlpha(Color.BLACK, 128));
  }

  @Test public void getTextPaintAlphaMultipliesColorAndLayerAlpha() {
    assertEquals(64, TextLayer.getTextPaintAlpha(Color.argb(128, 0, 0, 0), 128));
  }
}
