package com.codeheadsystems.metrics.impl;

import static org.assertj.core.api.Assertions.assertThat;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.codahale.metrics.Timer;
import com.codeheadsystems.metrics.Tags;
import java.time.Duration;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

/**
 * Test with a registered reporter.
 */
public class MicrometerMetricsPublisherIntegTest {

  @Test
  void testFullTrip() throws Exception {
    final MetricRegistry metrics = new MetricRegistry();
    TestReporter reporter = new TestReporter(metrics);
    reporter.start(1, TimeUnit.SECONDS);
    try (MicrometerMetricsPublisher publisher = new MicrometerMetricsPublisher(metrics, "integ-test")) {
      publisher.increment("test", 1L, Tags.of("a", "1"));
      publisher.time("test", Duration.ofMillis(10), Tags.of("b", "2"));
    }
    reporter.report();
    assertThat(reporter.timers).containsKey("test.b.2");
    assertThat(reporter.meters).containsKey("test.a.1");
  }

  static class TestReporter extends ScheduledReporter {

    SortedMap<String, Counter> counters;
    SortedMap<String, Timer> timers;
    SortedMap<String, Meter> meters;

    protected TestReporter(final MetricRegistry registry) {
      super(registry, "test-reporter", MetricFilter.ALL, TimeUnit.SECONDS, TimeUnit.MILLISECONDS);
    }

    @Override
    public void report(final SortedMap<String, Gauge> gauges,
                       final SortedMap<String, Counter> counters,
                       final SortedMap<String, Histogram> histograms,
                       final SortedMap<String, Meter> meters,
                       final SortedMap<String, Timer> timers) {
      this.counters = counters;
      this.timers = timers;
      this.meters = meters;
    }
  }

}
