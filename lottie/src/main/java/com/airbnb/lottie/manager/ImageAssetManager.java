package com.airbnb.lottie.manager;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;

import com.airbnb.lottie.ImageAssetDelegate;
import com.airbnb.lottie.L;
import com.airbnb.lottie.LottieImageAsset;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ImageAssetManager {
  private static final Object bitmapHashLock = new Object();

  private final Context context;
  private String imagesFolder;
  @Nullable private ImageAssetDelegate delegate;
  private final Map<String, LottieImageAsset> imageAssets;

  public ImageAssetManager(Drawable.Callback callback, String imagesFolder,
      ImageAssetDelegate delegate, Map<String, LottieImageAsset> imageAssets) {
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
    setDelegate(delegate);
  }

  public void setDelegate(@Nullable ImageAssetDelegate assetDelegate) {
    this.delegate = assetDelegate;
  }

  /**
   * Returns the previously set bitmap or null.
   */
  @Nullable public Bitmap updateBitmap(String id, @Nullable Bitmap bitmap) {
    if (bitmap == null) {
      LottieImageAsset asset = imageAssets.get(id);
      Bitmap ret = asset.getBitmap();
      asset.setBitmap(null);
      return ret;
    }
    return putBitmap(id, bitmap);
  }

  @Nullable public Bitmap bitmapForId(String id) {
    LottieImageAsset asset = imageAssets.get(id);
    if (asset == null) {
      return null;
    }
    Bitmap bitmap = asset.getBitmap();
    if (bitmap != null) {
      return bitmap;
    }

    if (delegate != null) {
      bitmap = delegate.fetchBitmap(asset);
      if (bitmap != null) {
        putBitmap(id, bitmap);
      }
      return bitmap;
    }

    String filename = asset.getFileName();
    BitmapFactory.Options opts = new BitmapFactory.Options();
    opts.inScaled = true;
    opts.inDensity = 160;

    if (filename.startsWith("data:") && filename.indexOf("base64,") > 0) {
      // Contents look like a base64 data URI, with the format data:image/png;base64,<data>.
      byte[] data;
      try {
        data = Base64.decode(filename.substring(filename.indexOf(',') + 1), Base64.DEFAULT);
      } catch (IllegalArgumentException e) {
        Log.w(L.TAG, "data URL did not have correct base64 format.", e);
        return null;
      }
      bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, opts);
      return putBitmap(id, bitmap);
    }

    InputStream is;
    try {
      if (TextUtils.isEmpty(imagesFolder)) {
        throw new IllegalStateException("You must set an images folder before loading an image." +
            " Set it with LottieComposition#setImagesFolder or LottieDrawable#setImagesFolder");
      }
      is = context.getAssets().open(imagesFolder + filename);
    } catch (IOException e) {
      Log.w(L.TAG, "Unable to open asset.", e);
      return null;
    }
    bitmap = BitmapFactory.decodeStream(is, null, opts);
    return putBitmap(id, bitmap);
  }

  public void recycleBitmaps() {
    synchronized (bitmapHashLock) {
      for (Map.Entry<String, LottieImageAsset> entry : imageAssets.entrySet()) {
        LottieImageAsset asset = entry.getValue();
        Bitmap bitmap = asset.getBitmap();
        if (bitmap != null) {
          bitmap.recycle();
          asset.setBitmap(null);
        }
      }
    }
  }


  public boolean hasSameContext(Context context) {
    return context == null && this.context == null || this.context.equals(context);
  }

  private Bitmap putBitmap(String key, @Nullable Bitmap bitmap) {
    synchronized (bitmapHashLock) {
      imageAssets.get(key).setBitmap(bitmap);
      return bitmap;
    }
  }
}
