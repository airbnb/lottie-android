package com.airbnb.lottie;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.util.Log;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class LottieTask<T> {

  public enum State {
    Loading,
    Success,
    Fail
  }

  @Nullable private Thread taskObserver;

  /* Preserve add order. */
  private final Set<LottieListener<T>> successListeners = new LinkedHashSet<>(1);
  private final Set<LottieListener<Throwable>> failureListeners = new LinkedHashSet<>(1);
  private final Handler handler = new Handler(Looper.getMainLooper());
  private final FutureTask<LottieResult<T>> task;

  @Nullable private LottieResult<T> result = null;

  public LottieTask(Callable<LottieResult<T>> runnable) {
    this(runnable, false);
  }

  /**
   * runNow is only used for testing.
   */
  @VisibleForTesting
  public LottieTask(Callable<LottieResult<T>> runnable, boolean runNow) {
    task = new FutureTask<>(runnable);

    if (runNow) {
      try {
        setResult(runnable.call());
      } catch (Exception e) {
        setResult(new LottieResult<T>(e));
      }
    } else {
      task.run();
      startTaskObserverIfNeeded();
    }
  }

  public State getState() {
    if (result == null) {
      return State.Loading;
    } else if (result.getException() != null) {
      return State.Fail;
    } else {
      return State.Success;
    }
  }

  private void setResult(@Nullable LottieResult<T> result) {
    this.result = result;
    notifyListeners();
  }

  @Nullable public T getValue() {
    if (result == null) {
      return null;
    }
    return result.getValue();
  }

  @Nullable public Throwable getException() {
    if (result == null) {
      return null;
    }
    return result.getException();
  }

  public LottieTask<T> addListener(LottieListener<T> listener) {
    if (result != null && result.getValue() != null) {
      listener.onResult(result.getValue());
      return this;
    }

    synchronized (successListeners) {
      successListeners.add(listener);
    }
    startTaskObserverIfNeeded();
    return this;
  }

  public LottieTask<T> removeListener(LottieListener<T> listener) {
    synchronized (successListeners) {
      successListeners.remove(listener);
    }
    stopTaskObserverIfNeeded();
    return this;
  }

  public LottieTask<T> addFailureListener(LottieListener<Throwable> listener) {
    if (result != null && result.getException() != null) {
      listener.onResult(result.getException());
      return this;
    }

    synchronized (failureListeners) {
      failureListeners.add(listener);
    }
    startTaskObserverIfNeeded();
    return this;
  }

  public LottieTask<T> removeFailureListener(LottieListener<T> listener) {
    synchronized (failureListeners) {
      failureListeners.remove(listener);
    }
    stopTaskObserverIfNeeded();
    return this;
  }

  private void notifyListeners() {
    handler.post(new Runnable() {
      @Override public void run() {
        if (result == null || task.isCancelled()) {
          return;
        }
        // Local reference in case it gets set on a background thread.
        LottieResult<T> result = LottieTask.this.result;
        if (result.getValue() != null) {
          notifySuccessListeners(result.getValue());
        } else {
          notifyFailureListeners(result.getException());
        }
      }
    });
  }

  private void notifySuccessListeners(T value) {
    // Allow listeners to remove themself in onResult.
    // Otherwise we risk ConcurrentModificationException.
    List<LottieListener<T>> listenersCopy = new ArrayList<>(successListeners);
    for (LottieListener<T> l : listenersCopy) {
      l.onResult(value);
    }
  }

  private void notifyFailureListeners(Throwable e) {
    // Allow listeners to remove themself in onResult.
    // Otherwise we risk ConcurrentModificationException.
    List<LottieListener<Throwable>> listenersCopy = new ArrayList<>(failureListeners);
    if (listenersCopy.isEmpty()) {
      Log.w(L.TAG, "Lottie encountered an error but no failure listener was added.", e);
      return;
    }

    for (LottieListener<Throwable> l : listenersCopy) {
      l.onResult(e);
    }
  }

  private void stopTaskObserverIfNeeded() {
    if (!taskObserverAlive()) {
      return;
    }
    if (successListeners.isEmpty() || result != null) {
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
            setResult(task.get());
          } catch (InterruptedException | ExecutionException e) {
            setResult(new LottieResult<T>(e));
          }
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
