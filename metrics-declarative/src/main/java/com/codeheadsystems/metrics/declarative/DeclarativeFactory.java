package com.codeheadsystems.metrics.declarative;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This is used to identify the metrics factory to apply for the declarative metrics.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface DeclarativeFactory {

  /**
   * Specify this if you want to override the generated metric name..
   *
   * @return the string
   */
  String value() default "";
}
