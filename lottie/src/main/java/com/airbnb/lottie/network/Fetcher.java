package com.airbnb.lottie.network;

import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

public interface Fetcher {
    @WorkerThread
    @NonNull
    Result fetchSync(@NonNull String url) throws IOException;
}
