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
package org.assertj.assertions.generator;

import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;
import org.assertj.assertions.generator.data.BeanWithOneException;
import org.assertj.assertions.generator.data.BeanWithTwoExceptions;
import org.junit.experimental.theories.DataPoint;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * This interface contains a set of constants for testing generation from {@link org.assertj.assertions.generator.data.BeanWithOneException} class.
 */
public interface BeanWithExceptionsTest {
    List<TypeToken<? extends Throwable>> ONE_EXCEPTION =
        ImmutableList.<TypeToken<? extends Throwable>>of(TypeToken.of(IOException.class));
    List<TypeToken<? extends Throwable>> TWO_EXCEPTIONS =
        ImmutableList.<TypeToken<? extends Throwable>>of(TypeToken.of(IOException.class),
                                                         TypeToken.of(SQLException.class));
    @DataPoint
    TypeToken<BeanWithOneException> BEAN_WITH_ONE_EXCEPTION = TypeToken.of(BeanWithOneException.class);
    
    @DataPoint
    TypeToken<BeanWithTwoExceptions> BEAN_WITH_TWO_EXCEPTIONS = TypeToken.of(BeanWithTwoExceptions.class);
    
    @DataPoint
    GetterWithException STRING_1_EXCEPTION = new GetterWithException(BEAN_WITH_ONE_EXCEPTION, "stringPropertyThrowsException", ONE_EXCEPTION, false);
    @DataPoint
    GetterWithException STRING_2_EXCEPTIONS = new GetterWithException(BEAN_WITH_TWO_EXCEPTIONS, "stringPropertyThrowsException", TWO_EXCEPTIONS, false);
    @DataPoint
    GetterWithException BOOLEAN_1_EXCEPTION = new GetterWithException(BEAN_WITH_ONE_EXCEPTION, "booleanPropertyThrowsException", ONE_EXCEPTION, true);
    @DataPoint
    GetterWithException BOOLEAN_2_EXCEPTIONS = new GetterWithException(BEAN_WITH_TWO_EXCEPTIONS, "booleanPropertyThrowsException", TWO_EXCEPTIONS, true);
    @DataPoint
    GetterWithException ARRAY_1_EXCEPTION = new GetterWithException(BEAN_WITH_ONE_EXCEPTION, "arrayPropertyThrowsException", ONE_EXCEPTION, false);
    @DataPoint
    GetterWithException ARRAY_2_EXCEPTIONS = new GetterWithException(BEAN_WITH_TWO_EXCEPTIONS, "arrayPropertyThrowsException", TWO_EXCEPTIONS, false);
    @DataPoint
    GetterWithException ITERABLE_1_EXCEPTION = new GetterWithException(BEAN_WITH_ONE_EXCEPTION, "iterablePropertyThrowsException", ONE_EXCEPTION, false);
    @DataPoint
    GetterWithException ITERABLE_2_EXCEPTIONS = new GetterWithException(BEAN_WITH_TWO_EXCEPTIONS, "iterablePropertyThrowsException", TWO_EXCEPTIONS, false);


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
