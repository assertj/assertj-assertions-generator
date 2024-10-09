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
 * Copyright 2012-2024 the original author or authors.
 */
package org.assertj.assertions.generator.description;

import com.google.common.reflect.TypeToken;
import org.assertj.assertions.generator.data.EnemyReport;
import org.assertj.assertions.generator.data.Name;
import org.assertj.assertions.generator.data.nba.Player;
import org.assertj.assertions.generator.data.nba.team.Coach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.assertj.assertions.generator.util.ClassUtil.propertyNameOf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchException;
import static org.assertj.core.api.BDDAssertions.then;

class FieldDescriptionTest {

  private static final TypeToken<Player> PLAYER_TYPE = TypeToken.of(Player.class);
  private FieldDescription fieldDescription;

  @Test
  void should_create_valid_typename_from_class() throws Exception {
    fieldDescription = new FieldDescription(Player.class.getDeclaredField("name"), PLAYER_TYPE);
    assertThat(fieldDescription.getName()).isEqualTo("name");
    assertThat(fieldDescription.getTypeName()).isEqualTo(Name.class.getName());
    assertThat(fieldDescription.getElementTypeName()).isNull();
    assertThat(fieldDescription.getFullyQualifiedTypeName()).isEqualTo("org.assertj.assertions.generator.data.Name");
  }

  @Test
  void should_create_valid_typename_from_class_for_user_defined_type_in_same_package() throws Exception {
    fieldDescription = new FieldDescription(Coach.class.getDeclaredField("team"), TypeToken.of(Coach.class));
    assertThat(fieldDescription.getName()).isEqualTo("team");
    assertThat(fieldDescription.getTypeName()).isEqualTo("Team");
    assertThat(fieldDescription.getFullyQualifiedTypeName()).isEqualTo("org.assertj.assertions.generator.data.nba.team.Team");
  }
  
  @Test
  void should_show_information_in_toString() throws Exception {
    fieldDescription = new FieldDescription(Player.class.getDeclaredField("name"), PLAYER_TYPE);
    assertThat(fieldDescription.toString()).contains("name", Player.class.getName(), Name.class.getName());
  }

  @Test
  void should_detect_real_number_correctly() throws Exception {
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
  void should_generate_default_predicate_correctly() throws Exception {
    fieldDescription = new FieldDescription(Player.class.getDeclaredField("bad"), PLAYER_TYPE);
    assertThat(fieldDescription.getNegativePredicate()).as("negative").isEqualTo("isNotBad");
    assertThat(fieldDescription.getPredicate()).as("positive").isEqualTo("isBad");
  }

  @Test
  void should_describe_array_field_correctly() throws Exception {
    fieldDescription = new FieldDescription(Player.class.getDeclaredField("previousTeamNames"), PLAYER_TYPE);
    assertThat(fieldDescription.getTypeName()).isEqualTo("String[]");
    assertThat(fieldDescription.getElementTypeName()).isEqualTo("String");
  }

  @Test
  void should_generate_readable_predicate_for_javadoc() throws Exception {
    fieldDescription = new FieldDescription(Player.class.getDeclaredField("bad"), PLAYER_TYPE);
    assertThat(fieldDescription.getPredicateForJavadoc()).isEqualTo("is bad");
    assertThat(fieldDescription.getNegativePredicateForJavadoc()).isEqualTo("is not bad");
  }

  @Test
  void should_generate_readable_predicate_for_error_message() throws Exception {
    fieldDescription = new FieldDescription(Player.class.getDeclaredField("bad"), PLAYER_TYPE);
    assertThat(fieldDescription.getPredicateForErrorMessagePart1()).isEqualTo("is bad");
    assertThat(fieldDescription.getPredicateForErrorMessagePart2()).isEqualTo("is not");

    fieldDescription = new FieldDescription(Player.class.getDeclaredField("isDisabled"), PLAYER_TYPE);
    assertThat(fieldDescription.getPredicateForErrorMessagePart1()).isEqualTo("is disabled");
    assertThat(fieldDescription.getPredicateForErrorMessagePart2()).isEqualTo("is not");
  }

  private static final TypeToken<FieldPropertyNames> FIELD_PROP_TYPE = TypeToken.of(FieldPropertyNames.class);

  @SuppressWarnings("unused")
  private static class FieldPropertyNames {
    public boolean isBoolean = true;

    public boolean isBoolean() {
      return isBoolean;
    }

    public boolean isNotBoolean() {
      return !isBoolean;
    }

    public String stringProp;

    public String getStringProp() {
      return stringProp;
    }

    public int onlyField;

    public String badGetter;

    public int getBadGetter() {
      return -1;
    }
  }

  @Test
  void should_find_getter_method_for_predicate_field() throws Exception {

    fieldDescription = new FieldDescription(FieldPropertyNames.class.getDeclaredField("isBoolean"), FIELD_PROP_TYPE);

    GetterDescription posGetter = new GetterDescription("boolean", FIELD_PROP_TYPE,
                                                        FieldPropertyNames.class.getMethod("isBoolean"));
    GetterDescription negGetter = new GetterDescription("boolean", FIELD_PROP_TYPE,
                                                        FieldPropertyNames.class.getMethod("isNotBoolean"));

    ClassDescription classDesc = new ClassDescription(FIELD_PROP_TYPE);
    classDesc.addGetterDescriptions(Arrays.asList(posGetter, negGetter));

    assertThat(classDesc.hasGetterForField(fieldDescription))
        .as("Correctly identifies there is a getter")
        .isTrue();
  }

  @Test
  void should_return_property_of_field() throws Exception {
    assertThat(propertyNameOf(FieldPropertyNames.class.getDeclaredField("isBoolean"))).isEqualTo("boolean");
    assertThat(propertyNameOf(EnemyReport.class.getDeclaredField("realTarget"))).isEqualTo("realTarget");
    assertThat(propertyNameOf(EnemyReport.class.getDeclaredField("privateTarget"))).isEqualTo("privateTarget");
    assertThat(propertyNameOf(EnemyReport.class.getDeclaredField("isTarget"))).isEqualTo("target");
  }

  @Test
  void should_find_getter_method_for_field() throws Exception {

    fieldDescription = new FieldDescription(FieldPropertyNames.class.getDeclaredField("stringProp"), FIELD_PROP_TYPE);

    GetterDescription getter = new GetterDescription("stringProp", FIELD_PROP_TYPE,
                                                     FieldPropertyNames.class.getMethod("getStringProp"));

    ClassDescription classDesc = new ClassDescription(FIELD_PROP_TYPE);
    classDesc.addGetterDescriptions(Collections.singletonList(getter));

    assertThat(classDesc.hasGetterForField(fieldDescription))
        .as("Correctly identifies there is a getter")
        .isTrue();
  }

  @Test
  void should_not_find_getter_method_for_mismatched_return_type() throws Exception {

    fieldDescription = new FieldDescription(FieldPropertyNames.class.getDeclaredField("badGetter"), FIELD_PROP_TYPE);

    GetterDescription getter = new GetterDescription("badGetter", FIELD_PROP_TYPE,
                                                     FieldPropertyNames.class.getMethod("getBadGetter"));

    ClassDescription classDesc = new ClassDescription(FIELD_PROP_TYPE);
    classDesc.addGetterDescriptions(Collections.singletonList(getter));

    assertThat(classDesc.hasGetterForField(fieldDescription))
        .as("Correctly identifies there is no getter")
        .isFalse();
  }

  @Test
  void should_not_find_getter_method_for_missing_getter() throws Exception {

    fieldDescription = new FieldDescription(FieldPropertyNames.class.getDeclaredField("onlyField"), FIELD_PROP_TYPE);

    ClassDescription classDesc = new ClassDescription(FIELD_PROP_TYPE);

    assertThat(classDesc.hasGetterForField(fieldDescription))
        .as("Correctly identifies there is no getter")
        .isFalse();
  }

  // Given that everything is written via reflection, we can not actually get access to a 
  // non-existant field anymore. Thus, there is no need to test it!
  //  @Test
  //  public void should_not_find_getter_method_for_non_existent_field()

  @Test
  void should_fail_find_with_npe_for_null_field_desc() {
    // GIVEN
    ClassDescription classDesc = new ClassDescription(FIELD_PROP_TYPE);
    // WHEN
    Exception exception = catchException(() -> classDesc.hasGetterForField(null));
    // THEN
    then(exception).isInstanceOf(NullPointerException.class);
  }

  @Test
  void should_fail_has_with_npe_for_null_field_desc() {
    // GIVEN
    ClassDescription classDesc = new ClassDescription(FIELD_PROP_TYPE);
    // WHEN
    Exception exception = catchException(() -> classDesc.hasGetterForField(null));
    // THEN
    then(exception).isInstanceOf(NullPointerException.class);
  }

}
