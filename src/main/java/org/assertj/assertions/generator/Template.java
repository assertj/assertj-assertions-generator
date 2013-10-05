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

import static java.lang.Thread.currentThread;
import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.commons.io.IOUtils.copy;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLDecoder;

import org.apache.commons.lang3.CharEncoding;

/**
 * 
 * Holds the template content for assertion generation, can be initialized from a {@link File} or an {@link URL}.
 * <p>
 * Template content example for <code>hasXXX</code> property assertion :
 * 
 * <pre>
 * public ${class_to_assert}Assert has${Property}(${propertyType} ${property}) {
 *   // check that actual ${class_to_assert} we want to make assertions on is not null.
 *   isNotNull();
 * 
 *   // we overrides the default error message with a more explicit one
 *   String errorMessage = format("Expected ${class_to_assert}'s ${property} to be <%s> but was <%s>", ${property}, actual.get${Property}());
 *   
 *   // check
 *   if (!actual.get${Property}().equals(${property})) { throw new AssertionError(errorMessage); }
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
   * Creates a new </code>{@link Template}</code> from the given content.
   * 
   * @param templateContent the template content
   */
  public Template(Type type, String templateContent) {
    this.content = templateContent;
    this.type = type;
  }

  /**
   * Creates a new </code>{@link Template}</code> from the content of the given {@link URL}.
   * 
   * @param url the {@link URL} to read to set the content of the {@link Template}
   * @throws RuntimeException if we fail to read the {@link URL} content
   */
  public Template(Type type, URL url) {
    this.type = type;
    try {
      File urlFile = new File(URLDecoder.decode(url.getFile(), CharEncoding.UTF_8));
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
   * Creates a new </code>{@link Template}</code> from the content of the given {@link File} searched in the classpath.
   * 
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

  public enum Type {
    IS, HAS_FOR_ARRAY, HAS_FOR_ITERABLE, HAS, HAS_FOR_PRIMITIVE, ASSERT_CLASS;
  }

}
