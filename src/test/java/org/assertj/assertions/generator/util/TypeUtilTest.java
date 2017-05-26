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
package org.assertj.assertions.generator.util;

import com.google.common.reflect.TypeToken;
import org.assertj.assertions.generator.AssertionGeneratorTest;
import org.assertj.assertions.generator.description.GetterDescriptionTest;
import org.assertj.core.api.BooleanAssert;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.assertj.core.api.Java6Assertions.assertThat;

/**
 * Describes tests for the {@link TypeUtil} class
 */
public class TypeUtilTest {

  public static class Inner {}

  @Test
  public void resolveTypeNameInPackage() throws Exception {
    assertThat(TypeUtil.resolveTypeNameInPackage(String.class, String.class.getPackage().getName()))
        .as("java lang type has simple name with package")
        .isEqualTo(String.class.getSimpleName());
    assertThat(TypeUtil.resolveTypeNameInPackage(String.class, null))
        .as("java lang type has simple name without package")
        .isEqualTo(String.class.getSimpleName());


    assertThat(TypeUtil.resolveTypeNameInPackage(TypeUtilTest.class, null))
        .as("normal type has FQN without package")
        .isEqualTo(TypeUtilTest.class.getName());
    assertThat(TypeUtil.resolveTypeNameInPackage(TypeUtilTest.class, TypeUtilTest.class.getPackage().getName()))
        .as("normal type does not has FQN with package")
        .isEqualTo(TypeUtilTest.class.getSimpleName());
    assertThat(TypeUtil.resolveTypeNameInPackage(TypeUtilTest.class, String.class.getPackage().getName()))
        .as("normal type does not have FQN with other package")
        .isEqualTo(TypeUtilTest.class.getName());


    assertThat(TypeUtil.resolveTypeNameInPackage(Inner.class, null))
        .as("inner type has FQN without package")
        .isEqualTo(Inner.class.getName());
    assertThat(TypeUtil.resolveTypeNameInPackage(Inner.class, Inner.class.getPackage().getName()))
        .as("inner type does not have FQN with package")
        .isEqualTo(String.format("%s$%s", TypeUtilTest.class.getSimpleName(), Inner.class.getSimpleName()));
  }


  @Test
  public void isJavaLangType() throws Exception {
    assertThat(TypeUtil.isJavaLangType(Object.class)).isTrue();
    assertThat(TypeUtil.isJavaLangType(boolean.class)).isTrue();
    assertThat(TypeUtil.isJavaLangType(Boolean.class)).isTrue();

    // wrong
    assertThat(TypeUtil.isJavaLangType(TypeUtilTest.class)).isFalse();
  }

  @Test
  public void getAssertType() throws Exception {
    TypeToken<TypeUtilTest> thisType = TypeToken.of(TypeUtilTest.class);

    assertThat(TypeUtil.getAssertType(thisType, thisType.getRawType().getPackage().getName()))
        .as("resolves non-built-in type correctly")
        .isEqualTo("TypeUtilTestAssert");

    TypeToken<Boolean> primitive = TypeToken.of(boolean.class);
    assertThat(TypeUtil.getAssertType(primitive, thisType.getRawType().getPackage().getName()))
        .as("resolves primitive correctly")
        .isEqualTo(BooleanAssert.class.getName());

    TypeToken<Boolean> wrapper = TypeToken.of(Boolean.class);
    assertThat(TypeUtil.getAssertType(wrapper, thisType.getRawType().getPackage().getName()))
        .as("resolves primitive wrapper correctly")
        .isEqualTo(BooleanAssert.class.getName());

    assertThat(TypeUtil.getAssertType(wrapper, BooleanAssert.class.getPackage().getName()))
        .as("resolves package correctly for built-in package")
        .isEqualTo(BooleanAssert.class.getSimpleName());
  }

  @Test
  public void getTypeNameWithoutDots() throws Exception {
  }

  @Test
  public void isBoolean() throws Exception {
    TypeToken<Boolean> wrapper = new TypeToken<Boolean>(getClass()) {};
    TypeToken<Boolean> primitive = TypeToken.of(boolean.class);
    TypeToken<TypeUtilTest> neither = TypeToken.of(TypeUtilTest.class);

    assertThat(TypeUtil.isBoolean(wrapper)).as("for wrapper").isTrue();
    assertThat(TypeUtil.isBoolean(primitive)).as("for primitive").isTrue();
    assertThat(TypeUtil.isBoolean(neither)).as("for non-boolean").isFalse();
  }

  @Test
  public void isInnerPackageOf() throws Exception {

    assertThat(TypeUtil.isInnerPackageOf(TypeUtilTest.class.getPackage(),
        AssertionGeneratorTest.class.getPackage()))
        .as("from 'super' package")
        .isTrue();

    assertThat(TypeUtil.isInnerPackageOf(TypeUtilTest.class.getPackage(),
        TypeUtilTest.class.getPackage()))
        .as("same package")
        .isTrue();

    assertThat(TypeUtil.isInnerPackageOf(TypeUtilTest.class.getPackage(),
        GetterDescriptionTest.class.getPackage()))
        .as("sibling package")
        .isFalse();

  }

  @SuppressWarnings("unused")
  static class Foo<T> {
    List<T> listOfT;
    List<Foo<String>> listOfFooString;
    List<Foo<Integer>[]> listOfFooIntArr;
    List<Foo<int[][]>[]> listOfFooIntArrArrArr;
    Class<?> clazz;
    Class<Foo<?>> clazzFoo;
  }

  @Test
  public void create_generic_type_declaration() throws Exception {
    TypeToken<Foo<Integer>> fooInteger = new TypeToken<Foo<Integer>>(getClass()) {};
    String result = TypeUtil.getTypeDeclaration(fooInteger, false, true);
    String expected = String.format("%s.%s.%s<Integer>", TypeUtilTest.class.getPackage().getName(), TypeUtilTest.class.getSimpleName(), Foo.class.getSimpleName());
    assertThat(result).isEqualTo(expected);

    // nested!
    TypeToken<Foo<Foo<Integer>>> fooFooInteger = new TypeToken<Foo<Foo<Integer>>>(getClass()){};
    result = TypeUtil.getTypeDeclaration(fooFooInteger, false, false);
    expected = String.format("%s.%s<%s.%s<Integer>>", TypeUtilTest.class.getSimpleName(), Foo.class.getSimpleName(),
                                                      TypeUtilTest.class.getSimpleName(), Foo.class.getSimpleName());
    assertThat(result).isEqualTo(expected);

    // check getting the field's type
    Field listOfTField = Foo.class.getDeclaredField("listOfT");
    result = TypeUtil.getTypeDeclaration(fooFooInteger.resolveType(listOfTField.getGenericType()), false, false);
    expected = String.format("List<%s.%s<Integer>>", TypeUtilTest.class.getSimpleName(), Foo.class.getSimpleName());
    assertThat(result).isEqualTo(expected);

    result = TypeUtil.getTypeDeclaration(fooFooInteger.resolveType(listOfTField.getGenericType()), true, false);
    expected = String.format("List<? extends %s.%s<Integer>>", TypeUtilTest.class.getSimpleName(), Foo.class.getSimpleName());
    assertThat(result).isEqualTo(expected);

    // List of non-T type
    Field listOfFooStringField = Foo.class.getDeclaredField("listOfFooString");
    result = TypeUtil.getTypeDeclaration(fooInteger.resolveType(listOfFooStringField.getGenericType()), false, false);
    expected = String.format("List<%s.%s<String>>", TypeUtilTest.class.getSimpleName(), Foo.class.getSimpleName());
    assertThat(result).isEqualTo(expected);

    result = TypeUtil.getTypeDeclaration(fooInteger.resolveType(listOfFooStringField.getGenericType()), true, false);
    expected = String.format("List<? extends %s.%s<String>>", TypeUtilTest.class.getSimpleName(), Foo.class.getSimpleName());
    assertThat(result).isEqualTo(expected);

    // List of Foo<Integer>[]

    Field listOfFooIntArr = Foo.class.getDeclaredField("listOfFooIntArr");
    result = TypeUtil.getTypeDeclaration(fooInteger.resolveType(listOfFooIntArr.getGenericType()), false, false);
    expected = String.format("List<%s.%s<Integer>[]>", TypeUtilTest.class.getSimpleName(), Foo.class.getSimpleName());
    assertThat(result).isEqualTo(expected);

    // List of Foo<int[][]>[]
    Field listOfFooIntArrArrArr = Foo.class.getDeclaredField("listOfFooIntArrArrArr");
    result = TypeUtil.getTypeDeclaration(fooInteger.resolveType(listOfFooIntArrArrArr.getGenericType()), false, false);
    expected = String.format("List<%s.%s<int[][]>[]>", TypeUtilTest.class.getSimpleName(), Foo.class.getSimpleName());
    assertThat(result).isEqualTo(expected);
  }


  @Test
  public void create_wildcard_version() throws Exception {
    TypeToken<Foo<Integer>> fooInteger = new TypeToken<Foo<Integer>>(getClass()) {};
    Field clazz = Foo.class.getDeclaredField("clazz");
    String result = TypeUtil.getTypeDeclaration(fooInteger.resolveType(clazz.getGenericType()), false, true);
    String expected = "Class<?>";
    assertThat(result).isEqualTo(expected);

    result = TypeUtil.getTypeDeclaration(fooInteger.resolveType(clazz.getGenericType()), true, false);
    assertThat(result).isEqualTo(expected);

    Field clazzFoo = Foo.class.getDeclaredField("clazzFoo");
    result = TypeUtil.getTypeDeclaration(fooInteger.resolveType(clazzFoo.getGenericType()), true, false);
    expected = String.format("Class<? extends %s.%s<?>>", TypeUtilTest.class.getSimpleName(), Foo.class.getSimpleName());
    assertThat(result).isEqualTo(expected);

    result = TypeUtil.getTypeDeclaration(fooInteger.resolveType(clazzFoo.getGenericType()), false, false);
    expected = String.format("Class<%s.%s<?>>", TypeUtilTest.class.getSimpleName(), Foo.class.getSimpleName());
    assertThat(result).isEqualTo(expected);
  }


}