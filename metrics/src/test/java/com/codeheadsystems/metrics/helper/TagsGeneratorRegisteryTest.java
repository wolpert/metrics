package com.codeheadsystems.metrics.helper;

import static org.assertj.core.api.Assertions.assertThat;

import com.codeheadsystems.metrics.Tags;
import com.codeheadsystems.metrics.TagsGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TagsGeneratorRegisteryTest {

  private static final Tags INTEGER_TAGS = Tags.of("integer", "1");
  private static final Tags FLOAT_TAGS = Tags.of("float", "1.0");

  private static final TagsGenerator<Integer> INTEGER_TAGS_GENERATOR = (i) -> INTEGER_TAGS;
  private static final TagsGenerator<Float> FLOAT_TAGS_GENERATOR = (f) -> FLOAT_TAGS;

  private TagsGeneratorRegistery tagsGeneratorRegistery;

  @BeforeEach
  public void setup() {
    tagsGeneratorRegistery = new TagsGeneratorRegistery();
    tagsGeneratorRegistery.register(Integer.class, INTEGER_TAGS_GENERATOR);
    tagsGeneratorRegistery.register(Float.class, FLOAT_TAGS_GENERATOR);
  }

  @Test
  public void testGet() {
    assertThat(tagsGeneratorRegistery.get(Integer.class).from(1)).isEqualTo(INTEGER_TAGS);
    assertThat(tagsGeneratorRegistery.get(Float.class).from(1.0f)).isEqualTo(FLOAT_TAGS);
  }

  @Test
  public void testDeregister() {
    tagsGeneratorRegistery.deregister(Integer.class);
    assertThat(tagsGeneratorRegistery.get(Integer.class)).isNull();
  }

  @Test
  public void testGetRegisteredClasses() {
    assertThat(tagsGeneratorRegistery.getRegisteredClasses()).containsExactlyInAnyOrder(Integer.class, Float.class);
  }


}