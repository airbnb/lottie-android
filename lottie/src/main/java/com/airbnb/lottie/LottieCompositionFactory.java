package com.airbnb.lottie;

import static com.airbnb.lottie.utils.Utils.closeQuietly;
import static okio.Okio.buffer;
import static okio.Okio.source;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RawRes;
import androidx.annotation.WorkerThread;

import com.airbnb.lottie.model.Font;
import com.airbnb.lottie.model.LottieCompositionCache;
import com.airbnb.lottie.network.NetworkCache;
import com.airbnb.lottie.parser.LottieCompositionMoshiParser;
import com.airbnb.lottie.parser.moshi.JsonReader;
import com.airbnb.lottie.utils.Logger;
import com.airbnb.lottie.utils.Utils;

import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import okio.BufferedSource;
import okio.Okio;
import okio.Source;

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
  private static final Set<LottieTaskIdleListener> taskIdleListeners = new HashSet<>();

  /**
   * reference magic bytes for zip compressed files.
   * useful to determine if an InputStream is a zip file or not
   */
  private static final byte[] ZIP_MAGIC = new byte[]{0x50, 0x4b, 0x03, 0x04};
  private static final byte[] GZIP_MAGIC = new byte[]{0x1f, (byte) 0x8b, 0x08};


  private LottieCompositionFactory() {
  }

  /**
   * Set the maximum number of compositions to keep cached in memory.
   * This must be {@literal >} 0.
   */
  public static void setMaxCacheSize(int size) {
    LottieCompositionCache.getInstance().resize(size);
  }

  public static void clearCache(Context context) {
    taskCache.clear();
    LottieCompositionCache.getInstance().clear();
    final NetworkCache networkCache = L.networkCache(context);
    if (networkCache != null) {
      networkCache.clear();
    }
  }

  /**
   * Use this to register a callback for when the composition factory is idle or not.
   * This can be used to provide data to an espresso idling resource.
   * Refer to FragmentVisibilityTests and its LottieIdlingResource in the Lottie repo for
   * an example.
   */
  public static void registerLottieTaskIdleListener(LottieTaskIdleListener listener) {
    taskIdleListeners.add(listener);
    listener.onIdleChanged(taskCache.size() == 0);
  }

  public static void unregisterLottieTaskIdleListener(LottieTaskIdleListener listener) {
    taskIdleListeners.remove(listener);
  }

  /**
   * Fetch an animation from an http url. Once it is downloaded once, Lottie will cache the file to disk for
   * future use. Because of this, you may call `fromUrl` ahead of time to warm the cache if you think you
   * might need an animation in the future.
   * <p>
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
    return cache(cacheKey, () -> {
      LottieResult<LottieComposition> result = L.networkFetcher(context).fetchSync(context, url, cacheKey);
      if (cacheKey != null && result.getValue() != null) {
        LottieCompositionCache.getInstance().put(cacheKey, result.getValue());
      }
      return result;
    }, null);
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
    final LottieComposition cachedComposition = cacheKey == null ? null : LottieCompositionCache.getInstance().get(cacheKey);
    if (cachedComposition != null) {
      return new LottieResult<>(cachedComposition);
    }
    LottieResult<LottieComposition> result = L.networkFetcher(context).fetchSync(context, url, cacheKey);
    if (cacheKey != null && result.getValue() != null) {
      LottieCompositionCache.getInstance().put(cacheKey, result.getValue());
    }
    return result;
  }

  /**
   * Parse an animation from src/main/assets. It is recommended to use {@link #fromRawRes(Context, int)} instead.
   * The asset file name will be used as a cache key so future usages won't have to parse the json again.
   * However, if your animation has images, you may package the json and images as a single flattened zip file in assets.
   * <p>
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
   * <p>
   * Pass null as the cache key to skip the cache.
   *
   * @see #fromZipStream(ZipInputStream, String)
   */
  public static LottieTask<LottieComposition> fromAsset(Context context, final String fileName, @Nullable final String cacheKey) {
    // Prevent accidentally leaking an Activity.
    final Context appContext = context.getApplicationContext();
    return cache(cacheKey, () -> fromAssetSync(appContext, fileName, cacheKey), null);
  }

  /**
   * Parse an animation from src/main/assets. It is recommended to use {@link #fromRawRes(Context, int)} instead.
   * The asset file name will be used as a cache key so future usages won't have to parse the json again.
   * However, if your animation has images, you may package the json and images as a single flattened zip file in assets.
   * <p>
   * To skip the cache, add null as a third parameter.
   *
   * @see #fromZipStreamSync(Context, ZipInputStream, String)
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
   * <p>
   * Pass null as the cache key to skip the cache.
   *
   * @see #fromZipStreamSync(Context, ZipInputStream, String)
   */
  @WorkerThread
  public static LottieResult<LottieComposition> fromAssetSync(Context context, String fileName, @Nullable String cacheKey) {
    final LottieComposition cachedComposition = cacheKey == null ? null : LottieCompositionCache.getInstance().get(cacheKey);
    if (cachedComposition != null) {
      return new LottieResult<>(cachedComposition);
    }
    try {
      BufferedSource source = Okio.buffer(source(context.getAssets().open(fileName)));
      if (isZipCompressed(source)) {
        return fromZipStreamSync(context, new ZipInputStream(source.inputStream()), cacheKey);
      } else if (isGzipCompressed(source)) {
        return fromJsonInputStreamSync(new GZIPInputStream(source.inputStream()), cacheKey);
      }
      return fromJsonInputStreamSync(source.inputStream(), cacheKey);
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
   * <p>
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
   * <p>
   * Pass null as the cache key to skip caching.
   */
  public static LottieTask<LottieComposition> fromRawRes(Context context, @RawRes final int rawRes, @Nullable final String cacheKey) {
    // Prevent accidentally leaking an Activity.
    final WeakReference<Context> contextRef = new WeakReference<>(context);
    final Context appContext = context.getApplicationContext();
    return cache(cacheKey, () -> {
      @Nullable Context originalContext = contextRef.get();
      Context context1 = originalContext != null ? originalContext : appContext;
      return fromRawResSync(context1, rawRes, cacheKey);
    }, null);
  }

  /**
   * Parse an animation from raw/res. This is recommended over putting your animation in assets because
   * it uses a hard reference to R.
   * The resource id will be used as a cache key so future usages won't parse the json again.
   * Note: to correctly load dark mode (-night) resources, make sure you pass Activity as a context (instead of e.g. the application context).
   * The Activity won't be leaked.
   * <p>
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
   * <p>
   * Pass null as the cache key to skip caching.
   */
  @WorkerThread
  public static LottieResult<LottieComposition> fromRawResSync(Context context, @RawRes int rawRes, @Nullable String cacheKey) {
    final LottieComposition cachedComposition = cacheKey == null ? null : LottieCompositionCache.getInstance().get(cacheKey);
    if (cachedComposition != null) {
      return new LottieResult<>(cachedComposition);
    }
    try {
      BufferedSource source = Okio.buffer(source(context.getResources().openRawResource(rawRes)));
      if (isZipCompressed(source)) {
        return fromZipStreamSync(context, new ZipInputStream(source.inputStream()), cacheKey);
      } else if (isGzipCompressed(source)) {
        try {
          return fromJsonInputStreamSync(new GZIPInputStream(source.inputStream()), cacheKey);
        } catch (IOException e) {
          // This shouldn't happen because we check the header for magic bytes.
          return new LottieResult<>(e);
        }
      }
      return fromJsonInputStreamSync(source.inputStream(), cacheKey);
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
    return cache(cacheKey, () -> fromJsonInputStreamSync(stream, cacheKey), () -> closeQuietly(stream));
  }

  /**
   * @see #fromJsonInputStreamSync(InputStream, String, boolean)
   */
  public static LottieTask<LottieComposition> fromJsonInputStream(final InputStream stream, @Nullable final String cacheKey, boolean close) {
    return cache(cacheKey, () -> fromJsonInputStreamSync(stream, cacheKey, close), () -> {
      if (close) {
        closeQuietly(stream);
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

  /**
   * Return a LottieComposition for the given InputStream to json.
   */
  @WorkerThread
  public static LottieResult<LottieComposition> fromJsonInputStreamSync(InputStream stream, @Nullable String cacheKey, boolean close) {
    return fromSourceSync(source(stream), cacheKey, close);
  }

  /**
   * @see #fromJsonSync(JSONObject, String)
   */
  @Deprecated
  public static LottieTask<LottieComposition> fromJson(final JSONObject json, @Nullable final String cacheKey) {
    return cache(cacheKey, () -> {
      //noinspection deprecation
      return fromJsonSync(json, cacheKey);
    }, null);
  }

  /**
   * Prefer passing in the json string directly. This method just calls `toString()` on your JSONObject.
   * If you are loading this animation from the network, just use the response body string instead of
   * parsing it first for improved performance.
   */
  @Deprecated
  @WorkerThread
  public static LottieResult<LottieComposition> fromJsonSync(final JSONObject json, @Nullable String cacheKey) {
    return fromJsonStringSync(json.toString(), cacheKey);
  }

  /**
   * @see #fromJsonStringSync(String, String)
   */
  public static LottieTask<LottieComposition> fromJsonString(final String json, @Nullable final String cacheKey) {
    return cache(cacheKey, () -> fromJsonStringSync(json, cacheKey), null);
  }

  /**
   * Return a LottieComposition for the specified raw json string.
   * If loading from a file, it is preferable to use the InputStream or rawRes version.
   */
  @WorkerThread
  public static LottieResult<LottieComposition> fromJsonStringSync(String json, @Nullable String cacheKey) {
    ByteArrayInputStream stream = new ByteArrayInputStream(json.getBytes());
    return fromSourceSync(source(stream), cacheKey);
  }

  public static LottieTask<LottieComposition> fromSource(final Source source, @Nullable final String cacheKey) {
    return cache(cacheKey, () -> fromSourceSync(source, cacheKey), () -> Utils.closeQuietly(source));
  }

  @WorkerThread
  public static LottieResult<LottieComposition> fromSourceSync(final Source source, @Nullable String cacheKey) {
    return fromJsonReaderSync(JsonReader.of(buffer(source)), cacheKey);
  }

  @WorkerThread
  public static LottieResult<LottieComposition> fromSourceSync(final Source source, @Nullable String cacheKey,
      boolean close) {
    return fromJsonReaderSyncInternal(JsonReader.of(buffer(source)), cacheKey, close);
  }

  public static LottieTask<LottieComposition> fromJsonReader(final JsonReader reader, @Nullable final String cacheKey) {
    return cache(cacheKey, () -> fromJsonReaderSync(reader, cacheKey), () -> Utils.closeQuietly(reader));
  }

  @WorkerThread
  public static LottieResult<LottieComposition> fromJsonReaderSync(final JsonReader reader, @Nullable String cacheKey) {
    return fromJsonReaderSync(reader, cacheKey, true);
  }

  @WorkerThread
  public static LottieResult<LottieComposition> fromJsonReaderSync(final JsonReader reader, @Nullable String cacheKey,
      boolean close) {
    return fromJsonReaderSyncInternal(reader, cacheKey, close);
  }

  private static LottieResult<LottieComposition> fromJsonReaderSyncInternal(
      JsonReader reader, @Nullable String cacheKey, boolean close) {
    try {
      final LottieComposition cachedComposition = cacheKey == null ? null : LottieCompositionCache.getInstance().get(cacheKey);
      if (cachedComposition != null) {
        return new LottieResult<>(cachedComposition);
      }
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

  /**
   * In this overload, embedded fonts will NOT be parsed. If your zip file has custom fonts, use the overload
   * that takes Context as the first parameter.
   */
  public static LottieTask<LottieComposition> fromZipStream(final ZipInputStream inputStream, @Nullable final String cacheKey) {
    return fromZipStream(null, inputStream, cacheKey);
  }

  /**
   * In this overload, embedded fonts will NOT be parsed. If your zip file has custom fonts, use the overload
   * that takes Context as the first parameter.
   */
  public static LottieTask<LottieComposition> fromZipStream(final ZipInputStream inputStream, @Nullable final String cacheKey, boolean close) {
    return fromZipStream(null, inputStream, cacheKey, close);
  }

  /**
   * @see #fromZipStreamSync(Context, ZipInputStream, String)
   */
  public static LottieTask<LottieComposition> fromZipStream(Context context, final ZipInputStream inputStream, @Nullable final String cacheKey) {
    return cache(cacheKey, () -> fromZipStreamSync(context, inputStream, cacheKey), () -> closeQuietly(inputStream));
  }

  /**
   * @see #fromZipStreamSync(Context, ZipInputStream, String)
   */
  public static LottieTask<LottieComposition> fromZipStream(Context context, final ZipInputStream inputStream,
      @Nullable final String cacheKey, boolean close) {
    return cache(cacheKey, () -> fromZipStreamSync(context, inputStream, cacheKey), close ? () -> closeQuietly(inputStream) : null);
  }

  /**
   * Parses a zip input stream into a Lottie composition.
   * Your zip file should just be a folder with your json file and images zipped together.
   * It will automatically store and configure any images inside the animation if they exist.
   * <p>
   * In this overload, embedded fonts will NOT be parsed. If your zip file has custom fonts, use the overload
   * that takes Context as the first parameter.
   * <p>
   * The ZipInputStream will be automatically closed at the end. If you would like to keep it open, use the overload
   * with a close parameter and pass in false.
   */
  public static LottieResult<LottieComposition> fromZipStreamSync(ZipInputStream inputStream, @Nullable String cacheKey) {
    return fromZipStreamSync(inputStream, cacheKey, true);
  }

  /**
   * Parses a zip input stream into a Lottie composition.
   * Your zip file should just be a folder with your json file and images zipped together.
   * It will automatically store and configure any images inside the animation if they exist.
   * <p>
   * In this overload, embedded fonts will NOT be parsed. If your zip file has custom fonts, use the overload
   * that takes Context as the first parameter.
   */
  public static LottieResult<LottieComposition> fromZipStreamSync(ZipInputStream inputStream, @Nullable String cacheKey, boolean close) {
    return fromZipStreamSync(null, inputStream, cacheKey, close);
  }

  /**
   * Parses a zip input stream into a Lottie composition.
   * Your zip file should just be a folder with your json file and images zipped together.
   * It will automatically store and configure any images inside the animation if they exist.
   * <p>
   * The ZipInputStream will be automatically closed at the end. If you would like to keep it open, use the overload
   * with a close parameter and pass in false.
   *
   * @param context is optional and only needed if your zip file contains ttf or otf fonts. If yours doesn't, you may pass null.
   *                Embedded fonts may be .ttf or .otf files, can be in subdirectories, but must have the same name as the
   *                font family (fFamily) in your animation file.
   */
  @WorkerThread
  public static LottieResult<LottieComposition> fromZipStreamSync(@Nullable Context context, ZipInputStream inputStream, @Nullable String cacheKey) {
    return fromZipStreamSync(context, inputStream, cacheKey, true);
  }

  /**
   * Parses a zip input stream into a Lottie composition.
   * Your zip file should just be a folder with your json file and images zipped together.
   * It will automatically store and configure any images inside the animation if they exist.
   *
   * @param context is optional and only needed if your zip file contains ttf or otf fonts. If yours doesn't, you may pass null.
   *                Embedded fonts may be .ttf or .otf files, can be in subdirectories, but must have the same name as the
   *                font family (fFamily) in your animation file.
   */
  @WorkerThread
  public static LottieResult<LottieComposition> fromZipStreamSync(@Nullable Context context, ZipInputStream inputStream,
      @Nullable String cacheKey, boolean close) {
    try {
      return fromZipStreamSyncInternal(context, inputStream, cacheKey);
    } finally {
      if (close) {
        closeQuietly(inputStream);
      }
    }
  }

  @WorkerThread
  private static LottieResult<LottieComposition> fromZipStreamSyncInternal(Context context, ZipInputStream inputStream, @Nullable String cacheKey) {
    LottieComposition composition = null;
    Map<String, Bitmap> images = new HashMap<>();
    Map<String, Typeface> fonts = new HashMap<>();

    try {
      final LottieComposition cachedComposition = cacheKey == null ? null : LottieCompositionCache.getInstance().get(cacheKey);
      if (cachedComposition != null) {
        return new LottieResult<>(cachedComposition);
      }
      ZipEntry entry = inputStream.getNextEntry();
      while (entry != null) {
        final String entryName = entry.getName();
        if (entryName.contains("__MACOSX")) {
          inputStream.closeEntry();
        } else if (entry.getName().equalsIgnoreCase("manifest.json")) { //ignore .lottie manifest
          inputStream.closeEntry();
        } else if (entry.getName().contains(".json")) {
          JsonReader reader = JsonReader.of(buffer(source(inputStream)));
          composition = LottieCompositionFactory.fromJsonReaderSyncInternal(reader, null, false).getValue();
        } else if (entryName.contains(".png") || entryName.contains(".webp") || entryName.contains(".jpg") || entryName.contains(".jpeg")) {
          String[] splitName = entryName.split("/");
          String name = splitName[splitName.length - 1];
          images.put(name, BitmapFactory.decodeStream(inputStream));
        } else if (entryName.contains(".ttf") || entryName.contains(".otf")) {
          String[] splitName = entryName.split("/");
          String fileName = splitName[splitName.length - 1];
          String fontFamily = fileName.split("\\.")[0];
          File tempFile = new File(context.getCacheDir(), fileName);
          FileOutputStream fos = new FileOutputStream(tempFile);
          try {
            try (OutputStream output = new FileOutputStream(tempFile)) {
              byte[] buffer = new byte[4 * 1024];
              int read;
              while ((read = inputStream.read(buffer)) != -1) {
                output.write(buffer, 0, read);
              }
              output.flush();
            }
          } catch (Throwable e) {
            Logger.warning("Unable to save font " + fontFamily + " to the temporary file: " + fileName + ". ", e);
          }
          Typeface typeface = Typeface.createFromFile(tempFile);
          if (!tempFile.delete()) {
            Logger.warning("Failed to delete temp font file " + tempFile.getAbsolutePath() + ".");
          }
          fonts.put(fontFamily, typeface);
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

    for (Map.Entry<String, Typeface> e : fonts.entrySet()) {
      boolean found = false;
      for (Font font : composition.getFonts().values()) {
        if (font.getFamily().equals(e.getKey())) {
          found = true;
          font.setTypeface(e.getValue());
        }
      }
      if (!found) {
        Logger.warning("Parsed font for " + e.getKey() + " however it was not found in the animation.");
      }
    }

    if (images.isEmpty()) {
      for (Map.Entry<String, LottieImageAsset> entry : composition.getImages().entrySet()) {
        LottieImageAsset asset = entry.getValue();
        if (asset == null) {
          return null;
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
            Logger.warning("data URL did not have correct base64 format.", e);
            return null;
          }
          Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, opts);
          bitmap = Utils.resizeBitmapIfNeeded(bitmap, asset.getWidth(), asset.getHeight());
          asset.setBitmap(bitmap);
        }
      }
    }

    if (cacheKey != null) {
      LottieCompositionCache.getInstance().put(cacheKey, composition);
    }
    return new LottieResult<>(composition);
  }

  /**
   * Check if a given InputStream points to a .zip compressed file
   */
  private static Boolean isZipCompressed(BufferedSource inputSource) {
    return matchesMagicBytes(inputSource, ZIP_MAGIC);
  }

  /**
   * Check if a given InputStream points to a .gzip compressed file
   */
  private static Boolean isGzipCompressed(BufferedSource inputSource) {
    return matchesMagicBytes(inputSource, GZIP_MAGIC);
  }

  private static Boolean matchesMagicBytes(BufferedSource inputSource, byte[] magic) {
    try {
      BufferedSource peek = inputSource.peek();
      for (byte b : magic) {
        if (peek.readByte() != b) {
          return false;
        }
      }
      peek.close();
      return true;
    } catch (NoSuchMethodError e) {
      // This happens in the Android Studio layout preview.
      return false;
    } catch (Exception e) {
      Logger.error("Failed to check zip file header", e);
      return false;
    }
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
  private static LottieTask<LottieComposition> cache(@Nullable final String cacheKey, Callable<LottieResult<LottieComposition>> callable,
      @Nullable Runnable onCached) {
    LottieTask<LottieComposition> task = null;
    final LottieComposition cachedComposition = cacheKey == null ? null : LottieCompositionCache.getInstance().get(cacheKey);
    if (cachedComposition != null) {
      task = new LottieTask<>(cachedComposition);
    }
    if (cacheKey != null && taskCache.containsKey(cacheKey)) {
      task = taskCache.get(cacheKey);
    }
    if (task != null) {
      if (onCached != null) {
        onCached.run();
      }
      return task;
    }

    task = new LottieTask<>(callable);
    if (cacheKey != null) {
      AtomicBoolean resultAlreadyCalled = new AtomicBoolean(false);
      task.addListener(result -> {
        taskCache.remove(cacheKey);
        resultAlreadyCalled.set(true);
        if (taskCache.size() == 0) {
          notifyTaskCacheIdleListeners(true);
        }
      });
      task.addFailureListener(result -> {
        taskCache.remove(cacheKey);
        resultAlreadyCalled.set(true);
        if (taskCache.size() == 0) {
          notifyTaskCacheIdleListeners(true);
        }
      });
      // It is technically possible for the task to finish and for the listeners to get called
      // before this code runs. If this happens, the task will be put in taskCache but never removed.
      // This would require this thread to be sleeping at exactly this point in the code
      // for long enough for the task to finish and call the listeners. Unlikely but not impossible.
      if (!resultAlreadyCalled.get()) {
        taskCache.put(cacheKey, task);
        if (taskCache.size() == 1) {
          notifyTaskCacheIdleListeners(false);
        }
      }
    }
    return task;
  }

  private static void notifyTaskCacheIdleListeners(boolean idle) {
    List<LottieTaskIdleListener> listeners = new ArrayList<>(taskIdleListeners);
    for (int i = 0; i < listeners.size(); i++) {
      listeners.get(i).onIdleChanged(idle);
    }
  }
}
