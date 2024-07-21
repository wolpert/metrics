package com.codeheadsystems.metrics;

/**
 * Used for a supplier that can throw an exception.
 *
 * @param <R> the return value.
 * @param <E> the exception that can be thrown.
 */
@FunctionalInterface
public interface CheckedSupplier<R, E extends Exception> {

  /**
   * The method to get the result. If it fails, the checked exception is thrown.
   *
   * @return the result.
   * @throws E on failure.
   */
  R get() throws E;

}
