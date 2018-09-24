package com.airbnb.lottie.model.content;

import android.graphics.Paint;
import androidx.annotation.Nullable;

import com.airbnb.lottie.LottieDrawable;
import com.airbnb.lottie.animation.content.Content;
import com.airbnb.lottie.animation.content.StrokeContent;
import com.airbnb.lottie.model.animatable.AnimatableColorValue;
import com.airbnb.lottie.model.animatable.AnimatableFloatValue;
import com.airbnb.lottie.model.animatable.AnimatableIntegerValue;
import com.airbnb.lottie.model.layer.BaseLayer;

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
  private final float miterLimit;

  public ShapeStroke(String name, @Nullable AnimatableFloatValue offset,
      List<AnimatableFloatValue> lineDashPattern, AnimatableColorValue color,
      AnimatableIntegerValue opacity, AnimatableFloatValue width, LineCapType capType,
      LineJoinType joinType, float miterLimit) {
    this.name = name;
    this.offset = offset;
    this.lineDashPattern = lineDashPattern;
    this.color = color;
    this.opacity = opacity;
    this.width = width;
    this.capType = capType;
    this.joinType = joinType;
    this.miterLimit = miterLimit;
  }

  @Override public Content toContent(LottieDrawable drawable, BaseLayer layer) {
    return new StrokeContent(drawable, layer, this);
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

  public float getMiterLimit() {
    return miterLimit;
  }
}
