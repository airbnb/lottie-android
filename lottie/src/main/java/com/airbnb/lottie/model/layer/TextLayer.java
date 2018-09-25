package com.airbnb.lottie.model.layer;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import androidx.annotation.Nullable;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.LottieDrawable;
import com.airbnb.lottie.LottieProperty;
import com.airbnb.lottie.TextDelegate;
import com.airbnb.lottie.animation.content.ContentGroup;
import com.airbnb.lottie.animation.keyframe.BaseKeyframeAnimation;
import com.airbnb.lottie.animation.keyframe.TextKeyframeAnimation;
import com.airbnb.lottie.model.DocumentData;
import com.airbnb.lottie.model.Font;
import com.airbnb.lottie.model.FontCharacter;
import com.airbnb.lottie.model.animatable.AnimatableTextProperties;
import com.airbnb.lottie.model.content.ShapeGroup;
import com.airbnb.lottie.utils.Utils;
import com.airbnb.lottie.value.LottieValueCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TextLayer extends BaseLayer {
  private final char[] tempCharArray = new char[1];
  private final RectF rectF = new RectF();
  private final Matrix matrix = new Matrix();
  private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG) {{
    setStyle(Style.FILL);
  }};
  private final Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG) {{
    setStyle(Style.STROKE);
  }};
  private final Map<FontCharacter, List<ContentGroup>> contentsForCharacter = new HashMap<>();
  private final TextKeyframeAnimation textAnimation;
  private final LottieDrawable lottieDrawable;
  private final LottieComposition composition;
  @Nullable private BaseKeyframeAnimation<Integer, Integer> colorAnimation;
  @Nullable private BaseKeyframeAnimation<Integer, Integer> strokeColorAnimation;
  @Nullable private BaseKeyframeAnimation<Float, Float> strokeWidthAnimation;
  @Nullable private BaseKeyframeAnimation<Float, Float> trackingAnimation;

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

  @Override void drawLayer(Canvas canvas, Matrix parentMatrix, int parentAlpha) {
    canvas.save();
    if (!lottieDrawable.useTextGlyphs()) {
      canvas.setMatrix(parentMatrix);
    }
    DocumentData documentData = textAnimation.getValue();
    Font font = composition.getFonts().get(documentData.fontName);
    if (font == null) {
      // Something is wrong.
      canvas.restore();
      return;
    }

    if (colorAnimation != null) {
      fillPaint.setColor(colorAnimation.getValue());
    } else {
      fillPaint.setColor(documentData.color);
    }

    if (strokeColorAnimation != null) {
      strokePaint.setColor(strokeColorAnimation.getValue());
    } else {
      strokePaint.setColor(documentData.strokeColor);
    }
    int alpha = transform.getOpacity().getValue() * 255 / 100;
    fillPaint.setAlpha(alpha);
    strokePaint.setAlpha(alpha);

    if (strokeWidthAnimation != null) {
      strokePaint.setStrokeWidth(strokeWidthAnimation.getValue());
    } else {
      float parentScale = Utils.getScale(parentMatrix);
      strokePaint.setStrokeWidth((float) (documentData.strokeWidth * Utils.dpScale() * parentScale));
    }

    if (lottieDrawable.useTextGlyphs()) {
      drawTextGlyphs(documentData, parentMatrix, font, canvas);
    } else {
      drawTextWithFont(documentData, font, parentMatrix, canvas);
    }

    canvas.restore();
  }

  private void drawTextGlyphs(
      DocumentData documentData, Matrix parentMatrix, Font font, Canvas canvas) {
    float fontScale = (float) documentData.size / 100f;
    float parentScale = Utils.getScale(parentMatrix);
    String text = documentData.text;


    for (int i = 0; i < text.length(); i++) {
      char c = text.charAt(i);
      int characterHash = FontCharacter.hashFor(c, font.getFamily(), font.getStyle());
      FontCharacter character = composition.getCharacters().get(characterHash);
      if (character == null) {
        // Something is wrong. Potentially, they didn't export the text as a glyph.
        continue;
      }
      drawCharacterAsGlyph(character, parentMatrix, fontScale, documentData, canvas);
      float tx = (float) character.getWidth() * fontScale * Utils.dpScale() * parentScale;
      // Add tracking
      float tracking = documentData.tracking / 10f;
      if (trackingAnimation != null) {
        tracking += trackingAnimation.getValue();
      }
      tx += tracking * parentScale;
      canvas.translate(tx, 0);
    }
  }

  private void drawTextWithFont(
      DocumentData documentData, Font font, Matrix parentMatrix, Canvas canvas) {
    float parentScale = Utils.getScale(parentMatrix);
    Typeface typeface = lottieDrawable.getTypeface(font.getFamily(), font.getStyle());
    if (typeface == null) {
      return;
    }
    String text = documentData.text;
    TextDelegate textDelegate = lottieDrawable.getTextDelegate();
    if (textDelegate != null) {
      text = textDelegate.getTextInternal(text);
    }
    fillPaint.setTypeface(typeface);
    fillPaint.setTextSize((float) (documentData.size * Utils.dpScale()));
    strokePaint.setTypeface(fillPaint.getTypeface());
    strokePaint.setTextSize(fillPaint.getTextSize());
    for (int i = 0; i < text.length(); i++) {
      char character = text.charAt(i);
      drawCharacterFromFont(character, documentData, canvas);
      tempCharArray[0] = character;
      float charWidth = fillPaint.measureText(tempCharArray, 0, 1);
      // Add tracking
      float tracking = documentData.tracking / 10f;
      if (trackingAnimation != null) {
        tracking += trackingAnimation.getValue();
      }
      float tx = charWidth + tracking * parentScale;
      canvas.translate(tx, 0);
    }
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
      matrix.preTranslate(0, (float) -documentData.baselineShift * Utils.dpScale());
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

  private void drawCharacterFromFont(char c, DocumentData documentData, Canvas canvas) {
    tempCharArray[0] = c;
    if (documentData.strokeOverFill) {
      drawCharacter(tempCharArray, fillPaint, canvas);
      drawCharacter(tempCharArray, strokePaint, canvas);
    } else {
      drawCharacter(tempCharArray, strokePaint, canvas);
      drawCharacter(tempCharArray, fillPaint, canvas);
    }
  }

  private void drawCharacter(char[] character, Paint paint, Canvas canvas) {
    if (paint.getColor() == Color.TRANSPARENT) {
      return;
    }
    if (paint.getStyle() == Paint.Style.STROKE && paint.getStrokeWidth() == 0) {
      return;
    }
    canvas.drawText(character, 0, 1, 0, 0, paint);
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
      contents.add(new ContentGroup(lottieDrawable, this, sg));
    }
    contentsForCharacter.put(character, contents);
    return contents;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> void addValueCallback(T property, @Nullable LottieValueCallback<T> callback) {
    super.addValueCallback(property, callback);
    if (property == LottieProperty.COLOR && colorAnimation != null) {
      colorAnimation.setValueCallback((LottieValueCallback<Integer>) callback);
    } else if (property == LottieProperty.STROKE_COLOR && strokeColorAnimation != null) {
      strokeColorAnimation.setValueCallback((LottieValueCallback<Integer>) callback);
    } else if (property == LottieProperty.STROKE_WIDTH && strokeWidthAnimation != null) {
      strokeWidthAnimation.setValueCallback((LottieValueCallback<Float>) callback);
    } else if (property == LottieProperty.TEXT_TRACKING && trackingAnimation != null) {
      trackingAnimation.setValueCallback((LottieValueCallback<Float>) callback);
    }
  }
}
