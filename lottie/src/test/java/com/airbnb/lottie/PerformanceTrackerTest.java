package com.airbnb.lottie;

import androidx.core.util.Pair;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static junit.framework.Assert.assertTrue;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class PerformanceTrackerTest {

  private PerformanceTracker performanceTracker;

  @Before
  public void setup() {
    performanceTracker = new PerformanceTracker();
    performanceTracker.setEnabled(true);
  }

  @Test
  public void testDisabled() {
    performanceTracker.setEnabled(false);
    performanceTracker.recordRenderTime("Hello", 16f);
    assertTrue(performanceTracker.getSortedRenderTimes().isEmpty());
  }

  @Test
  public void testOneFrame() {
    performanceTracker.recordRenderTime("Hello", 16f);
    List<Pair<String, Float>> sortedRenderTimes = performanceTracker.getSortedRenderTimes();
    assertThat(sortedRenderTimes.size(), equalTo(1));
    assertThat(sortedRenderTimes.get(0).first, equalTo("Hello"));
    assertThat(sortedRenderTimes.get(0).second, equalTo(16f));
  }

  @Test
  public void testTwoFrames() {
    performanceTracker.recordRenderTime("Hello", 16f);
    performanceTracker.recordRenderTime("Hello", 8f);
    List<Pair<String, Float>> sortedRenderTimes = performanceTracker.getSortedRenderTimes();
    assertThat(sortedRenderTimes.size(), equalTo(1));
    assertThat(sortedRenderTimes.get(0).first, equalTo("Hello"));
    assertThat(sortedRenderTimes.get(0).second, equalTo(12f));
  }

  @Test
  public void testTwoLayers() {
    performanceTracker.recordRenderTime("Hello", 16f);
    performanceTracker.recordRenderTime("World", 8f);
    List<Pair<String, Float>> sortedRenderTimes = performanceTracker.getSortedRenderTimes();
    assertThat(sortedRenderTimes.size(), equalTo(2));
    assertThat(sortedRenderTimes.get(0).first, equalTo("Hello"));
    assertThat(sortedRenderTimes.get(0).second, equalTo(16f));
    assertThat(sortedRenderTimes.get(1).first, equalTo("World"));
    assertThat(sortedRenderTimes.get(1).second, equalTo(8f));
  }

  @Test
  public void testTwoLayersAlternatingFrames() {
    performanceTracker.recordRenderTime("Hello", 16f);
    performanceTracker.recordRenderTime("World", 8f);
    performanceTracker.recordRenderTime("Hello", 32f);
    performanceTracker.recordRenderTime("World", 4f);
    List<Pair<String, Float>> sortedRenderTimes = performanceTracker.getSortedRenderTimes();
    assertThat(sortedRenderTimes.size(), equalTo(2));
    assertThat(sortedRenderTimes.get(0).first, equalTo("Hello"));
    assertThat(sortedRenderTimes.get(0).second, equalTo(24f));
    assertThat(sortedRenderTimes.get(1).first, equalTo("World"));
    assertThat(sortedRenderTimes.get(1).second, equalTo(6f));
  }
}
