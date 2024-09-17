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
 * Copyright 2012-2021 the original author or authors.
 */
package org.assertj.assertions.generator.util;

import static org.assertj.assertions.generator.util.StringUtil.camelCaseToWords;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class StringUtilTest {

  @Test
  public void testCamelCaseToWords() throws Exception {
    assertThat(camelCaseToWords("BestPlayer")).isEqualTo("best player");
    assertThat(camelCaseToWords("Run")).isEqualTo("run");
    assertThat(camelCaseToWords("RunFasterThanLight")).isEqualTo("run faster than light");
    assertThat(camelCaseToWords("RunAMillionTime")).isEqualTo("run a million time");
  }

}
