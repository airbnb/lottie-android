package com.airbnb.lottie.model.layer;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Typeface;

import androidx.annotation.Nullable;
import androidx.collection.LongSparseArray;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.LottieDrawable;
import com.airbnb.lottie.LottieProperty;
import com.airbnb.lottie.TextDelegate;
import com.airbnb.lottie.animation.content.ContentGroup;
import com.airbnb.lottie.animation.keyframe.BaseKeyframeAnimation;
import com.airbnb.lottie.animation.keyframe.TextKeyframeAnimation;
import com.airbnb.lottie.animation.keyframe.ValueCallbackKeyframeAnimation;
import com.airbnb.lottie.model.DocumentData;
import com.airbnb.lottie.model.Font;
import com.airbnb.lottie.model.FontCharacter;
import com.airbnb.lottie.model.animatable.AnimatableTextProperties;
import com.airbnb.lottie.model.content.ShapeGroup;
import com.airbnb.lottie.utils.Utils;
import com.airbnb.lottie.value.LottieValueCallback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TextLayer extends BaseLayer {
  // Capacity is 2 because emojis are 2 characters. Some are longer in which case, the capacity will
  // be expanded but that should be pretty rare.
  private final StringBuilder stringBuilder = new StringBuilder(2);
  private final RectF rectF = new RectF();
  private final Matrix matrix = new Matrix();
  private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG) {{
    setStyle(Style.FILL);
  }};
  private final Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG) {{
    setStyle(Style.STROKE);
  }};
  private final Map<FontCharacter, List<ContentGroup>> contentsForCharacter = new HashMap<>();
  private final LongSparseArray<String> codePointCache = new LongSparseArray<>();
  /**
   * If this is paragraph text, one line may wrap depending on the size of the document data box.
   */
  private final List<TextSubLine> textSubLines = new ArrayList<>();
  private final TextKeyframeAnimation textAnimation;
  private final LottieDrawable lottieDrawable;
  private final LottieComposition composition;
  @Nullable
  private BaseKeyframeAnimation<Integer, Integer> colorAnimation;
  @Nullable
  private BaseKeyframeAnimation<Integer, Integer> colorCallbackAnimation;
  @Nullable
  private BaseKeyframeAnimation<Integer, Integer> strokeColorAnimation;
  @Nullable
  private BaseKeyframeAnimation<Integer, Integer> strokeColorCallbackAnimation;
  @Nullable
  private BaseKeyframeAnimation<Float, Float> strokeWidthAnimation;
  @Nullable
  private BaseKeyframeAnimation<Float, Float> strokeWidthCallbackAnimation;
  @Nullable
  private BaseKeyframeAnimation<Float, Float> trackingAnimation;
  @Nullable
  private BaseKeyframeAnimation<Float, Float> trackingCallbackAnimation;
  @Nullable
  private BaseKeyframeAnimation<Float, Float> textSizeCallbackAnimation;
  @Nullable
  private BaseKeyframeAnimation<Typeface, Typeface> typefaceCallbackAnimation;

  TextLayer(LottieDrawable lottieDrawable, Layer layerModel) {
    super(lottieDrawable, layerModel);
    this.lottieDrawable = lottieDrawable;
    composition = layerModel.getComposition();
    //noinspection ConstantConditions
    textAnimation = layerModel.getText().createAnimation();
    textAnimation.addUpdateListener(this);
    addAnimation(textAnimation);

    AnimatableTextProperties textProperties = layerModel.getTextProperties();
    if (textProperties != null && textProperties.color != null) {
      colorAnimation = textProperties.color.createAnimation();
      colorAnimation.addUpdateListener(this);
      addAnimation(colorAnimation);
    }

    if (textProperties != null && textProperties.stroke != null) {
      strokeColorAnimation = textProperties.stroke.createAnimation();
      strokeColorAnimation.addUpdateListener(this);
      addAnimation(strokeColorAnimation);
    }

    if (textProperties != null && textProperties.strokeWidth != null) {
      strokeWidthAnimation = textProperties.strokeWidth.createAnimation();
      strokeWidthAnimation.addUpdateListener(this);
      addAnimation(strokeWidthAnimation);
    }

    if (textProperties != null && textProperties.tracking != null) {
      trackingAnimation = textProperties.tracking.createAnimation();
      trackingAnimation.addUpdateListener(this);
      addAnimation(trackingAnimation);
    }
  }

  @Override
  public void getBounds(RectF outBounds, Matrix parentMatrix, boolean applyParents) {
    super.getBounds(outBounds, parentMatrix, applyParents);
    // TODO: use the correct text bounds.
    outBounds.set(0, 0, composition.getBounds().width(), composition.getBounds().height());
  }

  @Override
  void drawLayer(Canvas canvas, Matrix parentMatrix, int parentAlpha) {
    DocumentData documentData = textAnimation.getValue();
    Font font = composition.getFonts().get(documentData.fontName);
    if (font == null) {
      return;
    }
    canvas.save();

    if (!lottieDrawable.useTextGlyphs()) {
      // Parent matrices are applied directly to the glyph paths.
      canvas.concat(parentMatrix);
    }
    // DO NOT SUBMIT
    if (documentData.boxSize != null & documentData.boxPosition != null) {
      if (lottieDrawable.useTextGlyphs()) {
        canvas.save();
        canvas.concat(parentMatrix);
      }
      Paint paint = new Paint();
      paint.setColor(Color.BLUE);
      paint.setAlpha(128);
      canvas.drawRect(documentData.boxPosition.x, documentData.boxPosition.y, documentData.boxPosition.x + documentData.boxSize.x,
          documentData.boxPosition.y + documentData.boxSize.y, paint);
      if (lottieDrawable.useTextGlyphs()) {
        canvas.restore();
      }
    }

    configurePaint(documentData, parentMatrix);

    if (lottieDrawable.useTextGlyphs()) {
      drawTextWithGlyphs(documentData, parentMatrix, font, canvas);
    } else {
      drawTextWithFont(documentData, font, canvas);
    }

    canvas.restore();
  }

  private void configurePaint(DocumentData documentData, Matrix parentMatrix) {
    if (colorCallbackAnimation != null) {
      fillPaint.setColor(colorCallbackAnimation.getValue());
    } else if (colorAnimation != null) {
      fillPaint.setColor(colorAnimation.getValue());
    } else {
      fillPaint.setColor(documentData.color);
    }

    if (strokeColorCallbackAnimation != null) {
      strokePaint.setColor(strokeColorCallbackAnimation.getValue());
    } else if (strokeColorAnimation != null) {
      strokePaint.setColor(strokeColorAnimation.getValue());
    } else {
      strokePaint.setColor(documentData.strokeColor);
    }
    int opacity = transform.getOpacity() == null ? 100 : transform.getOpacity().getValue();
    int alpha = opacity * 255 / 100;
    fillPaint.setAlpha(alpha);
    strokePaint.setAlpha(alpha);

    if (strokeWidthCallbackAnimation != null) {
      strokePaint.setStrokeWidth(strokeWidthCallbackAnimation.getValue());
    } else if (strokeWidthAnimation != null) {
      strokePaint.setStrokeWidth(strokeWidthAnimation.getValue());
    } else {
      float parentScale = Utils.getScale(parentMatrix);
      strokePaint.setStrokeWidth(documentData.strokeWidth * Utils.dpScale() * parentScale);
    }
  }

  private void drawTextWithGlyphs(
      DocumentData documentData, Matrix parentMatrix, Font font, Canvas canvas) {
    float textSize;
    if (textSizeCallbackAnimation != null) {
      textSize = textSizeCallbackAnimation.getValue();
    } else {
      textSize = documentData.size;
    }
    float fontScale = textSize / 100f;
    float parentScale = Utils.getScale(parentMatrix);

    String text = documentData.text;

    // Split full text in multiple lines
    List<String> textLines = getTextLines(text);
    int textLineCount = textLines.size();
    // Add tracking
    float tracking = documentData.tracking / 10f;
    if (trackingCallbackAnimation != null) {
      tracking += trackingCallbackAnimation.getValue();
    } else if (trackingAnimation != null) {
      tracking += trackingAnimation.getValue();
    }
    int lineIndex = -1;
    for (int i = 0; i < textLineCount; i++) {
      String textLine = textLines.get(i);
      float boxWidth = documentData.boxSize == null ? 0f : documentData.boxSize.x;
      List<TextSubLine> lines = splitGlyphTextIntoLines(textLine, boxWidth, font, fontScale, tracking, parentScale, true);
      for (int j = 0; j < lines.size(); j++) {
        TextSubLine line = lines.get(j);
        lineIndex++;

        canvas.save();

        offsetCanvas(canvas, documentData, lineIndex, line.width);
        drawGlyphTextLine(line.text, documentData, parentMatrix, font, canvas, parentScale, fontScale, tracking);

        canvas.restore();
      }
    }
  }

  private void drawGlyphTextLine(String text, DocumentData documentData, Matrix parentMatrix,
      Font font, Canvas canvas, float parentScale, float fontScale, float tracking) {
    for (int i = 0; i < text.length(); i++) {
      char c = text.charAt(i);
      int characterHash = FontCharacter.hashFor(c, font.getFamily(), font.getStyle());
      FontCharacter character = composition.getCharacters().get(characterHash);
      if (character == null) {
        // Something is wrong. Potentially, they didn't export the text as a glyph.
        continue;
      }
      drawCharacterAsGlyph(character, parentMatrix, fontScale, documentData, canvas);
      float tx = (float) character.getWidth() * fontScale * Utils.dpScale() * parentScale + tracking * parentScale;
      canvas.translate(tx, 0);
    }
  }

  private void drawTextWithFont(DocumentData documentData, Font font, Canvas canvas) {
    Typeface typeface = getTypeface(font);
    if (typeface == null) {
      return;
    }
    String text = documentData.text;
    TextDelegate textDelegate = lottieDrawable.getTextDelegate();
    if (textDelegate != null) {
      text = textDelegate.getTextInternal(getName(), text);
    }
    fillPaint.setTypeface(typeface);
    float textSize;
    if (textSizeCallbackAnimation != null) {
      textSize = textSizeCallbackAnimation.getValue();
    } else {
      textSize = documentData.size;
    }
    fillPaint.setTextSize(textSize * Utils.dpScale());
    strokePaint.setTypeface(fillPaint.getTypeface());
    strokePaint.setTextSize(fillPaint.getTextSize());

    // Calculate tracking
    float tracking = documentData.tracking / 10f;
    if (trackingCallbackAnimation != null) {
      tracking += trackingCallbackAnimation.getValue();
    } else if (trackingAnimation != null) {
      tracking += trackingAnimation.getValue();
    }
    tracking = tracking * Utils.dpScale() * textSize / 100.0f;

    // Split full text in multiple lines
    List<String> textLines = getTextLines(text);
    int textLineCount = textLines.size();
    int lineIndex = -1;
    for (int i = 0; i < textLineCount; i++) {
      String textLine = textLines.get(i);
      float boxWidth = documentData.boxSize == null ? 0f : documentData.boxSize.x;
      List<TextSubLine> lines = splitGlyphTextIntoLines(textLine, boxWidth, font, tracking, 0f, 0f, false);
      for (int j = 0; j < lines.size(); j++) {
        TextSubLine line = lines.get(j);
        lineIndex++;

        canvas.save();

        offsetCanvas(canvas, documentData, lineIndex, line.width);
        drawFontTextLine(line.text, documentData, canvas, tracking);

        canvas.restore();
      }
    }
  }

  private void offsetCanvas(Canvas canvas, DocumentData documentData, int lineIndex, float lineWidth) {
    PointF position = documentData.boxPosition;
    PointF size = documentData.boxSize;
    float lineOffset = lineIndex * documentData.lineHeight * Utils.dpScale();
    float lineStart = position == null ? 0f : position.x;
    float boxWidth = size == null ? 0f : size.x;
    switch (documentData.justification) {
      case LEFT_ALIGN:
        canvas.translate(lineStart, lineOffset);
        break;
      case RIGHT_ALIGN:
        canvas.translate(lineStart + boxWidth - lineWidth, lineOffset);
        break;
      case CENTER:
        canvas.translate(lineStart + boxWidth / 2f - lineWidth / 2f, lineOffset);
        break;
    }
  }

  @Nullable
  private Typeface getTypeface(Font font) {
    if (typefaceCallbackAnimation != null) {
      Typeface callbackTypeface = typefaceCallbackAnimation.getValue();
      if (callbackTypeface != null) {
        return callbackTypeface;
      }
    }
    Typeface drawableTypeface = lottieDrawable.getTypeface(font);
    if (drawableTypeface != null) {
      return drawableTypeface;
    }
    return font.getTypeface();
  }

  private List<String> getTextLines(String text) {
    // Split full text by carriage return character
    String formattedText = text.replaceAll("\r\n", "\r")
        .replaceAll("\u0003", "\r")
        .replaceAll("\n", "\r");
    String[] textLinesArray = formattedText.split("\r");
    return Arrays.asList(textLinesArray);
  }

  private void drawFontTextLine(String text, DocumentData documentData, Canvas canvas, float tracking) {
    for (int i = 0; i < text.length(); ) {
      String charString = codePointToString(text, i);
      i += charString.length();
      drawCharacterFromFont(charString, documentData, canvas);
      float charWidth = fillPaint.measureText(charString);
      float tx = charWidth + tracking;
      canvas.translate(tx, 0);
    }
  }

  private List<TextSubLine> splitGlyphTextIntoLines(
      String textLine, float boxWidth, Font font, float tracking, float fontScale, float parentScale, boolean usingGlyphs) {
    int lineCount = 0;

    float currentLineWidth = 0;
    int currentLineStartIndex = 0;

    int currentWordStartIndex = 0;
    float currentWordWidth = 0f;
    boolean nextCharacterStartsWord = false;

    // The measured size of a space.
    float spaceWidth = 0f;

    for (int i = 0; i < textLine.length(); i++) {
      char c = textLine.charAt(i);
      int characterHash = FontCharacter.hashFor(c, font.getFamily(), font.getStyle());
      FontCharacter character = composition.getCharacters().get(characterHash);
      if (character == null) {
        continue;
      }

      float currentCharWidth;
      if (usingGlyphs) {
        currentCharWidth = (float) character.getWidth() * fontScale * Utils.dpScale() * parentScale + tracking * parentScale;
      } else {
        currentCharWidth = fillPaint.measureText(textLine.substring(i, i + 1)) + tracking;
      }

      if (c == ' ') {
        spaceWidth = currentCharWidth;
        nextCharacterStartsWord = true;
      } else if (nextCharacterStartsWord) {
        nextCharacterStartsWord = false;
        currentWordStartIndex = i;
        currentWordWidth = currentCharWidth;
      } else {
        currentWordWidth += currentCharWidth;
      }
      currentLineWidth += currentCharWidth;

      if (boxWidth > 0f && currentLineWidth >= boxWidth) {
        if (c == ' ') {
          // Spaces at the end of a line don't do anything. Ignore it.
          // The next non-space character will hit the conditions below.
          continue;
        }
        TextSubLine subLine = ensureEnoughSubLines(++lineCount);
        if (currentWordStartIndex == currentLineStartIndex) {
          // Only word on line is wider than box, start wrapping mid-word.
          String substr = textLine.substring(currentLineStartIndex, i);
          String trimmed = substr.trim();
          float trimmedSpace = (trimmed.length() - substr.length()) * spaceWidth;
          subLine.set(trimmed, currentLineWidth - currentCharWidth - trimmedSpace);
          currentLineStartIndex = i;
          currentLineWidth = currentCharWidth;
          currentWordStartIndex = currentLineStartIndex;
          currentWordWidth = currentCharWidth;
        } else {
          String substr = textLine.substring(currentLineStartIndex, currentWordStartIndex - 1);
          String trimmed = substr.trim();
          float trimmedSpace = (substr.length() - trimmed.length()) * spaceWidth;
          subLine.set(trimmed, currentLineWidth - currentWordWidth - trimmedSpace - spaceWidth);
          currentLineStartIndex = currentWordStartIndex;
          currentLineWidth = currentWordWidth;
        }
      }
    }
    if (currentLineWidth > 0f) {
      TextSubLine line = ensureEnoughSubLines(++lineCount);
      line.set(textLine.substring(currentLineStartIndex), currentLineWidth);
    }
    return textSubLines.subList(0, lineCount);
  }

  /**
   * Elements are reused and not deleted to save allocations.
   */
  private TextSubLine ensureEnoughSubLines(int numLines) {
    for (int i = textSubLines.size(); i < numLines; i++) {
      textSubLines.add(new TextSubLine());
    }
    return textSubLines.get(numLines - 1);
  }

  private void drawCharacterAsGlyph(
      FontCharacter character,
      Matrix parentMatrix,
      float fontScale,
      DocumentData documentData,
      Canvas canvas) {
    List<ContentGroup> contentGroups = getContentsForCharacter(character);
    for (int j = 0; j < contentGroups.size(); j++) {
      Path path = contentGroups.get(j).getPath();
      path.computeBounds(rectF, false);
      matrix.set(parentMatrix);
      matrix.preTranslate(0, -documentData.baselineShift * Utils.dpScale());
      matrix.preScale(fontScale, fontScale);
      path.transform(matrix);
      if (documentData.strokeOverFill) {
        drawGlyph(path, fillPaint, canvas);
        drawGlyph(path, strokePaint, canvas);
      } else {
        drawGlyph(path, strokePaint, canvas);
        drawGlyph(path, fillPaint, canvas);
      }
    }
  }

  private void drawGlyph(Path path, Paint paint, Canvas canvas) {
    if (paint.getColor() == Color.TRANSPARENT) {
      return;
    }
    if (paint.getStyle() == Paint.Style.STROKE && paint.getStrokeWidth() == 0) {
      return;
    }
    canvas.drawPath(path, paint);
  }

  private void drawCharacterFromFont(String character, DocumentData documentData, Canvas canvas) {
    if (documentData.strokeOverFill) {
      drawCharacter(character, fillPaint, canvas);
      drawCharacter(character, strokePaint, canvas);
    } else {
      drawCharacter(character, strokePaint, canvas);
      drawCharacter(character, fillPaint, canvas);
    }
  }

  private void drawCharacter(String character, Paint paint, Canvas canvas) {
    if (paint.getColor() == Color.TRANSPARENT) {
      return;
    }
    if (paint.getStyle() == Paint.Style.STROKE && paint.getStrokeWidth() == 0) {
      return;
    }
    canvas.drawText(character, 0, character.length(), 0, 0, paint);
  }

  private List<ContentGroup> getContentsForCharacter(FontCharacter character) {
    if (contentsForCharacter.containsKey(character)) {
      return contentsForCharacter.get(character);
    }
    List<ShapeGroup> shapes = character.getShapes();
    int size = shapes.size();
    List<ContentGroup> contents = new ArrayList<>(size);
    for (int i = 0; i < size; i++) {
      ShapeGroup sg = shapes.get(i);
      contents.add(new ContentGroup(lottieDrawable, this, sg, composition));
    }
    contentsForCharacter.put(character, contents);
    return contents;
  }

  private String codePointToString(String text, int startIndex) {
    int firstCodePoint = text.codePointAt(startIndex);
    int firstCodePointLength = Character.charCount(firstCodePoint);
    int key = firstCodePoint;
    int index = startIndex + firstCodePointLength;
    while (index < text.length()) {
      int nextCodePoint = text.codePointAt(index);
      if (!isModifier(nextCodePoint)) {
        break;
      }
      int nextCodePointLength = Character.charCount(nextCodePoint);
      index += nextCodePointLength;
      key = key * 31 + nextCodePoint;
    }

    if (codePointCache.containsKey(key)) {
      return codePointCache.get(key);
    }

    stringBuilder.setLength(0);
    for (int i = startIndex; i < index; ) {
      int codePoint = text.codePointAt(i);
      stringBuilder.appendCodePoint(codePoint);
      i += Character.charCount(codePoint);
    }
    String str = stringBuilder.toString();
    codePointCache.put(key, str);
    return str;
  }

  private boolean isModifier(int codePoint) {
    return Character.getType(codePoint) == Character.FORMAT ||
        Character.getType(codePoint) == Character.MODIFIER_SYMBOL ||
        Character.getType(codePoint) == Character.NON_SPACING_MARK ||
        Character.getType(codePoint) == Character.OTHER_SYMBOL ||
        Character.getType(codePoint) == Character.DIRECTIONALITY_NONSPACING_MARK ||
        Character.getType(codePoint) == Character.SURROGATE;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> void addValueCallback(T property, @Nullable LottieValueCallback<T> callback) {
    super.addValueCallback(property, callback);
    if (property == LottieProperty.COLOR) {
      if (colorCallbackAnimation != null) {
        removeAnimation(colorCallbackAnimation);
      }

      if (callback == null) {
        colorCallbackAnimation = null;
      } else {
        colorCallbackAnimation = new ValueCallbackKeyframeAnimation<>((LottieValueCallback<Integer>) callback);
        colorCallbackAnimation.addUpdateListener(this);
        addAnimation(colorCallbackAnimation);
      }
    } else if (property == LottieProperty.STROKE_COLOR) {
      if (strokeColorCallbackAnimation != null) {
        removeAnimation(strokeColorCallbackAnimation);
      }

      if (callback == null) {
        strokeColorCallbackAnimation = null;
      } else {
        strokeColorCallbackAnimation = new ValueCallbackKeyframeAnimation<>((LottieValueCallback<Integer>) callback);
        strokeColorCallbackAnimation.addUpdateListener(this);
        addAnimation(strokeColorCallbackAnimation);
      }
    } else if (property == LottieProperty.STROKE_WIDTH) {
      if (strokeWidthCallbackAnimation != null) {
        removeAnimation(strokeWidthCallbackAnimation);
      }

      if (callback == null) {
        strokeWidthCallbackAnimation = null;
      } else {
        strokeWidthCallbackAnimation = new ValueCallbackKeyframeAnimation<>((LottieValueCallback<Float>) callback);
        strokeWidthCallbackAnimation.addUpdateListener(this);
        addAnimation(strokeWidthCallbackAnimation);
      }
    } else if (property == LottieProperty.TEXT_TRACKING) {
      if (trackingCallbackAnimation != null) {
        removeAnimation(trackingCallbackAnimation);
      }

      if (callback == null) {
        trackingCallbackAnimation = null;
      } else {
        trackingCallbackAnimation = new ValueCallbackKeyframeAnimation<>((LottieValueCallback<Float>) callback);
        trackingCallbackAnimation.addUpdateListener(this);
        addAnimation(trackingCallbackAnimation);
      }
    } else if (property == LottieProperty.TEXT_SIZE) {
      if (textSizeCallbackAnimation != null) {
        removeAnimation(textSizeCallbackAnimation);
      }

      if (callback == null) {
        textSizeCallbackAnimation = null;
      } else {
        textSizeCallbackAnimation = new ValueCallbackKeyframeAnimation<>((LottieValueCallback<Float>) callback);
        textSizeCallbackAnimation.addUpdateListener(this);
        addAnimation(textSizeCallbackAnimation);
      }
    } else if (property == LottieProperty.TYPEFACE) {
      if (typefaceCallbackAnimation != null) {
        removeAnimation(typefaceCallbackAnimation);
      }

      if (callback == null) {
        typefaceCallbackAnimation = null;
      } else {
        typefaceCallbackAnimation = new ValueCallbackKeyframeAnimation<>((LottieValueCallback<Typeface>) callback);
        typefaceCallbackAnimation.addUpdateListener(this);
        addAnimation(typefaceCallbackAnimation);
      }
    } else if (property == LottieProperty.TEXT) {
      textAnimation.setStringValueCallback((LottieValueCallback<String>) callback);
    }
  }

  private static class TextSubLine {
    private String text = "";
    private float width = 0f;

    void set(String text, float width) {
      this.text = text;
      this.width = width;
    }

    void reset() {
      text = "";
      width = 0f;
    }
  }
}
