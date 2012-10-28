package org.fest.assertions.generator;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;

import static java.lang.Thread.currentThread;
import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.commons.io.IOUtils.copy;

public class Template {
  static final String DEFAULT_IS_ASSERTION = "is_assertion_template.txt";
  static final String DEFAULT_HAS_ELEMENTS_ASSERTION_FOR_ARRAY = "has_elements_assertion_template_for_array.txt";
  static final String DEFAULT_HAS_ELEMENTS_ASSERTION_FOR_ITERABLE = "has_elements_assertion_template_for_iterable.txt";
  static final String DEFAULT_HAS_ASSERTION = "has_assertion_template.txt";
  static final String DEFAULT_CUSTOM_ASSERTION_CLASS = "custom_assertion_class_template.txt";

  static final String TEMPLATES_DIR = "templates/"; // + File.separator;

  private final String content;

  private Template(String content) {
    this.content = content;
  }

  static Template fromClasspath(String templateFileName) {
    try {
      return readTemplateThenClose(currentThread().getContextClassLoader().getResourceAsStream(templateFileName));
    } catch (Exception e) {
      throw new RuntimeException("Failed to read " + templateFileName, e);
    }
  }

  static Template from(URL url) {

    if (url.getPath().endsWith("/")) throw new RuntimeException("Failed to read template from " + url);

    try {
      InputStream input = url.openStream();
      return readTemplateThenClose(input);
    } catch (IOException e) {
      throw new RuntimeException("Failed to read template from " + url, e);
    }
  }

  static private Template readTemplateThenClose(InputStream input) throws IOException {
    StringWriter writer = null;
    try {
      writer = new StringWriter();
      copy(input, writer);
      return new Template(writer.toString());
    } finally {
      closeQuietly(input);
      closeQuietly(writer);
    }
  }

  public String getContent() {
    return content;
  }
}
