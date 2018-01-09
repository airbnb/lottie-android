package com.airbnb.lottie.model.content;

import android.support.annotation.Nullable;
import android.util.JsonReader;
import android.util.Log;

import com.airbnb.lottie.L;
import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.LottieDrawable;
import com.airbnb.lottie.animation.content.Content;
import com.airbnb.lottie.animation.content.ContentGroup;
import com.airbnb.lottie.model.animatable.AnimatableTransform;
import com.airbnb.lottie.model.layer.BaseLayer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ShapeGroup implements ContentModel {
  @Nullable
  public static ContentModel shapeItemWithJson(JsonReader reader, LottieComposition composition)
      throws IOException {
    String type = null;

    reader.beginObject();
    while (reader.hasNext()) {
      if (reader.nextName().equals("ty")) {
        type = reader.nextString();
        break;
      } else {
        reader.skipValue();
      }
    }

    ContentModel model = null;
    //noinspection ConstantConditions
    switch (type) {
      case "gr":
        model = ShapeGroup.Factory.newInstance(reader, composition);
        break;
      case "st":
        model = ShapeStroke.Factory.newInstance(reader, composition);
        break;
      case "gs":
        model = GradientStroke.Factory.newInstance(reader, composition);
        break;
      case "fl":
        model = ShapeFill.Factory.newInstance(reader, composition);
        break;
      case "gf":
        model = GradientFill.Factory.newInstance(reader, composition);
        break;
      case "tr":
        model = AnimatableTransform.Factory.newInstance(reader, composition);
        break;
      case "sh":
        model = ShapePath.Factory.newInstance(reader, composition);
        break;
      case "el":
        model = CircleShape.Factory.newInstance(reader, composition);
        break;
      case "rc":
        model = RectangleShape.Factory.newInstance(reader, composition);
        break;
      case "tm":
        model = ShapeTrimPath.Factory.newInstance(reader, composition);
        break;
      case "sr":
        model = PolystarShape.Factory.newInstance(reader, composition);
        break;
      case "mm":
        model = MergePaths.Factory.newInstance(reader);
        break;
      case "rp":
        model = Repeater.Factory.newInstance(reader, composition);
        break;
      default:
        Log.w(L.TAG, "Unknown shape type " + type);
    }

    while (reader.hasNext()) {
      reader.skipValue();
    }
    reader.endObject();

    return model;
  }

  private final String name;
  private final List<ContentModel> items;

  public ShapeGroup(String name, List<ContentModel> items) {
    this.name = name;
    this.items = items;
  }

  static class Factory {
    private Factory() {
    }

    private static ShapeGroup newInstance(
        JsonReader reader, LottieComposition composition) throws IOException {
      String name = null;
      List<ContentModel> items = new ArrayList<>();

      while (reader.hasNext()) {
        switch (reader.nextName()) {
          case "nm":
            name = reader.nextString();
            break;
          case "it":
            reader.beginArray();
            while (reader.hasNext()) {
              ContentModel newItem = shapeItemWithJson(reader, composition);
              if (newItem != null) {
                items.add(newItem);
              }
            }
            reader.endArray();
            break;
          default:
            reader.skipValue();
        }
      }

      return new ShapeGroup(name, items);
    }
  }

  public String getName() {
    return name;
  }

  public List<ContentModel> getItems() {
    return items;
  }

  @Override public Content toContent(LottieDrawable drawable, BaseLayer layer) {
    return new ContentGroup(drawable, layer, this);
  }

  @Override public String toString() {
    return "ShapeGroup{" + "name='" + name + "\' Shapes: " + Arrays.toString(items.toArray()) + '}';
  }
}
