package com.airbnb.lottie.model.layer;

import android.graphics.Canvas;
import android.graphics.Matrix;

import com.airbnb.lottie.LottieDrawable;
import com.airbnb.lottie.animation.keyframe.CameraTransformKeyframeAnimation;

public class CameraLayer extends BaseLayer {

  private final CompositionLayer compositionLayer;
  private final CameraTransformKeyframeAnimation cameraTransform;
  // private final Matrix cameraMatrix = new Matrix();
  // private final Matrix parentAndCameraMatrix = new Matrix();

  CameraLayer(LottieDrawable lottieDrawable, Layer layerModel, CompositionLayer compositionLayer) {
    super(lottieDrawable, layerModel);
    //noinspection DataFlowIssue
    cameraTransform = new CameraTransformKeyframeAnimation(compositionLayer.getLayerModel().getComposition(), layerModel.getCameraTransform());
    cameraTransform.addAnimationsToLayer(this);
    cameraTransform.addListener(this);
    this.compositionLayer = compositionLayer;
  }

  @Override void setProgress(float progress) {
    super.setProgress(progress);
    cameraTransform.setProgress(progress);
  }

  @Override void drawLayer(Canvas canvas, Matrix parentMatrix, int parentAlpha) {
    Matrix cameraMatrix = cameraTransform.getMatrix();
    // parentAndCameraMatrix.set(parentMatrix);
    // parentAndCameraMatrix.preConcat(cameraMatrix);
    canvas.save();
    canvas.concat(cameraMatrix);
    compositionLayer.drawLayerInternal(canvas, parentMatrix, parentAlpha);
    canvas.restore();
  }
}
