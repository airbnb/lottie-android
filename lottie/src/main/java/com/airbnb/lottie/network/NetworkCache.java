package com.airbnb.lottie.network;


import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.WorkerThread;

import com.airbnb.lottie.utils.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Helper class to save and restore animations fetched from an URL to the app disk cache.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class NetworkCache {

  @NonNull
  private final LottieNetworkCacheProvider cacheProvider;

  public NetworkCache(@NonNull LottieNetworkCacheProvider cacheProvider) {
    this.cacheProvider = cacheProvider;
  }

  public void clear() {
    File parentDir = parentDir();
    if (parentDir.exists()) {
      File[] files = parentDir.listFiles();
      if (files != null && files.length > 0) {
        for (File file : files) {
          file.delete();
        }
      }
      parentDir.delete();
    }
  }

  /**
   * If the animation doesn't exist in the cache, null will be returned.
   * <p>
   * Once the animation is successfully parsed, {@link #renameTempFile(String, FileExtension)} must be
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
   * to an composition, {@link #renameTempFile(String, FileExtension)} should be called to move the file
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
   * If the file created by {@link #writeTempCacheFile(String, InputStream, FileExtension)} was successfully parsed,
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
    File file = cacheProvider.getCacheDir();
    if (file.isFile()) {
      file.delete();
    }
    if (!file.exists()) {
      file.mkdirs();
    }
    return file;
  }

  private static String filenameForUrl(String url, FileExtension extension, boolean isTemp) {
    String prefix = "lottie_cache_";
    String suffix = (isTemp ? extension.tempExtension() : extension.extension);
    String sanitizedUrl = url.replaceAll("\\W+", "");
    // The max filename on Android is 255 chars.
    int maxUrlLength = 255 - prefix.length() - suffix.length();
    if (sanitizedUrl.length() > maxUrlLength) {
      // If the url is too long, use md5 as the cache key instead.
      // md5 is preferable to substring because it is impossible to know
      // which parts of the url are significant. If it is the end chars
      // then substring could cause multiple animations to use the same
      // cache key.
      // md5 is probably better for everything but:
      //     1. It is slower and unnecessary in most cases.
      //     2. Upon upgrading, if the cache key algorithm changes,
      //        all old cached animations will get orphaned.
      sanitizedUrl = getMD5(sanitizedUrl, maxUrlLength);
    }

    return prefix + sanitizedUrl + suffix;
  }

  private static String getMD5(String input, int maxLength) {
    MessageDigest md;
    try {
      md = MessageDigest.getInstance("MD5");
    } catch (NoSuchAlgorithmException e) {
      // For some reason, md5 doesn't exist, return a substring.
      // This should never happen.
      return input.substring(0, maxLength);
    }
    byte[] messageDigest = md.digest(input.getBytes());
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < messageDigest.length; i++) {
      byte b = messageDigest[i];
      sb.append(String.format("%02x", b));
    }
    return sb.toString();
  }
}
