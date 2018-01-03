package com.airbnb.lottie.model.content;

import android.graphics.PointF;
import android.util.JsonReader;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.LottieDrawable;
import com.airbnb.lottie.animation.content.Content;
import com.airbnb.lottie.animation.content.EllipseContent;
import com.airbnb.lottie.model.animatable.AnimatablePathValue;
import com.airbnb.lottie.model.animatable.AnimatablePointValue;
import com.airbnb.lottie.model.animatable.AnimatableValue;
import com.airbnb.lottie.model.layer.BaseLayer;

import java.io.IOException;

public class CircleShape implements ContentModel {
  private final String name;
  private final AnimatableValue<PointF, PointF> position;
  private final AnimatablePointValue size;
  private final boolean isReversed;

  private CircleShape(String name, AnimatableValue<PointF, PointF> position,
      AnimatablePointValue size, boolean isReversed) {
    this.name = name;
    this.position = position;
    this.size = size;
    this.isReversed = isReversed;
  }

  @Override public Content toContent(LottieDrawable drawable, BaseLayer layer) {
    return new EllipseContent(drawable, layer, this);
  }

  static class Factory {
    private Factory() {
    }

    static CircleShape newInstance(
        JsonReader reader, LottieComposition composition) throws IOException {
      String name = null;
      AnimatableValue<PointF, PointF> position = null;
      AnimatablePointValue size = null;
      boolean reversed = false;

      // reader.beginObject();
      while (reader.hasNext()) {
        switch (reader.nextName()) {
          case "nm":
            name = reader.nextString();
            break;
          case "p":
            position = AnimatablePathValue
                .createAnimatablePathOrSplitDimensionPath(reader, composition);
            break;
          case "s":
            size = AnimatablePointValue.Factory.newInstance(reader, composition);
            break;
          case "d":
            // "d" is 2 for normal and 3 for reversed.
            reversed = reader.nextInt() == 3;
            break;
          default:
            reader.skipValue();
        }
      }
      // reader.endObject();

      return new CircleShape(name, position, size, reversed);
    }
  }

  public String getName() {
    return name;
  }

  public AnimatableValue<PointF, PointF> getPosition() {
    return position;
  }

  public AnimatablePointValue getSize() {
    return size;
  }

  public boolean isReversed() {
    return isReversed;
  }
}
