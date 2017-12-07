package com.airbnb.lottie;

import android.content.Context;
import android.content.res.Resources;
import android.os.Environment;

import com.airbnb.happo.SnapshotProvider;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class LottieSnapshotProvider extends SnapshotProvider {

  private static final float[] PROGRESS = {0f, 0.01f, 0.25f, 0.5f, 0.75f, 0.99f, 1f};
  private static final int CORES = Runtime.getRuntime().availableProcessors();

  private final BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();
  private final ThreadPoolExecutor executor = new ThreadPoolExecutor(CORES, CORES, 15, TimeUnit.MINUTES, queue);
  private final Context context;

  private int remainingTasks = 0;

  LottieSnapshotProvider(Context context) {
    this.context = context;
  }

  @Override
  public void beginSnapshotting() {
    String[] animations;
    try {
      animations = context.getAssets().list("");
    } catch (IOException e) {
      onError(e);
      return;
    }

//    animations = Arrays.copyOfRange(animations, 10, 30);

    File dir = new File(Environment.getExternalStorageDirectory() + "/Snapshots");
    //noinspection ResultOfMethodCallIgnored
    dir.mkdirs();
    for (File file : dir.listFiles()) {
      //noinspection ResultOfMethodCallIgnored
      file.delete();
    }
    for (final String animation : animations) {
      if (!animation.contains(".json")) {
        continue;
      }
      remainingTasks += 1;
      executor.execute(new Runnable() {
        @Override
        public void run() {
          runAnimation(animation);
          decrementAndCompleteIfDone();
        }
      });
    }
  }

  private void runAnimation(final String name) {
    LottieComposition composition = LottieComposition.Factory.fromFileSync(context, name);
    if (composition.getBounds().width() > Resources.getSystem().getDisplayMetrics().widthPixels ||
        composition.getBounds().height() > Resources.getSystem().getDisplayMetrics().heightPixels) {
      return;
    }
    drawComposition(composition, name);
  }

  private void drawComposition(LottieComposition composition, String name) {
    LottieAnimationView view = new LottieAnimationView(context);
    view.setComposition(composition);
    for (float progress : PROGRESS) {
      view.setProgress(progress);
      recordSnapshot(view, 1080, "android", name, Integer.toString((int) (progress * 100)));
    }
  }

  @Override
  public void stopSnapshotting() {
    queue.clear();
  }

  private void decrementAndCompleteIfDone() {
    remainingTasks--;
    if (remainingTasks < 0) {
      throw new IllegalStateException("Remaining tasks cannot be negative.");
    }
    if (remainingTasks == 0) {
      onComplete();
    }
  }
}
