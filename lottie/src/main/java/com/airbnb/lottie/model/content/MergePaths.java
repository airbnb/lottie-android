package com.airbnb.lottie.model.content;

import android.support.annotation.Nullable;
import android.util.JsonReader;
import android.util.Log;

import com.airbnb.lottie.L;
import com.airbnb.lottie.LottieDrawable;
import com.airbnb.lottie.animation.content.Content;
import com.airbnb.lottie.animation.content.MergePathsContent;
import com.airbnb.lottie.model.layer.BaseLayer;

import java.io.IOException;


public class MergePaths implements ContentModel {

  public enum MergePathsMode {
    Merge,
    Add,
    Subtract,
    Intersect,
    ExcludeIntersections;

    private static MergePathsMode forId(int id) {
      switch (id) {
        case 1:
          return Merge;
        case 2:
          return Add;
        case 3:
          return Subtract;
        case 4:
          return Intersect;
        case 5:
          return ExcludeIntersections;
        default:
          return Merge;
      }
    }
  }

  private final String name;
  private final MergePathsMode mode;

  private MergePaths(String name, MergePathsMode mode) {
    this.name = name;
    this.mode = mode;
  }

  public String getName() {
    return name;
  }

  public MergePathsMode getMode() {
    return mode;
  }

  @Override @Nullable public Content toContent(LottieDrawable drawable, BaseLayer layer) {
    if (!drawable.enableMergePathsForKitKatAndAbove()) {
      Log.w(L.TAG, "Animation contains merge paths but they are disabled.");
      return null;
    }
    return new MergePathsContent(this);
  }

  @Override
  public String toString() {
    return "MergePaths{" + "mode=" +  mode + '}';
  }

  static class Factory {
    private Factory() {
    }

    static MergePaths newInstance(JsonReader reader) throws IOException {
      String name = null;
      MergePathsMode mode = null;

      // reader.beginObject();
      while (reader.hasNext()) {
        switch (reader.nextName()) {
          case "nm":
            name = reader.nextString();
            break;
          case "mm":
            mode =  MergePathsMode.forId(reader.nextInt());
            break;
          default:
            reader.skipValue();
        }
      }
      // reader.endObject();

      return new MergePaths(name, mode);
    }
  }
}
