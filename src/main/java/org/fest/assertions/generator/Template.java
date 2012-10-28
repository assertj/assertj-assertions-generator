package org.fest.assertions.generator;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;

import static java.lang.Thread.currentThread;
import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.commons.io.IOUtils.copy;

public class Template {

  public enum Type {
    IS("is_assertion_template.txt"),
    HAS_FOR_ARRAY("has_elements_assertion_template_for_array.txt"),
    HAS_FOR_ITERABLE("has_elements_assertion_template_for_iterable.txt"),
    HAS("has_assertion_template.txt"),
    CUSTOM("custom_assertion_class_template.txt");

    public final String defaultFileName;

    private Type(String defaultFileName) {
      this.defaultFileName = defaultFileName;
    }
  }

  static final String TEMPLATES_DIR = "templates/";

  private final String content;
  private final Type assertionType;

  private Template(Type assertionType, String content) {
    this.content = content;
    this.assertionType = assertionType;
  }

  public static Factory of(Type assertionType) {
    return new Factory(assertionType);
  }

  String getContent() {
    return content;
  }

  public static class Factory {

    private final Type selectedType;
    private String templateContent;
    private String path = TEMPLATES_DIR;

    Factory(Type assertionTemplateType) {
      this.selectedType = assertionTemplateType;
    }

    public Factory in(String directory){
      this.path = directory;
      return this;
    }

    public Factory from(URL url) {

      if (url.getPath().endsWith("/")) throw new RuntimeException("Failed to read template from " + url);

      try {
        InputStream input = url.openStream();
        templateContent = readContentThenClose(input);
      } catch (IOException e) {
        throw new RuntimeException("Failed to read template from " + url, e);
      }
      return this;
    }

    public Factory fromClasspath(String filePath) {
      try {
        templateContent = readContentThenClose(currentThread().getContextClassLoader().getResourceAsStream(filePath));
      } catch (Exception e) {
        throw new RuntimeException("Failed to read " + filePath, e);
      }
      return this;
    }

    public Template create() {
      try{
        if(templateContent == null) loadFromDefaultLocation();
        return new Template(selectedType,templateContent);
      }finally {
        templateContent = null;
        path = TEMPLATES_DIR;
      }
    }

    private void loadFromDefaultLocation(){
      fromClasspath(path + selectedType.defaultFileName);
    }

    private String readContentThenClose(InputStream input) throws IOException {
      StringWriter writer = null;
      try {
        writer = new StringWriter();
        copy(input, writer);
        return writer.toString();
      } finally {
        closeQuietly(input);
        closeQuietly(writer);
      }
    }

  }
}
