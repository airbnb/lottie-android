package com.airbnb.lottie.parser;

import android.graphics.Path;
import android.util.JsonWriter;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.model.animatable.AnimatableGradientColorValue;
import com.airbnb.lottie.model.animatable.AnimatableIntegerValue;
import com.airbnb.lottie.model.animatable.AnimatablePointValue;
import com.airbnb.lottie.model.content.GradientFill;
import com.airbnb.lottie.model.content.GradientType;
import com.airbnb.lottie.parser.moshi.JsonReader;
import com.airbnb.lottie.value.Keyframe;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collections;

import okio.Buffer;

class GradientFillParser {
  private static final JsonReader.Options NAMES = JsonReader.Options.of(
      "nm",
      "g",
      "o",
      "t",
      "s",
      "e",
      "r",
      "hd"
  );
  private static final JsonReader.Options GRADIENT_NAMES = JsonReader.Options.of(
      "p",
      "k"
  );

  private GradientFillParser() {
  }

  static GradientFill parse(
      JsonReader reader, LottieComposition composition) throws IOException {
    String name = null;
    AnimatableGradientColorValue color = null;
    AnimatableIntegerValue opacity = null;
    GradientType gradientType = null;
    AnimatablePointValue startPoint = null;
    AnimatablePointValue endPoint = null;
    Path.FillType fillType = Path.FillType.WINDING;
    boolean hidden = false;

    while (reader.hasNext()) {
      switch (reader.selectName(NAMES)) {
        case 0:
          name = reader.nextString();
          break;
        case 1:
          int points = -1;
          boolean pointsKnown = false;
          String colorJson = null;
          reader.beginObject();
          while (reader.hasNext()) {
            switch (reader.selectName(GRADIENT_NAMES)) {
              case 0:
                points = reader.nextInt();
                pointsKnown = true;
                break;
              case 1:
                if (pointsKnown) {
                  color = AnimatableValueParser.parseGradientColor(reader, composition, points);
                } else {
                  // The point count is needed to separate opacity stops from color stops.
                  colorJson = bufferJsonValue(reader);
                }
                break;
              default:
                reader.skipName();
                reader.skipValue();
            }
          }
          reader.endObject();
          if (colorJson != null) {
            JsonReader colorReader = JsonReader.of(new Buffer().writeUtf8(colorJson));
            color = AnimatableValueParser.parseGradientColor(colorReader, composition, points);
          }
          break;
        case 2:
          opacity = AnimatableValueParser.parseInteger(reader, composition);
          break;
        case 3:
          gradientType = reader.nextInt() == 1 ? GradientType.LINEAR : GradientType.RADIAL;
          break;
        case 4:
          startPoint = AnimatableValueParser.parsePoint(reader, composition);
          break;
        case 5:
          endPoint = AnimatableValueParser.parsePoint(reader, composition);
          break;
        case 6:
          fillType = reader.nextInt() == 1 ? Path.FillType.WINDING : Path.FillType.EVEN_ODD;
          break;
        case 7:
          hidden = reader.nextBoolean();
          break;
        default:
          reader.skipName();
          reader.skipValue();
      }
    }

    // Telegram sometimes omits opacity.
    // https://github.com/airbnb/lottie-android/issues/1600
    opacity = opacity == null ? new AnimatableIntegerValue(Collections.singletonList(new Keyframe<>(100))) : opacity;
    return new GradientFill(
        name, gradientType, fillType, color, opacity, startPoint, endPoint, null, null, hidden);
  }

  private static String bufferJsonValue(JsonReader reader) throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(stringWriter);
    copyJsonValue(reader, jsonWriter);
    jsonWriter.close();
    return stringWriter.toString();
  }

  private static void copyJsonValue(JsonReader reader, JsonWriter writer) throws IOException {
    switch (reader.peek()) {
      case BEGIN_ARRAY:
        reader.beginArray();
        writer.beginArray();
        while (reader.hasNext()) {
          copyJsonValue(reader, writer);
        }
        reader.endArray();
        writer.endArray();
        break;
      case BEGIN_OBJECT:
        reader.beginObject();
        writer.beginObject();
        while (reader.hasNext()) {
          writer.name(reader.nextName());
          copyJsonValue(reader, writer);
        }
        reader.endObject();
        writer.endObject();
        break;
      case STRING:
        writer.value(reader.nextString());
        break;
      case NUMBER:
        writer.value(reader.nextDouble());
        break;
      case BOOLEAN:
        writer.value(reader.nextBoolean());
        break;
      case NULL:
        reader.skipValue();
        writer.nullValue();
        break;
      default:
        throw new IOException("Unexpected token in gradient color: " + reader.peek());
    }
  }
}
