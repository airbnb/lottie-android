package com.airbnb.lottie;

import androidx.collection.ArraySet;
import androidx.core.util.Pair;
import android.util.Log;

import com.airbnb.lottie.utils.MeanCalculator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PerformanceTracker {

  public interface FrameListener {
    void onFrameRendered(float renderTimeMs);
  }

  private boolean enabled = false;
  private final Set<FrameListener> frameListeners = new ArraySet<>();
  private final Map<String, MeanCalculator> layerRenderTimes = new HashMap<>();
  private final Comparator<Pair<String, Float>> floatComparator =
      new Comparator<Pair<String, Float>>() {
    @Override public int compare(Pair<String, Float> o1, Pair<String, Float> o2) {
      float r1 = o1.second;
      float r2 = o2.second;
      if (r2 > r1) {
        return 1;
      } else if (r1 > r2) {
        return -1;
      }
      return 0;
    }
  };

  void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public void recordRenderTime(String layerName, float millis) {
    if (!enabled) {
      return;
    }
    MeanCalculator meanCalculator = layerRenderTimes.get(layerName);
    if (meanCalculator == null) {
      meanCalculator = new MeanCalculator();
      layerRenderTimes.put(layerName, meanCalculator);
    }
    meanCalculator.add(millis);

    if (layerName.equals("__container")) {
      for (FrameListener listener : frameListeners) {
        listener.onFrameRendered(millis);
      }
    }
  }

  public void addFrameListener(FrameListener frameListener) {
    frameListeners.add(frameListener);
  }

  @SuppressWarnings("unused") public void removeFrameListener(FrameListener frameListener) {
    frameListeners.add(frameListener);
  }

  public void clearRenderTimes() {
    layerRenderTimes.clear();
  }

  public void logRenderTimes() {
    if (!enabled) {
      return;
    }
    List<Pair<String, Float>> sortedRenderTimes = getSortedRenderTimes();
    Log.d(L.TAG, "Render times:");
    for (int i = 0; i < sortedRenderTimes.size(); i++) {
      Pair<String, Float> layer = sortedRenderTimes.get(i);
      Log.d(L.TAG, String.format("\t\t%30s:%.2f", layer.first, layer.second));
    }
  }

  public List<Pair<String, Float>> getSortedRenderTimes() {
    if (!enabled) {
      return Collections.emptyList();
    }
    List<Pair<String, Float>> sortedRenderTimes = new ArrayList<>(layerRenderTimes.size());
    for (Map.Entry<String, MeanCalculator> e : layerRenderTimes.entrySet()) {
      sortedRenderTimes.add(new Pair<>(e.getKey(), e.getValue().getMean()));
    }
    Collections.sort(sortedRenderTimes, floatComparator);
    return sortedRenderTimes;
  }
}
