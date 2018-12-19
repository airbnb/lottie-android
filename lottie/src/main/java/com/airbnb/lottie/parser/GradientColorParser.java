package com.airbnb.lottie.parser;

import android.graphics.Color;
import androidx.annotation.IntRange;
import android.util.JsonReader;
import android.util.JsonToken;

import com.airbnb.lottie.model.content.GradientColor;
import com.airbnb.lottie.utils.MiscUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GradientColorParser implements com.airbnb.lottie.parser.ValueParser<GradientColor> {
  /** The number of colors if it exists in the json or -1 if it doesn't (legacy bodymovin) */
  private int colorPoints;

  public GradientColorParser(int colorPoints) {
    this.colorPoints = colorPoints;
  }

  /**
   * Both the color stops and opacity stops are in the same array.
   * There are {@link #colorPoints} colors sequentially as:
   * [
   *     ...,
   *     position,
   *     red,
   *     green,
   *     blue,
   *     ...
   * ]
   *
   * The remainder of the array is the opacity stops sequentially as:
   * [
   *     ...,
   *     position,
   *     opacity,
   *     ...
   * ]
   */
  @Override public GradientColor parse(JsonReader reader, float scale)
      throws IOException {
    List<Float> array = new ArrayList<>();
    // The array was started by Keyframe because it thought that this may be an array of keyframes
    // but peek returned a number so it considered it a static array of numbers.
    boolean isArray = reader.peek() == JsonToken.BEGIN_ARRAY;
    if (isArray) {
      reader.beginArray();
    }
    while (reader.hasNext()) {
      array.add((float) reader.nextDouble());
    }
    if(isArray) {
      reader.endArray();
    }
    if (colorPoints == -1) {
      colorPoints = array.size() / 4;
    }

    float[] positions = new float[colorPoints];
    int[] colors = new int[colorPoints];

    int r = 0;
    int g = 0;
    for (int i = 0; i < colorPoints * 4; i++) {
      int colorIndex = i / 4;
      double value = array.get(i);
      switch (i % 4) {
        case 0:
          // position
          positions[colorIndex] = (float) value;
          break;
        case 1:
          r = (int) (value * 255);
          break;
        case 2:
          g = (int) (value * 255);
          break;
        case 3:
          int b = (int) (value * 255);
          colors[colorIndex] = Color.argb(255, r, g, b);
          break;
      }
    }

    GradientColor gradientColor = new GradientColor(positions, colors);
    addOpacityStopsToGradientIfNeeded(gradientColor, array);
    return gradientColor;
  }

  /**
   * This cheats a little bit.
   * Opacity stops can be at arbitrary intervals independent of color stops.
   * This uses the existing color stops and modifies the opacity at each existing color stop
   * based on what the opacity would be.
   *
   * This should be a good approximation is nearly all cases. However, if there are many more
   * opacity stops than color stops, information will be lost.
   */
  private void addOpacityStopsToGradientIfNeeded(GradientColor gradientColor, List<Float> array) {
    int startIndex = colorPoints * 4;
    if (array.size() <= startIndex) {
      return;
    }

    int opacityStops = (array.size() - startIndex) / 2;
    double[] positions = new double[opacityStops];
    double[] opacities = new double[opacityStops];

    for (int i = startIndex, j = 0; i < array.size(); i++) {
      if (i % 2 == 0) {
        positions[j] = array.get(i);
      } else {
        opacities[j] = array.get(i);
        j++;
      }
    }

    for (int i = 0; i < gradientColor.getSize(); i++) {
      int color = gradientColor.getColors()[i];
      color = Color.argb(
          getOpacityAtPosition(gradientColor.getPositions()[i], positions, opacities),
          Color.red(color),
          Color.green(color),
          Color.blue(color)
      );
      gradientColor.getColors()[i] = color;
    }
  }

  @IntRange(from=0, to=255)
  private int getOpacityAtPosition(double position, double[] positions, double[] opacities) {
    for (int i = 1; i < positions.length; i++) {
      double lastPosition = positions[i - 1];
      double thisPosition = positions[i];
      if (positions[i] >= position) {
        double progress = (position - lastPosition) / (thisPosition - lastPosition);
        return (int) (255 * MiscUtils.lerp(opacities[i - 1], opacities[i], progress));
      }
    }
    return (int) (255 * opacities[opacities.length - 1]);
  }
}