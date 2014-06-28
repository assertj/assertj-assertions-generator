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
 * Copyright @2010-2011 the original author or authors.
 */
package org.assertj.assertions.generator.description;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores the information needed to generate an assertion for a getter method.
 * <p>
 * Let's say we have the following method in class <code>Person</code> :
 * 
 * <pre>
 * <code>public int getAge()</code>
 * </pre>
 * <p>
 * To generate <code>PersonAssert</code> <code>hasAge(int expectedAge)</code> assertion in <code>PersonAssert</code>, we
 * need to know :
 * <ul>
 * <li>the property name, here "age"</li>
 * <li>property type</li>
 * </ul>
 * Note that <code>Person</code> doesn't need to have an <code>age</code> field, just the <code>getAge</code> method.
 * <p>
 * 
 * @author Joel Costigliola
 * 
 */
public class GetterDescription extends DataDescription implements Comparable<GetterDescription> {

  private final List<TypeName> exceptions;

  public GetterDescription(String propertyName, TypeDescription typeDescription, List<TypeName> exceptions) {
    super(propertyName, typeDescription);
    this.exceptions = new ArrayList<TypeName>(exceptions);
  }

  public String getPropertyName() {
    return getName();
  }

  public int compareTo(GetterDescription other) {
    return getName().compareTo(other.getName());
  }

  @Override
  public String toString() {
    return "GetterDescription [propertyName=" + getName() + ", typeDescription=" + typeDescription + "]";
  }

  public List<TypeName> getExceptions() {
    return exceptions;
  }

  public boolean isRealNumberType() {
    return typeDescription.isRealNumber();
  }

}
