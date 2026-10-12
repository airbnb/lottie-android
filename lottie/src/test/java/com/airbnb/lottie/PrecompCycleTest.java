package com.airbnb.lottie;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;

import org.junit.Test;

import java.lang.reflect.Field;
import java.util.List;

/**
 * A precomposition whose reference chain closes a cycle used to recurse in
 * {@code BaseLayer.forModel} / {@code CompositionLayer} until the stack was exhausted.
 *
 * @see <a href="https://github.com/airbnb/lottie-android">CWE-674 Uncontrolled Recursion</a>
 */
public class PrecompCycleTest extends BaseTest {

  private static String ks() {
    return "\"ks\":{\"o\":{\"a\":0,\"k\":100,\"ix\":11},\"r\":{\"a\":0,\"k\":0,\"ix\":10},"
        + "\"p\":{\"a\":0,\"k\":[50,50,0],\"ix\":2},\"a\":{\"a\":0,\"k\":[0,0,0],\"ix\":1},"
        + "\"s\":{\"a\":0,\"k\":[100,100,100],\"ix\":6}}";
  }

  private static String precompLayer(int ind, String name, String refId) {
    return "{\"ddd\":0,\"ind\":" + ind + ",\"ty\":0,\"nm\":\"" + name + "\",\"refId\":\"" + refId
        + "\",\"sr\":1," + ks() + ",\"ao\":0,\"w\":100,\"h\":100,\"ip\":0,\"op\":30,\"st\":0,\"bm\":0}";
  }

  private static String shapeLayer(int ind, String name) {
    return "{\"ddd\":0,\"ind\":" + ind + ",\"ty\":4,\"nm\":\"" + name + "\",\"sr\":1," + ks()
        + ",\"ao\":0,\"shapes\":[],\"ip\":0,\"op\":30,\"st\":0,\"bm\":0}";
  }

  private static String asset(String id, String layers) {
    return "{\"id\":\"" + id + "\",\"layers\":[" + layers + "]}";
  }

  private static String composition(String assets, String layers) {
    return "{\"v\":\"5.0.0\",\"fr\":30,\"ip\":0,\"op\":30,\"w\":100,\"h\":100,\"nm\":\"cycle\","
        + "\"ddd\":0,\"assets\":[" + assets + "],\"layers\":[" + layers + "]}";
  }

  private static LottieComposition parse(String json) {
    LottieResult<LottieComposition> result = LottieCompositionFactory.fromJsonStringSync(json, null);
    assertNotNull("composition should parse", result.getValue());
    return result.getValue();
  }

  /** Building the layer tree is where the unbounded recursion used to happen. */
  private static void buildLayerTree(LottieComposition composition) {
    LottieDrawable drawable = new LottieDrawable();
    drawable.setComposition(composition);
  }

  @Test
  public void testSelfReferencingPrecompDoesNotStackOverflow() {
    String json = composition(
        asset("comp_0", precompLayer(1, "self", "comp_0")),
        precompLayer(1, "root", "comp_0"));
    buildLayerTree(parse(json));
  }

  @Test
  public void testMutuallyReferencingPrecompsDoNotStackOverflow() {
    String json = composition(
        asset("comp_a", precompLayer(1, "to_b", "comp_b"))
            + "," + asset("comp_b", precompLayer(1, "to_a", "comp_a")),
        precompLayer(1, "root", "comp_a"));
    buildLayerTree(parse(json));
  }

  @Test
  public void testUndefinedPrecompRefDoesNotThrow() {
    String json = composition("", precompLayer(1, "root", "comp_missing"));
    buildLayerTree(parse(json));
  }

  @Test
  public void testPrecompLayerWithoutRefIdDoesNotThrow() {
    String json = composition("",
        "{\"ddd\":0,\"ind\":1,\"ty\":0,\"nm\":\"no ref\",\"sr\":1," + ks()
            + ",\"ao\":0,\"w\":100,\"h\":100,\"ip\":0,\"op\":30,\"st\":0,\"bm\":0}");
    buildLayerTree(parse(json));
  }

  /**
   * A precomp that is referenced by two different layers is a diamond, not a cycle. It must still
   * be built twice, which only holds if the guard tracks the current path rather than every
   * precomp seen so far.
   */
  @Test
  public void testSharedPrecompIsNotTreatedAsACycle() throws Exception {
    String json = composition(
        asset("comp_0", shapeLayer(1, "rect")),
        precompLayer(1, "a", "comp_0") + "," + precompLayer(2, "b", "comp_0"));
    LottieComposition composition = parse(json);
    LottieDrawable drawable = new LottieDrawable();
    drawable.setComposition(composition);

    Field compositionLayerField = LottieDrawable.class.getDeclaredField("compositionLayer");
    compositionLayerField.setAccessible(true);
    Object compositionLayer = compositionLayerField.get(drawable);
    assertNotNull("root composition layer should be built", compositionLayer);

    Field layersField = compositionLayer.getClass().getDeclaredField("layers");
    layersField.setAccessible(true);
    List<?> children = (List<?>) layersField.get(compositionLayer);
    assertEquals("both references to the shared precomp should be built", 2, children.size());
  }
}
