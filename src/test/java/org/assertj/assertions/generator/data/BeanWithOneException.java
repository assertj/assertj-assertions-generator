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
package org.assertj.assertions.generator.data;

import java.io.IOException;
import java.util.List;

/**
 * This is a bean whose getters throws one exception.
 */
public class BeanWithOneException {
  @SuppressWarnings("unused")
  public String getStringPropertyThrowsException() throws IOException {
    return null;
  }

  @SuppressWarnings("unused")
  public boolean isBooleanPropertyThrowsException() throws IOException {
    return false;
  }

  @SuppressWarnings("unused")
  public String[] getArrayPropertyThrowsException() throws IOException {
    return null;
  }

  @SuppressWarnings("unused")
  public List<String> getIterablePropertyThrowsException() throws IOException {
    return null;
  }
}
