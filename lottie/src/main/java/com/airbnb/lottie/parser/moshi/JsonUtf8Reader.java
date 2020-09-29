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

import java.io.EOFException;
import java.io.IOException;

import androidx.annotation.Nullable;
import okio.Buffer;
import okio.BufferedSource;
import okio.ByteString;

final class JsonUtf8Reader extends JsonReader {
  private static final long MIN_INCOMPLETE_INTEGER = Long.MIN_VALUE / 10;

  private static final ByteString SINGLE_QUOTE_OR_SLASH = ByteString.encodeUtf8("'\\");
  private static final ByteString DOUBLE_QUOTE_OR_SLASH = ByteString.encodeUtf8("\"\\");
  private static final ByteString UNQUOTED_STRING_TERMINALS
      = ByteString.encodeUtf8("{}[]:, \n\t\r\f/\\;#=");
  private static final ByteString LINEFEED_OR_CARRIAGE_RETURN = ByteString.encodeUtf8("\n\r");
  private static final ByteString CLOSING_BLOCK_COMMENT = ByteString.encodeUtf8("*/");

  private static final int PEEKED_NONE = 0;
  private static final int PEEKED_BEGIN_OBJECT = 1;
  private static final int PEEKED_END_OBJECT = 2;
  private static final int PEEKED_BEGIN_ARRAY = 3;
  private static final int PEEKED_END_ARRAY = 4;
  private static final int PEEKED_TRUE = 5;
  private static final int PEEKED_FALSE = 6;
  private static final int PEEKED_NULL = 7;
  private static final int PEEKED_SINGLE_QUOTED = 8;
  private static final int PEEKED_DOUBLE_QUOTED = 9;
  private static final int PEEKED_UNQUOTED = 10;
  /** When this is returned, the string value is stored in peekedString. */
  private static final int PEEKED_BUFFERED = 11;
  private static final int PEEKED_SINGLE_QUOTED_NAME = 12;
  private static final int PEEKED_DOUBLE_QUOTED_NAME = 13;
  private static final int PEEKED_UNQUOTED_NAME = 14;
  private static final int PEEKED_BUFFERED_NAME = 15;
  /** When this is returned, the integer value is stored in peekedLong. */
  private static final int PEEKED_LONG = 16;
  private static final int PEEKED_NUMBER = 17;
  private static final int PEEKED_EOF = 18;

  /* State machine when parsing numbers */
  private static final int NUMBER_CHAR_NONE = 0;
  private static final int NUMBER_CHAR_SIGN = 1;
  private static final int NUMBER_CHAR_DIGIT = 2;
  private static final int NUMBER_CHAR_DECIMAL = 3;
  private static final int NUMBER_CHAR_FRACTION_DIGIT = 4;
  private static final int NUMBER_CHAR_EXP_E = 5;
  private static final int NUMBER_CHAR_EXP_SIGN = 6;
  private static final int NUMBER_CHAR_EXP_DIGIT = 7;

  /** The input JSON. */
  private final BufferedSource source;
  private final Buffer buffer;

  private int peeked = PEEKED_NONE;

  /**
   * A peeked value that was composed entirely of digits with an optional
   * leading dash. Positive values may not have a leading 0.
   */
  private long peekedLong;

  /**
   * The number of characters in a peeked number literal.
   */
  private int peekedNumberLength;

  /**
   * A peeked string that should be parsed on the next double, long or string.
   * This is populated before a numeric value is parsed and used if that parsing
   * fails.
   */
  private @Nullable
  String peekedString;

  JsonUtf8Reader(BufferedSource source) {
    if (source == null) {
      throw new NullPointerException("source == null");
    }
    this.source = source;
    // Don't use source.getBuffer(). Because android studio use old version okio instead of your own okio.
    this.buffer = source.buffer();
    pushScope(JsonScope.EMPTY_DOCUMENT);
  }


  @Override public void beginArray() throws IOException {
    int p = peeked;
    if (p == PEEKED_NONE) {
      p = doPeek();
    }
    if (p == PEEKED_BEGIN_ARRAY) {
      pushScope(JsonScope.EMPTY_ARRAY);
      pathIndices[stackSize - 1] = 0;
      peeked = PEEKED_NONE;
    } else {
      throw new JsonDataException("Expected BEGIN_ARRAY but was " + peek()
          + " at path " + getPath());
    }
  }

  @Override public void endArray() throws IOException {
    int p = peeked;
    if (p == PEEKED_NONE) {
      p = doPeek();
    }
    if (p == PEEKED_END_ARRAY) {
      stackSize--;
      pathIndices[stackSize - 1]++;
      peeked = PEEKED_NONE;
    } else {
      throw new JsonDataException("Expected END_ARRAY but was " + peek()
          + " at path " + getPath());
    }
  }

  @Override public void beginObject() throws IOException {
    int p = peeked;
    if (p == PEEKED_NONE) {
      p = doPeek();
    }
    if (p == PEEKED_BEGIN_OBJECT) {
      pushScope(JsonScope.EMPTY_OBJECT);
      peeked = PEEKED_NONE;
    } else {
      throw new JsonDataException("Expected BEGIN_OBJECT but was " + peek()
          + " at path " + getPath());
    }
  }

  @Override public void endObject() throws IOException {
    int p = peeked;
    if (p == PEEKED_NONE) {
      p = doPeek();
    }
    if (p == PEEKED_END_OBJECT) {
      stackSize--;
      pathNames[stackSize] = null; // Free the last path name so that it can be garbage collected!
      pathIndices[stackSize - 1]++;
      peeked = PEEKED_NONE;
    } else {
      throw new JsonDataException("Expected END_OBJECT but was " + peek()
          + " at path " + getPath());
    }
  }

  @Override public boolean hasNext() throws IOException {
    int p = peeked;
    if (p == PEEKED_NONE) {
      p = doPeek();
    }
    return p != PEEKED_END_OBJECT && p != PEEKED_END_ARRAY && p != PEEKED_EOF;
  }

  @Override public Token peek() throws IOException {
    int p = peeked;
    if (p == PEEKED_NONE) {
      p = doPeek();
    }

    switch (p) {
      case PEEKED_BEGIN_OBJECT:
        return Token.BEGIN_OBJECT;
      case PEEKED_END_OBJECT:
        return Token.END_OBJECT;
      case PEEKED_BEGIN_ARRAY:
        return Token.BEGIN_ARRAY;
      case PEEKED_END_ARRAY:
        return Token.END_ARRAY;
      case PEEKED_SINGLE_QUOTED_NAME:
      case PEEKED_DOUBLE_QUOTED_NAME:
      case PEEKED_UNQUOTED_NAME:
      case PEEKED_BUFFERED_NAME:
        return Token.NAME;
      case PEEKED_TRUE:
      case PEEKED_FALSE:
        return Token.BOOLEAN;
      case PEEKED_NULL:
        return Token.NULL;
      case PEEKED_SINGLE_QUOTED:
      case PEEKED_DOUBLE_QUOTED:
      case PEEKED_UNQUOTED:
      case PEEKED_BUFFERED:
        return Token.STRING;
      case PEEKED_LONG:
      case PEEKED_NUMBER:
        return Token.NUMBER;
      case PEEKED_EOF:
        return Token.END_DOCUMENT;
      default:
        throw new AssertionError();
    }
  }

  private int doPeek() throws IOException {
    int peekStack = scopes[stackSize - 1];
    if (peekStack == JsonScope.EMPTY_ARRAY) {
      scopes[stackSize - 1] = JsonScope.NONEMPTY_ARRAY;
    } else if (peekStack == JsonScope.NONEMPTY_ARRAY) {
      // Look for a comma before the next element.
      int c = nextNonWhitespace(true);
      buffer.readByte(); // consume ']' or ','.
      switch (c) {
        case ']':
          return peeked = PEEKED_END_ARRAY;
        case ';':
          checkLenient(); // fall-through
        case ',':
          break;
        default:
          throw syntaxError("Unterminated array");
      }
    } else if (peekStack == JsonScope.EMPTY_OBJECT || peekStack == JsonScope.NONEMPTY_OBJECT) {
      scopes[stackSize - 1] = JsonScope.DANGLING_NAME;
      // Look for a comma before the next element.
      if (peekStack == JsonScope.NONEMPTY_OBJECT) {
        int c = nextNonWhitespace(true);
        buffer.readByte(); // Consume '}' or ','.
        switch (c) {
          case '}':
            return peeked = PEEKED_END_OBJECT;
          case ';':
            checkLenient(); // fall-through
          case ',':
            break;
          default:
            throw syntaxError("Unterminated object");
        }
      }
      int c = nextNonWhitespace(true);
      switch (c) {
        case '"':
          buffer.readByte(); // consume the '\"'.
          return peeked = PEEKED_DOUBLE_QUOTED_NAME;
        case '\'':
          buffer.readByte(); // consume the '\''.
          checkLenient();
          return peeked = PEEKED_SINGLE_QUOTED_NAME;
        case '}':
          if (peekStack != JsonScope.NONEMPTY_OBJECT) {
            buffer.readByte(); // consume the '}'.
            return peeked = PEEKED_END_OBJECT;
          } else {
            throw syntaxError("Expected name");
          }
        default:
          checkLenient();
          if (isLiteral((char) c)) {
            return peeked = PEEKED_UNQUOTED_NAME;
          } else {
            throw syntaxError("Expected name");
          }
      }
    } else if (peekStack == JsonScope.DANGLING_NAME) {
      scopes[stackSize - 1] = JsonScope.NONEMPTY_OBJECT;
      // Look for a colon before the value.
      int c = nextNonWhitespace(true);
      buffer.readByte(); // Consume ':'.
      switch (c) {
        case ':':
          break;
        case '=':
          checkLenient();
          if (source.request(1) && buffer.getByte(0) == '>') {
            buffer.readByte(); // Consume '>'.
          }
          break;
        default:
          throw syntaxError("Expected ':'");
      }
    } else if (peekStack == JsonScope.EMPTY_DOCUMENT) {
      scopes[stackSize - 1] = JsonScope.NONEMPTY_DOCUMENT;
    } else if (peekStack == JsonScope.NONEMPTY_DOCUMENT) {
      int c = nextNonWhitespace(false);
      if (c == -1) {
        return peeked = PEEKED_EOF;
      } else {
        checkLenient();
      }
    } else if (peekStack == JsonScope.CLOSED) {
      throw new IllegalStateException("JsonReader is closed");
    }

    int c = nextNonWhitespace(true);
    switch (c) {
      case ']':
        if (peekStack == JsonScope.EMPTY_ARRAY) {
          buffer.readByte(); // Consume ']'.
          return peeked = PEEKED_END_ARRAY;
        }
        // fall-through to handle ",]"
      case ';':
      case ',':
        // In lenient mode, a 0-length literal in an array means 'null'.
        if (peekStack == JsonScope.EMPTY_ARRAY || peekStack == JsonScope.NONEMPTY_ARRAY) {
          checkLenient();
          return peeked = PEEKED_NULL;
        } else {
          throw syntaxError("Unexpected value");
        }
      case '\'':
        checkLenient();
        buffer.readByte(); // Consume '\''.
        return peeked = PEEKED_SINGLE_QUOTED;
      case '"':
        buffer.readByte(); // Consume '\"'.
        return peeked = PEEKED_DOUBLE_QUOTED;
      case '[':
        buffer.readByte(); // Consume '['.
        return peeked = PEEKED_BEGIN_ARRAY;
      case '{':
        buffer.readByte(); // Consume '{'.
        return peeked = PEEKED_BEGIN_OBJECT;
      default:
    }

    int result = peekKeyword();
    if (result != PEEKED_NONE) {
      return result;
    }

    result = peekNumber();
    if (result != PEEKED_NONE) {
      return result;
    }

    if (!isLiteral(buffer.getByte(0))) {
      throw syntaxError("Expected value");
    }

    checkLenient();
    return peeked = PEEKED_UNQUOTED;
  }

  private int peekKeyword() throws IOException {
    // Figure out which keyword we're matching against by its first character.
    byte c = buffer.getByte(0);
    String keyword;
    String keywordUpper;
    int peeking;
    if (c == 't' || c == 'T') {
      keyword = "true";
      keywordUpper = "TRUE";
      peeking = PEEKED_TRUE;
    } else if (c == 'f' || c == 'F') {
      keyword = "false";
      keywordUpper = "FALSE";
      peeking = PEEKED_FALSE;
    } else if (c == 'n' || c == 'N') {
      keyword = "null";
      keywordUpper = "NULL";
      peeking = PEEKED_NULL;
    } else {
      return PEEKED_NONE;
    }

    // Confirm that chars [1..length) match the keyword.
    int length = keyword.length();
    for (int i = 1; i < length; i++) {
      if (!source.request(i + 1)) {
        return PEEKED_NONE;
      }
      c = buffer.getByte(i);
      if (c != keyword.charAt(i) && c != keywordUpper.charAt(i)) {
        return PEEKED_NONE;
      }
    }

    if (source.request(length + 1) && isLiteral(buffer.getByte(length))) {
      return PEEKED_NONE; // Don't match trues, falsey or nullsoft!
    }

    // We've found the keyword followed either by EOF or by a non-literal character.
    buffer.skip(length);
    return peeked = peeking;
  }

  private int peekNumber() throws IOException {
    long value = 0; // Negative to accommodate Long.MIN_VALUE more easily.
    boolean negative = false;
    boolean fitsInLong = true;
    int last = NUMBER_CHAR_NONE;

    int i = 0;

    charactersOfNumber:
    for (; true; i++) {
      if (!source.request(i + 1)) {
        break;
      }

      byte c = buffer.getByte(i);
      switch (c) {
        case '-':
          if (last == NUMBER_CHAR_NONE) {
            negative = true;
            last = NUMBER_CHAR_SIGN;
            continue;
          } else if (last == NUMBER_CHAR_EXP_E) {
            last = NUMBER_CHAR_EXP_SIGN;
            continue;
          }
          return PEEKED_NONE;

        case '+':
          if (last == NUMBER_CHAR_EXP_E) {
            last = NUMBER_CHAR_EXP_SIGN;
            continue;
          }
          return PEEKED_NONE;

        case 'e':
        case 'E':
          if (last == NUMBER_CHAR_DIGIT || last == NUMBER_CHAR_FRACTION_DIGIT) {
            last = NUMBER_CHAR_EXP_E;
            continue;
          }
          return PEEKED_NONE;

        case '.':
          if (last == NUMBER_CHAR_DIGIT) {
            last = NUMBER_CHAR_DECIMAL;
            continue;
          }
          return PEEKED_NONE;

        default:
          if (c < '0' || c > '9') {
            if (!isLiteral(c)) {
              break charactersOfNumber;
            }
            return PEEKED_NONE;
          }
          if (last == NUMBER_CHAR_SIGN || last == NUMBER_CHAR_NONE) {
            value = -(c - '0');
            last = NUMBER_CHAR_DIGIT;
          } else if (last == NUMBER_CHAR_DIGIT) {
            if (value == 0) {
              return PEEKED_NONE; // Leading '0' prefix is not allowed (since it could be octal).
            }
            long newValue = value * 10 - (c - '0');
            fitsInLong &= value > MIN_INCOMPLETE_INTEGER
                || (value == MIN_INCOMPLETE_INTEGER && newValue < value);
            value = newValue;
          } else if (last == NUMBER_CHAR_DECIMAL) {
            last = NUMBER_CHAR_FRACTION_DIGIT;
          } else if (last == NUMBER_CHAR_EXP_E || last == NUMBER_CHAR_EXP_SIGN) {
            last = NUMBER_CHAR_EXP_DIGIT;
          }
      }
    }

    // We've read a complete number. Decide if it's a PEEKED_LONG or a PEEKED_NUMBER.
    if (last == NUMBER_CHAR_DIGIT && fitsInLong && (value != Long.MIN_VALUE || negative)
        && (value != 0 || !negative)) {
      peekedLong = negative ? value : -value;
      buffer.skip(i);
      return peeked = PEEKED_LONG;
    } else if (last == NUMBER_CHAR_DIGIT || last == NUMBER_CHAR_FRACTION_DIGIT
        || last == NUMBER_CHAR_EXP_DIGIT) {
      peekedNumberLength = i;
      return peeked = PEEKED_NUMBER;
    } else {
      return PEEKED_NONE;
    }
  }

  private boolean isLiteral(int c) throws IOException {
    switch (c) {
      case '/':
      case '\\':
      case ';':
      case '#':
      case '=':
        checkLenient(); // fall-through
      case '{':
      case '}':
      case '[':
      case ']':
      case ':':
      case ',':
      case ' ':
      case '\t':
      case '\f':
      case '\r':
      case '\n':
        return false;
      default:
        return true;
    }
  }

  @Override public String nextName() throws IOException {
    int p = peeked;
    if (p == PEEKED_NONE) {
      p = doPeek();
    }
    String result;
    if (p == PEEKED_UNQUOTED_NAME) {
      result = nextUnquotedValue();
    } else if (p == PEEKED_DOUBLE_QUOTED_NAME) {
      result = nextQuotedValue(DOUBLE_QUOTE_OR_SLASH);
    } else if (p == PEEKED_SINGLE_QUOTED_NAME) {
      result = nextQuotedValue(SINGLE_QUOTE_OR_SLASH);
    } else if (p == PEEKED_BUFFERED_NAME) {
      result = peekedString;
    } else {
      throw new JsonDataException("Expected a name but was " + peek() + " at path " + getPath());
    }
    peeked = PEEKED_NONE;
    pathNames[stackSize - 1] = result;
    return result;
  }

  @Override public int selectName(Options options) throws IOException {
    int p = peeked;
    if (p == PEEKED_NONE) {
      p = doPeek();
    }
    if (p < PEEKED_SINGLE_QUOTED_NAME || p > PEEKED_BUFFERED_NAME) {
      return -1;
    }
    if (p == PEEKED_BUFFERED_NAME) {
      return findName(peekedString, options);
    }

    int result = source.select(options.doubleQuoteSuffix);
    if (result != -1) {
      peeked = PEEKED_NONE;
      pathNames[stackSize - 1] = options.strings[result];

      return result;
    }

    // The next name may be unnecessary escaped. Save the last recorded path name, so that we
    // can restore the peek state in case we fail to find a match.
    String lastPathName = pathNames[stackSize - 1];

    String nextName = nextName();
    result = findName(nextName, options);

    if (result == -1) {
      peeked = PEEKED_BUFFERED_NAME;
      peekedString = nextName;
      // We can't push the path further, make it seem like nothing happened.
      pathNames[stackSize - 1] = lastPathName;
    }

    return result;
  }

  @Override public void skipName() throws IOException {
    if (failOnUnknown) {
      throw new JsonDataException("Cannot skip unexpected " + peek() + " at " + getPath());
    }
    int p = peeked;
    if (p == PEEKED_NONE) {
      p = doPeek();
    }
    if (p == PEEKED_UNQUOTED_NAME) {
      skipUnquotedValue();
    } else if (p == PEEKED_DOUBLE_QUOTED_NAME) {
      skipQuotedValue(DOUBLE_QUOTE_OR_SLASH);
    } else if (p == PEEKED_SINGLE_QUOTED_NAME) {
      skipQuotedValue(SINGLE_QUOTE_OR_SLASH);
    } else if (p != PEEKED_BUFFERED_NAME) {
      throw new JsonDataException("Expected a name but was " + peek() + " at path " + getPath());
    }
    peeked = PEEKED_NONE;
    pathNames[stackSize - 1] = "null";
  }

  /**
   * If {@code name} is in {@code options} this consumes it and returns its index.
   * Otherwise this returns -1 and no name is consumed.
   */
  private int findName(String name, Options options) {
    for (int i = 0, size = options.strings.length; i < size; i++) {
      if (name.equals(options.strings[i])) {
        peeked = PEEKED_NONE;
        pathNames[stackSize - 1] = name;

        return i;
      }
    }
    return -1;
  }

  @Override public String nextString() throws IOException {
    int p = peeked;
    if (p == PEEKED_NONE) {
      p = doPeek();
    }
    String result;
    if (p == PEEKED_UNQUOTED) {
      result = nextUnquotedValue();
    } else if (p == PEEKED_DOUBLE_QUOTED) {
      result = nextQuotedValue(DOUBLE_QUOTE_OR_SLASH);
    } else if (p == PEEKED_SINGLE_QUOTED) {
      result = nextQuotedValue(SINGLE_QUOTE_OR_SLASH);
    } else if (p == PEEKED_BUFFERED) {
      result = peekedString;
      peekedString = null;
    } else if (p == PEEKED_LONG) {
      result = Long.toString(peekedLong);
    } else if (p == PEEKED_NUMBER) {
      result = buffer.readUtf8(peekedNumberLength);
    } else {
      throw new JsonDataException("Expected a string but was " + peek() + " at path " + getPath());
    }
    peeked = PEEKED_NONE;
    pathIndices[stackSize - 1]++;
    return result;
  }

  /**
   * If {@code string} is in {@code options} this consumes it and returns its index.
   * Otherwise this returns -1 and no string is consumed.
   */
  private int findString(String string, Options options) {
    for (int i = 0, size = options.strings.length; i < size; i++) {
      if (string.equals(options.strings[i])) {
        peeked = PEEKED_NONE;
        pathIndices[stackSize - 1]++;

        return i;
      }
    }
    return -1;
  }

  @Override public boolean nextBoolean() throws IOException {
    int p = peeked;
    if (p == PEEKED_NONE) {
      p = doPeek();
    }
    if (p == PEEKED_TRUE) {
      peeked = PEEKED_NONE;
      pathIndices[stackSize - 1]++;
      return true;
    } else if (p == PEEKED_FALSE) {
      peeked = PEEKED_NONE;
      pathIndices[stackSize - 1]++;
      return false;
    }
    throw new JsonDataException("Expected a boolean but was " + peek() + " at path " + getPath());
  }

  @Override public double nextDouble() throws IOException {
    int p = peeked;
    if (p == PEEKED_NONE) {
      p = doPeek();
    }

    if (p == PEEKED_LONG) {
      peeked = PEEKED_NONE;
      pathIndices[stackSize - 1]++;
      return (double) peekedLong;
    }

    if (p == PEEKED_NUMBER) {
      peekedString = buffer.readUtf8(peekedNumberLength);
    } else if (p == PEEKED_DOUBLE_QUOTED) {
      peekedString = nextQuotedValue(DOUBLE_QUOTE_OR_SLASH);
    } else if (p == PEEKED_SINGLE_QUOTED) {
      peekedString = nextQuotedValue(SINGLE_QUOTE_OR_SLASH);
    } else if (p == PEEKED_UNQUOTED) {
      peekedString = nextUnquotedValue();
    } else if (p != PEEKED_BUFFERED) {
      throw new JsonDataException("Expected a double but was " + peek() + " at path " + getPath());
    }

    peeked = PEEKED_BUFFERED;
    double result;
    try {
      result = Double.parseDouble(peekedString);
    } catch (NumberFormatException e) {
      throw new JsonDataException("Expected a double but was " + peekedString
          + " at path " + getPath());
    }
    if (!lenient && (Double.isNaN(result) || Double.isInfinite(result))) {
      throw new JsonEncodingException("JSON forbids NaN and infinities: " + result
          + " at path " + getPath());
    }
    peekedString = null;
    peeked = PEEKED_NONE;
    pathIndices[stackSize - 1]++;
    return result;
  }

  /**
   * Returns the string up to but not including {@code quote}, unescaping any character escape
   * sequences encountered along the way. The opening quote should have already been read. This
   * consumes the closing quote, but does not include it in the returned string.
   *
   * @throws IOException if any unicode escape sequences are malformed.
   */
  private String nextQuotedValue(ByteString runTerminator) throws IOException {
    StringBuilder builder = null;
    while (true) {
      long index = source.indexOfElement(runTerminator);
      if (index == -1L) throw syntaxError("Unterminated string");

      // If we've got an escape character, we're going to need a string builder.
      if (buffer.getByte(index) == '\\') {
        if (builder == null) builder = new StringBuilder();
        builder.append(buffer.readUtf8(index));
        buffer.readByte(); // '\'
        builder.append(readEscapeCharacter());
        continue;
      }

      // If it isn't the escape character, it's the quote. Return the string.
      if (builder == null) {
        String result = buffer.readUtf8(index);
        buffer.readByte(); // Consume the quote character.
        return result;
      } else {
        builder.append(buffer.readUtf8(index));
        buffer.readByte(); // Consume the quote character.
        return builder.toString();
      }
    }
  }

  /** Returns an unquoted value as a string. */
  private String nextUnquotedValue() throws IOException {
    long i = source.indexOfElement(UNQUOTED_STRING_TERMINALS);
    return i != -1 ? buffer.readUtf8(i) : buffer.readUtf8();
  }

  private void skipQuotedValue(ByteString runTerminator) throws IOException {
    while (true) {
      long index = source.indexOfElement(runTerminator);
      if (index == -1L) throw syntaxError("Unterminated string");

      if (buffer.getByte(index) == '\\') {
        buffer.skip(index + 1);
        readEscapeCharacter();
      } else {
        buffer.skip(index + 1);
        return;
      }
    }
  }

  private void skipUnquotedValue() throws IOException {
    long i = source.indexOfElement(UNQUOTED_STRING_TERMINALS);
    buffer.skip(i != -1L ? i : buffer.size());
  }

  @Override public int nextInt() throws IOException {
    int p = peeked;
    if (p == PEEKED_NONE) {
      p = doPeek();
    }

    int result;
    if (p == PEEKED_LONG) {
      result = (int) peekedLong;
      if (peekedLong != result) { // Make sure no precision was lost casting to 'int'.
        throw new JsonDataException("Expected an int but was " + peekedLong
            + " at path " + getPath());
      }
      peeked = PEEKED_NONE;
      pathIndices[stackSize - 1]++;
      return result;
    }

    if (p == PEEKED_NUMBER) {
      peekedString = buffer.readUtf8(peekedNumberLength);
    } else if (p == PEEKED_DOUBLE_QUOTED || p == PEEKED_SINGLE_QUOTED) {
      peekedString = p == PEEKED_DOUBLE_QUOTED
          ? nextQuotedValue(DOUBLE_QUOTE_OR_SLASH)
          : nextQuotedValue(SINGLE_QUOTE_OR_SLASH);
      try {
        result = Integer.parseInt(peekedString);
        peeked = PEEKED_NONE;
        pathIndices[stackSize - 1]++;
        return result;
      } catch (NumberFormatException ignored) {
        // Fall back to parse as a double below.
      }
    } else if (p != PEEKED_BUFFERED) {
      throw new JsonDataException("Expected an int but was " + peek() + " at path " + getPath());
    }

    peeked = PEEKED_BUFFERED;
    double asDouble;
    try {
      asDouble = Double.parseDouble(peekedString);
    } catch (NumberFormatException e) {
      throw new JsonDataException("Expected an int but was " + peekedString
          + " at path " + getPath());
    }
    result = (int) asDouble;
    if (result != asDouble) { // Make sure no precision was lost casting to 'int'.
      throw new JsonDataException("Expected an int but was " + peekedString
          + " at path " + getPath());
    }
    peekedString = null;
    peeked = PEEKED_NONE;
    pathIndices[stackSize - 1]++;
    return result;
  }

  @Override public void close() throws IOException {
    peeked = PEEKED_NONE;
    scopes[0] = JsonScope.CLOSED;
    stackSize = 1;
    buffer.clear();
    source.close();
  }

  @Override public void skipValue() throws IOException {
    if (failOnUnknown) {
      throw new JsonDataException("Cannot skip unexpected " + peek() + " at " + getPath());
    }
    int count = 0;
    do {
      int p = peeked;
      if (p == PEEKED_NONE) {
        p = doPeek();
      }

      if (p == PEEKED_BEGIN_ARRAY) {
        pushScope(JsonScope.EMPTY_ARRAY);
        count++;
      } else if (p == PEEKED_BEGIN_OBJECT) {
        pushScope(JsonScope.EMPTY_OBJECT);
        count++;
      } else if (p == PEEKED_END_ARRAY) {
        count--;
        if (count < 0) {
          throw new JsonDataException(
              "Expected a value but was " + peek() + " at path " + getPath());
        }
        stackSize--;
      } else if (p == PEEKED_END_OBJECT) {
        count--;
        if (count < 0) {
          throw new JsonDataException(
              "Expected a value but was " + peek() + " at path " + getPath());
        }
        stackSize--;
      } else if (p == PEEKED_UNQUOTED_NAME || p == PEEKED_UNQUOTED) {
        skipUnquotedValue();
      } else if (p == PEEKED_DOUBLE_QUOTED || p == PEEKED_DOUBLE_QUOTED_NAME) {
        skipQuotedValue(DOUBLE_QUOTE_OR_SLASH);
      } else if (p == PEEKED_SINGLE_QUOTED || p == PEEKED_SINGLE_QUOTED_NAME) {
        skipQuotedValue(SINGLE_QUOTE_OR_SLASH);
      } else if (p == PEEKED_NUMBER) {
        buffer.skip(peekedNumberLength);
      } else if (p == PEEKED_EOF) {
        throw new JsonDataException(
            "Expected a value but was " + peek() + " at path " + getPath());
      }
      peeked = PEEKED_NONE;
    } while (count != 0);

    pathIndices[stackSize - 1]++;
    pathNames[stackSize - 1] = "null";
  }

  /**
   * Returns the next character in the stream that is neither whitespace nor a
   * part of a comment. When this returns, the returned character is always at
   * {@code buffer.getByte(0)}.
   */
  private int nextNonWhitespace(boolean throwOnEof) throws IOException {
    /*
     * This code uses ugly local variables 'p' and 'l' representing the 'pos'
     * and 'limit' fields respectively. Using locals rather than fields saves
     * a few field reads for each whitespace character in a pretty-printed
     * document, resulting in a 5% speedup. We need to flush 'p' to its field
     * before any (potentially indirect) call to fillBuffer() and reread both
     * 'p' and 'l' after any (potentially indirect) call to the same method.
     */
    int p = 0;
    while (source.request(p + 1)) {
      int c = buffer.getByte(p++);
      if (c == '\n' || c == ' ' || c == '\r' || c == '\t') {
        continue;
      }

      buffer.skip(p - 1);
      if (c == '/') {
        if (!source.request(2)) {
          return c;
        }

        checkLenient();
        byte peek = buffer.getByte(1);
        switch (peek) {
          case '*':
            // skip a /* c-style comment */
            buffer.readByte(); // '/'
            buffer.readByte(); // '*'
            if (!skipToEndOfBlockComment()) {
              throw syntaxError("Unterminated comment");
            }
            p = 0;
            continue;

          case '/':
            // skip a // end-of-line comment
            buffer.readByte(); // '/'
            buffer.readByte(); // '/'
            skipToEndOfLine();
            p = 0;
            continue;

          default:
            return c;
        }
      } else if (c == '#') {
        // Skip a # hash end-of-line comment. The JSON RFC doesn't specify this behaviour, but it's
        // required to parse existing documents.
        checkLenient();
        skipToEndOfLine();
        p = 0;
      } else {
        return c;
      }
    }
    if (throwOnEof) {
      throw new EOFException("End of input");
    } else {
      return -1;
    }
  }

  private void checkLenient() throws IOException {
    if (!lenient) {
      throw syntaxError("Use JsonReader.setLenient(true) to accept malformed JSON");
    }
  }

  /**
   * Advances the position until after the next newline character. If the line
   * is terminated by "\r\n", the '\n' must be consumed as whitespace by the
   * caller.
   */
  private void skipToEndOfLine() throws IOException {
    long index = source.indexOfElement(LINEFEED_OR_CARRIAGE_RETURN);
    buffer.skip(index != -1 ? index + 1 : buffer.size());
  }

  /**
   * Skips through the next closing block comment.
   */
  private boolean skipToEndOfBlockComment() throws IOException {
    long index = source.indexOf(CLOSING_BLOCK_COMMENT);
    boolean found = index != -1;
    buffer.skip(found ? index + CLOSING_BLOCK_COMMENT.size() : buffer.size());
    return found;
  }


  @Override public String toString() {
    return "JsonReader(" + source + ")";
  }

  /**
   * Unescapes the character identified by the character or characters that immediately follow a
   * backslash. The backslash '\' should have already been read. This supports both unicode escapes
   * "u000A" and two-character escapes "\n".
   *
   * @throws IOException if any unicode escape sequences are malformed.
   */
  private char readEscapeCharacter() throws IOException {
    if (!source.request(1)) {
      throw syntaxError("Unterminated escape sequence");
    }

    byte escaped = buffer.readByte();
    switch (escaped) {
      case 'u':
        if (!source.request(4)) {
          throw new EOFException("Unterminated escape sequence at path " + getPath());
        }
        // Equivalent to Integer.parseInt(stringPool.get(buffer, pos, 4), 16);
        char result = 0;
        for (int i = 0, end = i + 4; i < end; i++) {
          byte c = buffer.getByte(i);
          result <<= 4;
          if (c >= '0' && c <= '9') {
            result += (c - '0');
          } else if (c >= 'a' && c <= 'f') {
            result += (c - 'a' + 10);
          } else if (c >= 'A' && c <= 'F') {
            result += (c - 'A' + 10);
          } else {
            throw syntaxError("\\u" + buffer.readUtf8(4));
          }
        }
        buffer.skip(4);
        return result;

      case 't':
        return '\t';

      case 'b':
        return '\b';

      case 'n':
        return '\n';

      case 'r':
        return '\r';

      case 'f':
        return '\f';

      case '\n':
      case '\'':
      case '"':
      case '\\':
      case '/':
        return (char) escaped;

      default:
        if (!lenient) throw syntaxError("Invalid escape sequence: \\" + (char) escaped);
        return (char) escaped;
    }
  }

}