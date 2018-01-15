package com.airbnb.lottie.animation.content;

import java.util.ListIterator;

/**
 * Content that may want to absorb and take ownership of the content around it.
 * For example, merge paths will absorb the shapes above it and repeaters will absorb the content
 * above it.
 */
interface GreedyContent {
  /**
   * An iterator of contents that can be used to take ownership of contents. If ownership is taken,
   * the content should be removed from the iterator.
   *
   * The contents should be iterated by calling hasPrevious() and previous() so that the list of
   * contents is traversed from bottom to top which is the correct order for handling AE logic.
   */
  void absorbContent(ListIterator<Content> contents);
}
