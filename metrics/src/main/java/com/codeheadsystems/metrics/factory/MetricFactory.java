package com.codeheadsystems.metrics.factory;

import com.codeheadsystems.metrics.Metrics;
import com.codeheadsystems.metrics.Tags;
import com.codeheadsystems.metrics.TagsGenerator;
import com.codeheadsystems.metrics.TagsSupplier;
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
  private final TagsSupplier tagsSupplier;
  private final TagsGenerator<Throwable> defaultTagsGeneratorForThrowable;
  private final TagsGeneratorRegistry tagsGeneratorRegistry;

  private final ThreadLocal<MetricsImpl> metricsImplThreadLocal;

  private MetricFactory(final Builder builder) {
    this.clock = builder.clock;
    this.metricPublisher = builder.metricPublisher;
    final Tags tags = builder.tags;
    this.tagsSupplier = () -> tags;
    this.defaultTagsGeneratorForThrowable = builder.defaultTagsGeneratorForThrowable;
    this.tagsGeneratorRegistry = builder.tagsGeneratorRegistry;
    this.metricsImplThreadLocal = ThreadLocal.withInitial(this::newInstance);
    LOGGER.info("MetricFactory({},{},{},{},{})",
        clock, metricPublisher, tagsSupplier, defaultTagsGeneratorForThrowable, tagsGeneratorRegistry);
  }

  private MetricsImpl newInstance() {
    return new MetricsImpl(clock, metricPublisher, tagsSupplier, defaultTagsGeneratorForThrowable, tagsGeneratorRegistry);
  }

  /**
   * Reset, used as a way to clear out the metrics.
   */
  public void reset() {
    metricsImplThreadLocal.get().reset();
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

  /**
   * With metrics r.
   *
   * @param <R>      the type parameter
   * @param function the function
   * @return the r
   */
  public <R> R withMetrics(Function<Metrics, R> function) {
    final MetricsImpl metrics = metricsImplThreadLocal.get();
    try {
      return function.apply(metrics);
    } finally {
      try {
        metrics.close();
      } catch (Throwable e) {
        LOGGER.warn("Metrics was unable to close", e);
      }
    }
  }

  /**
   * The type Builder.
   */
  public static class Builder {

    private final Tags tags = Tags.empty();
    private Clock clock = Clock.systemUTC();
    private MetricPublisher metricPublisher = new NullMetricsPublisher();
    private TagsGenerator<Throwable> defaultTagsGeneratorForThrowable;
    private TagsGeneratorRegistry tagsGeneratorRegistry = new TagsGeneratorRegistry();

    /**
     * Builder builder.
     *
     * @return the builder
     */
    public static Builder builder() {
      return new Builder();
    }

    /**
     * With clock builder.
     *
     * @param clock the clock
     * @return the builder
     */
    public Builder withClock(Clock clock) {
      this.clock = clock;
      return this;
    }

    /**
     * With metric publisher builder.
     *
     * @param metricPublisher the metric publisher
     * @return the builder
     */
    public Builder withMetricPublisher(MetricPublisher metricPublisher) {
      this.metricPublisher = metricPublisher;
      return this;
    }


    /**
     * With tags builder.
     *
     * @param tags the tags
     * @return the builder
     */
    public Builder withTags(Tags tags) {
      this.tags.add(tags);
      return this;
    }

    /**
     * With tags builder.
     *
     * @param tags the tags
     * @return the builder
     */
    public Builder withTags(String... tags) {
      this.tags.add(tags);
      return this;
    }

    /**
     * With default tags generator for throwable builder.
     *
     * @param defaultTagsGeneratorForThrowable the default tags generator for throwable
     * @return the builder
     */
    public Builder withDefaultTagsGeneratorForThrowable(TagsGenerator<Throwable> defaultTagsGeneratorForThrowable) {
      this.defaultTagsGeneratorForThrowable = defaultTagsGeneratorForThrowable;
      return this;
    }

    /**
     * With tags generator registry builder.
     *
     * @param tagsGeneratorRegistry the tags generator registry
     * @return the builder
     */
    public Builder withTagsGeneratorRegistry(TagsGeneratorRegistry tagsGeneratorRegistry) {
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
    public <R> Builder withTagsGenerator(Class<R> clazz, TagsGenerator<R> tagsGenerator) {
      tagsGeneratorRegistry.register(clazz, tagsGenerator);
      return this;
    }

    /**
     * Build metric factory.
     *
     * @return the metric factory
     */
    public MetricFactory build() {
      return new MetricFactory(this);
    }

  }

}