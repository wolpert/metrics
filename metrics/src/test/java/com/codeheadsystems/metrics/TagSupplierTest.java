package com.codeheadsystems.metrics;

import static org.assertj.core.api.Assertions.assertThat;

import io.micrometer.core.instrument.Tags;
import org.junit.jupiter.api.Test;

/**
 * Tests for the tags supplier.
 */
public class TagSupplierTest {

  private static final Tags TAGS = Tags.of("a", "1", "b", "2");
  private static final Tags UPDATED = Tags.of("b", "3", "c", "4");

  @Test
  void supplierWorks() {
    TagsSupplier<String> tagsSupplier = s -> TAGS;
    assertThat(tagsSupplier.from(""))
        .isEqualTo(TAGS);
  }

  @Test
  void withWorks() {
    TagsSupplier<String> tagsSupplier = s -> UPDATED;
    assertThat(tagsSupplier.from("", TAGS))
        .isEqualTo(Tags.of("a", "1", "b", "3", "c", "4"));
  }

}
