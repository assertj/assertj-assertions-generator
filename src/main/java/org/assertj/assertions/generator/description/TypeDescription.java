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
 * Stores the information needed to generate an assertion for a getter method, and data related to the getter returned
 * type (mostly to import needed classes).
 * <p>
 * We need to know :
 * <ul>
 * <li>the property type</li>
 * <li>the involved generic types</li>
 * <li>the component type in case of property is an array</li>
 * </ul>
 * <p>
 * For example, let's say we have the following method in class <code>Team</code> :
 * 
 * <pre>
 * <code>public List&lt;Player&gt; getPlayers()</code>
 * </pre>
 * 
 * To generate the following assertion :
 * 
 * <pre>
 * <code>TeamAssert</code> <code>hasPlayers(Player... expectedPlayers)</code>
 * </pre>
 * 
 * we need to know the generic type of players property : <code>Player</code>.
 * <p>
 * 
 * @author Joel Costigliola
 * 
 */
public class TypeDescription {

  private TypeName typeName;
  private boolean isArray;
  private boolean isIterable;
  // for array or iterable types only
  private TypeName elementTypeName;

  public TypeDescription(TypeName typeName) {
    super();
    this.typeName = typeName;
    this.isArray = false;
    this.isIterable = false;
    this.elementTypeName = null;
  }

  public String getSimpleName() {
    return typeName.getSimpleName();
  }

  public boolean isArray() {
    return isArray;
  }

  public void setArray(boolean isArray) {
    this.isArray = isArray;
  }

  public boolean isPrimitive() {
    return typeName.isPrimitive();
  }

  public boolean isBoolean() {
    return typeName.isBoolean();
  }

  public TypeName getElementTypeName() {
    return elementTypeName;
  }

  public void setElementTypeName(TypeName elementTypeName) {
    this.elementTypeName = elementTypeName;
  }

  public boolean isIterable() {
    return isIterable;
  }

  public void setIterable(boolean isIterable) {
    this.isIterable = isIterable;
  }

  @Override
  public String toString() {
    return "TypeDescription[typeName=" + typeName + ", array=" + isArray + ", iterable=" + isIterable + ", primitive="
        + isPrimitive() + ", boolean=" + isBoolean() + ", elementTypeName=" + elementTypeName + "]";
  }

}
