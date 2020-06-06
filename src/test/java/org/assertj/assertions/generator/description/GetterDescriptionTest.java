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
 * Copyright 2012-2020 the original author or authors.
 */
package org.assertj.assertions.generator.description;

import com.google.common.reflect.TypeToken;
import org.assertj.assertions.generator.data.Movie;
import org.assertj.assertions.generator.data.lotr.TolkienCharacter;
import org.assertj.assertions.generator.data.nba.Player;
import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

public class GetterDescriptionTest {

  private static final TypeToken<Player> PLAYER_TYPE_DESCRIPTION = TypeToken.of(Player.class);

  private GetterDescription getterDescription;

  private static Method PLAYER_GET_POINTS_METHOD;

  @BeforeClass
  public static void setupClass() throws Exception {
    PLAYER_GET_POINTS_METHOD = Player.class.getMethod("getPoints");
  }

  @Test
  public void should_create_valid_typename_from_class() throws Exception {
    getterDescription = new GetterDescription("points", PLAYER_TYPE_DESCRIPTION, PLAYER_GET_POINTS_METHOD);
    assertThat(getterDescription.getName()).isEqualTo("points");
    assertThat(getterDescription.getTypeName()).isEqualTo("java.util.List");
    assertThat(getterDescription.getElementTypeName()).isEqualTo("int[]");
    assertThat(getterDescription.getFullyQualifiedTypeName()).isEqualTo("java.util.List");
  }

  @Test
  public void should_create_valid_typename_from_class_for_user_defined_type_in_same_package() throws Exception {
    getterDescription = new GetterDescription("race", TypeToken.of(TolkienCharacter.class), TolkienCharacter.class.getMethod("getRace"));
    assertThat(getterDescription.getName()).isEqualTo("race");
    assertThat(getterDescription.getTypeName()).isEqualTo("Race");
    assertThat(getterDescription.getFullyQualifiedTypeName()).isEqualTo("org.assertj.assertions.generator.data.lotr.Race");
  }

  @Test
  public void should_create_valid_typename_from_class_for_user_defined_type_in_different_package() throws Exception {
    getterDescription = new GetterDescription("name", PLAYER_TYPE_DESCRIPTION, Player.class.getMethod("getName"));
    assertThat(getterDescription.getName()).isEqualTo("name");
    assertThat(getterDescription.getTypeName()).isEqualTo("org.assertj.assertions.generator.data.Name");
    assertThat(getterDescription.getFullyQualifiedTypeName()).isEqualTo("org.assertj.assertions.generator.data.Name");
  }

  @Test
  public void should_show_information_in_toString() throws Exception {
    getterDescription = new GetterDescription("points", PLAYER_TYPE_DESCRIPTION, PLAYER_GET_POINTS_METHOD);
    assertThat(getterDescription.toString()).contains("points").contains("List<int[]>");
  }

  @Test
  public void should_not_be_predicate() throws Exception {
    getterDescription = new GetterDescription("points", PLAYER_TYPE_DESCRIPTION, PLAYER_GET_POINTS_METHOD);
    assertThat(getterDescription.isPredicate()).as("points").isFalse();
    getterDescription = new GetterDescription("rookie", PLAYER_TYPE_DESCRIPTION, Player.class.getMethod("isRookie"));
    assertThat(getterDescription.isPredicate()).as("rookie").isTrue();
  }

  @Test
  public void should_generate_predicate_for_javadoc() throws Exception {
    getterDescription = new GetterDescription("rookie", PLAYER_TYPE_DESCRIPTION, Player.class.getMethod("isRookie"));
    assertThat(getterDescription.getPredicateForJavadoc()).isEqualTo("is rookie");
    assertThat(getterDescription.getNegativePredicateForJavadoc()).isEqualTo("is not rookie");

    getterDescription = new GetterDescription("rookie", PLAYER_TYPE_DESCRIPTION, Player.class.getMethod("wasRookie"));
    assertThat(getterDescription.getPredicateForJavadoc()).isEqualTo("was rookie");
    assertThat(getterDescription.getNegativePredicateForJavadoc()).isEqualTo("was not rookie");

    getterDescription = new GetterDescription("win", PLAYER_TYPE_DESCRIPTION, Player.class.getMethod("shouldWin"));
    assertThat(getterDescription.getPredicateForJavadoc()).isEqualTo("should win");
    assertThat(getterDescription.getNegativePredicateForJavadoc()).isEqualTo("should not win");

    getterDescription = new GetterDescription("win", PLAYER_TYPE_DESCRIPTION, Player.class.getMethod("canWin"));
    assertThat(getterDescription.getPredicateForJavadoc()).isEqualTo("can win");
    assertThat(getterDescription.getNegativePredicateForJavadoc()).isEqualTo("cannot win");

    getterDescription = new GetterDescription("win", PLAYER_TYPE_DESCRIPTION, Player.class.getMethod("willWin"));
    assertThat(getterDescription.getPredicateForJavadoc()).isEqualTo("will win");
    assertThat(getterDescription.getNegativePredicateForJavadoc()).isEqualTo("will not win");

    getterDescription = new GetterDescription("trophy", PLAYER_TYPE_DESCRIPTION, Player.class.getMethod("hasTrophy"));
    assertThat(getterDescription.getPredicateForJavadoc()).isEqualTo("has trophy");
    assertThat(getterDescription.getNegativePredicateForJavadoc()).isEqualTo("does not have trophy");

    getterDescription = new GetterDescription("fun", PLAYER_TYPE_DESCRIPTION, Player.class.getMethod("doesNotHaveFun"));
    assertThat(getterDescription.getPredicateForJavadoc()).isEqualTo("does not have fun");
    assertThat(getterDescription.getNegativePredicateForJavadoc()).isEqualTo("has fun");

    getterDescription = new GetterDescription("win", PLAYER_TYPE_DESCRIPTION, Player.class.getMethod("cannotWin"));
    assertThat(getterDescription.getPredicateForJavadoc()).isEqualTo("cannot win");
    assertThat(getterDescription.getNegativePredicateForJavadoc()).isEqualTo("can win");

    getterDescription = new GetterDescription("play", PLAYER_TYPE_DESCRIPTION, Player.class.getMethod("shouldNotPlay"));
    assertThat(getterDescription.getPredicateForJavadoc()).isEqualTo("should not play");
    assertThat(getterDescription.getNegativePredicateForJavadoc()).isEqualTo("should play");
  }

  @Test
  public void should_generate_predicate_for_error_message() throws Exception {
    getterDescription = new GetterDescription("rookie", PLAYER_TYPE_DESCRIPTION, Player.class.getMethod("isRookie"));
    assertThat(getterDescription.getPredicateForErrorMessagePart1()).isEqualTo("is rookie");
    assertThat(getterDescription.getPredicateForErrorMessagePart2()).isEqualTo("is not");

    getterDescription = new GetterDescription("rookie", PLAYER_TYPE_DESCRIPTION, Player.class.getMethod("wasRookie"));
    assertThat(getterDescription.getPredicateForErrorMessagePart1()).isEqualTo("was rookie");
    assertThat(getterDescription.getPredicateForErrorMessagePart2()).isEqualTo("was not");

    getterDescription = new GetterDescription("win", PLAYER_TYPE_DESCRIPTION, Player.class.getMethod("shouldWin"));
    assertThat(getterDescription.getPredicateForErrorMessagePart1()).isEqualTo("should win");
    assertThat(getterDescription.getPredicateForErrorMessagePart2()).isEqualTo("should not");

    getterDescription = new GetterDescription("win", PLAYER_TYPE_DESCRIPTION, Player.class.getMethod("canWin"));
    assertThat(getterDescription.getPredicateForErrorMessagePart1()).isEqualTo("can win");
    assertThat(getterDescription.getPredicateForErrorMessagePart2()).isEqualTo("cannot");

    getterDescription = new GetterDescription("win", PLAYER_TYPE_DESCRIPTION, Player.class.getMethod("willWin"));
    assertThat(getterDescription.getPredicateForErrorMessagePart1()).isEqualTo("will win");
    assertThat(getterDescription.getPredicateForErrorMessagePart2()).isEqualTo("will not");

    getterDescription = new GetterDescription("trophy", PLAYER_TYPE_DESCRIPTION, Player.class.getMethod("hasTrophy"));
    assertThat(getterDescription.getPredicateForErrorMessagePart1()).isEqualTo("has trophy");
    assertThat(getterDescription.getPredicateForErrorMessagePart2()).isEqualTo("does not have");

    getterDescription = new GetterDescription("fun", PLAYER_TYPE_DESCRIPTION, Player.class.getMethod("doesNotHaveFun"));
    assertThat(getterDescription.getPredicateForErrorMessagePart1()).isEqualTo("does not have fun");
    assertThat(getterDescription.getPredicateForErrorMessagePart2()).isEqualTo("has");

    getterDescription = new GetterDescription("win", PLAYER_TYPE_DESCRIPTION, Player.class.getMethod("cannotWin"));
    assertThat(getterDescription.getPredicateForErrorMessagePart1()).isEqualTo("cannot win");
    assertThat(getterDescription.getPredicateForErrorMessagePart2()).isEqualTo("can");

    getterDescription = new GetterDescription("play", PLAYER_TYPE_DESCRIPTION, Player.class.getMethod("shouldNotPlay"));
    assertThat(getterDescription.getPredicateForErrorMessagePart1()).isEqualTo("should not play");
    assertThat(getterDescription.getPredicateForErrorMessagePart2()).isEqualTo("should");
  }

  @Test
  public void should_describe_inner_class_getter_correctly() throws Exception {
    Method getPublicCategory = Movie.class.getMethod("getPublicCategory");
    getterDescription = new GetterDescription("publicCategory", TypeToken.of(Movie.class), getPublicCategory);
    assertThat(getterDescription.getName()).isEqualTo("publicCategory");
    assertThat(getterDescription.getTypeName()).isEqualTo("Movie.PublicCategory");
    assertThat(getterDescription.getElementTypeName()).isNull();
  }

}
