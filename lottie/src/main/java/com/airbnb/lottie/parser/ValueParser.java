package com.airbnb.lottie.parser;


import com.airbnb.lottie.parser.moshi.JsonReader;

import java.io.IOException;

interface ValueParser<V> {
  V parse(JsonReader reader, float scale) throws IOException;
}
