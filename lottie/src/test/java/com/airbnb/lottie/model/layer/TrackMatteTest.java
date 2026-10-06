package com.airbnb.lottie.model.layer;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

import com.airbnb.lottie.BaseTest;
import com.airbnb.lottie.LottieCompositionFactory;
import java.util.List;
import org.junit.Test;

public class TrackMatteTest extends BaseTest {

  @Test
  public void testMatteParentResolvesToANonAdjacentLayer() {
    List<Layer> layers = layers("tp",
        "{\"ty\":3,\"ind\":1,\"tt\":1,\"tp\":3,\"ks\":{}}",
        "{\"ty\":3,\"ind\":2,\"ks\":{}}",
        "{\"ty\":3,\"ind\":3,\"td\":1,\"ks\":{}}");

    assertArrayEquals(new int[]{2, -1, -1}, CompositionLayer.resolveTrackMattes(layers));
    assertTrue(layers.get(2).isMatteTarget());
  }

  @Test
  public void testWithoutMatteParentTheLayerAboveIsTheMatte() {
    List<Layer> layers = layers("no_tp",
        "{\"ty\":3,\"ind\":1,\"ks\":{}}",
        "{\"ty\":3,\"ind\":2,\"tt\":1,\"ks\":{}}");

    assertArrayEquals(new int[]{-1, 0}, CompositionLayer.resolveTrackMattes(layers));
  }

  @Test
  public void testUnresolvableMatteParentFallsBackToTheLayerAbove() {
    List<Layer> layers = layers("bad_tp",
        "{\"ty\":3,\"ind\":1,\"ks\":{}}",
        "{\"ty\":3,\"ind\":2,\"tt\":1,\"tp\":99,\"ks\":{}}");

    assertArrayEquals(new int[]{-1, 0}, CompositionLayer.resolveTrackMattes(layers));
  }

  private static List<Layer> layers(String cacheKey, String... layers) {
    String json = "{\"v\":\"5.7.0\",\"fr\":24,\"ip\":0,\"op\":10,\"w\":10,\"h\":10,\"layers\":["
        + String.join(",", layers) + "]}";
    return LottieCompositionFactory.fromJsonStringSync(json, cacheKey).getValue().getLayers();
  }
}
