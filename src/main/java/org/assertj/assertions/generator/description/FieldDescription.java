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
 * Copyright 2012-2014 the original author or authors.
 */
package org.assertj.assertions.generator.description;

import static org.apache.commons.lang3.StringUtils.capitalize;


/**
 * Stores the information needed to generate an assertion for a public field.
 * <p>
 * Let's say we have the following method in class <code>Person</code> :
 * 
 * <pre>
 * <code>public int age</code>
 * </pre>
 * <p>
 * To generate <code>PersonAssert</code> <code>hasAge(int expectedAge)</code> assertion in <code>PersonAssert</code>, we
 * need to know :
 * <ul>
 * <li>the field name, here "age"</li>
 * <li>the field type</li>
 * </ul>
 * This class is immutable.
 * 
 * @author Joel Costigliola
 */
public class FieldDescription extends DataDescription implements Comparable<FieldDescription> {

  public FieldDescription(String name, TypeDescription typeDescription) {
    super(name, name, typeDescription);
  }

  public int compareTo(FieldDescription other) {
    return getName().compareTo(other.getName());
  }

  @Override
  public String toString() {
    return "FieldDescription[name=" + getName() + ", typeDescription=" + typeDescription + "]";
  }

  @Override
  public boolean isPredicate() {
    return typeDescription.isBoolean();
  }

  @Override
  public String getPredicate() {
	final String retval = super.getNegativePredicate();
	return retval == null ? "is" + capitalize(originalMember) : originalMember;
  }
  
  @Override
  public String getNegativePredicate() {
	final String retval = super.getNegativePredicate();
	return retval == null ? "isNot" + capitalize(originalMember) : retval;
  }
}
