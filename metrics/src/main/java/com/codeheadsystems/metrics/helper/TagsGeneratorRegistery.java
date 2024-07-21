package com.codeheadsystems.metrics.helper;

import com.codeheadsystems.metrics.Tags;
import com.codeheadsystems.metrics.TagsGenerator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Gives you the ability to register generators for tags.
 */
public class TagsGeneratorRegistery {

  private final Map<Class<?>, TagsGenerator<?>> tagsGeneratorMap;

  /**
   * Default Constructor.
   */
  public TagsGeneratorRegistery() {
    this.tagsGeneratorMap = new HashMap<>();
  }

  /**
   * Register a class with a tags generator.
   *
   * @param clazz         for the generator.
   * @param tagsGenerator for the class.
   */
  public void register(final Class<?> clazz, final TagsGenerator<?> tagsGenerator) {
    tagsGeneratorMap.put(clazz, tagsGenerator);
  }

  /**
   * Deregister a class.
   *
   * @param clazz for the generator.
   */
  public void deregister(final Class<?> clazz) {
    tagsGeneratorMap.remove(clazz);
  }

  /**
   * Get the tags generator for the class.
   *
   * @param clazz for the generator.
   * @param <R>   the type.
   * @return tags generator.
   */
  public <R> TagsGenerator<R> get(final Class<R> clazz) {
    return (TagsGenerator<R>) tagsGeneratorMap.get(clazz);
  }

  /**
   * Helper method to aggregate tags if they exist in the registry.
   *
   * @param existingTags to aggregate to.
   * @param object       to get the tags from.
   * @param <R>          the type.
   */
  public <R> void aggregateIfFound(final Tags existingTags, final R object) {
    Class<R> clazz = (Class<R>) object.getClass();
    if (tagsGeneratorMap.containsKey(clazz)) {
      TagsGenerator<R> tagsGenerator = get(clazz);
      existingTags.add(tagsGenerator.from(object));
    }

  }

  /**
   * Get the registered classes.
   *
   * @return set of classes.
   */
  public Set<Class<?>> getRegisteredClasses() {
    return tagsGeneratorMap.keySet();
  }

}
