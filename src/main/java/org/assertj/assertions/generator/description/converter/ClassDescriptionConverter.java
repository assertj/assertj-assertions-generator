/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 * Copyright 2012-2021 the original author or authors.
 */
package org.assertj.assertions.generator.description.converter;

import org.assertj.assertions.generator.description.ClassDescription;

/**
 * 
 * General contract to convert an object to a {@link ClassDescription}.
 *  
 * @param <T> the valueType to convert to {@link ClassDescription}.
 * @author Joel Costigliola 
 *
 */
public interface ClassDescriptionConverter<T> {

  /**
   * Convert T instance to a {@link ClassDescription} instance.
   * @param instance to convert to a {@link ClassDescription} instance.
   * @return the {@link ClassDescription} instance from given T instance.
   */
  ClassDescription convertToClassDescription(T instance);
  
}
