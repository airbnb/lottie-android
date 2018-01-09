package com.airbnb.lottie.model;

import android.graphics.PointF;
import android.util.JsonReader;
import android.util.JsonToken;

import com.airbnb.lottie.model.animatable.AnimatableValue;
import com.airbnb.lottie.utils.JsonUtils;

import java.io.IOException;

public class PointFFactory implements AnimatableValue.Factory<PointF> {
  public static final PointFFactory INSTANCE = new PointFFactory();

  private PointFFactory() {
  }

  @Override public PointF valueFromObject(JsonReader reader, float scale) throws IOException {
    JsonToken token = reader.peek();
    if (token == JsonToken.BEGIN_ARRAY) {
      return JsonUtils.jsonToPoint(reader, scale);
    } else if (token == JsonToken.BEGIN_OBJECT) {
      return JsonUtils.jsonToPoint(reader, scale);
    } else if (token == JsonToken.NUMBER) {
      // This is the case where the static value for a property is an array of numbers.
      // We begin the array to see if we have an array of keyframes but it's just an array
      // of static numbers instead.
      PointF point = new PointF((float) reader.nextDouble() * scale, (float) reader.nextDouble() * scale);
      while (reader.hasNext()) {
        reader.skipValue();
      }
      return point;
    } else {
      throw new IllegalArgumentException("Cannot convert json to point. Next token is " + token);
    }
  }
}
