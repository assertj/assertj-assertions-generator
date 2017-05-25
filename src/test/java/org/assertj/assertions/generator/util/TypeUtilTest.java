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
 * Copyright 2012-2015 the original author or authors.
 */
package org.assertj.assertions.generator.util;

import com.google.common.reflect.TypeToken;
import org.assertj.assertions.generator.AssertionGeneratorTest;
import org.assertj.assertions.generator.description.GetterDescriptionTest;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.assertj.core.api.Java6Assertions.assertThat;

/**
 * Describes tests for the {@link TypeUtil} class
 */
public class TypeUtilTest {
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

    // List of non-T type
    Field listOfFooStringField = Foo.class.getDeclaredField("listOfFooString");
    result = TypeUtil.getTypeDeclaration(fooFooInteger.resolveType(listOfFooStringField.getGenericType()), false, false);
    expected = String.format("List<%s.%s<String>>", TypeUtilTest.class.getSimpleName(), Foo.class.getSimpleName());
    assertThat(result).isEqualTo(expected);

    // List of Foo<Integer>[]

    Field listOfFooIntArr = Foo.class.getDeclaredField("listOfFooIntArr");
    result = TypeUtil.getTypeDeclaration(fooFooInteger.resolveType(listOfFooIntArr.getGenericType()), false, false);
    expected = String.format("List<%s.%s<Integer>[]>", TypeUtilTest.class.getSimpleName(), Foo.class.getSimpleName());
    assertThat(result).isEqualTo(expected);

    // List of Foo<int[][]>[]
    Field listOfFooIntArrArrArr = Foo.class.getDeclaredField("listOfFooIntArrArrArr");
    result = TypeUtil.getTypeDeclaration(fooFooInteger.resolveType(listOfFooIntArrArrArr.getGenericType()), false, false);
    expected = String.format("List<%s.%s<int[][]>[]>", TypeUtilTest.class.getSimpleName(), Foo.class.getSimpleName());
    assertThat(result).isEqualTo(expected);
  }




}