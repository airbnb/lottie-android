package com.airbnb.lottie.network;

import android.content.Context;
import android.support.annotation.WorkerThread;
import android.support.v4.util.Pair;
import android.util.Base64;
import android.util.Log;

import com.airbnb.lottie.L;
import com.airbnb.lottie.LottieResult;
import com.airbnb.lottie.LottieTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;

class NetworkCache {
  private static final String CACHE_DIR = "lottie_cache_";

  private final Context appContext;
  private final String url;

  NetworkCache(Context appContext, String url) {
    this.appContext = appContext.getApplicationContext();
    this.url = url;
  }

  @WorkerThread
  LottieResult<Pair<FileExtension, InputStream>> fetch() {
    File cachedFile = null;
    try {
      cachedFile = getCachedFile(url);
    } catch (FileNotFoundException e) {
      return new LottieResult<>(e);
    }
    if (cachedFile == null) {
      return new LottieResult<>(new IllegalStateException("Tried to retrive " + url + " from cache but it doesn't exit."));
    }

    FileInputStream inputStream;
    try {
      inputStream = new FileInputStream(cachedFile);
    } catch (FileNotFoundException e) {
      return new LottieResult<>(e);
    }

    FileExtension extension;
    if (cachedFile.getAbsolutePath().endsWith(".zip")) {
      extension = FileExtension.Zip;
    } else {
      extension = FileExtension.Json;
    }

    L.debug("Cache hit for " + url + " at " + cachedFile.getAbsolutePath());
    return new LottieResult<>(new Pair<>(extension, (InputStream) inputStream));
  }

  private File getCachedFile(String url) throws FileNotFoundException {
    File jsonFile = new File(appContext.getCacheDir(), filenameForUrl(url, FileExtension.Json, false));
    if (jsonFile.exists()) {
      return jsonFile;
    }
    File zipFile = new File(appContext.getCacheDir(), filenameForUrl(url, FileExtension.Zip, false));
    if (zipFile.exists()) {
      return zipFile;
    }
    return null;
  }

  private static String filenameForUrl(String url, FileExtension extension, boolean isTemp) {
    String extensionString = isTemp ? extension.extension : extension.tempExtension();
    return CACHE_DIR + url.replaceAll("\\W+", "") + extension;
  }

  File writeTempCacheFile(InputStream stream, FileExtension extension) throws IOException {
    return writeCacheFile(stream, extension);
  }

  private File writeCacheFile(InputStream stream, FileExtension extension) throws IOException {
    String fileName = filenameForUrl(url, extension, true);
    File file = new File(appContext.getCacheDir(), fileName);
    try {
      OutputStream output = new FileOutputStream(file);
      //noinspection TryFinallyCanBeTryWithResources
      try {
        byte[] buffer = new byte[1024];
        int read;

        while ((read = stream.read(buffer)) != -1) {
          output.write(buffer, 0, read);
        }

        output.flush();
      } finally {
        output.close();
      }
    } finally {
      stream.close();
    }
    return file;
  }

  void copyTempFileToRealFile(String url, FileExtension extension) {
    String fileName = filenameForUrl(url, extension, true);
    File file = new File(appContext.getCacheDir(), fileName);
    String newFileName = file.getAbsolutePath().replace(".temp", "");
    File newFile = new File(newFileName);
    boolean renamed = file.renameTo(newFile);
    L.debug("Copying temp file to real file (" + newFile + ")");
    if (!renamed) {
      L.warn( "Unable to rename cache file " + file.getAbsolutePath() + " to " + newFile.getAbsolutePath() + ".");
    }
  }
}
