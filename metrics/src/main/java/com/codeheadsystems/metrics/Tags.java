package com.codeheadsystems.metrics;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A holder for tags.
 */
public class Tags {

  private final Map<String, String> tags;

  /**
   * Default constructor... tags are optional.
   *
   * @param tags to start with if they are here.
   */
  public Tags(final String... tags) {
    this.tags = new HashMap<>();
    add(tags);
  }

  /**
   * Prefill with a set of tags.
   *
   * @param tags to start with, makes a copy of the map.
   */
  public Tags(final Map<String, String> tags) {
    this.tags = new HashMap<>(tags);
  }

  /**
   * Copy constructor.
   *
   * @param tags to start with, makes a copy.
   */
  public Tags(final Tags tags) {
    this.tags = new HashMap<>(tags.getTags());
  }

  /**
   * Returns a new Tags object with the tags provided.
   *
   * @param tags to add.
   * @return tags object.
   */
  public static Tags of(final String... tags) {
    return new Tags(tags);
  }

  /**
   * Returns a new Tags object with the tags provided.
   *
   * @param tags to clone.
   * @return new tags object.
   */
  public static Tags of(final Tags tags) {
    return new Tags(tags);
  }

  /**
   * Returns a new Tags object with the tags provided.
   *
   * @return empty tags.
   */
  public static Tags empty() {
    return new Tags();
  }

  /**
   * Adds the tags into this tags. Changes this object.
   *
   * @param tags to add.
   * @return this.
   */
  public Tags add(final String... tags) {
    if (tags.length % 2 != 0) {
      throw new IllegalArgumentException("Tags must be in key value pairs");
    }
    for (int i = 0; i < tags.length; i += 2) {
      this.tags.put(tags[i], tags[i + 1]);
    }
    return this;
  }

  /**
   * Adds the tags into this tags. Changes this object.
   *
   * @param tags to add.
   * @return this.
   */
  public Tags add(final Tags tags) {
    this.tags.putAll(tags.getTags());
    return this;
  }

  /**
   * Copy constructor of this tags object, and includes the new tags.
   *
   * @param tags to add to us.
   * @return a new object.
   */
  public Tags from(final String... tags) {
    return new Tags(this).add(tags);
  }

  /**
   * Copy constructor of this tags object, and includes the new tags.
   *
   * @param tags to add to us.
   * @return a new object.
   */
  public Tags from(final Tags tags) {
    return new Tags(this).add(tags);
  }


  public Map<String, String> getTags() {
    return tags;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final Tags tags1 = (Tags) o;
    return Objects.equals(tags, tags1.tags);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(tags);
  }

  @Override
  public String toString() {
    return "Tags{" + "tags=" + tags + '}';
  }
}
