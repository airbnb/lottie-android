package com.airbnb.lottie;

/**
 * Use this instead of {@link android.animation.ArgbEvaluator} because it interpolates through the gamma color
 * space which looks better to us humans.
 * <p>
 * Writted by Romain Guy and Francois Blavoet.
 * https://androidstudygroup.slack.com/archives/animation/p1476461064000335
 */
class GammaEvaluator {

  // Opto-electronic conversion function for the sRGB color space
  // Takes a gamma-encoded sRGB value and converts it to a linear sRGB value
  private static float OECF_sRGB(float linear) {
    // IEC 61966-2-1:1999
    return linear <= 0.0031308f ?
        linear * 12.92f : (float) ((Math.pow(linear, 1.0f / 2.4f) * 1.055f) - 0.055f);
  }

  // Electro-optical conversion function for the sRGB color space
  // Takes a linear sRGB value and converts it to a gamma-encoded sRGB value
  private static float EOCF_sRGB(float srgb) {
    // IEC 61966-2-1:1999
    return srgb <= 0.04045f ? srgb / 12.92f : (float) Math.pow((srgb + 0.055f) / 1.055f, 2.4f);
  }

  static int evaluate(float fraction, int startInt, int endInt) {
    float startA = ((startInt >> 24) & 0xff) / 255.0f;
    float startR = ((startInt >> 16) & 0xff) / 255.0f;
    float startG = ((startInt >> 8) & 0xff) / 255.0f;
    float startB = (startInt & 0xff) / 255.0f;

    float endA = ((endInt >> 24) & 0xff) / 255.0f;
    float endR = ((endInt >> 16) & 0xff) / 255.0f;
    float endG = ((endInt >> 8) & 0xff) / 255.0f;
    float endB = (endInt & 0xff) / 255.0f;

    // convert from sRGB to linear
    startR = EOCF_sRGB(startR);
    startG = EOCF_sRGB(startG);
    startB = EOCF_sRGB(startB);

    endR = EOCF_sRGB(endR);
    endG = EOCF_sRGB(endG);
    endB = EOCF_sRGB(endB);

    // compute the interpolated color in linear space
    float a = startA + fraction * (endA - startA);
    float r = startR + fraction * (endR - startR);
    float g = startG + fraction * (endG - startG);
    float b = startB + fraction * (endB - startB);

    // convert back to sRGB in the [0..255] range
    a = a * 255.0f;
    r = OECF_sRGB(r) * 255.0f;
    g = OECF_sRGB(g) * 255.0f;
    b = OECF_sRGB(b) * 255.0f;

    return Math.round(a) << 24 | Math.round(r) << 16 | Math.round(g) << 8 | Math.round(b);
  }
}