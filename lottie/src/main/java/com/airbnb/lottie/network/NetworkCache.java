package com.airbnb.lottie.network;

import android.content.Context;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.core.util.Pair;

import com.airbnb.lottie.utils.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Helper class to save and restore animations fetched from an URL to the app disk cache.
 */
public class NetworkCache {
  private final Context appContext;

  public NetworkCache(Context appContext) {
    this.appContext = appContext.getApplicationContext();
  }

  public void clear() {
    File parentDir = parentDir();
    if (parentDir.exists()) {
      File[] files = parentDir.listFiles();
      if (files != null && files.length > 0) {
        for (File file : parentDir.listFiles()) {
          file.delete();
        }
      }
      parentDir.delete();
    }
  }

  /**
   * If the animation doesn't exist in the cache, null will be returned.
   *
   * Once the animation is successfully parsed, {@link #renameTempFile(FileExtension)} must be
   * called to move the file from a temporary location to its permanent cache location so it can
   * be used in the future.
   */
  @Nullable
  @WorkerThread
  Pair<FileExtension, InputStream> fetch(String url) {
    File cachedFile;
    try {
      cachedFile = getCachedFile(url);
    } catch (FileNotFoundException e) {
      return null;
    }
    if (cachedFile == null) {
      return null;
    }

    FileInputStream inputStream;
    try {
      inputStream = new FileInputStream(cachedFile);
    } catch (FileNotFoundException e) {
      return null;
    }

    FileExtension extension;
    if (cachedFile.getAbsolutePath().endsWith(".zip")) {
      extension = FileExtension.ZIP;
    } else {
      extension = FileExtension.JSON;
    }

    Logger.debug("Cache hit for " + url + " at " + cachedFile.getAbsolutePath());
    return new Pair<>(extension, (InputStream) inputStream);
  }

  /**
   * Writes an InputStream from a network response to a temporary file. If the file successfully parses
   * to an composition, {@link #renameTempFile(FileExtension)} should be called to move the file
   * to its final location for future cache hits.
   */
  File writeTempCacheFile(String url, InputStream stream, FileExtension extension) throws IOException {
    String fileName = filenameForUrl(url, extension, true);
    File file = new File(parentDir(), fileName);
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

  /**
   * If the file created by {@link #writeTempCacheFile(InputStream, FileExtension)} was successfully parsed,
   * this should be called to remove the temporary part of its name which will allow it to be a cache hit in the future.
   */
  void renameTempFile(String url, FileExtension extension) {
    String fileName = filenameForUrl(url, extension, true);
    File file = new File(parentDir(), fileName);
    String newFileName = file.getAbsolutePath().replace(".temp", "");
    File newFile = new File(newFileName);
    boolean renamed = file.renameTo(newFile);
    Logger.debug("Copying temp file to real file (" + newFile + ")");
    if (!renamed) {
      Logger.warning("Unable to rename cache file " + file.getAbsolutePath() + " to " + newFile.getAbsolutePath() + ".");
    }
  }

  /**
   * Returns the cache file for the given url if it exists. Checks for both json and zip.
   * Returns null if neither exist.
   */
  @Nullable
  private File getCachedFile(String url) throws FileNotFoundException {
    File jsonFile = new File(parentDir(), filenameForUrl(url, FileExtension.JSON, false));
    if (jsonFile.exists()) {
      return jsonFile;
    }
    File zipFile = new File(parentDir(), filenameForUrl(url, FileExtension.ZIP, false));
    if (zipFile.exists()) {
      return zipFile;
    }
    return null;
  }

  private File parentDir() {
    File file = new File(appContext.getCacheDir(), "lottie_network_cache");
    if (file.isFile()) {
      file.delete();
    }
    if (!file.exists()) {
      file.mkdirs();
    }
    return file;
  }

  private static String filenameForUrl(String url, FileExtension extension, boolean isTemp) {
    return "lottie_cache_" + url.replaceAll("\\W+", "") + (isTemp ? extension.tempExtension(): extension.extension);
  }
}
