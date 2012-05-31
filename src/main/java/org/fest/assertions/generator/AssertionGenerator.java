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
import static org.apache.commons.io.IOUtils.closeQuietly;

import static org.fest.util.Collections.list;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;

// TODO use fluent API to generate file ? something like : generateAssertFor(class).inDirectory(dir).execute()
public class AssertionGenerator {

  private static final String ASSERT_CLASS_SUFFIX = "Assert.java";
  private static final String JAVA_LANG_PACKAGE = "java.lang";
  private static final String IMPORT_LINE = "import %s.%s;%s";
  private static final String PROPERTY_WITH_UPPERCASE_FIRST_CHAR_REGEXP = "\\$\\{Property\\}";
  private static final String PROPERTY_WITH_LOWERCASE_FIRST_CHAR_REGEXP = "\\$\\{property\\}";
  private static final String PACKAGE__REGEXP = "\\$\\{package\\}";
  private static final String PROPERTY_TYPE_REGEXP = "\\$\\{propertyType\\}";
  private static final String CLASS_TO_ASSERT_REGEXP = "\\$\\{class_to_assert\\}";
  private static final String IMPORTS = "${imports}";
  private static final String GET_PREFIX = "get";
  private static final String IS_PREFIX = "is";
  private static final String LINE_SEPARATOR = System.getProperty("line.separator");
  private static final String TEMPLATES_DIR = "./templates/"; // + File.separator;

  private String classAssertionTemplate;
  private String classAssertionTemplateFileName = TEMPLATES_DIR + "custom_assertion_class_template.txt";
  private String hasAssertionTemplate;
  private String hasAssertionTemplateFileName = TEMPLATES_DIR + "has_assertion_template.txt";
  private String isAssertionTemplate;
  private String isAssertionTemplateFileName = TEMPLATES_DIR + "is_assertion_template.txt";
  private String targetDirectory = "."; 

  /**
   * Creates a new </code>{@link AssertionGenerator}</code>.
   * 
   * @throws FileNotFoundException
   * @throws IOException
   */
  public AssertionGenerator() throws FileNotFoundException, IOException {
    super();
    classAssertionTemplate = readClassAssertionTemplate();
    hasAssertionTemplate = readHasAssertionTemplate();
    isAssertionTemplate = readIsAssertionTemplate();
  }

  public void setAssertionClassTemplateFileName(String assertionClassTemplateFileName) {
    this.classAssertionTemplateFileName = assertionClassTemplateFileName;
    this.classAssertionTemplate = readClassAssertionTemplate();
  }

  public void setHasAssertionTemplateFileName(String hasAssertionTemplateFileName) {
    this.hasAssertionTemplateFileName = hasAssertionTemplateFileName;
    this.hasAssertionTemplate = readHasAssertionTemplate();
  }

  public void setIsAssertionTemplateFileName(String isAssertionTemplateFileName) {
    this.isAssertionTemplateFileName = isAssertionTemplateFileName;
    this.isAssertionTemplate = readIsAssertionTemplate();
  }

  public void setDirectoryWhereAssertionFilesAreGenerated(String targetDirectory) {
    this.targetDirectory = targetDirectory;
  }

  /**
   * Builds and returns the the custom assertion java file for the given class.
   * <p>
   * Example with the Player below
   * 
   * <pre></pre>
   * Generated assertions class will be :
   * 
   * <pre></pre>
   * @param clazz the class we want to have assertion for.
   * @return the custom assertion java file for the given class
   * @throws IOException
   */
  public File generateCustomAssertion(Class<?> clazz) throws IOException {

    // use class template first
    StringBuilder assertionFileContentBuilder = new StringBuilder(classAssertionTemplate);

    // generate assertion method for each property with a public getter
    assertionFileContentBuilder.append(generateAssertionsForGettersOf(clazz));

    // generate assertion method for each boolean property with a public getter (isXXX)
    assertionFileContentBuilder.append(generateAssertionsForBooleanGettersOf(clazz));

    // close class with }
    assertionFileContentBuilder.append(LINE_SEPARATOR).append("}").append(LINE_SEPARATOR);

    // resolve template markers
    String assertionFileContent = assertionFileContentBuilder.toString();
    assertionFileContent = assertionFileContent.replaceAll(PACKAGE__REGEXP, clazz.getPackage().getName());
    assertionFileContent = assertionFileContent.replaceAll(CLASS_TO_ASSERT_REGEXP, clazz.getSimpleName());
    assertionFileContent = assertionFileContent.replace(IMPORTS, generateNeededImportsFor(clazz));

    // finally create the assertion file 
    String assertionFileName = clazz.getSimpleName() + ASSERT_CLASS_SUFFIX;
    return createCustomAssertionFile(assertionFileContent, assertionFileName, targetDirectory);
  }

  protected String generateAssertionsForBooleanGettersOf(Class<?> clazz) {
    StringBuilder assertionsForBooleanGetters = new StringBuilder();
    List<Method> booleanGetterMethods = booleanGetterMethodsOf(clazz);
    for (Method booleanGetter : booleanGetterMethods) {
      String isAssertionContent = isAssertionTemplate;
      String propertyName = booleanGetter.getName().substring(IS_PREFIX.length());
      isAssertionContent = isAssertionContent.replaceAll(CLASS_TO_ASSERT_REGEXP, clazz.getSimpleName());
      isAssertionContent = isAssertionContent.replaceAll(PROPERTY_WITH_UPPERCASE_FIRST_CHAR_REGEXP, propertyName);
      isAssertionContent = isAssertionContent.replaceAll(PROPERTY_TYPE_REGEXP, booleanGetter.getReturnType().getSimpleName());
      // lowercase the first character
      String propertyNameWithLowercasedFirstChar = lowercaseFirstCharOf(propertyName);
      isAssertionContent = isAssertionContent.replaceAll(PROPERTY_WITH_LOWERCASE_FIRST_CHAR_REGEXP,
          propertyNameWithLowercasedFirstChar);
      assertionsForBooleanGetters.append(isAssertionContent);
    }
    return assertionsForBooleanGetters.toString();
  }

  protected String generateAssertionsForGettersOf(Class<?> clazz) {
    StringBuilder assertionsForGetters = new StringBuilder();
    List<Method> getters = getterMethodsOf(clazz);
    for (Method getter : getters) {
      String hasAssertionContent = hasAssertionTemplate;
      if (getter.getReturnType().isPrimitive()) {
        // primitive must be compared with != instead of !equals()
        hasAssertionContent = hasAssertionContent.replace("!actual.get${Property}().equals(${property})",
            "actual.get${Property}() != ${property}");
      }
      String propertyName = getter.getName().substring(GET_PREFIX.length());
      hasAssertionContent = hasAssertionContent.replaceAll(CLASS_TO_ASSERT_REGEXP, clazz.getSimpleName());
      hasAssertionContent = hasAssertionContent.replaceAll(PROPERTY_WITH_UPPERCASE_FIRST_CHAR_REGEXP, propertyName);
      hasAssertionContent = hasAssertionContent.replaceAll(PROPERTY_TYPE_REGEXP, getter.getReturnType().getSimpleName());
      // lowercase the first character
      String propertyNameWithLowercasedFirstChar = lowercaseFirstCharOf(propertyName);
      hasAssertionContent = hasAssertionContent.replaceAll(PROPERTY_WITH_LOWERCASE_FIRST_CHAR_REGEXP,
          propertyNameWithLowercasedFirstChar);
      assertionsForGetters.append(hasAssertionContent);
    }
    return assertionsForGetters.toString();
  }

  protected String generateNeededImportsFor(Class<?> clazz) {
    StringBuilder imports = new StringBuilder();
    for (Method getter : getterMethodsOf(clazz)) {
      Class<?> propertyType = getter.getReturnType();
      // no need to generate import for types in java.lang
      if (!propertyType.isPrimitive() && !propertyType.getPackage().getName().equals(JAVA_LANG_PACKAGE)) {
        imports.append(format(IMPORT_LINE, propertyType.getPackage().getName(), propertyType.getSimpleName(), LINE_SEPARATOR));
      }
    }
    return imports.toString();
  }

  private static String lowercaseFirstCharOf(String propertyName) {
    String firstChar = propertyName.substring(0, 1);
    String propertyNameWithLowercasedFirstChar = propertyName.replace(firstChar, firstChar.toLowerCase());
    return propertyNameWithLowercasedFirstChar;
  }

  private List<Method> getterMethodsOf(Class<?> clazz) {
    Method[] declaredMethods = clazz.getDeclaredMethods();
    List<Method> getters = list();
    for (int i = 0; i < declaredMethods.length; i++) {
      Method method = declaredMethods[i];
      // if (method.isAccessible() && (method.getName().startsWith("get") || method.getName().startsWith("is"))) {
      if (method.getName().startsWith(GET_PREFIX) && method.getReturnType() != null) {
        // probably a getter
        getters.add(method);
      }
    }
    return getters;
  }

  private List<Method> booleanGetterMethodsOf(Class<?> clazz) {
    Method[] declaredMethods = clazz.getDeclaredMethods();
    List<Method> booleanGetters = list();
    for (int i = 0; i < declaredMethods.length; i++) {
      Method method = declaredMethods[i];
      // if (method.isAccessible() && (method.getName().startsWith("get") || method.getName().startsWith("is"))) {
      if (method.getName().startsWith(IS_PREFIX) && Boolean.TYPE.equals(method.getReturnType())) {
        // probably a getter
        booleanGetters.add(method);
      }
    }
    return booleanGetters;
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

  private String readClassAssertionTemplate() {
    BufferedReader reader = null;
    try {
      reader = new BufferedReader(new FileReader(classAssertionTemplateFileName));
      String line = null;
      StringBuilder classAssertTemplateBuilder = new StringBuilder();
      while ((line = reader.readLine()) != null) {
        classAssertTemplateBuilder.append(line).append(LINE_SEPARATOR);
      }
      return classAssertTemplateBuilder.toString();
    } catch (Exception e) {
      throw new RuntimeException("Failed to read " + classAssertionTemplateFileName, e);
    } finally {
      closeQuietly(reader);
    }
  }

  private String readHasAssertionTemplate() {
    BufferedReader reader = null;
    try {
      reader = new BufferedReader(new FileReader(hasAssertionTemplateFileName));
      String line = null;
      StringBuilder hasAssertionTemplateBuilder = new StringBuilder();
      while ((line = reader.readLine()) != null) {
        hasAssertionTemplateBuilder.append(line).append(LINE_SEPARATOR);
      }
      return hasAssertionTemplateBuilder.toString();
    } catch (Exception e) {
      throw new RuntimeException("Failed to read " + hasAssertionTemplateFileName, e);
    } finally {
      closeQuietly(reader);
    }
  }

  private String readIsAssertionTemplate() {
    BufferedReader reader = null;
    try {
      reader = new BufferedReader(new FileReader(isAssertionTemplateFileName));
      String line = null;
      StringBuilder isAssertionTemplateBuilder = new StringBuilder();
      while ((line = reader.readLine()) != null) {
        isAssertionTemplateBuilder.append(line).append(LINE_SEPARATOR);
      }
      return isAssertionTemplateBuilder.toString();
    } catch (Exception e) {
      throw new RuntimeException("Failed to read " + isAssertionTemplateFileName, e);
    } finally {
      closeQuietly(reader);
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
