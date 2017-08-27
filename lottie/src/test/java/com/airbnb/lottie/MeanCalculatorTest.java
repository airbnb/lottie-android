package com.airbnb.lottie;

import com.airbnb.lottie.utils.MeanCalculator;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class MeanCalculatorTest {

  private MeanCalculator meanCalculator;

  @Before
  public void setup() {
    meanCalculator = new MeanCalculator();
  }

  @Test
  public void testMeanWithNoNumbers() {
    assertThat(meanCalculator.getMean(), equalTo(0f));
  }

  @Test
  public void testMeanWithOneNumber() {
    meanCalculator.add(2);
    assertThat(meanCalculator.getMean(), equalTo(2f));
  }

  @Test
  public void testMeanWithTwoNumbers() {
    meanCalculator.add(2);
    meanCalculator.add(4);
    assertThat(meanCalculator.getMean(), equalTo(3f));
  }

  @Test
  public void testMeanWithTwentyNumbers() {
    for (int i = 1; i <= 20; i++) {
      meanCalculator.add(i);
    }
    assertThat(meanCalculator.getMean(), equalTo(10.5f));
  }

  @Test
  public void testMeanWithHugeNumber() {
    meanCalculator.add(Integer.MAX_VALUE - 1);
    meanCalculator.add(Integer.MAX_VALUE - 1);
    assertThat(meanCalculator.getMean(), equalTo((float) (Integer.MAX_VALUE - 1)));
  }

  @Test
  public void testMeanWithHugeNumberAndNegativeHugeNumber() {
    meanCalculator.add(Integer.MAX_VALUE - 1);
    meanCalculator.add(Integer.MAX_VALUE - 1);
    meanCalculator.add(-Integer.MAX_VALUE + 1);
    meanCalculator.add(-Integer.MAX_VALUE + 1);
    assertThat(meanCalculator.getMean(), equalTo(0f));
  }
}
