package org.assertj.assertions.generator;

import org.assertj.assertions.generator.data.BeanWithOneException;
import org.assertj.assertions.generator.data.BeanWithTwoExceptions;
import org.assertj.assertions.generator.description.TypeName;
import org.junit.experimental.theories.DataPoint;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * This interface contains a set of constants for testing generation from {@link org.assertj.assertions.generator.data.BeanWithOneException} class.
 */
public interface BeanWithExceptionsTest {
    static final Class<?>[] ONE_EXCEPTION = new Class<?>[]{IOException.class};
    static final Class<?>[] TWO_EXCEPTIONS = new Class<?>[]{IOException.class, SQLException.class};

    @DataPoint
    public static final Class<BeanWithOneException> BEAN_WITH_ONE_EXCEPTION = BeanWithOneException.class;
    
    @DataPoint
    public static final Class<BeanWithTwoExceptions> BEAN_WITH_TWO_EXCEPTIONS = BeanWithTwoExceptions.class;
    
    @DataPoint
    public static final GetterWithException STRING_1_EXCEPTION = new GetterWithException(BEAN_WITH_ONE_EXCEPTION, "stringPropertyThrowsException", ONE_EXCEPTION, false);
    @DataPoint
    public static final GetterWithException STRING_2_EXCEPTIONS = new GetterWithException(BEAN_WITH_TWO_EXCEPTIONS, "stringPropertyThrowsException", TWO_EXCEPTIONS, false);
    @DataPoint
    public static final GetterWithException BOOLEAN_1_EXCEPTION = new GetterWithException(BEAN_WITH_ONE_EXCEPTION, "booleanPropertyThrowsException", ONE_EXCEPTION, true);
    @DataPoint
    public static final GetterWithException BOOLEAN_2_EXCEPTIONS = new GetterWithException(BEAN_WITH_TWO_EXCEPTIONS, "booleanPropertyThrowsException", TWO_EXCEPTIONS, true);
    @DataPoint
    public static final GetterWithException ARRAY_1_EXCEPTION = new GetterWithException(BEAN_WITH_ONE_EXCEPTION, "arrayPropertyThrowsException", ONE_EXCEPTION, false);
    @DataPoint
    public static final GetterWithException ARRAY_2_EXCEPTIONS = new GetterWithException(BEAN_WITH_TWO_EXCEPTIONS, "arrayPropertyThrowsException", TWO_EXCEPTIONS, false);
    @DataPoint
    public static final GetterWithException ITERABLE_1_EXCEPTION = new GetterWithException(BEAN_WITH_ONE_EXCEPTION, "iterablePropertyThrowsException", ONE_EXCEPTION, false);
    @DataPoint
    public static final GetterWithException ITERABLE_2_EXCEPTIONS = new GetterWithException(BEAN_WITH_TWO_EXCEPTIONS, "iterablePropertyThrowsException", TWO_EXCEPTIONS, false);


    public static class GetterWithException {
        private final Class<?> beanClass;
        private final String propertyName;
        private final List<TypeName> exceptions;
        private final boolean booleanType;

        public GetterWithException(Class<?> beanClass, String propertyName, Class<?>[] exceptions, boolean booleanType) {
            this.beanClass = beanClass;
            this.propertyName = propertyName;
            this.booleanType = booleanType;
            List<TypeName> list = new ArrayList<TypeName>(exceptions.length);
            for (Class<?> exception : exceptions) {
                list.add(new TypeName(exception));
            }
            this.exceptions = list;
        }

        public String getPropertyName() {
            return propertyName;
        }

        public TypeName[] getExceptions() {
            return exceptions.toArray(new TypeName[exceptions.size()]);
        }

        public Class getBeanClass() {
            return beanClass;
        }

        public boolean isBooleanType() {
            return booleanType;
        }
    }
}
