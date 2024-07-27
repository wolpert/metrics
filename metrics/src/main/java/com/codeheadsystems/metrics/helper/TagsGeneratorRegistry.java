package com.codeheadsystems.metrics.helper;

import com.codeheadsystems.metrics.Tags;
import com.codeheadsystems.metrics.TagsGenerator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Gives you the ability to register generators for tags. Note that null objects will not generate
 * any tags.
 */
public class TagsGeneratorRegistry {

  private final Map<Class<?>, TagsGenerator<?>> tagsGeneratorMap;

  /**
   * Default Constructor.
   */
  public TagsGeneratorRegistry() {
    this.tagsGeneratorMap = new HashMap<>();
  }

  /**
   * Register a class with a tags generator.
   *
   * @param clazz         for the generator.
   * @param tagsGenerator for the class.
   */
  public <R> void register(final Class<R> clazz, final TagsGenerator<R> tagsGenerator) {
    tagsGeneratorMap.put(clazz, tagsGenerator);
  }

  /**
   * Deregister a class.
   *
   * @param clazz for the generator.
   */
  public <R> void deregister(final Class<R> clazz) {
    tagsGeneratorMap.remove(clazz);
  }

  /**
   * Get the tags generator for the class.
   *
   * @param clazz for the generator.
   * @param <R>   the type.
   * @return tags generator.
   */
  @SuppressWarnings("unchecked")
  public <R> TagsGenerator<R> get(final Class<R> clazz) {
    return (TagsGenerator<R>) tagsGeneratorMap.get(clazz);
  }

  /**
   * Helper method to aggregate tags if they exist in the registry.
   * If the object is null, this won't do anything.
   *
   * @param existingTags to aggregate to.
   * @param object       to get the tags from.
   * @param <R>          the type.
   */
  public <R> void aggregateIfFound(final Tags existingTags, final R object) {
    if (object == null) {
      return;
    }
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
