package com.airbnb.lottie.network;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.support.v4.util.Pair;
import android.util.Base64;
import android.util.Log;

import com.airbnb.lottie.L;
import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.LottieCompositionFactory;
import com.airbnb.lottie.LottieImageAsset;
import com.airbnb.lottie.LottieResult;
import com.airbnb.lottie.LottieTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class NetworkFetcher {

  private final Context appContext;
  private final String url;

  private final NetworkCache networkCache;

  public static LottieTask<LottieComposition> fetch(Context context, String url) {
    return new NetworkFetcher(context, url).fetch();
  }

  private NetworkFetcher(Context context, String url) {
    appContext = context.getApplicationContext();
    this.url = url;
    networkCache = new NetworkCache(appContext, url);
  }

  private LottieTask<LottieComposition> fetch() {
    return new LottieTask<>(new Callable<LottieResult<LottieComposition>>() {
      @Override public LottieResult<LottieComposition> call() throws Exception {
        return fetchSync();
      }
    });
  }

  @WorkerThread
  private LottieResult<LottieComposition> fetchSync() {
    LottieResult<LottieComposition> result = fetchFromCache();
    if (result.getValue() != null) {
      return result;
    }

    L.debug("Animation for " + url + " not found in cache. Fetching from network.");
    return fetchFromNetwork();
  }

  private LottieResult<LottieComposition> fetchFromCache() {
    LottieResult<Pair<FileExtension, InputStream>> cacheResult = networkCache.fetch();
    if (cacheResult.getValue() == null) {
      return new LottieResult<>(new IllegalArgumentException(url + " Does not exist in the cache. Fetching."));
    }

    FileExtension extension = cacheResult.getValue().first;
    InputStream inputStream = cacheResult.getValue().second;
    if (extension == FileExtension.Zip) {
      return LottieCompositionFactory.fromZipStreamSync(new ZipInputStream(inputStream), url);
    } else {
      return LottieCompositionFactory.fromJsonInputStreamSync(inputStream, url);
    }
  }

  private LottieResult<LottieComposition> fetchFromNetwork() {
    try {
      return fetchFromNetworkInternal();
    } catch (IOException e) {
      return new LottieResult<>(e);
    }
  }

  private LottieResult fetchFromNetworkInternal() throws IOException {
    L.debug( "Fetching " + url);
    HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();

    connection.setRequestMethod("GET");
    connection.connect();

    File file;
    FileExtension extension;
    LottieResult<LottieComposition> result;
    switch (connection.getContentType()) {
      case "application/zip":
        L.debug("Handling zip response.");
        extension = FileExtension.Zip;
        file = networkCache.writeTempCacheFile(connection.getInputStream(), extension);
        result = LottieCompositionFactory.fromZipStreamSync(new ZipInputStream(new FileInputStream(file)), url);
        break;
      case "application/json":
      default:
        L.debug("Received json response.");
        extension = FileExtension.Json;
        file = networkCache.writeTempCacheFile(connection.getInputStream(), extension);
        result = LottieCompositionFactory.fromJsonInputStreamSync(new FileInputStream(new File(file.getAbsolutePath())), url);
        break;
    }

    if (result.getValue() != null) {
      networkCache.copyTempFileToRealFile(url, extension);
    }

    L.debug("Completed fetch from network. Success: " + (result.getValue() != null));
    return result;
  }
}
