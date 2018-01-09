package com.airbnb.lottie.model.content;

import android.graphics.Path;
import android.support.annotation.Nullable;
import android.util.JsonReader;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.LottieDrawable;
import com.airbnb.lottie.animation.content.Content;
import com.airbnb.lottie.animation.content.GradientFillContent;
import com.airbnb.lottie.model.animatable.AnimatableFloatValue;
import com.airbnb.lottie.model.animatable.AnimatableGradientColorValue;
import com.airbnb.lottie.model.animatable.AnimatableIntegerValue;
import com.airbnb.lottie.model.animatable.AnimatablePointValue;
import com.airbnb.lottie.model.layer.BaseLayer;

import java.io.IOException;

public class GradientFill implements ContentModel {

  private final GradientType gradientType;
  private final Path.FillType fillType;
  private final AnimatableGradientColorValue gradientColor;
  private final AnimatableIntegerValue opacity;
  private final AnimatablePointValue startPoint;
  private final AnimatablePointValue endPoint;
  private final String name;
  @Nullable private final AnimatableFloatValue highlightLength;
  @Nullable private final AnimatableFloatValue highlightAngle;

  private GradientFill(String name, GradientType gradientType, Path.FillType fillType,
      AnimatableGradientColorValue gradientColor,
      AnimatableIntegerValue opacity, AnimatablePointValue startPoint,
      AnimatablePointValue endPoint, AnimatableFloatValue highlightLength,
      AnimatableFloatValue highlightAngle) {
    this.gradientType = gradientType;
    this.fillType = fillType;
    this.gradientColor = gradientColor;
    this.opacity = opacity;
    this.startPoint = startPoint;
    this.endPoint = endPoint;
    this.name = name;
    this.highlightLength = highlightLength;
    this.highlightAngle = highlightAngle;
  }

  public String getName() {
    return name;
  }

  public GradientType getGradientType() {
    return gradientType;
  }

  public Path.FillType getFillType() {
    return fillType;
  }

  public AnimatableGradientColorValue getGradientColor() {
    return gradientColor;
  }

  public AnimatableIntegerValue getOpacity() {
    return opacity;
  }

  public AnimatablePointValue getStartPoint() {
    return startPoint;
  }

  public AnimatablePointValue getEndPoint() {
    return endPoint;
  }

  @Nullable AnimatableFloatValue getHighlightLength() {
    return highlightLength;
  }

  @Nullable AnimatableFloatValue getHighlightAngle() {
    return highlightAngle;
  }

  @Override public Content toContent(LottieDrawable drawable, BaseLayer layer) {
    return new GradientFillContent(drawable, layer, this);
  }

  static class Factory {
    private Factory() {
    }

    static GradientFill newInstance(
        JsonReader reader, LottieComposition composition) throws IOException {
      String name = null;
      AnimatableGradientColorValue color = null;
      AnimatableIntegerValue opacity = null;
      GradientType gradientType = null;
      AnimatablePointValue startPoint = null;
      AnimatablePointValue endPoint = null;
      Path.FillType fillType = null;

      while (reader.hasNext()) {
        switch (reader.nextName()) {
          case "nm":
            name = reader.nextString();
            break;
          case "g":
            int points = -1;
            reader.beginObject();
            while (reader.hasNext()) {
              switch (reader.nextName()) {
                case "p":
                  points = reader.nextInt();
                  break;
                case "k":
                  color = AnimatableGradientColorValue.Factory
                      .newInstance(reader, composition, points);
                  break;
                default:
                  reader.skipValue();
              }
            }
            reader.endObject();
            break;
          case "o":
            opacity = AnimatableIntegerValue.Factory.newInstance(reader, composition);
            break;
          case "t":
            gradientType = reader.nextInt() == 1 ? GradientType.Linear : GradientType.Radial;
            break;
          case "s":
            startPoint = AnimatablePointValue.Factory.newInstance(reader, composition);
            break;
          case "e":
            endPoint = AnimatablePointValue.Factory.newInstance(reader, composition);
            break;
          case "r":
            fillType = reader.nextInt() == 1 ? Path.FillType.WINDING : Path.FillType.EVEN_ODD;
            break;
          default:
            reader.skipValue();
        }
      }

      return new GradientFill(name, gradientType, fillType, color, opacity, startPoint, endPoint,
          null, null);
    }
  }
}
