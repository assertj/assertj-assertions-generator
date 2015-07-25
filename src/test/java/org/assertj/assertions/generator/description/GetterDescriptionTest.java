/**
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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.List;

import org.assertj.assertions.generator.data.nba.Player;
import org.junit.Test;

public class GetterDescriptionTest {

  private static final TypeDescription PLAYER_TYPE_DESCRIPTION = new TypeDescription(new TypeName(Player.class));
  private static final TypeDescription BOOLEAN_TYPE_DESCRIPTION = new TypeDescription(new TypeName(boolean.class));
  private static final List<TypeName> EMPTY_TYPENAME_LIST = Collections.<TypeName> emptyList();;

  private GetterDescription getterDescription;

  @Test
  public void should_create_valid_typename_from_class() {
    getterDescription = new GetterDescription("bestPlayer", "getBestPlayer", PLAYER_TYPE_DESCRIPTION,
                                              EMPTY_TYPENAME_LIST);
    assertThat(getterDescription.getPropertyName()).isEqualTo("bestPlayer");
    assertThat(getterDescription.getTypeName()).isEqualTo("Player");
    assertThat(getterDescription.getElementTypeName(Player.class.getPackage().getName())).isNull();
  }

  @Test
  public void should_show_information_in_toString() {
    getterDescription = new GetterDescription("bestPlayer", "getBestPlayer", PLAYER_TYPE_DESCRIPTION,
                                              EMPTY_TYPENAME_LIST);
    assertThat(getterDescription.toString()).contains("bestPlayer").contains(Player.class.getName());
  }

  @Test
  public void should_not_be_predicate() {
    getterDescription = new GetterDescription("bestPlayer", "getBestPlayer", PLAYER_TYPE_DESCRIPTION,
                                              EMPTY_TYPENAME_LIST);
    assertThat(getterDescription.isPredicate()).as("bestPlayer").isFalse();
    getterDescription = new GetterDescription("runFlag", "getRunFlag", BOOLEAN_TYPE_DESCRIPTION, EMPTY_TYPENAME_LIST);
    assertThat(getterDescription.isPredicate()).as("runFlag").isFalse();
  }

  @Test
  public void should_be_predicate() {
    for (String p : new String[] { "is", "can", "was", "has", "should" }) {
      getterDescription = new GetterDescription("bestPlayer", p + "BestPlayer", BOOLEAN_TYPE_DESCRIPTION,
                                                EMPTY_TYPENAME_LIST);
      assertThat(getterDescription.isPredicate()).as(p).isTrue();
    }
  }

  @Test
  public void should_generate_predicate_for_javadoc() {
    getterDescription = new GetterDescription("rookie", "isRookie", BOOLEAN_TYPE_DESCRIPTION, EMPTY_TYPENAME_LIST);
    assertThat(getterDescription.getPredicateForJavadoc()).isEqualTo("is rookie");
    assertThat(getterDescription.getNegativePredicateForJavadoc()).isEqualTo("is not rookie");

    getterDescription = new GetterDescription("", "wasRookie", BOOLEAN_TYPE_DESCRIPTION, EMPTY_TYPENAME_LIST);
    assertThat(getterDescription.getPredicateForJavadoc()).isEqualTo("was rookie");
    assertThat(getterDescription.getNegativePredicateForJavadoc()).isEqualTo("was not rookie");

    getterDescription = new GetterDescription("", "shouldWin", BOOLEAN_TYPE_DESCRIPTION, EMPTY_TYPENAME_LIST);
    assertThat(getterDescription.getPredicateForJavadoc()).isEqualTo("should win");
    assertThat(getterDescription.getNegativePredicateForJavadoc()).isEqualTo("should not win");

    getterDescription = new GetterDescription("", "canWin", BOOLEAN_TYPE_DESCRIPTION, EMPTY_TYPENAME_LIST);
    assertThat(getterDescription.getPredicateForJavadoc()).isEqualTo("can win");
    assertThat(getterDescription.getNegativePredicateForJavadoc()).isEqualTo("cannot win");

    getterDescription = new GetterDescription("", "willWin", BOOLEAN_TYPE_DESCRIPTION, EMPTY_TYPENAME_LIST);
    assertThat(getterDescription.getPredicateForJavadoc()).isEqualTo("will win");
    assertThat(getterDescription.getNegativePredicateForJavadoc()).isEqualTo("will not win");

    getterDescription = new GetterDescription("", "hasTrophy", BOOLEAN_TYPE_DESCRIPTION, EMPTY_TYPENAME_LIST);
    assertThat(getterDescription.getPredicateForJavadoc()).isEqualTo("has trophy");
    assertThat(getterDescription.getNegativePredicateForJavadoc()).isEqualTo("does not have trophy");

    getterDescription = new GetterDescription("", "doesNotHaveFun", BOOLEAN_TYPE_DESCRIPTION, EMPTY_TYPENAME_LIST);
    assertThat(getterDescription.getPredicateForJavadoc()).isEqualTo("does not have fun");
    assertThat(getterDescription.getNegativePredicateForJavadoc()).isEqualTo("has fun");

    getterDescription = new GetterDescription("", "cannotWin", BOOLEAN_TYPE_DESCRIPTION, EMPTY_TYPENAME_LIST);
    assertThat(getterDescription.getPredicateForJavadoc()).isEqualTo("cannot win");
    assertThat(getterDescription.getNegativePredicateForJavadoc()).isEqualTo("can win");

    getterDescription = new GetterDescription("", "shouldNotPlay", BOOLEAN_TYPE_DESCRIPTION, EMPTY_TYPENAME_LIST);
    assertThat(getterDescription.getPredicateForJavadoc()).isEqualTo("should not play");
    assertThat(getterDescription.getNegativePredicateForJavadoc()).isEqualTo("should play");
  }

  @Test
  public void should_generate_predicate_for_error_message() {
    getterDescription = new GetterDescription("rookie", "isRookie", BOOLEAN_TYPE_DESCRIPTION, EMPTY_TYPENAME_LIST);
    assertThat(getterDescription.getPredicateForErrorMessagePart1()).isEqualTo("is rookie");
    assertThat(getterDescription.getPredicateForErrorMessagePart2()).isEqualTo("is not");

    getterDescription = new GetterDescription("", "wasRookie", BOOLEAN_TYPE_DESCRIPTION, EMPTY_TYPENAME_LIST);
    assertThat(getterDescription.getPredicateForErrorMessagePart1()).isEqualTo("was rookie");
    assertThat(getterDescription.getPredicateForErrorMessagePart2()).isEqualTo("was not");

    getterDescription = new GetterDescription("", "shouldWin", BOOLEAN_TYPE_DESCRIPTION, EMPTY_TYPENAME_LIST);
    assertThat(getterDescription.getPredicateForErrorMessagePart1()).isEqualTo("should win");
    assertThat(getterDescription.getPredicateForErrorMessagePart2()).isEqualTo("should not");

    getterDescription = new GetterDescription("", "canWin", BOOLEAN_TYPE_DESCRIPTION, EMPTY_TYPENAME_LIST);
    assertThat(getterDescription.getPredicateForErrorMessagePart1()).isEqualTo("can win");
    assertThat(getterDescription.getPredicateForErrorMessagePart2()).isEqualTo("cannot");

    getterDescription = new GetterDescription("", "willWin", BOOLEAN_TYPE_DESCRIPTION, EMPTY_TYPENAME_LIST);
    assertThat(getterDescription.getPredicateForErrorMessagePart1()).isEqualTo("will win");
    assertThat(getterDescription.getPredicateForErrorMessagePart2()).isEqualTo("will not");

    getterDescription = new GetterDescription("", "hasTrophy", BOOLEAN_TYPE_DESCRIPTION, EMPTY_TYPENAME_LIST);
    assertThat(getterDescription.getPredicateForErrorMessagePart1()).isEqualTo("has trophy");
    assertThat(getterDescription.getPredicateForErrorMessagePart2()).isEqualTo("does not have");

    getterDescription = new GetterDescription("", "doesNotHaveFun", BOOLEAN_TYPE_DESCRIPTION, EMPTY_TYPENAME_LIST);
    assertThat(getterDescription.getPredicateForErrorMessagePart1()).isEqualTo("does not have fun");
    assertThat(getterDescription.getPredicateForErrorMessagePart2()).isEqualTo("has");

    getterDescription = new GetterDescription("", "cannotWin", BOOLEAN_TYPE_DESCRIPTION, EMPTY_TYPENAME_LIST);
    assertThat(getterDescription.getPredicateForErrorMessagePart1()).isEqualTo("cannot win");
    assertThat(getterDescription.getPredicateForErrorMessagePart2()).isEqualTo("can");

    getterDescription = new GetterDescription("", "shouldNotPlay", BOOLEAN_TYPE_DESCRIPTION, EMPTY_TYPENAME_LIST);
    assertThat(getterDescription.getPredicateForErrorMessagePart1()).isEqualTo("should not play");
    assertThat(getterDescription.getPredicateForErrorMessagePart2()).isEqualTo("should");
  }
}
