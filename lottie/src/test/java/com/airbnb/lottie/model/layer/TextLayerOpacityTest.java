package com.airbnb.lottie.model.layer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;

import com.airbnb.lottie.BaseTest;
import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.LottieCompositionFactory;
import com.airbnb.lottie.LottieDrawable;
import com.airbnb.lottie.LottieResult;
import com.airbnb.lottie.RenderMode;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A text layer's own opacity is already part of the alpha that BaseLayer passes to drawLayer,
 * so it must only be applied once.
 * https://github.com/airbnb/lottie-android/issues/2747
 */
public class TextLayerOpacityTest extends BaseTest {

  @Test
  public void testTextLayerOpacityIsAppliedOnceWithFont() {
    List<Integer> alphas = drawAndRecordAlphas(fontModeJson(50, null), true);
    assertAllAlphas(128, alphas);
  }

  @Test
  public void testTextLayerOpacityIsAppliedOnceWithGlyphs() {
    List<Integer> alphas = drawAndRecordAlphas(glyphModeJson(50), false);
    assertAllAlphas(128, alphas);
  }

  @Test
  public void testParentAlphaIsStillAppliedToText() {
    List<Integer> alphas = drawAndRecordAlphas(fontModeJson(100, 50), true);
    assertAllAlphas(128, alphas);
  }

  @Test
  public void testTextLayerAndParentOpacityAreMultiplied() {
    List<Integer> alphas = drawAndRecordAlphas(fontModeJson(50, 50), true);
    assertAllAlphas(64, alphas);
  }

  private static void assertAllAlphas(int expected, List<Integer> alphas) {
    assertFalse("No text was drawn", alphas.isEmpty());
    for (int alpha : alphas) {
      assertEquals(expected, alpha, 1);
    }
  }

  private static List<Integer> drawAndRecordAlphas(String json, boolean useFontMap) {
    LottieResult<LottieComposition> result = LottieCompositionFactory.fromJsonStringSync(json, null);
    assertNull(result.getException());
    LottieDrawable drawable = new LottieDrawable();
    // Software rendering would draw into an internal bitmap instead of the recording canvas.
    drawable.setRenderMode(RenderMode.HARDWARE);
    if (useFontMap) {
      // A font map disables glyph rendering, so only set it for font mode.
      drawable.setFontMap(Collections.singletonMap("Test", Typeface.DEFAULT));
    }
    drawable.setComposition(result.getValue());
    drawable.setBounds(0, 0, 100, 100);
    AlphaRecordingCanvas canvas = new AlphaRecordingCanvas();
    drawable.draw(canvas);
    return canvas.alphas;
  }

  /**
   * @param parentOpacity When not null, the text layer is placed inside a precomp layer with this opacity.
   */
  private static String fontModeJson(int textOpacity, Integer parentOpacity) {
    String textLayer = textLayerJson(textOpacity);
    String layers;
    String assets;
    if (parentOpacity == null) {
      layers = textLayer;
      assets = "";
    } else {
      layers = "{\"ty\":0,\"ind\":1,\"refId\":\"comp_0\",\"w\":100,\"h\":100,\"ip\":0,\"op\":10,\"st\":0,"
          + "\"ks\":{\"o\":{\"a\":0,\"k\":" + parentOpacity + "}}}";
      assets = "{\"id\":\"comp_0\",\"layers\":[" + textLayer + "]}";
    }
    return "{\"v\":\"5.10.2\",\"fr\":10,\"ip\":0,\"op\":10,\"w\":100,\"h\":100,"
        + "\"assets\":[" + assets + "],"
        + "\"fonts\":{\"list\":[{\"fName\":\"Test\",\"fFamily\":\"Test\",\"fStyle\":\"Regular\",\"ascent\":70}]},"
        + "\"layers\":[" + layers + "]}";
  }

  private static String glyphModeJson(int textOpacity) {
    return "{\"v\":\"5.10.2\",\"fr\":10,\"ip\":0,\"op\":10,\"w\":100,\"h\":100,\"assets\":[],"
        + "\"fonts\":{\"list\":[{\"fName\":\"Test\",\"fFamily\":\"Test\",\"fStyle\":\"Regular\",\"ascent\":70}]},"
        + "\"layers\":[" + textLayerJson(textOpacity) + "],"
        + "\"chars\":[{\"ch\":\"A\",\"size\":20,\"style\":\"Regular\",\"w\":60,\"fFamily\":\"Test\","
        + "\"data\":{\"shapes\":[{\"ty\":\"gr\",\"it\":[{\"ty\":\"sh\",\"ks\":{\"a\":0,\"k\":{"
        + "\"i\":[[0,0],[0,0],[0,0]],\"o\":[[0,0],[0,0],[0,0]],\"v\":[[0,0],[30,-60],[60,0]],\"c\":true}}}]}]}}]}";
  }

  private static String textLayerJson(int opacity) {
    return "{\"ty\":5,\"ind\":1,\"ip\":0,\"op\":10,\"st\":0,"
        + "\"ks\":{\"o\":{\"a\":0,\"k\":" + opacity + "},\"p\":{\"a\":0,\"k\":[10,50,0]}},"
        + "\"t\":{\"d\":{\"k\":[{\"s\":{\"s\":20,\"f\":\"Test\",\"t\":\"A\",\"j\":0,\"tr\":0,\"lh\":24,\"ls\":0,"
        + "\"fc\":[1,0,0]},\"t\":0}]},\"p\":{},\"m\":{\"g\":1,\"a\":{\"a\":0,\"k\":[0,0]}},\"a\":[]}}";
  }

  private static class AlphaRecordingCanvas extends Canvas {
    final List<Integer> alphas = new ArrayList<>();

    AlphaRecordingCanvas() {
      super(Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888));
    }

    @Override public void drawText(String text, int start, int end, float x, float y, Paint paint) {
      alphas.add(paint.getAlpha());
    }

    // Robolectric does not track clips, so report every clip as non-empty to keep layers drawing.
    @Override public boolean clipRect(RectF rect) {
      return true;
    }

    @Override public void drawPath(Path path, Paint paint) {
      alphas.add(paint.getAlpha());
    }
  }
}
