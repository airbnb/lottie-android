package com.airbnb.lottie.model.layer;

import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.RenderNode;
import android.os.Build;
import android.util.Log;

import androidx.annotation.CallSuper;
import androidx.annotation.FloatRange;
import androidx.annotation.Nullable;

import com.airbnb.lottie.L;
import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.LottieDrawable;
import com.airbnb.lottie.animation.LPaint;
import com.airbnb.lottie.animation.content.Content;
import com.airbnb.lottie.animation.content.DrawingContent;
import com.airbnb.lottie.animation.keyframe.BaseKeyframeAnimation;
import com.airbnb.lottie.animation.keyframe.FloatKeyframeAnimation;
import com.airbnb.lottie.animation.keyframe.MaskKeyframeAnimation;
import com.airbnb.lottie.animation.keyframe.TransformKeyframeAnimation;
import com.airbnb.lottie.model.KeyPath;
import com.airbnb.lottie.model.KeyPathElement;
import com.airbnb.lottie.model.content.BlurEffect;
import com.airbnb.lottie.model.content.Mask;
import com.airbnb.lottie.model.content.ShapeData;
import com.airbnb.lottie.parser.DropShadowEffect;
import com.airbnb.lottie.utils.Logger;
import com.airbnb.lottie.utils.Utils;
import com.airbnb.lottie.value.LottieValueCallback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class BaseLayer
    implements DrawingContent, BaseKeyframeAnimation.AnimationListener, KeyPathElement {
  /**
   * These flags were in Canvas but they were deprecated and removed.
   * TODO: test removing these on older versions of Android.
   */
  private static final int CLIP_SAVE_FLAG = 0x02;
  private static final int CLIP_TO_LAYER_SAVE_FLAG = 0x10;
  private static final int MATRIX_SAVE_FLAG = 0x01;
  private static final int SAVE_FLAGS = CLIP_SAVE_FLAG | CLIP_TO_LAYER_SAVE_FLAG | MATRIX_SAVE_FLAG;

  @Nullable
  static BaseLayer forModel(
      CompositionLayer compositionLayer, Layer layerModel, LottieDrawable drawable, LottieComposition composition) {
    switch (layerModel.getLayerType()) {
      case SHAPE:
        return new ShapeLayer(drawable, layerModel, compositionLayer, composition);
      case PRE_COMP:
        return new CompositionLayer(drawable, compositionLayer, layerModel,
            composition.getPrecomps(layerModel.getRefId()), composition);
      case SOLID:
        return new SolidLayer(drawable, compositionLayer, layerModel);
      case IMAGE:
        return new ImageLayer(drawable, compositionLayer, layerModel);
      case NULL:
        return new NullLayer(drawable, compositionLayer, layerModel);
      case TEXT:
        return new TextLayer(drawable, compositionLayer, layerModel);
      case UNKNOWN:
      default:
        // Do nothing
        Logger.warning("Unknown layer type " + layerModel.getLayerType());
        return null;
    }
  }

  private final Path path = new Path();
  private final Matrix matrix = new Matrix();
  private final Matrix canvasMatrix = new Matrix();
  private final Paint contentPaint = new LPaint(Paint.ANTI_ALIAS_FLAG);
  private final Paint dstInPaint = new LPaint(Paint.ANTI_ALIAS_FLAG, PorterDuff.Mode.DST_IN);
  private final Paint dstOutPaint = new LPaint(Paint.ANTI_ALIAS_FLAG, PorterDuff.Mode.DST_OUT);
  private final Paint mattePaint = new LPaint(Paint.ANTI_ALIAS_FLAG);
  private final Paint clearPaint = new LPaint(PorterDuff.Mode.CLEAR);
  private final RectF rect = new RectF();
  private final RectF canvasBounds = new RectF();
  private final RectF maskBoundsRect = new RectF();
  private final RectF matteBoundsRect = new RectF();
  private final RectF tempMaskBoundsRect = new RectF();
  private final String drawTraceName;
  final Matrix boundsMatrix = new Matrix();
  final LottieDrawable lottieDrawable;
  final Layer layerModel;
  @Nullable
  private MaskKeyframeAnimation mask;
  @Nullable
  private FloatKeyframeAnimation inOutAnimation;
  @Nullable
  private BaseLayer matteLayer;
  /**
   * This should only be used by {@link #buildParentLayerListIfNeeded()}
   * to construct the list of parent layers.
   */
  @Nullable
  private BaseLayer parentLayer;
  private List<BaseLayer> parentLayers;

  private final List<BaseKeyframeAnimation<?, ?>> animations = new ArrayList<>();
  final TransformKeyframeAnimation transform;
  private boolean visible = true;

  private boolean outlineMasksAndMattes;
  @Nullable private Paint outlineMasksAndMattesPaint;

  float blurMaskFilterRadius = 0f;
  @Nullable BlurMaskFilter blurMaskFilter;
  @Nullable protected final CompositionLayer compositionLayer;

  BaseLayer(LottieDrawable lottieDrawable, @Nullable CompositionLayer compositionLayer, Layer layerModel) {
    this.lottieDrawable = lottieDrawable;
    this.compositionLayer = compositionLayer;
    this.layerModel = layerModel;
    drawTraceName = layerModel.getName() + "#draw";
    if (layerModel.getMatteType() == Layer.MatteType.INVERT) {
      mattePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
    } else {
      mattePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
    }

    this.transform = layerModel.getTransform().createAnimation();
    transform.addListener(this);

    if (layerModel.getMasks() != null && !layerModel.getMasks().isEmpty()) {
      this.mask = new MaskKeyframeAnimation(layerModel.getMasks());
      for (BaseKeyframeAnimation<?, Path> animation : mask.getMaskAnimations()) {
        // Don't call addAnimation() because progress gets set manually in setProgress to
        // properly handle time scale.
        animation.addUpdateListener(this);
      }
      for (BaseKeyframeAnimation<Integer, Integer> animation : mask.getOpacityAnimations()) {
        addAnimation(animation);
        animation.addUpdateListener(this);
      }
    }
    setupInOutAnimations();
  }

  /**
   * Enable this to debug slow animations by outlining masks and mattes. The performance overhead of the masks and mattes will
   * be proportional to the surface area of all of the masks/mattes combined.
   * <p>
   * DO NOT leave this enabled in production.
   */
  void setOutlineMasksAndMattes(boolean outline) {
    if (outline && outlineMasksAndMattesPaint == null) {
      outlineMasksAndMattesPaint = new LPaint();
    }
    outlineMasksAndMattes = outline;
  }

  @Override
  public void onValueChanged() {
    invalidateSelf();
  }

  Layer getLayerModel() {
    return layerModel;
  }

  void setMatteLayer(@Nullable BaseLayer matteLayer) {
    this.matteLayer = matteLayer;
  }

  boolean hasMatteOnThisLayer() {
    return matteLayer != null;
  }

  void setParentLayer(@Nullable BaseLayer parentLayer) {
    this.parentLayer = parentLayer;
  }

  private void setupInOutAnimations() {
    if (!layerModel.getInOutKeyframes().isEmpty()) {
      inOutAnimation = new FloatKeyframeAnimation(layerModel.getInOutKeyframes());
      inOutAnimation.setIsDiscrete();
      inOutAnimation.addUpdateListener(() -> setVisible(inOutAnimation.getFloatValue() == 1f));
      setVisible(inOutAnimation.getValue() == 1f);
      addAnimation(inOutAnimation);
    } else {
      setVisible(true);
    }
  }

  protected boolean isInvalid = false;

  public void invalidateSelf() {
    isInvalid = true;
    if (compositionLayer == null) {
      // TODO: do this via inheritance.
      lottieDrawable.invalidateSelf();
    } else {
      compositionLayer.invalidateSelf();
    }
    // if (parentLayer != null) {
    //   parentLayer.invalidateSelf();
    // }
    // if (parentLayers != null) {
    //   for (BaseLayer layer : parentLayers) {
    //     layer.invalidateSelf();
    //   }
    // }
    // if (matteLayer != null) {
    //   matteLayer.invalidateSelf();
    // }
  }

  public void addAnimation(@Nullable BaseKeyframeAnimation<?, ?> newAnimation) {
    if (newAnimation == null) {
      return;
    }
    animations.add(newAnimation);
  }

  public void removeAnimation(BaseKeyframeAnimation<?, ?> animation) {
    animations.remove(animation);
  }

  @CallSuper
  @Override
  public void getBounds(
      RectF outBounds, Matrix parentMatrix, boolean applyParents) {
    rect.set(0, 0, 0, 0);
    buildParentLayerListIfNeeded();
    boundsMatrix.set(parentMatrix);

    if (applyParents) {
      if (parentLayers != null) {
        for (int i = parentLayers.size() - 1; i >= 0; i--) {
          boundsMatrix.preConcat(parentLayers.get(i).transform.getMatrix());
        }
      } else if (parentLayer != null) {
        boundsMatrix.preConcat(parentLayer.transform.getMatrix());
      }
    }

    boundsMatrix.preConcat(transform.getMatrix());
  }


  RenderNode renderNode;
  String renderNodeName;
  private final Matrix lastDrawnMatrix = new Matrix();

  @Override
  public void draw(Canvas originalCanvas, Matrix parentMatrix, int parentAlpha) {
    L.beginSection(drawTraceName);
    if (!visible || layerModel.isHidden()) {
      L.endSection(drawTraceName);
      return;
    }
    L.beginSection("Layer#setup");
    if (renderNode == null) {
      L.beginSection("Layer#newRenderNode");
      renderNodeName = layerModel.getId() + ":" + layerModel.getName();
      if (parentLayers != null) {
        for (BaseLayer layer : parentLayers) {
          renderNodeName += "_" + layer.getName();
        }
      }
      renderNode = new RenderNode(renderNodeName);
      L.endSection("Layer#newRenderNode");
    }

    buildParentLayerListIfNeeded();
    L.endSection("Layer#setup");
    L.beginSection("Layer#parentMatrix");
    matrix.reset();
    matrix.set(parentMatrix);
    for (int i = parentLayers.size() - 1; i >= 0; i--) {
      matrix.preConcat(parentLayers.get(i).transform.getMatrix());
    }
    L.endSection("Layer#parentMatrix");
    L.beginSection("Layer#opacity");
    // It is unclear why but getting the opacity here would sometimes NPE.
    // The extra code here is designed to avoid this.
    // https://github.com/airbnb/lottie-android/issues/2083
    int opacity = 100;
    BaseKeyframeAnimation<?, Integer> opacityAnimation = transform.getOpacity();
    if (opacityAnimation != null) {
      Integer opacityValue = opacityAnimation.getValue();
      if (opacityValue != null) {
        opacity = opacityValue;
      }
    }
    int alpha = (int) ((parentAlpha / 255f * (float) opacity / 100f) * 255);
    L.endSection("Layer#opacity");
    L.beginSection("Layer#hasMatteOrMask");
    boolean hasMatteOrMask = !hasMatteOnThisLayer() && !hasMasksOnThisLayer();
    L.endSection("Layer#hasMatteOrMask");
    L.beginSection("Layer#reuseRenderNode");
    L.endSection("Layer#reuseRenderNode");
    if (hasMatteOrMask) {
      matrix.preConcat(transform.getMatrix());
      boolean reuseRenderNode = renderNode.hasDisplayList() && !isInvalid && matrix.equals(lastDrawnMatrix);
      if (reuseRenderNode) {
        L.beginSection("Layer#drawRenderNode");
        originalCanvas.drawRenderNode(renderNode);
        L.endSection("Layer#drawRenderNode");
        recordRenderTime(L.endSection(drawTraceName));
        return;
      }
      L.beginSection("Layer#setPositionAndBeginRecording");
      renderNode.setPosition(layerModel.getComposition().getBounds());
      Canvas recordingCanvas = renderNode.beginRecording();
      L.endSection("Layer#setPositionAndBeginRecording");

      L.beginSection("Layer#drawLayer");
      lastDrawnMatrix.set(matrix);
      drawLayer(recordingCanvas, matrix, alpha);
      L.endSection("Layer#drawLayer");
      L.beginSection("Layer#endRecording");
      renderNode.endRecording();
      L.endSection("Layer#endRecording");
      L.beginSection("Layer#drawRenderNode");
      originalCanvas.drawRenderNode(renderNode);
      L.endSection("Layer#drawRenderNode");
      recordRenderTime(L.endSection(drawTraceName));
      isInvalid = false;
      return;
    }

    L.beginSection("Layer#computeBounds");
    getBounds(rect, matrix, false);
    L.endSection("Layer#computeBounds");

    intersectBoundsWithMatte(rect, parentMatrix);

    matrix.preConcat(transform.getMatrix());
    intersectBoundsWithMask(rect, matrix);

    boolean reuseRenderNode = renderNode.hasDisplayList() && !isInvalid && matrix.equals(lastDrawnMatrix);
    if (reuseRenderNode) {
      L.beginSection("Layer#drawRenderNode");
      originalCanvas.drawRenderNode(renderNode);
      L.endSection("Layer#drawRenderNode");
      recordRenderTime(L.endSection(drawTraceName));
      return;
    }
    L.beginSection("Layer#setPositionAndCompositingLayer");
    renderNode.setPosition(layerModel.getComposition().getBounds());
    renderNode.setUseCompositingLayer(true, new Paint());
    Canvas recordingCanvas = renderNode.beginRecording();
    L.endSection("Layer#setPositionAndCompositingLayer");

    // Intersect the mask and matte rect with the canvas bounds.
    // If the canvas has a transform, then we need to transform its bounds by its matrix
    // so that we know the coordinate space that the canvas is showing.
    canvasBounds.set(0f, 0f, recordingCanvas.getWidth(), recordingCanvas.getHeight());
    //noinspection deprecation
    recordingCanvas.getMatrix(canvasMatrix);
    if (!canvasMatrix.isIdentity()) {
      canvasMatrix.invert(canvasMatrix);
      canvasMatrix.mapRect(canvasBounds);
    }
    if (!rect.intersect(canvasBounds)) {
      rect.set(0, 0, 0, 0);
    }

    // Ensure that what we are drawing is >=1px of width and height.
    // On older devices, drawing to an offscreen buffer of <1px would draw back as a black bar.
    // https://github.com/airbnb/lottie-android/issues/1625
    if (rect.width() >= 1f && rect.height() >= 1f) {
      L.beginSection("Layer#saveLayer");
      contentPaint.setAlpha(255);
      Utils.saveLayerCompat(recordingCanvas, rect, contentPaint);
      L.endSection("Layer#saveLayer");

      // Clear the off screen buffer. This is necessary for some phones.
      clearCanvas(recordingCanvas);
      L.beginSection("Layer#drawLayer");
      drawLayer(recordingCanvas, matrix, alpha);
      L.endSection("Layer#drawLayer");

      if (hasMasksOnThisLayer()) {
        applyMasks(recordingCanvas, matrix);
      }

      if (hasMatteOnThisLayer()) {
        L.beginSection("Layer#drawMatte");
        L.beginSection("Layer#saveLayer");
        Utils.saveLayerCompat(recordingCanvas, rect, mattePaint, SAVE_FLAGS);
        L.endSection("Layer#saveLayer");
        clearCanvas(recordingCanvas);
        //noinspection ConstantConditions
        matteLayer.draw(recordingCanvas, parentMatrix, alpha);
        L.beginSection("Layer#restoreLayer");
        recordingCanvas.restore();
        L.endSection("Layer#restoreLayer");
        L.endSection("Layer#drawMatte");
      }

      L.beginSection("Layer#restoreLayer");
      recordingCanvas.restore();
      L.endSection("Layer#restoreLayer");
    }

    if (outlineMasksAndMattes && outlineMasksAndMattesPaint != null) {
      outlineMasksAndMattesPaint.setStyle(Paint.Style.STROKE);
      outlineMasksAndMattesPaint.setColor(0xFFFC2803);
      outlineMasksAndMattesPaint.setStrokeWidth(4);
      recordingCanvas.drawRect(rect, outlineMasksAndMattesPaint);
      outlineMasksAndMattesPaint.setStyle(Paint.Style.FILL);
      outlineMasksAndMattesPaint.setColor(0x50EBEBEB);
      recordingCanvas.drawRect(rect, outlineMasksAndMattesPaint);
    }

    renderNode.endRecording();
    L.beginSection("Layer#drawRenderNode");
    originalCanvas.drawRenderNode(renderNode);
    L.endSection("Layer#drawRenderNode");
    isInvalid = false;
    lastDrawnMatrix.set(matrix);

    recordRenderTime(L.endSection(drawTraceName));
  }

  private void recordRenderTime(float ms) {
    lottieDrawable.getComposition()
        .getPerformanceTracker().recordRenderTime(layerModel.getName(), ms);

  }

  private void clearCanvas(Canvas canvas) {
    L.beginSection("Layer#clearLayer");
    // If we don't pad the clear draw, some phones leave a 1px border of the graphics buffer.
    canvas.drawRect(rect.left - 1, rect.top - 1, rect.right + 1, rect.bottom + 1, clearPaint);
    L.endSection("Layer#clearLayer");
  }

  private void intersectBoundsWithMask(RectF rect, Matrix matrix) {
    maskBoundsRect.set(0, 0, 0, 0);
    if (!hasMasksOnThisLayer()) {
      return;
    }
    //noinspection ConstantConditions
    int size = mask.getMasks().size();
    for (int i = 0; i < size; i++) {
      Mask mask = this.mask.getMasks().get(i);
      BaseKeyframeAnimation<?, Path> maskAnimation = this.mask.getMaskAnimations().get(i);
      Path maskPath = maskAnimation.getValue();
      if (maskPath == null) {
        // This should never happen but seems to happen occasionally.
        // There is no known repro for this but is is probably best to just skip this mask if that is the case.
        // https://github.com/airbnb/lottie-android/issues/1879
        continue;
      }
      path.set(maskPath);
      path.transform(matrix);

      switch (mask.getMaskMode()) {
        case MASK_MODE_NONE:
          // Mask mode none will just render the original content so it is the whole bounds.
          return;
        case MASK_MODE_SUBTRACT:
          // If there is a subtract mask, the mask could potentially be the size of the entire
          // canvas so we can't use the mask bounds.
          return;
        case MASK_MODE_INTERSECT:
        case MASK_MODE_ADD:
          if (mask.isInverted()) {
            return;
          }
        default:
          path.computeBounds(tempMaskBoundsRect, false);
          // As we iterate through the masks, we want to calculate the union region of the masks.
          // We initialize the rect with the first mask. If we don't call set() on the first call,
          // the rect will always extend to (0,0).
          if (i == 0) {
            maskBoundsRect.set(tempMaskBoundsRect);
          } else {
            maskBoundsRect.set(
                Math.min(maskBoundsRect.left, tempMaskBoundsRect.left),
                Math.min(maskBoundsRect.top, tempMaskBoundsRect.top),
                Math.max(maskBoundsRect.right, tempMaskBoundsRect.right),
                Math.max(maskBoundsRect.bottom, tempMaskBoundsRect.bottom)
            );
          }
      }
    }

    boolean intersects = rect.intersect(maskBoundsRect);
    if (!intersects) {
      rect.set(0f, 0f, 0f, 0f);
    }
  }

  private void intersectBoundsWithMatte(RectF rect, Matrix matrix) {
    if (!hasMatteOnThisLayer()) {
      return;
    }

    if (layerModel.getMatteType() == Layer.MatteType.INVERT) {
      // We can't trim the bounds if the mask is inverted since it extends all the way to the
      // composition bounds.
      return;
    }
    matteBoundsRect.set(0f, 0f, 0f, 0f);
    matteLayer.getBounds(matteBoundsRect, matrix, true);
    boolean intersects = rect.intersect(matteBoundsRect);
    if (!intersects) {
      rect.set(0f, 0f, 0f, 0f);
    }
  }

  abstract void drawLayer(Canvas canvas, Matrix parentMatrix, int parentAlpha);

  private void applyMasks(Canvas canvas, Matrix matrix) {
    L.beginSection("Layer#saveLayer");
    Utils.saveLayerCompat(canvas, rect, dstInPaint, SAVE_FLAGS);
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
      // Pre-Pie, offscreen buffers were opaque which meant that outer border of a mask
      // might get drawn depending on the result of float rounding.
      clearCanvas(canvas);
    }
    L.endSection("Layer#saveLayer");
    for (int i = 0; i < mask.getMasks().size(); i++) {
      Mask mask = this.mask.getMasks().get(i);
      BaseKeyframeAnimation<ShapeData, Path> maskAnimation = this.mask.getMaskAnimations().get(i);
      BaseKeyframeAnimation<Integer, Integer> opacityAnimation = this.mask.getOpacityAnimations().get(i);
      switch (mask.getMaskMode()) {
        case MASK_MODE_NONE:
          // None mask should have no effect. If all masks are NONE, fill the
          // mask canvas with a rectangle so it fully covers the original layer content.
          // However, if there are other masks, they should be the only ones that have an effect so
          // this should noop.
          if (areAllMasksNone()) {
            contentPaint.setAlpha(255);
            canvas.drawRect(rect, contentPaint);
          }
          break;
        case MASK_MODE_ADD:
          if (mask.isInverted()) {
            applyInvertedAddMask(canvas, matrix, maskAnimation, opacityAnimation);
          } else {
            applyAddMask(canvas, matrix, maskAnimation, opacityAnimation);
          }
          break;
        case MASK_MODE_SUBTRACT:
          if (i == 0) {
            contentPaint.setColor(Color.BLACK);
            contentPaint.setAlpha(255);
            canvas.drawRect(rect, contentPaint);
          }
          if (mask.isInverted()) {
            applyInvertedSubtractMask(canvas, matrix, maskAnimation, opacityAnimation);
          } else {
            applySubtractMask(canvas, matrix, maskAnimation);
          }
          break;
        case MASK_MODE_INTERSECT:
          if (mask.isInverted()) {
            applyInvertedIntersectMask(canvas, matrix, maskAnimation, opacityAnimation);
          } else {
            applyIntersectMask(canvas, matrix, maskAnimation, opacityAnimation);
          }
          break;
      }
    }
    L.beginSection("Layer#restoreLayer");
    canvas.restore();
    L.endSection("Layer#restoreLayer");
  }

  private boolean areAllMasksNone() {
    if (mask.getMaskAnimations().isEmpty()) {
      return false;
    }
    for (int i = 0; i < mask.getMasks().size(); i++) {
      if (mask.getMasks().get(i).getMaskMode() != Mask.MaskMode.MASK_MODE_NONE) {
        return false;
      }
    }
    return true;
  }

  private void applyAddMask(Canvas canvas, Matrix matrix,
      BaseKeyframeAnimation<ShapeData, Path> maskAnimation, BaseKeyframeAnimation<Integer, Integer> opacityAnimation) {
    Path maskPath = maskAnimation.getValue();
    path.set(maskPath);
    path.transform(matrix);
    contentPaint.setAlpha((int) (opacityAnimation.getValue() * 2.55f));
    canvas.drawPath(path, contentPaint);
  }

  private void applyInvertedAddMask(Canvas canvas, Matrix matrix,
      BaseKeyframeAnimation<ShapeData, Path> maskAnimation, BaseKeyframeAnimation<Integer, Integer> opacityAnimation) {
    Utils.saveLayerCompat(canvas, rect, contentPaint);
    canvas.drawRect(rect, contentPaint);
    Path maskPath = maskAnimation.getValue();
    path.set(maskPath);
    path.transform(matrix);
    contentPaint.setAlpha((int) (opacityAnimation.getValue() * 2.55f));
    canvas.drawPath(path, dstOutPaint);
    canvas.restore();
  }

  private void applySubtractMask(Canvas canvas, Matrix matrix, BaseKeyframeAnimation<ShapeData, Path> maskAnimation) {
    Path maskPath = maskAnimation.getValue();
    path.set(maskPath);
    path.transform(matrix);
    canvas.drawPath(path, dstOutPaint);
  }

  private void applyInvertedSubtractMask(Canvas canvas, Matrix matrix,
      BaseKeyframeAnimation<ShapeData, Path> maskAnimation, BaseKeyframeAnimation<Integer, Integer> opacityAnimation) {
    Utils.saveLayerCompat(canvas, rect, dstOutPaint);
    canvas.drawRect(rect, contentPaint);
    dstOutPaint.setAlpha((int) (opacityAnimation.getValue() * 2.55f));
    Path maskPath = maskAnimation.getValue();
    path.set(maskPath);
    path.transform(matrix);
    canvas.drawPath(path, dstOutPaint);
    canvas.restore();
  }

  private void applyIntersectMask(Canvas canvas, Matrix matrix,
      BaseKeyframeAnimation<ShapeData, Path> maskAnimation, BaseKeyframeAnimation<Integer, Integer> opacityAnimation) {
    Utils.saveLayerCompat(canvas, rect, dstInPaint);
    Path maskPath = maskAnimation.getValue();
    path.set(maskPath);
    path.transform(matrix);
    contentPaint.setAlpha((int) (opacityAnimation.getValue() * 2.55f));
    canvas.drawPath(path, contentPaint);
    canvas.restore();
  }

  private void applyInvertedIntersectMask(Canvas canvas, Matrix matrix,
      BaseKeyframeAnimation<ShapeData, Path> maskAnimation, BaseKeyframeAnimation<Integer, Integer> opacityAnimation) {
    Utils.saveLayerCompat(canvas, rect, dstInPaint);
    canvas.drawRect(rect, contentPaint);
    dstOutPaint.setAlpha((int) (opacityAnimation.getValue() * 2.55f));
    Path maskPath = maskAnimation.getValue();
    path.set(maskPath);
    path.transform(matrix);
    canvas.drawPath(path, dstOutPaint);
    canvas.restore();
  }

  boolean hasMasksOnThisLayer() {
    return mask != null && !mask.getMaskAnimations().isEmpty();
  }

  private void setVisible(boolean visible) {
    if (visible != this.visible) {
      this.visible = visible;
      invalidateSelf();
    }
  }

  void setProgress(@FloatRange(from = 0f, to = 1f) float progress) {
    L.beginSection("BaseLayer#setProgress");
    // Time stretch should not be applied to the layer transform.
    L.beginSection("BaseLayer#setProgress.transform");
    transform.setProgress(progress);
    L.endSection("BaseLayer#setProgress.transform");
    if (mask != null) {
      L.beginSection("BaseLayer#setProgress.mask");
      for (int i = 0; i < mask.getMaskAnimations().size(); i++) {
        mask.getMaskAnimations().get(i).setProgress(progress);
      }
      L.endSection("BaseLayer#setProgress.mask");
    }
    if (inOutAnimation != null) {
      L.beginSection("BaseLayer#setProgress.inout");
      inOutAnimation.setProgress(progress);
      L.endSection("BaseLayer#setProgress.inout");
    }
    if (matteLayer != null) {
      L.beginSection("BaseLayer#setProgress.matte");
      matteLayer.setProgress(progress);
      L.endSection("BaseLayer#setProgress.matte");
    }
    L.beginSection("BaseLayer#setProgress.animations." + animations.size());
    for (int i = 0; i < animations.size(); i++) {
      animations.get(i).setProgress(progress);
    }
    L.endSection("BaseLayer#setProgress.animations." + animations.size());
    L.endSection("BaseLayer#setProgress");
  }

  private void buildParentLayerListIfNeeded() {
    L.beginSection("Layer#buildParentLayerIfNeeded");
    if (parentLayers != null) {
      L.endSection("Layer#buildParentLayerIfNeeded");
      return;
    }
    if (parentLayer == null) {
      parentLayers = Collections.emptyList();
      L.endSection("Layer#buildParentLayerIfNeeded");
      return;
    }

    parentLayers = new ArrayList<>();
    BaseLayer layer = parentLayer;
    while (layer != null) {
      parentLayers.add(layer);
      layer = layer.parentLayer;
    }
    L.endSection("Layer#buildParentLayerIfNeeded");
  }

  @Override
  public String getName() {
    return layerModel.getName();
  }

  @Nullable
  public BlurEffect getBlurEffect() {
    return layerModel.getBlurEffect();
  }

  public BlurMaskFilter getBlurMaskFilter(float radius) {
    if (blurMaskFilterRadius == radius) {
      return blurMaskFilter;
    }
    blurMaskFilter = new BlurMaskFilter(radius / 2f, BlurMaskFilter.Blur.NORMAL);
    blurMaskFilterRadius = radius;
    return blurMaskFilter;
  }

  @Nullable
  public DropShadowEffect getDropShadowEffect() {
    return layerModel.getDropShadowEffect();
  }

  @Override
  public void setContents(List<Content> contentsBefore, List<Content> contentsAfter) {
    // Do nothing
  }

  @Override
  public void resolveKeyPath(
      KeyPath keyPath, int depth, List<KeyPath> accumulator, KeyPath currentPartialKeyPath) {
    if (matteLayer != null) {
      KeyPath matteCurrentPartialKeyPath = currentPartialKeyPath.addKey(matteLayer.getName());
      if (keyPath.fullyResolvesTo(matteLayer.getName(), depth)) {
        accumulator.add(matteCurrentPartialKeyPath.resolve(matteLayer));
      }

      if (keyPath.propagateToChildren(getName(), depth)) {
        int newDepth = depth + keyPath.incrementDepthBy(matteLayer.getName(), depth);
        matteLayer.resolveChildKeyPath(keyPath, newDepth, accumulator, matteCurrentPartialKeyPath);
      }
    }

    if (!keyPath.matches(getName(), depth)) {
      return;
    }

    if (!"__container".equals(getName())) {
      currentPartialKeyPath = currentPartialKeyPath.addKey(getName());

      if (keyPath.fullyResolvesTo(getName(), depth)) {
        accumulator.add(currentPartialKeyPath.resolve(this));
      }
    }

    if (keyPath.propagateToChildren(getName(), depth)) {
      int newDepth = depth + keyPath.incrementDepthBy(getName(), depth);
      resolveChildKeyPath(keyPath, newDepth, accumulator, currentPartialKeyPath);
    }
  }

  void resolveChildKeyPath(
      KeyPath keyPath, int depth, List<KeyPath> accumulator, KeyPath currentPartialKeyPath) {
  }

  @CallSuper
  @Override
  public <T> void addValueCallback(T property, @Nullable LottieValueCallback<T> callback) {
    transform.applyValueCallback(property, callback);
  }
}
