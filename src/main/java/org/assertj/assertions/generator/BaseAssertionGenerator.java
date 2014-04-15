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

import static java.lang.String.format;
import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.commons.lang3.StringUtils.capitalize;
import static org.apache.commons.lang3.StringUtils.isEmpty;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Comparator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.assertj.assertions.generator.Template.Type;
import org.assertj.assertions.generator.description.ClassDescription;
import org.assertj.assertions.generator.description.GetterDescription;
import org.assertj.assertions.generator.description.TypeName;

public class BaseAssertionGenerator implements AssertionGenerator {

  // default file for templates
  static final String DEFAULT_IS_ASSERTION_TEMPLATE = "is_assertion_template.txt";
  static final String DEFAULT_HAS_ELEMENTS_ASSERTION_TEMPLATE_FOR_ARRAY =
    "has_elements_assertion_template_for_array.txt";
  static final String DEFAULT_HAS_ELEMENTS_ASSERTION_TEMPLATE_FOR_ITERABLE =
    "has_elements_assertion_template_for_iterable.txt";
  static final String DEFAULT_HAS_ASSERTION_TEMPLATE = "has_assertion_template.txt";
  static final String DEFAULT_HAS_ASSERTION_TEMPLATE_FOR_PRIMITIVE = "has_assertion_template_for_primitive.txt";
  static final String DEFAULT_CUSTOM_ASSERTION_CLASS_TEMPLATE = "custom_assertion_class_template.txt";
  static final String DEFAULT_ENTRY_POINT_ASSERTIONS_CLASS_TEMPLATE = "entry_point_assertions_class_template.txt";
  static final String DEFAULT_ENTRY_POINT_ASSERTION_TEMPLATE = "entry_point_assertion_template.txt";
  static final String ASSERT_CLASS_SUFFIX = "Assert";
  static final String ASSERT_CLASS_FILE_SUFFIX = ASSERT_CLASS_SUFFIX + ".java";
  static final String TEMPLATES_DIR = "templates" + File.separator;
  private static final String IMPORT_LINE = "import %s;%s";
  private static final String PROPERTY_WITH_UPPERCASE_FIRST_CHAR_REGEXP = "\\$\\{Property\\}";
  private static final String PROPERTY_WITH_LOWERCASE_FIRST_CHAR_REGEXP = "\\$\\{property\\}";
  private static final String PACKAGE_REGEXP = "\\$\\{package\\}";
  private static final String PACKAGE_FULL_REGEXP = "\\$\\{package_full\\}";
  private static final String PROPERTY_TYPE_REGEXP = "\\$\\{propertyType\\}";
  private static final String CLASS_TO_ASSERT_REGEXP = "\\$\\{class_to_assert\\}";
  private static final String ELEMENT_TYPE_REGEXP = "\\$\\{elementType\\}";
  private static final String ALL_ASSERTIONS_ENTRY_POINTS = "\\$\\{all_assertions_entry_points\\}";
  private static final String IMPORTS = "${imports}";
  private static final String THROWS = "${throws}";
  private static final String THROWS_JAVADOC = "${throws_javadoc}";
  private static final String LINE_SEPARATOR = System.getProperty("line.separator");
  private static final String ASSERTIONS_ENTRY_POINT_FILE = "Assertions.java";
  private static final String BDD_ASSERTIONS_ENTRY_POINT_FILE = "BddAssertions.java";
  // assertions classes are generated in their package directory starting from targetBaseDirectory.
  // ex : com.nba.Player -> targetBaseDirectory/com/nba/PlayerAssert.java
  private String targetBaseDirectory = ".";
  private Template classAssertionTemplate;
  private Template hasAssertionTemplate;
  private Template hasAssertionTemplateForPrimitive;
  private Template hasIterableElementsAssertionTemplate;
  private Template hasArrayElementsAssertionTemplate;
  private Template isAssertionTemplate;
  private Template entryPointAssertionsClassTemplate;
  private Template entryPointAssertionTemplate;

  /**
   * Creates a new </code>{@link BaseAssertionGenerator}</code> with default templates directory.
   *
   * @throws IOException if some template file could not be found or read
   */
  public BaseAssertionGenerator() throws IOException {
    this(TEMPLATES_DIR);
  }

  /**
   * Creates a new </code>{@link BaseAssertionGenerator}</code> with in the specified directory.
   *
   * @param templatesDirectory path where to find templates
   * @throws IOException if some template file could not be found or read
   */
  public BaseAssertionGenerator(String templatesDirectory) throws IOException {
    this(
          new Template(Template.Type.ASSERT_CLASS,
                       new File(templatesDirectory, DEFAULT_CUSTOM_ASSERTION_CLASS_TEMPLATE)),
          new Template(Template.Type.HAS,
                       new File(templatesDirectory, DEFAULT_HAS_ASSERTION_TEMPLATE)),
          new Template(Template.Type.HAS_FOR_PRIMITIVE,
                       new File(templatesDirectory, DEFAULT_HAS_ASSERTION_TEMPLATE_FOR_PRIMITIVE)),
          new Template(Template.Type.HAS_FOR_ITERABLE,
                       new File(templatesDirectory, DEFAULT_HAS_ELEMENTS_ASSERTION_TEMPLATE_FOR_ITERABLE)),
          new Template(Template.Type.HAS_FOR_ARRAY,
                       new File(templatesDirectory, DEFAULT_HAS_ELEMENTS_ASSERTION_TEMPLATE_FOR_ARRAY)),
          new Template(Template.Type.IS,
                       new File(templatesDirectory, DEFAULT_IS_ASSERTION_TEMPLATE)),
          new Template(Type.ENTRY_POINT_ASSERTIONS_CLASS,
                       new File(templatesDirectory, DEFAULT_ENTRY_POINT_ASSERTIONS_CLASS_TEMPLATE)),
          new Template(Type.ENTRY_POINT_ASSERTION,
                       new File(templatesDirectory, DEFAULT_ENTRY_POINT_ASSERTION_TEMPLATE))
    );
  }

  /**
   * Creates a new </code>{@link BaseAssertionGenerator}</code> with in the specified directory.
   *
   * @param classAssertionTemplate
   * @param hasAssertionTemplate
   * @param hasIterableElementsAssertionTemplate
   *
   * @param hasArrayElementsAssertionTemplate
   *
   * @param isAssertionTemplate
   * @param entryPointAssertionsClassTemplate
   *
   * @param entryPointAssertionTemplate
   */
  public BaseAssertionGenerator(Template classAssertionTemplate, Template hasAssertionTemplate,
                                Template hasAssertionTemplateForPrimitive,
                                Template hasIterableElementsAssertionTemplate,
                                Template hasArrayElementsAssertionTemplate,
                                Template isAssertionTemplate,
                                Template entryPointAssertionsClassTemplate,
                                Template entryPointAssertionTemplate) {
    this.setAssertionClassTemplate(classAssertionTemplate);
    this.setHasAssertionTemplate(hasAssertionTemplate);
    this.setHasAssertionTemplateForPrimitive(hasAssertionTemplateForPrimitive);
    this.setHasElementsAssertionForIterableTemplate(hasIterableElementsAssertionTemplate);
    this.setHasElementsAssertionForArrayTemplate(hasArrayElementsAssertionTemplate);
    this.setIsAssertionTemplate(isAssertionTemplate);
    this.setEntryPointAssertionsClassTemplate(entryPointAssertionsClassTemplate);
    this.setEntryPointAssertionTemplate(entryPointAssertionTemplate);
  }

  /**
   * Setter to define your own {@link Template} for assertion class general skeleton (see
   * custom_assertion_class_template.txt as an example).
   *
   * @param assertionClassTemplate the {@link Template} to use for assertion class general skeleton.
   */
  public final void setAssertionClassTemplate(Template assertionClassTemplate) {
    checkTemplateParameter(assertionClassTemplate, Template.Type.ASSERT_CLASS);
    this.classAssertionTemplate = assertionClassTemplate;
  }

  public final void setHasAssertionTemplate(Template hasAssertionTemplate) {
    checkTemplateParameter(hasAssertionTemplate, Template.Type.HAS);
    this.hasAssertionTemplate = hasAssertionTemplate;
  }

  public final void setHasAssertionTemplateForPrimitive(Template hasAssertionTemplateForPrimitive) {
    checkTemplateParameter(hasAssertionTemplateForPrimitive, Template.Type.HAS_FOR_PRIMITIVE);
    this.hasAssertionTemplateForPrimitive = hasAssertionTemplateForPrimitive;
  }

  public final void setHasElementsAssertionForIterableTemplate(Template hasIterableElementsAssertionTemplate) {
    checkTemplateParameter(hasIterableElementsAssertionTemplate, Template.Type.HAS_FOR_ITERABLE);
    this.hasIterableElementsAssertionTemplate = hasIterableElementsAssertionTemplate;
  }

  public final void setHasElementsAssertionForArrayTemplate(Template hasArrayElementsAssertionTemplate) {
    checkTemplateParameter(hasArrayElementsAssertionTemplate, Template.Type.HAS_FOR_ARRAY);
    this.hasArrayElementsAssertionTemplate = hasArrayElementsAssertionTemplate;
  }

  public final void setIsAssertionTemplate(Template isAssertionTemplate) {
    checkTemplateParameter(isAssertionTemplate, Template.Type.IS);
    this.isAssertionTemplate = isAssertionTemplate;
  }

  public void setEntryPointAssertionsClassTemplate(final Template entryPointAssertionsClassTemplate) {
    checkTemplateParameter(entryPointAssertionsClassTemplate, Type.ENTRY_POINT_ASSERTIONS_CLASS);
    this.entryPointAssertionsClassTemplate = entryPointAssertionsClassTemplate;
  }

  public void setEntryPointAssertionTemplate(final Template entryPointAssertionTemplate) {
    checkTemplateParameter(entryPointAssertionTemplate, Type.ENTRY_POINT_ASSERTION);
    this.entryPointAssertionTemplate = entryPointAssertionTemplate;
  }

  public void setDirectoryWhereAssertionFilesAreGenerated(String targetBaseDirectory) {
    this.targetBaseDirectory = targetBaseDirectory;
  }

  /**
   * {@inheritDoc}
   */
  public File generateCustomAssertionFor(ClassDescription classDescription) throws IOException {

    // Assertion content
    String assertionFileContent = generateCustomAssertionContentFor(classDescription);
    // finally create the assertion file, located in its package directory starting from targetBaseDirectory
    String targetDirectory = getDirectoryPathCorrespondingToPackage(classDescription.getPackageName());
    // build any needed directories
    new File(targetDirectory).mkdirs();
    return createCustomAssertionFile(assertionFileContent, classDescription.getClassName() + ASSERT_CLASS_FILE_SUFFIX,
                                     targetDirectory);
  }

  public String generateCustomAssertionContentFor(ClassDescription classDescription) throws IOException {

    // use class template first
    StringBuilder assertionFileContentBuilder = new StringBuilder(classAssertionTemplate.getContent());

    // generate assertion method for each property with a public getter
    assertionFileContentBuilder.append(generateAssertionsForGettersOf(classDescription));

    // close class with }

    assertionFileContentBuilder.append(LINE_SEPARATOR).append("}").append(LINE_SEPARATOR);
    // className could be a nested class like "OuterClass.NestedClass"
    String className = classDescription.getClassName();

    // resolve template markers
    String assertionFileContent = assertionFileContentBuilder.toString();
    assertionFileContent = assertionFileContent.replaceAll(PACKAGE_REGEXP, classDescription.getPackageName());
    assertionFileContent = assertionFileContent.replaceAll(CLASS_TO_ASSERT_REGEXP + ASSERT_CLASS_SUFFIX,
                                                           className + ASSERT_CLASS_SUFFIX);
    // used for no package class.
    assertionFileContent = assertionFileContent.replaceAll(PACKAGE_FULL_REGEXP,
                                                           isEmpty(classDescription.getPackageName()) ? "" :
                                                             "package " + classDescription.getPackageName() + ";");
    assertionFileContent = assertionFileContent.replaceAll(CLASS_TO_ASSERT_REGEXP,
                                                           classDescription.getClassNameWithOuterClass());
    assertionFileContent = assertionFileContent.replace(IMPORTS, listImports(classDescription.getImports(),
                                                                             classDescription.getPackageName()));
    return assertionFileContent;
  }

  @Override
  public String generateAssertionsEntryPointContentFor(final Set<ClassDescription> classDescriptionSet) {
    if (classDescriptionSet == null || classDescriptionSet.isEmpty()) return "";
    final String entryPointAssertionsClassTemplateContent = entryPointAssertionsClassTemplate.getContent();
    String entryPointAssertionsClassContent = new StringBuilder(entryPointAssertionsClassTemplateContent).toString();
    // resolve template markers
    String entryPointAssertionsClassPackage = determineEntryPointsAssertionsClassPackage(classDescriptionSet);
    entryPointAssertionsClassContent = entryPointAssertionsClassContent.replaceAll(PACKAGE_REGEXP,
                                                                                   entryPointAssertionsClassPackage);
    String entryPointAssertionsImportsContent = generateEntryPointsAssertionsImportFor(classDescriptionSet,
                                                                                       entryPointAssertionsClassPackage);
    entryPointAssertionsClassContent = entryPointAssertionsClassContent.replace(IMPORTS,
                                                                                entryPointAssertionsImportsContent);
    String allEntryPointsAssertionContent = generateAssertThatEntryPointsAssertionsFor(classDescriptionSet);
    entryPointAssertionsClassContent = entryPointAssertionsClassContent.replaceAll(ALL_ASSERTIONS_ENTRY_POINTS,
                                                                                   allEntryPointsAssertionContent);
    return entryPointAssertionsClassContent;
  }

  @Override
  public File generateAssertionsEntryPointFor(final Set<ClassDescription> classDescriptionSet) throws IOException {
    if (classDescriptionSet == null || classDescriptionSet.isEmpty()) return null;
    String assertionsEntryPointFileContent = generateAssertionsEntryPointContentFor(classDescriptionSet);
    // create the assertion entry point file, located in its package directory starting from targetBaseDirectory
    String entryPointAssertionsClassPackage = determineEntryPointsAssertionsClassPackage(classDescriptionSet);
    String targetDirectory = getDirectoryPathCorrespondingToPackage(entryPointAssertionsClassPackage);
    // build any needed directories
    new File(targetDirectory).mkdirs();
    return createCustomAssertionFile(assertionsEntryPointFileContent, ASSERTIONS_ENTRY_POINT_FILE, targetDirectory);
  }

  @Override
  public File generateBddAssertionsEntryPointFor(final Set<ClassDescription> classDescriptionSet) throws IOException {
    if (classDescriptionSet == null || classDescriptionSet.isEmpty()) return null;
    String bddAssertionsEntryPointFileContent = generateBddAssertionsEntryPointContentFor(classDescriptionSet);
    // create the assertion entry point file, located in its package directory starting from targetBaseDirectory
    String entryPointBddAssertionsClassPackage = determineEntryPointsAssertionsClassPackage(classDescriptionSet);
    String targetDirectory = getDirectoryPathCorrespondingToPackage(entryPointBddAssertionsClassPackage);
    // build any needed directories
    new File(targetDirectory).mkdirs();
    return createCustomAssertionFile(bddAssertionsEntryPointFileContent, BDD_ASSERTIONS_ENTRY_POINT_FILE, targetDirectory);
  }

  @Override
  public String generateBddAssertionsEntryPointContentFor(Set<ClassDescription> classDescriptionSet) {
    // Assertions and BddAssertions are very similar, we only have few things to change.
    String bddAssertionsEntryPointClassContent = generateAssertionsEntryPointContentFor(classDescriptionSet);
    bddAssertionsEntryPointClassContent = bddAssertionsEntryPointClassContent.replaceAll("assertThat", "then");
    bddAssertionsEntryPointClassContent = bddAssertionsEntryPointClassContent.replaceAll("Assertions", "BddAssertions");
    bddAssertionsEntryPointClassContent = bddAssertionsEntryPointClassContent.replaceAll("Entry point for assertion", "Entry point for BDD assertion");
    return bddAssertionsEntryPointClassContent;
  }

  private String generateAssertThatEntryPointsAssertionsFor(final Set<ClassDescription> classDescriptionSet) {
    // sort ClassDescription according to their class name.
    SortedSet<ClassDescription> sortedClassDescriptionSet =
      new TreeSet<ClassDescription>(new Comparator<ClassDescription>() {
        @Override
        public int compare(final ClassDescription cd1, final ClassDescription cd2) {
          return cd1.getClassName().compareTo(cd2.getClassName());
        }
      });
    sortedClassDescriptionSet.addAll(classDescriptionSet);
    // generate assertThat(MyClass) for each classDescription
    StringBuilder allAssertThatsContentBuilder = new StringBuilder();
    final String lineSeparator = System.getProperty("line.separator");
    for (ClassDescription classDescription : sortedClassDescriptionSet) {
      String assertThatContent = new StringBuilder(entryPointAssertionTemplate.getContent()).toString();
      // resolve template markers, use class name with outer class in case of inner classes like Movie.PublicCategory.
      assertThatContent = assertThatContent.replaceAll(CLASS_TO_ASSERT_REGEXP + ASSERT_CLASS_SUFFIX,
                                                       classDescription.getClassName() + ASSERT_CLASS_SUFFIX);
      // resolve template markers, use class name with outer class in case of inner classes like Movie.PublicCategory.
      assertThatContent = assertThatContent.replaceAll(CLASS_TO_ASSERT_REGEXP,
                                                       classDescription.getClassNameWithOuterClass());
      allAssertThatsContentBuilder.append(lineSeparator).append(assertThatContent);
    }
    return allAssertThatsContentBuilder.toString();
  }

  private String generateEntryPointsAssertionsImportFor(final Set<ClassDescription> classDescriptionSet,
                                                        final String entryPointAssertionsClassPackage) {
    final Set<TypeName> typeNameSet = new TreeSet<TypeName>();
    for (ClassDescription classDescription : classDescriptionSet) {
      typeNameSet.add(classDescription.getTypeName());
      // add also corresponding Assert class (NameAssert for class Name)
      // this is needed to generate both imports (Name and NameAssert imports)
      typeNameSet.add(new TypeName(classDescription.getClassName() + ASSERT_CLASS_SUFFIX,
                                   classDescription.getPackageName()));
    }
    return listImports(typeNameSet, entryPointAssertionsClassPackage);
  }

  private String determineEntryPointsAssertionsClassPackage(final Set<ClassDescription> classDescriptionSet) {
    SortedSet<String> packages = new TreeSet<String>(new Comparator<String>() {
      @Override
      public int compare(final String o1, final String o2) {
        return o1.length() - o2.length();
      }
    });
    for (ClassDescription classDescription : classDescriptionSet) {
      packages.add(classDescription.getPackageName());
    }
    // takes the base package of all given classes assuming they all belong to a common package, i.e a.b.c. over a.b.c.d
    // this can certainly be improved ...
    return packages.first();
  }

  /**
   * Returns the target directory path where the assertions file for given classDescription will be created.
   *
   * @param packageName
   * @return the target directory path corresponding to the given package.
   */
  private String getDirectoryPathCorrespondingToPackage(final String packageName) {
    return targetBaseDirectory + File.separator + packageName.replace('.', File.separatorChar);
  }

  private static String listImports(Set<TypeName> typesToImport, String classPackage) {
    StringBuilder importsBuilder = new StringBuilder();
    for (TypeName type : typesToImport) {
      if (!type.isPrimitive() && !type.belongsToJavaLangPackage() && !type.getPackageName().equals(classPackage)) {
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
      StringBuilder sb = new StringBuilder(getter.getElementTypeName());
      if (getter.isArrayPropertyType()) {
        sb.append("[]");
      }
      assertionContent = hasIterableElementsAssertionTemplate.getContent().replaceAll(ELEMENT_TYPE_REGEXP,
                                                                                      sb.toString());
    } else if (getter.isArrayPropertyType()) {
      assertionContent = hasArrayElementsAssertionTemplate.getContent().replaceAll(ELEMENT_TYPE_REGEXP,
                                                                                   getter.getElementTypeName());
    } else if (getter.isPrimitivePropertyType()) {
      assertionContent = hasAssertionTemplateForPrimitive.getContent();
    }

    assertionContent = declareExceptions(getter, assertionContent);

    String propertyName = getter.getPropertyName();
    assertionContent = assertionContent.replaceAll(PROPERTY_WITH_UPPERCASE_FIRST_CHAR_REGEXP, capitalize(propertyName));
    assertionContent = assertionContent.replaceAll(PROPERTY_TYPE_REGEXP, getter.getPropertyTypeName());
    return assertionContent.replaceAll(PROPERTY_WITH_LOWERCASE_FIRST_CHAR_REGEXP, propertyName);
  }

  /**
   * Handle case where getter throws an exception.
   *
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
      throwsJavaDoc.append(" if actual.").append(getter.isBooleanPropertyType() ? "is" : "get")
                   .append("${Property}() throws one.");
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
    if (assertionClassTemplate.getContent() == null) {
      throw new NullPointerException("Expecting a non null content in the Template");
    }
  }
}
