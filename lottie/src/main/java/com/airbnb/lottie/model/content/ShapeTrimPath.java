package com.airbnb.lottie.model.content;

import android.util.JsonReader;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.LottieDrawable;
import com.airbnb.lottie.animation.content.Content;
import com.airbnb.lottie.animation.content.TrimPathContent;
import com.airbnb.lottie.model.animatable.AnimatableFloatValue;
import com.airbnb.lottie.model.layer.BaseLayer;

import java.io.IOException;

public class ShapeTrimPath implements ContentModel {

  public enum Type {
    Simultaneously,
    Individually;

    static Type forId(int id) {
      switch (id) {
        case 1:
          return Simultaneously;
        case 2:
          return Individually;
        default:
          throw new IllegalArgumentException("Unknown trim path type " + id);
      }
    }
  }

  private final String name;
  private final Type type;
  private final AnimatableFloatValue start;
  private final AnimatableFloatValue end;
  private final AnimatableFloatValue offset;

  private ShapeTrimPath(String name, Type type, AnimatableFloatValue start,
      AnimatableFloatValue end, AnimatableFloatValue offset) {
    this.name = name;
    this.type = type;
    this.start = start;
    this.end = end;
    this.offset = offset;
  }

  public String getName() {
    return name;
  }

  public Type getType() {
    return type;
  }

  public AnimatableFloatValue getEnd() {
    return end;
  }

  public AnimatableFloatValue getStart() {
    return start;
  }

  public AnimatableFloatValue getOffset() {
    return offset;
  }

  @Override public Content toContent(LottieDrawable drawable, BaseLayer layer) {
    return new TrimPathContent(layer, this);
  }

  @Override public String toString() {
    return "Trim Path: {start: " + start + ", end: " + end + ", offset: " + offset + "}";
  }

  static class Factory {
    private Factory() {
    }

    static ShapeTrimPath newInstance(
        JsonReader reader, LottieComposition composition) throws IOException {
      String name = null;
      Type type = null;
      AnimatableFloatValue start = null;
      AnimatableFloatValue end = null;
      AnimatableFloatValue offset = null;

      // reader.beginObject();
      while (reader.hasNext()) {
        switch (reader.nextName()) {
          case "s":
            start = AnimatableFloatValue.Factory.newInstance(reader, composition, false);
            break;
          case "e":
            end = AnimatableFloatValue.Factory.newInstance(reader, composition, false);
            break;
          case "o":
            offset = AnimatableFloatValue.Factory.newInstance(reader, composition, false);
            break;
          case "nm":
            name = reader.nextString();
            break;
          case "m":
            type = Type.forId(reader.nextInt());
            break;
          default:
            reader.skipValue();
        }
      }
      // reader.endObject();

      return new ShapeTrimPath(name, type, start, end, offset);
    }
  }
}
