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
  private final TagsGenerator<Throwable> defaultTagsGeneratorForThrowable;
  private final TagsGeneratorRegistry tagsGeneratorRegistry;
  private final Tags tags;

  /**
   * Default constructor.
   *
   * @param clock                            the clock to use.
   * @param metricPublisher                  the metric implementation.
   * @param defaultTagsGeneratorForThrowable to use for exceptions, optional.
   * @param tagsGeneratorRegistry            to help with tags.
   */
  public MetricsImpl(final Clock clock,
                     final MetricPublisher metricPublisher,
                     final TagsGenerator<Throwable> defaultTagsGeneratorForThrowable,
                     final TagsGeneratorRegistry tagsGeneratorRegistry,
                     final Tags tags) {
    LOGGER.info("MetricsImpl({},{})", metricPublisher, tags);
    this.clock = clock;
    this.tagsGeneratorRegistry = tagsGeneratorRegistry;
    this.metricPublisher = metricPublisher;
    this.tags = tags;
    this.defaultTagsGeneratorForThrowable = defaultTagsGeneratorForThrowable;
  }

  /**
   * Called when the metrics object is usable.
   */
  public void open() {
    try {
      metricPublisher.open();
    } catch (Throwable e) {
      LOGGER.warn("Metrics was unable to close", e);
    }
  }

  @Override
  public void close() {
    try {
      metricPublisher.close();
    } catch (Throwable e) {
      LOGGER.warn("Metrics was unable to close", e);
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

}