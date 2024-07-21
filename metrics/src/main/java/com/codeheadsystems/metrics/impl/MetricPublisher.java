package com.codeheadsystems.metrics.impl;

import com.codeheadsystems.metrics.Tags;
import java.time.Duration;

/**
 * A list of methods required to implement for any metric provider.
 */
public interface MetricPublisher extends AutoCloseable {

  /**
   * If you need to close the metric provider, do it here.
   */
  @Override
  default void close() throws Exception {
  }

  /**
   * Increment a metric.
   *
   * @param metricName to increment.
   * @param value      how much to increment.
   * @param tags       to add to the metric.
   */
  void increment(String metricName, long value, Tags tags);

  /**
   * Store the execute time for a method.
   *
   * @param metricName to time.
   * @param duration   how long it lasted.
   * @param tags       to aad to the metric.
   */
  void time(String metricName, Duration duration, Tags tags);

}
