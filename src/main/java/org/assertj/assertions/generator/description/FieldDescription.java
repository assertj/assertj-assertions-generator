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
 * <li>the property name, here "age"</li>
 * <li>property type</li>
 * </ul>
 * This class is immutable.
 * 
 * @author Joel Costigliola
 */
public class FieldDescription implements Comparable<FieldDescription> {

  private String name;
  private TypeDescription typeDescription;
  
  public FieldDescription(String name, TypeDescription typeDescription) {
    super();
    this.name = name;
    this.typeDescription = typeDescription;
  }

  public String getName() {
    return name;
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

  public boolean isRealNumberType() {
    return typeDescription.isRealNumber();
  }
  
  public boolean isBooleanPropertyType() {
    return typeDescription.isBoolean();
  }

  public int compareTo(FieldDescription other) {
    return name.compareTo(other.name);
  }

  public String getElementTypeName() {
    return typeDescription.getElementTypeName() == null ? null : typeDescription.getElementTypeName().getSimpleNameWithOuterClass();
  }

  @Override
  public String toString() {
    return "FieldDescription[name=" + name + ", typeDescription=" + typeDescription + "]";
  }
}
