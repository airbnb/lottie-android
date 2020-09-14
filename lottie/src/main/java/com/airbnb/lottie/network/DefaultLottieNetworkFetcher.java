package com.airbnb.lottie.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class DefaultLottieNetworkFetcher implements LottieNetworkFetcher {

  @Nullable
  private HttpURLConnection connection;

  @Override
  @NonNull
  public LottieNetworkResult fetchSync(@NonNull String url) throws IOException {
    connection = (HttpURLConnection) new URL(url).openConnection();
    connection.setRequestMethod("GET");

    connection.connect();

    if (connection.getErrorStream() != null || connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
      String error = getErrorFromConnection(connection);
      return new LottieNetworkResult.Error("Unable to fetch " + url + ". Failed with " + connection.getResponseCode() + "\n" + error,
          connection.getResponseCode());
    }

    return new LottieNetworkResult.Success(connection.getInputStream(), connection.getContentType());
  }

  @Override public void disconnect() {
    if (connection != null) {
      connection.disconnect();
    }
  }

  private String getErrorFromConnection(HttpURLConnection connection) throws IOException {
    BufferedReader r = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
    StringBuilder error = new StringBuilder();
    String line;

    try {
      while ((line = r.readLine()) != null) {
        error.append(line).append('\n');
      }
    } catch (Exception e) {
      throw e;
    } finally {
      try {
        r.close();
      } catch (Exception e) {
        // Do nothing.
      }
    }
    return error.toString();
  }
}
