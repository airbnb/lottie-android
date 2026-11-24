package com.airbnb.lottie.parser;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import android.graphics.Color;

import com.airbnb.lottie.BaseTest;
import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.LottieCompositionFactory;
import com.airbnb.lottie.LottieDrawable;
import com.airbnb.lottie.LottieResult;
import com.airbnb.lottie.model.content.GradientColor;
import com.airbnb.lottie.model.content.GradientFill;
import com.airbnb.lottie.model.content.GradientStroke;
import com.airbnb.lottie.model.content.ShapeGroup;
import com.airbnb.lottie.parser.moshi.JsonReader;
import com.airbnb.lottie.value.Keyframe;

import android.graphics.Bitmap;
import android.graphics.Canvas;

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

  @Test
  public void animatedGradientPreservesHoldAndSharedKeyframesWhenKPrecedesP() throws IOException {
    String animatedK = "{\"a\":1,\"k\":["
        + "{\"t\":0,\"h\":1,\"s\":[0,0,0,0, 1,1,1,1, 0,0, 1,1]},"
        + "{\"t\":15,\"s\":[0,1,0,0, 1,0,0,1, 0,0.5, 1,1],"
        + "\"i\":{\"x\":[0.5],\"y\":[1]},\"o\":{\"x\":[0.5],\"y\":[0]}},"
        + "{\"t\":30,\"s\":[0,0,1,0, 1,0,1,1, 0,1, 1,0]}"
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
    assertEquals(2, actual.size());
    // Hold keyframe keeps its startFrame/endFrame and shares startValue == endValue
    assertEquals(0f, actual.get(0).startFrame, 0f);
    assertEquals(Float.valueOf(15f), actual.get(0).endFrame);
    assertSame(actual.get(0).startValue, actual.get(0).endValue);
    // Second keyframe matches expected values exactly
    assertEquals(expected.get(1).startFrame, actual.get(1).startFrame, 0f);
    assertEquals(expected.get(1).endFrame, actual.get(1).endFrame);
    assertEquals(expected.get(1).startValue, actual.get(1).startValue);
    assertEquals(expected.get(1).endValue, actual.get(1).endValue);
  }

  @Test
  public void singleStopKeyframeDoesNotCorruptColorPointsForLaterKeyframes() throws IOException {
    // Keyframe 0 triggers the single-stop issue-1967 expansion (array.size() == 4 && array[0] == 1);
    // Keyframe 1 has 3 color stops. colorPoints must remain immutable across keyframes.
    String animatedK = "{\"a\":1,\"k\":["
        + "{\"t\":0,\"s\":[1, 1, 0, 0]},"
        + "{\"t\":30,\"s\":[0,1,0,0, 0.5,0,1,0, 1,0,0,1]}"
        + "]}";
    String kWithoutP = "{"
        + "\"ty\":\"gf\",\"t\":1,\"r\":1,"
        + "\"s\":{\"a\":0,\"k\":[0,0]},\"e\":{\"a\":0,\"k\":[10,10]},"
        + "\"g\":{\"k\":" + animatedK + "}"
        + "}";

    List<Keyframe<GradientColor>> keyframes =
        GradientFillParser.parse(readerOf(kWithoutP), createComposition()).getGradientColor().getKeyframes();
    assertEquals(1, keyframes.size());
    GradientColor start = keyframes.get(0).startValue;
    GradientColor end = keyframes.get(0).endValue;
    assertNotNull(start);
    assertNotNull(end);
    // Both stops get expanded to 3 unified positions by AnimatableGradientColorValue, and end keeps its 3rd stop (blue)
    assertEquals(Color.argb(255, 0, 0, 255), end.getColors()[end.getColors().length - 1]);
  }

  @Test
  public void fullCompositionLoadsAndDrawsRegardlessOfGradientKeyOrder() throws IOException {
    String shapeGroupTemplate = "{"
        + "\"ty\":\"gr\",\"nm\":\"Group\",\"it\":["
        + "{\"ty\":\"rc\",\"d\":1,\"s\":{\"a\":0,\"k\":[180,180]},\"p\":{\"a\":0,\"k\":[0,0]},\"r\":{\"a\":0,\"k\":0}},"
        + "{\"ty\":\"gf\",\"o\":{\"a\":0,\"k\":100},\"r\":1,\"t\":1,"
        + "\"s\":{\"a\":0,\"k\":[0,30]},\"e\":{\"a\":0,\"k\":[0,-84]},\"g\":%s},"
        + "{\"ty\":\"tr\",\"p\":{\"a\":0,\"k\":[0,0]},\"a\":{\"a\":0,\"k\":[0,0]},\"s\":{\"a\":0,\"k\":[100,100]},\"r\":{\"a\":0,\"k\":0},\"o\":{\"a\":0,\"k\":100}}"
        + "]}";
    String pBeforeK = "{\"p\":3,\"k\":{\"a\":0,\"k\":" + STOPS_JSON + "}}";
    String kBeforeP = "{\"k\":{\"a\":0,\"k\":" + STOPS_JSON + "},\"p\":3}";

    LottieComposition comp = createComposition();
    ShapeGroup refGroup = ShapeGroupParser.parse(readerOf(String.format(shapeGroupTemplate, pBeforeK)), comp);
    ShapeGroup fixedGroup = ShapeGroupParser.parse(readerOf(String.format(shapeGroupTemplate, kBeforeP)), comp);
    GradientColor refColor = ((GradientFill) refGroup.getItems().get(1)).getGradientColor().getKeyframes().get(0).startValue;
    GradientColor fixedColor = ((GradientFill) fixedGroup.getItems().get(1)).getGradientColor().getKeyframes().get(0).startValue;

    assertNotNull(refColor);
    assertNotNull(fixedColor);
    assertArrayEquals(refColor.getPositions(), fixedColor.getPositions(), 0f);
    assertArrayEquals(refColor.getColors(), fixedColor.getColors());
    assertEquals(0, Color.alpha(fixedColor.getColors()[0]));
    assertEquals(255, Color.alpha(fixedColor.getColors()[fixedColor.getColors().length - 1]));

    // Also exercise full LottieCompositionFactory -> LottieDrawable -> Canvas draw pipeline
    String fullJson = "{"
        + "\"v\":\"5.9.0\",\"fr\":30,\"ip\":0,\"op\":30,\"w\":200,\"h\":200,\"nm\":\"issue2739\",\"ddd\":0,\"assets\":[],"
        + "\"layers\":[{\"ddd\":0,\"ind\":1,\"ty\":4,\"nm\":\"grad\",\"sr\":1,"
        + "\"ks\":{\"o\":{\"a\":0,\"k\":100},\"r\":{\"a\":0,\"k\":0},\"p\":{\"a\":0,\"k\":[100,100,0]},"
        + "\"a\":{\"a\":0,\"k\":[0,0,0]},\"s\":{\"a\":0,\"k\":[100,100,100]}},"
        + "\"ao\":0,\"ip\":0,\"op\":30,\"st\":0,\"bm\":0,"
        + "\"shapes\":[" + String.format(shapeGroupTemplate, kBeforeP) + "]}]}";
    LottieResult<LottieComposition> result = LottieCompositionFactory.fromJsonStringSync(fullJson, null);
    assertNotNull(result.getValue());
    assertEquals(0, result.getValue().getWarnings().size());
    LottieDrawable drawable = new LottieDrawable();
    drawable.setComposition(result.getValue());
    drawable.setBounds(0, 0, 200, 200);
    drawable.draw(new Canvas(Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888)));
  }
}
