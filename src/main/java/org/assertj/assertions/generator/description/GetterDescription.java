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

import com.google.common.collect.ImmutableList;
import com.google.common.reflect.Invokable;
import com.google.common.reflect.TypeToken;
import org.assertj.assertions.generator.util.ClassUtil;

import java.lang.reflect.Method;

import static org.assertj.assertions.generator.util.ClassUtil.isBoolean;
import static org.assertj.assertions.generator.util.ClassUtil.isValidPredicateName;

/**
 * Stores the information needed to generate an assertion for a public getter method.
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
 * <li>property valueType</li>
 * </ul>
 * Note that <code>Person</code> doesn't need to have an <code>age</code> field, just the <code>getAge</code> method.
 * <p>
 * 
 * @author Joel Costigliola
 * 
 */
public class GetterDescription extends DataDescription implements Comparable<GetterDescription> {

  private final Invokable<?, ?> invokable;
  private final ImmutableList<TypeToken<? extends Throwable>> exceptions;

  public GetterDescription(String propertyName, TypeToken<?> owningType, Method method) {
    super(propertyName, method, Visibility.PUBLIC, owningType.method(method).getReturnType(), owningType);
    this.invokable = owningType.method(method);
    this.exceptions = invokable.getExceptionTypes();
  }

  @Override
  public Method getOriginalMember() {
    return (Method) super.getOriginalMember();
  }

  @Override
  public int compareTo(GetterDescription other) {
    return super.compareTo(other);
  }

  public ImmutableList<TypeToken<? extends Throwable>> getExceptions() {
    return exceptions;
  }

  @Override
  public boolean isPredicate() {
    return isBoolean(valueType) && isValidPredicateName(originalMember.getName());
  }

}
