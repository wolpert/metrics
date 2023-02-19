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

import static com.codeheadsystems.metrics.dagger.MetricsModule.METER_REGISTRY;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import java.util.function.Supplier;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Provides helper methods for micrometer metrics.
 */
@Singleton
public class Metrics {

  private final MeterRegistry registry;

  private final ThreadLocal<Tags> tagThreadLocal;

  /**
   * Default constructor.
   *
   * @param registry    to use.
   * @param defaultTags if available.
   */
  @Inject
  public Metrics(@Named(METER_REGISTRY) final MeterRegistry registry,
                 final Supplier<Tags> defaultTags) {
    this.registry = registry;
    tagThreadLocal = ThreadLocal.withInitial(defaultTags::get);
  }

  /**
   * Closes the tags/resets.
   */
  public void close() {
    tagThreadLocal.set(Tags.empty());
  }

  /**
   * Gets the current tags. Changing tags from this object are not saved in the thread local.
   *
   * @return tags.
   */
  public Tags getTags() {
    return tagThreadLocal.get();
  }

  /**
   * Adds the thread local tags here.
   *
   * @param tags to add.
   */
  public void and(final Tags tags) {
    tagThreadLocal.set(getTags().and(tags));
  }

  /**
   * Adds the thread local tags here.
   *
   * @param tag to add.
   */
  public void and(final Tag tag) {
    tagThreadLocal.set(getTags().and(tag));
  }

  /**
   * Adds the thread local tags here.
   *
   * @param tags to add.
   */
  public void and(final String... tags) {
    tagThreadLocal.set(getTags().and(tags));
  }

  /**
   * Getter for the registry.
   *
   * @return registry.
   */
  public MeterRegistry registry() {
    return registry;
  }

  /**
   * Returns a counter with the default tags.
   *
   * @param name counter.
   * @return the counter.
   */
  public Counter counter(final String name) {
    return registry.counter(name, getTags());
  }

  /**
   * Returns a counter with the default tags and the tags you give it.
   *
   * @param name       counter.
   * @param customTags the custom tags.
   * @return the counter.
   */
  public Counter counter(final String name, final Tags customTags) {
    return registry.counter(name, getTags().and(customTags));
  }

  /**
   * Times the supplier setting up metric based on the name.
   *
   * @param name     for metrics.
   * @param supplier to return the thing.
   * @param <R>      type of thing.
   * @return thing we did.
   */
  public <R> R time(final String name, final Supplier<R> supplier) {
    return time(name, registry.timer(name, getTags()), supplier);
  }

  /**
   * Times the supplier setting up metric based on the name.
   *
   * @param name       for metrics.
   * @param customTags the custom tags.
   * @param supplier   to return the thing.
   * @param <R>        type of thing.
   * @return thing we did.
   */
  public <R> R time(final String name, final Tags customTags, final Supplier<R> supplier) {
    return time(name, customTags, registry.timer(name, getTags().and(customTags)), supplier);
  }

  /**
   * Helper method to time a request and include the success counters.
   *
   * @param name     for metrics.
   * @param supplier to return the thing.
   * @param <R>      type of thing.
   * @param timer    Timer to use.
   * @return thing we did.
   */
  public <R> R time(final String name,
                    final Timer timer,
                    final Supplier<R> supplier) {
    final Counter success = counter(name, Tags.of("success", "true"));
    final Counter failure = counter(name, Tags.of("success", "false"));
    return time(timer, success, failure, supplier);
  }

  /**
   * Helper method to time a request and include the success counters.
   *
   * @param name       for metrics.
   * @param customTags the custom tags.
   * @param supplier   to return the thing.
   * @param <R>        type of thing.
   * @param timer      Timer to use.
   * @return thing we did.
   */
  public <R> R time(final String name,
                    final Tags customTags,
                    final Timer timer,
                    final Supplier<R> supplier) {
    final Counter success = counter(name, customTags.and("success", "true"));
    final Counter failure = counter(name, customTags.and("success", "false"));
    return time(timer, success, failure, supplier);
  }


  /**
   * Helper method to time a request and include the success counters.
   *
   * @param failure  metric.
   * @param success  metric.
   * @param supplier to return the thing.
   * @param <R>      type of thing.
   * @param timer    Timer to use.
   * @return thing we did.
   */
  public <R> R time(final Timer timer,
                    final Counter success,
                    final Counter failure,
                    final Supplier<R> supplier) {
    try {
      final R result = timer.record(supplier);
      success.increment(1);
      failure.increment(0);
      return result;
    } catch (RuntimeException re) {
      success.increment(0);
      failure.increment(1);
      throw re;
    }
  }

}