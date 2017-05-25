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
 * Copyright 2012-2015 the original author or authors.
 */
package org.assertj.assertions.generator.description;

import com.google.common.reflect.TypeToken;
import org.assertj.assertions.generator.data.Name;
import org.assertj.assertions.generator.data.nba.Player;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class FieldDescriptionTest {

  private static final TypeToken<Player> PLAYER_TYPE = TypeToken.of(Player.class);
  private FieldDescription fieldDescription;

  @Test
  public void should_create_valid_typename_from_class() throws Exception {
    fieldDescription = new FieldDescription(Player.class.getDeclaredField("name"), PLAYER_TYPE);
    assertThat(fieldDescription.getName()).isEqualTo("name");
    assertThat(fieldDescription.getTypeName(false, false)).isEqualTo(Name.class.getSimpleName());
    assertThat(fieldDescription.getElementTypeName(Player.class.getPackage().getName())).isNull();
  }

  @Test
  public void should_show_information_in_toString() throws Exception {
    fieldDescription = new FieldDescription(Player.class.getDeclaredField("name"), PLAYER_TYPE);
    assertThat(fieldDescription.toString()).contains("name")
        .contains(Player.class.getName())
        .contains(Name.class.getName());
  }

  @Test
  public void should_detect_real_number_correctly() throws Exception {
    fieldDescription = new FieldDescription(Player.class.getDeclaredField("sizeDouble"), PLAYER_TYPE);
    assertThat(fieldDescription.isRealNumberType()).as("double").isTrue();
    fieldDescription = new FieldDescription(Player.class.getDeclaredField("sizeAsDoubleWrapper"), PLAYER_TYPE);
    assertThat(fieldDescription.isRealNumberType()).as("Double").isTrue();
    fieldDescription = new FieldDescription(Player.class.getDeclaredField("size"), PLAYER_TYPE);
    assertThat(fieldDescription.isRealNumberType()).as("float").isTrue();
    fieldDescription = new FieldDescription(Player.class.getDeclaredField("sizeAsFloatWrapper"), PLAYER_TYPE);
    assertThat(fieldDescription.isRealNumberType()).as("Float").isTrue();

    // not real number types
    fieldDescription = new FieldDescription(Player.class.getDeclaredField("pointsPerGame"), PLAYER_TYPE);
    assertThat(fieldDescription.isRealNumberType()).as("int").isFalse();
    fieldDescription = new FieldDescription(Player.class.getDeclaredField("pointsPerGame"), PLAYER_TYPE);
    assertThat(fieldDescription.isRealNumberType()).as("Integer").isFalse();
    fieldDescription = new FieldDescription(Player.class.getDeclaredField("team"), PLAYER_TYPE);
    assertThat(fieldDescription.isRealNumberType()).as("String").isFalse();
  }
  
  @Test
  public void should_generate_default_predicate_correctly() throws Exception {
    fieldDescription = new FieldDescription(Player.class.getDeclaredField("bad"), PLAYER_TYPE);
    assertThat(fieldDescription.getNegativePredicate()).as("negative").isEqualTo("isNotBad");
    assertThat(fieldDescription.getPredicate()).as("positive").isEqualTo("isBad");
  }

  @Test
  public void should_generate_readable_predicate_for_javadoc() throws Exception {
    fieldDescription = new FieldDescription(Player.class.getDeclaredField("bad"), PLAYER_TYPE);
    assertThat(fieldDescription.getPredicateForJavadoc()).isEqualTo("is bad");
    assertThat(fieldDescription.getNegativePredicateForJavadoc()).isEqualTo("is not bad");
  }

  @Test
  public void should_generate_readable_predicate_for_error_message() throws Exception {
    fieldDescription = new FieldDescription(Player.class.getDeclaredField("bad"), PLAYER_TYPE);
    assertThat(fieldDescription.getPredicateForErrorMessagePart1()).isEqualTo("is bad");
    assertThat(fieldDescription.getPredicateForErrorMessagePart2()).isEqualTo("is not");

    fieldDescription = new FieldDescription(Player.class.getDeclaredField("isDisabled"), PLAYER_TYPE);
    assertThat(fieldDescription.getPredicateForErrorMessagePart1()).isEqualTo("is disabled");
    assertThat(fieldDescription.getPredicateForErrorMessagePart2()).isEqualTo("is not");
  }
}
