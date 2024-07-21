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

package com.codeheadsystems.metrics.dagger;

import com.codeheadsystems.metrics.Tags;
import dagger.BindsOptionalOf;
import dagger.Module;
import dagger.Provides;
import java.util.Optional;
import java.util.function.Supplier;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Provide your own meter registry named "Provided Meter Registry" else you get the default.
 */
@Module(includes = MetricsModule.Binder.class)
public class MetricsModule {


  /**
   * Optional default tags to use.
   */
  public static final String PROVIDED_DEFAULT_METRIC_TAGS = "Provided default metric tags";



  /**
   * Default modules, using simply meter registry.
   */
  public MetricsModule() {
  }

  /**
   * Will provide the default tags.
   *
   * @param tags that are given by the app, if set.
   * @return the tags.
   */
  @Provides
  @Singleton
  public Supplier<Tags> defaultTags(@Named(PROVIDED_DEFAULT_METRIC_TAGS) final Optional<Tags> tags) {
    if (tags.isPresent()) {
      return tags::get;
    } else {
      return Tags::empty;
    }
  }

  /**
   * Binder module.
   */
  @Module
  public interface Binder {


    /**
     * Set your default tags if you want.
     *
     * @return tags. tags
     */
    @Named(PROVIDED_DEFAULT_METRIC_TAGS)
    @BindsOptionalOf
    Tags defaulTags();

  }

}
