package com.airbnb.lottie.model.animatable;

import android.support.annotation.Nullable;
import android.util.JsonReader;

import com.airbnb.lottie.LottieComposition;

import java.io.IOException;

public class AnimatableTextProperties {

  @Nullable public final AnimatableColorValue color;
  @Nullable public final AnimatableColorValue stroke;
  @Nullable public final AnimatableFloatValue strokeWidth;
  @Nullable public final AnimatableFloatValue tracking;

  AnimatableTextProperties(@Nullable AnimatableColorValue color,
      @Nullable AnimatableColorValue stroke, @Nullable AnimatableFloatValue strokeWidth,
      @Nullable AnimatableFloatValue tracking) {
    this.color = color;
    this.stroke = stroke;
    this.strokeWidth = strokeWidth;
    this.tracking = tracking;
  }

  public static final class Factory {

    private Factory() {
    }

    public static AnimatableTextProperties newInstance(
        JsonReader reader, LottieComposition composition) throws IOException {
      AnimatableTextProperties anim = null;

      reader.beginObject();
      while (reader.hasNext()) {
        switch (reader.nextName()) {
          case "a":
          anim = parseAnimatableTextProperties(reader, composition);
          break;
        default:
          reader.skipValue();
        }
      }
      reader.endObject();
      if (anim == null) {
        return new AnimatableTextProperties(null, null, null, null);
      }
      return anim;
    }

    private static AnimatableTextProperties parseAnimatableTextProperties(
        JsonReader reader, LottieComposition composition) throws IOException {
      AnimatableColorValue color = null;
      AnimatableColorValue stroke = null;
      AnimatableFloatValue strokeWidth = null;
      AnimatableFloatValue tracking = null;

      reader.beginObject();
      while (reader.hasNext()) {
        switch (reader.nextName()) {
          case "fc":
            color = AnimatableColorValue.Factory.newInstance(reader, composition);
            break;
          case "sc":
            stroke = AnimatableColorValue.Factory.newInstance(reader, composition);
            break;
          case "sw":
            strokeWidth = AnimatableFloatValue.Factory.newInstance(reader, composition);
            break;
          case "t":
            tracking = AnimatableFloatValue.Factory.newInstance(reader, composition);
            break;
          default:
            reader.skipValue();
        }
      }

      return new AnimatableTextProperties(color, stroke, strokeWidth, tracking);
    }
  }
}
