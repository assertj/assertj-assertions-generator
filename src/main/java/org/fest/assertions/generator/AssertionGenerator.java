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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// TODO use fluent API to generate file ? something like : generateAssertFor(class).inDirectory(dir).execute()
// TODO hasNoTeamMates
public class AssertionGenerator {

  private static final String ASSERT_CLASS_SUFFIX = "Assert.java";
  private static final String JAVA_LANG_PACKAGE = "java.lang";
  private static final String IMPORT_LINE = "import %s.%s;%s";
  private static final String PROPERTY_WITH_UPPERCASE_FIRST_CHAR_REGEXP = "\\$\\{Property\\}";
  private static final String PROPERTY_WITH_LOWERCASE_FIRST_CHAR_REGEXP = "\\$\\{property\\}";
  private static final String PACKAGE__REGEXP = "\\$\\{package\\}";
  private static final String PROPERTY_TYPE_REGEXP = "\\$\\{propertyType\\}";
  private static final String CLASS_TO_ASSERT_REGEXP = "\\$\\{class_to_assert\\}";
  private static final String ELEMENT_TYPE_REGEXP = "\\$\\{elementType\\}";
  private static final String IMPORTS = "${imports}";
  private static final String GET_PREFIX = "get";
  private static final String IS_PREFIX = "is";
  private static final String LINE_SEPARATOR = System.getProperty("line.separator");
  private static final String TEMPLATES_DIR = "./templates/"; // + File.separator;

  private String classAssertionTemplate;
  private String hasAssertionTemplate;
  private String hasIterableElementsAssertionTemplate;
  private String hasArrayElementsAssertionTemplate;
  private String isAssertionTemplate;
  private String targetDirectory = ".";

  /**
   * Creates a new </code>{@link AssertionGenerator}</code> with default templates directory.
   * 
   * @throws FileNotFoundException if some template file could not be found
   * @throws IOException if some template file could not be read
   */
  public AssertionGenerator() throws FileNotFoundException, IOException {
    this(TEMPLATES_DIR);
  }

  /**
   * Creates a new </code>{@link AssertionGenerator}</code> with default templates directory.
   * 
   * @throws FileNotFoundException if some template file could not be found
   * @throws IOException if some template file could not be read
   */
  public AssertionGenerator(String templatesDirectory) throws FileNotFoundException, IOException {
    super();
    setAssertionClassTemplateFileName(templatesDirectory + "custom_assertion_class_template.txt");
    setHasAssertionTemplateFileName(templatesDirectory + "has_assertion_template.txt");
    setHasElementsAssertionForIterableTemplateFileName(templatesDirectory + "has_elements_assertion_template_for_iterable.txt");
    setHasElementsAssertionForArrayTemplateFileName(templatesDirectory + "has_elements_assertion_template_for_array.txt");
    setIsAssertionTemplateFileName(templatesDirectory + "is_assertion_template.txt");
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
      Class<?> returnTypeName = booleanGetter.getReturnType();
      String propertyName = booleanGetter.getName().substring(IS_PREFIX.length());
      isAssertionContent = replacePropertyNameAndGetterReturnType(isAssertionContent, returnTypeName, propertyName);
      assertionsForBooleanGetters.append(isAssertionContent);
    }
    return assertionsForBooleanGetters.toString();
  }

  private static String replacePropertyNameAndGetterReturnType(String assertionContent, Class<?> returnType, String propertyName) {
    assertionContent = assertionContent.replaceAll(PROPERTY_WITH_UPPERCASE_FIRST_CHAR_REGEXP, propertyName);
    assertionContent = assertionContent.replaceAll(PROPERTY_TYPE_REGEXP, returnType.getSimpleName());
    assertionContent = assertionContent.replaceAll(PROPERTY_WITH_LOWERCASE_FIRST_CHAR_REGEXP, lowercaseFirstCharOf(propertyName));
    return assertionContent;
  }

  protected String generateAssertionsForGettersOf(Class<?> clazz) {
    StringBuilder assertionsForGetters = new StringBuilder();
    List<Method> getters = getterMethodsOf(clazz);
    for (Method getter : getters) {
      String assertionContent = hasAssertionTemplate;
      Class<?> returnType = getter.getReturnType();
      if (isIterable(returnType)) {
        assertionContent = hasIterableElementsAssertionTemplate;
        ParameterizedType parameterizedType = (ParameterizedType) getter.getGenericReturnType();
        Class<?> actualParameterClass = (Class<?>) parameterizedType.getActualTypeArguments()[0];
        assertionContent = assertionContent.replaceAll(ELEMENT_TYPE_REGEXP, actualParameterClass.getSimpleName());
      } else if (returnType.isArray()) {
        assertionContent = hasArrayElementsAssertionTemplate;
        assertionContent = assertionContent.replaceAll(ELEMENT_TYPE_REGEXP, returnType.getComponentType().getSimpleName());
      } else if (returnType.isPrimitive()) {
        // primitive must be compared with != instead of !equals()
        assertionContent = assertionContent.replace("!actual.get${Property}().equals(${property})",
            "actual.get${Property}() != ${property}");
      }
      String propertyName = getter.getName().substring(GET_PREFIX.length());
      assertionContent = assertionContent.replaceAll(PROPERTY_WITH_UPPERCASE_FIRST_CHAR_REGEXP, propertyName);
      assertionContent = assertionContent.replaceAll(PROPERTY_TYPE_REGEXP, returnType.getSimpleName());
      // lowercase the first character
      String propertyNameWithLowercasedFirstChar = lowercaseFirstCharOf(propertyName);
      assertionContent = assertionContent.replaceAll(PROPERTY_WITH_LOWERCASE_FIRST_CHAR_REGEXP,
          propertyNameWithLowercasedFirstChar);
      assertionsForGetters.append(assertionContent);
    }
    return assertionsForGetters.toString();
  }

  private static boolean isIterable(Class<?> returnType) {
    return Iterable.class.isAssignableFrom(returnType);
  }

  private String generateNeededImportsFor(Class<?> clazz) {
    // collect property types
    Set<Class<?>> propertyTypes = new HashSet<Class<?>>();
    for (Method getter : getterMethodsOf(clazz)) {
      Class<?> propertyType = getter.getReturnType();
      propertyTypes.add(propertyType);
      if (propertyType.isArray()) {
        Class<?> componentType = propertyType.getComponentType();
        propertyTypes.add(componentType);
      }
      if (isIterable(propertyType)) {
        ParameterizedType parameterizedType = (ParameterizedType) getter.getGenericReturnType();
        Class<?> actualParameterClass = (Class<?>) parameterizedType.getActualTypeArguments()[0];
        propertyTypes.add(actualParameterClass);
      }
    }
    // remove clazz since our assertion class will be in the same package.
    propertyTypes.remove(clazz);
    // generate imports
    StringBuilder imports = new StringBuilder();
    for (Class<?> propertyType : propertyTypes) {
      // no need to generate import for types in java.lang
      addImportTypeFor(propertyType, imports);
    }
    return imports.toString();
  }

  private static void addImportTypeFor(Class<?> clazz, StringBuilder imports) {
    Package classPackage = clazz.getPackage();
    if (!clazz.isPrimitive() && classPackage != null && !classPackage.getName().equals(JAVA_LANG_PACKAGE)) {
      imports.append(format(IMPORT_LINE, clazz.getPackage().getName(), clazz.getSimpleName(), LINE_SEPARATOR));
    }
  }

  private static String lowercaseFirstCharOf(String propertyName) {
    String firstChar = propertyName.substring(0, 1);
    String propertyNameWithLowercasedFirstChar = propertyName.replace(firstChar, firstChar.toLowerCase());
    return propertyNameWithLowercasedFirstChar;
  }

  private List<Method> getterMethodsOf(Class<?> clazz) {
    Method[] declaredMethods = clazz.getDeclaredMethods();
    List<Method> getters = new ArrayList<Method>();
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
    List<Method> booleanGetters = new ArrayList<Method>();
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

  private static String readTemplate(String templateFileName) {
    BufferedReader reader = null;
    try {
      reader = new BufferedReader(new FileReader(templateFileName));
      String line = null;
      StringBuilder hasAssertionTemplateBuilder = new StringBuilder();
      while ((line = reader.readLine()) != null) {
        hasAssertionTemplateBuilder.append(line).append(LINE_SEPARATOR);
      }
      return hasAssertionTemplateBuilder.toString();
    } catch (Exception e) {
      throw new RuntimeException("Failed to read " + templateFileName, e);
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
