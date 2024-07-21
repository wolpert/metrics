package com.codeheadsystems.metrics.impl;

import com.codeheadsystems.metrics.Tags;
import java.time.Duration;

/**
 * Null metrics implementation.
 */
public class NullMetricsPublisher implements MetricPublisher {

  @Override
  public void time(String metricName, Duration duration, Tags tags) {
  }

  @Override
  public void increment(final String metricName, final long value, final Tags tags) {

  }
}
