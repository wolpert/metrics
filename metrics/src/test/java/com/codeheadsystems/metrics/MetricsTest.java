package com.codeheadsystems.metrics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codeheadsystems.metrics.impl.MetricImpl;
import java.time.Clock;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MetricsTest {

  private static final String METRIC_NAME = "name";
  private static final Tags DEFAULT_TAGS = new Tags("a", "1", "b", "2");
  private static final Tags OVERRIDE_TAGS = new Tags("b", "3", "c", "4");
  private static final String[] OVERRIDE_ARRAY = new String[]{"b", "3", "c", "4"};
  private static final Tags COMBINED_TAGS = new Tags("a", "1", "b", "3", "c", "4");
  private static final Object RESULT = new Object();
  private static final Tags ERROR_TAGS = new Tags("error", "true");
  private static final Tags RESULT_TAGS = new Tags("result", "true");

  @Mock private MetricImpl metricImpl;
  @Mock private Clock clock;

  private Metrics metrics;

  @BeforeEach
  void setUp() {
    metrics = new Metrics(clock, metricImpl, () -> DEFAULT_TAGS);
  }

  @Test
  void testAnd() {
    metrics.and(OVERRIDE_TAGS);
    assertThat(metrics.getTags())
        .isEqualTo(COMBINED_TAGS);
  }

  @Test
  void close() throws Exception {
    metrics.and(OVERRIDE_TAGS);
    metrics.close();
    assertThat(metrics.getTags())
        .isEqualTo(DEFAULT_TAGS);
    verify(metricImpl).close();
  }

  @Test
  void addArray() {
    metrics.and(OVERRIDE_ARRAY);
    assertThat(metrics.getTags())
        .isEqualTo(COMBINED_TAGS);
  }

  @Test
  void testIncrement() {
    metrics.increment(METRIC_NAME, 1L, OVERRIDE_ARRAY);
    verify(metricImpl)
        .increment(METRIC_NAME, 1L, COMBINED_TAGS);
    assertThat(metrics.getTags())
        .isEqualTo(DEFAULT_TAGS);
  }

  @Test
  void time_base() {
    when(clock.millis()).thenReturn(200L).thenReturn(300L);
    final Object result = metrics.time(METRIC_NAME, this::testMethod);

    verify(metricImpl)
        .time(METRIC_NAME, Duration.ofMillis(100), DEFAULT_TAGS);
    assertThat(result).isEqualTo(RESULT);
  }

  @Test
  void time_manualTags() {
    when(clock.millis()).thenReturn(200L).thenReturn(300L);
    final Object result = metrics.time(METRIC_NAME, this::testMethod, "something", "else");

    verify(metricImpl)
        .time(METRIC_NAME, Duration.ofMillis(100), DEFAULT_TAGS.from("something", "else"));
    assertThat(result).isEqualTo(RESULT);
  }

  @Test
  void time_moreManualTags() {
    when(clock.millis()).thenReturn(200L).thenReturn(300L);
    final Object result = metrics.time(METRIC_NAME, this::testMethod, "something", "else", "another", "tag");

    verify(metricImpl)
        .time(METRIC_NAME, Duration.ofMillis(100), DEFAULT_TAGS.from("something", "else", "another", "tag"));
    assertThat(result).isEqualTo(RESULT);
  }

  @Test
  void time_baseException() throws SomeException {
    when(clock.millis()).thenReturn(200L).thenReturn(300L);
    final Object result = metrics.time(METRIC_NAME, this::testMethodWithExceptionDefined);

    verify(metricImpl)
        .time(METRIC_NAME, Duration.ofMillis(100), DEFAULT_TAGS);
    assertThat(result).isEqualTo(RESULT);
  }

  @Test
  void time_baseWithException() throws SomeException {
    when(clock.millis()).thenReturn(200L).thenReturn(300L);
    assertThatExceptionOfType(SomeException.class)
        .isThrownBy(() -> metrics.time(METRIC_NAME, this::testMethodWithExceptionThrown));

    verify(metricImpl)
        .time(METRIC_NAME, Duration.ofMillis(100), DEFAULT_TAGS);
  }

  @Test
  void time_withTags() {
    when(clock.millis()).thenReturn(200L).thenReturn(300L);
    final Object result = metrics.time(METRIC_NAME, this::testMethod, OVERRIDE_ARRAY);

    verify(metricImpl)
        .time(METRIC_NAME, Duration.ofMillis(100), COMBINED_TAGS);
    assertThat(result).isEqualTo(RESULT);
  }

  @Test
  void time_baseException_withTags() throws SomeException {
    when(clock.millis()).thenReturn(200L).thenReturn(300L);
    final Object result = metrics.time(METRIC_NAME, this::testMethodWithExceptionDefined, OVERRIDE_ARRAY);

    verify(metricImpl)
        .time(METRIC_NAME, Duration.ofMillis(100), COMBINED_TAGS);
    assertThat(result).isEqualTo(RESULT);
  }

  @Test
  void time_baseWithException_withTags() throws SomeException {
    when(clock.millis()).thenReturn(200L).thenReturn(300L);
    assertThatExceptionOfType(SomeException.class)
        .isThrownBy(() -> metrics.time(METRIC_NAME, this::testMethodWithExceptionThrown, OVERRIDE_ARRAY));

    verify(metricImpl)
        .time(METRIC_NAME, Duration.ofMillis(100), COMBINED_TAGS);
  }

  Object testMethod() {
    return RESULT;
  }

  Object testMethodWithExceptionThrown() throws SomeException {
    throw new SomeException();
  }

  Object testMethodWithExceptionDefined() throws SomeException {
    return RESULT;
  }


  static class SomeException extends Exception {
    public SomeException() {
      super("oops");
    }
  }

}