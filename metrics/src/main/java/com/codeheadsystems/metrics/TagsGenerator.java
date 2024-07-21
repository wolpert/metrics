package com.codeheadsystems.metrics;

/**
 * A supplier for tags given the object in question. Example, an HTTP response object
 * could supply tags using data from the response.
 */
@FunctionalInterface
public interface TagsGenerator<T> {

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
   * @return aggregated tags without updating the original.
   */
  default Tags from(final T object, final Tags initialTags) {
    return initialTags.from(from(object));
  }
}
