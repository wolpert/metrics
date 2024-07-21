/*
 *    Copyright (c) 2022 Ned Wolpert <ned.wolpert@gmail.com>
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.codeheadsystems.metrics.test;


import com.codeheadsystems.metrics.MetricFactory;
import com.codeheadsystems.metrics.Metrics;
import com.codeheadsystems.metrics.impl.NullMetricsImpl;
import org.junit.jupiter.api.BeforeEach;

/**
 * Extend this to have metricsImpl you can use for your test. Queryable, but calls the needed components.
 */
public abstract class BaseMetricTest {


  /**
   * The MetricsImpl.
   */
  protected Metrics metrics;

  protected MetricFactory metricsFactory;

  /**
   * Sets metricsImpl.
   */
  @BeforeEach
  protected void setupMetrics() {
    metrics = new NullMetricsImpl();
    metricsFactory = MetricFactory.builder().build();
  }
}
