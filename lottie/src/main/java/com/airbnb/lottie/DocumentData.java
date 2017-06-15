package com.airbnb.lottie;

import android.graphics.Color;
import android.support.annotation.ColorInt;

import org.json.JSONArray;
import org.json.JSONObject;

class DocumentData {

  String text;
  String fontFamily;
  int size;
  int justification;
  int tracking;
  double lineHeight;
  @ColorInt int color;

  DocumentData() {
  }


  DocumentData(String text, String fontFamily, int size, int justification, int tracking,
      double lineHeight, @ColorInt int color) {
    this.text = text;
    this.fontFamily = fontFamily;
    // TODO: figure out what these are.
    this.size = size;
    this.justification = justification;
    this.tracking = tracking;
    this.lineHeight = lineHeight;
    this.color = color;
  }

  void set(DocumentData documentData) {
    text = documentData.text;
    fontFamily = documentData.fontFamily;
    size = documentData.size;
    justification = documentData.justification;
    tracking = documentData.tracking;
    lineHeight = documentData.lineHeight;
    color = documentData.color;
  }


  static final class Factory {

    private Factory() {
    }

    static DocumentData newInstance(JSONObject json) {
      String text = json.optString("t");
      String fontFamily = json.optString("f");
      int size = json.optInt("s");
      int justification = json.optInt("j");
      int tracking = json.optInt("tr");
      double lineHeight = json.optDouble("lh");
      JSONArray colorArray = json.optJSONArray("fc");
      int color = Color.argb(
          255,
          (int) (colorArray.optDouble(0) * 255),
          (int) (colorArray.optDouble(1) * 255),
          (int) (colorArray.optDouble(2) * 255));

      return new DocumentData(text, fontFamily, size, justification, tracking, lineHeight, color);
    }
  }

  @Override public int hashCode() {
    int result;
    long temp;
    result = text.hashCode();
    result = 31 * result + fontFamily.hashCode();
    result = 31 * result + size;
    result = 31 * result + justification;
    result = 31 * result + tracking;
    temp = Double.doubleToLongBits(lineHeight);
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    result = 31 * result + color;
    return result;
  }
}
