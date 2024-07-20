package com.codeheadsystems.metrics;

import io.micrometer.core.instrument.Tags;

/**
 * A supplier for tags given the object in question. Example, an HTTP response object
 * could supply tags using data from the response.
 */
@FunctionalInterface
public interface TagsSupplier<T> {

  /**
   * Get the tags for the object.
   *
   * @param object to get tags for.
   * @return tags.
   */
  Tags from(T object);

  /**
   * Starting with the tags gen, will add the tags as received from the object.
   *
   * @param object      tags to get from the object.
   * @param initialTags base tags
   * @return aggregated tags.
   */
  default Tags from(final T object, final Tags initialTags) {
    return initialTags.and(from(object));
  }
}
