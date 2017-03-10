package com.airbnb.lottie;

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

  private final MergePathsMode mode;

  private MergePaths(MergePathsMode mode) {
    this.mode = mode;
  }

  static class Factory {
    private Factory() {
    }

    static MergePaths newInstance(JSONObject json) {
      return new MergePaths(MergePathsMode.forId(json.optInt("mm", 1)));
    }
  }

  MergePathsMode getMode() {
    return mode;
  }

  @Override
  public String toString() {
    return "MergePaths{" + "mode=" +  mode + '}';
  }
}
