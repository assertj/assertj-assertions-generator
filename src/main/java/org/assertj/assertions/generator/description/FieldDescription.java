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
 * Copyright 2012-2020 the original author or authors.
 */
package org.assertj.assertions.generator.description;

import com.google.common.reflect.TypeToken;
import org.assertj.assertions.generator.util.ClassUtil;

import java.lang.reflect.Field;

import static org.apache.commons.lang3.StringUtils.capitalize;
import static org.assertj.assertions.generator.util.ClassUtil.propertyNameOf;

/**
 * Stores the information needed to generate an assertion for a field.
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
 * <li>the field valueType</li>
 * <li>its visibility to determine how to access the field value</li>
 * </ul>
 * This class is immutable.
 * 
 * @author Joel Costigliola
 */
public class FieldDescription extends DataDescription implements Comparable<FieldDescription> {

  public FieldDescription(Field field, TypeToken<?> owningType) {
    this(field, Visibility.PUBLIC, owningType);
  }

  public FieldDescription(Field field, Visibility visibility, TypeToken<?> owningType) {
    super(propertyNameOf(field), field, visibility, owningType.resolveType(field.getGenericType()), owningType);
  }

  @Override
  public int compareTo(FieldDescription other) {
    return super.compareTo(other);
  }

  @Override
  public Field getOriginalMember() {
    return (Field) super.getOriginalMember();
  }

  @Override
  public String toString() {
    return "FieldDescription[originalMember=" + originalMember + ", valueType=" + valueType + ", visibility="
           + visibility + ']';
  }

  @Override
  public boolean isPredicate() {
    return ClassUtil.isBoolean(valueType);
  }

  @Override
  public String getPredicate() {
    return hasNegativePredicate() ? originalMember.getName() : "is" + capitalize(originalMember.getName());
  }

  @Override
  public String getNegativePredicate() {
    return !hasNegativePredicate() ? "isNot" + capitalize(originalMember.getName()) : super.getNegativePredicate();
  }

}
