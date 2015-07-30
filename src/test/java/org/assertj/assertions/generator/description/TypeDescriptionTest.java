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
package org.assertj.assertions.generator.description;

import static org.junit.rules.ExpectedException.none;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;


public class TypeDescriptionTest {
  
  @Rule
  public ExpectedException thrown = none();
  
  @SuppressWarnings("unused")
  @Test
  public void should_throw_exception_in_constructor_call_if_typename_parameter_is_null() throws Exception {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("typeName must not be null.");
    new TypeDescription(null);
  }

}
