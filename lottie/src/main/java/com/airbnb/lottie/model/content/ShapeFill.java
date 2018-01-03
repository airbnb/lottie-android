package com.airbnb.lottie.model.content;

import android.graphics.Path;
import android.support.annotation.Nullable;
import android.util.JsonReader;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.LottieDrawable;
import com.airbnb.lottie.animation.content.Content;
import com.airbnb.lottie.animation.content.FillContent;
import com.airbnb.lottie.model.animatable.AnimatableColorValue;
import com.airbnb.lottie.model.animatable.AnimatableIntegerValue;
import com.airbnb.lottie.model.layer.BaseLayer;

import java.io.IOException;

public class ShapeFill implements ContentModel {
  private final boolean fillEnabled;
  private final Path.FillType fillType;
  private final String name;
  @Nullable private final AnimatableColorValue color;
  @Nullable private final AnimatableIntegerValue opacity;

  private ShapeFill(String name, boolean fillEnabled, Path.FillType fillType,
      @Nullable AnimatableColorValue color, @Nullable AnimatableIntegerValue opacity) {
    this.name = name;
    this.fillEnabled = fillEnabled;
    this.fillType = fillType;
    this.color = color;
    this.opacity = opacity;
  }

  static class Factory {
    private Factory() {
    }

    static ShapeFill newInstance(
        JsonReader reader, LottieComposition composition) throws IOException {
      AnimatableColorValue color = null;
      boolean fillEnabled = false;
      AnimatableIntegerValue opacity = null;
      String name = null;
      int fillTypeInt = 1;

      // reader.beginObject();
      while (reader.hasNext()) {
        switch (reader.nextName()) {
          case "nm":
            name = reader.nextString();
            break;
          case "c":
            color = AnimatableColorValue.Factory.newInstance(reader, composition);
            break;
          case "o":
            opacity = AnimatableIntegerValue.Factory.newInstance(reader, composition);
            break;
          case "fillEnabled":
            fillEnabled = reader.nextBoolean();
            break;
          case "r":
            fillTypeInt = reader.nextInt();
            break;
          default:
            reader.skipValue();
        }
      }
      // reader.endObject();

      Path.FillType fillType = fillTypeInt == 1 ? Path.FillType.WINDING : Path.FillType.EVEN_ODD;
      return new ShapeFill(name, fillEnabled, fillType, color, opacity);
    }
  }

  public String getName() {
    return name;
  }

  @Nullable public AnimatableColorValue getColor() {
    return color;
  }

  @Nullable public AnimatableIntegerValue getOpacity() {
    return opacity;
  }

  public Path.FillType getFillType() {
    return fillType;
  }

  @Override public Content toContent(LottieDrawable drawable, BaseLayer layer) {
    return new FillContent(drawable, layer, this);
  }

  @Override
  public String toString() {
    return "ShapeFill{" + "color=" +
        ", fillEnabled=" + fillEnabled +
        '}';
  }
}
