package com.codeheadsystems.metrics.declarative;

import static org.assertj.core.api.Assertions.assertThat;

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

}