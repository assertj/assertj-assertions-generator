package org.fest.assertions.generator.description.converter;

import org.fest.assertions.generator.description.ClassDescription;

/**
 * 
 * General contract to convert an object to a {@link ClassDescription}.
 *  
 * @param <T> the type to convert to {@link ClassDescription}. 
 * @author Joel Costigliola 
 *
 */
public interface ClassDescriptionConverter<T> {

  /**
   * Convert T instance to a {@link ClassDescription} instance.
   * @param instance to convert to a {@link ClassDescription} instance.
   * @return the {@link ClassDescription} instance from given T instance.
   */
  public ClassDescription convertToClassDescription(T instance);
  
}
