package org.fest.assertions.generator.util;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.generator.util.ClassUtil.*;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

import org.junit.Test;

import org.fest.assertions.generator.data.ArtWork;
import org.fest.assertions.generator.data.Movie;
import org.fest.assertions.generator.data.Name;
import org.fest.assertions.generator.data.Player;
import org.fest.assertions.generator.data.lotr.Race;
import org.fest.assertions.generator.data.lotr.Ring;

public class ClassUtilTest {

  public static final String TEST_PACKAGE_NAME = "org.fest.assertions.generator.data";
  private static final Class[] EXPECTED_CLASSES_IN_TEST_PACKAGE = new Class[]{Player.class, ArtWork.class, Name.class, Movie.class, Ring.class, Race.class};
  private static final Class<?>[] NO_PARAMS = new Class[0];
  private Method method;

  @Test
  public void should_get_classes_in_package_and_subpackages() throws ClassNotFoundException {
    List<Class<?>> classesInPackage = getClassesInPackage("org.fest.assertions.generator.data");
    assertThat(classesInPackage).contains(Player.class, ArtWork.class, Name.class, Movie.class, Ring.class, Race.class);
  }

  @Test
  public void should_get_classes_with_provided_class_loader() throws ClassNotFoundException {
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    List<Class<?>> classesInPackage = collectClasses(classLoader, "org.fest.assertions.generator.data");
    assertThat(classesInPackage).contains(Player.class, ArtWork.class, Name.class, Movie.class, Ring.class, Race.class);
  }

  @Test
  public void should_return_property_of_getter_method() throws Exception {
    method = Player.class.getMethod("getTeam", NO_PARAMS);
    assertThat(propertyNameOf(method)).isEqualTo("team");

    method = Player.class.getMethod("isRookie", NO_PARAMS);
    assertThat(propertyNameOf(method)).isEqualTo("rookie");
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
    method = Player.class.getMethod("getTeam", NO_PARAMS);
    assertThat(isStandardGetter(method)).isTrue();

    method = Player.class.getMethod("isRookie", NO_PARAMS);
    assertThat(isStandardGetter(method)).isFalse();
  }

  @Test
  public void should_return_true_if_method_is_a_boolean_getter() throws Exception {
    method = Player.class.getMethod("getTeam", NO_PARAMS);
    assertThat(isBooleanGetter(method)).isFalse();

    method = Player.class.getMethod("isRookie", NO_PARAMS);
    assertThat(isBooleanGetter(method)).isTrue();
  }

  @Test
  public void should_return_true_if_string_follows_getter_name_pattern() throws Exception {
    assertThat(isValidGetterName("isRookie")).isTrue();
    assertThat(isValidGetterName("getTeam")).isTrue();
  }

  @Test
  public void should_return_false_if_string_does_not_follow_getter_name_pattern() throws Exception {
    assertThat(isValidGetterName("isrookie")).isFalse();
    assertThat(isValidGetterName("getteam")).isFalse();
    assertThat(isValidGetterName("GetTeam")).isFalse();
  }

  @Test
  public void should_return_getters_methods_only() throws Exception {
    List<Method> playerGetterMethods = getterMethodsOf(Player.class);
    assertThat(playerGetterMethods).contains(Player.class.getMethod("getTeam", NO_PARAMS))
        .doesNotContain(Player.class.getMethod("isInTeam", String.class));
  }

}
