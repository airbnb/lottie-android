package com.airbnb.lottie;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import androidx.annotation.Nullable;
import androidx.annotation.RawRes;
import androidx.annotation.WorkerThread;
import android.util.JsonReader;
import android.util.Log;

import com.airbnb.lottie.model.LottieCompositionCache;
import com.airbnb.lottie.network.NetworkFetcher;
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
 * Helpers to create or cache a LottieComposition.
 *
 * All factory methods take a cache key. The animation will be stored in an LRU cache for future use.
 * In-progress tasks will also be held so they can be returned for subsequent requests for the same
 * animation prior to the cache being populated.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class LottieCompositionFactory {
  /**
   * Keep a map of cache keys to in-progress tasks and return them for new requests.
   * Without this, simultaneous requests to parse a composition will trigger multiple parallel
   * parse tasks prior to the cache getting populated.
   */
  private static final Map<String, LottieTask<LottieComposition>> taskCache = new HashMap<>();

  private LottieCompositionFactory() {
  }

  /**
   * Fetch an animation from an http url. Once it is downloaded once, Lottie will cache the file to disk for
   * future use. Because of this, you may call `fromUrl` ahead of time to warm the cache if you think you
   * might need an animation in the future.
   */
  public static LottieTask<LottieComposition> fromUrl(Context context, String url) {
    return NetworkFetcher.fetch(context, url);
  }

  /**
   * Fetch an animation from an http url. Once it is downloaded once, Lottie will cache the file to disk for
   * future use. Because of this, you may call `fromUrl` ahead of time to warm the cache if you think you
   * might need an animation in the future.
   */
  @WorkerThread
  public static LottieResult<LottieComposition> fromUrlSync(Context context, String url) {
    return NetworkFetcher.fetchSync(context, url);
  }

  /**
   * Parse an animation from src/main/assets. It is recommended to use {@link #fromRawRes(Context, int)} instead.
   * The asset file name will be used as a cache key so future usages won't have to parse the json again.
   * However, if your animation has images, you may package the json and images as a single flattened zip file in assets.
   *
   * @see #fromZipStream(ZipInputStream, String)
   */
  public static LottieTask<LottieComposition> fromAsset(Context context, final String fileName) {
    // Prevent accidentally leaking an Activity.
    final Context appContext = context.getApplicationContext();
    return cache(fileName, new Callable<LottieResult<LottieComposition>>() {
      @Override public LottieResult<LottieComposition> call() {
        return fromAssetSync(appContext, fileName);
      }
    });
  }

  /**
   * Parse an animation from src/main/assets. It is recommended to use {@link #fromRawRes(Context, int)} instead.
   * The asset file name will be used as a cache key so future usages won't have to parse the json again.
   * However, if your animation has images, you may package the json and images as a single flattened zip file in assets.
   *
   * @see #fromZipStreamSync(ZipInputStream, String)
   */
  @WorkerThread
  public static LottieResult<LottieComposition> fromAssetSync(Context context, String fileName) {
    try {
      String cacheKey = "asset_" + fileName;
      if (fileName.endsWith(".zip")) {
        return fromZipStreamSync(new ZipInputStream(context.getAssets().open(fileName)), cacheKey);
      }
      return fromJsonInputStreamSync(context.getAssets().open(fileName), cacheKey);
    } catch (IOException e) {
      return new LottieResult<>(e);
    }
  }

  /**
   * Parse an animation from raw/res. This is recommended over putting your animation in assets because
   * it uses a hard reference to R.
   * The resource id will be used as a cache key so future usages won't parse the json again.
   */
  public static LottieTask<LottieComposition> fromRawRes(Context context, @RawRes final int rawRes) {
    // Prevent accidentally leaking an Activity.
    final Context appContext = context.getApplicationContext();
    return cache(rawResCacheKey(rawRes), new Callable<LottieResult<LottieComposition>>() {
      @Override public LottieResult<LottieComposition> call() {
        return fromRawResSync(appContext, rawRes);
      }
    });
  }

  /**
   * Parse an animation from raw/res. This is recommended over putting your animation in assets because
   * it uses a hard reference to R.
   * The resource id will be used as a cache key so future usages won't parse the json again.
   */
  @WorkerThread
  public static LottieResult<LottieComposition> fromRawResSync(Context context, @RawRes int rawRes) {
    try {
      return fromJsonInputStreamSync(context.getResources().openRawResource(rawRes), rawResCacheKey(rawRes));
    } catch (Resources.NotFoundException e) {
      return new LottieResult<>(e);
    }
  }

  private static String rawResCacheKey(@RawRes int resId) {
    return "rawRes_" + resId;
  }

  /**
   * Auto-closes the stream.
   *
   * @see #fromJsonInputStreamSync(InputStream, String, boolean)
   */
  public static LottieTask<LottieComposition> fromJsonInputStream(final InputStream stream, @Nullable final String cacheKey) {
    return cache(cacheKey, new Callable<LottieResult<LottieComposition>>() {
      @Override public LottieResult<LottieComposition> call() {
        return fromJsonInputStreamSync(stream, cacheKey);
      }
    });
  }

  /**
   * Return a LottieComposition for the given InputStream to json.
   */
  @WorkerThread
  public static LottieResult<LottieComposition> fromJsonInputStreamSync(InputStream stream, @Nullable String cacheKey) {
    return fromJsonInputStreamSync(stream, cacheKey, true);
  }

  @WorkerThread
  private static LottieResult<LottieComposition> fromJsonInputStreamSync(InputStream stream, @Nullable String cacheKey, boolean close) {
    try {
      return fromJsonReaderSync(new JsonReader(new InputStreamReader(stream)), cacheKey);
    } finally {
      if (close) {
        closeQuietly(stream);
      }
    }
  }

  /**
   * @see #fromJsonSync(JSONObject, String)
   */
  @Deprecated
  public static LottieTask<LottieComposition> fromJson(final JSONObject json, @Nullable final String cacheKey) {
    return cache(cacheKey, new Callable<LottieResult<LottieComposition>>() {
      @Override public LottieResult<LottieComposition> call() {
        //noinspection deprecation
        return fromJsonSync(json, cacheKey);
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
  public static LottieResult<LottieComposition> fromJsonSync(JSONObject json, @Nullable String cacheKey) {
    return fromJsonStringSync(json.toString(), cacheKey);
  }

  /**
   * @see #fromJsonStringSync(String, String)
   */
  public static LottieTask<LottieComposition> fromJsonString(final String json, @Nullable final String cacheKey) {
    return cache(cacheKey, new Callable<LottieResult<LottieComposition>>() {
      @Override public LottieResult<LottieComposition> call() {
        return fromJsonStringSync(json, cacheKey);
      }
    });
  }

  /**
   * Return a LottieComposition for the specified raw json string.
   * If loading from a file, it is preferable to use the InputStream or rawRes version.
   */
  @WorkerThread
  public static LottieResult<LottieComposition> fromJsonStringSync(String json, @Nullable String cacheKey) {
    return fromJsonReaderSync(new JsonReader(new StringReader(json)), cacheKey);
  }

  public static LottieTask<LottieComposition> fromJsonReader(final JsonReader reader, @Nullable final String cacheKey) {
    return cache(cacheKey, new Callable<LottieResult<LottieComposition>>() {
      @Override public LottieResult<LottieComposition> call() {
        return fromJsonReaderSync(reader, cacheKey);
      }
    });
  }

  /**
   * Return a LottieComposition for the specified json.
   */
  @WorkerThread
  public static LottieResult<LottieComposition> fromJsonReaderSync(JsonReader reader, @Nullable String cacheKey) {
    try {
      LottieComposition composition = LottieCompositionParser.parse(reader);
      LottieCompositionCache.getInstance().put(cacheKey, composition);
      return new LottieResult<>(composition);
    } catch (Exception e) {
      return new LottieResult<>(e);
    }
  }

  public static LottieTask<LottieComposition> fromZipStream(final ZipInputStream inputStream, @Nullable final String cacheKey) {
    return cache(cacheKey, new Callable<LottieResult<LottieComposition>>() {
      @Override public LottieResult<LottieComposition> call() {
        return fromZipStreamSync(inputStream, cacheKey);
      }
    });
  }

  /**
   * Parses a zip input stream into a Lottie composition.
   * Your zip file should just be a folder with your json file and images zipped together.
   * It will automatically store and configure any images inside the animation if they exist.
   */
  @WorkerThread
  public static LottieResult<LottieComposition> fromZipStreamSync(ZipInputStream inputStream, @Nullable String cacheKey) {
    try {
      return fromZipStreamSyncInternal(inputStream, cacheKey);
    } finally {
      closeQuietly(inputStream);
    }
  }

  @WorkerThread
  private static LottieResult<LottieComposition> fromZipStreamSyncInternal(ZipInputStream inputStream, @Nullable String cacheKey) {
    LottieComposition composition = null;
    Map<String, Bitmap> images = new HashMap<>();

    try {
      ZipEntry entry = inputStream.getNextEntry();
      while (entry != null) {
        if (entry.getName().contains("__MACOSX")) {
          inputStream.closeEntry();
        } else if (entry.getName().contains(".json")) {
          composition = LottieCompositionFactory.fromJsonInputStreamSync(inputStream, cacheKey, false).getValue();
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

    LottieCompositionCache.getInstance().put(cacheKey, composition);
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

  /**
   * First, check to see if there are any in-progress tasks associated with the cache key and return it if there is.
   * If not, create a new task for the callable.
   * Then, add the new task to the task cache and set up listeners to it gets cleared when done.
   */
  private static LottieTask<LottieComposition> cache(
          @Nullable final String cacheKey, Callable<LottieResult<LottieComposition>> callable) {
    final LottieComposition cachedComposition = LottieCompositionCache.getInstance().get(cacheKey);
    if (cachedComposition != null) {
      return new LottieTask<>(new Callable<LottieResult<LottieComposition>>() {
        @Override
        public LottieResult<LottieComposition> call() {
          Log.d("Gabe", "call\treturning from cache");
          return new LottieResult<>(cachedComposition);
        }
      });
    }
    if (taskCache.containsKey(cacheKey)) {
      return taskCache.get(cacheKey);
    }

    LottieTask<LottieComposition> task = new LottieTask<>(callable);
    task.addListener(new LottieListener<LottieComposition>() {
      @Override public void onResult(LottieComposition result) {
        if (cacheKey != null) {
          LottieCompositionCache.getInstance().put(cacheKey, result);
        }
        taskCache.remove(cacheKey);
      }
    });
    task.addFailureListener(new LottieListener<Throwable>() {
      @Override public void onResult(Throwable result) {
        taskCache.remove(cacheKey);
      }
    });
    taskCache.put(cacheKey, task);
    return task;
  }
}
