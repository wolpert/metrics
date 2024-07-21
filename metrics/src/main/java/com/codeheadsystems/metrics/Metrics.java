package com.codeheadsystems.metrics;

public interface Metrics {
  Tags and(Tags overrideTags);

  Tags and(String... overrideTags);

  void increment(String metricName, long value, String... tags);

  <R, E extends Exception> R time(String metricName,
                                  CheckedSupplier<R, E> supplier,
                                  TagsGenerator<R> tagsGeneratorForResult,
                                  TagsGenerator<Throwable> tagsGeneratorForThrowable,
                                  String... tags) throws E;

  <R, E extends Exception> R time(String metricName,
                                  CheckedSupplier<R, E> supplier,
                                  String... tags) throws E;
}
