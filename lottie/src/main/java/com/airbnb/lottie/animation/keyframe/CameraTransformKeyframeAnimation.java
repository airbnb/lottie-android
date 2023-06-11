package com.airbnb.lottie.animation.keyframe;

import android.graphics.Camera;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.util.Log;

import androidx.annotation.Nullable;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.animation.Point3F;
import com.airbnb.lottie.model.animatable.AnimatableCameraTransform;
import com.airbnb.lottie.model.animatable.AnimatablePoint3Value;
import com.airbnb.lottie.model.layer.BaseLayer;

public class CameraTransformKeyframeAnimation {
  @Nullable private final BaseKeyframeAnimation<Point3F, Point3F> pointOfInterest;
  private final BaseKeyframeAnimation<Point3F, Point3F> position;
  private final BaseKeyframeAnimation<Point3F, Point3F> orientation;
  private final BaseKeyframeAnimation<Float, Float> rotationX;
  private final BaseKeyframeAnimation<Float, Float> rotationY;
  private final BaseKeyframeAnimation<Float, Float> rotationZ;

  private final Camera camera = new Camera();
  private final LottieComposition composition;
  private final Matrix matrix = new Matrix();


  public CameraTransformKeyframeAnimation(LottieComposition composition, AnimatableCameraTransform animatableCameraTransform) {
    this.composition = composition;
    AnimatablePoint3Value poi = animatableCameraTransform.getPointOfInterest();
    pointOfInterest = poi == null ? null : poi.createAnimation();
    position = animatableCameraTransform.getPosition().createAnimation();
    orientation = animatableCameraTransform.getOrientation().createAnimation();
    rotationX = animatableCameraTransform.getRotationX().createAnimation();
    rotationY = animatableCameraTransform.getRotationY().createAnimation();
    rotationZ = animatableCameraTransform.getRotationZ().createAnimation();
    camera.save();
  }

  public void addAnimationsToLayer(BaseLayer layer) {
    layer.addAnimation(pointOfInterest);
    layer.addAnimation(position);
    layer.addAnimation(orientation);
    layer.addAnimation(rotationX);
    layer.addAnimation(rotationY);
    layer.addAnimation(rotationZ);
  }

  public void addListener(BaseKeyframeAnimation.AnimationListener listener) {
    if (pointOfInterest != null) {
      pointOfInterest.addUpdateListener(listener);
    }
    position.addUpdateListener(listener);
    orientation.addUpdateListener(listener);
    rotationX.addUpdateListener(listener);
    rotationY.addUpdateListener(listener);
    rotationZ.addUpdateListener(listener);
  }

  public void setProgress(float progress) {
    if (pointOfInterest != null) {
      pointOfInterest.setProgress(progress);
    }
    position.setProgress(progress);
    orientation.setProgress(progress);
    rotationX.setProgress(progress);
    rotationY.setProgress(progress);
    rotationZ.setProgress(progress);
  }

  public Matrix getMatrix() {
    // Is this the best way to reset a camera?
    camera.restore();
    camera.save();

    Point3F position = this.position.getValue();
    Rect bounds = composition.getBounds();
    float originX = -bounds.width() / 2f;
    float originY = -bounds.height() / 2f;
    float lx = originX + position.x;
    float ly = originY + position.y;

    camera.setLocation(-1, 1, -8f);
    camera.translate(0f, 0f, 200f);
    // camera.translate(-originX, originY, 0f);

    Point3F orientation = this.orientation.getValue();
    float rx = this.rotationX.getValue() + orientation.x;
    camera.rotateX(rx);
    float ry = this.rotationY.getValue() + orientation.y;
    camera.rotateY(ry);
    float rz = this.rotationZ.getValue() + orientation.z;
    camera.rotateZ(rz);

    camera.getMatrix(matrix);
    matrix.preTranslate(-lx, -ly);
    matrix.postTranslate(-lx, -ly);

    Log.d("Gabe", String.format("lx %.2f ly %.2f", lx, ly));

    // TODO: point of interest.

    return matrix;
  }
}
