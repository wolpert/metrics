package com.codeheadsystems.metrics;

import java.time.Duration;

/**
 * Metric interface.
 */
public interface Metrics {

  /**
   * Add tags to the existing tags.
   *
   * @param overrideTags to add.
   * @return the aggregated tags.
   */
  Tags and(Tags overrideTags);

  /**
   * Add tags to the existing tags.
   *
   * @param overrideTags to add.
   * @return the aggregated tags.
   */
  Tags and(String... overrideTags);

  /**
   * Increments the metric with the value.
   *
   * @param metricName to increment.
   * @param value      the value to add.
   * @param tags       to use, if any.
   */
  void increment(String metricName, long value, Tags tags);

  /**
   * Increments the metric by 1.
   *
   * @param metricName to increment.
   * @param tags       to use, if any.
   */
  default void increment(String metricName, Tags tags) {
    increment(metricName, 1, tags);
  }

  /**
   * Increments the metric by 1.
   *
   * @param metricName to increment.
   * @param tags       to use, if any.
   */
  default void increment(String metricName, String... tags) {
    increment(metricName, 1, tags);
  }

  /**
   * Increments the metric with the value.
   *
   * @param metricName to increment.
   * @param value      the value to add.
   * @param tags       to use, if any.
   */
  default void increment(String metricName, long value, String... tags) {
    increment(metricName, value, Tags.of(tags));
  }

  /**
   * Times the action in the supplier.
   *
   * @param metricName                to store the time.
   * @param supplier                  which is called to get the result.
   * @param tagsGeneratorForResult    optional generator for tags based on the result.
   * @param tagsGeneratorForThrowable optional tag generator for any thrown exception.
   * @param tags                      optional tags you may want to include.
   * @param <R>                       the type of result from the supplier.
   * @param <E>                       the exception the supplier can throw.
   * @return the result of the supplier.
   * @throws E if the supplier throws an exception.
   */
  <R, E extends Exception> R time(String metricName,
                                  CheckedSupplier<R, E> supplier,
                                  TagsGenerator<R> tagsGeneratorForResult,
                                  TagsGenerator<Throwable> tagsGeneratorForThrowable,
                                  Tags tags) throws E;

  /**
   * Times the action in the supplier.
   *
   * @param metricName                to store the time.
   * @param supplier                  which is called to get the result.
   * @param tagsGeneratorForResult    optional generator for tags based on the result.
   * @param tagsGeneratorForThrowable optional tag generator for any thrown exception.
   * @param tags                      optional tags you may want to include.
   * @param <R>                       the type of result from the supplier.
   * @param <E>                       the exception the supplier can throw.
   * @return the result of the supplier.
   * @throws E if the supplier throws an exception.
   */
  default <R, E extends Exception> R time(String metricName,
                                          CheckedSupplier<R, E> supplier,
                                          TagsGenerator<R> tagsGeneratorForResult,
                                          TagsGenerator<Throwable> tagsGeneratorForThrowable,
                                          String... tags) throws E {
    return time(metricName, supplier, tagsGeneratorForResult, tagsGeneratorForThrowable, Tags.of(tags));
  }

  /**
   * Times the action in the supplier.
   *
   * @param metricName to store the time.
   * @param supplier   which is called to get the result.
   * @param tags       optional tags you may want to include.
   * @param <R>        the type of result from the supplier.
   * @param <E>        the exception the supplier can throw.
   * @return the result of the supplier.
   * @throws E if the supplier throws an exception.
   */
  default <R, E extends Exception> R time(String metricName,
                                          CheckedSupplier<R, E> supplier,
                                          Tags tags) throws E {
    return time(metricName, supplier, null, null, tags);
  }

  /**
   * Times the action in the supplier.
   *
   * @param metricName to store the time.
   * @param supplier   which is called to get the result.
   * @param tags       optional tags you may want to include.
   * @param <R>        the type of result from the supplier.
   * @param <E>        the exception the supplier can throw.
   * @return the result of the supplier.
   * @throws E if the supplier throws an exception.
   */
  default <R, E extends Exception> R time(String metricName,
                                          Tags tags,
                                          CheckedSupplier<R, E> supplier) throws E {
    return time(metricName, supplier, null, null, tags);
  }

  /**
   * Times the action in the supplier.
   *
   * @param metricName to store the time.
   * @param supplier   which is called to get the result.
   * @param tags       optional tags you may want to include.
   * @param <R>        the type of result from the supplier.
   * @param <E>        the exception the supplier can throw.
   * @return the result of the supplier.
   * @throws E if the supplier throws an exception.
   */
  default <R, E extends Exception> R time(String metricName,
                                          CheckedSupplier<R, E> supplier,
                                          String... tags) throws E {
    return time(metricName, supplier, Tags.of(tags));
  }

  /**
   * Allows to set the time metric manually instead of using the helpers.
   *
   * @param metricName to store the time.
   * @param duration   how long it took.
   * @param tags       optional tags you may want to include.
   */
  void publishTime(String metricName, Duration duration, Tags tags);


}
