package com.airbnb.lottie;

import android.app.Application;

import com.airbnb.lottie.model.KeyPath;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

import static junit.framework.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
public class KeyPathTest {
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
    Application context = RuntimeEnvironment.application;
    lottieDrawable = new LottieDrawable();

    Writer writer = new StringWriter();
    char[] buffer = new char[1024];
    try (InputStream is = context.getResources().openRawResource(R.raw.squares)) {
      Reader reader = new BufferedReader(new InputStreamReader(is));
      int n;
      while ((n = reader.read(buffer)) != -1) {
        writer.write(buffer, 0, n);
      }
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }

    try {
      LottieComposition composition = LottieComposition.Factory
          .fromJsonSync(context.getResources(), new JSONObject(writer.toString()));
      lottieDrawable.setComposition(composition);
    } catch (JSONException e) {
      throw new IllegalStateException(e);
    }
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
  @Test public void testVVVW() { assertSize(1, V[0], V[1], V[2], W); }
  //</editor-fold>

  //<editor-fold desc="Two Wildcards">
  @Test public void testWWVV() { assertSize(4, W, W,    V[2], V[3]); }
  @Test public void testWVWV() { assertSize(2, W, V[1], W,    V[3]); }
  @Test public void testWVVW() { assertSize(2, W, V[1], V[2], W); }
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
  @Test public void testWWWW() { assertSize(4, W, W, W, W); }
  //</editor-fold>

  //<editor-fold desc="One Globstar">
  @Test public void testG() { assertSize(14, G); }
  @Test public void testGI() { assertSize(0, G, I); }
  @Test public void testGV0() { assertSize(1, G, V[0]); }
  @Test public void testGV0V0() { assertSize(0, G, V[0], V[0]); }
  @Test public void testGV1() { assertSize(2, G, V[1]); }
  @Test public void testGV1W() { assertSize(2, G, V[1], W); }
  @Test public void testGV2() { assertSize(4, G, V[2]); }
  @Test public void testGV3() { assertSize(4, G, V[3]); }
  //</editor-fold>

  //<editor-fold desc="Two Globstars">
  @Test public void testGV0G() { assertSize(6, G, V[0], G); }
  @Test public void testGV1G() { assertSize(6, G, V[1], G); }
  @Test public void testGV2G() { assertSize(6, G, V[2], G); }
  @Test public void testGIG() { assertSize(0, G, I, G); }
  //</editor-fold>

  private void assertSize(int size, String... keys) {
    KeyPath keyPath = new KeyPath(keys);
    List<KeyPath> resolvedKeyPaths = lottieDrawable.resolveKeyPath(keyPath);
    assertEquals(size, resolvedKeyPaths.size());
  }
}
