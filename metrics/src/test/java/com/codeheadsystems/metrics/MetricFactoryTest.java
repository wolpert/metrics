package com.codeheadsystems.metrics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.codeheadsystems.metrics.helper.TagsGeneratorRegistry;
import com.codeheadsystems.metrics.impl.MetricPublisher;
import com.codeheadsystems.metrics.impl.MetricsImpl;
import java.time.Clock;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MetricFactoryTest {

  private static final Tags BASE_TAGS = new Tags("a", "1", "b", "2");
  private static final Tags ADDED_TAGS = new Tags("b", "3", "c", "4");
  private static final Tags COMBINED_TAGS = new Tags("a", "1", "b", "3", "c", "4");
  private static final Tags THIRD_TAGS = new Tags("d", "5");

  @Mock private Clock clock;
  @Mock private MetricPublisher metricPublisher;
  @Mock private Tags tags;
  @Mock private TagsGenerator<Throwable> defaultTagsGeneratorForThrowable;
  @Mock private TagsGeneratorRegistry tagsGeneratorRegistry;

  @Test
  void testDefault() {
    final MetricFactory metricFactory = MetricFactory.builder().build();
    final Object result = metricFactory.with(metrics -> metrics.time("test", () -> "result"));
    assertThat(result).isEqualTo("result");
  }

  @Test
  void testBuilt() throws Exception {
    final MetricFactory metricFactory = MetricFactory.builder()
        .withClock(clock)
        .withMetricPublisher(metricPublisher)
        .build();
    final Object result = metricFactory.with(metrics -> metrics.time("test", () -> "result"));
    assertThat(result).isEqualTo("result");
    verify(clock, times(3)).millis();
    verify(metricPublisher).time("test", Duration.ofMillis(0), Tags.empty());
    verify(metricPublisher).close();
  }

  @Test
  void testWith() throws Exception {
    final MetricFactory metricFactory = MetricFactory.builder()
        .withMetricPublisher(metricPublisher)
        .withTags(BASE_TAGS).build();
    final Boolean result = metricFactory.with(metrics ->
        metrics.time("test", () -> {
          metrics.increment("test", 1L);
          boolean testResult = ((MetricsImpl) metrics).getTags().equals(BASE_TAGS);
          metrics.and(ADDED_TAGS);
          metrics.increment("test", 2L);
          testResult = testResult && ((MetricsImpl) metricFactory.metrics()).getTags().equals(COMBINED_TAGS);
          return testResult;
        }));
    assertThat(result).isTrue();
    verify(metricPublisher).open();
    verify(metricPublisher).increment("test", 1L, BASE_TAGS);
    verify(metricPublisher).increment("test", 2L, COMBINED_TAGS);
    verify(metricPublisher).time(eq("test"), any(), eq(COMBINED_TAGS));
    verify(metricPublisher).close();
    final Boolean secondResult = metricFactory.with(metrics -> metrics.time("test", () -> {
      boolean testResult = ((MetricsImpl) metrics).getTags().equals(BASE_TAGS);
      metrics.and(ADDED_TAGS);
      testResult = testResult && ((MetricsImpl) metricFactory.metrics()).getTags().equals(COMBINED_TAGS);
      return testResult;
    }));
    assertThat(secondResult).isTrue();
  }

  @Test
  void testWithNester_defaultBehavior() throws Exception {
    final MetricFactory metricFactory = MetricFactory.builder()
        .withTags(Tags.empty())
        .withMetricPublisher(metricPublisher).build();
    testNestedMetrics(metricFactory, 1);
  }

  @Test
  void testWithNester_openCloseOnlyInitial() throws Exception {
    final MetricFactory metricFactory = MetricFactory.builder()
        .withTags(Tags.empty())
        .withCloseAndOpenOnlyForInitial(true)
        .withMetricPublisher(metricPublisher).build();
    testNestedMetrics(metricFactory, 1);
  }

  @Test
  void testWithNester_openCloseAll() throws Exception {
    final MetricFactory metricFactory = MetricFactory.builder()
        .withTags(Tags.empty())
        .withCloseAndOpenOnlyForInitial(false)
        .withMetricPublisher(metricPublisher).build();
    testNestedMetrics(metricFactory, 2);
  }

  void testNestedMetrics(MetricFactory metricFactory, int expectedTimes) throws Exception {
    metricFactory.with(metrics -> { // outer
      metrics.time("outer", () -> {
        metricFactory.with(innerMetrics -> { // inner
          innerMetrics.increment("inner", 2L, ADDED_TAGS);
          return null;
        });
        return null;
      }, BASE_TAGS);
      return null;
    });
    verify(metricPublisher, times(expectedTimes)).open();
    verify(metricPublisher).increment("inner", 2L, ADDED_TAGS);
    verify(metricPublisher).time(eq("outer"), any(), eq(BASE_TAGS));
    verify(metricPublisher, times(expectedTimes)).close();
  }
}