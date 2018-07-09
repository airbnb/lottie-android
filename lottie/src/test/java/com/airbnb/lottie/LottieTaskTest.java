package com.airbnb.lottie;

import android.util.Log;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.concurrent.Callable;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verifyZeroInteractions;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class LottieTaskTest {

  @Test
  public void testListener() {
    LottieTaskListener<Integer> listener = mock(LottieTaskListener.class);
    LottieTask<Integer> task = new LottieTask<>(new Callable<LottieResult<Integer>>() {
      @Override public LottieResult<Integer> call() {
        return new LottieResult<>(5);
      }
    }, true)
        .addListener(listener);
    Mockito.verify(listener, times(1)).onResult(new LottieResult<Integer>(5));
  }

  @Test
  public void testException() {
    LottieTaskListener<Integer> listener = mock(LottieTaskListener.class);
    LottieTask<Integer> task = new LottieTask<>(new Callable<LottieResult<Integer>>() {
      @Override public LottieResult<Integer> call() {
        throw new IllegalStateException("foo");
      }
    }, true)
        .addListener(listener);
    Mockito.verify(listener, times(1)).onResult(new LottieResult<Integer>(new IllegalStateException("foo")));
  }

  @Test
  public void testRemoveListener() {
    final Semaphore lock = new Semaphore(0);
    LottieTaskListener<Integer> listener = mock(LottieTaskListener.class);
    LottieTask<Integer> task = new LottieTask<>(new Callable<LottieResult<Integer>>() {
      @Override public LottieResult<Integer> call() {
        return new LottieResult<>(5);
      }
    })
        .addListener(listener)
        .addListener(new LottieTaskListener<Integer>() {
          @Override public void onResult(LottieResult<Integer> result) {
            lock.release();
          }
        });
    task.removeListener(listener);
    try {
      lock.acquire();
    } catch (InterruptedException e) {
      throw new IllegalStateException(e);
    }
    verifyZeroInteractions(listener);
  }

  @Test
  public void testAddListenerAfter() {
    LottieTask<Integer> task = new LottieTask<>(new Callable<LottieResult<Integer>>() {
      @Override public LottieResult<Integer> call() {
        return new LottieResult<>(5);
      }
    }, true);

    LottieTaskListener<Integer> listener = mock(LottieTaskListener.class);
    task.addListener(listener);
    Mockito.verify(listener, times(1)).onResult(new LottieResult<Integer>(5));
  }
}
