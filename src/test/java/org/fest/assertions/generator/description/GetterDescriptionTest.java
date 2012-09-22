package org.fest.assertions.generator.description;

import static org.fest.assertions.api.Assertions.assertThat;

import org.junit.Test;

import org.fest.assertions.generator.data.Player;

public class GetterDescriptionTest {

  private GetterDescription getterDescription;

  @Test
  public void should_create_valid_typename_from_class() {
    getterDescription = new GetterDescription("bestPlayer", new TypeDescription(new TypeName(Player.class)));
    assertThat(getterDescription.getPropertyName()).isEqualTo("bestPlayer");
    assertThat(getterDescription.getPropertyTypeName()).isEqualTo("Player");
    assertThat(getterDescription.getElementTypeName()).isNull();
  }
}
