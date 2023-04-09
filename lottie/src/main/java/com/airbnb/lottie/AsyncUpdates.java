package com.airbnb.lottie;

/**
 * **Note: this API is experimental and may changed.**
 * <p/>
 * When async updates are enabled, parts of animation updates will happen off of the main thread.
 * <p/>
 * At a high level, during the animation loop, there are two main code paths:
 * 1. setProgress
 * 2. draw
 * <p/>
 * setProgress is called on every frame when the internal animator updates or if you manually call setProgress.
 * setProgress must then iterate through every single node in the animation (every shape, fill, mask, stroke, etc.)
 * and call setProgress on it. When progress is set on a node, it will:
 * 1. Call the dynamic property value callback if one has been set by you.
 * 2. Recalculate what its own progress is. Various animation features like interpolators or time remapping
 *    will cause the progress value for a given node to be different than the top level progress.
 * 3. If a node's progress has changed, it will call invalidate which will invalidate values that are
 *    cached and derived from that node's progress and then bubble up the invalidation to LottieDrawable
 *    to ensure that Android renders a new frame.
 * <p/>
 * draw is what actually draws your animation to a canvas. Many of Lottie's operations are completed or
 * cached in the setProgress path. However, there are a few things (like parentMatrix) that Lottie only has access
 * to in the draw path and it, of course, needs to actually execute the canvas operations to draw the animation.
 * <p/>
 * Without async updates, in a single main thread frame, Lottie will call setProgress immediately followed by draw.
 * <p/>
 * With async updates, Lottie will determine if the most recent setProgress is still close enough to be considered
 * valid. An existing progress will be considered valid if it is within LottieDrawable.MAX_DELTA_MS_ASYNC_SET_PROGRESS
 * milliseconds from the current actual progress.
 * If the calculated progress is close enough, it will only execute draw. Once draw completes, it will schedule a
 * setProgress to be run on a background thread immediately after draw finishes and it will likely complete well
 * before the next frame starts.
 * <p/>
 * The background thread is created via LottieDrawable.setProgressExecutor. You can refer to it for the current default
 * thread pool configuration.
 */
public enum AsyncUpdates {
  /**
   * Default value.
   * <p/>
   * This will default to DISABLED until this feature has had time to incubate.
   * The behavior of AUTOMATIC may change over time.
   */
  AUTOMATIC,
  /**
   * Use the async update path. Refer to the docs for {@link AsyncUpdates} for more details.
   */
  ENABLED,
  /**
   * Do not use the async update path. Refer to the docs for {@link AsyncUpdates} for more details.
   */
  DISABLED,
}
