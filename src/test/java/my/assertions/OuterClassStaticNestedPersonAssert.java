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
package my.assertions;

import org.assertj.assertions.generator.data.OuterClass;
import org.assertj.core.api.AbstractObjectAssert;
import org.assertj.core.util.Objects;

/**
 * this class was generated but kep in source control to see what was needed in term of import when dealing with nested classes 
 */
@javax.annotation.Generated(value = "assertj-assertions-generator")
public class OuterClassStaticNestedPersonAssert
    extends AbstractObjectAssert<OuterClassStaticNestedPersonAssert, OuterClass.StaticNestedPerson> {

  /**
   * Creates a new <code>{@link OuterClassStaticNestedPersonAssert}</code> to make assertions on actual OuterClass.StaticNestedPerson.
   * @param actual the OuterClass.StaticNestedPerson we want to make assertions on.
   */
  public OuterClassStaticNestedPersonAssert(OuterClass.StaticNestedPerson actual) {
    super(actual, OuterClassStaticNestedPersonAssert.class);
  }

  /**
   * An entry point for OuterClassStaticNestedPersonAssert to follow AssertJ standard <code>assertThat()</code> statements.<br>
   * With a static import, one can write directly: <code>assertThat(myOuterClass.StaticNestedPerson)</code> and get specific assertion with code completion.
   * @param actual the OuterClass.StaticNestedPerson we want to make assertions on.
   * @return a new <code>{@link OuterClassStaticNestedPersonAssert}</code>
   */
  @org.assertj.core.util.CheckReturnValue
  public static OuterClassStaticNestedPersonAssert assertThat(OuterClass.StaticNestedPerson actual) {
    OuterClass.StaticNestedPerson.SNP_InnerPerson d = null;
    OuterClass.StaticNestedPerson.SNP_StaticNestedPerson f = null;
    return new OuterClassStaticNestedPersonAssert(actual);
  }

  /**
   * Verifies that the actual OuterClass.StaticNestedPerson's name is equal to the given one.
   * @param name the given name to compare the actual OuterClass.StaticNestedPerson's name to.
   * @return this assertion object.
   * @throws AssertionError - if the actual OuterClass.StaticNestedPerson's name is not equal to the given one.
   */
  public OuterClassStaticNestedPersonAssert hasName(String name) {
    // check that actual OuterClass.StaticNestedPerson we want to make assertions on is not null.
    isNotNull();

    // overrides the default error message with a more explicit one
    String assertjErrorMessage = "\nExpecting name of:\n  <%s>\nto be:\n  <%s>\nbut was:\n  <%s>";

    // null safe check
    String actualName = actual.getName();
    if (!Objects.areEqual(actualName, name)) {
      failWithMessage(assertjErrorMessage, actual, name, actualName);
    }

    // return the current assertion for method chaining
    return this;
  }

}
