package com.airbnb.lottie;

import android.util.JsonReader;

import junit.framework.Assert;

import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.io.FileNotFoundException;
import java.io.StringReader;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;

public class LottieCompositionFactoryTest extends BaseTest {
  private static final String JSON = "{\"v\":\"4.11.1\",\"fr\":60,\"ip\":0,\"op\":180,\"w\":300,\"h\":300,\"nm\":\"Comp 1\",\"ddd\":0,\"assets\":[]," +
      "\"layers\":[{\"ddd\":0,\"ind\":1,\"ty\":4,\"nm\":\"Shape Layer 1\",\"sr\":1,\"ks\":{\"o\":{\"a\":0,\"k\":100,\"ix\":11},\"r\":{\"a\":0," +
      "\"k\":0,\"ix\":10},\"p\":{\"a\":0,\"k\":[150,150,0],\"ix\":2},\"a\":{\"a\":0,\"k\":[0,0,0],\"ix\":1},\"s\":{\"a\":0,\"k\":[100,100,100]," +
      "\"ix\":6}},\"ao\":0,\"shapes\":[{\"ty\":\"rc\",\"d\":1,\"s\":{\"a\":0,\"k\":[100,100],\"ix\":2},\"p\":{\"a\":0,\"k\":[0,0],\"ix\":3}," +
      "\"r\":{\"a\":0,\"k\":0,\"ix\":4},\"nm\":\"Rectangle Path 1\",\"mn\":\"ADBE Vector Shape - Rect\",\"hd\":false},{\"ty\":\"fl\"," +
      "\"c\":{\"a\":0,\"k\":[0.928262987324,0,0,1],\"ix\":4},\"o\":{\"a\":0,\"k\":100,\"ix\":5},\"r\":1,\"nm\":\"Fill 1\",\"mn\":\"ADBE Vector " +
      "Graphic - Fill\",\"hd\":false}],\"ip\":0,\"op\":180,\"st\":0,\"bm\":0}]}";

  private static final String NOT_JSON = "not json";

  @Test
  public void testLoadJsonString() {
    LottieResult<LottieComposition> result = LottieCompositionFactory.fromJsonStringSync(JSON, "json");
    assertNull(result.getException());
    assertNotNull(result.getValue());
  }

  @Test
  public void testLoadInvalidJsonString() {
    LottieResult<LottieComposition> result = LottieCompositionFactory.fromJsonStringSync(NOT_JSON, "not_json");
    assertNotNull(result.getException());
    assertNull(result.getValue());
  }

  @Test
  public void testLoadJsonReader() {
    JsonReader reader = new JsonReader(new StringReader(JSON));
    LottieResult<LottieComposition> result = LottieCompositionFactory.fromJsonReaderSync(reader, "json");
    assertNull(result.getException());
    assertNotNull(result.getValue());
  }

  @Test
  public void testLoadInvalidJsonReader() {
    JsonReader reader = new JsonReader(new StringReader(NOT_JSON));
    LottieResult<LottieComposition> result = LottieCompositionFactory.fromJsonReaderSync(reader, "json");
    assertNotNull(result.getException());
    assertNull(result.getValue());
  }

  @Test
  public void testLoadInvalidAssetName() {
    LottieResult<LottieComposition> result = LottieCompositionFactory.fromAssetSync(RuntimeEnvironment.application, "square2.json");
    assertEquals(FileNotFoundException.class, result.getException().getClass());
    assertNull(result.getValue());
  }

  @Test
  public void testNonJsonAssetFile() {
    LottieResult<LottieComposition> result = LottieCompositionFactory.fromAssetSync(RuntimeEnvironment.application, "not_json.txt");
    assertNotNull(result.getException());
    assertNull(result.getValue());
  }

  @Test
  public void testLoadInvalidRawResName() {
    LottieResult<LottieComposition> result = LottieCompositionFactory.fromRawResSync(RuntimeEnvironment.application, 0);
    assertNotNull(result.getException());
    assertNull(result.getValue());
  }
}
