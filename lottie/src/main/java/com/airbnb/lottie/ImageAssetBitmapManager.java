package com.airbnb.lottie;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static junit.framework.Assert.assertNotNull;

class ImageAssetBitmapManager {
  private final Context context;
  private String imagesFolder;
  @Nullable private ImageAssetDelegate assetDelegate;
  private final Map<String, LottieImageAsset> imageAssets;
  private final Map<String, Bitmap> bitmaps = new HashMap<>();

  ImageAssetBitmapManager(Drawable.Callback callback, String imagesFolder,
      ImageAssetDelegate assetDelegate, Map<String, LottieImageAsset> imageAssets) {
    assertNotNull(callback);

    this.imagesFolder = imagesFolder;
    if (!TextUtils.isEmpty(imagesFolder) &&
        this.imagesFolder.charAt(this.imagesFolder.length() - 1) != '/') {
      this.imagesFolder += '/';
    }

    if (!(callback instanceof View)) {
      Log.w(L.TAG, "LottieDrawable must be inside of a view for images to work.");
      this.imageAssets = new HashMap<>();
      context = null;
      return;
    }

    context = ((View) callback).getContext();
    this.imageAssets = imageAssets;
    setAssetDelegate(assetDelegate);
  }

  void setAssetDelegate(@Nullable ImageAssetDelegate assetDelegate) {
    this.assetDelegate = assetDelegate;
  }

  @Nullable
  Bitmap bitmapForId(String id) {
    Bitmap bitmap = bitmaps.get(id);
    if (bitmap == null) {
      LottieImageAsset imageAsset = imageAssets.get(id);
      if (imageAsset == null) {
        return null;
      }
      if (assetDelegate != null) {
        bitmap = assetDelegate.fetchBitmap(imageAsset);
        if (bitmap != null) {
          bitmaps.put(id, bitmap);
        }
        return bitmap;
      }

      InputStream is;
      try {
        if (TextUtils.isEmpty(imagesFolder)) {
          throw new IllegalStateException("You must set an images folder before loading an image." +
              " Set it with LottieComposition#setImagesFolder or LottieDrawable#setImagesFolder");
        }
        is = context.getAssets().open(imagesFolder + imageAsset.getFileName());
      } catch (IOException e) {
        Log.w(L.TAG, "Unable to open asset.", e);
        return null;
      }
      BitmapFactory.Options opts = new BitmapFactory.Options();
      opts.inScaled = true;
      opts.inDensity = 160;
      bitmap = BitmapFactory.decodeStream(is, null, opts);
      bitmaps.put(id, bitmap);
    }
    return bitmap;
  }

  void recycleBitmaps() {
    Iterator<Map.Entry<String, Bitmap>> it = bitmaps.entrySet().iterator();
    while (it.hasNext()) {
      Map.Entry<String, Bitmap> entry = it.next();
      entry.getValue().recycle();
      it.remove();
    }
  }

  boolean hasSameContext(Context context) {
    return context == null && this.context == null ||
        context != null && this.context.equals(context);
  }
}
