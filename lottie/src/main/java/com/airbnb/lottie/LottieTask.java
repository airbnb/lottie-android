package com.airbnb.lottie;

import android.os.Handler;
import android.os.Looper;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import android.util.Log;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

/**
 * Helper to run asynchronous tasks with a result.
 * Results can be obtained with {@link #addListener(LottieListener)}.
 * Failures can be obtained with {@link #addFailureListener(LottieListener)}.
 *
 * A task will produce a single result or a single failure.
 */
public class LottieTask<T> {

  /**
   * Set this to change the executor that LottieTasks are run on. This will be the executor that composition parsing and url
   * fetching happens on.
   *
   * You may change this to run deserialization synchronously for testing.
   */
  @SuppressWarnings("WeakerAccess")
  public static Executor EXECUTOR = Executors.newCachedThreadPool();

  @Nullable private Thread taskObserver;

  /* Preserve add order. */
  private final Set<LottieListener<T>> successListeners = new LinkedHashSet<>(1);
  private final Set<LottieListener<Throwable>> failureListeners = new LinkedHashSet<>(1);
  private final Handler handler = new Handler(Looper.getMainLooper());
  private final FutureTask<LottieResult<T>> task;

  @Nullable private volatile LottieResult<T> result = null;

  @RestrictTo(RestrictTo.Scope.LIBRARY)
  public LottieTask(Callable<LottieResult<T>> runnable) {
    this(runnable, false);
  }

  /**
   * runNow is only used for testing.
   */
  @RestrictTo(RestrictTo.Scope.LIBRARY)
  LottieTask(Callable<LottieResult<T>> runnable, boolean runNow) {
    task = new FutureTask<>(runnable);

    if (runNow) {
      try {
        setResult(runnable.call());
      } catch (Throwable e) {
        setResult(new LottieResult<T>(e));
      }
    } else {
      EXECUTOR.execute(task);
      startTaskObserverIfNeeded();
    }
  }

  private void setResult(@Nullable LottieResult<T> result) {
    if (this.result != null) {
      throw new IllegalStateException("A task may only be set once.");
    }
    this.result = result;
    notifyListeners();
  }

  /**
   * Add a task listener. If the task has completed, the listener will be called synchronously.
   * @return the task for call chaining.
   */
  public synchronized LottieTask<T> addListener(LottieListener<T> listener) {
    if (result != null && result.getValue() != null) {
      listener.onResult(result.getValue());
    }

    successListeners.add(listener);
    startTaskObserverIfNeeded();
    return this;
  }

  /**
   * Remove a given task listener. The task will continue to execute so you can re-add
   * a listener if neccesary.
   * @return the task for call chaining.
   */
  public synchronized LottieTask<T> removeListener(LottieListener<T> listener) {
    successListeners.remove(listener);
    stopTaskObserverIfNeeded();
    return this;
  }

  /**
   * Add a task failure listener. This will only be called in the even that an exception
   * occurs. If an exception has already occurred, the listener will be called immediately.
   * @return the task for call chaining.
   */
  public synchronized LottieTask<T> addFailureListener(LottieListener<Throwable> listener) {
    if (result != null && result.getException() != null) {
      listener.onResult(result.getException());
    }

    failureListeners.add(listener);
    startTaskObserverIfNeeded();
    return this;
  }

  /**
   * Remove a given task failure listener. The task will continue to execute so you can re-add
   * a listener if neccesary.
   * @return the task for call chaining.
   */
  public synchronized LottieTask<T> removeFailureListener(LottieListener<Throwable> listener) {
    failureListeners.remove(listener);
    stopTaskObserverIfNeeded();
    return this;
  }

  private void notifyListeners() {
    // Listeners should be called on the main thread.
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
    // Allows listeners to remove themselves in onResult.
    // Otherwise we risk ConcurrentModificationException.
    List<LottieListener<T>> listenersCopy = new ArrayList<>(successListeners);
    for (LottieListener<T> l : listenersCopy) {
      l.onResult(value);
    }
  }

  private void notifyFailureListeners(Throwable e) {
    // Allows listeners to remove themselves in onResult.
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

  /**
   * We monitor the task with an observer thread to determine when it is done and should notify
   * the appropriate listeners.
   */
  private synchronized void startTaskObserverIfNeeded() {
    if (taskObserverAlive() || result != null) {
      return;
    }
    taskObserver = new Thread("LottieTaskObserver") {
      private boolean taskComplete = false;

      @Override public void run() {
        while (true) {
          if (isInterrupted() || taskComplete) {
            return;
          }
          if (task.isDone()) {
            try {
              setResult(task.get());
            } catch (InterruptedException | ExecutionException e) {
              setResult(new LottieResult<T>(e));
            }
            taskComplete = true;
            stopTaskObserverIfNeeded();
          }
        }
      }
    };
    taskObserver.start();
    L.debug("Starting TaskObserver thread");
  }

  /**
   * We can stop observing the task if there are no more listeners or if the task is complete.
   */
  private synchronized void stopTaskObserverIfNeeded() {
    if (!taskObserverAlive()) {
      return;
    }
    if (successListeners.isEmpty() || result != null) {
      taskObserver.interrupt();
      taskObserver = null;
      L.debug("Stopping TaskObserver thread");
    }
  }

  private boolean taskObserverAlive() {
    return taskObserver != null && taskObserver.isAlive();
  }
}
