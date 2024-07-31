package com.codeheadsystems.metrics.declarative;

import com.codeheadsystems.metrics.MetricFactory;
import com.codeheadsystems.metrics.Tags;
import com.codeheadsystems.metrics.impl.MetricPublisher;
import java.io.IOException;
import java.time.Duration;

/**
 * The type Sample object.
 */
public class SampleObject {

  /**
   * Metrics factory metric factory.
   *
   * @return the metric factory
   */
  @DeclarativeFactory
  public MetricFactory metricsFactory(MetricPublisher metricPublisher) {
    return MetricFactory.builder()
        .withMetricPublisher(metricPublisher)
        .build();
  }


  public void methodWithoutMetrics() {

  }

  /**
   * Method with metrics.
   */
  @Metrics
  public void methodWithMetrics() {
  }

  /**
   * Method with metrics and tags boolean.
   *
   * @param name the a name
   * @return the boolean
   */
  @Metrics("metricsNameWasOverridden")
  public Boolean methodWithMetricsAndTagsReturnTrue(@Tag("name") String name) {
    return true;
  }

  @Metrics
  public boolean methodWithMetricsAndTagsWithDefinedException(@Tag("anotherName") String name, String other) throws IOException {
    return true;
  }

  @Metrics
  public boolean methodWithMetricsAndTagsWithThrownException(@Tag("name") String name, @Tag("thing") String other) throws IOException {
    throw new IOException();
  }

  @Metrics
  public void methodWithMetricsAndTagsAndThrownRuntimeException(@Tag("notname") String name) {
    throw new IllegalStateException();
  }
}
