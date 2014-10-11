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
    assertThat(getterDescription.getTypeName()).isEqualTo("Player");
    assertThat(getterDescription.getElementTypeName()).isNull();
  }

  @Test
  public void should_show_information_in_toString() {
    getterDescription = new GetterDescription("bestPlayer", new TypeDescription(new TypeName(Player.class)), Collections.<TypeName>emptyList());
    assertThat(getterDescription.toString()).contains("bestPlayer").contains(Player.class.getName());
  }
}
