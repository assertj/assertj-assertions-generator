package org.assertj.assertions.generator.description;

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.assertions.generator.data.Player;
import org.junit.Test;

public class FieldDescriptionTest {

  private FieldDescription fieldDescription;

  @Test
  public void should_create_valid_typename_from_class() {
    fieldDescription = new FieldDescription("bestPlayer", new TypeDescription(new TypeName(Player.class)));
    assertThat(fieldDescription.getName()).isEqualTo("bestPlayer");
    assertThat(fieldDescription.getPropertyTypeName()).isEqualTo("Player");
    assertThat(fieldDescription.getElementTypeName()).isNull();
  }

  @Test
  public void should_show_information_in_toString() {
    fieldDescription = new FieldDescription("bestPlayer", new TypeDescription(new TypeName(Player.class)));
    assertThat(fieldDescription.toString()).contains("bestPlayer").contains(Player.class.getName());
  }
}
