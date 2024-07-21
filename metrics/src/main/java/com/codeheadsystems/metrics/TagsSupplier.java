package com.codeheadsystems.metrics;

import java.util.function.Supplier;

/**
 * A supplier for tags. This is a convince class.
 */
@FunctionalInterface
public interface TagsSupplier extends Supplier<Tags> {

}