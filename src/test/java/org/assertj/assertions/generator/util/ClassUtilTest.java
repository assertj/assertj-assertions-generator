package org.assertj.assertions.generator.util;

import org.assertj.assertions.generator.NestedClassesTest;
import org.assertj.assertions.generator.data.*;
import org.assertj.assertions.generator.data.lotr.FellowshipOfTheRing;
import org.assertj.assertions.generator.data.lotr.Race;
import org.assertj.assertions.generator.data.lotr.Ring;
import org.assertj.assertions.generator.data.lotr.TolkienCharacter;
import org.junit.Test;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

import static org.assertj.assertions.generator.data.OuterClass.StaticNestedPerson;
import static org.assertj.assertions.generator.util.ClassUtil.*;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Theories.class)
public class ClassUtilTest implements NestedClassesTest {

  private static final Class<?>[] NO_PARAMS = new Class[0];

  @Test
  public void should_get_class_only() {
    assertThat(ClassUtil.collectClasses(this.getClass().getClassLoader(), Movie.class.getName())).containsOnly(Movie.class);
  }

  @Test(expected = IllegalArgumentException.class)
  public void should_thow_exception_when_classLoader_null() {
    ClassUtil.collectClasses((ClassLoader) null, "org.assertj.assertions.generator.data");
  }

  @Test
  public void should_get_classes_in_package_and_subpackages() throws ClassNotFoundException {
    List<Class<?>> classesInPackage = collectClasses("org.assertj.assertions.generator.data");
    assertThat(classesInPackage).containsOnly(Player.class, ArtWork.class, Name.class, Movie.class,
                                              Movie.PublicCategory.class, Ring.class, Race.class,
                                              FellowshipOfTheRing.class, TolkienCharacter.class,
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
    assertThat(isStandardGetter(Player.class.getMethod("getWithParam", new Class[] { String.class }))).isFalse();
  }

  @Test
  public void should_return_true_if_method_is_a_boolean_getter() throws Exception {
    assertThat(isBooleanGetter(Player.class.getMethod("isRookie", NO_PARAMS))).isTrue();
  }
  
  @Test
  public void should_return_false_if_method_is_not_a_boolean_getter() throws Exception {
    assertThat(isBooleanGetter(Player.class.getMethod("getTeam", NO_PARAMS))).isFalse();
    assertThat(isStandardGetter(Player.class.getMethod("isVoid", NO_PARAMS))).isFalse();
    assertThat(isStandardGetter(Player.class.getMethod("isWithParam", new Class[] { String.class }))).isFalse();
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
                                   .doesNotContain(
                                           Player.class.getMethod("isInTeam", String.class));
  }

  @Test
  public void should_also_return_inherited_getters_methods() throws Exception {
    List<Method> playerGetterMethods = getterMethodsOf(Movie.class);
    assertThat(playerGetterMethods).contains(Movie.class.getMethod("getReleaseDate", NO_PARAMS),
            ArtWork.class.getMethod("getTitle", NO_PARAMS));
  }

  @Theory
  public void testGetSimpleNameWithOuterClass(NestedClass nestedClass) throws Exception {
    String actualName = ClassUtil.getSimpleNameWithOuterClass(nestedClass.getNestedClass());
    assertThat(actualName).isEqualTo(nestedClass.getClassNameWithOuterClass());
  }

  @Test
  public void testGetSimpleNameWithOuterClass_notNestedClass() throws Exception {
    assertThat(ClassUtil.getSimpleNameWithOuterClass(String.class)).isNull();
  }
}
