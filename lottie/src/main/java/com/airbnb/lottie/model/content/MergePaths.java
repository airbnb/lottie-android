package com.airbnb.lottie.model.content;

import androidx.annotation.Nullable;

import com.airbnb.lottie.L;
import com.airbnb.lottie.LottieDrawable;
import com.airbnb.lottie.animation.content.Content;
import com.airbnb.lottie.animation.content.MergePathsContent;
import com.airbnb.lottie.model.layer.BaseLayer;


public class MergePaths implements ContentModel {

  public enum MergePathsMode {
    Merge,
    Add,
    Subtract,
    Intersect,
    ExcludeIntersections;

    public static MergePathsMode forId(int id) {
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

  public MergePaths(String name, MergePathsMode mode) {
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
      L.warn("Animation contains merge paths but they are disabled.");
      return null;
    }
    return new MergePathsContent(this);
  }

  @Override
  public String toString() {
    return "MergePaths{" + "mode=" +  mode + '}';
  }
}
