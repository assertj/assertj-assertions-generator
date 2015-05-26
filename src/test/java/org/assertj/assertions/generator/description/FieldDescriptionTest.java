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
 * Copyright 2012-2014 the original author or authors.
 */
package org.assertj.assertions.generator.description;

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.assertions.generator.data.nba.Player;
import org.junit.Test;

public class FieldDescriptionTest {

  private DataDescription fieldDescription;
  private TypeDescription boolDesc;

  @Test
  public void should_create_valid_typename_from_class() {
    fieldDescription = new FieldDescription("bestPlayer", new TypeDescription(new TypeName(Player.class)));
    assertThat(fieldDescription.getName()).isEqualTo("bestPlayer");
    assertThat(fieldDescription.getTypeName()).isEqualTo("Player");
    assertThat(fieldDescription.getElementTypeName(Player.class.getPackage().getName())).isNull();
    boolDesc = new TypeDescription(new TypeName(boolean.class));
  }

  @Test
  public void should_show_information_in_toString() {
    fieldDescription = new FieldDescription("bestPlayer", new TypeDescription(new TypeName(Player.class)));
    assertThat(fieldDescription.toString()).contains("bestPlayer").contains(Player.class.getName());
  }

  @Test
  public void should_detect_number_correctly() throws Exception {
    fieldDescription = new FieldDescription("double", new TypeDescription(new TypeName(double.class)));
    assertThat(fieldDescription.isNumberType()).as("double").isTrue();
    fieldDescription = new FieldDescription("Double", new TypeDescription(new TypeName(Double.class)));
    assertThat(fieldDescription.isNumberType()).as("Double").isTrue();
    fieldDescription = new FieldDescription("float", new TypeDescription(new TypeName(float.class)));
    assertThat(fieldDescription.isNumberType()).as("float").isTrue();
    fieldDescription = new FieldDescription("Float", new TypeDescription(new TypeName(Float.class)));
    assertThat(fieldDescription.isNumberType()).as("Float").isTrue();
    fieldDescription = new FieldDescription("int", new TypeDescription(new TypeName(int.class)));
    assertThat(fieldDescription.isNumberType()).as("int").isTrue();
    fieldDescription = new FieldDescription("Integer", new TypeDescription(new TypeName(Integer.class)));
    assertThat(fieldDescription.isNumberType()).as("Integer").isTrue();

      fieldDescription = new FieldDescription("long", new TypeDescription(new TypeName(long.class)));
      assertThat(fieldDescription.isNumberType()).as("long").isTrue();
      fieldDescription = new FieldDescription("Long", new TypeDescription(new TypeName(Long.class)));
      assertThat(fieldDescription.isNumberType()).as("Long").isTrue();

      fieldDescription = new FieldDescription("short", new TypeDescription(new TypeName(short.class)));
      assertThat(fieldDescription.isNumberType()).as("short").isTrue();
      fieldDescription = new FieldDescription("Short", new TypeDescription(new TypeName(Short.class)));
      assertThat(fieldDescription.isNumberType()).as("Short").isTrue();

      fieldDescription = new FieldDescription("byte", new TypeDescription(new TypeName(byte.class)));
      assertThat(fieldDescription.isNumberType()).as("byte").isTrue();
      fieldDescription = new FieldDescription("Byte", new TypeDescription(new TypeName(Byte.class)));
      assertThat(fieldDescription.isNumberType()).as("Byte").isTrue();

    // not number types
    fieldDescription = new FieldDescription("String", new TypeDescription(new TypeName(String.class)));
    assertThat(fieldDescription.isNumberType()).as("String").isFalse();
  }
  
  @Test
  public void should_detect_predicate_correctly() throws Exception {
    TypeDescription boolDesc = new TypeDescription(new TypeName(boolean.class));
    String[] list = new String[] { "anything", "isSomething", "somethingElse" };
    for (String p : list) {
      fieldDescription = new FieldDescription(p, boolDesc);
      assertThat(fieldDescription.isPredicate()).as(p + ":bool").isTrue();
    }
    TypeDescription floatDesc = new TypeDescription(new TypeName(float.class));
    for (String p : list) {
      fieldDescription = new FieldDescription(p, floatDesc);
      assertThat(fieldDescription.isPredicate()).as(p + ":float").isFalse();
    }
  }
  
  @Test
  public void should_generate_default_predicate_correctly() throws Exception {
    fieldDescription = new FieldDescription("bad", boolDesc);
    assertThat(fieldDescription.getNegativePredicate()).as("negative").isEqualTo("isNotBad");
    assertThat(fieldDescription.getPredicate()).as("positive").isEqualTo("isBad");
  }

  @Test
  public void should_generate_readable_predicate_for_javadoc() {
    fieldDescription = new FieldDescription("bad", boolDesc);
    assertThat(fieldDescription.getPredicateForJavadoc()).isEqualTo("is bad");
    assertThat(fieldDescription.getNegativePredicateForJavadoc()).isEqualTo("is not bad");
  }

  @Test
  public void should_generate_readable_predicate_for_error_message() {
    fieldDescription = new FieldDescription("bad", boolDesc);
    assertThat(fieldDescription.getPredicateForErrorMessagePart1()).isEqualTo("is bad");
    assertThat(fieldDescription.getPredicateForErrorMessagePart2()).isEqualTo("is not");

    fieldDescription = new FieldDescription("canBeGood", boolDesc);
    assertThat(fieldDescription.getPredicateForErrorMessagePart1()).isEqualTo("can be good");
    assertThat(fieldDescription.getPredicateForErrorMessagePart2()).isEqualTo("cannot");
  }
}
