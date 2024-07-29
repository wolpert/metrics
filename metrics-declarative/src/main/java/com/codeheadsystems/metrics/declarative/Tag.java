package com.codeheadsystems.metrics.declarative;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Tag annotation to specify the value should be used for metrics tag. Optionally specify the tag name.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
public @interface Tag {

  /**
   * Set this if you want to specify the tag name in an override..
   *
   * @return the string
   */
  String value() default "";

}
