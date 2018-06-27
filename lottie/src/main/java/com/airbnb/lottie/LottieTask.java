package com.airbnb.lottie;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.FutureTask;

public class LottieTask<T> {

  public interface TaskListener<T> {
    void onResult(LottieResult<T> result);
  }

  public enum State {
    Loading,
    Success,
    Fail
  }

  private final Set<TaskListener<T>> listeners = new HashSet<>(1);
  private final Handler handler = new Handler(Looper.getMainLooper());
  private final FutureTask<LottieResult<T>> task;

  private State state = State.Loading;
  @Nullable private LottieResult<T> result = null;
  private boolean throwOnException = false;

  public LottieTask(Runnable runnable) {
    task = new FutureTask<>(runnable);
  }

  public State getState() {
    return state;
  }

  @RestrictTo(RestrictTo.Scope.LIBRARY)
  public void setValue(T value) {
    this.value = value;
    this.state = State.Success;
    notifyListeners();
  }

  @Nullable public T getValue() {
    return value;
  }

  @RestrictTo(RestrictTo.Scope.LIBRARY)
  public void setException(Throwable exception) {
    if (throwOnException) {
      throw new IllegalStateException("Task " + id + " failed!", exception);
    }
    this.exception = exception;
    this.state = State.Fail;
    notifyListeners();
  }

  @Nullable public Throwable getException() {
    return exception;
  }

  public void addListener(TaskListener<T> listener) {
    if (state != State.Loading) {
      listener.onResult(this);
    }
    synchronized (listeners) {
      listeners.add(listener);
    }
  }

  public void cancel() {
    cancelled = true;
  }

  public void removeListener(TaskListener<T> listener) {
    synchronized (listeners) {
      listeners.remove(listener);
    }
  }

  public void throwOnException() {
    throwOnException = true;
  }

  private void notifyListeners() {
    handler.post(new Runnable() {
      @Override public void run() {
        if (cancelled) {
          return;
        }
        synchronized (listeners) {
          for (TaskListener<T> s : listeners) {
            s.onResult(LottieTask.this);
          }
        }
      }
    });
  }
}
