package com.airbnb.lottie.parser;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import android.graphics.Color;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.model.content.GradientColor;
import com.airbnb.lottie.model.content.GradientFill;
import com.airbnb.lottie.parser.moshi.JsonReader;
import com.airbnb.lottie.value.Keyframe;

import org.junit.Test;

import java.io.IOException;

import okio.Buffer;

public class GradientFillParserTest {
  private static final String COLOR_STOPS = "[0,0,0,0,0.5,0,0,0,1,0,0,0,0,0,0.222,0.5,0.444,1]";

  @Test public void testGradientPointCountCanFollowColorStops() throws IOException {
    GradientColor pointsFirst = parseColor("{\"p\":3,\"k\":{\"a\":0,\"k\":" + COLOR_STOPS + "}}");
    GradientColor colorFirst = parseColor("{\"k\":{\"a\":0,\"k\":" + COLOR_STOPS + "},\"p\":3}");

    assertArrayEquals(pointsFirst.getPositions(), colorFirst.getPositions(), 0f);
    assertArrayEquals(pointsFirst.getColors(), colorFirst.getColors());
    assertEquals(0, Color.alpha(colorFirst.getColors()[0]));
    assertEquals(255, Color.alpha(colorFirst.getColors()[colorFirst.getColors().length - 1]));
  }

  private static GradientColor parseColor(String gradient) throws IOException {
    JsonReader reader = JsonReader.of(new Buffer().writeUtf8("{\"g\":" + gradient + "}"));
    reader.beginObject();
    GradientFill fill = GradientFillParser.parse(reader, new LottieComposition());
    reader.endObject();
    Keyframe<GradientColor> keyframe = fill.getGradientColor().getKeyframes().get(0);
    return keyframe.startValue;
  }
}
