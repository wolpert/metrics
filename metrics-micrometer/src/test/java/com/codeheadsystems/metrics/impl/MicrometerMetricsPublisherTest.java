package com.codeheadsystems.metrics.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codahale.metrics.MetricRegistry;
import com.codeheadsystems.metrics.Tags;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.time.Clock;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MicrometerMetricsPublisherTest {

  private static final Duration duration = Duration.ofSeconds(1);
  private static final String PREFIX = "prefix";

  @Mock private MetricRegistry metricRegistry;
  @Mock private MeterRegistry meterRegistry;
  @Mock private Clock clock;
  @Mock private Counter counter;
  @Mock private Timer timer;

  private MicrometerMetricsPublisher publisher;

  @Test
  void constructor_MeterRegistry_count() {
    publisher = new MicrometerMetricsPublisher(meterRegistry);
    when(meterRegistry.counter("test", io.micrometer.core.instrument.Tags.empty())).thenReturn(counter);

    publisher.increment("test", 1L, Tags.empty());
    verify(counter).increment(1L);
  }

  @Test
  void constructor_MeterRegistry_time() {
    publisher = new MicrometerMetricsPublisher(meterRegistry);
    when(meterRegistry.timer("test", io.micrometer.core.instrument.Tags.empty())).thenReturn(timer);

    publisher.time("test", duration, Tags.empty());
    verify(timer).record(duration);
  }

}