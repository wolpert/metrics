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

package com.codeheadsystems.metrics.helper;

import com.codahale.metrics.MetricRegistry;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.dropwizard.DropwizardConfig;
import io.micrometer.core.instrument.dropwizard.DropwizardMeterRegistry;
import io.micrometer.core.instrument.util.HierarchicalNameMapper;

/**
 * Helper used to instrument dropwizard to allow for micrometer.
 */
public class DropwizardMetricsHelper {

  /**
   * Use this to create a meter registry that works for drop wizard.
   *
   * @param metricRegistry from drop wizard.
   * @return meter registry
   */
  public MeterRegistry instrument(final MetricRegistry metricRegistry) {
    final DropwizardConfig config = new DropwizardConfig() {
      @Override
      public String prefix() {
        return "slf4j";
      }

      @Override
      public String get(final String key) {
        return null;
      }
    };
    return new DropwizardMeterRegistry(config, metricRegistry, HierarchicalNameMapper.DEFAULT, Clock.SYSTEM) {
      @Override
      protected Double nullGaugeValue() {
        return null;
      }
    };
  }

}
