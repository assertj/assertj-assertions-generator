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
 * Copyright 2012-2021 the original author or authors.
 */
package org.assertj.assertions.generator.description.converter;

import com.google.common.reflect.TypeToken;
import org.assertj.assertions.generator.BeanWithExceptionsTest;
import org.assertj.assertions.generator.NestedClassesTest;
import org.assertj.assertions.generator.data.Movie;
import org.assertj.assertions.generator.data.Name;
import org.assertj.assertions.generator.data.Team;
import org.assertj.assertions.generator.data.TreeEnum;
import org.assertj.assertions.generator.data.WithPrivateFields;
import org.assertj.assertions.generator.data.WithPrivateFieldsParent;
import org.assertj.assertions.generator.data.art.ArtWork;
import org.assertj.assertions.generator.data.lotr.FellowshipOfTheRing;
import org.assertj.assertions.generator.data.nba.Player;
import org.assertj.assertions.generator.data.nba.PlayerAgent;
import org.assertj.assertions.generator.description.ClassDescription;
import org.assertj.assertions.generator.description.GetterDescription;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.assertions.generator.util.ClassUtil.getTypeDeclaration;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Theories.class)
public class ClassToClassDescriptionConverterTest implements NestedClassesTest, BeanWithExceptionsTest {
  private static ClassToClassDescriptionConverter converter;

  @BeforeClass
  public static void beforeAllTests() {
    converter = new ClassToClassDescriptionConverter();
  }

  @Test
  public void should_build_player_class_description() throws Exception {
    // Given
    Class<?> clazz = Player.class;
    // When
    ClassDescription classDescription = converter.convertToClassDescription(clazz);
    // Then
    assertThat(classDescription.getClassNameWithOuterClass()).isEqualTo("Player");
    assertThat(classDescription.getPackageName()).isEqualTo("org.assertj.assertions.generator.data.nba");
    assertThat(classDescription.getFullyQualifiedClassName()).isEqualTo("org.assertj.assertions.generator.data.nba.Player");
    assertThat(classDescription.getFullyQualifiedOuterClassName()).isEqualTo("org.assertj.assertions.generator.data.nba.Player");
    assertThat(classDescription.getFullyQualifiedClassNameWithoutGenerics()).isEqualTo(classDescription.getFullyQualifiedClassName());
    assertThat(classDescription.getGettersDescriptions()).hasSize(20);
    assertThat(classDescription.getAssertClassName()).isEqualTo("PlayerAssert");
    assertThat(classDescription.getAssertClassFilename()).isEqualTo("PlayerAssert.java");
    assertThat(classDescription.getFullyQualifiedAssertClassName()).isEqualTo("org.assertj.assertions.generator.data.nba.PlayerAssert");
    assertThat(classDescription.getAbstractAssertClassName()).isEqualTo("AbstractPlayerAssert");
    assertThat(classDescription.getAbstractAssertClassFilename()).isEqualTo("AbstractPlayerAssert.java");
    assertThat(classDescription.getFullyQualifiedParentAssertClassName()).isEqualTo("org.assertj.core.api.AbstractObjectAssert");
  }

  @Test
  public void should_build_movie_class_description() throws Exception {
    // Given
    Class<?> clazz = Movie.class;
    // When
    ClassDescription classDescription = converter.convertToClassDescription(clazz);
    // Then
    assertThat(classDescription.getClassNameWithOuterClass()).isEqualTo("Movie");
    assertThat(classDescription.getPackageName()).isEqualTo("org.assertj.assertions.generator.data");
    assertThat(classDescription.getGettersDescriptions()).hasSize(3);
    assertThat(classDescription.getFieldsDescriptions()).hasSize(7);
    assertThat(classDescription.getDeclaredGettersDescriptions()).hasSize(2);
    assertThat(classDescription.getDeclaredFieldsDescriptions()).hasSize(5);
    assertThat(classDescription.getSuperType()).isEqualTo(TypeToken.of(ArtWork.class));
    assertThat(classDescription.getAssertClassName()).isEqualTo("MovieAssert");
    assertThat(classDescription.getAssertClassFilename()).isEqualTo("MovieAssert.java");
    assertThat(classDescription.getFullyQualifiedAssertClassName()).isEqualTo("org.assertj.assertions.generator.data.MovieAssert");
    assertThat(classDescription.getAbstractAssertClassName()).isEqualTo("AbstractMovieAssert");
    assertThat(classDescription.getAbstractAssertClassFilename()).isEqualTo("AbstractMovieAssert.java");
    assertThat(classDescription.getFullyQualifiedParentAssertClassName())
        .isEqualTo("org.assertj.assertions.generator.data.art.AbstractArtWorkAssert");
    assertThat(classDescription.implementsComparable()).as("implementsComparable ? ").isFalse();
  }

  @Test
  public void should_build_comparable_class_description() throws Exception {
    // Given
    Class<?> clazz = Name.class;
    // When
    ClassDescription classDescription = converter.convertToClassDescription(clazz);
    // Then
    assertThat(classDescription.getClassNameWithOuterClass()).isEqualTo("Name");
    assertThat(classDescription.implementsComparable()).as("implementsComparable ? ").isTrue();
  }

  @Test
  public void should_build_WithPrivateFields_class_description() throws Exception {
    ClassDescription classDescription = converter.convertToClassDescription(WithPrivateFields.class);
    assertThat(classDescription.getClassNameWithOuterClass()).isEqualTo("WithPrivateFields");
    assertThat(classDescription.getPackageName()).isEqualTo("org.assertj.assertions.generator.data");
    assertThat(classDescription.getFieldsDescriptions()).hasSize(8);
    assertThat(classDescription.getGettersDescriptions()).hasSize(2);
    assertThat(classDescription.getDeclaredGettersDescriptions()).hasSize(1);
    assertThat(classDescription.getDeclaredFieldsDescriptions()).hasSize(5);
    assertThat(classDescription.getSuperType()).isEqualTo(TypeToken.of(WithPrivateFieldsParent.class));
  }

  @Theory
  public void should_build_nested_class_description(NestedClass nestedClass) throws Exception {
    // Given
    Class<?> clazz = nestedClass.nestedClass;
    // When
    ClassDescription classDescription = converter.convertToClassDescription(clazz);
    // Then
    assertThat(classDescription.getClassNameWithOuterClass()).isEqualTo(nestedClass.classNameWithOuterClass);
    assertThat(classDescription.getFullyQualifiedOuterClassName()).isEqualTo(nestedClass.fullyQualifiedOuterClassName);
    assertThat(classDescription.getAssertClassName()).isEqualTo(nestedClass.assertClassName);
    assertThat(classDescription.getAssertClassFilename()).isEqualTo(nestedClass.assertClassFilename);
    assertThat(classDescription.getAbstractAssertClassName()).isEqualTo(nestedClass.abstractAssertClassName);
    assertThat(classDescription.getAbstractAssertClassFilename()).isEqualTo(nestedClass.abstractAssertClassFilename);
    assertThat(classDescription.getPackageName()).isEqualTo(clazz.getPackage().getName());
    assertThat(classDescription.getGettersDescriptions()).hasSize(1);
  }

  @Theory
  public void should_build_getter_with_exception_description(GetterWithException getter) throws Exception {
    // Given
    TypeToken<?> type = getter.getBeanClass();
    Class<?> clazz = type.getRawType();
    // When
    ClassDescription classDescription = converter.convertToClassDescription(clazz);
    // Then
    assertThat(classDescription.getClassNameWithOuterClass()).isEqualTo(clazz.getSimpleName());
    assertThat(classDescription.getPackageName()).isEqualTo(clazz.getPackage().getName());
    assertThat(classDescription.getGettersDescriptions()).hasSize(4);
    for (GetterDescription desc : classDescription.getGettersDescriptions()) {
      if (desc.getName().equals(getter.getPropertyName())) {
        assertThat(desc.getExceptions()).containsOnlyElementsOf(getter.getExceptions());
        break;
      }
    }
  }

  class WithPrimitiveArrayCollection {
    List<int[]> scores;

    @SuppressWarnings("unused")
    public List<int[]> getScores() {
      return scores;
    }
  }

  @Test
  public void should_build_class_description_for_iterable_of_primitive_type_array() throws Exception {
    // Given
    Class<?> clazz = WithPrimitiveArrayCollection.class;
    // When
    ClassDescription classDescription = converter.convertToClassDescription(clazz);
    assertThat(classDescription.getGettersDescriptions()).hasSize(1);
    // Then
    GetterDescription getterDescription = classDescription.getGettersDescriptions().iterator().next();
    assertThat(getterDescription.isIterableType())
        .as("getterDescription must be iterable")
        .isTrue();
    assertThat(getterDescription.getElementTypeName())
        .as("getterDesc must have correct element type")
        .isEqualTo("int[]");
    assertThat(getterDescription.isArrayType())
        .as("getterDescription must not be an array")
        .isFalse();
  }

  static class WithPrimitiveArrayArrayCollection {
    int[][] scores;

    @SuppressWarnings("unused")
    public int[][] getScores() {
      return scores;
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void should_fail_to_build_class_description_for_local_class() throws Exception {
    class Local {
    }
    converter.convertToClassDescription(Local.class);
  }

  @Test
  public void should_build_class_description_for_array_of_primitive_type_array() throws Exception {
    // When
    ClassDescription classDescription = converter.convertToClassDescription(WithPrimitiveArrayArrayCollection.class);
    // Then
    assertThat(classDescription.getGettersDescriptions()).hasSize(1);
    GetterDescription getterDescription = classDescription.getGettersDescriptions().iterator().next();
    assertThat(getterDescription.isIterableType()).as("getterDescription is an iterable ?").isFalse();
    assertThat(getterDescription.isArrayType()).as("getterDescription is an array ?").isTrue();
    assertThat(getterDescription.getElementTypeName()).isEqualTo("int[]");
  }

  @Test
  public void should_build_class_description_for_enum_type() throws Exception {
    // When
    ClassDescription classDescription = converter.convertToClassDescription(TreeEnum.class);
    // Then
    // should not contain getDeclaringClassGetter as we don't want to have hasDeclaringClass assertion
    assertThat(classDescription.getGettersDescriptions()).hasSize(1);
    GetterDescription getterDescription = classDescription.getGettersDescriptions().iterator().next();
    assertThat(getterDescription.isIterableType()).as("getterDescription must be iterable").isTrue();
    assertThat(getterDescription.getElementTypeName())
        .as("getterDescription must get the internal component type without package")
        .isEqualTo(TreeEnum.class.getSimpleName());
    assertThat(getterDescription.isArrayType()).as("getterDescription must be an array").isFalse();
  }

  class WithIterableObjectType {
    List<Player[]> players;

    @SuppressWarnings("unused")
    public List<Player[]> getPlayers() {
      return players;
    }
  }

  @Test
  public void should_build_class_description_for_iterable_of_Object_type() throws Exception {
    // Given
    Class<?> clazz = WithIterableObjectType.class;
    // When
    ClassDescription classDescription = converter.convertToClassDescription(clazz);
    // Then
    assertThat(classDescription.getGettersDescriptions()).hasSize(1);
    GetterDescription getterDescription = classDescription.getGettersDescriptions().iterator().next();
    assertThat(getterDescription.isIterableType())
        .as("getterDescription must be iterable")
        .isTrue();
    assertThat(getterDescription.getElementTypeName())
        .as("getterDesc element type must return correct array type")
        .isEqualTo(getTypeDeclaration(new TypeToken<Player[]>() {
        }));
    assertThat(getterDescription.isArrayType()).as("getterDescription is not an array").isFalse();
  }

  @Test
  public void should_build_class_description_for_interface() throws Exception {
    // Given an interface
    Class<?> clazz = PlayerAgent.class;
    // When
    ClassDescription classDescription = converter.convertToClassDescription(clazz);
    // Then
    assertThat(classDescription.getSuperType()).isNull();
    assertThat(classDescription.getGettersDescriptions()).hasSize(1);
    GetterDescription getterDescription = classDescription.getGettersDescriptions().iterator().next();
    assertThat(getterDescription.isIterableType()).as("getterDescription is not iterable").isFalse();
    assertThat(getterDescription.getName()).as("getterDesc must have correct name")
                                           .isEqualTo("managedPlayer");
    assertThat(getterDescription.getTypeName()).as("getterDesc must have correct type (not fully qualified because in same package)")
                                               .isEqualTo("Player");
  }

  @Test
  public void should_build_fellowshipOfTheRing_class_description() throws Exception {
    // Given
    Class<?> clazz = FellowshipOfTheRing.class;
    // When
    ClassDescription classDescription = converter.convertToClassDescription(clazz);
    // Then
    assertThat(classDescription.getClassNameWithOuterClass()).isEqualTo("FellowshipOfTheRing");
    assertThat(classDescription.getFullyQualifiedClassName()).isEqualTo("org.assertj.assertions.generator.data.lotr.FellowshipOfTheRing");
    assertThat(classDescription.getFullyQualifiedClassNameWithoutGenerics()).isEqualTo(classDescription.getFullyQualifiedClassName());
    assertThat(classDescription.getPackageName()).isEqualTo("org.assertj.assertions.generator.data.lotr");
    assertThat(classDescription.getGettersDescriptions()).hasSize(1);
  }

  @Test
  public void should_handle_toString() {
    ClassDescription classDescription = converter.convertToClassDescription(FellowshipOfTheRing.class);
    assertThat(classDescription.toString()).contains(FellowshipOfTheRing.class.getName());
  }

  @Test
  public void should_build_class_description_for_class_with_public_fields() throws Exception {
    // Given
    Class<?> clazz = Team.class;
    // When
    ClassDescription classDescription = converter.convertToClassDescription(clazz);
    // Then
    assertThat(classDescription.getClassNameWithOuterClass()).isEqualTo("Team");
    assertThat(classDescription.getFullyQualifiedClassName()).isEqualTo("org.assertj.assertions.generator.data.Team");
    assertThat(classDescription.getFullyQualifiedClassNameWithoutGenerics()).isEqualTo(classDescription.getFullyQualifiedClassName());
    assertThat(classDescription.getPackageName()).isEqualTo("org.assertj.assertions.generator.data");
    assertThat(classDescription.getGettersDescriptions()).extracting("name")
                                                         .containsExactly("division");
    assertThat(classDescription.getFieldsDescriptions()).extracting("name")
                                                        .containsOnly("name",
                                                                      "oldNames",
                                                                      "westCoast",
                                                                      "rank",
                                                                      "players",
                                                                      "points",
                                                                      "victoryRatio",
                                                                      "division",
                                                                      "privateField");
  }

  class Bug21_SQLException extends SQLException {
    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unused")
    public SQLException getExceptionChain() {
      return null;
    }
  }

  @Test
  public void bug21_reflection_error_on_iterable_ParameterizedType() {
    ClassDescription classDescription = converter.convertToClassDescription(Bug21_SQLException.class);
    // exceptionChain is a SQLException which is an Iterable<Throwable> but looking only at SQLException we can't deduce
    // iterable valueType
    assertThat(classDescription.getGettersDescriptions()).extracting("name").contains("exceptionChain");
  }

  @Test
  public void should_only_describe_overridden_getter_once() {
    ClassDescription myClassDescription = converter.convertToClassDescription(ClassOverridingGetter.class);
    assertThat(myClassDescription.getGettersDescriptions()).extracting("name").containsOnlyOnce("myList");
  }

  public interface InterfaceWithGetter {
    @SuppressWarnings("unused")
    List<String> getMyList();
  }

  class ClassOverridingGetter implements InterfaceWithGetter {
    @Override
    public ArrayList<String> getMyList() {
      return null;
    }
  }

}
