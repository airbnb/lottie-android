package com.airbnb.lottie.model.content;

import android.support.annotation.Nullable;
import android.util.JsonReader;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.LottieDrawable;
import com.airbnb.lottie.animation.content.Content;
import com.airbnb.lottie.animation.content.GradientStrokeContent;
import com.airbnb.lottie.model.animatable.AnimatableFloatValue;
import com.airbnb.lottie.model.animatable.AnimatableGradientColorValue;
import com.airbnb.lottie.model.animatable.AnimatableIntegerValue;
import com.airbnb.lottie.model.animatable.AnimatablePointValue;
import com.airbnb.lottie.model.layer.BaseLayer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GradientStroke implements ContentModel {

  private final String name;
  private final GradientType gradientType;
  private final AnimatableGradientColorValue gradientColor;
  private final AnimatableIntegerValue opacity;
  private final AnimatablePointValue startPoint;
  private final AnimatablePointValue endPoint;
  private final AnimatableFloatValue width;
  private final ShapeStroke.LineCapType capType;
  private final ShapeStroke.LineJoinType joinType;
  private final List<AnimatableFloatValue> lineDashPattern;
  @Nullable private final AnimatableFloatValue dashOffset;

  private GradientStroke(String name, GradientType gradientType,
      AnimatableGradientColorValue gradientColor,
      AnimatableIntegerValue opacity, AnimatablePointValue startPoint,
      AnimatablePointValue endPoint, AnimatableFloatValue width, ShapeStroke.LineCapType capType,
      ShapeStroke.LineJoinType joinType, List<AnimatableFloatValue> lineDashPattern,
      @Nullable AnimatableFloatValue dashOffset) {
    this.name = name;
    this.gradientType = gradientType;
    this.gradientColor = gradientColor;
    this.opacity = opacity;
    this.startPoint = startPoint;
    this.endPoint = endPoint;
    this.width = width;
    this.capType = capType;
    this.joinType = joinType;
    this.lineDashPattern = lineDashPattern;
    this.dashOffset = dashOffset;
  }

  public String getName() {
    return name;
  }

  public GradientType getGradientType() {
    return gradientType;
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

  public AnimatableFloatValue getWidth() {
    return width;
  }

  public ShapeStroke.LineCapType getCapType() {
    return capType;
  }

  public ShapeStroke.LineJoinType getJoinType() {
    return joinType;
  }

  public List<AnimatableFloatValue> getLineDashPattern() {
    return lineDashPattern;
  }

  @Nullable public AnimatableFloatValue getDashOffset() {
    return dashOffset;
  }

  @Override public Content toContent(LottieDrawable drawable, BaseLayer layer) {
    return new GradientStrokeContent(drawable, layer, this);
  }

  static class Factory {
    private Factory() {
    }

    static GradientStroke newInstance(
        JsonReader reader, LottieComposition composition) throws IOException {
      String name = null;
      AnimatableGradientColorValue color = null;
      AnimatableIntegerValue opacity = null;
      GradientType gradientType = null;
      AnimatablePointValue startPoint = null;
      AnimatablePointValue endPoint = null;
      AnimatableFloatValue width = null;
      ShapeStroke.LineCapType capType = null;
      ShapeStroke.LineJoinType joinType = null;
      AnimatableFloatValue offset = null;


      List<AnimatableFloatValue> lineDashPattern = new ArrayList<>();

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
          case "w":
            width = AnimatableFloatValue.Factory.newInstance(reader, composition);
            break;
          case "lc":
            capType = ShapeStroke.LineCapType.values()[reader.nextInt() - 1];
            break;
          case "lj":
            joinType = ShapeStroke.LineJoinType.values()[reader.nextInt() - 1];
            break;
          case "d":
            reader.beginArray();
            while (reader.hasNext()) {
              String n = null;
              AnimatableFloatValue val = null;
              reader.beginObject();
              while (reader.hasNext()) {
                switch (reader.nextName()) {
                  case "n":
                    n = reader.nextString();
                    break;
                  case "v":
                    val =  AnimatableFloatValue.Factory.newInstance(reader, composition);
                    break;
                  default:
                    reader.skipValue();
                }
              }
              reader.endObject();

              if (n.equals("o")) {
                offset = val;
              } else if (n.equals("d") || n.equals("g")) {
                lineDashPattern.add(val);
              }
            }
            reader.endArray();
            if (lineDashPattern.size() == 1) {
              // If there is only 1 value then it is assumed to be equal parts on and off.
              lineDashPattern.add(lineDashPattern.get(0));
            }
            break;
          default:
            reader.skipValue();
        }
      }

      return new GradientStroke(
          name, gradientType, color, opacity, startPoint, endPoint, width, capType, joinType,
          lineDashPattern, offset);
    }
  }
}
