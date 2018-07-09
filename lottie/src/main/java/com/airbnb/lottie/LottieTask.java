package com.airbnb.lottie;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.support.annotation.VisibleForTesting;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class LottieTask<T> {

  public enum LottieTaskState {
    Loading,
    Success,
    Fail
  }

  @Nullable private Thread taskObserver;

  /* Preserve add order. */
  private final Set<LottieTaskListener<T>> listeners = new LinkedHashSet<>(1);
  private final Handler handler = new Handler(Looper.getMainLooper());
  private final FutureTask<LottieResult<T>> task;

  @Nullable private LottieResult<T> result = null;
  private boolean throwOnException = false;

  public LottieTask(Callable<LottieResult<T>> runnable) {
    this(runnable, false);
  }

  @VisibleForTesting
  public LottieTask(Callable<LottieResult<T>> runnable, boolean runNow) {
    task = new FutureTask<>(runnable);

    if (runNow) {
      try {
        result = runnable.call();
      } catch (Exception e) {
        result = new LottieResult<>(e);
      }
      notifyListeners();
    } else {
      task.run();
      startTaskObserverIfNeeded();
    }
  }

  public LottieTaskState getState() {
    if (result == null) {
      return LottieTaskState.Loading;
    } else if (result.getException() != null) {
      return LottieTaskState.Fail;
    } else {
      return LottieTaskState.Success;
    }
  }

  @RestrictTo(RestrictTo.Scope.LIBRARY)
  public void setValue(T value) {
    result = new LottieResult<>(value);
    notifyListeners();
  }

  @Nullable public T getValue() {
    if (result == null) {
      return null;
    }
    return result.getValue();
  }

  @RestrictTo(RestrictTo.Scope.LIBRARY)
  public void setException(Throwable exception) {
    if (throwOnException) {
      throw new IllegalStateException("LottieTask failed!", exception);
    }
    this.result = new LottieResult<>(exception);
    notifyListeners();
  }

  @Nullable public Throwable getException() {
    if (result == null) {
      return null;
    }
    return result.getException();
  }

  public LottieTask<T> addListener(LottieTaskListener<T> listener) {
    if (getState() != LottieTaskState.Loading) {
      listener.onResult(result);
    }
    synchronized (listeners) {
      listeners.add(listener);
    }
    startTaskObserverIfNeeded();
    return this;
  }

  public LottieTask<T> removeListener(LottieTaskListener<T> listener) {
    synchronized (listeners) {
      listeners.remove(listener);
    }
    stopTaskObserverIfNeeded();
    return this;
  }

  public void throwOnException() {
    throwOnException = true;
    if (result != null && result.getException() != null) {
      throw new IllegalStateException("LottieTask failed!", result.getException());
    }
  }

  private void notifyListeners() {
    handler.post(new Runnable() {
      @Override public void run() {
        if (task.isCancelled()) {
          return;
        }
        // Allow listeners to remove themself in onResult. Otherwise we risk ConcurrentModificationException.
        List<LottieTaskListener<T>> listenersCopy = new ArrayList<>(listeners);
        for (LottieTaskListener<T> l : listenersCopy) {
          l.onResult(result);
        }
      }
    });
  }

  private void stopTaskObserverIfNeeded() {
    if (!taskObserverAlive()) {
      return;
    }
    if (listeners.isEmpty() || result != null) {
      taskObserver.interrupt();
      if (L.DBG) {
        Log.d(L.TAG, "Stopping TaskObserver thread");
      }
    }
  }

  private void startTaskObserverIfNeeded() {
    if (taskObserverAlive() || result != null) {
      return;
    }
    taskObserver = new Thread("LottieTaskObserver") {
      @Override public void run() {
        if (isInterrupted()) {
          return;
        }
        if (task.isDone()) {
          try {
            result = task.get();
          } catch (InterruptedException | ExecutionException e) {
            result = new LottieResult<>(e);
          }
          notifyListeners();
          stopTaskObserverIfNeeded();
        }
      }
    };
    taskObserver.start();
    if (L.DBG) {
      Log.d(L.TAG, "Starting TaskObserver thread");
    }
  }

  private boolean taskObserverAlive() {
    return taskObserver != null && taskObserver.isAlive();
  }
}
