package com.codeheadsystems.metrics.declarative;

import static org.assertj.core.api.Assertions.assertThat;

import com.codeheadsystems.metrics.impl.NullMetricsImpl;
import org.junit.jupiter.api.Test;

/**
 * The type Declarative metrics manager test.
 */
class DeclarativeMetricsManagerTest {

  /**
   * Test metrics is never null.
   */
  @Test
  void testMetricsIsNeverNull() {
    assertThat(DeclarativeMetricsManager.metrics()).isNotNull();
  }

  /**
   * Metrics factory is created.
   */
  @Test
  void metricsFactoryIsCreated() {
    SampleObject sampleObject = new SampleObject();
    sampleObject.metricsFactory();
    assertThat(DeclarativeMetricsManager.metrics())
        .isNotNull()
        .isNotInstanceOf(NullMetricsImpl.class);
  }

  /**
   * Call some method.
   */
  @Test
  void callSomeMethod() {
    SampleObject sampleObject = new SampleObject();
    sampleObject.methodWithMetrics();
  }

  /**
   * Call other method.
   */
  @Test
  void callOtherMethod() {
    SampleObject sampleObject = new SampleObject();
    sampleObject.methodWithMetricsAndTags("aValue");
  }

}