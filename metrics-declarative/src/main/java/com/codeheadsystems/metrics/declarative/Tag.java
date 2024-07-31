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
   * Set this if you to include the tag in the metrics. You 'have to' set the value to a non-empty string for it to
   * work. (I cannot use reflection has you cannot be sure to get the name of the object parameter passed in.)
   *
   * @return the string
   */
  String value();

}
