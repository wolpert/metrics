package com.codeheadsystems.metrics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.codeheadsystems.metrics.helper.TagsGeneratorRegistry;
import com.codeheadsystems.metrics.impl.MetricPublisher;
import java.time.Clock;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MetricFactoryTest {

  @Mock private Clock clock;
  @Mock private MetricPublisher metricPublisher;
  @Mock private Tags tags;
  @Mock private TagsGenerator<Throwable> defaultTagsGeneratorForThrowable;
  @Mock private TagsGeneratorRegistry tagsGeneratorRegistry;

  @Test
  void testDefault() {
    final MetricFactory metricFactory = MetricFactory.builder().build();
    final Object result = metricFactory.withMetrics(metrics -> metrics.time("test", () -> "result"));
    assertThat(result).isEqualTo("result");
  }

  @Test
  void testBuilt() throws Exception {
    final MetricFactory metricFactory = MetricFactory.builder()
        .withClock(clock)
        .withMetricPublisher(metricPublisher)
        .build();
    final Object result = metricFactory.withMetrics(metrics -> metrics.time("test", () -> "result"));
    assertThat(result).isEqualTo("result");
    verify(clock, times(2)).millis();
    verify(metricPublisher).time("test", Duration.ofMillis(0), Tags.empty());
    verify(metricPublisher).close();
  }

}