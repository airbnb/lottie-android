package com.airbnb.lottie;

import android.support.annotation.Nullable;

import org.json.JSONObject;


public class MergePaths {

  enum MergePathsMode {
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

  static class Factory {
    private Factory() {
    }

    static MergePaths newInstance(JSONObject json) {
      return new MergePaths(json.optString("nm"), MergePathsMode.forId(json.optInt("mm", 1)));
    }
  }

  public String getName() {
    return name;
  }

  MergePathsMode getMode() {
    return mode;
  }

  @Override
  public String toString() {
    return "MergePaths{" + "mode=" +  mode + '}';
  }
}
