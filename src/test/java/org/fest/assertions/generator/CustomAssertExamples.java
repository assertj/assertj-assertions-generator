package org.fest.assertions.generator;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.generator.data.PlayerAssert.assertThat;

import org.junit.Test;

import org.fest.assertions.examples.advanced.TolkienCharacterAssert;
import org.fest.assertions.generator.data.TolkienCharacter;

/**
 * 
 * Shows some example of a custom assertion class: {@link TolkienCharacterAssert} that allows us to make assertions
 * specific to {@link TolkienCharacter}.
 * 
 * @author Joel Costigliola
 */
public class CustomAssertExamples extends AbstractAssertionsExamples {

  @Test
  public void succesful_custom_assertion_example() {
    // custom assertion : assertThat is resolved from TolkienCharacterAssert static import
    assertThat(rose).hasTeam("Chicago Bulls");
  }

  @Test
  public void failed_custom_assertion_example() {
    sam.setName("Sammy");
    try {
      assertThat(rose).hasTeam("Los Angeles Lakers");
    } catch (AssertionError e) {
      // As we are defining custom assertion, we can set meaningful assertion error message, like this one :
      assertThat(e).hasMessage("Expected Player's team to be <Los Angeles Lakers> but was <Chicago Bulls>");
    }
  }
  


}
