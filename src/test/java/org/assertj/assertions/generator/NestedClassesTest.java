package org.assertj.assertions.generator;

import org.assertj.assertions.generator.data.OuterClass;
import org.junit.experimental.theories.DataPoint;

/**
 * This class contains a set of constants for nested classes.
 */
public interface NestedClassesTest {
    @DataPoint
    public static final NestedClass SNC = new NestedClass(OuterClass.StaticNestedPerson.class, "OuterClass.StaticNestedPerson");
    @DataPoint
    public static final NestedClass SNC_SNC = new NestedClass(OuterClass.StaticNestedPerson.SNP_StaticNestedPerson.class, "OuterClass.StaticNestedPerson.SNP_StaticNestedPerson");
    @DataPoint
    public static final NestedClass SNC_IC = new NestedClass(OuterClass.StaticNestedPerson.SNP_InnerPerson.class, "OuterClass.StaticNestedPerson.SNP_InnerPerson");
    @DataPoint
    public static final NestedClass IC = new NestedClass(OuterClass.InnerPerson.class, "OuterClass.InnerPerson");
    @DataPoint
    public static final NestedClass IC_IC = new NestedClass(OuterClass.InnerPerson.IP_InnerPerson.class, "OuterClass.InnerPerson.IP_InnerPerson");

    public static class NestedClass {
        private final Class<?> nestedClass;
        private final String classNameWithOuterClass;

        public NestedClass(Class<?> nestedClass, String classNameWithOuterClass) {
            this.nestedClass = nestedClass;
            this.classNameWithOuterClass = classNameWithOuterClass;
        }

        public String getClassNameWithOuterClass() {
            return classNameWithOuterClass;
        }

        public Class<?> getNestedClass() {
            return nestedClass;
        }
    }
}
