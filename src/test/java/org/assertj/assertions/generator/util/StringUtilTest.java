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
