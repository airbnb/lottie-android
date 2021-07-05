package com.airbnb.lottie.manager;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;

import androidx.annotation.Nullable;

import com.airbnb.lottie.ImageAssetDelegate;
import com.airbnb.lottie.LottieImageAsset;
import com.airbnb.lottie.utils.Logger;
import com.airbnb.lottie.utils.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class ImageAssetManager {
  private static final Object bitmapHashLock = new Object();

  private final Context context;
  private final String imagesFolder;
  @Nullable private ImageAssetDelegate delegate;
  private final Map<String, LottieImageAsset> imageAssets;
  private boolean alwaysCallDelegate = false;

  public ImageAssetManager(Drawable.Callback callback, String imagesFolder,
      ImageAssetDelegate delegate, Map<String, LottieImageAsset> imageAssets) {
    if (!TextUtils.isEmpty(imagesFolder) && imagesFolder.charAt(imagesFolder.length() - 1) != '/') {
      this.imagesFolder = imagesFolder + '/';
    } else {
      this.imagesFolder = imagesFolder;
    }

    if (!(callback instanceof View)) {
      Logger.warning("LottieDrawable must be inside of a view for images to work.");
      this.imageAssets = new HashMap<>();
      context = null;
      return;
    }

    context = ((View) callback).getContext();
    this.imageAssets = imageAssets;
    setDelegate(delegate);
  }

  public ImageAssetManager(Context context, String imagesFolder, ImageAssetDelegate delegate,
      Map<String, LottieImageAsset> imageAssets, boolean alwaysCallDelegate) {
    this.context = context;
    this.alwaysCallDelegate = alwaysCallDelegate;
    if (!TextUtils.isEmpty(imagesFolder) && imagesFolder.charAt(imagesFolder.length() - 1) != '/') {
      this.imagesFolder = imagesFolder + '/';
    } else {
      this.imagesFolder = imagesFolder;
    }
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
    Bitmap prevBitmap = imageAssets.get(id).getBitmap();
    putBitmap(id, bitmap);
    return prevBitmap;
  }

  @Nullable public Bitmap bitmapForId(String id) {
    LottieImageAsset asset = imageAssets.get(id);
    if (asset == null) {
      return null;
    }
    Bitmap bitmap = asset.getBitmap();
    if (bitmap != null && !alwaysCallDelegate) {
      return bitmap;
    }

    BitmapFactory.Options opts = new BitmapFactory.Options();
    opts.inScaled = true;
    opts.inDensity = 160;

    maybeDecodeBase64Image(asset, opts);
    maybeDecodeImageFromAssets(asset, opts);

    if (delegate != null) {
      bitmap = delegate.fetchBitmap(asset);
      if (bitmap != null) {
        putBitmap(id, bitmap);
      }
    }
    return asset.getBitmap();
  }

  private void maybeDecodeBase64Image(LottieImageAsset asset, BitmapFactory.Options opts) {
    if (asset.getBitmap() != null) {
      return;
    }
    String filename = asset.getFileName();
    if (asset.getBitmap() == null && filename.startsWith("data:") && filename.indexOf("base64,") > 0) {
      // Contents look like a base64 data URI, with the format data:image/png;base64,<data>.
      try {
        byte[] data = Base64.decode(filename.substring(filename.indexOf(',') + 1), Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, opts);
        putBitmap(asset.getId(), bitmap);
      } catch (IllegalArgumentException e) {
        Logger.warning("data URL did not have correct base64 format.", e);
      }
    }
  }

  private void maybeDecodeImageFromAssets(LottieImageAsset asset, BitmapFactory.Options opts) {
    if (asset.getBitmap() != null) {
      return;
    }
    String filename = asset.getFileName();
    InputStream is;
    try {
      if (delegate == null && TextUtils.isEmpty(imagesFolder)) {
        throw new IllegalStateException("You must set an images folder or an image asset delegate before loading an image." +
            " Set it with LottieComposition#setImagesFolder or LottieDrawable#setImagesFolder");
      }
      is = context.getAssets().open(imagesFolder + filename);
    } catch (IOException e) {
      Logger.warning("Unable to open asset.", e);
      return;
    }
    try {
      Bitmap bitmap = BitmapFactory.decodeStream(is, null, opts);
      bitmap = Utils.resizeBitmapIfNeeded(bitmap, asset.getWidth(), asset.getHeight());
      putBitmap(asset.getId(), bitmap);
    } catch (IllegalArgumentException e) {
      Logger.warning("Unable to decode image.", e);
    }
  }

  public boolean hasSameContext(Context context) {
    return context == null && this.context == null || this.context.equals(context);
  }

  private void putBitmap(String key, @Nullable Bitmap bitmap) {
    synchronized (bitmapHashLock) {
      imageAssets.get(key).setBitmap(bitmap);
    }
  }
}
