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
package org.assertj.assertions.generator;

import org.assertj.assertions.generator.Template.Type;
import org.assertj.assertions.generator.description.ClassDescription;
import org.assertj.assertions.generator.description.GetterDescription;
import org.assertj.assertions.generator.description.TypeName;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;

import static java.lang.String.format;
import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.commons.lang3.StringUtils.capitalize;


public class BaseAssertionGenerator implements AssertionGenerator {

  // default file for templates
  static final String DEFAULT_IS_ASSERTION_TEMPLATE = "is_assertion_template.txt";
  static final String DEFAULT_HAS_ELEMENTS_ASSERTION_TEMPLATE_FOR_ARRAY = "has_elements_assertion_template_for_array.txt";
  static final String DEFAULT_HAS_ELEMENTS_ASSERTION_TEMPLATE_FOR_ITERABLE = "has_elements_assertion_template_for_iterable.txt";
  static final String DEFAULT_HAS_ASSERTION_TEMPLATE = "has_assertion_template.txt";
  static final String DEFAULT_CUSTOM_ASSERTION_CLASS_TEMPLATE = "custom_assertion_class_template.txt";

  static final String ASSERT_CLASS_SUFFIX = "Assert";
  static final String ASSERT_CLASS_FILE_SUFFIX = ASSERT_CLASS_SUFFIX + ".java";
  private static final String IMPORT_LINE = "import %s;%s";
  private static final String PROPERTY_WITH_UPPERCASE_FIRST_CHAR_REGEXP = "\\$\\{Property\\}";
  private static final String PROPERTY_WITH_LOWERCASE_FIRST_CHAR_REGEXP = "\\$\\{property\\}";
  private static final String PACKAGE__REGEXP = "\\$\\{package\\}";
  private static final String PROPERTY_TYPE_REGEXP = "\\$\\{propertyType\\}";
  private static final String CLASS_TO_ASSERT_REGEXP = "\\$\\{class_to_assert\\}";
  private static final String ELEMENT_TYPE_REGEXP = "\\$\\{elementType\\}";
  private static final String IMPORTS = "${imports}";
  private static final String THROWS = "${throws}";
  private static final String THROWS_JAVADOC = "${throws_javadoc}";
  private static final String LINE_SEPARATOR = System.getProperty("line.separator");
  static final String TEMPLATES_DIR = "templates" + File.separator;

  // assertions classes are generated in their package directory starting from targetBaseDirectory.
  // ex : com.nba.Player -> targetBaseDirectory/com/nba/PlayerAssert.java
  private String targetBaseDirectory = ".";

  private Template classAssertionTemplate;
  private Template hasAssertionTemplate;
  private Template hasIterableElementsAssertionTemplate;
  private Template hasArrayElementsAssertionTemplate;
  private Template isAssertionTemplate;

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
   * Creates a new </code>{@link BaseAssertionGenerator}</code> with in the specified directory.
   * 
   * @param templatesDirectory path where to find templates
   * @throws FileNotFoundException if some template file could not be found
   * @throws IOException if some template file could not be read
   */
  public BaseAssertionGenerator(String templatesDirectory) throws FileNotFoundException, IOException {
    setAssertionClassTemplate(new Template(Template.Type.ASSERT_CLASS, new File(templatesDirectory,
        DEFAULT_CUSTOM_ASSERTION_CLASS_TEMPLATE)));
    setHasAssertionTemplate(new Template(Template.Type.HAS,
        new File(templatesDirectory, DEFAULT_HAS_ASSERTION_TEMPLATE)));
    setHasElementsAssertionForIterableTemplate(new Template(Template.Type.HAS_FOR_ITERABLE, new File(
        templatesDirectory, DEFAULT_HAS_ELEMENTS_ASSERTION_TEMPLATE_FOR_ITERABLE)));
    setHasElementsAssertionForArrayTemplate(new Template(Template.Type.HAS_FOR_ARRAY, new File(templatesDirectory,
        DEFAULT_HAS_ELEMENTS_ASSERTION_TEMPLATE_FOR_ARRAY)));
    setIsAssertionTemplate(new Template(Template.Type.IS, new File(templatesDirectory, DEFAULT_IS_ASSERTION_TEMPLATE)));
  }

  /**
   * Setter to define your own {@link Template} for assertion class general skeleton (see
   * custom_assertion_class_template.txt as an example).
   * 
   * @param assertionClassTemplate the {@link Template} to use for assertion class general skeleton.
   */
  public void setAssertionClassTemplate(Template assertionClassTemplate) {
    checkTemplateParameter(assertionClassTemplate, Template.Type.ASSERT_CLASS);
    this.classAssertionTemplate = assertionClassTemplate;
  }

  public void setHasAssertionTemplate(Template hasAssertionTemplate) {
    checkTemplateParameter(hasAssertionTemplate, Template.Type.HAS);
    this.hasAssertionTemplate = hasAssertionTemplate;
  }

  public void setHasElementsAssertionForIterableTemplate(Template hasIterableElementsAssertionTemplate) {
    checkTemplateParameter(hasIterableElementsAssertionTemplate, Template.Type.HAS_FOR_ITERABLE);
    this.hasIterableElementsAssertionTemplate = hasIterableElementsAssertionTemplate;
  }

  public void setHasElementsAssertionForArrayTemplate(Template hasArrayElementsAssertionTemplate) {
    checkTemplateParameter(hasArrayElementsAssertionTemplate, Template.Type.HAS_FOR_ARRAY);
    this.hasArrayElementsAssertionTemplate = hasArrayElementsAssertionTemplate;
  }

  public void setIsAssertionTemplate(Template isAssertionTemplate) {
    checkTemplateParameter(isAssertionTemplate, Template.Type.IS);
    this.isAssertionTemplate = isAssertionTemplate;
  }

  public void setDirectoryWhereAssertionFilesAreGenerated(String targetBaseDirectory) {
    this.targetBaseDirectory = targetBaseDirectory;
  }

  /** {@inheritDoc} */
  public File generateCustomAssertionFor(ClassDescription classDescription) throws IOException {

    // use class template first
    StringBuilder assertionFileContentBuilder = new StringBuilder(classAssertionTemplate.getContent());

    // generate assertion method for each property with a public getter
    assertionFileContentBuilder.append(generateAssertionsForGettersOf(classDescription));

    // close class with }
    assertionFileContentBuilder.append(LINE_SEPARATOR).append("}").append(LINE_SEPARATOR);

    String className = classDescription.getClassName(); // className could be a nested class like "OuterClass.NestedClass"

    // resolve template markers
    String assertionFileContent = assertionFileContentBuilder.toString();
    assertionFileContent = assertionFileContent.replaceAll(PACKAGE__REGEXP, classDescription.getPackageName());
    assertionFileContent = assertionFileContent.replaceAll(CLASS_TO_ASSERT_REGEXP + ASSERT_CLASS_SUFFIX, className + ASSERT_CLASS_SUFFIX);
    assertionFileContent = assertionFileContent.replaceAll(CLASS_TO_ASSERT_REGEXP, classDescription.getClassNameWithOuterClass());
    assertionFileContent = assertionFileContent.replace(IMPORTS,
        listImports(classDescription.getImports(), classDescription.getPackageName()));

    // finally create the assertion file, located in its package directory starting from targetBaseDirectory
    String targetDirectory = getTargetDirectoryPathFor(classDescription);
    // build any needed directories
    new File(targetDirectory).mkdirs();
    return createCustomAssertionFile(assertionFileContent, className + ASSERT_CLASS_FILE_SUFFIX, targetDirectory);
  }

  /**
   * Returns the target directory path where the assertions file for given classDescription will be created.
   * 
   * @param classDescription the {@link ClassDescription} we want to generate an assertion file for.
   * @return the target directory path where the assertions file for given classDescription will be created.
   */
  private String getTargetDirectoryPathFor(ClassDescription classDescription) {
    return targetBaseDirectory + File.separator + classDescription.getPackageName().replace('.', File.separatorChar);
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
    String assertionContent = hasAssertionTemplate.getContent();
    if (getter.isBooleanPropertyType()) {
      assertionContent = isAssertionTemplate.getContent();
    } else if (getter.isIterablePropertyType()) {
      assertionContent = hasIterableElementsAssertionTemplate.getContent().replaceAll(ELEMENT_TYPE_REGEXP,
          getter.getElementTypeName());
    } else if (getter.isArrayPropertyType()) {
      assertionContent = hasArrayElementsAssertionTemplate.getContent().replaceAll(ELEMENT_TYPE_REGEXP,
          getter.getElementTypeName());
    }
    if (getter.isPrimitivePropertyType()) {
      // primitive must be compared with != instead of (not) equals()
      assertionContent = assertionContent.replace("!actual${Property}.equals(${property})",
          "actual${Property} != ${property}");
    }
      
    assertionContent = declareExceptions(getter, assertionContent);

    String propertyName = getter.getPropertyName();
    assertionContent = assertionContent.replaceAll(PROPERTY_WITH_UPPERCASE_FIRST_CHAR_REGEXP, capitalize(propertyName));
    assertionContent = assertionContent.replaceAll(PROPERTY_TYPE_REGEXP, getter.getPropertyTypeName());
    return assertionContent.replaceAll(PROPERTY_WITH_LOWERCASE_FIRST_CHAR_REGEXP, propertyName);
  }

  /**
   * Handle case where getter throws an exception.
   * @param getter
   * @param assertionContent
   * @return
   */
  private String declareExceptions(GetterDescription getter, String assertionContent) {            
    StringBuilder throwsClause = new StringBuilder();
    StringBuilder throwsJavaDoc = new StringBuilder();
    boolean first = true;
    for (TypeName exception : getter.getExceptions()) {
      if (first) {
        throwsClause.append("throws ");
      } else {
        throwsClause.append(", ");
      }
      first = false;
      String exceptionName = exception.getSimpleNameWithOuterClass(); 
      throwsClause.append(exceptionName);
      throwsJavaDoc.append(LINE_SEPARATOR).append("   * @throws ").append(exceptionName);
      throwsJavaDoc.append(" if actual.").append(getter.isBooleanPropertyType() ? "is" : "get").append("${Property}() throws one.");        
    }
    if (!getter.getExceptions().isEmpty()) {
      throwsClause.append(' ');
    }
    assertionContent = assertionContent.replace(THROWS_JAVADOC, throwsJavaDoc.toString());
    assertionContent = assertionContent.replace(THROWS, throwsClause.toString());
    return assertionContent;
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

  private File createCustomAssertionFile(String assertionFileContent, String assertionFileName, String targetDirectory)
      throws IOException {
    File assertionJavaFile = new File(targetDirectory, assertionFileName);
    assertionJavaFile.createNewFile();
    fillAssertionJavaFile(assertionFileContent, assertionJavaFile);
    return assertionJavaFile;
  }

  private static void checkTemplateParameter(Template assertionClassTemplate, Type templateType) {
    if (assertionClassTemplate == null) {
      throw new NullPointerException("Expecting a non null Template");
    }
    if (templateType != assertionClassTemplate.getType()) {
      throw new IllegalArgumentException("Expecting a Template type to be '" + templateType + "' but was '"
          + assertionClassTemplate.getType() + "'");
    }
  }

}
