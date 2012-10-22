package org.fest.assertions.generator;

import java.io.InputStream;
import java.io.StringWriter;

import static java.lang.Thread.currentThread;
import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.commons.io.IOUtils.copy;

public class Template {
  static final String DEFAULT_IS_ASSERTION = "is_assertion_template.txt";
  static final String DEFAULT_HAS_ELEMENTS_ASSERTION_FOR_ARRAY = "has_elements_assertion_template_for_array.txt";
  static final String DEFAULT_HAS_ELEMENTS_ASSERTION_FOR_ITERABLE = "has_elements_assertion_template_for_iterable.txt";
  static final String DEFAULT_HAS_ASSERTION = "has_assertion_template.txt";
  static final String DEFAULT_CUSTOM_ASSERTION_CLASS = "custom_assertion_class_template.txt";

  private final String templatePath;
  private final String content;

  private Template(String templatePath, String content) {
    this.content = content;
    this.templatePath = templatePath;
  }

  static Template fromClasspath(String templateFileName) {
    InputStream inputStream = null;
    StringWriter writer = null;
    try {
      // load from classpath
      inputStream = currentThread().getContextClassLoader().getResourceAsStream(templateFileName);
      writer = new StringWriter();
      copy(inputStream, writer);
      return new Template(templateFileName, writer.toString());
    } catch (Exception e) {
      throw new RuntimeException("Failed to read " + templateFileName, e);
    } finally {
      closeQuietly(inputStream);
      closeQuietly(writer);
    }
  }

  public String getContent() {
    return content;
  }
}
