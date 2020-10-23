package com.airbnb.lottie.network;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import androidx.annotation.NonNull;

public class DefaultLottieNetworkFetcher implements LottieNetworkFetcher {

  @Override
  @NonNull
  public LottieFetchResult fetchSync(@NonNull String url) throws IOException {
    final HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
    connection.setRequestMethod("GET");
    connection.connect();
    return new DefaultLottieFetchResult(connection);
  }
}
