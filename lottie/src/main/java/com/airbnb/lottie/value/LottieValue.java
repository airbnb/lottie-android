package com.airbnb.lottie.value;

import com.airbnb.lottie.animation.content.Content;
import com.airbnb.lottie.animation.content.ContentGroup;
import com.airbnb.lottie.model.layer.BaseLayer;
import com.airbnb.lottie.model.layer.CompositionLayer;
import com.airbnb.lottie.model.layer.ShapeLayer;

import java.util.List;

/**
 * Interface used to update or add values to an existing animation.
 *
 * If updateValue is true, the value will update the value of the keyframe closest to the
 * specified frame. Within the keyframe, either the start or end value will be updated depending
 * on which is closer to the specified frame.
 *
 * If the previous/next keyframe is contiguous with the one being updated, its start/end values
 * will be updated to maintain continuity.
 *
 * TODO: updateValue == false.
 */
public abstract class LottieValue<T> {

  final int frame;
  final boolean updateValue;
  private T value;

  LottieValue(T value, int frame, boolean updateValue) {
    if (!updateValue) {
      throw new UnsupportedOperationException("Only updateValue is supported yet!");
    }
    this.frame = frame;
    this.updateValue = updateValue;
    this.value = value;
  }

  public T getValue() {
    return value;
  }

  public void setValue(T value) {
    this.value = value;
  }

  @SuppressWarnings("unused") public abstract void offsetValue(T value);
  public abstract void apply(Content content);

  /**
   * Applies this value to any content in the composition that matches the key path.
   *
   * @see KeyPath for more information on KeyPath matching.
   */
  public void apply(CompositionLayer comp, KeyPath keyPath) {
    if (keyPath.getKeyPath().length == 0 && keyPath.isWildcard()) {
      applyRecursive(comp);
      return;
    }

    for (int i = comp.getLayers().size() - 1; i >= 0; i--) {
      apply(comp.getLayers().get(i), keyPath, 0);
    }
  }

  private void apply(Content content, KeyPath keyPath, int depth) {
    String key = keyPath.getKeyPath()[depth];
    boolean matchesThis = key.equals("*") || key.equals(content.getName());
    if (!matchesThis) {
      return;
    }
    if (depth == keyPath.getKeyPath().length - 1) {
      if (keyPath.isWildcard()) {
        applyRecursive(content);
      } else {
        apply(content);
      }
      return;
    }
    if (content instanceof ContentGroup) {
      List<Content> contents = ((ContentGroup) content).getContents();
      for (int i = contents.size() - 1; i >= 0; i--) {
        apply(contents.get(i), keyPath, depth + 1);
      }
    } else if (content instanceof CompositionLayer) {
      List<BaseLayer> layers = ((CompositionLayer) content).getLayers();
      for (int i = layers.size() - 1; i >= 0; i--) {
        apply(layers.get(i), keyPath, depth + 1);
      }
    } else if (content instanceof ShapeLayer) {
      List<Content> contents = ((ShapeLayer) content).getContents();
      for (int i = contents.size() - 1; i >= 0; i--) {
        apply(contents.get(i), keyPath, depth + 1);
      }
    }
  }

  private void applyRecursive(Content content) {
    apply(content);
    if (content instanceof ContentGroup) {
      List<Content> contents = ((ContentGroup) content).getContents();
      for (int i = contents.size() - 1; i >= 0; i--) {
        applyRecursive(contents.get(i));
      }
    } else if (content instanceof CompositionLayer) {
      List<BaseLayer> layers = ((CompositionLayer) content).getLayers();
      for (int i = layers.size() - 1; i >= 0; i--) {
        applyRecursive(layers.get(i));
      }
    } else if (content instanceof ShapeLayer) {
      List<Content> contents = ((ShapeLayer) content).getContents();
      for (int i = contents.size() - 1; i >= 0; i--) {
        applyRecursive(contents.get(i));
      }
    }
  }
}
