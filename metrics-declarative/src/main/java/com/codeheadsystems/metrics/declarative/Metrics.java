package com.codeheadsystems.metrics.declarative;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annottate your method with this to automatically add a with()/time() call around your method.
 * You can use the @Tag annotation to specify the tags for the metric too.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Metrics {

  /**
   * Specify this if you want to override the generated metric name..
   *
   * @return the string
   */
  String value() default "";

}
