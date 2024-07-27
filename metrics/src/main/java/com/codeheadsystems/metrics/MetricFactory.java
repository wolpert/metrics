package com.codeheadsystems.metrics;

import com.codeheadsystems.metrics.helper.TagsGeneratorRegistry;
import com.codeheadsystems.metrics.impl.MetricPublisher;
import com.codeheadsystems.metrics.impl.MetricsImpl;
import com.codeheadsystems.metrics.impl.NullMetricsPublisher;
import java.time.Clock;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides a way to manage metrics from threads.
 */
public class MetricFactory {

  private static final Logger LOGGER = LoggerFactory.getLogger(MetricFactory.class);

  private final Clock clock;
  private final MetricPublisher metricPublisher;
  private final Tags initialTags;
  private final TagsGenerator<Throwable> defaultTagsGeneratorForThrowable;
  private final TagsGeneratorRegistry tagsGeneratorRegistry;
  private final Boolean closeAndOpenOnlyForInitial;
  private final ThreadLocal<MetricsImpl> metricsImplThreadLocal;

  private MetricFactory(final Builder builder) {
    this.clock = builder.clock;
    this.metricPublisher = builder.metricPublisher;
    this.initialTags = builder.tags;
    this.defaultTagsGeneratorForThrowable = builder.defaultTagsGeneratorForThrowable;
    this.tagsGeneratorRegistry = builder.tagsGeneratorRegistry;
    this.closeAndOpenOnlyForInitial = builder.closeAndOpenOnlyForInitial;
    this.metricsImplThreadLocal = new ThreadLocal<>();
    LOGGER.info("MetricFactory({},{},{},{},{})",
        clock, metricPublisher, initialTags, defaultTagsGeneratorForThrowable, tagsGeneratorRegistry);
  }

  public static Builder builder() {
    return Builder.builder();
  }

  /**
   * Returns this thread's instance. Note, you should use withMetrics if you can, as it manages the
   * count.
   *
   * @return the metrics
   */
  public Metrics metrics() {
    return metricsImplThreadLocal.get();
  }

  private MetricsImpl createMetrics(final Tags tags) {
    return new MetricsImpl(clock, metricPublisher, defaultTagsGeneratorForThrowable, tagsGeneratorRegistry, tags);
  }

  /**
   * With metrics r.
   *
   * @param <R>      the type parameter
   * @param function the function
   * @return the r
   */
  public <R> R withMetrics(final Function<Metrics, R> function) {
    final MetricsImpl oldMetrics = metricsImplThreadLocal.get();
    final Tags oldTags = oldMetrics == null ? initialTags : oldMetrics.getTags();
    MetricsImpl metrics = null;
    try {
      metrics = createMetrics(Tags.of(oldTags));
      metricsImplThreadLocal.set(metrics);
      if (!closeAndOpenOnlyForInitial || oldMetrics == null) {
        metrics.open();
      }
      return function.apply(metrics);
    } finally {
      if (!closeAndOpenOnlyForInitial || oldMetrics == null) {
        if (metrics != null) {
          metrics.close();
        } else {
          LOGGER.error("[BUG] Metrics was not created, unable to close");
        }
      }
      metricsImplThreadLocal.set(oldMetrics);
    }
  }

  /**
   * The type Builder.
   * This builder can be used without any arguments to build an empty factory. It will
   * use a real metrics implementation, but no publisher.
   */
  public static class Builder {

    private static final Logger LOGGER = LoggerFactory.getLogger(Builder.class);

    private final Tags tags = Tags.empty();
    private Clock clock = Clock.systemUTC();
    private MetricPublisher metricPublisher = new NullMetricsPublisher();
    private TagsGenerator<Throwable> defaultTagsGeneratorForThrowable;
    private TagsGeneratorRegistry tagsGeneratorRegistry = new TagsGeneratorRegistry();
    private Boolean closeAndOpenOnlyForInitial = true;

    private Builder() {
    }

    /**
     * Builder builder.
     *
     * @return the builder
     */
    public static Builder builder() {
      LOGGER.info("MetricFactory.Builder()");
      return new Builder();
    }

    /**
     * With clock builder.
     *
     * @param closeAndOpenOnlyForInitial the clock
     * @return the builder
     */
    public Builder withCloseAndOpenOnlyForInitial(final Boolean closeAndOpenOnlyForInitial) {
      LOGGER.info("withCloseAndOpenOnlyForInitial({})", closeAndOpenOnlyForInitial);
      this.closeAndOpenOnlyForInitial = closeAndOpenOnlyForInitial;
      return this;
    }

    /**
     * With clock builder.
     *
     * @param clock the clock
     * @return the builder
     */
    public Builder withClock(final Clock clock) {
      LOGGER.info("withClock({})", clock);
      this.clock = clock;
      return this;
    }

    /**
     * With metric publisher builder.
     *
     * @param metricPublisher the metric publisher
     * @return the builder
     */
    public Builder withMetricPublisher(final MetricPublisher metricPublisher) {
      LOGGER.info("withMetricPublisher({})", metricPublisher);
      this.metricPublisher = metricPublisher;
      return this;
    }


    /**
     * With tags builder.
     *
     * @param tags the tags
     * @return the builder
     */
    public Builder withTags(final Tags tags) {
      LOGGER.info("withTags({})", tags);
      this.tags.add(tags);
      return this;
    }

    /**
     * With tags builder.
     *
     * @param tags the tags
     * @return the builder
     */
    public Builder withTags(final String... tags) {
      LOGGER.info("withTags({})", (Object) tags);
      this.tags.add(tags);
      return this;
    }

    /**
     * With default tags generator for throwable builder.
     *
     * @param defaultTagsGeneratorForThrowable the default tags generator for throwable
     * @return the builder
     */
    public Builder withDefaultTagsGeneratorForThrowable(final TagsGenerator<Throwable> defaultTagsGeneratorForThrowable) {
      LOGGER.info("withDefaultTagsGeneratorForThrowable({})", defaultTagsGeneratorForThrowable);
      this.defaultTagsGeneratorForThrowable = defaultTagsGeneratorForThrowable;
      return this;
    }

    /**
     * With tags generator registry builder.
     *
     * @param tagsGeneratorRegistry the tags generator registry
     * @return the builder
     */
    public Builder withTagsGeneratorRegistry(final TagsGeneratorRegistry tagsGeneratorRegistry) {
      LOGGER.info("withTagsGeneratorRegistry({})", tagsGeneratorRegistry);
      this.tagsGeneratorRegistry = tagsGeneratorRegistry;
      return this;
    }

    /**
     * With tags generator builder.
     *
     * @param <R>           the type parameter
     * @param clazz         the clazz
     * @param tagsGenerator the tags generator
     * @return the builder
     */
    public <R> Builder withTagsGenerator(final Class<R> clazz, final TagsGenerator<R> tagsGenerator) {
      LOGGER.info("withTagsGenerator({},{})", clazz, tagsGenerator);
      tagsGeneratorRegistry.register(clazz, tagsGenerator);
      return this;
    }

    /**
     * Build metric factory.
     *
     * @return the metric factory
     */
    public MetricFactory build() {
      LOGGER.info("build()");
      return new MetricFactory(this);
    }

  }

}