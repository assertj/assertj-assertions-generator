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
 * Copyright 2012-2017 the original author or authors.
 */
package org.assertj.assertions.generator.data;

import org.assertj.assertions.generator.GenerateAssertion;

/**
 * class with annotated non standard getters in order to generate assertions for them.
 */
public class AnnotatedClass {

  @GenerateAssertion
  public boolean thisIsAProperty() {
    return false;
  }

  public boolean thisIsNotAProperty() {
    return false;
  }

  // assertion will be generated as this is a standard getter
  public boolean getStuff() {
    return false;
  }

  @GenerateAssertion
  public Object anotherProperty() {
    return null;
  }
}
