package com.airbnb.lottie.model.content;

import android.util.JsonReader;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.LottieDrawable;
import com.airbnb.lottie.animation.content.Content;
import com.airbnb.lottie.animation.content.ShapeContent;
import com.airbnb.lottie.model.animatable.AnimatableShapeValue;
import com.airbnb.lottie.model.layer.BaseLayer;

import java.io.IOException;

public class ShapePath implements ContentModel {
  private final String name;
  private final int index;
  private final AnimatableShapeValue shapePath;

  private ShapePath(String name, int index, AnimatableShapeValue shapePath) {
    this.name = name;
    this.index = index;
    this.shapePath = shapePath;
  }

  public String getName() {
    return name;
  }

  public AnimatableShapeValue getShapePath() {
    return shapePath;
  }

  @Override public Content toContent(LottieDrawable drawable, BaseLayer layer) {
    return new ShapeContent(drawable, layer, this);
  }

  @Override public String toString() {
    return "ShapePath{" + "name=" + name +
        ", index=" + index +
        '}';
  }

  static class Factory {
    private Factory() {
    }

    static ShapePath newInstance(
        JsonReader reader, LottieComposition composition) throws IOException {
      String name = null;
      int ind = 0;
      AnimatableShapeValue shape = null;

      while (reader.hasNext()) {
        switch (reader.nextName()) {
          case "nm":
            name = reader.nextString();
            break;
          case "ind":
            ind = reader.nextInt();
            break;
          case "ks":
            shape = AnimatableShapeValue.Factory.newInstance(reader, composition);
            break;
          default:
            reader.skipValue();
        }
      }

      return new ShapePath(name, ind, shape);
    }
  }
}
