package com.codeheadsystems.metrics.factory;

import com.codeheadsystems.metrics.Metrics;
import com.codeheadsystems.metrics.TagsGenerator;
import com.codeheadsystems.metrics.TagsSupplier;
import com.codeheadsystems.metrics.helper.TagsGeneratorRegistry;
import com.codeheadsystems.metrics.impl.MetricPublisher;
import com.codeheadsystems.metrics.impl.MetricsImpl;
import java.time.Clock;
import java.util.Optional;
import java.util.function.Function;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides a way to manage metrics from threads.
 */
@Singleton
public class MetricFactory {

  private static final Logger LOGGER = LoggerFactory.getLogger(MetricFactory.class);

  private final Clock clock;
  private final MetricPublisher metricPublisher;
  private final TagsSupplier tagsSupplier;
  private final TagsGenerator<Throwable> defaultTagsGeneratorForThrowable;
  private final TagsGeneratorRegistry tagsGeneratorRegistry;

  private final ThreadLocal<MetricsImpl> metricsImplThreadLocal;

  /**
   * Default constructor.
   *
   * @param clock                            the clock to use.
   * @param metricPublisher                  the metric implementation.
   * @param tagsSupplier                     the tags supplier.
   * @param defaultTagsGeneratorForThrowable to use for exceptions, optional.
   * @param tagsGeneratorRegistry            to help with tags.
   */
  @Inject
  public MetricFactory(final Clock clock,
                       final MetricPublisher metricPublisher,
                       final TagsSupplier tagsSupplier,
                       final Optional<TagsGenerator<Throwable>> defaultTagsGeneratorForThrowable,
                       final TagsGeneratorRegistry tagsGeneratorRegistry) {
    LOGGER.info("MetricFactory({},{},{},{},{})",
        clock, metricPublisher, tagsSupplier, defaultTagsGeneratorForThrowable, tagsGeneratorRegistry);
    this.clock = clock;
    this.metricPublisher = metricPublisher;
    this.tagsSupplier = tagsSupplier;
    this.defaultTagsGeneratorForThrowable = defaultTagsGeneratorForThrowable.orElse(null);
    this.tagsGeneratorRegistry = tagsGeneratorRegistry;
    this.metricsImplThreadLocal = ThreadLocal.withInitial(this::newInstance);
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

}