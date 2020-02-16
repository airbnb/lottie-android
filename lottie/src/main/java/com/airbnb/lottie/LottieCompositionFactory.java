package com.airbnb.lottie;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;

import com.airbnb.lottie.model.LottieCompositionCache;
import com.airbnb.lottie.network.NetworkCache;
import com.airbnb.lottie.network.NetworkFetcher;
import com.airbnb.lottie.parser.LottieCompositionMoshiParser;
import com.airbnb.lottie.parser.moshi.JsonReader;

import com.airbnb.lottie.utils.Utils;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import androidx.annotation.Nullable;
import androidx.annotation.RawRes;
import androidx.annotation.WorkerThread;

import static com.airbnb.lottie.parser.moshi.JsonReader.*;
import static com.airbnb.lottie.utils.Utils.closeQuietly;
import static okio.Okio.buffer;
import static okio.Okio.source;

/**
 * Helpers to create or cache a LottieComposition.
 * <p>
 * All factory methods take a cache key. The animation will be stored in an LRU cache for future use.
 * In-progress tasks will also be held so they can be returned for subsequent requests for the same
 * animation prior to the cache being populated.
 */
@SuppressWarnings({"WeakerAccess", "unused", "NullAway"})
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
   * Set the maximum number of compositions to keep cached in memory.
   * This must be > 0.
   */
  public static void setMaxCacheSize(int size) {
    LottieCompositionCache.getInstance().resize(size);
  }

  public static void clearCache(Context context) {
    taskCache.clear();
    LottieCompositionCache.getInstance().clear();
    new NetworkCache(context).clear();
  }

  /**
   * Fetch an animation from an http url. Once it is downloaded once, Lottie will cache the file to disk for
   * future use. Because of this, you may call `fromUrl` ahead of time to warm the cache if you think you
   * might need an animation in the future.
   *
   * To skip the cache, add null as a third parameter.
   */
  public static LottieTask<LottieComposition> fromUrl(final Context context, final String url) {
    return fromUrl(context, url, "url_" + url);
  }

  /**
   * Fetch an animation from an http url. Once it is downloaded once, Lottie will cache the file to disk for
   * future use. Because of this, you may call `fromUrl` ahead of time to warm the cache if you think you
   * might need an animation in the future.
   */
  public static LottieTask<LottieComposition> fromUrl(final Context context, final String url, @Nullable final String cacheKey) {
    return cache(cacheKey, new Callable<LottieResult<LottieComposition>>() {
      @Override
      public LottieResult<LottieComposition> call() {
        return NetworkFetcher.fetchSync(context, url, cacheKey);
      }
    });
  }

  /**
   * Fetch an animation from an http url. Once it is downloaded once, Lottie will cache the file to disk for
   * future use. Because of this, you may call `fromUrl` ahead of time to warm the cache if you think you
   * might need an animation in the future.
   */
  @WorkerThread
  public static LottieResult<LottieComposition> fromUrlSync(Context context, String url) {
    return fromUrlSync(context, url, url);
  }


  /**
   * Fetch an animation from an http url. Once it is downloaded once, Lottie will cache the file to disk for
   * future use. Because of this, you may call `fromUrl` ahead of time to warm the cache if you think you
   * might need an animation in the future.
   */
  @WorkerThread
  public static LottieResult<LottieComposition> fromUrlSync(Context context, String url, @Nullable String cacheKey) {
    return NetworkFetcher.fetchSync(context, url, cacheKey);
  }

  /**
   * Parse an animation from src/main/assets. It is recommended to use {@link #fromRawRes(Context, int)} instead.
   * The asset file name will be used as a cache key so future usages won't have to parse the json again.
   * However, if your animation has images, you may package the json and images as a single flattened zip file in assets.
   *
   * To skip the cache, add null as a third parameter.
   *
   * @see #fromZipStream(ZipInputStream, String)
   */
  public static LottieTask<LottieComposition> fromAsset(Context context, final String fileName) {
    String cacheKey = "asset_" + fileName;
    return fromAsset(context, fileName, cacheKey);
  }

  /**
   * Parse an animation from src/main/assets. It is recommended to use {@link #fromRawRes(Context, int)} instead.
   * The asset file name will be used as a cache key so future usages won't have to parse the json again.
   * However, if your animation has images, you may package the json and images as a single flattened zip file in assets.
   *
   * Pass null as the cache key to skip the cache.
   *
   * @see #fromZipStream(ZipInputStream, String)
   */
  public static LottieTask<LottieComposition> fromAsset(Context context, final String fileName, @Nullable final String cacheKey) {
    // Prevent accidentally leaking an Activity.
    final Context appContext = context.getApplicationContext();
    return cache(cacheKey, new Callable<LottieResult<LottieComposition>>() {
      @Override
      public LottieResult<LottieComposition> call() {
        return fromAssetSync(appContext, fileName, cacheKey);
      }
    });
  }

  /**
   * Parse an animation from src/main/assets. It is recommended to use {@link #fromRawRes(Context, int)} instead.
   * The asset file name will be used as a cache key so future usages won't have to parse the json again.
   * However, if your animation has images, you may package the json and images as a single flattened zip file in assets.
   *
   * To skip the cache, add null as a third parameter.
   *
   * @see #fromZipStreamSync(ZipInputStream, String)
   */
  @WorkerThread
  public static LottieResult<LottieComposition> fromAssetSync(Context context, String fileName) {
      String cacheKey = "asset_" + fileName;
      return fromAssetSync(context, fileName, cacheKey);
  }

  /**
   * Parse an animation from src/main/assets. It is recommended to use {@link #fromRawRes(Context, int)} instead.
   * The asset file name will be used as a cache key so future usages won't have to parse the json again.
   * However, if your animation has images, you may package the json and images as a single flattened zip file in assets.
   *
   * Pass null as the cache key to skip the cache.
   *
   * @see #fromZipStreamSync(ZipInputStream, String)
   */
  @WorkerThread
  public static LottieResult<LottieComposition> fromAssetSync(Context context, String fileName, @Nullable String cacheKey) {
    try {
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
   * Note: to correctly load dark mode (-night) resources, make sure you pass Activity as a context (instead of e.g. the application context).
   * The Activity won't be leaked.
   *
   * To skip the cache, add null as a third parameter.
   */
  public static LottieTask<LottieComposition> fromRawRes(Context context, @RawRes final int rawRes) {
    return fromRawRes(context, rawRes, rawResCacheKey(context, rawRes));
  }

  /**
   * Parse an animation from raw/res. This is recommended over putting your animation in assets because
   * it uses a hard reference to R.
   * The resource id will be used as a cache key so future usages won't parse the json again.
   * Note: to correctly load dark mode (-night) resources, make sure you pass Activity as a context (instead of e.g. the application context).
   * The Activity won't be leaked.
   *
   * Pass null as the cache key to skip caching.
   */
  public static LottieTask<LottieComposition> fromRawRes(Context context, @RawRes final int rawRes, @Nullable String cacheKey) {
    // Prevent accidentally leaking an Activity.
    final WeakReference<Context> contextRef = new WeakReference<>(context);
    final Context appContext = context.getApplicationContext();
    return cache(cacheKey, new Callable<LottieResult<LottieComposition>>() {
      @Override
      public LottieResult<LottieComposition> call() {
        @Nullable Context originalContext = contextRef.get();
        Context context = originalContext != null ? originalContext : appContext;
        return fromRawResSync(context, rawRes);
      }
    });
  }

  /**
   * Parse an animation from raw/res. This is recommended over putting your animation in assets because
   * it uses a hard reference to R.
   * The resource id will be used as a cache key so future usages won't parse the json again.
   * Note: to correctly load dark mode (-night) resources, make sure you pass Activity as a context (instead of e.g. the application context).
   * The Activity won't be leaked.
   *
   * To skip the cache, add null as a third parameter.
   */
  @WorkerThread
  public static LottieResult<LottieComposition> fromRawResSync(Context context, @RawRes int rawRes) {
    return fromRawResSync(context, rawRes, rawResCacheKey(context, rawRes));
  }

  /**
   * Parse an animation from raw/res. This is recommended over putting your animation in assets because
   * it uses a hard reference to R.
   * The resource id will be used as a cache key so future usages won't parse the json again.
   * Note: to correctly load dark mode (-night) resources, make sure you pass Activity as a context (instead of e.g. the application context).
   * The Activity won't be leaked.
   *
   * Pass null as the cache key to skip caching.
   */
  @WorkerThread
  public static LottieResult<LottieComposition> fromRawResSync(Context context, @RawRes int rawRes, @Nullable String cacheKey) {
    try {
      return fromJsonInputStreamSync(context.getResources().openRawResource(rawRes), cacheKey);
    } catch (Resources.NotFoundException e) {
      return new LottieResult<>(e);
    }
  }

  private static String rawResCacheKey(Context context, @RawRes int resId) {
    return "rawRes" + (isNightMode(context) ? "_night_" : "_day_") + resId;
  }

  /**
   * It is important to include day/night in the cache key so that if it changes, the cache won't return an animation from the wrong bucket.
   */
  private static boolean isNightMode(Context context) {
    int nightModeMasked = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
    return nightModeMasked == Configuration.UI_MODE_NIGHT_YES;
  }

  /**
   * Auto-closes the stream.
   *
   * @see #fromJsonInputStreamSync(InputStream, String, boolean)
   */
  public static LottieTask<LottieComposition> fromJsonInputStream(final InputStream stream, @Nullable final String cacheKey) {
    return cache(cacheKey, new Callable<LottieResult<LottieComposition>>() {
      @Override
      public LottieResult<LottieComposition> call() {
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
      return fromJsonReaderSync(of(buffer(source(stream))), cacheKey);
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
      @Override
      public LottieResult<LottieComposition> call() {
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
      @Override
      public LottieResult<LottieComposition> call() {
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


    ByteArrayInputStream stream = new ByteArrayInputStream(json.getBytes());
    return fromJsonReaderSync(of(buffer(source(stream))), cacheKey);
  }

  public static LottieTask<LottieComposition> fromJsonReader(final JsonReader reader, @Nullable final String cacheKey) {
    return cache(cacheKey, new Callable<LottieResult<LottieComposition>>() {
      @Override
      public LottieResult<LottieComposition> call() {
        return fromJsonReaderSync(reader, cacheKey);
      }
    });
  }


  @WorkerThread
  public static LottieResult<LottieComposition> fromJsonReaderSync(com.airbnb.lottie.parser.moshi.JsonReader reader, @Nullable String cacheKey) {
    return fromJsonReaderSyncInternal(reader, cacheKey, true);
  }


  private static LottieResult<LottieComposition> fromJsonReaderSyncInternal(
      com.airbnb.lottie.parser.moshi.JsonReader reader, @Nullable String cacheKey, boolean close) {
    try {
      LottieComposition composition = LottieCompositionMoshiParser.parse(reader);
      if (cacheKey != null) {
        LottieCompositionCache.getInstance().put(cacheKey, composition);
      }
      return new LottieResult<>(composition);
    } catch (Exception e) {
      return new LottieResult<>(e);
    } finally {
      if (close) {
        closeQuietly(reader);
      }
    }
  }


  public static LottieTask<LottieComposition> fromZipStream(final ZipInputStream inputStream, @Nullable final String cacheKey) {
    return cache(cacheKey, new Callable<LottieResult<LottieComposition>>() {
      @Override
      public LottieResult<LottieComposition> call() {
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
        final String entryName = entry.getName();
        if (entryName.contains("__MACOSX")) {
          inputStream.closeEntry();
        } else if (entry.getName().contains(".json")) {
          com.airbnb.lottie.parser.moshi.JsonReader reader = of(buffer(source(inputStream)));
          composition = LottieCompositionFactory.fromJsonReaderSyncInternal(reader, null, false).getValue();
        } else if (entryName.contains(".png") || entryName.contains(".webp")) {
          String[] splitName = entryName.split("/");
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
        imageAsset.setBitmap(Utils.resizeBitmapIfNeeded(e.getValue(), imageAsset.getWidth(), imageAsset.getHeight()));
      }
    }

    // Ensure that all bitmaps have been set.
    for (Map.Entry<String, LottieImageAsset> entry : composition.getImages().entrySet()) {
      if (entry.getValue().getBitmap() == null) {
        return new LottieResult<>(new IllegalStateException("There is no image for " + entry.getValue().getFileName()));
      }
    }

    if (cacheKey != null) {
      LottieCompositionCache.getInstance().put(cacheKey, composition);
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

  /**
   * First, check to see if there are any in-progress tasks associated with the cache key and return it if there is.
   * If not, create a new task for the callable.
   * Then, add the new task to the task cache and set up listeners so it gets cleared when done.
   */
  private static LottieTask<LottieComposition> cache(
      @Nullable final String cacheKey, Callable<LottieResult<LottieComposition>> callable) {
    final LottieComposition cachedComposition = cacheKey == null ? null : LottieCompositionCache.getInstance().get(cacheKey);
    if (cachedComposition != null) {
      return new LottieTask<>(new Callable<LottieResult<LottieComposition>>() {
        @Override
        public LottieResult<LottieComposition> call() {
          return new LottieResult<>(cachedComposition);
        }
      });
    }
    if (cacheKey != null && taskCache.containsKey(cacheKey)) {
      return taskCache.get(cacheKey);
    }

    LottieTask<LottieComposition> task = new LottieTask<>(callable);
    if (cacheKey != null) {
      task.addListener(new LottieListener<LottieComposition>() {
        @Override
        public void onResult(LottieComposition result) {
          taskCache.remove(cacheKey);
        }
      });
      task.addFailureListener(new LottieListener<Throwable>() {
        @Override
        public void onResult(Throwable result) {
          taskCache.remove(cacheKey);
        }
      });
      taskCache.put(cacheKey, task);
    }
    return task;
  }
}
