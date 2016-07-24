/**
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 * Copyright 2012-2015 the original author or authors.
 */
package org.assertj.assertions.generator.description.converter;

import java.util.Collections;
import java.util.Set;

public class AnnotationConfiguration {

  private final Set<Class<?>> includeAnnotations;

  public AnnotationConfiguration(Set<Class<?>> includeAnnotations) {
    this.includeAnnotations = includeAnnotations;
  }

  public AnnotationConfiguration() {
    this(Collections.<Class<?>>emptySet());
  }

  public Set<Class<?>> getIncludeAnnotations() {
    return includeAnnotations;
  }
}
