package com.airbnb.lottie.model.layer;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import androidx.annotation.Nullable;

import com.airbnb.lottie.LottieDrawable;
import com.airbnb.lottie.LottieProperty;
import com.airbnb.lottie.animation.keyframe.BaseKeyframeAnimation;
import com.airbnb.lottie.animation.keyframe.ValueCallbackKeyframeAnimation;
import com.airbnb.lottie.value.LottieValueCallback;

public class SolidLayer extends BaseLayer {

  private final RectF rect = new RectF();
  private final Paint paint = new Paint();
  private final float[] points = new float[8];
  private final Path path = new Path();
  private final Layer layerModel;
  @Nullable private BaseKeyframeAnimation<ColorFilter, ColorFilter> colorFilterAnimation;

  SolidLayer(LottieDrawable lottieDrawable, Layer layerModel) {
    super(lottieDrawable, layerModel);
    this.layerModel = layerModel;

    paint.setAlpha(0);
    paint.setStyle(Paint.Style.FILL);
    paint.setColor(layerModel.getSolidColor());
  }

  @Override public void drawLayer(Canvas canvas, Matrix parentMatrix, int parentAlpha) {
    int backgroundAlpha = Color.alpha(layerModel.getSolidColor());
    if (backgroundAlpha == 0) {
      return;
    }

    int alpha = (int) (parentAlpha / 255f * (backgroundAlpha / 255f * transform.getOpacity().getValue() / 100f) * 255);
    paint.setAlpha(alpha);
    if (colorFilterAnimation != null) {
      paint.setColorFilter(colorFilterAnimation.getValue());
    }
    if (alpha > 0) {
      points[0] = 0;
      points[1] = 0;
      points[2] = layerModel.getSolidWidth();
      points[3] = 0;
      points[4] = layerModel.getSolidWidth();
      points[5] = layerModel.getSolidHeight();
      points[6] = 0;
      points[7] = layerModel.getSolidHeight();

      // We can't map rect here because if there is rotation on the transform then we aren't
      // actually drawing a rect.
      parentMatrix.mapPoints(points);
      path.reset();
      path.moveTo(points[0], points[1]);
      path.lineTo(points[2], points[3]);
      path.lineTo(points[4], points[5]);
      path.lineTo(points[6], points[7]);
      path.lineTo(points[0], points[1]);
      path.close();
      canvas.drawPath(path, paint);
    }
  }

  @Override public void getBounds(RectF outBounds, Matrix parentMatrix) {
    super.getBounds(outBounds, parentMatrix);
    rect.set(0, 0, layerModel.getSolidWidth(), layerModel.getSolidHeight());
    boundsMatrix.mapRect(rect);
    outBounds.set(rect);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> void addValueCallback(T property, @Nullable LottieValueCallback<T> callback) {
    super.addValueCallback(property, callback);
    if (property == LottieProperty.COLOR_FILTER) {
      if (callback == null) {
        colorFilterAnimation = null;
      } else {
        colorFilterAnimation =
            new ValueCallbackKeyframeAnimation<>((LottieValueCallback<ColorFilter>) callback);
      }
    }
  }
}
