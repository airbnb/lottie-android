package com.airbnb.lottie;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.Nullable;
import android.support.annotation.RawRes;
import android.support.annotation.WorkerThread;
import android.util.JsonReader;

import com.airbnb.lottie.parser.LottieCompositionParser;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static com.airbnb.lottie.utils.Utils.closeQuietly;

/**
 * Helpers to create a LottieComposition.
 */
public class LottieCompositionFactory {

  private LottieCompositionFactory() {
  }

  public static LottieTask<LottieComposition> fromAsset(Context context, final String fileName) {
    // Prevent accidentally leaking an Activity.
    final Context appContext = context.getApplicationContext();
    return new LottieTask<>(new Callable<LottieResult<LottieComposition>>() {
      @Override public LottieResult<LottieComposition> call() {
        return fromAssetSync(appContext, fileName);
      }
    });
  }

  @WorkerThread
  public static LottieResult<LottieComposition> fromAssetSync(Context context, String fileName) {
    try {
      if (fileName.endsWith(".zip")) {
        return fromZipStreamSync(new ZipInputStream(context.getAssets().open(fileName)));
      }
      return fromJsonInputStreamSync(context.getAssets().open(fileName));
    } catch (IOException e) {
      return new LottieResult<>(e);
    }
  }

  public static LottieTask<LottieComposition> fromRawRes(Context context, @RawRes final int rawRes) {
    // Prevent accidentally leaking an Activity.
    final Context appContext = context.getApplicationContext();
    return new LottieTask<>(new Callable<LottieResult<LottieComposition>>() {
      @Override public LottieResult<LottieComposition> call() throws Exception {
        return fromRawResSync(appContext, rawRes);
      }
    });
  }

  @WorkerThread
  public static LottieResult<LottieComposition> fromRawResSync(Context context, @RawRes int resId) {
    try {
      return fromJsonInputStreamSync(context.getResources().openRawResource(resId));
    } catch (Resources.NotFoundException e) {
      return new LottieResult<>(e);
    }
  }

  /**
   * Auto-closes the stream.
   *
   * @see #fromJsonInputStreamSync(InputStream, boolean)
   */
  public static LottieTask<LottieComposition> fromJsonInputStream(final InputStream stream) {
    return new LottieTask<>(new Callable<LottieResult<LottieComposition>>() {
      @Override public LottieResult<LottieComposition> call() throws Exception {
        return fromJsonInputStreamSync(stream);
      }
    });
  }

  /**
   * Auto-closes the stream.
   *
   * @see #fromJsonInputStreamSync(InputStream, boolean)
   */
  @WorkerThread
  public static LottieResult<LottieComposition> fromJsonInputStreamSync(InputStream stream) {
    return fromJsonInputStreamSync(stream, true);
  }

  /**
   * Return a LottieComposition for the given InputStream to json.
   */
  @WorkerThread
  public static LottieResult<LottieComposition> fromJsonInputStreamSync(InputStream stream, boolean close) {
    try {
      return fromJsonReaderSync(new JsonReader(new InputStreamReader(stream)));
    } finally {
      if (close) {
        closeQuietly(stream);
      }
    }
  }

  /**
   * @see #fromJsonSync(JSONObject)
   */
  @Deprecated
  public static LottieTask<LottieComposition> fromJson(final JSONObject json) {
    return new LottieTask<>(new Callable<LottieResult<LottieComposition>>() {
      @Override public LottieResult<LottieComposition> call() throws Exception {
        return fromJsonSync(json);
      }
    });
  }

  /**
   * Prefer passing in the json string directly. This method just calls `toString()` on your JSONObject.
   * If you are loading this animation from the network, just use the response body string instead of
   * parsing it first for improved performance.
   */
  @Deprecated
  @WorkerThread
  public static LottieResult<LottieComposition> fromJsonSync(JSONObject json) {
    return fromJsonStringSync(json.toString());
  }

  /**
   * @see #fromJsonStringSync(String)
   */
  public static LottieTask<LottieComposition> fromJsonString(final String json) {
    return new LottieTask<>(new Callable<LottieResult<LottieComposition>>() {
      @Override public LottieResult<LottieComposition> call() throws Exception {
        return fromJsonStringSync(json);
      }
    });
  }

  /**
   * Return a LottieComposition for the specified raw json string.
   * If loading from a file, it is preferable to use the InputStream or rawRes version.
   */
  @WorkerThread
  public static LottieResult<LottieComposition> fromJsonStringSync(String json) {
    return fromJsonReaderSync(new JsonReader(new StringReader(json)));
  }

  public static LottieTask<LottieComposition> fromJsonReader(final JsonReader reader) {
    return new LottieTask<>(new Callable<LottieResult<LottieComposition>>() {
      @Override public LottieResult<LottieComposition> call() throws Exception {
        return fromJsonReaderSync(reader);
      }
    });
  }

  /**
   * Return a LottieComposition for the specified json.
   */
  @WorkerThread
  public static LottieResult<LottieComposition> fromJsonReaderSync(JsonReader reader) {
    try {
      return new LottieResult<>(LottieCompositionParser.parse(reader));
    } catch (Exception e) {
      return new LottieResult<>(e);
    }
  }

  public static LottieTask<LottieComposition> fromZipStream(final ZipInputStream inputStream) {
    return new LottieTask<>(new Callable<LottieResult<LottieComposition>>() {
      @Override public LottieResult<LottieComposition> call() throws Exception {
        return fromZipStreamSync(inputStream);
      }
    });
  }

  /**
   * Parses a zip input stream into a Lottie composition. It will automatically store and configure any images inside the animation
   * if they exist.
   */
  @WorkerThread
  private static LottieResult<LottieComposition> fromZipStreamSync(ZipInputStream inputStream, boolean close) {
    try {
      return fromZipStreamSync(inputStream);
    } finally {
      if (close) {
        closeQuietly(inputStream);
      }
    }
  }

  @WorkerThread
  private static LottieResult<LottieComposition> fromZipStreamSync(ZipInputStream inputStream) {
    LottieComposition composition = null;
    Map<String, Bitmap> images = new HashMap<>();

    try {
      ZipEntry entry = inputStream.getNextEntry();
      while (entry != null) {
        if (entry.getName().contains("__MACOSX")) {
          inputStream.closeEntry();
        } else if (entry.getName().contains(".json")) {
          composition = LottieComposition.Factory.fromInputStreamSync(inputStream, false);
        } else if (entry.getName().contains(".png")) {
          String[] splitName = entry.getName().split("/");
          String name = splitName[splitName.length - 1];
          images.put(name, BitmapFactory.decodeStream(inputStream));
        } else {
          inputStream.closeEntry();
        }

        entry = inputStream.getNextEntry();
      }
    } catch (IOException e) {
      return new LottieResult<>(e);
    }


    if (composition == null) {
      return new LottieResult<>(new IllegalArgumentException("Unable to parse composition"));
    }

    for (Map.Entry<String, Bitmap> e : images.entrySet()) {
      LottieImageAsset imageAsset = findImageAssetForFileName(composition, e.getKey());
      if (imageAsset != null) {
        imageAsset.setBitmap(e.getValue());
      }
    }

    // Ensure that all bitmaps have been set.
    for (Map.Entry<String, LottieImageAsset> entry : composition.getImages().entrySet()) {
      if (entry.getValue().getBitmap() == null) {
        return new LottieResult<>(new IllegalStateException("There is no image for " + entry.getValue().getFileName()));
      }
    }

    return new LottieResult<>(composition);
  }

  @Nullable
  private static LottieImageAsset findImageAssetForFileName(LottieComposition composition, String fileName) {
    for (LottieImageAsset asset : composition.getImages().values()) {
      if (asset.getFileName().equals(fileName)) {
        return asset;
      }
    }
    return null;
  }
}
