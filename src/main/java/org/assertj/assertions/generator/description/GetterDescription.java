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
public class GetterDescription implements Comparable<GetterDescription> {

  private String propertyName;
  private TypeDescription typeDescription;

  public GetterDescription(String propertyName, TypeDescription typeDescription) {
    super();
    this.propertyName = propertyName;
    this.typeDescription = typeDescription;
  }

  public String getPropertyName() {
    return propertyName;
  }

  public String getPropertyTypeName() {
    return typeDescription.getSimpleNameWithOuterClass();
  }

  public boolean isIterablePropertyType() {
    return typeDescription.isIterable();
  }

  public boolean isArrayPropertyType() {
    return typeDescription.isArray();
  }

  public boolean isPrimitivePropertyType() {
    return typeDescription.isPrimitive();
  }

  public boolean isBooleanPropertyType() {
    return typeDescription.isBoolean();
  }

  public int compareTo(GetterDescription other) {
    return propertyName.compareTo(other.propertyName);
  }

  public String getElementTypeName() {
    return typeDescription.getElementTypeName() == null ? null : typeDescription.getElementTypeName().getSimpleNameWithOuterClass();
  }

  @Override
  public String toString() {
    return "GetterDescription [propertyName=" + propertyName + ", typeDescription=" + typeDescription + "]";
  }

}
