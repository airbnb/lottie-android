package com.airbnb.lottie.parser;

import android.util.JsonReader;
import android.util.JsonToken;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.animation.Keyframe;
import com.airbnb.lottie.model.animatable.AnimatableValue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class KeyframesParser {

  private KeyframesParser() {}

  public static <T> List<Keyframe<T>> parse(JsonReader reader,
      LottieComposition composition, float scale, AnimatableValue.Factory<T> valueFactory)
      throws IOException {
    List<Keyframe<T>> keyframes = new ArrayList<>();

    if (reader.peek() == JsonToken.STRING) {
      composition.addWarning("Lottie doesn't support expressions.");
      return keyframes;
    }

    reader.beginObject();
    while (reader.hasNext()) {
      switch (reader.nextName()) {
        case "k":
          if (reader.peek() == JsonToken.BEGIN_ARRAY) {
            reader.beginArray();

            if (reader.peek() == JsonToken.NUMBER) {
              // For properties in which the static value is an array of numbers.
              keyframes.add(
                  KeyframeParser.parse(reader, composition, scale, valueFactory, false));
            } else {
              while (reader.hasNext()) {
                keyframes.add(KeyframeParser.parse(reader, composition, scale, valueFactory, true));
              }
            }
            reader.endArray();
          } else {
            keyframes.add(KeyframeParser.parse(reader, composition, scale, valueFactory, false));
          }
          break;
        default:
          reader.skipValue();
      }
    }
    reader.endObject();

    setEndFrames(keyframes);
    return keyframes;
  }

  /**
   * The json doesn't include end frames. The data can be taken from the start frame of the next
   * keyframe though.
   */
  public static void setEndFrames(List<? extends Keyframe<?>> keyframes) {
    int size = keyframes.size();
    for (int i = 0; i < size - 1; i++) {
      // In the json, the keyframes only contain their starting frame.
      keyframes.get(i).endFrame = keyframes.get(i + 1).startFrame;
    }
    Keyframe<?> lastKeyframe = keyframes.get(size - 1);
    if (lastKeyframe.startValue == null) {
      // The only purpose the last keyframe has is to provide the end frame of the previous
      // keyframe.
      //noinspection SuspiciousMethodCalls
      keyframes.remove(lastKeyframe);
    }
  }
}
