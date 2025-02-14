package com.airbnb.lottie;

import static org.junit.Assert.assertNotNull;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.test.platform.app.InstrumentationRegistry;

import com.airbnb.lottie.network.LottieFetchResult;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.InputStream;
import java.util.Objects;
import java.util.concurrent.Executor;

public class LottieInitializeTest extends BaseTest {

  @Rule
  public final TemporaryFolder temporaryFolder1 = new TemporaryFolder();

  @Rule
  public final TemporaryFolder temporaryFolder2 = new TemporaryFolder();

  private final Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();

  private final Executor originalExecutor = LottieTask.EXECUTOR;

  @Before
  public void setExecutor() {
    LottieTask.EXECUTOR = Runnable::run;
  }

  @After
  public void resetExecutor() {
    LottieTask.EXECUTOR = originalExecutor;
  }

  @Test
  public void fetchAfterSecondInitialize() {
    initializeLottie(temporaryFolder1);
    // Fetching here causes the resource to be cached in temporaryFolder1:
    LottieResult<LottieComposition> result1 = LottieCompositionFactory.fromUrlSync(context, "resources://test1.json");
    assertNotNull(result1.getValue());

    // Manually delete to simulate the end of a test:
    temporaryFolder1.delete();

    initializeLottie(temporaryFolder2);
    // Fetching here fails if L.setCacheProvider doesn't reset both its internal networkFetcher and its internal networkCache, because
    // temporaryFolder1 has been deleted:
    LottieResult<LottieComposition> result2 = LottieCompositionFactory.fromUrlSync(context, "resources://test1.json");
    assertNotNull(result2.getValue());
  }

  private void initializeLottie(TemporaryFolder temporaryFolder) {
    LottieConfig lottieConfig = new LottieConfig.Builder()
        .setNetworkCacheDir(temporaryFolder.getRoot())
        .setNetworkFetcher(url -> {
          if (url.startsWith("resources://")) {
            InputStream stream = Objects.requireNonNull(getClass().getClassLoader())
                .getResourceAsStream(url.substring("resources://".length()));
            if (stream != null) {
              return new LottieFetchSuccess(stream);
            }
          }

          return new LottieFetchFailure("Could not load <$url>");
        })
        .build();
    Lottie.initialize(lottieConfig);
  }

  private static class LottieFetchSuccess implements LottieFetchResult {

    @NonNull private final InputStream jsonStream;

    LottieFetchSuccess(@NonNull InputStream jsonStream) {
      this.jsonStream = jsonStream;
    }

    @Override public boolean isSuccessful() {
      return true;
    }

    @Override @NonNull public InputStream bodyByteStream() {
      return jsonStream;
    }

    @Override public String contentType() {
      return "application/json";
    }

    @Override @Nullable public String error() {
      return null;
    }

    @Override public void close() {
      // No-op
    }
  }

  private static class LottieFetchFailure implements LottieFetchResult {

    @NonNull private final String errorMessage;

    LottieFetchFailure(@NonNull String errorMessage) {
      this.errorMessage = errorMessage;
    }

    @Override public boolean isSuccessful() {
      return false;
    }

    @Override @NonNull public InputStream bodyByteStream() {
      throw new RuntimeException("LottieFetchFailure has no body");
    }

    @Override @Nullable public String contentType() {
      return null;
    }

    @Override public String error() {
      return errorMessage;
    }

    @Override public void close() {
      // No-op
    }
  }
}
