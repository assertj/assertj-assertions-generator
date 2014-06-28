package org.assertj.assertions.generator.util;

import java.lang.reflect.ParameterizedType;

import org.assertj.assertions.generator.NestedClassesTest;
import org.assertj.assertions.generator.data.*;
import org.assertj.assertions.generator.data.lotr.FellowshipOfTheRing;
import org.assertj.assertions.generator.data.lotr.Race;
import org.assertj.assertions.generator.data.lotr.Ring;
import org.assertj.assertions.generator.data.lotr.TolkienCharacter;
import org.assertj.assertions.generator.data.nba.Player;
import org.junit.Test;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.assertj.assertions.generator.data.OuterClass.StaticNestedPerson;
import static org.assertj.assertions.generator.util.ClassUtil.*;
import static org.assertj.core.api.Assertions.assertThat;

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
  public void should_get_classes_in_package_and_subpackages() throws ClassNotFoundException {
    List<Class<?>> classesInPackage = collectClasses("org.assertj.assertions.generator.data");
    assertThat(classesInPackage).containsOnly(Player.class, ArtWork.class, Name.class, Movie.class,
                                              Movie.PublicCategory.class, Ring.class, Race.class,
                                              FellowshipOfTheRing.class, TolkienCharacter.class,
                                              Team.class,
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
  public void should_get_classes_with_provided_class_loader() throws ClassNotFoundException {
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    List<Class<?>> classesInPackage = collectClasses(classLoader, "org.assertj.assertions.generator.data");
    assertThat(classesInPackage).contains(Player.class, ArtWork.class, Name.class, Movie.class, Ring.class, Race.class);
  }

  @Test
  public void should_return_empty_collection_if_package_does_not_exist() throws ClassNotFoundException {
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
    assertThat(isIterable(Iterable.class)).isTrue();
    assertThat(isIterable(Collection.class)).isTrue();
    assertThat(isIterable(List.class)).isTrue();
    assertThat(isIterable(String.class)).isFalse();
  }

  @Test
  public void should_return_true_if_method_is_a_standard_getter() throws Exception {
    assertThat(isStandardGetter(Player.class.getMethod("getTeam", NO_PARAMS))).isTrue();
  }

  @Test
  public void should_return_false_if_method_is_not_a_standard_getter() throws Exception {
    assertThat(isStandardGetter(Player.class.getMethod("isRookie", NO_PARAMS))).isFalse();
    assertThat(isStandardGetter(Player.class.getMethod("getVoid", NO_PARAMS))).isFalse();
    assertThat(isStandardGetter(Player.class.getMethod("getWithParam", new Class[]{String.class}))).isFalse();
  }

  @Test
  public void should_return_true_if_method_is_a_boolean_getter() throws Exception {
    assertThat(isBooleanGetter(Player.class.getMethod("isRookie", NO_PARAMS))).isTrue();
  }

  @Test
  public void should_return_false_if_method_is_not_a_boolean_getter() throws Exception {
    assertThat(isBooleanGetter(Player.class.getMethod("getTeam", NO_PARAMS))).isFalse();
    assertThat(isStandardGetter(Player.class.getMethod("isVoid", NO_PARAMS))).isFalse();
    assertThat(isStandardGetter(Player.class.getMethod("isWithParam", new Class[]{String.class}))).isFalse();
  }

  @Test
  public void should_return_true_if_string_follows_getter_name_pattern() throws Exception {
    assertThat(isValidGetterName("isRookie")).isTrue();
    assertThat(isValidGetterName("getTeam")).isTrue();
  }

  @Test
  public void should_return_false_if_string_follows_getter_name_pattern() throws Exception {
    assertThat(isValidGetterName("isrookie")).isFalse();
    assertThat(isValidGetterName("get")).isFalse();
  }

  @Test
  public void should_return_false_if_string_does_not_follow_getter_name_pattern() throws Exception {
    assertThat(isValidGetterName("isrookie")).isFalse();
    assertThat(isValidGetterName("getteam")).isFalse();
    assertThat(isValidGetterName("GetTeam")).isFalse();
    assertThat(isValidGetterName("get")).isFalse();
    assertThat(isValidGetterName("is")).isFalse();
  }

  @Test
  public void should_return_getters_methods_only() throws Exception {
    List<Method> playerGetterMethods = getterMethodsOf(Player.class);
    assertThat(playerGetterMethods).contains(Player.class.getMethod("getTeam", NO_PARAMS))
      .doesNotContain(Player.class.getMethod("isInTeam", String.class));
  }

  @Test
  public void should_also_return_inherited_getters_methods() throws Exception {
    List<Method> playerGetterMethods = getterMethodsOf(Movie.class);
    assertThat(playerGetterMethods).contains(Movie.class.getMethod("getReleaseDate", NO_PARAMS),
                                             ArtWork.class.getMethod("getTitle", NO_PARAMS));
  }

  @Test
  public void should_not_return_inherited_getters_methods() throws Exception {
    List<Method> playerGetterMethods = declaredGetterMethodsOf(Movie.class);
    assertThat(playerGetterMethods).contains(Movie.class.getMethod("getReleaseDate", NO_PARAMS))
                                   .doesNotContain(ArtWork.class.getMethod("getTitle", NO_PARAMS));
  }

  @Theory
  public void should_return_inner_class_name_with_outer_class_name(NestedClass nestedClass) {
    String actualName = getSimpleNameWithOuterClass(nestedClass.getNestedClass());
    assertThat(actualName).isEqualTo(nestedClass.getClassNameWithOuterClass());
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
    assertThat(clazz).isNull();
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
  }
}
