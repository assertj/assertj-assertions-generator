package org.assertj.assertions.generator.util;

import com.google.common.base.CaseFormat;

public class StringUtil {

  public static String camelCaseToWords(String camleCaseString) {
    return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, camleCaseString).replace('_', ' ');
  }

}
