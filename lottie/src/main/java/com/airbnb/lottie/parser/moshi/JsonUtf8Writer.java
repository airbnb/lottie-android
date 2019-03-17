/*
 * Copyright (C) 2010 Google Inc.
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
package com.airbnb.lottie.parser.moshi;


import java.io.IOException;

import androidx.annotation.Nullable;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Sink;

import static com.airbnb.lottie.parser.moshi.JsonScope.DANGLING_NAME;
import static com.airbnb.lottie.parser.moshi.JsonScope.EMPTY_ARRAY;
import static com.airbnb.lottie.parser.moshi.JsonScope.EMPTY_DOCUMENT;
import static com.airbnb.lottie.parser.moshi.JsonScope.EMPTY_OBJECT;
import static com.airbnb.lottie.parser.moshi.JsonScope.NONEMPTY_ARRAY;
import static com.airbnb.lottie.parser.moshi.JsonScope.NONEMPTY_DOCUMENT;
import static com.airbnb.lottie.parser.moshi.JsonScope.NONEMPTY_OBJECT;


final class JsonUtf8Writer extends JsonWriter {

  /*
   * From RFC 7159, "All Unicode characters may be placed within the
   * quotation marks except for the characters that must be escaped:
   * quotation mark, reverse solidus, and the control characters
   * (U+0000 through U+001F)."
   *
   * We also escape '\u2028' and '\u2029', which JavaScript interprets as
   * newline characters. This prevents eval() from failing with a syntax
   * error. http://code.google.com/p/google-gson/issues/detail?id=341
   */
  private static final String[] REPLACEMENT_CHARS;
  static {
    REPLACEMENT_CHARS = new String[128];
    for (int i = 0; i <= 0x1f; i++) {
      REPLACEMENT_CHARS[i] = String.format("\\u%04x", (int) i);
    }
    REPLACEMENT_CHARS['"'] = "\\\"";
    REPLACEMENT_CHARS['\\'] = "\\\\";
    REPLACEMENT_CHARS['\t'] = "\\t";
    REPLACEMENT_CHARS['\b'] = "\\b";
    REPLACEMENT_CHARS['\n'] = "\\n";
    REPLACEMENT_CHARS['\r'] = "\\r";
    REPLACEMENT_CHARS['\f'] = "\\f";
  }

  /** The output data, containing at most one top-level array or object. */
  private final BufferedSink sink;

  /** The name/value separator; either ":" or ": ". */
  private String separator = ":";

  private String deferredName;

  JsonUtf8Writer(BufferedSink sink) {
    if (sink == null) {
      throw new NullPointerException("sink == null");
    }
    this.sink = sink;
    pushScope(EMPTY_DOCUMENT);
  }

  @Override public void setIndent(String indent) {
    super.setIndent(indent);
    this.separator = !indent.isEmpty() ? ": " : ":";
  }

  @Override public JsonWriter beginArray() throws IOException {
    if (promoteValueToName) {
      throw new IllegalStateException(
          "Array cannot be used as a map key in JSON at path " + getPath());
    }
    writeDeferredName();
    return open(EMPTY_ARRAY, NONEMPTY_ARRAY, "[");
  }

  @Override public JsonWriter endArray() throws IOException {
    return close(EMPTY_ARRAY, NONEMPTY_ARRAY, "]");
  }

  @Override public JsonWriter beginObject() throws IOException {
    if (promoteValueToName) {
      throw new IllegalStateException(
          "Object cannot be used as a map key in JSON at path " + getPath());
    }
    writeDeferredName();
    return open(EMPTY_OBJECT, NONEMPTY_OBJECT, "{");
  }

  @Override public JsonWriter endObject() throws IOException {
    promoteValueToName = false;
    return close(EMPTY_OBJECT, NONEMPTY_OBJECT, "}");
  }

  /**
   * Enters a new scope by appending any necessary whitespace and the given
   * bracket.
   */
  private JsonWriter open(int empty, int nonempty, String openBracket) throws IOException {
    if (stackSize == flattenStackSize
        && (scopes[stackSize - 1] == empty || scopes[stackSize - 1] == nonempty)) {
      // Cancel this open. Invert the flatten stack size until this is closed.
      flattenStackSize = ~flattenStackSize;
      return this;
    }
    beforeValue();
    checkStack();
    pushScope(empty);
    pathIndices[stackSize - 1] = 0;
    sink.writeUtf8(openBracket);
    return this;
  }

  /**
   * Closes the current scope by appending any necessary whitespace and the
   * given bracket.
   */
  private JsonWriter close(int empty, int nonempty, String closeBracket) throws IOException {
    int context = peekScope();
    if (context != nonempty && context != empty) {
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

    stackSize--;
    pathNames[stackSize] = null; // Free the last path name so that it can be garbage collected!
    pathIndices[stackSize - 1]++;
    if (context == nonempty) {
      newline();
    }
    sink.writeUtf8(closeBracket);
    return this;
  }

  @Override public JsonWriter name(String name)  {
    if (name == null) {
      throw new NullPointerException("name == null");
    }
    if (stackSize == 0) {
      throw new IllegalStateException("JsonWriter is closed.");
    }
    int context = peekScope();
    if ((context != EMPTY_OBJECT && context != NONEMPTY_OBJECT) || deferredName != null) {
      throw new IllegalStateException("Nesting problem.");
    }
    deferredName = name;
    pathNames[stackSize - 1] = name;
    promoteValueToName = false;
    return this;
  }

  private void writeDeferredName() throws IOException {
    if (deferredName != null) {
      beforeName();
      string(sink, deferredName);
      deferredName = null;
    }
  }

  @Override public JsonWriter value(String value) throws IOException {
    if (value == null) {
      return nullValue();
    }
    if (promoteValueToName) {
      return name(value);
    }
    writeDeferredName();
    beforeValue();
    string(sink, value);
    pathIndices[stackSize - 1]++;
    return this;
  }

  @Override public JsonWriter nullValue() throws IOException {
    if (promoteValueToName) {
      throw new IllegalStateException(
          "null cannot be used as a map key in JSON at path " + getPath());
    }
    if (deferredName != null) {
      if (serializeNulls) {
        writeDeferredName();
      } else {
        deferredName = null;
        return this; // skip the name and the value
      }
    }
    beforeValue();
    sink.writeUtf8("null");
    pathIndices[stackSize - 1]++;
    return this;
  }

  @Override public JsonWriter value(boolean value) throws IOException {
    if (promoteValueToName) {
      throw new IllegalStateException(
          "Boolean cannot be used as a map key in JSON at path " + getPath());
    }
    writeDeferredName();
    beforeValue();
    sink.writeUtf8(value ? "true" : "false");
    pathIndices[stackSize - 1]++;
    return this;
  }

  @Override public JsonWriter value(Boolean value) throws IOException {
    if (value == null) {
      return nullValue();
    }
    return value(value.booleanValue());
  }

  @Override public JsonWriter value(double value) throws IOException {
    if (!lenient && (Double.isNaN(value) || Double.isInfinite(value))) {
      throw new IllegalArgumentException("Numeric values must be finite, but was " + value);
    }
    if (promoteValueToName) {
      return name(Double.toString(value));
    }
    writeDeferredName();
    beforeValue();
    sink.writeUtf8(Double.toString(value));
    pathIndices[stackSize - 1]++;
    return this;
  }

  @Override public JsonWriter value(long value) throws IOException {
    if (promoteValueToName) {
      return name(Long.toString(value));
    }
    writeDeferredName();
    beforeValue();
    sink.writeUtf8(Long.toString(value));
    pathIndices[stackSize - 1]++;
    return this;
  }

  @Override public JsonWriter value(@Nullable Number value) throws IOException {
    if (value == null) {
      return nullValue();
    }

    String string = value.toString();
    if (!lenient
        && (string.equals("-Infinity") || string.equals("Infinity") || string.equals("NaN"))) {
      throw new IllegalArgumentException("Numeric values must be finite, but was " + value);
    }
    if (promoteValueToName) {
      return name(string);
    }
    writeDeferredName();
    beforeValue();
    sink.writeUtf8(string);
    pathIndices[stackSize - 1]++;
    return this;
  }

  @Override public JsonWriter value(BufferedSource source) throws IOException {
    if (promoteValueToName) {
      throw new IllegalStateException(
          "BufferedSource cannot be used as a map key in JSON at path " + getPath());
    }
    writeDeferredName();
    beforeValue();
    sink.writeAll(source);
    pathIndices[stackSize - 1]++;
    return this;
  }

  /**
   * Ensures all buffered data is written to the underlying {@link Sink}
   * and flushes that writer.
   */
  @Override public void flush() throws IOException {
    if (stackSize == 0) {
      throw new IllegalStateException("JsonWriter is closed.");
    }
    sink.flush();
  }

  /**
   * Flushes and closes this writer and the underlying {@link Sink}.
   *
   * @throws JsonDataException if the JSON document is incomplete.
   */
  @Override public void close() throws IOException {
    sink.close();

    int size = stackSize;
    if (size > 1 || size == 1 && scopes[size - 1] != NONEMPTY_DOCUMENT) {
      throw new IOException("Incomplete document");
    }
    stackSize = 0;
  }

  /**
   * Writes {@code value} as a string literal to {@code sink}. This wraps the value in double quotes
   * and escapes those characters that require it.
   */
  static void string(BufferedSink sink, String value) throws IOException {
    String[] replacements = REPLACEMENT_CHARS;
    sink.writeByte('"');
    int last = 0;
    int length = value.length();
    for (int i = 0; i < length; i++) {
      char c = value.charAt(i);
      String replacement;
      if (c < 128) {
        replacement = replacements[c];
        if (replacement == null) {
          continue;
        }
      } else if (c == '\u2028') {
        replacement = "\\u2028";
      } else if (c == '\u2029') {
        replacement = "\\u2029";
      } else {
        continue;
      }
      if (last < i) {
        sink.writeUtf8(value, last, i);
      }
      sink.writeUtf8(replacement);
      last = i + 1;
    }
    if (last < length) {
      sink.writeUtf8(value, last, length);
    }
    sink.writeByte('"');
  }

  private void newline() throws IOException {
    if (indent == null) {
      return;
    }

    sink.writeByte('\n');
    for (int i = 1, size = stackSize; i < size; i++) {
      sink.writeUtf8(indent);
    }
  }

  /**
   * Inserts any necessary separators and whitespace before a name. Also
   * adjusts the stack to expect the name's value.
   */
  private void beforeName() throws IOException {
    int context = peekScope();
    if (context == NONEMPTY_OBJECT) { // first in object
      sink.writeByte(',');
    } else if (context != EMPTY_OBJECT) { // not in an object!
      throw new IllegalStateException("Nesting problem.");
    }
    newline();
    replaceTop(DANGLING_NAME);
  }

  /**
   * Inserts any necessary separators and whitespace before a literal value,
   * inline array, or inline object. Also adjusts the stack to expect either a
   * closing bracket or another element.
   */
  @SuppressWarnings("fallthrough")
  private void beforeValue() throws IOException {
    switch (peekScope()) {
      case NONEMPTY_DOCUMENT:
        if (!lenient) {
          throw new IllegalStateException(
              "JSON must have only one top-level value.");
        }
        // fall-through
      case EMPTY_DOCUMENT: // first in document
        replaceTop(NONEMPTY_DOCUMENT);
        break;

      case EMPTY_ARRAY: // first in array
        replaceTop(NONEMPTY_ARRAY);
        newline();
        break;

      case NONEMPTY_ARRAY: // another in array
        sink.writeByte(',');
        newline();
        break;

      case DANGLING_NAME: // value for name
        sink.writeUtf8(separator);
        replaceTop(NONEMPTY_OBJECT);
        break;

      default:
        throw new IllegalStateException("Nesting problem.");
    }
  }
}