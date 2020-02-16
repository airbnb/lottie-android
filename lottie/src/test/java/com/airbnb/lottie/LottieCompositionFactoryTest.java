package com.airbnb.lottie;

import com.airbnb.lottie.model.LottieCompositionCache;

import com.airbnb.lottie.parser.moshi.JsonReader;
import org.apache.tools.ant.filters.StringInputStream;
import org.junit.Before;
import org.junit.Test;
import org.robolectric.RuntimeEnvironment;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static okio.Okio.buffer;
import static okio.Okio.source;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("ReferenceEquality")
public class LottieCompositionFactoryTest extends BaseTest {
    private static final String JSON = "{\"v\":\"4.11.1\",\"fr\":60,\"ip\":0,\"op\":180,\"w\":300,\"h\":300,\"nm\":\"Comp 1\",\"ddd\":0,\"assets\":[]," +
            "\"layers\":[{\"ddd\":0,\"ind\":1,\"ty\":4,\"nm\":\"Shape Layer 1\",\"sr\":1,\"ks\":{\"o\":{\"a\":0,\"k\":100,\"ix\":11},\"r\":{\"a\":0," +
            "\"k\":0,\"ix\":10},\"p\":{\"a\":0,\"k\":[150,150,0],\"ix\":2},\"a\":{\"a\":0,\"k\":[0,0,0],\"ix\":1},\"s\":{\"a\":0,\"k\":[100,100,100]," +
            "\"ix\":6}},\"ao\":0,\"shapes\":[{\"ty\":\"rc\",\"d\":1,\"s\":{\"a\":0,\"k\":[100,100],\"ix\":2},\"p\":{\"a\":0,\"k\":[0,0],\"ix\":3}," +
            "\"r\":{\"a\":0,\"k\":0,\"ix\":4},\"nm\":\"Rectangle Path 1\",\"mn\":\"ADBE Vector Shape - Rect\",\"hd\":false},{\"ty\":\"fl\"," +
            "\"c\":{\"a\":0,\"k\":[0.928262987324,0,0,1],\"ix\":4},\"o\":{\"a\":0,\"k\":100,\"ix\":5},\"r\":1,\"nm\":\"Fill 1\",\"mn\":\"ADBE Vector " +
            "Graphic - Fill\",\"hd\":false}],\"ip\":0,\"op\":180,\"st\":0,\"bm\":0}]}";

    private static final String NOT_JSON = "not json";

    @Before
    public void setup() {
        LottieCompositionCache.getInstance().clear();
        LottieCompositionCache.getInstance().resize(20);
    }

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
        JsonReader reader = JsonReader.of(buffer(source(new StringInputStream(JSON))));
        LottieResult<LottieComposition> result = LottieCompositionFactory.fromJsonReaderSync(reader, "json");
        assertNull(result.getException());
        assertNotNull(result.getValue());
    }

    @Test
    public void testLoadInvalidJsonReader() {
        JsonReader reader = JsonReader.of(buffer(source(new StringInputStream(NOT_JSON))));
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

    @Test
    public void testNullMultipleTimesAsync() {
        JsonReader reader = JsonReader.of(buffer(source(getNeverCompletingInputStream())));
        LottieTask<LottieComposition> task1 = LottieCompositionFactory.fromJsonReader(reader, null);
        LottieTask<LottieComposition> task2 = LottieCompositionFactory.fromJsonReader(reader, null);
        assertFalse(task1 == task2);
    }

    @Test
    public void testNullMultipleTimesSync() {
        JsonReader reader = JsonReader.of(buffer(source(getNeverCompletingInputStream())));
        LottieResult<LottieComposition> task1 = LottieCompositionFactory.fromJsonReaderSync(reader, null);
        LottieResult<LottieComposition> task2 = LottieCompositionFactory.fromJsonReaderSync(reader, null);
        assertFalse(task1 == task2);
    }

    @Test
    public void testCacheWorks() {
        JsonReader reader = JsonReader.of(buffer(source(getNeverCompletingInputStream())));
        LottieTask<LottieComposition> task1 = LottieCompositionFactory.fromJsonReader(reader, "foo");
        LottieTask<LottieComposition> task2 = LottieCompositionFactory.fromJsonReader(reader, "foo");
        assertTrue(task1 == task2);
    }

    @Test
    public void testZeroCacheWorks() {
        JsonReader reader = JsonReader.of(buffer(source(getNeverCompletingInputStream())));
        LottieCompositionFactory.setMaxCacheSize(1);
        LottieResult<LottieComposition> taskFoo1 = LottieCompositionFactory.fromJsonReaderSync(reader, "foo");
        LottieResult<LottieComposition> taskBar = LottieCompositionFactory.fromJsonReaderSync(reader, "bar");
        LottieResult<LottieComposition> taskFoo2 = LottieCompositionFactory.fromJsonReaderSync(reader, "foo");
        assertFalse(taskFoo1 == taskFoo2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCannotSetCacheSizeToZero() {
        LottieCompositionFactory.setMaxCacheSize(0);
    }

    private static InputStream getNeverCompletingInputStream() {
        return new InputStream() {
            @Override public int read() throws IOException {
                return 100;
            }
        };
    }
}
