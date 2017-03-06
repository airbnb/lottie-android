package com.airbnb.lottie;

import android.graphics.Bitmap;

/**
 * Delegate to handle the loading of bitmaps that are not packaged in the assets of your app.
 *
 * @see LottieDrawable#setImageAssetDelegate(ImageAssetDelegate)
 */
public interface ImageAssetDelegate {
  Bitmap fetchBitmap(LottieImageAsset asset);
}
