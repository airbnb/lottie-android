package com.airbnb.lottie;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.concurrent.Callable;
import java.util.concurrent.Semaphore;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verifyZeroInteractions;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class LottieTaskTest {

  @Mock
  public LottieListener<Integer> successListener;
  @Mock
  public LottieListener<Throwable> failureListener;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testListener() {
    LottieTask<Integer> task = new LottieTask<>(new Callable<LottieResult<Integer>>() {
      @Override public LottieResult<Integer> call() {
        return new LottieResult<>(5);
      }
    }, true)
        .addListener(successListener);
    Mockito.verify(successListener, times(1)).onResult(5);
  }

  @Test
  public void testException() {
    final IllegalStateException exception = new IllegalStateException("foo");
    LottieTask<Integer> task = new LottieTask<>(new Callable<LottieResult<Integer>>() {
      @Override public LottieResult<Integer> call() {
        throw exception;
      }
    }, true)
        .addFailureListener(failureListener);
    Mockito.verify(failureListener, times(1)).onResult(exception);
  }

  /**
   * This hangs on CI but not locally.
   */
  @Ignore
  @Test
  public void testRemoveListener() {
    final Semaphore lock = new Semaphore(0);
    LottieTask<Integer> task = new LottieTask<>(new Callable<LottieResult<Integer>>() {
      @Override public LottieResult<Integer> call() {
        return new LottieResult<>(5);
      }
    })
        .addListener(successListener)
        .addListener(new LottieListener<Integer>() {
          @Override public void onResult(Integer result) {
            lock.release();
          }
        });
    task.removeListener(successListener);
    try {
      lock.acquire();
    } catch (InterruptedException e) {
      throw new IllegalStateException(e);
    }
    verifyZeroInteractions(successListener);
  }

  @Test
  public void testAddListenerAfter() {
    LottieTask<Integer> task = new LottieTask<>(new Callable<LottieResult<Integer>>() {
      @Override public LottieResult<Integer> call() {
        return new LottieResult<>(5);
      }
    }, true);

    task.addListener(successListener);
    Mockito.verify(successListener, times(1)).onResult(5);
  }
}
