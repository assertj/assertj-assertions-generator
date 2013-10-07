package org.assertj.assertions.generator.description;

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.assertions.generator.data.Player;
import org.assertj.assertions.generator.description.GetterDescription;
import org.assertj.assertions.generator.description.TypeDescription;
import org.assertj.assertions.generator.description.TypeName;
import org.junit.Test;

import java.util.Collections;

public class GetterDescriptionTest {

  private GetterDescription getterDescription;

  @Test
  public void should_create_valid_typename_from_class() {
    getterDescription = new GetterDescription("bestPlayer", new TypeDescription(new TypeName(Player.class)), Collections.<TypeName>emptyList());
    assertThat(getterDescription.getPropertyName()).isEqualTo("bestPlayer");
    assertThat(getterDescription.getPropertyTypeName()).isEqualTo("Player");
    assertThat(getterDescription.getElementTypeName()).isNull();
  }

  @Test
  public void should_show_information_in_toString() {
    getterDescription = new GetterDescription("bestPlayer", new TypeDescription(new TypeName(Player.class)), Collections.<TypeName>emptyList());
    assertThat(getterDescription.toString()).contains("bestPlayer").contains(Player.class.getName());
  }
}
