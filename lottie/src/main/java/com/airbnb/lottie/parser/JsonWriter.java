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
package com.airbnb.lottie.parser;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.util.Arrays;

import androidx.annotation.Nullable;
import okio.BufferedSink;
import okio.BufferedSource;

import static com.airbnb.lottie.parser.JsonScope.EMPTY_ARRAY;
import static com.airbnb.lottie.parser.JsonScope.EMPTY_OBJECT;
import static com.airbnb.lottie.parser.JsonScope.NONEMPTY_ARRAY;
import static com.airbnb.lottie.parser.JsonScope.NONEMPTY_OBJECT;


/**
 * Writes a JSON (<a href="http://www.ietf.org/rfc/rfc7159.txt">RFC 7159</a>)
 * encoded value to a stream, one token at a time. The stream includes both
 * literal values (strings, numbers, booleans and nulls) as well as the begin
 * and end delimiters of objects and arrays.
 *
 * <h3>Encoding JSON</h3>
 * To encode your data as JSON, create a new {@code JsonWriter}. Each JSON
 * document must contain one top-level array or object. Call methods on the
 * writer as you walk the structure's contents, nesting arrays and objects as
 * necessary:
 * <ul>
 *   <li>To write <strong>arrays</strong>, first call {@link #beginArray()}.
 *       Write each of the array's elements with the appropriate {@link #value}
 *       methods or by nesting other arrays and objects. Finally close the array
 *       using {@link #endArray()}.
 *   <li>To write <strong>objects</strong>, first call {@link #beginObject()}.
 *       Write each of the object's properties by alternating calls to
 *       {@link #name} with the property's value. Write property values with the
 *       appropriate {@link #value} method or by nesting other objects or arrays.
 *       Finally close the object using {@link #endObject()}.
 * </ul>
 *
 * <h3>Example</h3>
 * Suppose we'd like to encode a stream of messages such as the following: <pre> {@code
 * [
 *   {
 *     "id": 912345678901,
 *     "text": "How do I stream JSON in Java?",
 *     "geo": null,
 *     "user": {
 *       "name": "json_newb",
 *       "followers_count": 41
 *      }
 *   },
 *   {
 *     "id": 912345678902,
 *     "text": "@json_newb just use JsonWriter!",
 *     "geo": [50.454722, -104.606667],
 *     "user": {
 *       "name": "jesse",
 *       "followers_count": 2
 *     }
 *   }
 * ]}</pre>
 * This code encodes the above structure: <pre>   {@code
 *   public void writeJsonStream(BufferedSink sink, List<Message> messages) throws IOException {
 *     JsonWriter writer = JsonWriter.of(sink);
 *     writer.setIndent("  ");
 *     writeMessagesArray(writer, messages);
 *     writer.close();
 *   }
 *
 *   public void writeMessagesArray(JsonWriter writer, List<Message> messages) throws IOException {
 *     writer.beginArray();
 *     for (Message message : messages) {
 *       writeMessage(writer, message);
 *     }
 *     writer.endArray();
 *   }
 *
 *   public void writeMessage(JsonWriter writer, Message message) throws IOException {
 *     writer.beginObject();
 *     writer.name("id").value(message.getId());
 *     writer.name("text").value(message.getText());
 *     if (message.getGeo() != null) {
 *       writer.name("geo");
 *       writeDoublesArray(writer, message.getGeo());
 *     } else {
 *       writer.name("geo").nullValue();
 *     }
 *     writer.name("user");
 *     writeUser(writer, message.getUser());
 *     writer.endObject();
 *   }
 *
 *   public void writeUser(JsonWriter writer, User user) throws IOException {
 *     writer.beginObject();
 *     writer.name("name").value(user.getName());
 *     writer.name("followers_count").value(user.getFollowersCount());
 *     writer.endObject();
 *   }
 *
 *   public void writeDoublesArray(JsonWriter writer, List<Double> doubles) throws IOException {
 *     writer.beginArray();
 *     for (Double value : doubles) {
 *       writer.value(value);
 *     }
 *     writer.endArray();
 *   }}</pre>
 *
 * <p>Each {@code JsonWriter} may be used to write a single JSON stream.
 * Instances of this class are not thread safe. Calls that would result in a
 * malformed JSON string will fail with an {@link IllegalStateException}.
 */
public abstract class JsonWriter implements Closeable, Flushable {
  // The nesting stack. Using a manual array rather than an ArrayList saves 20%. This stack will
  // grow itself up to 256 levels of nesting including the top-level document. Deeper nesting is
  // prone to trigger StackOverflowErrors.
  int stackSize = 0;
  int[] scopes = new int[32];
  String[] pathNames = new String[32];
  int[] pathIndices = new int[32];

  /**
   * A string containing a full set of spaces for a single level of indentation, or null for no
   * pretty printing.
   */
  String indent;
  boolean lenient;
  boolean serializeNulls;
  boolean promoteValueToName;

  /**
   * Controls the deepest stack size that has begin/end pairs flattened:
   *
   * <ul>
   *     <li>If -1, no begin/end pairs are being suppressed.
   *     <li>If positive, this is the deepest stack size whose begin/end pairs are eligible to be
   *         flattened.
   *     <li>If negative, it is the bitwise inverse (~) of the deepest stack size whose begin/end
   *         pairs have been flattened.
   * </ul>
   *
   * <p>We differentiate between what layer would be flattened (positive) from what layer is being
   * flattened (negative) so that we don't double-flatten.
   *
   * <p>To accommodate nested flattening we require callers to track the previous state when they
   * provide a new state. The previous state is returned from {@link #beginFlatten} and restored
   * with {@link #endFlatten}.
   */
  int flattenStackSize = -1;

  /** Returns a new instance that writes UTF-8 encoded JSON to {@code sink}. */
   public static JsonWriter of(BufferedSink sink) {
    return new JsonUtf8Writer(sink);
  }

  JsonWriter() {
    // Package-private to control subclasses.
  }

  /** Returns the scope on the top of the stack. */
  final int peekScope() {
    if (stackSize == 0) {
      throw new IllegalStateException("JsonWriter is closed.");
    }
    return scopes[stackSize - 1];
  }

  /** Before pushing a value on the stack this confirms that the stack has capacity. */
  final boolean checkStack() {
    if (stackSize != scopes.length) return false;

    if (stackSize == 256) {
      throw new JsonDataException("Nesting too deep at " + getPath() + ": circular reference?");
    }

    scopes = Arrays.copyOf(scopes, scopes.length * 2);
    pathNames = Arrays.copyOf(pathNames, pathNames.length * 2);
    pathIndices = Arrays.copyOf(pathIndices, pathIndices.length * 2);
    if (this instanceof JsonValueWriter) {
      ((JsonValueWriter) this).stack =
          Arrays.copyOf(((JsonValueWriter) this).stack, ((JsonValueWriter) this).stack.length * 2);
    }

    return true;
  }

  final void pushScope(int newTop) {
    scopes[stackSize++] = newTop;
  }

  /** Replace the value on the top of the stack with the given value. */
  final void replaceTop(int topOfStack) {
    scopes[stackSize - 1] = topOfStack;
  }

  /**
   * Sets the indentation string to be repeated for each level of indentation
   * in the encoded document. If {@code indent.isEmpty()} the encoded document
   * will be compact. Otherwise the encoded document will be more
   * human-readable.
   *
   * @param indent a string containing only whitespace.
   */
  public void setIndent(String indent) {
    this.indent = !indent.isEmpty() ? indent : null;
  }

  /**
   * Returns a string containing only whitespace, used for each level of
   * indentation. If empty, the encoded document will be compact.
   */
   public final String getIndent() {
    return indent != null ? indent : "";
  }

  /**
   * Configure this writer to relax its syntax rules. By default, this writer
   * only emits well-formed JSON as specified by <a
   * href="http://www.ietf.org/rfc/rfc7159.txt">RFC 7159</a>. Setting the writer
   * to lenient permits the following:
   * <ul>
   *   <li>Top-level values of any type. With strict writing, the top-level
   *       value must be an object or an array.
   *   <li>Numbers may be {@linkplain Double#isNaN() NaNs} or {@linkplain
   *       Double#isInfinite() infinities}.
   * </ul>
   */
  public final void setLenient(boolean lenient) {
    this.lenient = lenient;
  }

  /**
   * Returns true if this writer has relaxed syntax rules.
   */
   public final boolean isLenient() {
    return lenient;
  }



  /**
   * Begins encoding a new array. Each call to this method must be paired with
   * a call to {@link #endArray}.
   *
   * @return this writer.
   */
  public abstract JsonWriter beginArray() throws IOException;

  /**
   * Ends encoding the current array.
   *
   * @return this writer.
   */
  public abstract JsonWriter endArray() throws IOException;

  /**
   * Begins encoding a new object. Each call to this method must be paired
   * with a call to {@link #endObject}.
   *
   * @return this writer.
   */
  public abstract JsonWriter beginObject() throws IOException;

  /**
   * Ends encoding the current object.
   *
   * @return this writer.
   */
  public abstract JsonWriter endObject() throws IOException;

  /**
   * Encodes the property name.
   *
   * @param name the name of the forthcoming value. Must not be null.
   * @return this writer.
   */
  public abstract JsonWriter name(String name) throws IOException;

  /**
   * Encodes {@code value}.
   *
   * @param value the literal string value, or null to encode a null literal.
   * @return this writer.
   */
  public abstract JsonWriter value(@Nullable String value) throws IOException;

  /**
   * Encodes {@code null}.
   *
   * @return this writer.
   */
  public abstract JsonWriter nullValue() throws IOException;

  /**
   * Encodes {@code value}.
   *
   * @return this writer.
   */
  public abstract JsonWriter value(boolean value) throws IOException;

  /**
   * Encodes {@code value}.
   *
   * @return this writer.
   */
  public abstract JsonWriter value(@Nullable Boolean value) throws IOException;

  /**
   * Encodes {@code value}.
   *
   * @param value a finite value. May not be {@linkplain Double#isNaN() NaNs} or
   *     {@linkplain Double#isInfinite() infinities}.
   * @return this writer.
   */
  public abstract JsonWriter value(double value) throws IOException;

  /**
   * Encodes {@code value}.
   *
   * @return this writer.
   */
  public abstract JsonWriter value(long value) throws IOException;

  /**
   * Encodes {@code value}.
   *
   * @param value a finite value. May not be {@linkplain Double#isNaN() NaNs} or
   *     {@linkplain Double#isInfinite() infinities}.
   * @return this writer.
   */
  public abstract JsonWriter value(@Nullable Number value) throws IOException;

  /**
   * Writes {@code source} directly without encoding its contents.
   * are not respected.
   *
   * @return this writer.
   */
  public abstract JsonWriter value(BufferedSource source) throws IOException;

  /**
   * Changes the writer to treat the next value as a string name. This is useful for map adapters so
   * that arbitrary type adapters can use {@link #value} to write a name value.
   */
  final void promoteValueToName() throws IOException {
    int context = peekScope();
    if (context != NONEMPTY_OBJECT && context != EMPTY_OBJECT) {
      throw new IllegalStateException("Nesting problem.");
    }
    promoteValueToName = true;
  }

  /**
   * Cancels immediately-nested calls to {@link #beginArray()} or {@link #beginObject()} and their
   * matching calls to {@link #endArray} or {@link #endObject()}. Use this to compose JSON adapters
   * without nesting.
   *
   * <p>For example, the following creates JSON with nested arrays: {@code [1,[2,3,4],5]}.
   *
   * <pre>{@code
   *
   *   JsonAdapter<List<Integer>> integersAdapter = ...
   *
   *   public void writeNumbers(JsonWriter writer) {
   *     writer.beginArray();
   *     writer.value(1);
   *     integersAdapter.toJson(writer, Arrays.asList(2, 3, 4));
   *     writer.value(5);
   *     writer.endArray();
   *   }
   * }</pre>
   *
   * <p>With flattening we can create JSON with a single array {@code [1,2,3,4,5]}:
   *
   * <pre>{@code
   *
   *   JsonAdapter<List<Integer>> integersAdapter = ...
   *
   *   public void writeNumbers(JsonWriter writer) {
   *     writer.beginArray();
   *     int token = writer.beginFlatten();
   *     writer.value(1);
   *     integersAdapter.toJson(writer, Arrays.asList(2, 3, 4));
   *     writer.value(5);
   *     writer.endFlatten(token);
   *     writer.endArray();
   *   }
   * }</pre>
   *
   * <p>This method flattens arrays within arrays:
   *
   * <pre>{@code
   *
   *   Emit:       [1, [2, 3, 4], 5]
   *   To produce: [1, 2, 3, 4, 5]
   * }</pre>
   *
   * It also flattens objects within objects. Do not call {@link #name} before writing a flattened
   * object.
   *
   * <pre>{@code
   *
   *   Emit:       {"a": 1, {"b": 2}, "c": 3}
   *   To Produce: {"a": 1, "b": 2, "c": 3}
   * }</pre>
   *
   * Other combinations are permitted but do not perform flattening. For example, objects inside of
   * arrays are not flattened:
   *
   * <pre>{@code
   *
   *   Emit:       [1, {"b": 2}, 3, [4, 5], 6]
   *   To Produce: [1, {"b": 2}, 3, 4, 5, 6]
   * }</pre>
   *
   * <p>This method returns an opaque token. Callers must match all calls to this method with a call
   * to {@link #endFlatten} with the matching token.
   */
   public final int beginFlatten() {
    int context = peekScope();
    if (context != NONEMPTY_OBJECT && context != EMPTY_OBJECT
        && context != NONEMPTY_ARRAY && context != EMPTY_ARRAY) {
      throw new IllegalStateException("Nesting problem.");
    }
    int token = flattenStackSize;
    flattenStackSize = stackSize;
    return token;
  }

  /** Ends nested call flattening created by {@link #beginFlatten}. */
  public final void endFlatten(int token) {
    flattenStackSize = token;
  }

  /**
   * Returns a <a href="http://goessner.net/articles/JsonPath/">JsonPath</a> to
   * the current location in the JSON value.
   */
   public final String getPath() {
    return JsonScope.getPath(stackSize, scopes, pathNames, pathIndices);
  }
}