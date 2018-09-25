package com.airbnb.lottie;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.concurrent.Callable;
import java.util.concurrent.Semaphore;

import static org.mockito.Mockito.*;

public class LottieTaskTest extends BaseTest {

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
        .addListener(successListener)
        .addFailureListener(failureListener);
    verify(successListener, times(1)).onResult(5);
    verifyZeroInteractions(failureListener);
  }

  @Test
  public void testException() {
    final IllegalStateException exception = new IllegalStateException("foo");
    LottieTask<Integer> task = new LottieTask<>(new Callable<LottieResult<Integer>>() {
      @Override public LottieResult<Integer> call() {
        throw exception;
      }
    }, true)
        .addListener(successListener)
        .addFailureListener(failureListener);
    verifyZeroInteractions(successListener);
    verify(failureListener, times(1)).onResult(exception);
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
        .addFailureListener(failureListener)
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
    verifyZeroInteractions(failureListener);
  }

  @Test
  public void testAddListenerAfter() {
    LottieTask<Integer> task = new LottieTask<>(new Callable<LottieResult<Integer>>() {
      @Override public LottieResult<Integer> call() {
        return new LottieResult<>(5);
      }
    }, true);

    task.addListener(successListener);
    task.addFailureListener(failureListener);
    verify(successListener, times(1)).onResult(5);
    verifyZeroInteractions(failureListener);
  }
}
