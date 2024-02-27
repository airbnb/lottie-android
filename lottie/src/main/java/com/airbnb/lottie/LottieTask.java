package com.airbnb.lottie;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;

import com.airbnb.lottie.utils.Logger;
import com.airbnb.lottie.utils.LottieThreadFactory;

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
 * <p>
 * A task will produce a single result or a single failure.
 */
@SuppressWarnings("UnusedReturnValue")
public class LottieTask<T> {

  /**
   * Set this to change the executor that LottieTasks are run on. This will be the executor that composition parsing and url
   * fetching happens on.
   * <p>
   * You may change this to run deserialization synchronously for testing.
   */
  @SuppressWarnings("WeakerAccess")
  public static Executor EXECUTOR = Executors.newCachedThreadPool(new LottieThreadFactory());

  /* Preserve add order. */
  private final Set<LottieListener<T>> successListeners = new LinkedHashSet<>(1);
  private final Set<LottieListener<Throwable>> failureListeners = new LinkedHashSet<>(1);
  private final Handler handler = new Handler(Looper.getMainLooper());

  @Nullable private volatile LottieResult<T> result = null;

  @RestrictTo(RestrictTo.Scope.LIBRARY)
  public LottieTask(Callable<LottieResult<T>> runnable) {
    this(runnable, false);
  }

  public LottieTask(T result) {
    setResult(new LottieResult<>(result));
  }

  /**
   * runNow is only used for testing.
   */
  @RestrictTo(RestrictTo.Scope.LIBRARY) LottieTask(Callable<LottieResult<T>> runnable, boolean runNow) {
    if (runNow) {
      try {
        setResult(runnable.call());
      } catch (Throwable e) {
        setResult(new LottieResult<>(e));
      }
    } else {
      EXECUTOR.execute(new LottieFutureTask<T>(this, runnable));
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
   *
   * @return the task for call chaining.
   */
  public synchronized LottieTask<T> addListener(LottieListener<T> listener) {
    LottieResult<T> result = this.result;
    if (result != null && result.getValue() != null) {
      listener.onResult(result.getValue());
    }

    successListeners.add(listener);
    return this;
  }

  /**
   * Remove a given task listener. The task will continue to execute so you can re-add
   * a listener if necessary.
   *
   * @return the task for call chaining.
   */
  public synchronized LottieTask<T> removeListener(LottieListener<T> listener) {
    successListeners.remove(listener);
    return this;
  }

  /**
   * Add a task failure listener. This will only be called in the even that an exception
   * occurs. If an exception has already occurred, the listener will be called immediately.
   *
   * @return the task for call chaining.
   */
  public synchronized LottieTask<T> addFailureListener(LottieListener<Throwable> listener) {
    LottieResult<T> result = this.result;
    if (result != null && result.getException() != null) {
      listener.onResult(result.getException());
    }

    failureListeners.add(listener);
    return this;
  }

  /**
   * Remove a given task failure listener. The task will continue to execute so you can re-add
   * a listener if necessary.
   *
   * @return the task for call chaining.
   */
  public synchronized LottieTask<T> removeFailureListener(LottieListener<Throwable> listener) {
    failureListeners.remove(listener);
    return this;
  }

  @Nullable
  public LottieResult<T> getResult() {
    return result;
  }

  private void notifyListeners() {
    // Listeners should be called on the main thread.
    if (Looper.myLooper() == Looper.getMainLooper()) {
      notifyListenersInternal();
    } else {
      handler.post(this::notifyListenersInternal);
    }
  }

  private void notifyListenersInternal() {
    // Local reference in case it gets set on a background thread.
    LottieResult<T> result = LottieTask.this.result;
    if (result == null) {
      return;
    }
    if (result.getValue() != null) {
      notifySuccessListeners(result.getValue());
    } else {
      notifyFailureListeners(result.getException());
    }
  }

  private synchronized void notifySuccessListeners(T value) {
    // Allows listeners to remove themselves in onResult.
    // Otherwise we risk ConcurrentModificationException.
    List<LottieListener<T>> listenersCopy = new ArrayList<>(successListeners);
    for (LottieListener<T> l : listenersCopy) {
      l.onResult(value);
    }
  }

  private synchronized void notifyFailureListeners(Throwable e) {
    // Allows listeners to remove themselves in onResult.
    // Otherwise we risk ConcurrentModificationException.
    List<LottieListener<Throwable>> listenersCopy = new ArrayList<>(failureListeners);
    if (listenersCopy.isEmpty()) {
      Logger.warning("Lottie encountered an error but no failure listener was added:", e);
      return;
    }

    for (LottieListener<Throwable> l : listenersCopy) {
      l.onResult(e);
    }
  }

  private static class LottieFutureTask<T> extends FutureTask<LottieResult<T>> {

    private LottieTask<T> lottieTask;

    LottieFutureTask(LottieTask<T> task, Callable<LottieResult<T>> callable) {
      super(callable);
      lottieTask = task;
    }

    @Override
    protected void done() {
      try {
        if (isCancelled()) {
          // We don't need to notify and listeners if the task is cancelled.
          return;
        }

        try {
          lottieTask.setResult(get());
        } catch (InterruptedException | ExecutionException e) {
          lottieTask.setResult(new LottieResult<>(e));
        }
      } finally {
        // LottieFutureTask can be held in memory for up to 60 seconds after the task is done, which would
        // result in holding on to the associated LottieTask instance and leaking its listeners. To avoid
        // that, we clear our the reference to the LottieTask instance.
        //
        // How is LottieFutureTask held for up to 60 seconds? It's a bug in how the VM cleans up stack
        // local variables. When you have a loop that polls a blocking queue and assigns the result
        // to a local variable, after looping the local variable will still reference the previous value
        // until the queue returns the next result.
        //
        // Executors.newCachedThreadPool() relies on a SynchronousQueue and creates a cached thread pool
        // with a default keep alice of 60 seconds. After a given worker thread runs a task, that thread
        // will wait for up to 60 seconds for a new task to come, and while waiting it's also accidentally
        // keeping a reference to the previous task.
        //
        // See commit d577e728e9bccbafc707af3060ea914caa73c14f in AOSP for how that was fixed for Looper.
        lottieTask = null;
      }
    }
  }
}
