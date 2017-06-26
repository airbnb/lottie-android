package com.airbnb.lottie;

import org.json.JSONObject;

class Font {

  private final String family;
  private final String name;
  private final String style;
  private final double ascent;

  Font(String family, String name, String style, double ascent) {
    this.family = family;
    this.name = name;
    this.style = style;
    this.ascent = ascent;
  }

  @SuppressWarnings("unused") String getFamily() {
    return family;
  }

  String getName() {
    return name;
  }

  String getStyle() {
    return style;
  }

  @SuppressWarnings("unused") double getAscent() {
    return ascent;
  }

  static class Factory {

    static Font newInstance(JSONObject json) {
      String family = json.optString("fFamily");
      String name = json.optString("fName");
      String style = json.optString("fStyle");
      double ascent = json.optDouble("ascent");
      return new Font(family, name, style, ascent);
    }
  }
}
