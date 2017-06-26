package com.airbnb.lottie;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class TextLayer extends BaseLayer {
  private final RectF rectF = new RectF();
  private final Matrix matrix = new Matrix();
  private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG) {{
    setStyle(Style.FILL);
  }};
  private final Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG) {{
    setStyle(Style.STROKE);
  }};
  private final TextKeyframeAnimation textAnimation;
  @Nullable private KeyframeAnimation<Integer> colorAnimation;
  @Nullable private KeyframeAnimation<Integer> strokeAnimation;
  @Nullable private KeyframeAnimation<Float> strokeWidthAnimation;
  private final LottieDrawable lottieDrawable;
  private final LottieComposition composition;
  private final Map<FontCharacter, List<ContentGroup>> contentsForCharacter = new HashMap<>();

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
      strokeAnimation = textProperties.stroke.createAnimation();
      strokeAnimation.addUpdateListener(this);
      addAnimation(strokeAnimation);
    }

    if (textProperties != null && textProperties.strokeWidth != null) {
      strokeWidthAnimation = textProperties.strokeWidth.createAnimation();
      strokeWidthAnimation.addUpdateListener(this);
      addAnimation(strokeWidthAnimation);
    }
  }

  @Override void setProgress(float progress) {
    super.setProgress(progress);
  }

  @Override void drawLayer(Canvas canvas, Matrix parentMatrix, int parentAlpha) {
    canvas.save();
    DocumentData documentData = textAnimation.getValue();
    float fontScale = (float) documentData.size / 100f;
    float parentScale = Utils.getScale(parentMatrix);
    String text = documentData.text;
    if (colorAnimation != null) {
      fillPaint.setColor(colorAnimation.getValue());
    } else {
      fillPaint.setColor(documentData.color);
    }
    if (strokeAnimation != null) {
      strokePaint.setColor(strokeAnimation.getValue());
    } else {
      strokePaint.setColor(documentData.strokeColor);
    }
    if (strokeWidthAnimation != null) {
      strokePaint.setStrokeWidth(strokeWidthAnimation.getValue());
    } else {
      strokePaint.setStrokeWidth(documentData.strokeWidth * composition.getDpScale() * parentScale);
    }
    for (int i = 0; i < text.length(); i++) {
      char c = text.charAt(i);
      FontCharacter character =
          composition.getCharacters().get(FontCharacter.hashFor(c, documentData.fontFamily));
      List<ContentGroup> contentGroups = getContentsForCharacter(character);
      for (int j = 0; j < contentGroups.size(); j++) {
        Path path = contentGroups.get(j).getPath();
        path.computeBounds(rectF, false);
        matrix.set(parentMatrix);
        matrix.preScale(fontScale, fontScale);
        path.transform(matrix);
        if (documentData.strokeOverFill) {
          drawCharacter(canvas, path, fillPaint);
          drawCharacter(canvas, path, strokePaint);
        } else {
          drawCharacter(canvas, path, strokePaint);
          drawCharacter(canvas, path, fillPaint);
        }
      }
      float tx = (float) character.getWidth() * fontScale * composition.getDpScale() * parentScale;
      canvas.translate(tx, 0);
    }
    canvas.restore();
  }

  private void drawCharacter(Canvas canvas, Path path, Paint paint) {
    if (paint.getColor() == Color.TRANSPARENT) {
      return;
    }
    if (paint.getStyle() == Paint.Style.STROKE && paint.getStrokeWidth() == 0) {
      return;
    }
    canvas.drawPath(path, paint);
  }

  private List<ContentGroup> getContentsForCharacter(FontCharacter character) {
    if (contentsForCharacter.containsKey(character)) {
      return contentsForCharacter.get(character);
    }
    int size = character.getShapes().size();
    List<ContentGroup> contents = new ArrayList<>(size);
    for (int i = 0; i < size; i++) {
      ShapeGroup sg = character.getShapes().get(i);
      contents.add(new ContentGroup(lottieDrawable, this, sg));
    }
    contentsForCharacter.put(character, contents);
    return contents;
  }
}
