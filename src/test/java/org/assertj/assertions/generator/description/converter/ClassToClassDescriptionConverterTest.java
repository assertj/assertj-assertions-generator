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
import org.assertj.assertions.generator.description.TypeName;
import org.assertj.assertions.generator.description.converter.ClassToClassDescriptionConverter;
import org.junit.BeforeClass;
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
    assertThat(classDescription.getGetters()).hasSize(8);
    assertThat(classDescription.getImports()).containsOnly(new TypeName(Player.class), new TypeName(Name.class));
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
