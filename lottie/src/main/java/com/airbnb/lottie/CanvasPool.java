package com.airbnb.lottie;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v4.util.LongSparseArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertNotNull;

/**
 * Can be used for debugging. When needed, you can acquire and
 * draw to this bitmap layer when rendering to an offscreen
 * buffer to view the contents.
 */
@SuppressWarnings("unused") class CanvasPool {
  private final LongSparseArray<List<Bitmap>> availableBitmaps = new LongSparseArray<>();
  private final Map<BitmapCanvas, Bitmap> canvasBitmapMap = new HashMap<>();
  private final Map<Bitmap, BitmapCanvas> bitmapCanvasMap = new HashMap<>();

  BitmapCanvas acquire(int width, int height, Bitmap.Config config) {
    int key = getKey(width, height, config);
    List<Bitmap> bitmaps = availableBitmaps.get(key);
    if (bitmaps == null) {
      bitmaps = new ArrayList<>();
      availableBitmaps.put(key, bitmaps);
    }

    BitmapCanvas canvas;
    if (bitmaps.isEmpty()) {
      Bitmap bitmap = Bitmap.createBitmap(width, height, config);
      canvas = new BitmapCanvas(bitmap);
      canvasBitmapMap.put(canvas, bitmap);
      bitmapCanvasMap.put(bitmap, canvas);
    } else {
      Bitmap bitmap = bitmaps.remove(0);
      canvas = bitmapCanvasMap.get(bitmap);
      assertNotNull(canvas);
    }
    canvas.getBitmap().eraseColor(Color.TRANSPARENT);
    return canvas;
  }

  void release(BitmapCanvas canvas) {
    assertNotNull(canvas);
    Bitmap bitmap = canvasBitmapMap.get(canvas);
    assertNotNull(bitmap);
    int key = getKey(bitmap);
    List<Bitmap> bitmaps = availableBitmaps.get(key);
    assertNotNull(bitmaps);
    if (bitmaps.contains(bitmap)) {
      throw new IllegalStateException("Canvas already released.");
    }
    bitmaps.add(bitmap);
  }

  void recycleBitmaps() {
    for (int i = 0; i < availableBitmaps.size(); i++) {
      List<Bitmap> bitmaps = availableBitmaps.valueAt(i);
      Iterator<Bitmap> it = bitmaps.iterator();
      while (it.hasNext()) {
        Bitmap bitmap = it.next();
        BitmapCanvas canvas = bitmapCanvasMap.get(bitmap);
        bitmapCanvasMap.remove(bitmap);
        canvasBitmapMap.remove(canvas);
        bitmap.recycle();
        it.remove();
      }
    }
    if (!bitmapCanvasMap.isEmpty()) {
      throw new IllegalStateException("Not all canvases have been released!");
    }
  }

  private int getKey(Bitmap bitmap) {
    return getKey(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());
  }

  private int getKey(int width, int height, Bitmap.Config config) {
    return (width & 0xffff) << 17 | (height & 0xffff) << 1 | (config.ordinal() & 0x1);
  }
}
