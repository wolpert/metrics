package com.codeheadsystems.metrics.impl;

import static org.assertj.core.api.Assertions.assertThat;

import ch.qos.logback.classic.ClassicConstants;
import com.codahale.metrics.MetricRegistry;
import io.micrometer.core.instrument.Clock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MicrometerMetricsPublisherTest {

  private MicrometerMetricsPublisher publisher;

  @BeforeEach
  void setUp() {
    publisher = new MicrometerMetricsPublisher(new MetricRegistry(), "prefix", Clock.SYSTEM);
  }

  @Test
  void testConstructor() {
    assertThat(publisher).isNotNull();
  }

}