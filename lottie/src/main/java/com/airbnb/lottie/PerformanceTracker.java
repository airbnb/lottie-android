package com.airbnb.lottie;

import android.support.v4.util.Pair;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PerformanceTracker {

  private boolean enabled = false;
  private Map<String, MeanCalculator> layerRenderTimes = new HashMap<>();
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

  void recordRenderTime(String layerName, float millis) {
    if (!enabled) {
      return;
    }
    MeanCalculator meanCalculator = layerRenderTimes.get(layerName);
    if (meanCalculator == null) {
      meanCalculator = new MeanCalculator();
      layerRenderTimes.put(layerName, meanCalculator);
    }
    meanCalculator.add(millis);
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

  @SuppressWarnings("WeakerAccess") public List<Pair<String, Float>> getSortedRenderTimes() {
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
