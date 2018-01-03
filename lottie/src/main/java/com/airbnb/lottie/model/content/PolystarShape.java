package com.airbnb.lottie.model.content;

import android.graphics.PointF;
import android.util.JsonReader;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.LottieDrawable;
import com.airbnb.lottie.animation.content.Content;
import com.airbnb.lottie.animation.content.PolystarContent;
import com.airbnb.lottie.model.animatable.AnimatableFloatValue;
import com.airbnb.lottie.model.animatable.AnimatablePathValue;
import com.airbnb.lottie.model.animatable.AnimatableValue;
import com.airbnb.lottie.model.layer.BaseLayer;

import java.io.IOException;

public class PolystarShape implements ContentModel {
  public enum Type {
    Star(1),
    Polygon(2);

    private final int value;

    Type(int value) {
      this.value = value;
    }

    static Type forValue(int value) {
      for (Type type : Type.values()) {
        if (type.value == value) {
          return type;
        }
      }
      return null;
    }
  }

  private final String name;
  private final Type type;
  private final AnimatableFloatValue points;
  private final AnimatableValue<PointF, PointF> position;
  private final AnimatableFloatValue rotation;
  private final AnimatableFloatValue innerRadius;
  private final AnimatableFloatValue outerRadius;
  private final AnimatableFloatValue innerRoundedness;
  private final AnimatableFloatValue outerRoundedness;

  private PolystarShape(String name, Type type, AnimatableFloatValue points,
      AnimatableValue<PointF, PointF> position,
      AnimatableFloatValue rotation, AnimatableFloatValue innerRadius,
      AnimatableFloatValue outerRadius, AnimatableFloatValue innerRoundedness,
      AnimatableFloatValue outerRoundedness) {
    this.name = name;
    this.type = type;
    this.points = points;
    this.position = position;
    this.rotation = rotation;
    this.innerRadius = innerRadius;
    this.outerRadius = outerRadius;
    this.innerRoundedness = innerRoundedness;
    this.outerRoundedness = outerRoundedness;
  }

  public String getName() {
    return name;
  }

  public Type getType() {
    return type;
  }

  public AnimatableFloatValue getPoints() {
    return points;
  }

  public AnimatableValue<PointF, PointF> getPosition() {
    return position;
  }

  public AnimatableFloatValue getRotation() {
    return rotation;
  }

  public AnimatableFloatValue getInnerRadius() {
    return innerRadius;
  }

  public AnimatableFloatValue getOuterRadius() {
    return outerRadius;
  }

  public AnimatableFloatValue getInnerRoundedness() {
    return innerRoundedness;
  }

  public AnimatableFloatValue getOuterRoundedness() {
    return outerRoundedness;
  }

  @Override public Content toContent(LottieDrawable drawable, BaseLayer layer) {
    return new PolystarContent(drawable, layer, this);
  }

  static class Factory {
    private Factory() {
    }

    static PolystarShape newInstance(
        JsonReader reader, LottieComposition composition) throws IOException {
      String name = null;
      Type type = null;
      AnimatableFloatValue points = null;
      AnimatableValue<PointF, PointF> position = null;
      AnimatableFloatValue rotation = null;
      AnimatableFloatValue outerRadius = null;
      AnimatableFloatValue outerRoundedness = null;
      AnimatableFloatValue innerRadius = null;
      AnimatableFloatValue innerRoundedness = null;

      // reader.beginObject();
      while (reader.hasNext()) {
        switch (reader.nextName()) {
          case "nm":
            name = reader.nextString();
            break;
          case "sy":
            type = Type.forValue(reader.nextInt());
            break;
          case "pt":
            points = AnimatableFloatValue.Factory.newInstance(reader, composition, false);
            break;
          case "p":
            position = AnimatablePathValue .createAnimatablePathOrSplitDimensionPath(reader, composition);
            break;
          case "r":
            rotation = AnimatableFloatValue.Factory.newInstance(reader, composition, false);
            break;
          case "or":
            outerRadius = AnimatableFloatValue.Factory.newInstance(reader, composition);
            break;
          case "os":
            outerRoundedness = AnimatableFloatValue.Factory.newInstance(reader, composition, false);
            break;
          case "ir":
            innerRadius = AnimatableFloatValue.Factory.newInstance(reader, composition);
            break;
          case "is":
            innerRoundedness = AnimatableFloatValue.Factory.newInstance(reader, composition, false);
            break;
          default:
            reader.skipValue();
        }
      }
      // reader.endObject();

      return new PolystarShape(
          name, type, points, position, rotation, innerRadius, outerRadius, innerRoundedness, outerRoundedness);
    }
  }

}
