package com.airbnb.lottie.parser;

import android.graphics.Color;

import com.airbnb.lottie.model.content.GradientColor;
import com.airbnb.lottie.parser.moshi.JsonReader;
import com.airbnb.lottie.utils.MiscUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GradientColorParser implements com.airbnb.lottie.parser.ValueParser<GradientColor> {
  /**
   * The number of colors if it exists in the json or -1 if it doesn't (legacy bodymovin)
   */
  private int colorPoints;

  public GradientColorParser(int colorPoints) {
    this.colorPoints = colorPoints;
  }

  /**
   * Both the color stops and opacity stops are in the same array.
   * There are {@link #colorPoints} colors sequentially as:
   * [
   * ...,
   * position,
   * red,
   * green,
   * blue,
   * ...
   * ]
   * <p>
   * The remainder of the array is the opacity stops sequentially as:
   * [
   * ...,
   * position,
   * opacity,
   * ...
   * ]
   */
  @Override
  public GradientColor parse(JsonReader reader, float scale)
      throws IOException {
    List<Float> array = new ArrayList<>();
    // The array was started by Keyframe because it thought that this may be an array of keyframes
    // but peek returned a number so it considered it a static array of numbers.
    boolean isArray = reader.peek() == JsonReader.Token.BEGIN_ARRAY;
    if (isArray) {
      reader.beginArray();
    }
    while (reader.hasNext()) {
      array.add((float) reader.nextDouble());
    }
    if (array.size() == 4 && array.get(0) == 1f) {
      // If a gradient color only contains one color at position 1, add a second stop with the same
      // color at position 0. Android's LinearGradient shader requires at least two colors.
      // https://github.com/airbnb/lottie-android/issues/1967
      array.set(0, 0f);
      array.add(1f);
      array.add(array.get(1));
      array.add(array.get(2));
      array.add(array.get(3));
      colorPoints = 2;
    }
    if (isArray) {
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
          // Positions should monotonically increase. If they don't, it can cause rendering problems on some phones.
          // https://github.com/airbnb/lottie-android/issues/1675
          if (colorIndex > 0 && positions[colorIndex - 1] >= (float) value) {
            positions[colorIndex] = (float) value + 0.01f;
          } else {
            positions[colorIndex] = (float) value;
          }
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
    gradientColor = addOpacityStopsToGradientIfNeeded(gradientColor, array);
    return gradientColor;
  }

  /**
   * This cheats a little bit.
   * Opacity stops can be at arbitrary intervals independent of color stops.
   * This uses the existing color stops and modifies the opacity at each existing color stop
   * based on what the opacity would be.
   * <p>
   * This should be a good approximation is nearly all cases. However, if there are many more
   * opacity stops than color stops, information will be lost.
   */
  private GradientColor addOpacityStopsToGradientIfNeeded(GradientColor gradientColor, List<Float> array) {
    int startIndex = colorPoints * 4;
    if (array.size() <= startIndex) {
      return gradientColor;
    }

    float[] colorStopPositions = gradientColor.getPositions();
    int[] colorStopColors = gradientColor.getColors();

    int opacityStops = (array.size() - startIndex) / 2;
    float[] opacityStopPositions = new float[opacityStops];
    float[] opacityStopOpacities = new float[opacityStops];

    for (int i = startIndex, j = 0; i < array.size(); i++) {
      if (i % 2 == 0) {
        opacityStopPositions[j] = array.get(i);
      } else {
        opacityStopOpacities[j] = array.get(i);
        j++;
      }
    }

    int newColorPoints = colorPoints + opacityStops;
    float[] newPositions = new float[newColorPoints];
    int[] newColors = new int[newColorPoints];

    System.arraycopy(gradientColor.getPositions(), 0, newPositions, 0, colorPoints);
    System.arraycopy(opacityStopPositions, 0, newPositions, colorPoints, opacityStops);
    Arrays.sort(newPositions);

    for (int i = 0; i < newColorPoints; i++) {
      float position = newPositions[i];
      int colorStopIndex = Arrays.binarySearch(colorStopPositions, position);
      int opacityIndex = Arrays.binarySearch(opacityStopPositions, position);
      if (colorStopIndex < 0 || opacityIndex > 0) {
        // This is a stop derived from an opacity stop.
        if (opacityIndex < 0) {
          throw new IllegalArgumentException("Unable to find opacity stop position for " + position);
          // TODO: use this backup instead…
          // opacityIndex = -(opacityIndex + 1);
        }
        newColors[i] = getColorInBetweenColorStops(position, opacityStopOpacities[opacityIndex], colorStopPositions, colorStopColors);
      } else {
        newColors[i] = getColorWithOpacityStops(colorStopColors[colorStopIndex], position, opacityStopPositions, opacityStopOpacities);
      }
    }

    return new GradientColor(newPositions, newColors);
  }

  private int getColorInBetweenColorStops(float position, float opacity, float[] colorStopPositions, int[] colorStopColors) {
    if (colorStopColors.length < 2 || position == colorStopPositions[0]) {
      return colorStopColors[0];
    }
    for (int i = 1; i < colorStopPositions.length; i++) {
      float colorStopPosition = colorStopPositions[i];
      if (colorStopPosition < position && i != colorStopPositions.length - 1) {
        continue;
      }
      // We found the position in which position is between i - 1 and i.
      float distanceBetweenColors = colorStopPositions[i] - colorStopPositions[i - 1];
      float distanceToLowerColor = position - colorStopPositions[i - 1];
      float percentage = distanceToLowerColor / distanceBetweenColors;
      int upperColor = colorStopColors[i];
      int lowerColor = colorStopColors[i];
      int a = (int) (opacity * 255);
      int r = MiscUtils.lerp(Color.red(lowerColor), Color.red(upperColor), percentage);
      int g = MiscUtils.lerp(Color.green(lowerColor), Color.green(upperColor), percentage);
      int b = MiscUtils.lerp(Color.blue(lowerColor), Color.blue(upperColor), percentage);
      return Color.argb(a, r, g, b);
    }
    throw new IllegalArgumentException("Unreachable code.");
  }

  private int getColorWithOpacityStops(int color, float position, float[] opacityStopPositions, float[] opacityStopOpacities) {
    if (opacityStopPositions.length < 2) {
      return color;
    }
    for (int i = 0; i < opacityStopPositions.length; i++) {
      float opacityStopPosition = opacityStopPositions[i];
      if (opacityStopPosition < position && i != opacityStopPositions.length - 1) {
        continue;
      }
      final int a;
      if (opacityStopPosition <= position) {
        a = (int) (opacityStopOpacities[i] * 255);
      } else {
        // We found the position in which position in between i - 1 and i.
        float distanceBetweenOpacities = opacityStopPositions[i] - opacityStopPositions[i - 1];
        float distanceToLowerOpacity = position - opacityStopPositions[i - 1];
        float percentage = distanceToLowerOpacity / distanceBetweenOpacities;
        a = (int) (MiscUtils.lerp(opacityStopOpacities[i - 1], opacityStopOpacities[i], percentage) * 255);
      }
      int r = Color.red(color);
      int g = Color.green(color);
      int b = Color.blue(color);
      return Color.argb(a, r, g, b);
    }
    throw new IllegalArgumentException("Unreachable code.");
  }
}