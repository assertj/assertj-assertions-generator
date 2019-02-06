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
package org.assertj.assertions.generator.util;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import org.assertj.assertions.generator.AssertionGeneratorTest;
import org.assertj.assertions.generator.NestedClassesTest;
import org.assertj.assertions.generator.data.*;
import org.assertj.assertions.generator.data.art.ArtWork;
import org.assertj.assertions.generator.data.lotr.FellowshipOfTheRing;
import org.assertj.assertions.generator.data.lotr.Race;
import org.assertj.assertions.generator.data.lotr.Ring;
import org.assertj.assertions.generator.data.lotr.TolkienCharacter;
import org.assertj.assertions.generator.data.nba.Player;
import org.assertj.assertions.generator.data.nba.PlayerAgent;
import org.assertj.assertions.generator.description.GetterDescriptionTest;
import org.assertj.assertions.generator.description.Visibility;
import org.assertj.core.api.BooleanAssert;
import org.junit.Test;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;
import static org.assertj.assertions.generator.util.ClassUtil.*;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Theories.class)
public class ClassUtilTest implements NestedClassesTest {

  private static final Class<?>[] NO_PARAMS = new Class[0];

  private static final TypeToken<Player> PLAYER_TYPE = TypeToken.of(Player.class);

  @Test
  public void should_get_class_only() {
    assertThat(collectClasses(getClass().getClassLoader(), Movie.class.getName())).containsOnly(TypeToken.of(Movie.class));
  }

  @Test(expected = IllegalArgumentException.class)
  public void should_throw_exception_when_classLoader_null() {
    collectClasses((ClassLoader) null, "org.assertj.assertions.generator.data");
  }

  // Could easily be a method reference in Java 8 for TypeToken::of
  private static final Function<Class<?>, TypeToken<?>> TYPE_TOKEN_TRANSFORM = new Function<Class<?>, TypeToken<?>>() {
    @Override
    public TypeToken<?> apply(final Class<?> input) {
      return TypeToken.of(input);
    }
  };

  @Test
  public void should_get_classes_in_package_and_subpackages() {
    Set<TypeToken<?>> classesInPackage = collectClasses("org.assertj.assertions.generator.data");
    List<Class<?>> classes = asList(Player.class,
                                    PlayerAgent.class,
                                    ArtWork.class,
                                    Name.class,
                                    Movie.class,
                                    Movie.PublicCategory.class,
                                    Ring.class,
                                    Race.class,
                                    FellowshipOfTheRing.class,
                                    TolkienCharacter.class,
                                    Team.class,
                                    Dollar$.class,
                                    org.assertj.assertions.generator.data.nba.team.Team.class,
                                    TreeEnum.class,
                                    OuterClass.InnerPerson.IP_InnerPerson.class,
                                    OuterClass.InnerPerson.class,
                                    OuterClass.class,
                                    OuterClass.StaticNestedPerson.SNP_InnerPerson.class,
                                    OuterClass.StaticNestedPerson.class,
                                    OuterClass.StaticNestedPerson.SNP_StaticNestedPerson.class,
                                    BeanWithOneException.class,
                                    BeanWithTwoExceptions.class);

    // Java 8? :(
    assertThat(classesInPackage).containsAll(Lists.transform(classes, TYPE_TOKEN_TRANSFORM));
  }

  @Test
  public void should_get_private_classes_when_included() {
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    Set<TypeToken<?>> classesInPackage = collectClasses(classLoader,
            true, "org.assertj.assertions.generator.data",
            "org.assertj.assertions.generator.util.PackagePrivate");
    List<Class<?>> classes = asList(Player.class, ArtWork.class, Name.class, Movie.class, PackagePrivate.class, Ring.class, Race.class);

    assertThat(classesInPackage).containsAll(Lists.transform(classes, TYPE_TOKEN_TRANSFORM));
  }

  @Test
  public void should_get_classes_with_provided_class_loader() {
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    Set<TypeToken<?>> classesInPackage = collectClasses(classLoader, "org.assertj.assertions.generator.data");
    List<Class<?>> classes = asList(Player.class, ArtWork.class, Name.class, Movie.class, Ring.class, Race.class);

    assertThat(classesInPackage).containsAll(Lists.transform(classes, TYPE_TOKEN_TRANSFORM));
  }

  @Test
  public void should_return_empty_collection_if_package_does_not_exist() {
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    assertThat(collectClasses(classLoader, "fakepackage")).isEmpty();
    assertThat(collectClasses("fakepackage")).isEmpty();
  }

  @Test
  public void should_return_property_of_getter_method() throws Exception {
    assertThat(propertyNameOf(Player.class.getMethod("getTeam", NO_PARAMS))).isEqualTo("team");
    assertThat(propertyNameOf(Player.class.getMethod("isRookie", NO_PARAMS))).isEqualTo("rookie");
    assertThat(propertyNameOf(EnemyReport.class.getMethod("getTarget"))).isEqualTo("target");
  }

  @Test
  public void should_return_true_if_class_implements_iterable_interface() {
    assertThat(inheritsCollectionOrIsIterable(Iterable.class)).isTrue();
    assertThat(inheritsCollectionOrIsIterable(Collection.class)).isTrue();
    assertThat(inheritsCollectionOrIsIterable(List.class)).isTrue();
    assertThat(inheritsCollectionOrIsIterable(String.class)).isFalse();
  }

  @Test
  public void should_return_true_if_method_is_a_standard_getter() throws Exception {
    assertThat(isStandardGetter(Player.class.getMethod("getTeam", NO_PARAMS))).isTrue();
  }

  @Test
  public void should_return_false_if_method_is_not_a_standard_getter() throws Exception {
    assertThat(isStandardGetter(Player.class.getMethod("isRookie", NO_PARAMS))).isFalse();
    assertThat(isStandardGetter(Player.class.getMethod("getVoid", NO_PARAMS))).isFalse();
    assertThat(isStandardGetter(Player.class.getMethod("getWithParam", String.class))).isFalse();
  }

  @Test
  public void should_return_true_if_method_is_a_boolean_getter() throws Exception {
    assertThat(isPredicate(Player.class.getMethod("isRookie", NO_PARAMS))).isTrue();
    assertThat(isPredicate(Primitives.class.getMethod("isBoolean", NO_PARAMS))).isTrue();
    assertThat(isPredicate(Primitives.class.getMethod("isBooleanWrapper", NO_PARAMS))).isTrue();
  }

  @Test
  public void should_return_false_if_method_is_not_a_boolean_getter() throws Exception {
    assertThat(isPredicate(Player.class.getMethod("getTeam", NO_PARAMS))).isFalse();
    assertThat(isPredicate(Player.class.getMethod("isVoid", NO_PARAMS))).isFalse();
    assertThat(isPredicate(Player.class.getMethod("isWithParam", String.class))).isFalse();
  }

  @Test
  public void should_return_negative_predicate() {
    for (String[] pair : new String[][]{
        {"isADog", "isNotADog"},
        {"canRun", "cannotRun"},
        {"hasAHandle", "doesNotHaveAHandle"},
    }) {
      assertThat(getNegativePredicateFor(pair[0])).as(pair[0]).isEqualTo(pair[1]);
      assertThat(getNegativePredicateFor(pair[1])).as(pair[1]).isEqualTo(pair[0]);
    }
  }

  @Test
  public void should_return_true_if_string_follows_getter_name_pattern() throws Exception {
    for (String name : new String[]{"isRookie", "getTeam", "wasTeam", "canRun", "shouldWin",
        "hasTrophy", "doesNotHaveFun", "cannotWin", "shouldNotPlay"}) {
      assertThat(isValidGetterName(name)).as(name).isTrue();
    }
  }

  @Test
  public void should_return_predicate_prefix() throws Exception {
    assertThat(getPredicatePrefix("isRookie")).isEqualTo("is");
    assertThat(getPredicatePrefix("wasTeam")).isEqualTo("was");
    assertThat(getPredicatePrefix("canRun")).isEqualTo("can");
    assertThat(getPredicatePrefix("shouldWin")).isEqualTo("should");
    assertThat(getPredicatePrefix("hasTrophy")).isEqualTo("has");
    assertThat(getPredicatePrefix("doesNotHaveFun")).isEqualTo("doesNotHave");
    assertThat(getPredicatePrefix("cannotWin")).isEqualTo("cannot");
    assertThat(getPredicatePrefix("shouldNotPlay")).isEqualTo("shouldNot");
  }

  @Test
  public void should_return_false_if_string_does_not_follow_getter_name_pattern() throws Exception {
    for (String name : new String[]{"isrookie", "getteam", "GetTeam", "get", "is", "wascool", "hastRophy",
        "shouldnotWin"}) {
      assertThat(isValidGetterName(name)).as(name).isFalse();
    }
  }

  @Test
  public void should_return_getters_methods_only() throws Exception {
    Set<Method> playerGetterMethods = getterMethodsOf(PLAYER_TYPE, Collections.<Class<?>>emptySet());
    assertThat(playerGetterMethods).contains(Player.class.getMethod("getTeam", NO_PARAMS))
                                   .doesNotContain(Player.class.getMethod("isInTeam", String.class));
  }

  @Test
  public void should_also_return_inherited_getters_methods() throws Exception {
    Set<Method> playerGetterMethods = getterMethodsOf(TypeToken.of(Movie.class), Collections.<Class<?>>emptySet());
    assertThat(playerGetterMethods).contains(Movie.class.getMethod("getReleaseDate", NO_PARAMS),
                                             ArtWork.class.getMethod("getTitle", NO_PARAMS));
  }

  @Test
  public void should_not_return_inherited_getters_methods() throws Exception {
    Set<Method> playerGetterMethods = declaredGetterMethodsOf(TypeToken.of(Movie.class), Collections.<Class<?>>emptySet());
    assertThat(playerGetterMethods).contains(Movie.class.getMethod("getReleaseDate", NO_PARAMS))
                                   .doesNotContain(ArtWork.class.getMethod("getTitle", NO_PARAMS));
  }

  @Theory
  public void should_return_inner_class_name_with_outer_class_name(NestedClass nestedClass) {
    String actualName = getSimpleNameWithOuterClass(nestedClass.nestedClass);
    assertThat(actualName).isEqualTo(nestedClass.classNameWithOuterClass);
  }

  @Test
  public void testGetSimpleNameWithOuterClass_notNestedClass() throws Exception {
    assertThat(getSimpleNameWithOuterClass(String.class)).isEqualTo("String");
  }

  @Test
  public void getClass_on_parameterized_List_should_return_List_class() throws Exception {
    Method method = Generic.class.getMethod("getListOfInteger");
    Class<?> clazz = ClassUtil.getClass(method.getGenericReturnType());
    assertThat(clazz).isEqualTo(List.class);
  }

  @Test
  public void getClass_on_parameterized_List_should_return_Integer_class() throws Exception {
    Method method = Generic.class.getMethod("getListOfInteger");
    Class<?> clazz = ClassUtil.getClass(((ParameterizedType) method.getGenericReturnType()).getActualTypeArguments()[0]);
    assertThat(clazz).isEqualTo(Integer.class);
  }

  @Test
  public void getClass_on_wildcard_List_should_return_Integer_class() throws Exception {
    Method method = Generic.class.getMethod("getListOfWildcardInteger");
    Class<?> clazz = ClassUtil.getClass(((ParameterizedType) method.getGenericReturnType()).getActualTypeArguments()[0]);
    assertThat(clazz).isEqualTo(Integer.class);
  }

  @Test
  public void getClass_on_variable_type_should_return_null() throws Exception {
    Method method = Generic.class.getMethod("getGenericArray");
    Class<?> clazz = ClassUtil.getClass((method.getGenericReturnType()));
    assertThat(clazz).isEqualTo(Object[].class);
  }

  @Test
  public void getClassRelatedTo_on_non_generic_type_should_return_given_type() throws Exception {
    Set<Class<?>> classes = getClassesRelatedTo(String.class.getMethod("toString").getReturnType());
    assertThat(classes).containsOnly(String.class);
  }

  @Test
  public void getClassRelatedTo_on_generic_list_should_return_list_and_component_type() throws Exception {
    Method method = Generic.class.getMethod("getListOfInteger");
    Set<Class<?>> classes = getClassesRelatedTo(method.getGenericReturnType());
    assertThat(classes).containsOnly(Integer.class, List.class);
  }

  @Test
  public void getClass_on_generic_should_return_Number_class() throws Exception {
    Method method = Generic.class.getMethod("getNumber");
    Class<?> classes = ClassUtil.getClass(method.getGenericReturnType());
    assertThat(classes).isEqualTo(Number.class);
  }

  public static class Inner {
  }

  @Test
  public void resolve_type_name_in_package() throws Exception {
    assertThat(resolveTypeNameInPackage(String.class, String.class.getPackage().getName()))
        .as("java lang type has simple name with package")
        .isEqualTo(String.class.getSimpleName());
    assertThat(resolveTypeNameInPackage(String.class, null))
        .as("java lang type has simple name without package")
        .isEqualTo(String.class.getSimpleName());

    assertThat(resolveTypeNameInPackage(ClassUtilTest.class, null))
        .as("normal type has FQN without package")
        .isEqualTo(ClassUtilTest.class.getName());
    assertThat(resolveTypeNameInPackage(ClassUtilTest.class, ClassUtilTest.class.getPackage().getName()))
        .as("normal type does not has FQN with package")
        .isEqualTo(ClassUtilTest.class.getSimpleName());
    assertThat(resolveTypeNameInPackage(ClassUtilTest.class, String.class.getPackage().getName()))
        .as("normal type does not have FQN with other package")
        .isEqualTo(ClassUtilTest.class.getName());

    assertThat(resolveTypeNameInPackage(Inner.class, null))
        .as("inner type has FQN without package")
        .isEqualTo(Inner.class.getName());
    assertThat(resolveTypeNameInPackage(Inner.class, Inner.class.getPackage().getName()))
        .as("inner type does not have FQN with package")
        .isEqualTo(String.format("%s$%s", ClassUtilTest.class.getSimpleName(), Inner.class.getSimpleName()));
  }

  @Test
  public void java_lang_types_should_work_with_isJavaLangType() throws Exception {
    assertThat(isJavaLangType(Object.class)).isTrue();
    assertThat(isJavaLangType(boolean.class)).isTrue();
    assertThat(isJavaLangType(Boolean.class)).isTrue();

    // wrong
    assertThat(isJavaLangType(ClassUtilTest.class)).isFalse();
  }

  @Test
  public void can_properly_compute_Assert_types() throws Exception {
    TypeToken<ClassUtilTest> thisType = TypeToken.of(ClassUtilTest.class);

    assertThat(getAssertType(thisType, thisType.getRawType().getPackage().getName()))
        .as("resolves non-built-in type correctly")
        .isEqualTo("ClassUtilTestAssert");

    TypeToken<Boolean> primitive = TypeToken.of(boolean.class);
    assertThat(getAssertType(primitive, thisType.getRawType().getPackage().getName()))
        .as("resolves primitive correctly")
        .isEqualTo(BooleanAssert.class.getName());

    TypeToken<Boolean> wrapper = TypeToken.of(Boolean.class);
    assertThat(getAssertType(wrapper, thisType.getRawType().getPackage().getName()))
        .as("resolves primitive wrapper correctly")
        .isEqualTo(BooleanAssert.class.getName());

    assertThat(getAssertType(wrapper, BooleanAssert.class.getPackage().getName()))
        .as("resolves package correctly for built-in package")
        .isEqualTo(BooleanAssert.class.getSimpleName());
  }

  @Test
  public void properly_check_if_types_are_boolean() throws Exception {
    TypeToken<Boolean> wrapper = new TypeToken<Boolean>(getClass()) {
    };
    TypeToken<Boolean> primitive = TypeToken.of(boolean.class);
    TypeToken<ClassUtilTest> neither = TypeToken.of(ClassUtilTest.class);

    assertThat(isBoolean(wrapper)).as("for wrapper").isTrue();
    assertThat(isBoolean(primitive)).as("for primitive").isTrue();
    assertThat(isBoolean(neither)).as("for non-boolean").isFalse();
  }

  @Test
  public void should_check_that_nested_packages_work() throws Exception {

    assertThat(isInnerPackageOf(ClassUtilTest.class.getPackage(), AssertionGeneratorTest.class.getPackage()))
        .as("from 'super' package")
        .isTrue();

    assertThat(isInnerPackageOf(ClassUtilTest.class.getPackage(), ClassUtilTest.class.getPackage()))
        .as("same package")
        .isTrue();

    assertThat(isInnerPackageOf(ClassUtilTest.class.getPackage(), GetterDescriptionTest.class.getPackage()))
        .as("sibling package")
        .isFalse();

  }

  @Test
  public void should_return_all_fields_in_hierarchy_except_Object_fields() throws Exception {
    List<Field> fields = getAllFieldsInHierarchy(TypeToken.of(WithPrivateFields.class));
    assertThat(fields).extracting("name")
                      .contains("age", "name", "address", "country", "nickname", "city");
  }

  @Test
  public void should_determine_fields_visibility() throws Exception {
    List<Field> fields = ClassUtil.declaredFieldsOf(TypeToken.of(WithPrivateFields.class));
    List<Visibility> fieldsVisibility = new ArrayList<>();
    for (Field field : fields) fieldsVisibility.add(visibilityOf(field));
    assertThat(fieldsVisibility).containsOnly(Visibility.PUBLIC,
                                              Visibility.PACKAGE,
                                              Visibility.PRIVATE,
                                              Visibility.PROTECTED);
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

  @SuppressWarnings("unused")
  private static class Generic {
    public List<Integer> getListOfInteger() {
      return null;
    }

    public <T> T[] getGenericArray() {
      return null;
    }

    public List<? extends Integer> getListOfWildcardInteger() {
      return null;
    }

    public <T extends Number> T getNumber() {
      return null;
    }
  }
}
