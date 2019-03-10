/*
 * Copyright (C) 2017 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.airbnb.lottie.parser;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import androidx.annotation.Nullable;
import okio.BufferedSource;

import static com.airbnb.lottie.parser.JsonScope.EMPTY_ARRAY;
import static com.airbnb.lottie.parser.JsonScope.EMPTY_DOCUMENT;
import static com.airbnb.lottie.parser.JsonScope.EMPTY_OBJECT;
import static com.airbnb.lottie.parser.JsonScope.NONEMPTY_DOCUMENT;
import static java.lang.Double.NEGATIVE_INFINITY;
import static java.lang.Double.POSITIVE_INFINITY;

/** Writes JSON by building a Java object comprising maps, lists, and JSON primitives. */
final class JsonValueWriter extends JsonWriter {
  Object[] stack = new Object[32];
  private @Nullable
  String deferredName;

  JsonValueWriter() {
    pushScope(EMPTY_DOCUMENT);
  }

  public Object root() {
    int size = stackSize;
    if (size > 1 || size == 1 && scopes[size - 1] != NONEMPTY_DOCUMENT) {
      throw new IllegalStateException("Incomplete document");
    }
    return stack[0];
  }

  @Override public JsonWriter beginArray()  {
    if (promoteValueToName) {
      throw new IllegalStateException(
          "Array cannot be used as a map key in JSON at path " + getPath());
    }
    if (stackSize == flattenStackSize && scopes[stackSize - 1] == EMPTY_ARRAY) {
      // Cancel this open. Invert the flatten stack size until this is closed.
      flattenStackSize = ~flattenStackSize;
      return this;
    }
    checkStack();
    List<Object> list = new ArrayList<>();
    add(list);
    stack[stackSize] = list;
    pathIndices[stackSize] = 0;
    pushScope(EMPTY_ARRAY);
    return this;
  }

  @Override public JsonWriter endArray() throws IOException {
    if (peekScope() != EMPTY_ARRAY) {
      throw new IllegalStateException("Nesting problem.");
    }
    if (stackSize == ~flattenStackSize) {
      // Cancel this close. Restore the flattenStackSize so we're ready to flatten again!
      flattenStackSize = ~flattenStackSize;
      return this;
    }
    stackSize--;
    stack[stackSize] = null;
    pathIndices[stackSize - 1]++;
    return this;
  }

  @Override public JsonWriter beginObject() throws IOException {
    if (promoteValueToName) {
      throw new IllegalStateException(
          "Object cannot be used as a map key in JSON at path " + getPath());
    }
    if (stackSize == flattenStackSize && scopes[stackSize - 1] == EMPTY_OBJECT) {
      // Cancel this open. Invert the flatten stack size until this is closed.
      flattenStackSize = ~flattenStackSize;
      return this;
    }
    checkStack();
    Map<String, Object> map = new LinkedHashTreeMap<>();
    add(map);
    stack[stackSize] = map;
    pushScope(EMPTY_OBJECT);
    return this;
  }

  @Override public JsonWriter endObject() throws IOException {
    if (peekScope() != EMPTY_OBJECT) {
      throw new IllegalStateException("Nesting problem.");
    }
    if (deferredName != null) {
      throw new IllegalStateException("Dangling name: " + deferredName);
    }
    if (stackSize == ~flattenStackSize) {
      // Cancel this close. Restore the flattenStackSize so we're ready to flatten again!
      flattenStackSize = ~flattenStackSize;
      return this;
    }
    promoteValueToName = false;
    stackSize--;
    stack[stackSize] = null;
    pathNames[stackSize] = null; // Free the last path name so that it can be garbage collected!
    pathIndices[stackSize - 1]++;
    return this;
  }

  @Override public JsonWriter name(String name) throws IOException {
    if (name == null) {
      throw new NullPointerException("name == null");
    }
    if (stackSize == 0) {
      throw new IllegalStateException("JsonWriter is closed.");
    }
    if (peekScope() != EMPTY_OBJECT || deferredName != null) {
      throw new IllegalStateException("Nesting problem.");
    }
    deferredName = name;
    pathNames[stackSize - 1] = name;
    promoteValueToName = false;
    return this;
  }

  @Override public JsonWriter value(@Nullable String value) throws IOException {
    if (promoteValueToName) {
      return name(value);
    }
    add(value);
    pathIndices[stackSize - 1]++;
    return this;
  }

  @Override public JsonWriter nullValue() throws IOException {
    if (promoteValueToName) {
      throw new IllegalStateException(
          "null cannot be used as a map key in JSON at path " + getPath());
    }
    add(null);
    pathIndices[stackSize - 1]++;
    return this;
  }

  @Override public JsonWriter value(boolean value)  {
    if (promoteValueToName) {
      throw new IllegalStateException(
          "Boolean cannot be used as a map key in JSON at path " + getPath());
    }
    add(value);
    pathIndices[stackSize - 1]++;
    return this;
  }

  @Override public JsonWriter value(@Nullable Boolean value)  {
    if (promoteValueToName) {
      throw new IllegalStateException(
          "Boolean cannot be used as a map key in JSON at path " + getPath());
    }
    add(value);
    pathIndices[stackSize - 1]++;
    return this;
  }

  @Override public JsonWriter value(double value) throws IOException {
    if (!lenient
        && (Double.isNaN(value) || value == NEGATIVE_INFINITY || value == POSITIVE_INFINITY)) {
      throw new IllegalArgumentException("Numeric values must be finite, but was " + value);
    }
    if (promoteValueToName) {
      return name(Double.toString(value));
    }
    add(value);
    pathIndices[stackSize - 1]++;
    return this;
  }

  @Override public JsonWriter value(long value) throws IOException {
    if (promoteValueToName) {
      return name(Long.toString(value));
    }
    add(value);
    pathIndices[stackSize - 1]++;
    return this;
  }

  @Override public JsonWriter value(@Nullable Number value) throws IOException {
    // If it's trivially converted to a long, do that.
    if (value instanceof Byte
        || value instanceof Short
        || value instanceof Integer
        || value instanceof Long) {
      return value(value.longValue());
    }

    // If it's trivially converted to a double, do that.
    if (value instanceof Float || value instanceof Double) {
      return value(value.doubleValue());
    }

    if (value == null) {
      return nullValue();
    }

    // Everything else gets converted to a BigDecimal.
    BigDecimal bigDecimalValue = value instanceof BigDecimal
        ? ((BigDecimal) value)
        : new BigDecimal(value.toString());
    if (promoteValueToName) {
      return name(bigDecimalValue.toString());
    }
    add(bigDecimalValue);
    pathIndices[stackSize - 1]++;
    return this;
  }

  @Override public JsonWriter value(BufferedSource source) throws IOException {
    if (promoteValueToName) {
      throw new IllegalStateException(
          "BufferedSource cannot be used as a map key in JSON at path " + getPath());
    }
    Object value = JsonReader.of(source).readJsonValue();
    boolean serializeNulls = this.serializeNulls;
    this.serializeNulls = true;
    try {
      add(value);
    } finally {
      this.serializeNulls = serializeNulls;
    }
    pathIndices[stackSize - 1]++;
    return this;
  }

  @Override public void close() throws IOException {
    int size = stackSize;
    if (size > 1 || size == 1 && scopes[size - 1] != NONEMPTY_DOCUMENT) {
      throw new IOException("Incomplete document");
    }
    stackSize = 0;
  }

  @Override public void flush()  {
    if (stackSize == 0) {
      throw new IllegalStateException("JsonWriter is closed.");
    }
  }

  private JsonValueWriter add(@Nullable Object newTop) {
    int scope = peekScope();

    if (stackSize == 1) {
      if (scope != EMPTY_DOCUMENT) {
        throw new IllegalStateException("JSON must have only one top-level value.");
      }
      scopes[stackSize - 1] = NONEMPTY_DOCUMENT;
      stack[stackSize - 1] = newTop;

    } else if (scope == EMPTY_OBJECT && deferredName != null) {
      if (newTop != null || serializeNulls) {
        @SuppressWarnings("unchecked") // Our maps always have string keys and object values.
        Map<String, Object> map = (Map<String, Object>) stack[stackSize - 1];
        Object replaced = map.put(deferredName, newTop);
        if (replaced != null) {
          throw new IllegalArgumentException("Map key '" + deferredName
              + "' has multiple values at path " + getPath() + ": " + replaced + " and " + newTop);
        }
      }
      deferredName = null;

    } else if (scope == EMPTY_ARRAY) {
      @SuppressWarnings("unchecked") // Our lists always have object values.
      List<Object> list = (List<Object>) stack[stackSize - 1];
      list.add(newTop);

    } else {
      throw new IllegalStateException("Nesting problem.");
    }

    return this;
  }
}