package com.airbnb.lottie.network;

import android.util.Pair;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.LottieCompositionFactory;
import com.airbnb.lottie.LottieResult;
import com.airbnb.lottie.utils.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import java.util.zip.ZipInputStream;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

public class NetworkFetcher {

  @NonNull
  private final NetworkCache networkCache;
  @NonNull
  private final LottieNetworkFetcher fetcher;

  public NetworkFetcher(@NonNull NetworkCache networkCache, @NonNull LottieNetworkFetcher fetcher) {
    this.networkCache = networkCache;
    this.fetcher = fetcher;
  }

  @NonNull
  @WorkerThread
  public LottieResult<LottieComposition> fetchSync(@NonNull String url, @Nullable String cacheKey) {
    LottieComposition result = fetchFromCache(url, cacheKey);
    if (result != null) {
      return new LottieResult<>(result);
    }

    Logger.debug("Animation for " + url + " not found in cache. Fetching from network.");

    return fetchFromNetwork(url, cacheKey);
  }

  @Nullable
  @WorkerThread
  private LottieComposition fetchFromCache(@NonNull String url, @Nullable String cacheKey) {
    if (cacheKey == null) {
      return null;
    }
    Pair<FileExtension, InputStream> cacheResult = networkCache.fetch(url);
    if (cacheResult == null) {
      return null;
    }

    FileExtension extension = cacheResult.first;
    InputStream inputStream = cacheResult.second;
    LottieResult<LottieComposition> result;
    if (extension == FileExtension.ZIP) {
      result = LottieCompositionFactory.fromZipStreamSync(new ZipInputStream(inputStream), url);
    } else {
      result = LottieCompositionFactory.fromJsonInputStreamSync(inputStream, url);
    }
    if (result.getValue() != null) {
      return result.getValue();
    }
    return null;
  }

  @NonNull
  @WorkerThread
  private LottieResult<LottieComposition> fetchFromNetwork(@NonNull String url, @Nullable String cacheKey) {
    Logger.debug("Fetching " + url);

    LottieFetchResult fetchResult = null;
    try {
      fetchResult = fetcher.fetchSync(url);
      if (fetchResult.isSuccessful()) {
        InputStream inputStream = fetchResult.bodyByteStream();
        String contentType = fetchResult.contentType();
        LottieResult<LottieComposition> result = fromInputStream(url, inputStream, contentType, cacheKey);
        Logger.debug("Completed fetch from network. Success: " + (result.getValue() != null));
        return result;
      } else {
        return new LottieResult<>(new IllegalArgumentException(fetchResult.error()));
      }
    } catch (Exception e) {
      return new LottieResult<>(e);
    } finally {
      if (fetchResult != null) {
        try {
          fetchResult.close();
        } catch (IOException e) {
          Logger.warning("LottieFetchResult close failed ", e);
        }
      }
    }
  }

  @NonNull
  private LottieResult<LottieComposition> fromInputStream(@NonNull String url, @NonNull InputStream inputStream, @Nullable String contentType,
      @Nullable String cacheKey) throws IOException {
    FileExtension extension;
    LottieResult<LottieComposition> result;
    if (contentType == null) {
      // Assume JSON for best effort parsing. If it fails, it will just deliver the parse exception
      // in the result which is more useful than failing here.
      contentType = "application/json";
    }
    if (contentType.contains("application/zip") || url.split("\\?")[0].endsWith(".lottie")) {
      Logger.debug("Handling zip response.");
      extension = FileExtension.ZIP;
      result = fromZipStream(url, inputStream, cacheKey);
    } else {
      Logger.debug("Received json response.");
      extension = FileExtension.JSON;
      result = fromJsonStream(url, inputStream, cacheKey);
    }

    if (cacheKey != null && result.getValue() != null) {
      networkCache.renameTempFile(url, extension);
    }

    return result;
  }

  @NonNull
  private LottieResult<LottieComposition> fromZipStream(@NonNull String url, @NonNull InputStream inputStream, @Nullable String cacheKey)
      throws IOException {
    if (cacheKey == null) {
      return LottieCompositionFactory.fromZipStreamSync(new ZipInputStream(inputStream), null);
    }
    File file = networkCache.writeTempCacheFile(url, inputStream, FileExtension.ZIP);
    return LottieCompositionFactory.fromZipStreamSync(new ZipInputStream(new FileInputStream(file)), url);
  }

  @NonNull
  private LottieResult<LottieComposition> fromJsonStream(@NonNull String url, @NonNull InputStream inputStream, @Nullable String cacheKey)
      throws IOException {
    if (cacheKey == null) {
      return LottieCompositionFactory.fromJsonInputStreamSync(inputStream, null);
    }
    File file = networkCache.writeTempCacheFile(url, inputStream, FileExtension.JSON);
    return LottieCompositionFactory.fromJsonInputStreamSync(new FileInputStream(new File(file.getAbsolutePath())), url);
  }
}
