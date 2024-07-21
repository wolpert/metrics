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

package com.codeheadsystems.metrics;

import com.codeheadsystems.metrics.impl.MetricImpl;
import java.time.Clock;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides helper methods for micrometer metrics.
 * Note that this version does not allow nesting of metrics usage.
 */
public class Metrics implements AutoCloseable {

  private static final Logger LOGGER = LoggerFactory.getLogger(Metrics.class);

  private final Clock clock;
  private final MetricImpl metricImpl;
  private final TagsSupplier tagsSupplier;
  private final TagsGenerator<Throwable> defaultTagsGeneratorForThrowable;
  private Tags tags;


  /**
   * Default constructor.
   *
   * @param clock       the clock to use.
   * @param metricImpl  the metric implementation.
   * @param defaultTags if available.
   */
  public Metrics(final Clock clock,
                 final MetricImpl metricImpl,
                 final TagsSupplier defaultTags) {
    this(clock, metricImpl, defaultTags, null);
  }

  /**
   * Default constructor.
   *
   * @param clock                            the clock to use.
   * @param metricImpl                       the metric implementation.
   * @param defaultTags                      if available.
   * @param defaultTagsGeneratorForThrowable to use for exceptions, optional.
   */
  public Metrics(final Clock clock,
                 final MetricImpl metricImpl,
                 final TagsSupplier defaultTags,
                 final TagsGenerator<Throwable> defaultTagsGeneratorForThrowable) {
    this.clock = clock;
    LOGGER.info("Metrics({},{})", metricImpl, defaultTags);
    this.metricImpl = metricImpl;
    this.tagsSupplier = defaultTags;
    this.defaultTagsGeneratorForThrowable = defaultTagsGeneratorForThrowable;
    tags = new Tags(tagsSupplier.get());
  }

  @Override
  public void close() throws Exception {
    LOGGER.info("close()");
    metricImpl.close();
    tags = new Tags(tagsSupplier.get());
  }

  /**
   * Gets the current tags. Changing tags from this object are not saved in the thread local.
   *
   * @return tags. tags
   */
  public Tags getTags() {
    return tags;
  }

  /**
   * Adds the thread local tags here.
   * overrideTags
   *
   * @param overrideTags to add.
   */
  public Tags and(final Tags overrideTags) {
    getTags().add(overrideTags);
    return tags;
  }

  /**
   * Adds the thread local tags here.
   *
   * @param overrideTags to add.
   */
  public Tags and(final String... overrideTags) {
    getTags().add(overrideTags);
    return tags;
  }

  /**
   * Increments the metric with the value.
   *
   * @param metricName to increment.
   * @param value      the value to add.
   * @param tags       to use, if any.
   */
  public void increment(final String metricName, final long value, final String... tags) {
    final Tags aggregateTags = getTags().from(tags);
    metricImpl.increment(metricName, value, aggregateTags);
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
  public <R, E extends Exception> R time(final String metricName,
                                         final CheckedSupplier<R, E> supplier,
                                         final TagsGenerator<R> tagsGeneratorForResult,
                                         final TagsGenerator<Throwable> tagsGeneratorForThrowable,
                                         final String... tags) throws E {
    final Tags aggregateTags = getTags().from(tags);
    final long start = clock.millis();
    long endDuration = 0;
    try {
      final R r = supplier.get();
      endDuration = clock.millis();
      if (tagsGeneratorForResult != null) {
        aggregateTags.add(tagsGeneratorForResult.from(r));
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
      metricImpl.time(metricName, Duration.ofMillis(duration), aggregateTags);
    }
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
  public <R, E extends Exception> R time(final String metricName,
                                         final CheckedSupplier<R, E> supplier,
                                         final String... tags) throws E {
    return time(metricName, supplier, null, null, tags);
  }
}