package com.codeheadsystems.metrics.impl;

import com.codeheadsystems.metrics.CheckedSupplier;
import com.codeheadsystems.metrics.Metrics;
import com.codeheadsystems.metrics.Tags;
import com.codeheadsystems.metrics.TagsGenerator;

/**
 * Empty class that you can use for a metrics instance.
 */
public class NullMetricsImpl implements Metrics {
  @Override
  public Tags and(final Tags overrideTags) {
    return null;
  }

  @Override
  public Tags and(final String... overrideTags) {
    return null;
  }

  @Override
  public void increment(final String metricName, final long value, final String... tags) {

  }

  @Override
  public <R, E extends Exception> R time(final String metricName, final CheckedSupplier<R, E> supplier, final TagsGenerator<R> tagsGeneratorForResult, final TagsGenerator<Throwable> tagsGeneratorForThrowable, final String... tags) throws E {
    return supplier.get();
  }

  @Override
  public <R, E extends Exception> R time(final String metricName, final CheckedSupplier<R, E> supplier, final String... tags) throws E {
    return supplier.get();
  }
}
