package com.airbnb.lottie.parser;


import com.airbnb.lottie.model.DocumentData;
import com.airbnb.lottie.model.DocumentData.Justification;
import com.airbnb.lottie.parser.moshi.JsonReader;

import java.io.IOException;

public class DocumentDataParser implements ValueParser<DocumentData> {
  public static final DocumentDataParser INSTANCE = new DocumentDataParser();
  private static final JsonReader.Options NAMES = JsonReader.Options.of(
      "t",
      "f",
      "s",
      "j",
      "tr",
      "lh",
      "ls",
      "fc",
      "sc",
      "sw",
      "of"
  );

  private DocumentDataParser() {
  }

  @Override
  public DocumentData parse(JsonReader reader, float scale) throws IOException {
    String text = null;
    String fontName = null;
    double size = 0;
    Justification justification = Justification.CENTER;
    int tracking = 0;
    double lineHeight = 0;
    double baselineShift = 0;
    int fillColor = 0;
    int strokeColor = 0;
    double strokeWidth = 0;
    boolean strokeOverFill = true;

    reader.beginObject();
    while (reader.hasNext()) {
      switch (reader.selectName(NAMES)) {
        case 0:
          text = reader.nextString();
          break;
        case 1:
          fontName = reader.nextString();
          break;
        case 2:
          size = reader.nextDouble();
          break;
        case 3:
          int justificationInt = reader.nextInt();
          if (justificationInt > Justification.CENTER.ordinal() || justificationInt < 0) {
            justification = Justification.CENTER;
          } else {
            justification = Justification.values()[justificationInt];
          }
          break;
        case 4:
          tracking = reader.nextInt();
          break;
        case 5:
          lineHeight = reader.nextDouble();
          break;
        case 6:
          baselineShift = reader.nextDouble();
          break;
        case 7:
          fillColor = JsonUtils.jsonToColor(reader);
          break;
        case 8:
          strokeColor = JsonUtils.jsonToColor(reader);
          break;
        case 9:
          strokeWidth = reader.nextDouble();
          break;
        case 10:
          strokeOverFill = reader.nextBoolean();
          break;
        default:
          reader.skipName();
          reader.skipValue();
      }
    }
    reader.endObject();

    return new DocumentData(text, fontName, size, justification, tracking, lineHeight,
        baselineShift, fillColor, strokeColor, strokeWidth, strokeOverFill);
  }
}
