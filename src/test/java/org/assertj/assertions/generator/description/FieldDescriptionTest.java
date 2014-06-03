package org.assertj.assertions.generator.description;

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.assertions.generator.data.nba.Player;
import org.junit.Test;

public class FieldDescriptionTest {

  private DataDescription fieldDescription;

  @Test
  public void should_create_valid_typename_from_class() {
    fieldDescription = new FieldDescription("bestPlayer", new TypeDescription(new TypeName(Player.class)));
    assertThat(fieldDescription.getName()).isEqualTo("bestPlayer");
    assertThat(fieldDescription.getTypeName()).isEqualTo("Player");
    assertThat(fieldDescription.getElementTypeName()).isNull();
  }

  @Test
  public void should_show_information_in_toString() {
    fieldDescription = new FieldDescription("bestPlayer", new TypeDescription(new TypeName(Player.class)));
    assertThat(fieldDescription.toString()).contains("bestPlayer").contains(Player.class.getName());
  }

  @Test
  public void should_detect_real_number_correctly() throws Exception {
    fieldDescription = new FieldDescription("double", new TypeDescription(new TypeName(double.class)));
    assertThat(fieldDescription.isRealNumberType()).as("double").isTrue();
    fieldDescription = new FieldDescription("Double", new TypeDescription(new TypeName(Double.class)));
    assertThat(fieldDescription.isRealNumberType()).as("Double").isTrue();
    fieldDescription = new FieldDescription("float", new TypeDescription(new TypeName(float.class)));
    assertThat(fieldDescription.isRealNumberType()).as("float").isTrue();
    fieldDescription = new FieldDescription("Float", new TypeDescription(new TypeName(Float.class)));
    assertThat(fieldDescription.isRealNumberType()).as("Float").isTrue();
    // not real number types
    fieldDescription = new FieldDescription("int", new TypeDescription(new TypeName(int.class)));
    assertThat(fieldDescription.isRealNumberType()).as("int").isFalse();
    fieldDescription = new FieldDescription("Integer", new TypeDescription(new TypeName(Integer.class)));
    assertThat(fieldDescription.isRealNumberType()).as("Integer").isFalse();
    fieldDescription = new FieldDescription("String", new TypeDescription(new TypeName(String.class)));
    assertThat(fieldDescription.isRealNumberType()).as("String").isFalse();
  }
}
