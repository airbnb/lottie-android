package com.airbnb.lottie.model;

import android.util.JsonReader;

import java.io.IOException;

public class Font {

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

  @SuppressWarnings("unused") public String getFamily() {
    return family;
  }

  public String getName() {
    return name;
  }

  public String getStyle() {
    return style;
  }

  @SuppressWarnings("unused") float getAscent() {
    return ascent;
  }

  public static class Factory {

    public static Font newInstance(JsonReader reader) throws IOException {
      String family = null;
      String name = null;
      String style = null;
      float ascent = 0;

      reader.beginObject();
      while (reader.hasNext()) {
        switch (reader.nextName()) {
          case "fFamily":
            family = reader.nextString();
            break;
          case "fName":
            name = reader.nextString();
            break;
          case "fStyle":
            style = reader.nextString();
            break;
          case "ascent":
            ascent = (float) reader.nextDouble();
            break;
          default:
            reader.skipValue();
        }
      }
      reader.endObject();

      return new Font(family, name, style, ascent);
    }
  }
}
