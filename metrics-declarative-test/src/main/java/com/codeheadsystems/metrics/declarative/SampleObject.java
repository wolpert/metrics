package com.codeheadsystems.metrics.declarative;

import com.codeheadsystems.metrics.MetricFactory;
import com.codeheadsystems.metrics.Tags;
import com.codeheadsystems.metrics.impl.MetricPublisher;
import java.time.Duration;

/**
 * The type Sample object.
 */
public class SampleObject implements MetricPublisher {

  /**
   * Metrics factory metric factory.
   *
   * @return the metric factory
   */
  @DeclarativeFactory
  public MetricFactory metricsFactory() {
    return MetricFactory.builder().build();
  }

  /**
   * Method with metrics.
   */
  @Metrics
  public void methodWithMetrics() {
    System.out.println("methodWithMetrics()");
  }

  /**
   * Method with metrics and tags boolean.
   *
   * @param name the a name
   * @return the boolean
   */
  @Metrics
  public Boolean methodWithMetricsAndTags(@Tag String name) {
    System.out.println("methodWithMetricsAndTags()");
    return true;
  }

  @Override
  public void increment(final String metricName, final long value, final Tags tags) {

  }

  @Override
  public void time(final String metricName, final Duration duration, final Tags tags) {

  }
}
