package org.assertj.assertions.generator.description.converter;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.assertj.assertions.generator.data.Name;
import org.assertj.assertions.generator.data.Player;
import org.assertj.assertions.generator.data.lotr.FellowshipOfTheRing;
import org.assertj.assertions.generator.data.lotr.Race;
import org.assertj.assertions.generator.data.lotr.TolkienCharacter;
import org.assertj.assertions.generator.description.ClassDescription;
import org.assertj.assertions.generator.description.GetterDescription;
import org.assertj.assertions.generator.description.TypeName;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class ClassToClassDescriptionConverterTest {

  private static ClassToClassDescriptionConverter converter;

  @BeforeClass
  public static void beforeAllTests() {
    converter = new ClassToClassDescriptionConverter();
  }

  @Test
  public void should_build_player_class_description() throws Exception {
    ClassDescription classDescription = converter.convertToClassDescription(Player.class);
    assertThat(classDescription.getClassName()).isEqualTo("Player");
    assertThat(classDescription.getPackageName()).isEqualTo("org.assertj.assertions.generator.data");
    assertThat(classDescription.getGetters()).hasSize(9);
    assertThat(classDescription.getImports()).containsOnly(new TypeName(Player.class), new TypeName(Name.class));
  }

  @Ignore("in case of java 7 oracle, return int[], in case of java 6 return int")
  @Test
  public void should_build_class_description_for_iterable_of_simple_type() throws Exception {
    class Type {
      List<int[]> scores;

      public List<int[]> getScores() {
        return scores;
      }
    }
    ClassDescription classDescription = converter.convertToClassDescription(Type.class);
    assertThat(classDescription.getClassName()).isEqualTo("Type");
    assertThat(classDescription.getGetters()).hasSize(1);
    GetterDescription getterDescription = classDescription.getGetters().iterator().next();
    assertThat(getterDescription.isIterablePropertyType()).as("getterDescription must be iterable").isTrue();
    assertThat(getterDescription.getElementTypeName()).isEqualTo("int");
    assertThat(getterDescription.isArrayPropertyType()).as("getterDescription must be an array").isTrue();
  }

  @Test
  public void should_build_fellowshipOfTheRing_class_description() throws Exception {
    ClassDescription classDescription = converter.convertToClassDescription(FellowshipOfTheRing.class);
    assertThat(classDescription.getClassName()).isEqualTo("FellowshipOfTheRing");
    assertThat(classDescription.getPackageName()).isEqualTo("org.assertj.assertions.generator.data.lotr");
    assertThat(classDescription.getGetters()).hasSize(1);
    assertThat(classDescription.getImports()).containsOnly(new TypeName(Map.class), new TypeName(List.class),
        new TypeName(Race.class), new TypeName(TolkienCharacter.class));
  }

}
