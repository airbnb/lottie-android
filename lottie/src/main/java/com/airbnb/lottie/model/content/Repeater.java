package com.airbnb.lottie.model.content;

import android.support.annotation.Nullable;
import android.util.JsonReader;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.LottieDrawable;
import com.airbnb.lottie.animation.content.Content;
import com.airbnb.lottie.animation.content.RepeaterContent;
import com.airbnb.lottie.model.animatable.AnimatableFloatValue;
import com.airbnb.lottie.model.animatable.AnimatableTransform;
import com.airbnb.lottie.model.layer.BaseLayer;

import java.io.IOException;

public class Repeater implements ContentModel {
  private final String name;
  private final AnimatableFloatValue copies;
  private final AnimatableFloatValue offset;
  private final AnimatableTransform transform;

  Repeater(String name, AnimatableFloatValue copies, AnimatableFloatValue offset,
      AnimatableTransform transform) {
    this.name = name;
    this.copies = copies;
    this.offset = offset;
    this.transform = transform;
  }

  public String getName() {
    return name;
  }

  public AnimatableFloatValue getCopies() {
    return copies;
  }

  public AnimatableFloatValue getOffset() {
    return offset;
  }

  public AnimatableTransform getTransform() {
    return transform;
  }

  @Nullable @Override public Content toContent(LottieDrawable drawable, BaseLayer layer) {
    return new RepeaterContent(drawable, layer, this);
  }

  final static class Factory {

    private Factory() {
    }

    static Repeater newInstance(
        JsonReader reader, LottieComposition composition) throws IOException {
      String name = null;
      AnimatableFloatValue copies = null;
      AnimatableFloatValue offset = null;
      AnimatableTransform transform = null;

      // reader.beginObject();
      while (reader.hasNext()) {
        switch (reader.nextName()) {
          case "nm":
            name = reader.nextString();
            break;
          case "c":
            copies = AnimatableFloatValue.Factory.newInstance(reader, composition, false);
            break;
          case "o":
            offset = AnimatableFloatValue.Factory.newInstance(reader, composition, false);
            break;
          case "tr":
            transform = AnimatableTransform.Factory.newInstance(reader, composition);
            break;
          default:
            reader.skipValue();
        }
      }
      // reader.endObject();

      return new Repeater(name, copies, offset, transform);
    }
  }
}
