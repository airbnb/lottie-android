package com.airbnb.lottie.manager;

import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import androidx.annotation.Nullable;
import android.util.Log;
import android.view.View;

import com.airbnb.lottie.FontAssetDelegate;
import com.airbnb.lottie.L;
import com.airbnb.lottie.model.MutablePair;

import java.util.HashMap;
import java.util.Map;

public class FontAssetManager {
  private final MutablePair<String> tempPair = new MutablePair<>();

  /** Pair is (fontName, fontStyle) */
  private final Map<MutablePair<String>, Typeface> fontMap = new HashMap<>();
  /** Map of font families to their fonts. Necessary to create a font with a different style */
  private final Map<String, Typeface> fontFamilies = new HashMap<>();
  private final AssetManager assetManager;
  @Nullable private FontAssetDelegate delegate;
  private String defaultFontFileExtension = ".ttf";

  public FontAssetManager(Drawable.Callback callback, @Nullable FontAssetDelegate delegate) {
    this.delegate = delegate;
    if (!(callback instanceof View)) {
      Log.w(L.TAG, "LottieDrawable must be inside of a view for images to work.");
      assetManager = null;
      return;
    }

    assetManager = ((View) callback).getContext().getAssets();
  }

  public void setDelegate(@Nullable FontAssetDelegate assetDelegate) {
    this.delegate = assetDelegate;
  }

  /**
   * Sets the default file extension (include the `.`).
   *
   * e.g. `.ttf` `.otf`
   *
   * Defaults to `.ttf`
   */
  @SuppressWarnings("unused") public void setDefaultFontFileExtension(String defaultFontFileExtension) {
    this.defaultFontFileExtension = defaultFontFileExtension;
  }

  public Typeface getTypeface(String fontFamily, String style) {
    tempPair.set(fontFamily, style);
    Typeface typeface = fontMap.get(tempPair);
    if (typeface != null) {
      return typeface;
    }
    Typeface typefaceWithDefaultStyle = getFontFamily(fontFamily);
    typeface = typefaceForStyle(typefaceWithDefaultStyle, style);
    fontMap.put(tempPair, typeface);
    return typeface;
  }

  private Typeface getFontFamily(String fontFamily) {
    Typeface defaultTypeface = fontFamilies.get(fontFamily);
    if (defaultTypeface != null) {
      return defaultTypeface;
    }

    Typeface typeface = null;
    if (delegate != null) {
      typeface = delegate.fetchFont(fontFamily);
    }

    if (delegate != null && typeface == null) {
      String path = delegate.getFontPath(fontFamily);
      if (path != null) {
        typeface = Typeface.createFromAsset(assetManager, path);
      }
    }

    if (typeface == null) {
      String path = "fonts/" + fontFamily + defaultFontFileExtension;
      typeface = Typeface.createFromAsset(assetManager, path);
    }

    fontFamilies.put(fontFamily, typeface);
    return typeface;
  }

  private Typeface typefaceForStyle(Typeface typeface, String style) {
    int styleInt = Typeface.NORMAL;
    boolean containsItalic = style.contains("Italic");
    boolean containsBold = style.contains("Bold");
    if (containsItalic && containsBold) {
      styleInt = Typeface.BOLD_ITALIC;
    } else if (containsItalic) {
      styleInt = Typeface.ITALIC;
    } else if (containsBold) {
      styleInt = Typeface.BOLD;
    }

    if (typeface.getStyle() == styleInt) {
      return typeface;
    }

    return Typeface.create(typeface, styleInt);
  }
}
