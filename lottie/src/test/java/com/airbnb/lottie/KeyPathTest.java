package com.airbnb.lottie;

import android.app.Application;
import android.support.annotation.Nullable;

import com.airbnb.lottie.model.KeyPath;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.List;
import java.util.concurrent.Semaphore;

@RunWith(RobolectricTestRunner.class)
public class KeyPathTest {

  LottieDrawable lottieDrawable;

  @Before
  public void setupDrawable() {
    Application context = RuntimeEnvironment.application;
    final Semaphore semaphore = new Semaphore(0);
    lottieDrawable = new LottieDrawable();
    LottieComposition.Factory.fromRawFile(
        context, R.raw.squares, new OnCompositionLoadedListener() {
          @Override public void onCompositionLoaded(@Nullable LottieComposition composition) {
            lottieDrawable.setComposition(composition);
            semaphore.release();
          }
        }
    );
    try {
      semaphore.acquire();
    } catch (InterruptedException e) {
      throw new IllegalStateException(e);
    }
  }

  @Test
  public void testFullyQualified() {
    KeyPath keyPath = new KeyPath("Shape Layer 1", "Group 1", "Rectangle", "Stroke");
    List<KeyPath> resolvedKeyPaths = lottieDrawable.resolveKeyPath(keyPath);
    Assert.assertEquals(1, resolvedKeyPaths.size());
  }
}
