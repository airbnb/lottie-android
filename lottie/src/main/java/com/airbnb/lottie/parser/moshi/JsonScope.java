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

/**
 * Lexical scoping elements within a JSON reader or writer.
 */
final class JsonScope {
  private JsonScope() {
  }

  /**
   * An array with no elements requires no separators or newlines before it is closed.
   */
  static final int EMPTY_ARRAY = 1;

  /**
   * A array with at least one value requires a comma and newline before the next element.
   */
  static final int NONEMPTY_ARRAY = 2;

  /**
   * An object with no name/value pairs requires no separators or newlines before it is closed.
   */
  static final int EMPTY_OBJECT = 3;

  /**
   * An object whose most recent element is a key. The next element must be a value.
   */
  static final int DANGLING_NAME = 4;

  /**
   * An object with at least one name/value pair requires a separator before the next element.
   */
  static final int NONEMPTY_OBJECT = 5;

  /**
   * No object or array has been started.
   */
  static final int EMPTY_DOCUMENT = 6;

  /**
   * A document with at an array or object.
   */
  static final int NONEMPTY_DOCUMENT = 7;

  /**
   * A document that's been closed and cannot be accessed.
   */
  static final int CLOSED = 8;

  /**
   * Renders the path in a JSON document to a string. The {@code pathNames} and {@code pathIndices}
   * parameters corresponds directly to stack: At indices where the stack contains an object
   * (EMPTY_OBJECT, DANGLING_NAME or NONEMPTY_OBJECT), pathNames contains the name at this scope.
   * Where it contains an array (EMPTY_ARRAY, NONEMPTY_ARRAY) pathIndices contains the current index
   * in that array. Otherwise the value is undefined, and we take advantage of that by incrementing
   * pathIndices when doing so isn't useful.
   */
  static String getPath(int stackSize, int[] stack, String[] pathNames, int[] pathIndices) {
    StringBuilder result = new StringBuilder().append('$');
    for (int i = 0; i < stackSize; i++) {
      switch (stack[i]) {
        case EMPTY_ARRAY:
        case NONEMPTY_ARRAY:
          result.append('[').append(pathIndices[i]).append(']');
          break;

        case EMPTY_OBJECT:
        case DANGLING_NAME:
        case NONEMPTY_OBJECT:
          result.append('.');
          if (pathNames[i] != null) {
            result.append(pathNames[i]);
          }
          break;

        case NONEMPTY_DOCUMENT:
        case EMPTY_DOCUMENT:
        case CLOSED:
          break;
      }
    }
    return result.toString();
  }
}