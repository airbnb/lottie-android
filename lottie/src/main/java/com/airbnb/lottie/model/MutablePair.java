package com.airbnb.lottie.model;

import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.core.util.Pair;

import static androidx.annotation.RestrictTo.Scope.LIBRARY;

/**
 * Non final version of {@link Pair}
 * @param <T>
 */
@RestrictTo(LIBRARY)
public class MutablePair<T> {
  @Nullable T first;
  @Nullable T second;

  public void set(T first, T second) {
    this.first = first;
    this.second = second;
  }

  /**
   * Checks the two objects for equality by delegating to their respective
   * {@link Object#equals(Object)} methods.
   *
   * @param o the {@link Pair} to which this one is to be checked for equality
   * @return true if the underlying objects of the Pair are both considered
   *         equal
   */
  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Pair)) {
      return false;
    }
    Pair<?, ?> p = (Pair<?, ?>) o;
    return objectsEqual(p.first, first) && objectsEqual(p.second, second);
  }

  private static boolean objectsEqual(Object a, Object b) {
    return a == b || (a != null && a.equals(b));
  }

  /**
   * Compute a hash code using the hash codes of the underlying objects
   *
   * @return a hashcode of the Pair
   */
  @Override
  public int hashCode() {
    return (first == null ? 0 : first.hashCode()) ^ (second == null ? 0 : second.hashCode());
  }

  @Override
  public String toString() {
    return "Pair{" + String.valueOf(first) + " " + String.valueOf(second) + "}";
  }
}
