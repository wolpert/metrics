package com.codeheadsystems.metrics;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;

class TagsTest {

  private static final String[] DEFAULT_ARRAY = new String[]{"a", "1", "b", "2"};
  private static final Map<String, String> DEFAULT_MAP = Map.of("a", "1", "b", "2");

  private static final String[] OVERRIDE_ARRAY = new String[]{"b", "3", "c", "4"};
  private static final Map<String, String> OVERRIDE_MAP = Map.of("b", "3", "c", "4");

  private static final Map<String, String> COMBINED_MAP = Map.of("a", "1", "b", "3", "c", "4");

  @Test
  void testTagsArray() {
    Tags tags = new Tags(DEFAULT_ARRAY);
    assertThat(tags.getTags()).isEqualTo(DEFAULT_MAP);
  }

  @Test
  void testTagsMap() {
    Tags tags = new Tags(DEFAULT_MAP);
    assertThat(tags.getTags()).isEqualTo(DEFAULT_MAP);
  }

  @Test
  void testOf() {
    Tags tags = Tags.of(DEFAULT_ARRAY);
    assertThat(tags.getTags()).isEqualTo(DEFAULT_MAP);
  }

  @Test
  void empty() {
    Tags tags = Tags.empty();
    assertThat(tags.getTags()).isEmpty();
  }

  @Test
  void testAdd() {
    Tags tags = new Tags(DEFAULT_ARRAY);
    tags.add(new Tags(OVERRIDE_ARRAY));
    assertThat(tags.getTags()).isEqualTo(COMBINED_MAP);
  }

  @Test
  void testFrom() {
    Tags tags = new Tags(DEFAULT_ARRAY);
    Tags from = tags.from(new Tags(OVERRIDE_ARRAY));
    assertThat(tags.getTags()).isEqualTo(DEFAULT_MAP);
    assertThat(from.getTags()).isEqualTo(COMBINED_MAP);
  }

}