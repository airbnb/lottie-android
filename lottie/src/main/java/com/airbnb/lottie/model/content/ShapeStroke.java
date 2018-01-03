package com.airbnb.lottie.model.content;

import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.JsonReader;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.LottieDrawable;
import com.airbnb.lottie.animation.content.Content;
import com.airbnb.lottie.animation.content.StrokeContent;
import com.airbnb.lottie.model.animatable.AnimatableColorValue;
import com.airbnb.lottie.model.animatable.AnimatableFloatValue;
import com.airbnb.lottie.model.animatable.AnimatableIntegerValue;
import com.airbnb.lottie.model.layer.BaseLayer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ShapeStroke implements ContentModel {
  public enum LineCapType {
    Butt,
    Round,
    Unknown;

    public Paint.Cap toPaintCap() {
      switch (this) {
        case Butt:
          return Paint.Cap.BUTT;
        case Round:
          return Paint.Cap.ROUND;
        case Unknown:
        default:
          return Paint.Cap.SQUARE;
      }
    }
  }

  public enum LineJoinType {
    Miter,
    Round,
    Bevel;

    public Paint.Join toPaintJoin() {
      switch (this) {
        case Bevel:
          return Paint.Join.BEVEL;
        case Miter:
          return Paint.Join.MITER;
        case Round:
          return Paint.Join.ROUND;
      }
      return null;
    }
  }

  private final String name;
  @Nullable private final AnimatableFloatValue offset;
  private final List<AnimatableFloatValue> lineDashPattern;
  private final AnimatableColorValue color;
  private final AnimatableIntegerValue opacity;
  private final AnimatableFloatValue width;
  private final LineCapType capType;
  private final LineJoinType joinType;

  private ShapeStroke(String name, @Nullable AnimatableFloatValue offset,
      List<AnimatableFloatValue> lineDashPattern, AnimatableColorValue color,
      AnimatableIntegerValue opacity, AnimatableFloatValue width, LineCapType capType,
      LineJoinType joinType) {
    this.name = name;
    this.offset = offset;
    this.lineDashPattern = lineDashPattern;
    this.color = color;
    this.opacity = opacity;
    this.width = width;
    this.capType = capType;
    this.joinType = joinType;
  }

  @Override public Content toContent(LottieDrawable drawable, BaseLayer layer) {
    return new StrokeContent(drawable, layer, this);
  }

  static class Factory {
    private Factory() {
    }

    static ShapeStroke newInstance(
        JsonReader reader, LottieComposition composition) throws IOException {
      String name = null;
      AnimatableColorValue color = null;
      AnimatableFloatValue width = null;
      AnimatableIntegerValue opacity = null;
      LineCapType capType = null;
      LineJoinType joinType = null;
      AnimatableFloatValue offset = null;

      List<AnimatableFloatValue> lineDashPattern = new ArrayList<>();

      // reader.beginObject();
      while (reader.hasNext()) {
        switch (reader.nextName()) {
          case "nm":
            name = reader.nextString();
            break;
          case "c":
            color = AnimatableColorValue.Factory.newInstance(reader, composition);
            break;
          case "w":
            width = AnimatableFloatValue.Factory.newInstance(reader, composition);
            break;
          case "o":
            opacity = AnimatableIntegerValue.Factory.newInstance(reader, composition);
            break;
          case "lc":
            capType = LineCapType.values()[reader.nextInt() - 1];
            break;
          case "lj":
            joinType = LineJoinType.values()[reader.nextInt() - 1];
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
                    val = AnimatableFloatValue.Factory.newInstance(reader, composition);
                    break;
                  default:
                    reader.skipValue();
                }
              }
              reader.endObject();

              switch (n) {
                case "o":
                  offset = val;
                  break;
                case "d":
                case "g":
                  lineDashPattern.add(val);
                  break;
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
      // reader.endObject();

      return new ShapeStroke(
          name, offset, lineDashPattern, color, opacity, width, capType, joinType);
    }
  }

  public String getName() {
    return name;
  }

  public AnimatableColorValue getColor() {
    return color;
  }

  public AnimatableIntegerValue getOpacity() {
    return opacity;
  }

  public AnimatableFloatValue getWidth() {
    return width;
  }

  public List<AnimatableFloatValue> getLineDashPattern() {
    return lineDashPattern;
  }

  public AnimatableFloatValue getDashOffset() {
    return offset;
  }

  public LineCapType getCapType() {
    return capType;
  }

  public LineJoinType getJoinType() {
    return joinType;
  }
}
