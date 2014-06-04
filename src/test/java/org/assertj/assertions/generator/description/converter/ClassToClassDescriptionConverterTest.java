package org.assertj.assertions.generator.description.converter;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.assertj.assertions.generator.BeanWithExceptionsTest;
import org.assertj.assertions.generator.NestedClassesTest;
import org.assertj.assertions.generator.data.ArtWork;
import org.assertj.assertions.generator.data.Movie;
import org.assertj.assertions.generator.data.Movie.PublicCategory;
import org.assertj.assertions.generator.data.Name;
import org.assertj.assertions.generator.data.Player;
import org.assertj.assertions.generator.data.Team;
import org.assertj.assertions.generator.data.TreeEnum;
import org.assertj.assertions.generator.data.lotr.FellowshipOfTheRing;
import org.assertj.assertions.generator.data.lotr.Race;
import org.assertj.assertions.generator.data.lotr.TolkienCharacter;
import org.assertj.assertions.generator.description.ClassDescription;
import org.assertj.assertions.generator.description.GetterDescription;
import org.assertj.assertions.generator.description.TypeName;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

@RunWith(Theories.class)
public class ClassToClassDescriptionConverterTest implements NestedClassesTest, BeanWithExceptionsTest {
  private static ClassToClassDescriptionConverter converter;

  @BeforeClass
  public static void beforeAllTests() {
    converter = new ClassToClassDescriptionConverter();
  }

  @Test
  public void should_build_player_class_description() throws Exception {
    ClassDescription classDescription = converter.convertToClassDescription(Player.class);
    assertThat(classDescription.getClassName()).isEqualTo("Player");
    assertThat(classDescription.getClassNameWithOuterClass()).isEqualTo("Player");
    assertThat(classDescription.getPackageName()).isEqualTo("org.assertj.assertions.generator.data");
    assertThat(classDescription.getGettersDescriptions()).hasSize(9);
    assertThat(classDescription.getDeclaredGettersDescriptions()).hasSize(9);
    assertThat(classDescription.getImports()).containsOnly(new TypeName(Player.class), new TypeName(Name.class));
  }

  @Test
  public void should_build_movie_class_description() throws Exception {
    ClassDescription classDescription = converter.convertToClassDescription(Movie.class);
    assertThat(classDescription.getClassName()).isEqualTo("Movie");
    assertThat(classDescription.getClassNameWithOuterClass()).isEqualTo("Movie");
    assertThat(classDescription.getPackageName()).isEqualTo("org.assertj.assertions.generator.data");
    assertThat(classDescription.getGettersDescriptions()).hasSize(3);
    assertThat(classDescription.getFieldsDescriptions()).hasSize(2);
    assertThat(classDescription.getDeclaredGettersDescriptions()).hasSize(2);
    assertThat(classDescription.getDeclaredFieldsDescriptions()).hasSize(1);
    assertThat(classDescription.getSuperType()).isEqualTo(ArtWork.class);
    assertThat(classDescription.getImports()).containsOnly(new TypeName(PublicCategory.class), new TypeName(Date.class));
  }

  @Theory
  public void should_build_nestedclass_description(NestedClass nestedClass) throws Exception {
    Class<?> clazz = nestedClass.getNestedClass();
    ClassDescription classDescription = converter.convertToClassDescription(clazz);
    assertThat(classDescription.getClassName()).isEqualTo(clazz.getSimpleName());
    assertThat(classDescription.getClassNameWithOuterClass()).isEqualTo(nestedClass.getClassNameWithOuterClass());
    assertThat(classDescription.getPackageName()).isEqualTo(clazz.getPackage().getName());
    assertThat(classDescription.getGettersDescriptions()).hasSize(1);
    assertThat(classDescription.getImports()).isEmpty();
  }

  @Theory
  public void should_build_getter_with_exception_description(GetterWithException getter) throws Exception {
    Class<?> clazz = getter.getBeanClass();
    ClassDescription classDescription = converter.convertToClassDescription(clazz);
    assertThat(classDescription.getClassName()).isEqualTo(clazz.getSimpleName());
    assertThat(classDescription.getClassNameWithOuterClass()).isEqualTo(clazz.getSimpleName());
    assertThat(classDescription.getPackageName()).isEqualTo(clazz.getPackage().getName());
    assertThat(classDescription.getGettersDescriptions()).hasSize(4);
    assertThat(classDescription.getImports()).containsOnly(getter.getExceptions());

    for (GetterDescription desc : classDescription.getGettersDescriptions()) {
      if (desc.getPropertyName().equals(getter.getPropertyName())) {
        assertThat(desc.getExceptions()).containsOnly(getter.getExceptions());
        break;
      }
    }
  }

  @Test
  public void should_build_class_description_for_iterable_of_simple_type() throws Exception {
    class Type {
      List<int[]> scores;

      @SuppressWarnings("unused")
      public List<int[]> getScores() {
        return scores;
      }
    }
    ClassDescription classDescription = converter.convertToClassDescription(Type.class);
    assertThat(classDescription.getClassName()).isEqualTo("Type");
    assertThat(classDescription.getGettersDescriptions()).hasSize(1);
    GetterDescription getterDescription = classDescription.getGettersDescriptions().iterator().next();
    assertThat(getterDescription.isIterablePropertyType()).as("getterDescription must be iterable").isTrue();
    assertThat(getterDescription.getElementTypeName()).isEqualTo("int");
    assertThat(getterDescription.isArrayPropertyType()).as("getterDescription must be an array").isTrue();
  }

  @Test
  public void should_build_class_description_for_enum_type() throws Exception {
    ClassDescription classDescription = converter.convertToClassDescription(TreeEnum.class);
    assertThat(classDescription.getClassName()).isEqualTo("TreeEnum");
    // should not contain getDeclaringClassGetter as we don't want to have hasDeclaringClass assertion
    assertThat(classDescription.getGettersDescriptions()).hasSize(1);
    GetterDescription getterDescription = classDescription.getGettersDescriptions().iterator().next();
    assertThat(getterDescription.isIterablePropertyType()).as("getterDescription must be iterable").isTrue();
    assertThat(getterDescription.getElementTypeName()).isEqualTo("TreeEnum");
    assertThat(getterDescription.isArrayPropertyType()).as("getterDescription must be an array").isFalse();
  }

  @Test
  public void should_build_class_description_for_iterable_of_Object_type() throws Exception {
    // Given
    class Type {
      List<Player[]> players;
      @SuppressWarnings("unused")
      public List<Player[]> getPlayers() {
        return players;
      }
    }

    // When
    ClassDescription classDescription = converter.convertToClassDescription(Type.class);

    // Then
    assertThat(classDescription.getClassName()).isEqualTo("Type");
    assertThat(classDescription.getGettersDescriptions()).hasSize(1);
    GetterDescription getterDescription = classDescription.getGettersDescriptions().iterator().next();
    assertThat(getterDescription.isIterablePropertyType()).as("getterDescription must be iterable").isTrue();
    assertThat(getterDescription.getElementTypeName()).isEqualTo("Player");
    assertThat(getterDescription.isArrayPropertyType()).as("getterDescription must be an array").isTrue();
    assertThat(classDescription.getImports()).extracting("simpleName").contains(Player.class.getSimpleName());
  }

  @Test
  public void should_build_fellowshipOfTheRing_class_description() throws Exception {
    ClassDescription classDescription = converter.convertToClassDescription(FellowshipOfTheRing.class);
    assertThat(classDescription.getClassName()).isEqualTo("FellowshipOfTheRing");
    assertThat(classDescription.getClassNameWithOuterClass()).isEqualTo("FellowshipOfTheRing");
    assertThat(classDescription.getClassNameWithOuterClassNotSeparatedByDots()).isEqualTo("FellowshipOfTheRing");
    assertThat(classDescription.getPackageName()).isEqualTo("org.assertj.assertions.generator.data.lotr");
    assertThat(classDescription.getGettersDescriptions()).hasSize(1);
    assertThat(classDescription.getImports()).containsOnly(new TypeName(Map.class), new TypeName(List.class),
                                                           new TypeName(Race.class),
                                                           new TypeName(TolkienCharacter.class));
  }

  @Test
  public void should_handle_toString() {
    ClassDescription classDescription = converter.convertToClassDescription(FellowshipOfTheRing.class);
    assertThat(classDescription.toString()).contains(FellowshipOfTheRing.class.getSimpleName(),
                                                     "java.util.Map",
                                                     "java.util.List",
                                                     "org.assertj.assertions.generator.data.lotr.Race",
                                                     "org.assertj.assertions.generator.data.lotr.TolkienCharacter");
  }

  @Test
  public void should_build_class_description_for_class_with_public_fields() throws Exception {
    ClassDescription classDescription = converter.convertToClassDescription(Team.class);
    assertThat(classDescription.getClassName()).isEqualTo("Team");
    assertThat(classDescription.getClassNameWithOuterClass()).isEqualTo("Team");
    assertThat(classDescription.getClassNameWithOuterClassNotSeparatedByDots()).isEqualTo("Team");
    assertThat(classDescription.getPackageName()).isEqualTo("org.assertj.assertions.generator.data");
    assertThat(classDescription.getGettersDescriptions()).extracting("propertyName").containsExactly("division");
    assertThat(classDescription.getFieldsDescriptions()).extracting("name").containsOnly("name", "oldNames", "westCoast", "rank", "players", "points");
    assertThat(classDescription.getImports()).isEmpty();
  }

}
