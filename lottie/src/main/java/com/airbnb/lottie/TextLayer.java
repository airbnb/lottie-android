package com.airbnb.lottie;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class TextLayer extends BaseLayer {
  private final RectF rectF = new RectF();
  private final Matrix matrix = new Matrix();
  private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG) {{
    setStyle(Style.FILL);
  }};
  private final TextKeyframeAnimation textAnimation;
  private final KeyframeAnimation<Integer> colorAnimation;
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

    //noinspection ConstantConditions
    colorAnimation = layerModel.getTextProperties().color.createAnimation();
    colorAnimation.addUpdateListener(this);
    addAnimation(colorAnimation);
  }

  @Override void setProgress(float progress) {
    super.setProgress(progress);
  }

  @Override void drawLayer(Canvas canvas, Matrix parentMatrix, int parentAlpha) {
    canvas.save();
    // canvas.concat(parentMatrix);
    DocumentData documentData = textAnimation.getValue();
    float fontScale = (float) documentData.size / 100f;
    String text = documentData.text;
    // TODO: pull the right color.
    paint.setColor(colorAnimation.getValue());
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
        canvas.drawPath(path, paint);
      }
      float parentScale = Utils.getScale(parentMatrix);
      float tx = (float) character.getWidth() * fontScale * composition.getDpScale() * parentScale;
      canvas.translate(tx, 0);
    }
    canvas.restore();
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
