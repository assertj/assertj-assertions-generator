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
 * Copyright @2010-2011 the original author or authors.
 */
package org.fest.assertions.generator;

import static java.lang.String.format;
import static java.lang.Thread.currentThread;
import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.commons.io.IOUtils.copy;
import static org.apache.commons.lang3.StringUtils.capitalize;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Set;

import org.fest.assertions.generator.description.ClassDescription;
import org.fest.assertions.generator.description.GetterDescription;
import org.fest.assertions.generator.description.TypeName;

public class BaseAssertionGenerator implements AssertionGenerator {

  private static final String DEFAULT_IS_ASSERTION_TEMPLATE = "is_assertion_template.txt";
  private static final String DEFAULT_HAS_ELEMENTS_ASSERTION_TEMPLATE_FOR_ARRAY = "has_elements_assertion_template_for_array.txt";
  private static final String DEFAULT_HAS_ELEMENTS_ASSERTION_TEMPLATE_FOR_ITERABLE = "has_elements_assertion_template_for_iterable.txt";
  private static final String DEFAULT_HAS_ASSERTION_TEMPLATE = "has_assertion_template.txt";
  private static final String DEFAULT_CUSTOM_ASSERTION_CLASS_TEMPLATE = "custom_assertion_class_template.txt";
  static final String ASSERT_CLASS_SUFFIX = "Assert.java";
  private static final String IMPORT_LINE = "import %s;%s";
  private static final String PROPERTY_WITH_UPPERCASE_FIRST_CHAR_REGEXP = "\\$\\{Property\\}";
  private static final String PROPERTY_WITH_LOWERCASE_FIRST_CHAR_REGEXP = "\\$\\{property\\}";
  private static final String PACKAGE__REGEXP = "\\$\\{package\\}";
  private static final String PROPERTY_TYPE_REGEXP = "\\$\\{propertyType\\}";
  private static final String CLASS_TO_ASSERT_REGEXP = "\\$\\{class_to_assert\\}";
  private static final String ELEMENT_TYPE_REGEXP = "\\$\\{elementType\\}";
  private static final String IMPORTS = "${imports}";
  private static final String LINE_SEPARATOR = System.getProperty("line.separator");
  private static final String TEMPLATES_DIR = "templates/"; // + File.separator;
  
  private String targetDirectory = ".";
  private String classAssertionTemplate;
  private String hasAssertionTemplate;
  private String hasIterableElementsAssertionTemplate;
  private String hasArrayElementsAssertionTemplate;
  private String isAssertionTemplate;

  /**
   * Creates a new </code>{@link BaseAssertionGenerator}</code> with default templates directory.
   * 
   * @throws FileNotFoundException if some template file could not be found
   * @throws IOException if some template file could not be read
   */
  public BaseAssertionGenerator() throws FileNotFoundException, IOException {
    this(TEMPLATES_DIR);
  }

  /**
   * Creates a new </code>{@link BaseAssertionGenerator}</code> with default templates directory.
   * 
   * @throws FileNotFoundException if some template file could not be found
   * @throws IOException if some template file could not be read
   */
  public BaseAssertionGenerator(String templatesDirectory) throws FileNotFoundException, IOException {
    super();
    setAssertionClassTemplateFileName(templatesDirectory + DEFAULT_CUSTOM_ASSERTION_CLASS_TEMPLATE);
    setHasAssertionTemplateFileName(templatesDirectory + DEFAULT_HAS_ASSERTION_TEMPLATE);
    setHasElementsAssertionForIterableTemplateFileName(templatesDirectory + DEFAULT_HAS_ELEMENTS_ASSERTION_TEMPLATE_FOR_ITERABLE);
    setHasElementsAssertionForArrayTemplateFileName(templatesDirectory + DEFAULT_HAS_ELEMENTS_ASSERTION_TEMPLATE_FOR_ARRAY);
    setIsAssertionTemplateFileName(templatesDirectory + DEFAULT_IS_ASSERTION_TEMPLATE);
  }

  /**
   * Defines your own class template file name.
   * @param assertionClassTemplateFileName
   */
  public void setAssertionClassTemplateFileName(String assertionClassTemplateFileName) {
    this.classAssertionTemplate = readTemplate(assertionClassTemplateFileName);
  }

  public void setHasAssertionTemplateFileName(String hasAssertionTemplateFileName) {
    this.hasAssertionTemplate = readTemplate(hasAssertionTemplateFileName);
  }

  public void setHasElementsAssertionForIterableTemplateFileName(String hasIterableElementsAssertionTemplateFileName) {
    this.hasIterableElementsAssertionTemplate = readTemplate(hasIterableElementsAssertionTemplateFileName);
  }

  public void setHasElementsAssertionForArrayTemplateFileName(String hasArrayElementsAssertionTemplateFileName) {
    this.hasArrayElementsAssertionTemplate = readTemplate(hasArrayElementsAssertionTemplateFileName);
  }

  public void setIsAssertionTemplateFileName(String isAssertionTemplateFileName) {
    this.isAssertionTemplate = readTemplate(isAssertionTemplateFileName);
  }

  public void setDirectoryWhereAssertionFilesAreGenerated(String targetDirectory) {
    this.targetDirectory = targetDirectory;
  }

  /** {@inheritDoc} */
  public File generateCustomAssertionFor(ClassDescription classDescription) throws IOException {

    // use class template first
    StringBuilder assertionFileContentBuilder = new StringBuilder(classAssertionTemplate);

    // generate assertion method for each property with a public getter
    assertionFileContentBuilder.append(generateAssertionsForGettersOf(classDescription));

    // close class with }
    assertionFileContentBuilder.append(LINE_SEPARATOR).append("}").append(LINE_SEPARATOR);

    String className = classDescription.getClassName();
    // resolve template markers
    String assertionFileContent = assertionFileContentBuilder.toString();
    assertionFileContent = assertionFileContent.replaceAll(PACKAGE__REGEXP, classDescription.getPackageName());
    assertionFileContent = assertionFileContent.replaceAll(CLASS_TO_ASSERT_REGEXP, className);
    assertionFileContent = assertionFileContent.replace(IMPORTS, listImports(classDescription.getImports(), classDescription.getPackageName()));

    // finally create the assertion file
    String packageDirs = targetDirectory + File.separator + classDescription.getPackageName().replace('.', File.separatorChar);
    new File(packageDirs).mkdirs();
    return createCustomAssertionFile(assertionFileContent, className + ASSERT_CLASS_SUFFIX, packageDirs);
  }

  private static String listImports(Set<TypeName> typesToImport, String assertClassPackage) {
    StringBuilder importsBuilder = new StringBuilder();
    for (TypeName type : typesToImport) {
      if (!type.isPrimitive() && !type.belongsToJavaLangPackage() && !type.getPackageName().equals(assertClassPackage)) {
        importsBuilder.append(format(IMPORT_LINE, type, LINE_SEPARATOR));
      }
    }
    return importsBuilder.toString();
  }

  protected String generateAssertionsForGettersOf(ClassDescription classDescription) {
    StringBuilder assertionsForGetters = new StringBuilder();
    Set<GetterDescription> getters = classDescription.getGetters();
    for (GetterDescription getter : getters) {
      String assertionContent = assertionContentFor(getter);
      assertionsForGetters.append(assertionContent).append(LINE_SEPARATOR);
    }
    return assertionsForGetters.toString();
  }

  private String assertionContentFor(GetterDescription getter) {
    // sets default content (most likely case)
    String assertionContent = hasAssertionTemplate;
    if (getter.isBooleanPropertyType()) {
      assertionContent = isAssertionTemplate;
    } else if (getter.isIterablePropertyType()) {
      assertionContent = hasIterableElementsAssertionTemplate.replaceAll(ELEMENT_TYPE_REGEXP, getter.getElementTypeName());
    } else if (getter.isArrayPropertyType()) {
      assertionContent = hasArrayElementsAssertionTemplate.replaceAll(ELEMENT_TYPE_REGEXP, getter.getElementTypeName());
    }
    if (getter.isPrimitivePropertyType()) {
      // primitive must be compared with != instead of (not) equals()
      assertionContent = assertionContent.replace("!actual.get${Property}().equals(${property})",
          "actual.get${Property}() != ${property}");
    }
    String propertyName = getter.getPropertyName();
    assertionContent = assertionContent.replaceAll(PROPERTY_WITH_UPPERCASE_FIRST_CHAR_REGEXP, capitalize(propertyName));
    assertionContent = assertionContent.replaceAll(PROPERTY_TYPE_REGEXP, getter.getPropertyTypeName());
    return assertionContent.replaceAll(PROPERTY_WITH_LOWERCASE_FIRST_CHAR_REGEXP, propertyName);
  }

  private void fillAssertionJavaFile(String customAssertionContent, File assertionJavaFile) throws IOException {
    FileWriter fileWriter = null;
    try {
      fileWriter = new FileWriter(assertionJavaFile);
      fileWriter.write(customAssertionContent);
    } finally {
      closeQuietly(fileWriter);
    }
  }

  private static String readTemplate(String templateFileName) {
    InputStream inputStream= null;
    StringWriter writer= null;
    try {
      // load from classpath
      inputStream = currentThread().getContextClassLoader().getResourceAsStream(templateFileName);
      writer = new StringWriter();
      copy(inputStream, writer);
      return writer.toString();
    } catch (Exception e) {
      throw new RuntimeException("Failed to read " + templateFileName, e);
    } finally {
      closeQuietly(inputStream);
      closeQuietly(writer);
    }
  }

  private File createCustomAssertionFile(String assertionFileContent, String assertionFileName, String targetDirectory)
      throws IOException {
    File assertionJavaFile = new File(targetDirectory, assertionFileName);
    assertionJavaFile.createNewFile();
    fillAssertionJavaFile(assertionFileContent, assertionJavaFile);
    return assertionJavaFile;
  }

}
