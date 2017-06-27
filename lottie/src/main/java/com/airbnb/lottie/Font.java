package com.airbnb.lottie;

import org.json.JSONObject;

class Font {

  private final String family;
  private final String name;
  private final String style;
  private final float ascent;

  Font(String family, String name, String style, float ascent) {
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

  @SuppressWarnings("unused") float getAscent() {
    return ascent;
  }

  static class Factory {

    static Font newInstance(JSONObject json) {
      String family = json.optString("fFamily");
      String name = json.optString("fName");
      String style = json.optString("fStyle");
      float ascent = (float) json.optDouble("ascent");
      return new Font(family, name, style, ascent);
    }
  }
}
