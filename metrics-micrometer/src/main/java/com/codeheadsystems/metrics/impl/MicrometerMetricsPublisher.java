package com.codeheadsystems.metrics.impl;

import com.codahale.metrics.MetricRegistry;
import com.codeheadsystems.metrics.Tags;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.dropwizard.DropwizardConfig;
import io.micrometer.core.instrument.dropwizard.DropwizardMeterRegistry;
import io.micrometer.core.instrument.util.HierarchicalNameMapper;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The type Micrometer metrics publisher.
 */
public class MicrometerMetricsPublisher implements MetricPublisher {

  private static final Logger log = LoggerFactory.getLogger(MicrometerMetricsPublisher.class);
  private final MeterRegistry meterRegistry;

  /**
   * Constructor.
   *
   * @param meterRegistry The meter registry to use.
   */
  public MicrometerMetricsPublisher(final MeterRegistry meterRegistry) {
    log.info("MicrometerMetricsPublisher({})", meterRegistry);
    this.meterRegistry = meterRegistry;
  }

  /**
   * Constructor to use if embedded within Dropwizard, and you don't want to create your own
   * meterRegistry..
   *
   * @param metricRegistry The dropwizard metric registry to use.
   * @param prefix         the prefix
   */
  public MicrometerMetricsPublisher(final MetricRegistry metricRegistry,
                                    final String prefix) {
    log.info("MicrometerMetricsPublisher({}, {}, {})", metricRegistry, prefix);
    final DropwizardConfig config = new DropwizardConfig() {
      @Override
      public String prefix() {
        return prefix;
      }

      @Override
      public String get(final String key) {
        return null;
      }
    };
    this.meterRegistry = new DropwizardMeterRegistry(config, metricRegistry, HierarchicalNameMapper.DEFAULT, Clock.SYSTEM) {
      @Override
      protected Double nullGaugeValue() {
        return null;
      }
    };
  }

  @Override
  public void increment(final String metricName, final long value, final Tags tags) {
    log.trace("increment({}, {}, {})", metricName, value, tags);
    meterRegistry.counter(metricName, convert(tags)).increment(value);
  }

  @Override
  public void time(final String metricName, final Duration duration, final Tags tags) {
    log.trace("time({}, {}, {})", metricName, duration, tags);
    meterRegistry.timer(metricName, convert(tags)).record(duration);
  }

  private io.micrometer.core.instrument.Tags convert(final Tags tags) {
    final List<Tag> list = tags.getTags().entrySet().stream()
        .map(entry -> Tag.of(entry.getKey(), entry.getValue()))
        .collect(Collectors.toList());
    return io.micrometer.core.instrument.Tags.of(list);
  }

}
