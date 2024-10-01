package com.airbnb.lottie.utils;

import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.RenderEffect;
import android.graphics.RenderNode;
import android.graphics.Shader;
import android.os.Build;
import androidx.annotation.Nullable;
import androidx.core.graphics.BlendModeCompat;
import androidx.core.graphics.PaintCompat;
import com.airbnb.lottie.animation.LPaint;

/**
 * An OffscreenLayer encapsulates a "child surface" onto which canvas draw calls can be issued.
 * At the end, the result of these draw calls will be composited onto the parent surface, with
 * user-provided alpha, blend mode, color filter, and drop shadow.
 * <p>
 * To use the OffscreenLayer, call its start() method with the necessary parameters, draw
 * to the returned canvas, and then call finish() to composite the result onto the main canvas.
 * <p>
 * In this sense, using an OffscreenLayer is very similar to the Canvas.saveLayer() family
 * of functions, and in fact forwards to Canvas.saveLayer() when appropriate.
 * <p>
 * Unlike Canvas.saveLayer(), an OffscreenLayer also supports compositing with a drop-shadow,
 * and uses a hardware-accelerated target when available. It attempts to choose the fastest
 * approach to render and composite the contents, up to and including simply rendering
 * directly, if possible.
 */
public class OffscreenLayer {
  /**
   * Encapsulates configuration for compositing the layer contents on the parent. Similar to
   * Paint, but only includes operations that the OffscreenLayer can support.
   */
  public static class ComposeOp {
    public int alpha;
    @Nullable public BlendModeCompat blendMode;
    @Nullable public ColorFilter colorFilter;
    @Nullable public DropShadow shadow;

    public ComposeOp() {
      reset();
    }

    public boolean isTranslucent() {
      return alpha < 255;
    }

    public boolean hasBlendMode() {
      return blendMode != null && blendMode != BlendModeCompat.SRC_OVER;
    }

    public boolean hasShadow() {
      return shadow != null;
    }

    public boolean hasColorFilter() {
      return colorFilter != null;
    }

    public boolean isNoop() {
      return !isTranslucent() && !hasBlendMode() && !hasShadow() && !hasColorFilter();
    }

    public void reset() {
      alpha = 255;
      blendMode = null;
      colorFilter = null;
      shadow = null;
    }
  }

  protected enum RenderStrategy {
      /** No-op: simply render to the underlying canvas directly. */
      DIRECT,
      /** Use Canvas.saveLayer() and compose using Canvas.restore(). */
      SAVE_LAYER,
      /** Render everything onto an off-screen bitmap and then draw it onto the main canvas */
      BITMAP,
      /** Render into a RenderNode's display-list and then draw the render node. (Hardware accelerated) */
      RENDER_NODE
  }

  /** Parent render surface should compose onto. null if no rendering is in progress. */
  @Nullable private Canvas parentCanvas;
  /** Configuration for compositing the layer onto parentCanvas */
  @Nullable private ComposeOp op;
  /** Strategy that we've chosen for rendering this pass */
  private RenderStrategy currentStrategy;
  /** Rectangle that the final composition will occupy in the screen */
  @Nullable private Rect targetRect;
  /** targetRect with shadow render space included */
  @Nullable private RectF rectIncludingShadow;
  @Nullable private Rect intRectIncludingShadow;
  @Nullable private RectF tmpRect;
  @Nullable private RectF scaledRectIncludingShadow;
  @Nullable private Rect shadowBitmapSrcRect;

  @Nullable private RectF scaledBounds;

  private final static Matrix IDENTITY_MATRIX = new Matrix();

  // For RenderStrategy.SAVE_LAYER:
  /** Paint passed to Utils.saveLayerCompat(). */
  @Nullable private Paint composePaint;

  // For RenderStrategy.BITMAP:
  @Nullable private Bitmap bitmap;
  @Nullable private Canvas bitmapCanvas;
  @Nullable private Rect bitmapSrcRect;
  @Nullable private LPaint clearPaint;

  /** Temporary variable to store the parent canvas Matrix */
  @Nullable Matrix parentCanvasMatrix;
  /** parentCanvas' pre-existing matrix when start() was called */
  @Nullable float[] preExistingTransform;

  // Android doesn't render shadows on arbitrary bitmaps correctly. Instead, if we're using
  // render-to-bitmap, we have to draw them manually. To do so, we need an additional bitmap, an
  // associated canvas, paint, and some other data, which we define as members to avoid
  // reallocation. (And also because in the render-node case, we won't need them.)
  @Nullable private Bitmap shadowBitmap;
  @Nullable private Bitmap shadowMaskBitmap;
  @Nullable private Canvas shadowBitmapCanvas;
  @Nullable private Canvas shadowMaskBitmapCanvas;
  @Nullable private LPaint shadowPaint;
  @Nullable private BlurMaskFilter shadowBlurFilter;
  private float lastShadowBlurRadius = 0.0f;

  // For RenderStrategy.RENDER_NODE:
  @Nullable private RenderNode renderNode; // Render node with the initial contents of the layer
  @Nullable private RenderNode shadowRenderNode; // Render node for the shadow
  @Nullable private DropShadow lastRenderNodeShadow;

  public OffscreenLayer() {
  }

  private RenderStrategy chooseRenderStrategy(Canvas parentCanvas, ComposeOp op) {
    if (op.isNoop()) {
      // Can draw directly onto the final canvas, results will look the same.
      return RenderStrategy.DIRECT;
    }

    if (!op.hasShadow()) {
      // Canvas.saveLayer() supports alpha-compositing, blend modes, and color filters, which is
      // sufficient for this case, and is faster than manually maintaining an off-screen bitmap.
      // It is not clear if it's faster than RENDER_NODE and when, but this is what we've been
      // doing prior to OffscreenLayer, so keep that behavior to be safe and avoid regressions.
      return RenderStrategy.SAVE_LAYER;
    }

    // Beyond this point, we are sure that we need to render a drop shadow.

    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q || !parentCanvas.isHardwareAccelerated()) {
      // We don't have support for the RenderNode API, or we're rendering to a software canvas
      // which doesn't support RenderNodes anyhow. This is the slowest path: render to a bitmap,
      // add a shadow manually on CPU.
      return RenderStrategy.BITMAP;
    }

    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S) {
      // RenderEffect, which we need for shadows, was only introduced in S. This means that, pre-S,
      // we have to do renderShadow() on a bitmap, like in the BITMAP case. However, since it's not
      // possible to draw a RenderNode to a software-rendering canvas, there would be no way to get
      // our RenderNode contents onto a bitmap where we can renderShadow(). Therefore, fall back to
      // full bitmap mode.
      return RenderStrategy.BITMAP;
    }

    // The RenderNode and RenderEffect APIs are available, so use hardware acceleration.
    return RenderStrategy.RENDER_NODE;
  }

  private Bitmap allocateBitmap(RectF bounds, Bitmap.Config cfg) {
    // Add 5% to the width and height before allocating. This avoids repeated
    // reallocations in the worst cases of a bitmap growing every frame, while
    // still being relatively speedy to blit and operate on.
    int width = (int)Math.ceil(bounds.width() * 1.05);
    int height = (int)Math.ceil(bounds.height() * 1.05);
    return Bitmap.createBitmap(width, height, cfg);
  }

  private void deallocateBitmap(Bitmap bitmap) {
    bitmap.recycle();
  }

  private boolean needNewBitmap(@Nullable Bitmap bitmap, RectF bounds) {
    if (bitmap == null) {
      return true;
    }

    if (bounds.width() >= bitmap.getWidth() || bounds.height() >= bitmap.getHeight()) {
      return true;
    }

    // If the required area has reduced in size considerably, trigger a reallocation, since
    // we might be paying a large unnecessary penalty to work with a bitmap that big.
    return bounds.width() < bitmap.getWidth() * 0.75f || bounds.height() < bitmap.getHeight() * 0.75f;
  }

  public Canvas start(Canvas parentCanvas, RectF bounds, ComposeOp op) {
    if (this.parentCanvas != null) {
      throw new IllegalStateException("Cannot nest start() calls on a single OffscreenBitmap - call finish() first");
    }

    // Determine the scaling applied by the parentCanvas' pre-existing transform matrix. This is an optimization
    // to avoid creating bitmaps (or render nodes) with unreasonable sizes that will get scaled down when drawn
    // onto parentCanvas anyhow.
    if (preExistingTransform == null) preExistingTransform = new float[9];
    if (parentCanvasMatrix == null) parentCanvasMatrix = new Matrix();
    parentCanvas.getMatrix(parentCanvasMatrix);
    parentCanvasMatrix.getValues(preExistingTransform);

    float pixelScaleX = preExistingTransform[Matrix.MSCALE_X];
    float pixelScaleY = preExistingTransform[Matrix.MSCALE_Y];

    if (scaledBounds == null) scaledBounds = new RectF();
    scaledBounds.set(
        bounds.left * pixelScaleX,
        bounds.top * pixelScaleY,
        bounds.right * pixelScaleX,
        bounds.bottom * pixelScaleY
    );

    this.parentCanvas = parentCanvas;
    this.op = op;
    this.currentStrategy = chooseRenderStrategy(parentCanvas, op);
    if (this.targetRect == null) this.targetRect = new Rect();
    this.targetRect.set((int)bounds.left, (int)bounds.top, (int)bounds.right, (int)bounds.bottom);

    if (composePaint == null) composePaint = new LPaint();
    composePaint.reset();

    Canvas childCanvas;
    switch (currentStrategy) {
      case DIRECT:
        childCanvas = parentCanvas;
        childCanvas.save();
        break;

      case SAVE_LAYER:
        // Paint to use for composition
        composePaint.setAlpha(op.alpha);
        composePaint.setColorFilter(op.colorFilter);
        if (op.hasBlendMode()) {
          PaintCompat.setBlendMode(composePaint, op.blendMode);
        }

        // This adds an entry in the parentCanvas stack, will be popped and composited
        // when restore() is called
        Utils.saveLayerCompat(parentCanvas, bounds, composePaint);
        childCanvas = parentCanvas;
        break;

      case BITMAP:
        if (clearPaint == null) {
          clearPaint = new LPaint();
          clearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        }

        if (needNewBitmap(bitmap, scaledBounds)) {
          if (bitmap != null) {
            deallocateBitmap(bitmap);
          }
          bitmap = allocateBitmap(scaledBounds, Bitmap.Config.ARGB_8888);
          bitmapCanvas = new Canvas(bitmap);
        } else {
          if (bitmapCanvas == null) {
            throw new IllegalStateException("If needNewBitmap() returns true, we should have a canvas ready");
          }
          bitmapCanvas.setMatrix(OffscreenLayer.IDENTITY_MATRIX);
          bitmapCanvas.drawRect(-1, -1, scaledBounds.width() + 1, scaledBounds.height() + 1, this.clearPaint);
        }

        PaintCompat.setBlendMode(composePaint, op.blendMode);
        composePaint.setColorFilter(op.colorFilter);
        composePaint.setAlpha(op.alpha);

        childCanvas = bitmapCanvas;
        childCanvas.scale(pixelScaleX, pixelScaleY); // Replicate scaling applied by parentCanvas
        childCanvas.translate(-bounds.left, -bounds.top); // So that the image begins at the top-left of the bitmap
        break;

      case RENDER_NODE:
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
          throw new IllegalStateException("RenderNode not supported but we chose it as render strategy");
        }

        if (renderNode == null) renderNode = new RenderNode("OffscreenLayer.main");
        if (op.hasShadow() && shadowRenderNode == null) {
          shadowRenderNode = new RenderNode("OffscreenLayer.shadow");
          lastRenderNodeShadow = null;
        }

        // The render node needs some data in advance
        if (op.hasBlendMode() || op.hasColorFilter()) {
          if (composePaint == null) composePaint = new LPaint();
          composePaint.reset();
          PaintCompat.setBlendMode(composePaint, op.blendMode);
          composePaint.setColorFilter(op.colorFilter);
          renderNode.setUseCompositingLayer(true, composePaint);

          if (op.hasShadow()) {
            if (shadowRenderNode == null) {
              throw new IllegalStateException("Must initialize shadowRenderNode when we have shadow");
            }
            shadowRenderNode.setUseCompositingLayer(true, composePaint);
          }
        }
        renderNode.setAlpha(op.alpha / 255.f);
        if (op.hasShadow()) {
          if (shadowRenderNode == null) {
            throw new IllegalStateException("Must initialize shadowRenderNode when we have shadow");
          }

          // lottie-web composes the shadow onto the canvas first, and then the
          // contents separately - mirror this behavior
          shadowRenderNode.setAlpha(op.alpha / 255.f);
        }
        renderNode.setHasOverlappingRendering(true);
        renderNode.setPosition((int)scaledBounds.left, (int)scaledBounds.top, (int)scaledBounds.right, (int)scaledBounds.bottom);

        childCanvas = renderNode.beginRecording((int) scaledBounds.width(), (int) scaledBounds.height());
        childCanvas.setMatrix(OffscreenLayer.IDENTITY_MATRIX);
        childCanvas.scale(pixelScaleX, pixelScaleY); // Replicate scaling applied by parentCanvas
        childCanvas.translate(-bounds.left, -bounds.top); // So that the image begins at the top-left of the bitmap
        break;

      default:
        throw new RuntimeException("Invalid render strategy for OffscreenLayer");
    }

    return childCanvas;
  }

  public void finish() {
    if (parentCanvas == null || op == null || preExistingTransform == null || targetRect == null) {
      throw new IllegalStateException("OffscreenBitmap: finish() call without matching start()");
    }

    switch (currentStrategy) {
      case DIRECT:
        parentCanvas.restore();
        break;

      case SAVE_LAYER:
        parentCanvas.restore();
        break;

      case BITMAP:
        if (bitmap == null) {
          throw new IllegalStateException("Bitmap is not ready; should've been initialized at start() time");
        }

        if (op.hasShadow()) {
          // Composing the shadow first and then the content like this will be incorrect in the
          // presence of op.blendMode. However, that is not used at the moment, so this
          // optimization is safe. (Otherwise, we'd have to have another bitmap here for the
          // intermediate result.)
          renderBitmapShadow(parentCanvas, op.shadow);
        }

        if (bitmapSrcRect == null) bitmapSrcRect = new Rect();
        bitmapSrcRect.set(0, 0, (int)(targetRect.width() * preExistingTransform[Matrix.MSCALE_X]), (int)(targetRect.height() * preExistingTransform[Matrix.MSCALE_Y]));
        parentCanvas.drawBitmap(bitmap, bitmapSrcRect, targetRect, composePaint);
        break;

      case RENDER_NODE:
        if (renderNode == null) {
          throw new IllegalStateException("RenderNode is not ready; should've been initialized at start() time");
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
          throw new IllegalStateException("RenderNode not supported but we chose it as render strategy");
        }

        parentCanvas.save();
        parentCanvas.scale(1.0f / preExistingTransform[Matrix.MSCALE_X], 1.0f / preExistingTransform[Matrix.MSCALE_Y]);
        renderNode.endRecording();
        if (op.hasShadow()) {
          // Composing the shadow first and then the content like this will be incorrect in the
          // presence of op.blendMode. However, that is not used at the moment, so this
          // optimization is safe. (Otherwise, we'd have to have another render node here for the
          // intermediate result.)
          renderHardwareShadow(parentCanvas, op.shadow);
        }
        parentCanvas.drawRenderNode(renderNode);
        parentCanvas.restore();
        break;
    }

    parentCanvas = null;
  }

  private RectF calculateRectIncludingShadow(Rect rect, DropShadow shadow) {
    if (rectIncludingShadow == null) rectIncludingShadow = new RectF();
    if (tmpRect == null) tmpRect = new RectF();
    rectIncludingShadow.set(rect);
    rectIncludingShadow.offsetTo(rect.left + shadow.getDx(), rect.top + shadow.getDy());
    rectIncludingShadow.inset(-shadow.getRadius(), -shadow.getRadius());
    tmpRect.set(rect);
    rectIncludingShadow.union(tmpRect);
    return rectIncludingShadow;
  }

  /** Renders a shadow (only the shadow) of this.bitmap to the provided canvas. */
  private void renderBitmapShadow(Canvas targetCanvas, DropShadow shadow) {
    if (targetRect == null || bitmap == null) {
      throw new IllegalStateException("Cannot render to bitmap outside a start()/finish() block");
    }

    // This is an expanded rect that encompasses the full extent of the shadow.
    RectF rectIncludingShadow = calculateRectIncludingShadow(targetRect, shadow);
    if (intRectIncludingShadow == null) intRectIncludingShadow = new Rect();
    intRectIncludingShadow.set(
        (int)Math.floor(rectIncludingShadow.left),
        (int)Math.floor(rectIncludingShadow.top),
        (int)Math.ceil(rectIncludingShadow.right),
        (int)Math.ceil(rectIncludingShadow.bottom)
    );
    float pixelScaleX = preExistingTransform != null ? preExistingTransform[Matrix.MSCALE_X] : 1.0f;
    float pixelScaleY = preExistingTransform != null ? preExistingTransform[Matrix.MSCALE_Y] : 1.0f;
    if (scaledRectIncludingShadow == null) scaledRectIncludingShadow = new RectF();
    scaledRectIncludingShadow.set(
        rectIncludingShadow.left * pixelScaleX,
        rectIncludingShadow.top * pixelScaleY,
        rectIncludingShadow.right * pixelScaleX,
        rectIncludingShadow.bottom * pixelScaleY
    );

    if (shadowBitmapSrcRect == null) shadowBitmapSrcRect = new Rect();
    shadowBitmapSrcRect.set(0, 0, (int)scaledRectIncludingShadow.width(), (int)scaledRectIncludingShadow.height());
    if (needNewBitmap(shadowBitmap, scaledRectIncludingShadow)) {
      if (shadowBitmap != null) {
        deallocateBitmap(shadowBitmap);
      }
      if (shadowMaskBitmap != null) {
        deallocateBitmap(shadowMaskBitmap);
      }

      shadowBitmap = allocateBitmap(scaledRectIncludingShadow, Bitmap.Config.ARGB_8888);
      shadowMaskBitmap = allocateBitmap(scaledRectIncludingShadow, Bitmap.Config.ALPHA_8);
      shadowBitmapCanvas = new Canvas(shadowBitmap);
      shadowMaskBitmapCanvas = new Canvas(shadowMaskBitmap);
    } else {
      if (shadowBitmapCanvas == null || shadowMaskBitmapCanvas == null || clearPaint == null) {
        throw new IllegalStateException("If needNewBitmap() returns true, we should have a canvas and bitmap ready");
      }
      shadowBitmapCanvas.drawRect(shadowBitmapSrcRect, clearPaint);
      shadowMaskBitmapCanvas.drawRect(shadowBitmapSrcRect, clearPaint);
    }

    if (shadowMaskBitmap == null) throw new IllegalStateException("Expected to have allocated a shadow mask bitmap");
    
    if (shadowPaint == null) {
      shadowPaint = new LPaint(Paint.ANTI_ALIAS_FLAG);
    }

    // This is the offset of targetRect inside rectIncludingShadow
    float offsetX = targetRect.left - rectIncludingShadow.left;
    float offsetY = targetRect.top - rectIncludingShadow.top;

    // Draw the image onto the mask layer first. Since the mask layer is ALPHA_8, this discards color information.
    // Align it so that when drawn in the end, it originates at targetRect.x, targetRect.y
    // the int casts are very important here - they save us from some slow path for non-integer coords
    shadowMaskBitmapCanvas.drawBitmap(bitmap, (int)(offsetX * pixelScaleX), (int)(offsetY * pixelScaleY), null);

    // Prepare the shadow paint. This is the paint that will perform a blur and a tint of the mask
    if (shadowBlurFilter == null || lastShadowBlurRadius != shadow.getRadius()) {
      float scaledRadius = shadow.getRadius() * (pixelScaleX + pixelScaleY) / 2.0f;
      if (scaledRadius > 0) {
        shadowBlurFilter = new BlurMaskFilter(scaledRadius, BlurMaskFilter.Blur.NORMAL);
      } else {
        shadowBlurFilter = null;
      }

      lastShadowBlurRadius = shadow.getRadius();
    }
    shadowPaint.setColor(shadow.getColor());
    if (shadow.getRadius() > 0.0f) {
      shadowPaint.setMaskFilter(shadowBlurFilter);
    } else {
      shadowPaint.setMaskFilter(null);
    }
    shadowPaint.setFilterBitmap(true);

    // Draw the mask onto our shadowBitmap with the shadowPaint. This bitmap now contains the final
    // look of the shadow, correctly positioned inside a rectIncludingShadow-sized area
    // the int casts are very important here - they save us from some slow path for non-integer coords
    shadowBitmapCanvas.drawBitmap(shadowMaskBitmap, (int)(shadow.getDx() * pixelScaleX), (int)(shadow.getDy() * pixelScaleY), shadowPaint);

    // Now blit the result onto the final canvas. It might be tempting to skip shadowBitmap and draw the mask
    // directly onto the canvas with shadowPaint, but this breaks the blur, since Paint.setMaskFilter() is not
    // supported on hardware canvases.
    targetCanvas.drawBitmap(shadowBitmap, shadowBitmapSrcRect, intRectIncludingShadow, composePaint);
  }

  /** Renders a shadow (only the shadow) of this.renderNode to the provided canvas. */
  private void renderHardwareShadow(Canvas targetCanvas, DropShadow shadow) {
    if (renderNode == null || shadowRenderNode == null) {
      throw new IllegalStateException("Cannot render to render node outside a start()/finish() block");
    }

    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
      throw new RuntimeException("RenderEffect is not supported on API level <31");
    }

    // The render node canvas is in the pre-pixelscale space, so we have to scale the shadow parameters
    // by the pixel scale
    float pixelScaleX = preExistingTransform != null ? preExistingTransform[Matrix.MSCALE_X] : 1.0f;
    float pixelScaleY = preExistingTransform != null ? preExistingTransform[Matrix.MSCALE_Y] : 1.0f;

    if (lastRenderNodeShadow == null || !shadow.sameAs(lastRenderNodeShadow)) {
      RenderEffect effect = RenderEffect.createColorFilterEffect(new PorterDuffColorFilter(shadow.getColor(), PorterDuff.Mode.SRC_IN));
      if (shadow.getRadius() > 0.0f) {
        float scaledRadius = shadow.getRadius() * (pixelScaleX + pixelScaleY) / 2.0f;
        effect = RenderEffect.createBlurEffect(scaledRadius, scaledRadius, effect, Shader.TileMode.CLAMP);
      }
      shadowRenderNode.setRenderEffect(effect);
      lastRenderNodeShadow = shadow;
    }

    RectF rectIncludingShadow = calculateRectIncludingShadow(targetRect, shadow);
    RectF scaledRectIncludingShadow = new RectF(
        rectIncludingShadow.left * pixelScaleX,
        rectIncludingShadow.top * pixelScaleY,
        rectIncludingShadow.right * pixelScaleX,
        rectIncludingShadow.bottom * pixelScaleY
    );

    shadowRenderNode.setPosition(0, 0, (int)scaledRectIncludingShadow.width(), (int)scaledRectIncludingShadow.height());
    Canvas shadowCanvas = shadowRenderNode.beginRecording((int)scaledRectIncludingShadow.width(), (int)scaledRectIncludingShadow.height());
    // Offset so that the image starts at the top left, and then offset by shadow displacement
    shadowCanvas.translate(-scaledRectIncludingShadow.left + shadow.getDx() * pixelScaleX, -scaledRectIncludingShadow.top + shadow.getDy() * pixelScaleY);
    shadowCanvas.drawRenderNode(renderNode);
    shadowRenderNode.endRecording();

    targetCanvas.save();
    targetCanvas.translate(scaledRectIncludingShadow.left, scaledRectIncludingShadow.top);
    targetCanvas.drawRenderNode(shadowRenderNode);
    targetCanvas.restore();
  }
}
