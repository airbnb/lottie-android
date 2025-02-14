package com.airbnb.lottie;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

public class LottieTaskTest extends BaseTest {

  @Mock
  public LottieListener<Integer> successListener;
  @Mock
  public LottieListener<Throwable> failureListener;

  @Rule
  public MockitoRule rule = MockitoJUnit.rule();

  @Test
  public void testListener() {
    new LottieTask<>(() -> new LottieResult<>(5), true)
        .addListener(successListener)
        .addFailureListener(failureListener);
    verify(successListener, times(1)).onResult(5);
    verifyNoInteractions(failureListener);
  }

  @Test
  public void testException() {
    final IllegalStateException exception = new IllegalStateException("foo");
    new LottieTask<>((Callable<LottieResult<Integer>>) () -> {
      throw exception;
    }, true)
        .addListener(successListener)
        .addFailureListener(failureListener);
    verifyNoInteractions(successListener);
    verify(failureListener, times(1)).onResult(exception);
  }

  /**
   * This hangs on CI but not locally.
   */
  @Ignore("hangs on ci")
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
    verifyNoInteractions(successListener);
    verifyNoInteractions(failureListener);
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
    verifyNoInteractions(failureListener);
  }

  @Test
  public void executorIsRealThreadPoolByDefault() {
    AtomicBoolean isDirect = new AtomicBoolean();
    LottieTask.EXECUTOR.execute(() -> isDirect.set(true));
    assertFalse(isDirect.get());
  }

  @Test
  public void executorIsDirectWhenTestingPropertySetToTrue() throws Exception {
    // Use a custom ClassLoader to force a new class instance which will cause the
    // static initializers to run and observe the new system property set below.
    String lottieTaskName = LottieTask.class.getName();
    ClassLoader customLoader = new ClassLoader() {
      @Override public Class<?> loadClass(String name) throws ClassNotFoundException {
        if (name.equals(lottieTaskName)) {
          try (InputStream in = ClassLoader.getSystemResourceAsStream(lottieTaskName.replace('.', '/') + ".class")) {
            byte[] bytes = new byte[10 * 1024 * 1024];
            int read  = in.read(bytes);
            return defineClass(name, bytes, 0, read);
          } catch (IOException e) {
            throw new ClassNotFoundException(name, e);
          }
        }
        return super.loadClass(name);
      }
    };

    System.setProperty(LottieTask.DIRECT_EXECUTOR_PROPERTY_NAME, "true");
    try {
      Class<?> c = customLoader.loadClass(lottieTaskName);
      Executor e = (Executor) c.getField("EXECUTOR").get(null);

      AtomicBoolean isDirect = new AtomicBoolean();
      e.execute(() -> isDirect.set(true));
      assertTrue(isDirect.get());
    } finally {
      System.clearProperty(LottieTask.DIRECT_EXECUTOR_PROPERTY_NAME);
    }
  }
}
