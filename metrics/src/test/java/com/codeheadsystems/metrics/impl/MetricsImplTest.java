package com.codeheadsystems.metrics.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codeheadsystems.metrics.Tags;
import com.codeheadsystems.metrics.TagsGenerator;
import com.codeheadsystems.metrics.helper.TagsGeneratorRegistry;
import java.time.Clock;
import java.time.Duration;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MetricsImplTest {

  private static final String METRIC_NAME = "name";
  private static final Tags DEFAULT_TAGS = new Tags("a", "1", "b", "2");
  private static final Tags OVERRIDE_TAGS = new Tags("b", "3", "c", "4");
  private static final String[] OVERRIDE_ARRAY = new String[]{"b", "3", "c", "4"};
  private static final Tags COMBINED_TAGS = new Tags("a", "1", "b", "3", "c", "4");
  private static final Object RESULT = new Object();
  private static final Tags ERROR_TAGS = new Tags("error", "true");
  private static final Tags RESULT_TAGS = new Tags("result", "true");
  private static final TagsGenerator<Object> TAGS_GENERATOR_RESULT = object -> RESULT_TAGS;
  private static final TagsGenerator<Throwable> TAGS_GENERATOR_ERROR = object -> ERROR_TAGS;
  private static final Function<String, String> metricsName = Function.identity();

  @Mock private MetricPublisher metricPublisher;
  @Mock private Clock clock;
  @Mock private TagsGeneratorRegistry tagsGeneratorRegistry;

  private MetricsImpl metricsImpl;

  @BeforeEach
  void setUp() {
    metricsImpl = new MetricsImpl(
        clock,
        metricPublisher,
        null,
        null,
        DEFAULT_TAGS, metricsName);
  }

  @Test
  void testAnd() {
    metricsImpl.and(OVERRIDE_TAGS);
    assertThat(metricsImpl.getTags())
        .isEqualTo(COMBINED_TAGS);
  }

  @Test
  void close() throws Exception {
    metricsImpl.and(OVERRIDE_TAGS);
    metricsImpl.close();
    assertThat(metricsImpl.getTags())
        .isEqualTo(DEFAULT_TAGS);
    verify(metricPublisher).close();
  }

  @Test
  void addArray() {
    metricsImpl.and(OVERRIDE_ARRAY);
    assertThat(metricsImpl.getTags())
        .isEqualTo(COMBINED_TAGS);
  }

  @Test
  void testIncrement() {
    metricsImpl.increment(METRIC_NAME, 1L, OVERRIDE_ARRAY);
    verify(metricPublisher)
        .increment(METRIC_NAME, 1L, COMBINED_TAGS);
    assertThat(metricsImpl.getTags())
        .isEqualTo(DEFAULT_TAGS);
  }

  @Test
  void testPublishTime() {
    metricsImpl.publishTime(METRIC_NAME, Duration.ofMillis(100), COMBINED_TAGS);
    verify(metricPublisher)
        .time(METRIC_NAME, Duration.ofMillis(100), COMBINED_TAGS);
  }

  @Test
  void time_base() {
    when(clock.millis()).thenReturn(200L).thenReturn(300L);
    final Object result = metricsImpl.time(METRIC_NAME, this::testMethod);

    verify(metricPublisher)
        .time(METRIC_NAME, Duration.ofMillis(100), DEFAULT_TAGS);
    assertThat(result).isEqualTo(RESULT);
  }

  @Test
  void time_base_hardcodedMethod() {
    when(clock.millis()).thenReturn(200L).thenReturn(300L);
    final Object result = metricsImpl.time(METRIC_NAME, () -> RESULT);

    verify(metricPublisher)
        .time(METRIC_NAME, Duration.ofMillis(100), DEFAULT_TAGS);
    assertThat(result).isEqualTo(RESULT);
  }

  @Test
  void time_manualTags() {
    when(clock.millis()).thenReturn(200L).thenReturn(300L);
    final Object result = metricsImpl.time(METRIC_NAME, this::testMethod, "something", "else");

    verify(metricPublisher)
        .time(METRIC_NAME, Duration.ofMillis(100), DEFAULT_TAGS.from("something", "else"));
    assertThat(result).isEqualTo(RESULT);
  }

  @Test
  void time_moreManualTags() {
    when(clock.millis()).thenReturn(200L).thenReturn(300L);
    final Object result = metricsImpl.time(METRIC_NAME, this::testMethod, "something", "else", "another", "tag");

    verify(metricPublisher)
        .time(METRIC_NAME, Duration.ofMillis(100), DEFAULT_TAGS.from("something", "else", "another", "tag"));
    assertThat(result).isEqualTo(RESULT);
  }

  @Test
  void time_baseException() throws SomeException {
    when(clock.millis()).thenReturn(200L).thenReturn(300L);
    final Object result = metricsImpl.time(METRIC_NAME, this::testMethodWithExceptionDefined);

    verify(metricPublisher)
        .time(METRIC_NAME, Duration.ofMillis(100), DEFAULT_TAGS);
    assertThat(result).isEqualTo(RESULT);
  }

  @Test
  void time_baseWithException() throws SomeException {
    when(clock.millis()).thenReturn(200L).thenReturn(300L);
    assertThatExceptionOfType(SomeException.class)
        .isThrownBy(() -> metricsImpl.time(METRIC_NAME, this::testMethodWithExceptionThrown));

    verify(metricPublisher)
        .time(METRIC_NAME, Duration.ofMillis(100), DEFAULT_TAGS);
  }

  @Test
  void time_baseWithException_defaultHandler() throws SomeException {
    metricsImpl = new MetricsImpl(clock, metricPublisher, TAGS_GENERATOR_ERROR, null, DEFAULT_TAGS, metricsName);
    when(clock.millis()).thenReturn(200L).thenReturn(300L);
    assertThatExceptionOfType(SomeException.class)
        .isThrownBy(() -> metricsImpl.time(METRIC_NAME, this::testMethodWithExceptionThrown));

    verify(metricPublisher)
        .time(METRIC_NAME, Duration.ofMillis(100), DEFAULT_TAGS.from(ERROR_TAGS));
  }

  @Test
  void time_withTags() {
    when(clock.millis()).thenReturn(200L).thenReturn(300L);
    final Object result = metricsImpl.time(METRIC_NAME, this::testMethod, OVERRIDE_ARRAY);

    verify(metricPublisher)
        .time(METRIC_NAME, Duration.ofMillis(100), COMBINED_TAGS);
    assertThat(result).isEqualTo(RESULT);
  }

  @Test
  void time_withTags_changedInMethod() {
    when(clock.millis()).thenReturn(200L).thenReturn(300L);
    final Tags tags = Tags.of(OVERRIDE_ARRAY);
    final Object result = metricsImpl.time(METRIC_NAME, () -> testMethodWithTags(tags, "added", "true"), tags);

    verify(metricPublisher)
        .time(METRIC_NAME, Duration.ofMillis(100), Tags.of(COMBINED_TAGS).add("added", "true"));
    assertThat(result).isEqualTo(RESULT);
  }

  @Test
  void time_baseException_withTags() throws SomeException {
    when(clock.millis()).thenReturn(200L).thenReturn(300L);
    final Object result = metricsImpl.time(METRIC_NAME, this::testMethodWithExceptionDefined, OVERRIDE_ARRAY);

    verify(metricPublisher)
        .time(METRIC_NAME, Duration.ofMillis(100), COMBINED_TAGS);
    assertThat(result).isEqualTo(RESULT);
  }

  @Test
  void time_baseWithException_withTags() throws SomeException {
    when(clock.millis()).thenReturn(200L).thenReturn(300L);
    assertThatExceptionOfType(SomeException.class)
        .isThrownBy(() -> metricsImpl.time(METRIC_NAME, this::testMethodWithExceptionThrown, OVERRIDE_ARRAY));

    verify(metricPublisher)
        .time(METRIC_NAME, Duration.ofMillis(100), COMBINED_TAGS);
  }

  @Test
  void time_baseException_withTagGenerator() throws SomeException {
    when(clock.millis()).thenReturn(200L).thenReturn(300L);
    final Object result = metricsImpl.time(METRIC_NAME, this::testMethodWithExceptionDefined, TAGS_GENERATOR_RESULT, TAGS_GENERATOR_ERROR);

    verify(metricPublisher)
        .time(METRIC_NAME, Duration.ofMillis(100), DEFAULT_TAGS.from(RESULT_TAGS));
    assertThat(result).isEqualTo(RESULT);
  }

  @Test
  void time_baseWithException_withGenerator() throws SomeException {
    when(clock.millis()).thenReturn(200L).thenReturn(300L);
    assertThatExceptionOfType(SomeException.class)
        .isThrownBy(() -> metricsImpl.time(METRIC_NAME, this::testMethodWithExceptionThrown, TAGS_GENERATOR_RESULT, TAGS_GENERATOR_ERROR));

    verify(metricPublisher)
        .time(METRIC_NAME, Duration.ofMillis(100), DEFAULT_TAGS.from(ERROR_TAGS));
  }

  @Test
  void time_baseException_withTagGenerator_andOverrideTags() throws SomeException {
    when(clock.millis()).thenReturn(200L).thenReturn(300L);
    final Object result = metricsImpl.time(METRIC_NAME, this::testMethodWithExceptionDefined, TAGS_GENERATOR_RESULT, TAGS_GENERATOR_ERROR, OVERRIDE_ARRAY);

    verify(metricPublisher)
        .time(METRIC_NAME, Duration.ofMillis(100), COMBINED_TAGS.from(RESULT_TAGS));
    assertThat(result).isEqualTo(RESULT);
  }

  @Test
  void time_baseWithException_withGenerator_andOverrideTags() throws SomeException {
    when(clock.millis()).thenReturn(200L).thenReturn(300L);
    assertThatExceptionOfType(SomeException.class)
        .isThrownBy(() -> metricsImpl.time(METRIC_NAME, this::testMethodWithExceptionThrown, TAGS_GENERATOR_RESULT, TAGS_GENERATOR_ERROR, OVERRIDE_ARRAY));

    verify(metricPublisher)
        .time(METRIC_NAME, Duration.ofMillis(100), COMBINED_TAGS.from(ERROR_TAGS));
  }

  Object testMethod() {
    return RESULT;
  }

  Object testMethodWithTags(Tags tags, String... newTags) {
    tags.add(newTags);
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