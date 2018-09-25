package com.airbnb.lottie.model;

import androidx.annotation.CheckResult;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Defines which content to target.
 * The keypath can contain wildcards ('*') with match exactly 1 item.
 * or globstars ('**') which match 0 or more items.
 *
 * For example, if your content were arranged like this:
 * Gabriel (Shape Layer)
 *     Body (Shape Group)
 *         Left Hand (Shape)
 *             Fill (Fill)
 *             Transform (Transform)
 *         ...
 * Brandon (Shape Layer)
 *     Body (Shape Group)
 *         Left Hand (Shape)
 *             Fill (Fill)
 *             Transform (Transform)
 *         ...
 *
 *
 * You could:
 *     Match Gabriel left hand fill:
 *        new KeyPath("Gabriel", "Body", "Left Hand", "Fill");
 *     Match Gabriel and Brandon's left hand fill:
 *        new KeyPath("*", "Body", Left Hand", "Fill");
 *     Match anything with the name Fill:
 *        new KeyPath("**", "Fill");
 *
 *
 * NOTE: Content that are part of merge paths or repeaters cannot currently be resolved with
 * a {@link KeyPath}. This may be fixed in the future.
 */
public class KeyPath {

  private final List<String> keys;
  @Nullable private KeyPathElement resolvedElement;

  public KeyPath(String... keys) {
    this.keys = Arrays.asList(keys);
  }

  /**
   * Copy constructor. Copies keys as well.
   */
  private KeyPath(KeyPath keyPath) {
    keys = new ArrayList<>(keyPath.keys);
    resolvedElement = keyPath.resolvedElement;
  }

  /**
   * Returns a new KeyPath with the key added.
   * This is used during keypath resolution. Children normally don't know about all of their parent
   * elements so this is used to keep track of the fully qualified keypath.
   * This returns a key keypath because during resolution, the full keypath element tree is walked
   * and if this modified the original copy, it would remain after popping back up the element tree.
   */
  @CheckResult
  @RestrictTo(RestrictTo.Scope.LIBRARY)
  public KeyPath addKey(String key) {
    KeyPath newKeyPath = new KeyPath(this);
    newKeyPath.keys.add(key);
    return newKeyPath;
  }

  /**
   * Return a new KeyPath with the element resolved to the specified {@link KeyPathElement}.
   */
  @RestrictTo(RestrictTo.Scope.LIBRARY)
  public KeyPath resolve(KeyPathElement element) {
    KeyPath keyPath = new KeyPath(this);
    keyPath.resolvedElement = element;
    return keyPath;
  }

  /**
   * Returns a {@link KeyPathElement} that this has been resolved to. KeyPaths get resolved with
   * resolveKeyPath on LottieDrawable or LottieAnimationView.
   */
  @RestrictTo(RestrictTo.Scope.LIBRARY)
  @Nullable
  public KeyPathElement getResolvedElement() {
    return resolvedElement;
  }

  /**
   * Returns whether they key matches at the specified depth.
   */
  @SuppressWarnings("RedundantIfStatement")
  @RestrictTo(RestrictTo.Scope.LIBRARY)
  public boolean matches(String key, int depth) {
    if (isContainer(key)) {
      // This is an artificial layer we programatically create.
      return true;
    }
    if (depth >= keys.size()) {
      return false;
    }
    if (keys.get(depth).equals(key) ||
        keys.get(depth).equals("**") ||
        keys.get(depth).equals("*")) {
      return true;
    }
    return false;
  }

  /**
   * For a given key and depth, returns how much the depth should be incremented by when
   * resolving a keypath to children.
   *
   * This can be 0 or 2 when there is a globstar and the next key either matches or doesn't match
   * the current key.
   */
  @RestrictTo(RestrictTo.Scope.LIBRARY)
  public int incrementDepthBy(String key, int depth) {
    if (isContainer(key)) {
      // If it's a container then we added programatically and it isn't a part of the keypath.
      return 0;
    }
    if (!keys.get(depth).equals("**")) {
      // If it's not a globstar then it is part of the keypath.
      return 1;
    }
    if (depth == keys.size() - 1) {
      // The last key is a globstar.
      return 0;
    }
    if (keys.get(depth + 1).equals(key)) {
      // We are a globstar and the next key is our current key so consume both.
      return 2;
    }
    return 0;
  }

  /**
   * Returns whether the key at specified depth is fully specific enough to match the full set of
   * keys in this keypath.
   */
  @RestrictTo(RestrictTo.Scope.LIBRARY)
  public boolean fullyResolvesTo(String key, int depth) {
    if (depth >= keys.size()) {
      return false;
    }
    boolean isLastDepth = depth == keys.size() - 1;
    String keyAtDepth = keys.get(depth);
    boolean isGlobstar = keyAtDepth.equals("**");

    if (!isGlobstar) {
      boolean matches = keyAtDepth.equals(key) || keyAtDepth.equals("*");
      return (isLastDepth || (depth == keys.size() - 2 && endsWithGlobstar())) && matches;
    }

    boolean isGlobstarButNextKeyMatches = !isLastDepth && keys.get(depth + 1).equals(key);
    if (isGlobstarButNextKeyMatches) {
      return depth == keys.size() - 2 ||
          (depth == keys.size() - 3 && endsWithGlobstar());
    }

    if (isLastDepth) {
      return true;
    }
    if (depth + 1 < keys.size() - 1) {
      // We are a globstar but there is more than 1 key after the globstar we we can't fully match.
      return false;
    }
    // Return whether the next key (which we now know is the last one) is the same as the current
    // key.
    return keys.get(depth + 1).equals(key);
  }

  /**
   * Returns whether the keypath resolution should propagate to children. Some keypaths resolve
   * to content other than leaf contents (such as a layer or content group transform) so sometimes
   * this will return false.
   */
  @SuppressWarnings("SimplifiableIfStatement")
  @RestrictTo(RestrictTo.Scope.LIBRARY)
  public boolean propagateToChildren(String key, int depth) {
    if (key.equals("__container")) {
      return true;
    }
    return depth < keys.size() - 1 || keys.get(depth).equals("**");
  }

  /**
   * We artificially create some container groups (like a root ContentGroup for the entire animation
   * and for the contents of a ShapeLayer).
   */
  private boolean isContainer(String key) {
    return key.equals("__container");
  }

  private boolean endsWithGlobstar() {
    return keys.get(keys.size() - 1).equals("**");
  }

  public String keysToString() {
    return keys.toString();
  }

  @Override public String toString() {
    return "KeyPath{" + "keys=" + keys + ",resolved=" + (resolvedElement != null) + '}';
  }
}
