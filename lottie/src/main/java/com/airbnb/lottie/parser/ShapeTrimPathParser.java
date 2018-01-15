package com.airbnb.lottie.parser;

import android.util.JsonReader;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.model.animatable.AnimatableFloatValue;
import com.airbnb.lottie.model.content.ShapeTrimPath;

import java.io.IOException;

public class ShapeTrimPathParser {

  private ShapeTrimPathParser() {}

  public static ShapeTrimPath parse(
      JsonReader reader, LottieComposition composition) throws IOException {
    String name = null;
    ShapeTrimPath.Type type = null;
    AnimatableFloatValue start = null;
    AnimatableFloatValue end = null;
    AnimatableFloatValue offset = null;

    while (reader.hasNext()) {
      switch (reader.nextName()) {
        case "s":
          start = AnimatableFloatValue.Factory.newInstance(reader, composition, false);
          break;
        case "e":
          end = AnimatableFloatValue.Factory.newInstance(reader, composition, false);
          break;
        case "o":
          offset = AnimatableFloatValue.Factory.newInstance(reader, composition, false);
          break;
        case "nm":
          name = reader.nextString();
          break;
        case "m":
          type = ShapeTrimPath.Type.forId(reader.nextInt());
          break;
        default:
          reader.skipValue();
      }
    }

    return new ShapeTrimPath(name, type, start, end, offset);
  }
}
