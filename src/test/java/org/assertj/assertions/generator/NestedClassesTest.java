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
 * Copyright 2012-2015 the original author or authors.
 */
package org.assertj.assertions.generator;

import org.apache.commons.lang3.StringUtils;
import org.assertj.assertions.generator.data.OuterClass;
import org.junit.experimental.theories.DataPoint;

/**
 * This class contains a set of constants for nested classes.
 */
public interface NestedClassesTest {
  @DataPoint
  public static final NestedClass SNC = new NestedClass(OuterClass.StaticNestedPerson.class,
                                                        "OuterClass.StaticNestedPerson", "OuterClass");
  @DataPoint
  public static final NestedClass SNC_SNC = new NestedClass(OuterClass.StaticNestedPerson.SNP_StaticNestedPerson.class,
                                                            "OuterClass.StaticNestedPerson.SNP_StaticNestedPerson",
                                                            "OuterClass");
  @DataPoint
  public static final NestedClass SNC_IC = new NestedClass(OuterClass.StaticNestedPerson.SNP_InnerPerson.class,
                                                           "OuterClass.StaticNestedPerson.SNP_InnerPerson",
                                                           "OuterClass");
  @DataPoint
  public static final NestedClass IC = new NestedClass(OuterClass.InnerPerson.class, "OuterClass.InnerPerson",
                                                       "OuterClass");
  @DataPoint
  public static final NestedClass IC_IC = new NestedClass(OuterClass.InnerPerson.IP_InnerPerson.class,
                                                          "OuterClass.InnerPerson.IP_InnerPerson", "OuterClass");

  public static class NestedClass {
    private final Class<?> nestedClass;
    private final String classNameWithOuterClass;
    private final String outerClassName;

    public NestedClass(Class<?> nestedClass, String classNameWithOuterClass, String outerClassName) {
      this.nestedClass = nestedClass;
      this.classNameWithOuterClass = classNameWithOuterClass;
      this.outerClassName = outerClassName;
    }

    public String getClassNameWithOuterClass() {
      return classNameWithOuterClass;
    }

    public String getClassNameWithOuterClassNotSeparatedBytDots() {
      return StringUtils.remove(classNameWithOuterClass, '.');
    }

    public Class<?> getNestedClass() {
      return nestedClass;
    }

    public String getOuterClassName() {
      return outerClassName;
    }
  }
}
