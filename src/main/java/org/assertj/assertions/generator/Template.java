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
 * Copyright 2012-2025 the original author or authors.
 */
/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.assertj.assertions.generator;

import com.google.common.io.CharStreams;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import static com.google.common.io.Closeables.closeQuietly;
import static java.lang.Thread.currentThread;

/**
 * 
 * Holds the template content for assertion generation, can be initialized from a {@link File} or an {@link URL}.
 * <p>
 * Template content example for <code>hasXXX</code> property assertion :
 * 
 * <pre>
 * public ${custom_assertion_class} has${Property}(${propertyType} ${property}) {
 *   // check that actual ${class_to_assert} we want to make assertions on is not null.
 *   isNotNull();
 * 
 *   // we overrides the default error message with a more explicit one
 *   String assertjErrorMessage = format("Expected ${class_to_assert}'s ${property} to be &lt;%s&gt; but was &lt;%s&gt;", ${property}, actual.get${Property}());
 *   
 *   // check
 *   if (!actual.get${Property}().equals(${property})) { throw new AssertionError(assertjErrorMessage); }
 * 
 *   // return the current assertion for method chaining
 *   return this;
 * }
 * </pre>
 * 
 * @author Miguel Bazire
 * @author Joel Costigliola
 */
public class Template {

  private String content;
  private final Type type;

  /**
   * Creates a new <code>{@link Template}</code> from the given content.
   * 
   * @param type the type
   * @param templateContent the template content
   */
  public Template(Type type, String templateContent) {
    this.content = templateContent;
    this.type = type;
  }

  /**
   * Creates a new <code>{@link Template}</code> from the content of the given {@link URL}.
   *
   * @param type the type
   * @param url the {@link URL} to read to set the content of the {@link Template}
   * @throws RuntimeException if we fail to read the {@link URL} content
   */
  public Template(Type type, URL url) {
    this.type = type;
    try {
      File urlFile = new File(URLDecoder.decode(url.getFile(), StandardCharsets.UTF_8.name()));
      if (!urlFile.isFile()) {
        throw new RuntimeException("Failed to read template from an URL which is not a file, URL was :" + url);
      }
      // TODO : read from file directly ?
      content = readContentThenClose(url.openStream());
    } catch (IOException e) {
      throw new RuntimeException("Failed to read template from " + url, e);
    }
  }

  /**
   * Creates a new <code>{@link Template}</code> from the content of the given {@link File} searched in the classpath.
   *
   * @param type the type
   * @param file the {@link File} to read to set the content of the {@link Template}
   * @throws RuntimeException if we fail to read the {@link File} content
   */
  public Template(Type type, File file) {
    this.type = type;
    // don't use file.toURI().toURL() to call Template based URL constructor :
    // it does not load resources from classpath.
    String path = file.getPath();
    try {
      // TODO see if we can get rid of this and only call readTemplateFile(file, path.replace('\\', '/'));
      this.content = readTemplateFile(file, file.getPath());
    } catch (RuntimeException e) {
      // try to read file again but from assert-assertions-generator jar
      // => need to replace file system separator by '/' because it relies o
      this.content = readTemplateFile(file, path.replace('\\', '/'));
    }
  }

  private String readTemplateFile(File file, String path) {
    try {
      InputStream inputStream = currentThread().getContextClassLoader().getResourceAsStream(path);
      return readContentThenClose(inputStream);
    } catch (Exception e) {
      throw new RuntimeException("Failed to read template from file " + file, e);
    }
  }

  public String getContent() {
    return content;
  }

  public Type getType() {
    return type;
  }

  private String readContentThenClose(InputStream input) throws IOException {
    InputStreamReader reader = new InputStreamReader(input);
    try {
      return CharStreams.toString(reader);
    } finally {
      closeQuietly(input);
      closeQuietly(reader);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Template template = (Template) o;
    return type != template.type ? false : true;
  }

  @Override
  public int hashCode() {
    return type.hashCode();
  }

  public enum Type {
    // @format:off
    IS,
    IS_WITHOUT_NEGATION,
    IS_WRAPPER,
    IS_WRAPPER_WITHOUT_NEGATION,
    HAS,
    HAS_FOR_ARRAY, 
    HAS_FOR_ITERABLE, 
    HAS_FOR_PRIMITIVE, 
    HAS_FOR_PRIMITIVE_WRAPPER, 
    HAS_FOR_REAL_NUMBER, 
    HAS_FOR_REAL_NUMBER_WRAPPER, 
    HAS_FOR_WHOLE_NUMBER,
    HAS_FOR_WHOLE_NUMBER_WRAPPER,
    HAS_FOR_CHAR, 
    HAS_FOR_CHARACTER, 
    ASSERT_CLASS, 
    HIERARCHICAL_ASSERT_CLASS, 
    ABSTRACT_ASSERT_CLASS, 
    ASSERTIONS_ENTRY_POINT_CLASS, 
    ASSERTION_ENTRY_POINT, 
    SOFT_ASSERTIONS_ENTRY_POINT_CLASS, 
    JUNIT_SOFT_ASSERTIONS_ENTRY_POINT_CLASS, 
    SOFT_ENTRY_POINT_METHOD_ASSERTION, 
    BDD_ASSERTIONS_ENTRY_POINT_CLASS,
    BDD_ENTRY_POINT_METHOD_ASSERTION,
    BDD_SOFT_ASSERTIONS_ENTRY_POINT_CLASS,
    BDD_SOFT_ENTRY_POINT_METHOD_ASSERTION,
    JUNIT_BDD_SOFT_ASSERTIONS_ENTRY_POINT_CLASS,
    AUTO_CLOSEABLE_SOFT_ASSERTIONS_ENTRY_POINT_CLASS,
    AUTO_CLOSEABLE_BDD_SOFT_ASSERTIONS_ENTRY_POINT_CLASS
    // @format:on
  }

}
