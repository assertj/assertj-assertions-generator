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

import static org.assertj.assertions.generator.util.ClassUtil.collectClasses;
import static org.assertj.assertions.generator.util.ClassUtil.declaredGetterMethodsOf;
import static org.assertj.assertions.generator.util.ClassUtil.getClassesRelatedTo;
import static org.assertj.assertions.generator.util.ClassUtil.getNegativePredicateFor;
import static org.assertj.assertions.generator.util.ClassUtil.getPredicatePrefix;
import static org.assertj.assertions.generator.util.ClassUtil.getSimpleNameOuterClass;
import static org.assertj.assertions.generator.util.ClassUtil.getSimpleNameWithOuterClass;
import static org.assertj.assertions.generator.util.ClassUtil.getSimpleNameWithOuterClassNotSeparatedByDots;
import static org.assertj.assertions.generator.util.ClassUtil.getterMethodsOf;
import static org.assertj.assertions.generator.util.ClassUtil.inheritsCollectionOrIsIterable;
import static org.assertj.assertions.generator.util.ClassUtil.isPredicate;
import static org.assertj.assertions.generator.util.ClassUtil.isStandardGetter;
import static org.assertj.assertions.generator.util.ClassUtil.isValidGetterName;
import static org.assertj.assertions.generator.util.ClassUtil.propertyNameOf;
import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.assertj.assertions.generator.NestedClassesTest;
import org.assertj.assertions.generator.data.ArtWork;
import org.assertj.assertions.generator.data.BeanWithOneException;
import org.assertj.assertions.generator.data.BeanWithTwoExceptions;
import org.assertj.assertions.generator.data.Dollar$;
import org.assertj.assertions.generator.data.Movie;
import org.assertj.assertions.generator.data.Name;
import org.assertj.assertions.generator.data.OuterClass;
import org.assertj.assertions.generator.data.OuterClass.StaticNestedPerson;
import org.assertj.assertions.generator.data.Primitives;
import org.assertj.assertions.generator.data.Team;
import org.assertj.assertions.generator.data.TreeEnum;
import org.assertj.assertions.generator.data.lotr.FellowshipOfTheRing;
import org.assertj.assertions.generator.data.lotr.Race;
import org.assertj.assertions.generator.data.lotr.Ring;
import org.assertj.assertions.generator.data.lotr.TolkienCharacter;
import org.assertj.assertions.generator.data.nba.Player;
import org.assertj.assertions.generator.data.nba.PlayerAgent;
import org.junit.Test;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

@RunWith(Theories.class)
public class ClassUtilTest implements NestedClassesTest {

  private static final Class<?>[] NO_PARAMS = new Class[0];

  @Test
  public void should_get_class_only() {
    assertThat(collectClasses(getClass().getClassLoader(), Movie.class.getName())).containsOnly(Movie.class);
  }

  @Test(expected = IllegalArgumentException.class)
  public void should_thow_exception_when_classLoader_null() {
    collectClasses((ClassLoader) null, "org.assertj.assertions.generator.data");
  }

  @Test
  public void should_get_classes_in_package_and_subpackages() {
    Set<Class<?>> classesInPackage = collectClasses("org.assertj.assertions.generator.data");
    assertThat(classesInPackage).contains(Player.class, PlayerAgent.class, ArtWork.class, Name.class, Movie.class,
                                          Movie.PublicCategory.class, Ring.class, Race.class,
                                          FellowshipOfTheRing.class, TolkienCharacter.class,
                                          Team.class,
                                          Dollar$.class,
                                          org.assertj.assertions.generator.data.nba.Team.class,
                                          TreeEnum.class,
                                          OuterClass.InnerPerson.IP_InnerPerson.class,
                                          OuterClass.InnerPerson.class,
                                          OuterClass.class,
                                          StaticNestedPerson.SNP_InnerPerson.class,
                                          StaticNestedPerson.class,
                                          StaticNestedPerson.SNP_StaticNestedPerson.class,
                                          BeanWithOneException.class, BeanWithTwoExceptions.class);
  }

  @Test
  public void should_get_classes_with_provided_class_loader() {
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    Set<Class<?>> classesInPackage = collectClasses(classLoader, "org.assertj.assertions.generator.data");
    assertThat(classesInPackage).contains(Player.class, ArtWork.class, Name.class, Movie.class, Ring.class, Race.class);
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
    assertThat(isStandardGetter(Player.class.getMethod("getWithParam", new Class[] { String.class }))).isFalse();
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
    assertThat(isPredicate(Player.class.getMethod("isWithParam", new Class[] { String.class }))).isFalse();
  }

  @Test
  public void should_return_negative_predicate() {
	for (String[] pair : new String[][] {
		{ "isADog", "isNotADog" },
		{ "canRun", "cannotRun" },
		{ "hasAHandle", "doesNotHaveAHandle" },
		}) {
	  assertThat(getNegativePredicateFor(pair[0])).as(pair[0]).isEqualTo(pair[1]);
	  assertThat(getNegativePredicateFor(pair[1])).as(pair[1]).isEqualTo(pair[0]);
	}
  }
  
  @Test
  public void should_return_true_if_string_follows_getter_name_pattern() throws Exception {
    for (String name : new String[] { "isRookie", "getTeam", "wasTeam", "canRun", "shouldWin",
        "hasTrophy", "doesNotHaveFun", "cannotWin", "shouldNotPlay" }) {
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
    for (String name : new String[] { "isrookie", "getteam", "GetTeam", "get", "is", "wascool", "hastRophy", "shouldnotWin" }) {
      assertThat(isValidGetterName(name)).as(name).isFalse();
    }
  }

  @Test
  public void should_return_getters_methods_only() throws Exception {
	Set<Method> playerGetterMethods = getterMethodsOf(Player.class, Collections.<Class<?>>emptySet());
    assertThat(playerGetterMethods).contains(Player.class.getMethod("getTeam", NO_PARAMS))
                                   .doesNotContain(Player.class.getMethod("isInTeam", String.class));
  }

  @Test
  public void should_also_return_inherited_getters_methods() throws Exception {
    Set<Method> playerGetterMethods = getterMethodsOf(Movie.class, Collections.<Class<?>>emptySet());
    assertThat(playerGetterMethods).contains(Movie.class.getMethod("getReleaseDate", NO_PARAMS),
                                             ArtWork.class.getMethod("getTitle", NO_PARAMS));
  }

  @Test
  public void should_not_return_inherited_getters_methods() throws Exception {
	Set<Method> playerGetterMethods = declaredGetterMethodsOf(Movie.class, Collections.<Class<?>>emptySet());
    assertThat(playerGetterMethods).contains(Movie.class.getMethod("getReleaseDate", NO_PARAMS))
                                   .doesNotContain(ArtWork.class.getMethod("getTitle", NO_PARAMS));
  }

  @Theory
  public void should_return_inner_class_name_with_outer_class_name(NestedClass nestedClass) {
    String actualName = getSimpleNameWithOuterClass(nestedClass.getNestedClass());
    assertThat(actualName).isEqualTo(nestedClass.getClassNameWithOuterClass());
  }

  @Theory
  public void should_return_outer_class_name_for_nested_class(NestedClass nestedClass) {
    String actualName = getSimpleNameOuterClass(nestedClass.getNestedClass());
    assertThat(actualName).isEqualTo(nestedClass.getOuterClassName());
  }


  @Theory
  public void should_return_inner_class_name_with_outer_class_name_not_separated_by_dots(NestedClass nestedClass) {
    String actualName = getSimpleNameWithOuterClassNotSeparatedByDots(nestedClass.getNestedClass());
    assertThat(actualName).isEqualTo(nestedClass.getClassNameWithOuterClassNotSeparatedBytDots());
  }

  @Test
  public void should_return_simple_class_name() {
    String actualName = getSimpleNameWithOuterClassNotSeparatedByDots(Player.class);
    assertThat(actualName).isEqualTo("Player");
  }

  @Test
  public void testGetSimpleNameWithOuterClass_notNestedClass() throws Exception {
    assertThat(ClassUtil.getSimpleNameWithOuterClass(String.class)).isEqualTo("String");
  }

  @Test
  public void testGetSimpleNameOuterClass_notNestedClass() throws Exception {
    assertThat(ClassUtil.getSimpleNameOuterClass(String.class)).isEqualTo("String");
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

  private static class Generic {
    @SuppressWarnings("unused")
    public List<Integer> getListOfInteger() {
      return null;
    }

    @SuppressWarnings("unused")
    public <T> T[] getGenericArray() {
      return null;
    }

    @SuppressWarnings("unused")
    public List<? extends Integer> getListOfWildcardInteger() {
      return null;
    }

    @SuppressWarnings("unused")
    public <T extends Number> T getNumber() {
      return null;
    }
  }
}
