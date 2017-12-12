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

  @Test
  public void testFullyQualifiedStroke() {
    assertSize(1, "Shape Layer 1", "Group 1", "Rectangle", "Stroke");
  }

  @Test
  public void testFullyQualifiedInvalidStroke() {
    assertSize(0, "Shape Layer 1", "Group 1", "Rectangle", "INVALID");
  }

  @Test
  public void testFullyQualifiedLayer() {
    assertSize(1, "Shape Layer 1");
  }

  @Test
  public void testFullyQualifiedInvalidLayer() {
    assertSize(0, "INVALID");
  }

  @Test
  public void testWildcardLayer() {
    assertSize(2, "*", "Group 1", "Rectangle", "Stroke");
  }

  @Test
  public void testWildcardLayerAndGroup() {
    assertSize(4, "*", "*", "Rectangle", "Stroke");
  }

  @Test
  public void testWildcardLayerGroupAndRectangle() {
    assertSize(4, "*", "*", "*", "Stroke");
  }

  @Test
  public void testWildcardLayerGroupRectangleAndContents() {
    // This will be 8 once other contents (like the path/rectangle) are KeyFrameElements
    assertSize(4, "*", "*", "*", "*");
  }

  @Test
  public void testWildcardLayerGroupRectangle() {
    // This will be 8 once other contents (like the path/rectangle) are KeyFrameElements
    assertSize(4, "*", "*", "*");
  }

  @Test
  public void testGlobstarStroke() {
    assertSize(4, "**", "Stroke");
  }

  @Test
  public void testGlobstar() {
    // This will be 18 once other contents (like the path/rectangle) are KeyFrameElements
    assertSize(14, "**");
  }

  private void assertSize(int size, String... keys) {
    KeyPath keyPath = new KeyPath(keys);
    List<KeyPath> resolvedKeyPaths = lottieDrawable.resolveKeyPath(keyPath);
    assertEquals(size, resolvedKeyPaths.size());
  }
}
