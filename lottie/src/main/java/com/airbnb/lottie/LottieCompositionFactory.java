package com.airbnb.lottie;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.RawRes;
import android.support.annotation.WorkerThread;
import android.util.JsonReader;

import com.airbnb.lottie.parser.LottieCompositionParser;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.concurrent.Callable;

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

  public static LottieTask<LottieComposition> fromJsonInputStream(final InputStream stream) {
    return new LottieTask<>(new Callable<LottieResult<LottieComposition>>() {
      @Override public LottieResult<LottieComposition> call() throws Exception {
        return fromJsonInputStreamSync(stream);
      }
    });
  }

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
}
