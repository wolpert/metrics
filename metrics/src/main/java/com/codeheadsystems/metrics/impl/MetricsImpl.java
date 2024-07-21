/*
 *    Copyright (c) 2022 Ned Wolpert <ned.wolpert@gmail.com>
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.codeheadsystems.metrics.impl;

import com.codeheadsystems.metrics.CheckedSupplier;
import com.codeheadsystems.metrics.Metrics;
import com.codeheadsystems.metrics.Tags;
import com.codeheadsystems.metrics.TagsGenerator;
import com.codeheadsystems.metrics.TagsSupplier;
import com.codeheadsystems.metrics.helper.TagsGeneratorRegistry;
import java.time.Clock;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides helper methods for micrometer metrics.
 * Note that this version does not allow nesting of metrics usage.
 */
public class MetricsImpl implements AutoCloseable, Metrics {

  private static final Logger LOGGER = LoggerFactory.getLogger(MetricsImpl.class);

  private final Clock clock;
  private final MetricPublisher metricPublisher;
  private final TagsSupplier tagsSupplier;
  private final TagsGenerator<Throwable> defaultTagsGeneratorForThrowable;
  private final TagsGeneratorRegistry tagsGeneratorRegistry;
  private Tags tags;
  private int openCount = 0;

  /**
   * Default constructor.
   *
   * @param clock                            the clock to use.
   * @param metricPublisher                  the metric implementation.
   * @param defaultTags                      if available.
   * @param defaultTagsGeneratorForThrowable to use for exceptions, optional.
   * @param tagsGeneratorRegistry            to help with tags.
   */
  public MetricsImpl(final Clock clock,
                     final MetricPublisher metricPublisher,
                     final TagsSupplier defaultTags,
                     final TagsGenerator<Throwable> defaultTagsGeneratorForThrowable,
                     final TagsGeneratorRegistry tagsGeneratorRegistry) {
    this.clock = clock;
    this.tagsGeneratorRegistry = tagsGeneratorRegistry;
    LOGGER.info("MetricsImpl({},{})", metricPublisher, defaultTags);
    this.metricPublisher = metricPublisher;
    this.tagsSupplier = defaultTags;
    this.defaultTagsGeneratorForThrowable = defaultTagsGeneratorForThrowable;
    tags = new Tags(tagsSupplier.get());
  }

  /**
   * Used for metrics counting.
   */
  public void open() {
    openCount++;
    LOGGER.trace("open() {}", openCount);
  }

  /**
   * Resets the tags and open count.
   */
  public void reset() {
    openCount = 0;
    tags = new Tags(tagsSupplier.get());
    try {
      metricPublisher.close();
    } catch (Exception e) {
      LOGGER.error("Error closing metricPublisher", e);
    }
  }

  @Override
  public void close() throws Exception {
    openCount--;
    LOGGER.trace("close() {}", openCount);
    if (openCount < 1) {
      reset();
    }
  }

  /**
   * Gets the current tags. Changing tags from this object are not saved in the thread local.
   *
   * @return tags. tags
   */
  public Tags getTags() {
    return tags;
  }

  @Override
  public Tags and(final Tags overrideTags) {
    getTags().add(overrideTags);
    return tags;
  }

  @Override
  public Tags and(final String... overrideTags) {
    getTags().add(overrideTags);
    return tags;
  }

  @Override
  public void increment(final String metricName, final long value, final Tags tags) {
    final Tags aggregateTags = getTags().from(tags);
    metricPublisher.increment(metricName, value, aggregateTags);
  }

  @Override
  public <R, E extends Exception> R time(final String metricName,
                                         final CheckedSupplier<R, E> supplier,
                                         final TagsGenerator<R> tagsGeneratorForResult,
                                         final TagsGenerator<Throwable> tagsGeneratorForThrowable,
                                         final Tags tags) throws E {
    final Tags aggregateTags = getTags().from(tags);
    final long start = clock.millis();
    long endDuration = 0;
    try {
      final R r = supplier.get();
      endDuration = clock.millis();
      if (tagsGeneratorForResult != null) {
        aggregateTags.add(tagsGeneratorForResult.from(r));
      } else if (tagsGeneratorRegistry != null) {
        tagsGeneratorRegistry.aggregateIfFound(aggregateTags, r);
      }
      return r;
    } catch (final Throwable e) {
      endDuration = clock.millis();
      if (tagsGeneratorForThrowable != null) {
        aggregateTags.add(tagsGeneratorForThrowable.from(e));
      } else if (defaultTagsGeneratorForThrowable != null) {
        aggregateTags.add(defaultTagsGeneratorForThrowable.from(e));
      }
      throw e;
    } finally {
      final long duration = endDuration - start;
      metricPublisher.time(metricName, Duration.ofMillis(duration), aggregateTags);
    }
  }

  @Override
  public <R, E extends Exception> R time(final String metricName,
                                         final CheckedSupplier<R, E> supplier,
                                         final Tags tags) throws E {
    return time(metricName, supplier, null, null, tags);
  }
}