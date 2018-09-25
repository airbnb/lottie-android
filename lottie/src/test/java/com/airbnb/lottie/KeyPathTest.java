package com.airbnb.lottie;

import android.util.JsonReader;

import com.airbnb.lottie.model.KeyPath;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import static junit.framework.Assert.assertEquals;

public class KeyPathTest extends BaseTest {
  private static final String[] V = {
      "Shape Layer 1",
      "Group 1",
      "Rectangle",
      "Stroke"
  };
  private static final String I = "INVALID";
  private static final String W = "*";
  private static final String G = "**";

  private LottieDrawable lottieDrawable;

  @Before
  public void setupDrawable() {
    lottieDrawable = new LottieDrawable();
    LottieComposition composition = LottieCompositionFactory.fromJsonStringSync(Fixtures.SQUARES, "squares").getValue();
    lottieDrawable.setComposition(composition);
  }

  //<editor-fold desc="Basic Tests">
  @Test public void testV() { assertSize(1, V[0]); }
  @Test public void testI() { assertSize(0, I); }

  @Test public void testII() { assertSize(0, I,    I); }
  @Test public void testIV() { assertSize(0, I,    V[1]); }
  @Test public void testVI() { assertSize(0, V[0], I); }
  @Test public void testVV() { assertSize(1, V[0], V[1]); }

  @Test public void testIII() { assertSize(0, I,    I,    I); }
  @Test public void testIIV() { assertSize(0, I,    I,    V[3]); }
  @Test public void testIVI() { assertSize(0, I,    V[2], I); }
  @Test public void testIVV() { assertSize(0, I,    V[2], V[3]); }
  @Test public void testVII() { assertSize(0, V[0], I,    I); }
  @Test public void testVIV() { assertSize(0, V[0], I,    V[2]); }
  @Test public void testVVI() { assertSize(0, V[0], V[1], I); }
  @Test public void testVVV() { assertSize(1, V[0], V[1], V[2]); }

  @Test public void testIIII() { assertSize(0, I,    I,    I,    I); }
  @Test public void testIIIV() { assertSize(0, I,    I,    I,    V[3]); }
  @Test public void testIIVI() { assertSize(0, I,    I,    V[2], I); }
  @Test public void testIIVV() { assertSize(0, I,    I,    V[2], V[3]); }
  @Test public void testIVII() { assertSize(0, I,    V[1], I,    I); }
  @Test public void testIVIV() { assertSize(0, I,    V[1], I,    V[3]); }
  @Test public void testIVVI() { assertSize(0, I,    V[1], V[2], V[3]); }
  @Test public void testIVVV() { assertSize(0, I,    V[1], V[2], V[3]); }
  @Test public void testVIII() { assertSize(0, V[0], I,    I,    I); }
  @Test public void testVIIV() { assertSize(0, V[0], I,    I,    V[3]); }
  @Test public void testVIVI() { assertSize(0, V[0], I,    V[2], I); }
  @Test public void testVIVV() { assertSize(0, V[0], I,    V[2], V[3]); }
  @Test public void testVVII() { assertSize(0, V[0], V[1], I,    I); }
  @Test public void testVVIV() { assertSize(0, V[0], V[1], I,    V[3]); }
  @Test public void testVVVI() { assertSize(0, V[0], V[1], V[2], I); }
  @Test public void testVVVV() { assertSize(1, V[0], V[1], V[2], V[3]); }
  //</editor-fold>

  //<editor-fold desc="One Wildcard">
  @Test public void testWVVV() { assertSize(2, W,    V[1], V[2], V[3]); }
  @Test public void testVWVV() { assertSize(2, V[0], W,    V[2], V[3]); }
  @Test public void testVVWV() { assertSize(1, V[0], V[1], W,    V[3]); }
  @Test public void testVVVW() { assertSize(2, V[0], V[1], V[2], W); }
  //</editor-fold>

  //<editor-fold desc="Two Wildcards">
  @Test public void testWWVV() { assertSize(4, W, W,    V[2], V[3]); }
  @Test public void testWVWV() { assertSize(2, W, V[1], W,    V[3]); }
  @Test public void testWVVW() { assertSize(4, W, V[1], V[2], W); }
  @Test public void testWWIV() { assertSize(0, W, W,    I,    V[3]); }
  @Test public void testWWVI() { assertSize(0, W, W,    V[2], I); }
  @Test public void testWVW() { assertSize(2, W,  V[1], W); }
  //</editor-fold>

  //<editor-fold desc="Three Wildcards">
  @Test public void testWWW() { assertSize(4, W, W, W); }
  @Test public void testWWWV() { assertSize(4, W, W, W, V[3]); }
  @Test public void testWWWI() { assertSize(0, W, W, W, I); }
  //</editor-fold>

  //<editor-fold desc="Four Wildcards">
  @Test public void testWWWW() { assertSize(8, W, W, W, W); }
  //</editor-fold>

  //<editor-fold desc="One Globstar">
  @Test public void testG() { assertSize(18, G); }
  @Test public void testGI() { assertSize(0, G, I); }
  @Test public void testGV0() { assertSize(1, G, V[0]); }
  @Test public void testGV0V0() { assertSize(0, G, V[0], V[0]); }
  @Test public void testGV1() { assertSize(2, G, V[1]); }
  @Test public void testGV2() { assertSize(4, G, V[2]); }
  @Test public void testGV3() { assertSize(4, G, V[3]); }
  //</editor-fold>

  //<editor-fold desc="Two Globstars">
  @Test public void testGV0G() { assertSize(9, G, V[0], G); }
  @Test public void testGV1G() { assertSize(8, G, V[1], G); }
  @Test public void testGV2G() { assertSize(12, G, V[2], G); }
  @Test public void testGIG() {  assertSize(0, G, I,    G); }
  //</editor-fold>

  //<editor-fold desc="Wildcard and Globstar">
  @Test public void testWG() {   assertSize(18, W, G); }
  @Test public void testGV0W() { assertSize(2,  G, V[0], W); }
  @Test public void testWV0I() { assertSize(0,  W, V[0], I); }
  @Test public void testGV1W() { assertSize(2,  G, V[1], W); }
  @Test public void testWV1I() { assertSize(0,  W, V[1], I); }
  @Test public void testGV2W() { assertSize(8,  G, V[2], W); }
  @Test public void testWV2I() { assertSize(0,  W, V[2], I); }
  //</editor-fold>

  private void assertSize(int size, String... keys) {
    KeyPath keyPath = new KeyPath(keys);
    List<KeyPath> resolvedKeyPaths = lottieDrawable.resolveKeyPath(keyPath);
    assertEquals(size, resolvedKeyPaths.size());
  }
}
