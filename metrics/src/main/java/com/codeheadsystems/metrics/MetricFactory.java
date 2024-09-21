package com.codeheadsystems.metrics;

import com.codeheadsystems.metrics.helper.TagsGeneratorRegistry;
import com.codeheadsystems.metrics.impl.MetricPublisher;
import com.codeheadsystems.metrics.impl.MetricsImpl;
import com.codeheadsystems.metrics.impl.NullMetricsImpl;
import com.codeheadsystems.metrics.impl.NullMetricsPublisher;
import java.time.Clock;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides a way to manage metrics from threads.
 */
public class MetricFactory implements Metrics {

  private static final Logger LOGGER = LoggerFactory.getLogger(MetricFactory.class);
  private static final NullMetricsImpl NULL_METRICS = new NullMetricsImpl();

  private final Clock clock;
  private final MetricPublisher metricPublisher;
  private final Tags initialTags;
  private final TagsGenerator<Throwable> defaultTagsGeneratorForThrowable;
  private final TagsGeneratorRegistry tagsGeneratorRegistry;
  private final Boolean closeAndOpenOnlyForInitial;
  private final ThreadLocal<MetricsImpl> metricsImplThreadLocal;
  private final Function<String, String> metricsName;

  private MetricFactory(final Builder builder) {
    this.clock = builder.clock;
    this.metricPublisher = builder.metricPublisher;
    this.initialTags = builder.tags;
    this.defaultTagsGeneratorForThrowable = builder.defaultTagsGeneratorForThrowable;
    this.tagsGeneratorRegistry = builder.tagsGeneratorRegistry;
    this.closeAndOpenOnlyForInitial = builder.closeAndOpenOnlyForInitial;
    this.metricsImplThreadLocal = new ThreadLocal<>();
    this.metricsName = builder.prefix == null ? Function.identity() : s -> builder.prefix + s;
    LOGGER.info("MetricFactory({},{},{},{},{})",
        clock, metricPublisher, initialTags, defaultTagsGeneratorForThrowable, tagsGeneratorRegistry);
  }

  /**
   * Builder builder.
   *
   * @return the builder
   */
  public static Builder builder() {
    return Builder.builder();
  }

  /**
   * Returns this thread's instance.HOWEVER, if there is no instance, will return a null metrics to not blow up. Will
   * create a log message under info that a null was used.
   *
   * @return the metrics
   */
  public Metrics metrics() {
    final MetricsImpl metrics = metricsImplThreadLocal.get();
    if (metrics == null) {
      LOGGER.info("No metrics found, returning a null metrics");
      return NULL_METRICS;
    }
    return metrics;
  }

  private MetricsImpl createMetrics(final Tags tags) {
    return new MetricsImpl(clock, metricPublisher, defaultTagsGeneratorForThrowable, tagsGeneratorRegistry, tags, metricsName);
  }

  /**
   * Enable the metrics context. This sets up a new metrics instance. It is used internally to manage
   * the metric from within the with() method. If you use it, it is vital you close it. It is not thread safe.
   *
   * @return the metrics context
   */
  public MetricsContext enableMetricsContext() {
    final MetricsImpl oldMetrics = metricsImplThreadLocal.get();
    final Tags oldTags = oldMetrics == null ? initialTags : oldMetrics.getTags();
    final MetricsImpl metrics = createMetrics(Tags.of(oldTags));
    metricsImplThreadLocal.set(metrics);
    if (!closeAndOpenOnlyForInitial || oldMetrics == null) {
      metrics.open();
    }
    return new MetricsContext(oldMetrics, metrics);
  }

  /**
   * Disables the metrics context. This closes up a metrics resetting the current thread context.
   * It is used internally to manage
   * the metric from within the with() method. If you use it, it is vital you close it. It is not thread safe.
   *
   * @param metricsContext the metrics context
   */
  public void disableMetricsContext(final MetricsContext metricsContext) {
    if (!closeAndOpenOnlyForInitial || metricsContext.oldMetrics == null) {
      metricsContext.currentMetrics.close();
    }
    if (metricsContext.oldMetrics == null) {
      metricsImplThreadLocal.remove();
    } else {
      metricsImplThreadLocal.set(metricsContext.oldMetrics);
    }
  }

  /**
   * With metrics r.
   *
   * @param <R>      the type parameter
   * @param function the function
   * @return the r
   */
  public <R> R with(final Function<Metrics, R> function) {
    final MetricsContext metricsContext = enableMetricsContext();
    try {
      return function.apply(metricsContext.currentMetrics);
    } finally {
      disableMetricsContext(metricsContext);
    }
  }

  @Override
  public void increment(final String metricName, final long value, final Tags tags) {
    if (metricsImplThreadLocal.get() != null) {
      metrics().increment(metricName, value, tags);
    } else {
      with(metrics -> {
        metrics.increment(metricName, value, tags);
        return null;
      });
    }
  }

  @Override
  public Tags and(final Tags overrideTags) {
    if (metricsImplThreadLocal.get() != null) {
      return metrics().and(overrideTags);
    } else {
      return Tags.empty();
    }
  }

  @Override
  public Tags and(final String... overrideTags) {
    if (metricsImplThreadLocal.get() != null) {
      return metrics().and(overrideTags);
    } else {
      return Tags.empty();
    }
  }

  @Override
  public <R, E extends Exception> R time(final String metricName,
                                         final CheckedSupplier<R, E> supplier,
                                         final TagsGenerator<R> tagsGeneratorForResult,
                                         final TagsGenerator<Throwable> tagsGeneratorForThrowable,
                                         final Tags tags) throws E {
    if (metricsImplThreadLocal.get() != null) {
      return metrics().time(metricName, supplier, tagsGeneratorForResult, tagsGeneratorForThrowable, tags);
    } else {
      final AtomicReference<E> atomicReference = new AtomicReference<>();
      final R r = with(metrics -> {
        try {
          return metrics.time(metricName, supplier, tagsGeneratorForResult, tagsGeneratorForThrowable, tags);
        } catch (RuntimeException re) {
          throw re; // Don't wrap runtime exceptions
        } catch (Exception e) {
          atomicReference.set((E) e); // it cannot really be anything else.
          return null;
        }
      });
      if (atomicReference.get() != null) {
        throw atomicReference.get();
      }
      return r;
    }
  }

  @Override
  public void publishTime(final String metricName, final Duration duration, final Tags tags) {
    metrics().publishTime(metricName, duration, tags);
  }

  /**
   * Used to store the metrics for the current thread.
   */
  public static class MetricsContext {

    private final MetricsImpl oldMetrics;
    private final MetricsImpl currentMetrics;
    private final long start;

    /**
     * Instantiates a new Metrics context.
     *
     * @param oldMetrics     the old metrics
     * @param currentMetrics the current metrics
     */
    public MetricsContext(final MetricsImpl oldMetrics,
                          final MetricsImpl currentMetrics) {
      this.oldMetrics = oldMetrics;
      this.currentMetrics = currentMetrics;
      this.start = currentMetrics.clock().millis();
    }

    /**
     * Duration duration.
     *
     * @return the duration
     */
    public Duration duration() {
      return Duration.ofMillis(currentMetrics.clock().millis() - start);
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
    private String prefix = null;

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
     * With prefix builder.
     *
     * @param prefix the prefix
     * @return the builder
     */
    public Builder withPrefix(final String prefix) {
      LOGGER.info("withPrefix({})", prefix);
      this.prefix = prefix;
      return this;
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