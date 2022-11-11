package com.airbnb.lottie.parser;

import android.graphics.Color;

import com.airbnb.lottie.model.content.GradientColor;
import com.airbnb.lottie.parser.moshi.JsonReader;
import com.airbnb.lottie.utils.GammaEvaluator;
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

    // When there are opacity stops, we create a merged list of color stops and opacity stops.
    // For a given color stop, we linearly interpolate the opacity for the two opacity stops around it.
    // For a given opacity stop, we linearly interpolate the color for the two color stops around it.
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

    // Pre-SKIA (Oreo) devices render artifacts when there is two stops in the same position.
    // As a result, we have to de-dupe the merge color and opacity stop positions.
    float[] newPositions = mergeUniqueElements(gradientColor.getPositions(), opacityStopPositions);
    int newColorPoints = newPositions.length;
    int[] newColors = new int[newColorPoints];

    for (int i = 0; i < newColorPoints; i++) {
      float position = newPositions[i];
      int colorStopIndex = Arrays.binarySearch(colorStopPositions, position);
      int opacityIndex = Arrays.binarySearch(opacityStopPositions, position);
      if (colorStopIndex < 0 || opacityIndex > 0) {
        // This is a stop derived from an opacity stop.
        if (opacityIndex < 0) {
          // The formula here is derived from the return value for binarySearch. When an item isn't found, it returns -insertionPoint - 1.
          opacityIndex = -(opacityIndex + 1);
        }
        newColors[i] = getColorInBetweenColorStops(position, opacityStopOpacities[opacityIndex], colorStopPositions, colorStopColors);
      } else {
        // This os a step derived from a color stop.
        newColors[i] = getColorInBetweenOpacityStops(position, colorStopColors[colorStopIndex], opacityStopPositions, opacityStopOpacities);
      }
    }
    return new GradientColor(newPositions, newColors);
  }

  int getColorInBetweenColorStops(float position, float opacity, float[] colorStopPositions, int[] colorStopColors) {
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
      int lowerColor = colorStopColors[i - 1];
      int a = (int) (opacity * 255);
      int r = GammaEvaluator.evaluate(percentage, Color.red(lowerColor), Color.red(upperColor));
      int g = GammaEvaluator.evaluate(percentage, Color.green(lowerColor), Color.green(upperColor));
      int b = GammaEvaluator.evaluate(percentage, Color.blue(lowerColor), Color.blue(upperColor));
      return Color.argb(a, r, g, b);
    }
    throw new IllegalArgumentException("Unreachable code.");
  }

  private int getColorInBetweenOpacityStops(float position, int color, float[] opacityStopPositions, float[] opacityStopOpacities) {
    if (opacityStopOpacities.length < 2 || position <= opacityStopPositions[0]) {
      int a = (int) (opacityStopOpacities[0] * 255);
      int r = Color.red(color);
      int g = Color.green(color);
      int b = Color.blue(color);
      return Color.argb(a, r, g, b);
    }
    for (int i = 1; i < opacityStopPositions.length; i++) {
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

  /**
   * Takes two sorted float arrays and merges their elements while removing duplicates.
   */
  protected static float[] mergeUniqueElements(float[] arrayA, float[] arrayB) {
    if (arrayA.length == 0) {
      return arrayB;
    } else if (arrayB.length == 0) {
      return arrayA;
    }

    int aIndex = 0;
    int bIndex = 0;
    int numDuplicates = 0;
    // This will be the merged list but may be longer than what is needed if there are duplicates.
    // If there are, the 0 elements at the end need to be truncated.
    float[] mergedNotTruncated = new float[arrayA.length + arrayB.length];
    for (int i = 0; i < mergedNotTruncated.length; i++) {
      final float a = aIndex < arrayA.length ? arrayA[aIndex] : Float.NaN;
      final float b = bIndex < arrayB.length ? arrayB[bIndex] : Float.NaN;

      if (Float.isNaN(b) || a < b) {
        mergedNotTruncated[i] = a;
        aIndex++;
      } else if (Float.isNaN(a) || b < a) {
        mergedNotTruncated[i] = b;
        bIndex++;
      } else {
        mergedNotTruncated[i] = a;
        aIndex++;
        bIndex++;
        numDuplicates++;
      }
    }

    if (numDuplicates == 0) {
      return mergedNotTruncated;
    }


    return Arrays.copyOf(mergedNotTruncated, mergedNotTruncated.length - numDuplicates);
  }
}