package com.airbnb.lottie.parser;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.graphics.Color;

import com.airbnb.lottie.BaseTest;
import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.model.content.GradientColor;
import com.airbnb.lottie.model.content.GradientFill;
import com.airbnb.lottie.model.content.GradientStroke;
import com.airbnb.lottie.parser.moshi.JsonReader;
import com.airbnb.lottie.value.Keyframe;

import org.junit.Test;

import java.io.IOException;
import java.util.List;

import okio.Buffer;

public class GradientParserTest extends BaseTest {

  private static final String STOPS_JSON =
      "[0,0,0,0, 0.5,0,0,0, 1,0,0,0, 0,0, 0.222,0.5, 0.444,1]";

  private static JsonReader readerOf(String json) throws IOException {
    JsonReader reader = JsonReader.of(new Buffer().writeUtf8(json));
    reader.beginObject();
    return reader;
  }

  private static LottieComposition createComposition() {
    LottieComposition composition = new LottieComposition();
    composition.init(null, 0f, 60f, 30f, null, null, null, null, 1f, null, null, null, 0, 0);
    return composition;
  }

  @Test
  public void gradientFillParsesOpacityStopsRegardlessOfKeyOrder() throws IOException {
    String pBeforeK = "{"
        + "\"ty\":\"gf\",\"t\":1,\"r\":1,"
        + "\"s\":{\"a\":0,\"k\":[0,30]},"
        + "\"e\":{\"a\":0,\"k\":[0,-84]},"
        + "\"o\":{\"a\":0,\"k\":100},"
        + "\"g\":{\"p\":3,\"k\":{\"a\":0,\"k\":" + STOPS_JSON + "}}"
        + "}";
    String kBeforeP = "{"
        + "\"ty\":\"gf\",\"t\":1,\"r\":1,"
        + "\"s\":{\"a\":0,\"k\":[0,30]},"
        + "\"e\":{\"a\":0,\"k\":[0,-84]},"
        + "\"o\":{\"a\":0,\"k\":100},"
        + "\"g\":{\"k\":{\"a\":0,\"k\":" + STOPS_JSON + "},\"p\":3}"
        + "}";

    LottieComposition composition = createComposition();
    GradientFill expected = GradientFillParser.parse(readerOf(pBeforeK), composition);
    GradientFill actual = GradientFillParser.parse(readerOf(kBeforeP), composition);

    GradientColor expectedColor = expected.getGradientColor().getKeyframes().get(0).startValue;
    GradientColor actualColor = actual.getGradientColor().getKeyframes().get(0).startValue;
    assertNotNull(expectedColor);
    assertNotNull(actualColor);
    assertArrayEquals(expectedColor.getPositions(), actualColor.getPositions(), 0f);
    assertArrayEquals(expectedColor.getColors(), actualColor.getColors());
    assertEquals(0, Color.alpha(actualColor.getColors()[0]));
    assertEquals(255, Color.alpha(actualColor.getColors()[actualColor.getColors().length - 1]));
  }

  @Test
  public void gradientStrokeParsesOpacityStopsRegardlessOfKeyOrder() throws IOException {
    String pBeforeK = "{"
        + "\"ty\":\"gs\",\"t\":1,\"lc\":1,\"lj\":1,\"ml\":4,"
        + "\"w\":{\"a\":0,\"k\":2},"
        + "\"s\":{\"a\":0,\"k\":[0,30]},"
        + "\"e\":{\"a\":0,\"k\":[0,-84]},"
        + "\"o\":{\"a\":0,\"k\":100},"
        + "\"g\":{\"p\":3,\"k\":{\"a\":0,\"k\":" + STOPS_JSON + "}}"
        + "}";
    String kBeforeP = "{"
        + "\"ty\":\"gs\",\"t\":1,\"lc\":1,\"lj\":1,\"ml\":4,"
        + "\"w\":{\"a\":0,\"k\":2},"
        + "\"s\":{\"a\":0,\"k\":[0,30]},"
        + "\"e\":{\"a\":0,\"k\":[0,-84]},"
        + "\"o\":{\"a\":0,\"k\":100},"
        + "\"g\":{\"k\":{\"a\":0,\"k\":" + STOPS_JSON + "},\"p\":3}"
        + "}";

    LottieComposition composition = createComposition();
    GradientStroke expected = GradientStrokeParser.parse(readerOf(pBeforeK), composition);
    GradientStroke actual = GradientStrokeParser.parse(readerOf(kBeforeP), composition);

    GradientColor expectedColor = expected.getGradientColor().getKeyframes().get(0).startValue;
    GradientColor actualColor = actual.getGradientColor().getKeyframes().get(0).startValue;
    assertNotNull(expectedColor);
    assertNotNull(actualColor);
    assertArrayEquals(expectedColor.getPositions(), actualColor.getPositions(), 0f);
    assertArrayEquals(expectedColor.getColors(), actualColor.getColors());
    assertEquals(0, Color.alpha(actualColor.getColors()[0]));
    assertEquals(255, Color.alpha(actualColor.getColors()[actualColor.getColors().length - 1]));
  }

  @Test
  public void animatedGradientParsesRegardlessOfKeyOrder() throws IOException {
    String animatedK = "{\"a\":1,\"k\":["
        + "{\"t\":0,\"s\":[0,0,0,0, 1,1,1,1, 0,0, 1,1],"
        + "\"i\":{\"x\":[0.5],\"y\":[1]},\"o\":{\"x\":[0.5],\"y\":[0]}},"
        + "{\"t\":30,\"s\":[0,1,0,0, 1,0,0,1, 0,0.5, 1,1]}"
        + "]}";
    String pBeforeK = "{"
        + "\"ty\":\"gf\",\"t\":1,\"r\":1,"
        + "\"s\":{\"a\":0,\"k\":[0,0]},\"e\":{\"a\":0,\"k\":[10,10]},"
        + "\"g\":{\"p\":2,\"k\":" + animatedK + "}"
        + "}";
    String kBeforeP = "{"
        + "\"ty\":\"gf\",\"t\":1,\"r\":1,"
        + "\"s\":{\"a\":0,\"k\":[0,0]},\"e\":{\"a\":0,\"k\":[10,10]},"
        + "\"g\":{\"k\":" + animatedK + ",\"p\":2}"
        + "}";

    LottieComposition composition = createComposition();
    List<Keyframe<GradientColor>> expected =
        GradientFillParser.parse(readerOf(pBeforeK), composition).getGradientColor().getKeyframes();
    List<Keyframe<GradientColor>> actual =
        GradientFillParser.parse(readerOf(kBeforeP), composition).getGradientColor().getKeyframes();

    assertEquals(expected.size(), actual.size());
    for (int i = 0; i < expected.size(); i++) {
      Keyframe<GradientColor> expKf = expected.get(i);
      Keyframe<GradientColor> actKf = actual.get(i);
      assertEquals(expKf.startFrame, actKf.startFrame, 0f);
      assertEquals(expKf.endFrame, actKf.endFrame);
      assertEquals(expKf.isStatic(), actKf.isStatic());
      assertEquals(expKf.startValue, actKf.startValue);
      assertEquals(expKf.endValue, actKf.endValue);
    }
  }

  @Test
  public void gradientFillWithoutColorPointCountFallsBackToArrayLength() throws IOException {
    String noP = "{"
        + "\"ty\":\"gf\",\"t\":1,\"r\":1,"
        + "\"s\":{\"a\":0,\"k\":[0,0]},\"e\":{\"a\":0,\"k\":[10,10]},"
        + "\"g\":{\"k\":{\"a\":0,\"k\":[0,1,0,0, 1,0,0,1]}}"
        + "}";

    GradientFill fill = GradientFillParser.parse(readerOf(noP), createComposition());
    GradientColor color = fill.getGradientColor().getKeyframes().get(0).startValue;
    assertNotNull(color);
    assertArrayEquals(new float[]{0f, 1f}, color.getPositions(), 0f);
    assertArrayEquals(new int[]{Color.argb(255, 255, 0, 0), Color.argb(255, 0, 0, 255)}, color.getColors());
  }
}
