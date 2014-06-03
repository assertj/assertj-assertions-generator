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
public class FieldDescription extends DataDescription implements Comparable<FieldDescription> {

  public FieldDescription(String name, TypeDescription typeDescription) {
    super(name, typeDescription);
  }

  public int compareTo(FieldDescription other) {
    return name.compareTo(other.name);
  }

  @Override
  public String toString() {
    return "FieldDescription[name=" + name + ", typeDescription=" + typeDescription + "]";
  }
}
