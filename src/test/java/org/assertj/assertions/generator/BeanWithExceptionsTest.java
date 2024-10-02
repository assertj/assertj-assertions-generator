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
 * Copyright 2012-2021 the original author or authors.
 */
package org.assertj.assertions.generator;

import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;
import org.assertj.assertions.generator.data.BeanWithOneException;
import org.assertj.assertions.generator.data.BeanWithTwoExceptions;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * This interface contains a set of constants for testing generation from {@link org.assertj.assertions.generator.data.BeanWithOneException} class.
 */
public interface BeanWithExceptionsTest {

  List<TypeToken<? extends Throwable>> ONE_EXCEPTION =
    ImmutableList.of(TypeToken.of(IOException.class));

  List<TypeToken<? extends Throwable>> TWO_EXCEPTIONS =
    ImmutableList.of(TypeToken.of(IOException.class), TypeToken.of(SQLException.class));


  TypeToken<BeanWithOneException> BEAN_WITH_ONE_EXCEPTION = TypeToken.of(BeanWithOneException.class);


  TypeToken<BeanWithTwoExceptions> BEAN_WITH_TWO_EXCEPTIONS = TypeToken.of(BeanWithTwoExceptions.class);

  TypeToken<?>[] TYPE_TOKENS = {BEAN_WITH_ONE_EXCEPTION, BEAN_WITH_TWO_EXCEPTIONS};


  GetterWithException STRING_1_EXCEPTION = new GetterWithException(BEAN_WITH_ONE_EXCEPTION, "stringPropertyThrowsException", ONE_EXCEPTION, false);

  GetterWithException STRING_2_EXCEPTIONS = new GetterWithException(BEAN_WITH_TWO_EXCEPTIONS, "stringPropertyThrowsException", TWO_EXCEPTIONS, false);

  GetterWithException BOOLEAN_1_EXCEPTION = new GetterWithException(BEAN_WITH_ONE_EXCEPTION, "booleanPropertyThrowsException", ONE_EXCEPTION, true);

  GetterWithException BOOLEAN_2_EXCEPTIONS = new GetterWithException(BEAN_WITH_TWO_EXCEPTIONS, "booleanPropertyThrowsException", TWO_EXCEPTIONS, true);

  GetterWithException ARRAY_1_EXCEPTION = new GetterWithException(BEAN_WITH_ONE_EXCEPTION, "arrayPropertyThrowsException", ONE_EXCEPTION, false);

  GetterWithException ARRAY_2_EXCEPTIONS = new GetterWithException(BEAN_WITH_TWO_EXCEPTIONS, "arrayPropertyThrowsException", TWO_EXCEPTIONS, false);

  GetterWithException ITERABLE_1_EXCEPTION = new GetterWithException(BEAN_WITH_ONE_EXCEPTION, "iterablePropertyThrowsException", ONE_EXCEPTION, false);

  GetterWithException ITERABLE_2_EXCEPTIONS = new GetterWithException(BEAN_WITH_TWO_EXCEPTIONS, "iterablePropertyThrowsException", TWO_EXCEPTIONS, false);

  GetterWithException[] GETTER_WITH_EXCEPTIONS = {
    STRING_1_EXCEPTION,
    STRING_2_EXCEPTIONS,
    BOOLEAN_1_EXCEPTION,
    BOOLEAN_2_EXCEPTIONS,
    ARRAY_1_EXCEPTION,
    ARRAY_2_EXCEPTIONS,
    ITERABLE_1_EXCEPTION,
    ITERABLE_2_EXCEPTIONS
  };

  class GetterWithException implements Comparable<GetterWithException> {
    private final TypeToken<?> beanClass;
    private final String propertyName;
    private final List<TypeToken<? extends Throwable>> exceptions;
    private final boolean booleanType;

    public GetterWithException(TypeToken<?> beanClass,
                               String propertyName,
                               List<TypeToken<? extends Throwable>> exceptions,
                               boolean booleanType) {
      this.beanClass = beanClass;
      this.propertyName = propertyName;
      this.booleanType = booleanType;
      this.exceptions = exceptions;
    }

    public String getPropertyName() {
      return propertyName;
    }

    public List<TypeToken<? extends Throwable>> getExceptions() {
      return exceptions;
    }

    public TypeToken<?> getBeanClass() {
      return beanClass;
    }

    public boolean isBooleanType() {
      return booleanType;
    }

    @Override
    public int compareTo(GetterWithException o) {
      return propertyName.compareTo(o.propertyName);
    }
  }

}
