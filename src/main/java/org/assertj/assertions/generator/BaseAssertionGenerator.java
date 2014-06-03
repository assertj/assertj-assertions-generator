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
import static org.apache.commons.lang3.StringUtils.remove;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Comparator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.assertj.assertions.generator.Template.Type;
import org.assertj.assertions.generator.description.ClassDescription;
import org.assertj.assertions.generator.description.DataDescription;
import org.assertj.assertions.generator.description.FieldDescription;
import org.assertj.assertions.generator.description.GetterDescription;
import org.assertj.assertions.generator.description.TypeName;

public class BaseAssertionGenerator implements AssertionGenerator, AssertionsEntryPointGenerator {

  // default file for templates
  static final String DEFAULT_IS_ASSERTION_TEMPLATE = "is_assertion_template.txt";
  static final String DEFAULT_HAS_ELEMENTS_ASSERTION_TEMPLATE_FOR_ARRAY =
    "has_elements_assertion_template_for_array.txt";
  static final String DEFAULT_HAS_ELEMENTS_ASSERTION_TEMPLATE_FOR_ITERABLE =
    "has_elements_assertion_template_for_iterable.txt";
  static final String DEFAULT_HAS_ASSERTION_TEMPLATE = "has_assertion_template.txt";
  static final String DEFAULT_HAS_ASSERTION_TEMPLATE_FOR_PRIMITIVE = "has_assertion_template_for_primitive.txt";
  static final String DEFAULT_HAS_ASSERTION_TEMPLATE_FOR_REAL_NUMBER = "has_assertion_template_for_real_number.txt";
  static final String DEFAULT_CUSTOM_ASSERTION_CLASS_TEMPLATE = "custom_assertion_class_template.txt";
  static final String DEFAULT_ASSERTIONS_ENTRY_POINT_CLASS_TEMPLATE = "standard_assertions_entry_point_class_template.txt";
  static final String DEFAULT_ASSERTION_ENTRY_POINT_METHOD_TEMPLATE = "standard_assertion_entry_point_method_template.txt";
  static final String DEFAULT_SOFT_ENTRY_POINT_ASSERTIONS_CLASS_TEMPLATE =
    "soft_assertions_entry_point_class_template.txt";
  static final String DEFAULT_SOFT_ENTRY_POINT_ASSERTION_METHOD_TEMPLATE = "soft_assertion_entry_point_method_template.txt";
  static final String DEFAULT_BDD_ENTRY_POINT_ASSERTIONS_CLASS_TEMPLATE = "bdd_assertions_entry_point_class_template.txt";
  static final String DEFAULT_BDD_ENTRY_POINT_ASSERTION_METHOD_TEMPLATE = "bdd_assertion_entry_point_method_template.txt";
  static final String ASSERT_CLASS_SUFFIX = "Assert";
  static final String ASSERT_CLASS_FILE_SUFFIX = ASSERT_CLASS_SUFFIX + ".java";
  static final String TEMPLATES_DIR = "templates" + File.separator;
  private static final String IMPORT_LINE = "import %s;%s";
  private static final String PROPERTY_WITH_UPPERCASE_FIRST_CHAR_REGEXP = "\\$\\{Property\\}";
  private static final String PROPERTY_WITH_LOWERCASE_FIRST_CHAR_REGEXP = "\\$\\{property\\}";
  private static final String PACKAGE_REGEXP = "\\$\\{package\\}";
  private static final String PROPERTY_TYPE_REGEXP = "\\$\\{propertyType\\}";
  private static final String CLASS_TO_ASSERT_REGEXP = "\\$\\{class_to_assert\\}";
  private static final String CUSTOM_ASSERTION_CLASS_REGEXP = "\\$\\{custom_assertion_class\\}";
  private static final String ELEMENT_TYPE_REGEXP = "\\$\\{elementType\\}";
  private static final String ALL_ASSERTIONS_ENTRY_POINTS_REGEXP = "\\$\\{all_assertions_entry_points\\}";
  private static final String IMPORTS = "${imports}";
  private static final String THROWS = "${throws}";
  private static final String THROWS_JAVADOC = "${throws_javadoc}";
  private static final String LINE_SEPARATOR = System.getProperty("line.separator");
  // assertions classes are generated in their package directory starting from targetBaseDirectory.
  // ex : com.nba.Player -> targetBaseDirectory/com/nba/PlayerAssert.java
  private String targetBaseDirectory = ".";
  private Template classAssertionTemplate;
  private Template hasAssertionTemplate;
  private Template hasAssertionTemplateForPrimitive;
  private Template hasAssertionTemplateForRealNumber;
  private Template hasIterableElementsAssertionTemplate;
  private Template hasArrayElementsAssertionTemplate;
  private Template isAssertionTemplate;
  private Template standardAssertionsEntryPointClassTemplate;
  private Template standardAssertionEntryPointMethodTemplate;
  private Template softAssertionsEntryPointClassTemplate;
  private Template softAssertionEntryPointMethodTemplate;
  private Template bddAssertionsEntryPointClassTemplate;
  private Template bddAssertionEntryPointMethodTemplate;

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
          new Template(Type.ASSERTIONS_ENTRY_POINT_CLASS,
                       new File(templatesDirectory, DEFAULT_ASSERTIONS_ENTRY_POINT_CLASS_TEMPLATE)),
          new Template(Type.ASSERTION_ENTRY_POINT,
                       new File(templatesDirectory, DEFAULT_ASSERTION_ENTRY_POINT_METHOD_TEMPLATE)),
          new Template(Type.SOFT_ASSERTIONS_ENTRY_POINT_CLASS,
                       new File(templatesDirectory, DEFAULT_SOFT_ENTRY_POINT_ASSERTIONS_CLASS_TEMPLATE)),
          new Template(Type.SOFT_ENTRY_POINT_METHOD_ASSERTION,
                       new File(templatesDirectory, DEFAULT_SOFT_ENTRY_POINT_ASSERTION_METHOD_TEMPLATE)),
          new Template(Type.BDD_ASSERTIONS_ENTRY_POINT_CLASS,
                       new File(templatesDirectory, DEFAULT_BDD_ENTRY_POINT_ASSERTIONS_CLASS_TEMPLATE)),
          new Template(Type.BDD_ENTRY_POINT_METHOD_ASSERTION,
                       new File(templatesDirectory, DEFAULT_BDD_ENTRY_POINT_ASSERTION_METHOD_TEMPLATE)),
          new Template(Type.HAS_FOR_REAL_NUMBER,
                       new File(templatesDirectory, DEFAULT_HAS_ASSERTION_TEMPLATE_FOR_REAL_NUMBER))
    );
  }

  public BaseAssertionGenerator(Template classAssertionTemplate,
                                Template hasAssertionTemplate,
                                Template hasAssertionTemplateForPrimitive,
                                Template hasIterableElementsAssertionTemplate,
                                Template hasArrayElementsAssertionTemplate,
                                Template isAssertionTemplate,
                                Template entryPointAssertionsClassTemplate,
                                Template entryPointAssertionTemplate,
                                Template entryPointSoftAssertionsClassTemplate,
                                Template entryPointSoftAssertionTemplate,
                                Template bddAssertionsEntryPointClassTemplate,
                                Template bddAssertionEntryPointMethodTemplate,
                                Template hasAssertionTemplateForRealNumber) {

    this.setAssertionClassTemplate(classAssertionTemplate);
    this.setHasAssertionTemplate(hasAssertionTemplate);
    this.setHasAssertionTemplateForPrimitive(hasAssertionTemplateForPrimitive);
    this.setHasAssertionTemplateForRealNumber(hasAssertionTemplateForRealNumber);
    this.setHasElementsAssertionForIterableTemplate(hasIterableElementsAssertionTemplate);
    this.setHasElementsAssertionForArrayTemplate(hasArrayElementsAssertionTemplate);
    this.setIsAssertionTemplate(isAssertionTemplate);
    this.setStandardAssertionsEntryPointClassTemplate(entryPointAssertionsClassTemplate);
    this.setStandardAssertionEntryPointMethodTemplate(entryPointAssertionTemplate);
    this.setSoftAssertionsEntryPointClassTemplate(entryPointSoftAssertionsClassTemplate);
    this.setSoftAssertionEntryPointMethodTemplate(entryPointSoftAssertionTemplate);
    this.setBddAssertionsEntryPointClassTemplate(bddAssertionsEntryPointClassTemplate);
    this.setBddAssertionEntryPointMethodTemplate(bddAssertionEntryPointMethodTemplate);
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

  public final void setHasAssertionTemplateForRealNumber(Template hasAssertionTemplateForRealNumber) {
    checkTemplateParameter(hasAssertionTemplateForRealNumber, Template.Type.HAS_FOR_REAL_NUMBER);
    this.hasAssertionTemplateForRealNumber = hasAssertionTemplateForRealNumber;
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

  public void setStandardAssertionsEntryPointClassTemplate(final Template standardAssertionsEntryPointClassTemplate) {
    checkTemplateParameter(standardAssertionsEntryPointClassTemplate, Type.ASSERTIONS_ENTRY_POINT_CLASS);
    this.standardAssertionsEntryPointClassTemplate = standardAssertionsEntryPointClassTemplate;
  }

  public void setStandardAssertionEntryPointMethodTemplate(final Template standardAssertionEntryPointMethodTemplate) {
    checkTemplateParameter(standardAssertionEntryPointMethodTemplate, Type.ASSERTION_ENTRY_POINT);
    this.standardAssertionEntryPointMethodTemplate = standardAssertionEntryPointMethodTemplate;
  }

  public void setDirectoryWhereAssertionFilesAreGenerated(String targetBaseDirectory) {
    this.targetBaseDirectory = targetBaseDirectory;
  }

  public void setSoftAssertionsEntryPointClassTemplate(final Template softAssertionsEntryPointClassTemplate) {
    this.softAssertionsEntryPointClassTemplate = softAssertionsEntryPointClassTemplate;
  }

  public void setSoftAssertionEntryPointMethodTemplate(final Template softAssertionEntryPointMethodTemplate) {
    this.softAssertionEntryPointMethodTemplate = softAssertionEntryPointMethodTemplate;
  }

  public void setBddAssertionsEntryPointClassTemplate(final Template bddAssertionsEntryPointClassTemplate) {
    this.bddAssertionsEntryPointClassTemplate = bddAssertionsEntryPointClassTemplate;
  }

  public void setBddAssertionEntryPointMethodTemplate(final Template bddAssertionEntryPointMethodTemplate) {
    this.bddAssertionEntryPointMethodTemplate = bddAssertionEntryPointMethodTemplate;
  }

  @Override
  public File generateCustomAssertionFor(ClassDescription classDescription) throws IOException {

    // Assertion content
    String assertionFileContent = generateCustomAssertionContentFor(classDescription);
    // finally create the assertion file, located in its package directory starting from targetBaseDirectory
    String targetDirectory = getDirectoryPathCorrespondingToPackage(classDescription.getPackageName());
    // build any needed directories
    new File(targetDirectory).mkdirs();
    return createFile(assertionFileContent, classDescription.getClassNameWithOuterClassNotSeparatedByDots() + ASSERT_CLASS_FILE_SUFFIX,
                      targetDirectory);
  }

  @Override
  public String generateCustomAssertionContentFor(ClassDescription classDescription) throws IOException {

    // use class template first
    StringBuilder assertionFileContentBuilder = new StringBuilder(classAssertionTemplate.getContent());

    // generate assertion method for each property with a public getter
    assertionFileContentBuilder.append(generateAssertionsForGettersOf(classDescription));
    assertionFileContentBuilder.append(generateAssertionsForPublicFieldsOf(classDescription));

    // close class with }
    assertionFileContentBuilder.append(LINE_SEPARATOR).append("}").append(LINE_SEPARATOR);
    String assertionFileContent = assertionFileContentBuilder.toString();

    // Aggregate all necessary class imports to include together
    TreeSet<TypeName> imports = new TreeSet<TypeName>(classDescription.getImports());
    imports.add(new TypeName("org.assertj.core.api.AbstractAssert"));
    if (assertionFileContent.contains("Assertions")) {
      imports.add(new TypeName("org.assertj.core.api.Assertions"));
    }
    
    // resolve template markers, in case of nested class like "Outer.Nested", assert class will be OuterNestedAssert
    return assertionFileContent.replaceAll(PACKAGE_REGEXP, classDescription.getPackageName())
                               .replaceAll(CUSTOM_ASSERTION_CLASS_REGEXP, assertClassNameOf(classDescription))
                               .replaceAll(CLASS_TO_ASSERT_REGEXP, classDescription.getClassNameWithOuterClass())
                               .replace(IMPORTS, listImports(imports, classDescription.getPackageName()));
  }

  private static String assertClassNameOf(ClassDescription classDescription) {
    return classDescription.getClassNameWithOuterClassNotSeparatedByDots() + ASSERT_CLASS_SUFFIX;
  }

  @Override
  public String generateAssertionsEntryPointClassContentFor(final Set<ClassDescription> classDescriptionSet,
                                                            AssertionsEntryPointType assertionsEntryPointType,
                                                            String entryPointClassPackage) {
    if (noClassDescriptionsGiven(classDescriptionSet)) return "";
    Template assertionEntryPointMethodTemplate = chooseAssertionEntryPointMethodTemplate(assertionsEntryPointType);
    Template assertionsEntryPointClassTemplate = chooseAssertionEntryPointClassTemplate(assertionsEntryPointType);
    return generateAssertionsEntryPointClassContent(classDescriptionSet, assertionsEntryPointClassTemplate,
                                                    assertionEntryPointMethodTemplate, entryPointClassPackage);
  }

  private Template chooseAssertionEntryPointMethodTemplate(final AssertionsEntryPointType assertionsEntryPointType) {
    switch (assertionsEntryPointType) {
      case SOFT : return softAssertionEntryPointMethodTemplate;
      case BDD : return bddAssertionEntryPointMethodTemplate;
      default: return standardAssertionEntryPointMethodTemplate;
    }
  }

  private Template chooseAssertionEntryPointClassTemplate(final AssertionsEntryPointType assertionsEntryPointType) {
    switch (assertionsEntryPointType) {
      case SOFT : return softAssertionsEntryPointClassTemplate;
      case BDD : return bddAssertionsEntryPointClassTemplate;
      default: return standardAssertionsEntryPointClassTemplate;
    }
  }

  @Override
  public File generateAssertionsEntryPointClassFor(final Set<ClassDescription> classDescriptionSet,
                                                   AssertionsEntryPointType assertionsEntryPointType,
                                                   String entryPointClassPackage) throws IOException {
    if (noClassDescriptionsGiven(classDescriptionSet)) return null;
    String assertionsEntryPointFileContent = generateAssertionsEntryPointClassContentFor(classDescriptionSet, assertionsEntryPointType, entryPointClassPackage);
    return createAssertionsFileFor(classDescriptionSet, assertionsEntryPointFileContent,
                                   assertionsEntryPointType.getFileName(), entryPointClassPackage);
  }


  private String generateAssertionsEntryPointClassContent(final Set<ClassDescription> classDescriptionSet,
                                                          final Template entryPointAssertionsClassTemplate,
                                                          final Template entryPointAssertionMethodTemplate,
                                                          String entryPointClassPackage) {
    String entryPointAssertionsClassContent = entryPointAssertionsClassTemplate.getContent();
    // resolve template markers
    String classPackage = isEmpty(entryPointClassPackage) ? determineBestEntryPointsAssertionsClassPackage(classDescriptionSet) : entryPointClassPackage;
    entryPointAssertionsClassContent = entryPointAssertionsClassContent.replaceAll(PACKAGE_REGEXP, classPackage);

    String softAssertionsImportsContent = generateEntryPointsAssertionsImportFor(classDescriptionSet, classPackage);
    entryPointAssertionsClassContent = entryPointAssertionsClassContent.replace(IMPORTS,
                                                                                softAssertionsImportsContent);
    String allEntryPointsAssertionContent = generateAssertionEntryPointMethodsFor(classDescriptionSet,
                                                                                  entryPointAssertionMethodTemplate);
    entryPointAssertionsClassContent = entryPointAssertionsClassContent.replaceAll(ALL_ASSERTIONS_ENTRY_POINTS_REGEXP,
                                                                                   allEntryPointsAssertionContent);
    return entryPointAssertionsClassContent;
  }

  /**
   * create the assertions entry point file, located in its package directory starting from targetBaseDirectory.
   * <p>
   * If assertionsClassPackage is not set, we use the common base package of the given classes,
   * if some classe are in a.b.c package and others in a.b.c.d, then entry point class will be in a.b.c.
   * </p>
   *
   * @param classDescriptionSet used to determine the assertions class package
   * @param fileContent assertions entry point file content
   * @param fileName assertions entry point file name
   * @param assertionsClassPackage the entry point class package - automatically determined if null.
   * @return the created assertions entry point file
   * @throws IOException if file can't be created.
   */
  private File createAssertionsFileFor(final Set<ClassDescription> classDescriptionSet, final String fileContent,
                                       final String fileName, final String assertionsClassPackage) throws IOException {
    String classPackage = isEmpty(assertionsClassPackage) ? determineBestEntryPointsAssertionsClassPackage(classDescriptionSet) : assertionsClassPackage;
    String assertionsDirectory = getDirectoryPathCorrespondingToPackage(classPackage);
    // build any needed directories
    new File(assertionsDirectory).mkdirs();
    return createFile(fileContent, fileName, assertionsDirectory);
  }

  private String generateAssertionEntryPointMethodsFor(final Set<ClassDescription> classDescriptionSet,
                                                       Template assertionEntryPointMethodTemplate) {
    // sort ClassDescription according to their class name.
    SortedSet<ClassDescription> sortedClassDescriptionSet =
      new TreeSet<ClassDescription>(new Comparator<ClassDescription>() {
        @Override
        public int compare(final ClassDescription cd1, final ClassDescription cd2) {
          return cd1.getClassNameWithOuterClass().compareTo(cd2.getClassNameWithOuterClass());
        }
      });
    sortedClassDescriptionSet.addAll(classDescriptionSet);
    // generate for each classDescription the entry point method, e.g. assertThat(MyClass) or then(MyClass)
    StringBuilder allAssertThatsContentBuilder = new StringBuilder();
    final String lineSeparator = System.getProperty("line.separator");
    for (ClassDescription classDescription : sortedClassDescriptionSet) {
      String assertionEntryPointMethodContent = assertionEntryPointMethodTemplate.getContent();
      // resolve class assert (ex: PlayerAssert)
      // in case of inner classes like Movie.PublicCategory, class assert will be MoviePublicCategoryAssert
      assertionEntryPointMethodContent = assertionEntryPointMethodContent.replaceAll(CUSTOM_ASSERTION_CLASS_REGEXP,
                                                                                     assertClassNameOf(classDescription));
      // resolve class (ex: Player)
      // in case of inner classes like Movie.PublicCategory use class name with outer class i.e. Movie.PublicCategory.
      assertionEntryPointMethodContent = 
          assertionEntryPointMethodContent.replaceAll(CLASS_TO_ASSERT_REGEXP,
                                                      classDescription.getClassNameWithOuterClass());
      allAssertThatsContentBuilder.append(lineSeparator).append(assertionEntryPointMethodContent);
    }
    return allAssertThatsContentBuilder.toString();
  }

  private String generateEntryPointsAssertionsImportFor(final Set<ClassDescription> classDescriptionSet,
                                                        final String entryPointAssertionsClassPackage) {
    final Set<TypeName> typeNameSet = new TreeSet<TypeName>();
    for (ClassDescription classDescription : classDescriptionSet) {
      typeNameSet.add(classDescription.getTypeName());
      // add corresponding Assert class (NameAssert for class Name) to generate both Name and NameAssert import
      typeNameSet.add(new TypeName(assertClassNameOf(classDescription), classDescription.getPackageName()));
    }
    return listImports(typeNameSet, entryPointAssertionsClassPackage);
  }

  private String determineBestEntryPointsAssertionsClassPackage(final Set<ClassDescription> classDescriptionSet) {
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
    Set<GetterDescription> getters = classDescription.getGettersDescriptions();
    for (GetterDescription getter : getters) {
      String assertionContent = assertionContentFor(getter);
      assertionsForGetters.append(assertionContent).append(LINE_SEPARATOR);
    }
    return assertionsForGetters.toString();
  }

  protected String generateAssertionsForPublicFieldsOf(ClassDescription classDescription) {
    StringBuilder assertionsForPublicFields = new StringBuilder();
    Set<FieldDescription> fields = classDescription.getFieldsDescriptions();
    for (DataDescription field : fields) {
      String assertionContent = assertionContentFor(field);
      assertionsForPublicFields.append(assertionContent).append(LINE_SEPARATOR);
    }
    return assertionsForPublicFields.toString();
  }
  
  private String assertionContentFor(DataDescription field) {
    String assertionContent = hasAssertionTemplate.getContent();
    if (field.isBooleanType()) {
      assertionContent = isAssertionTemplate.getContent();
    } else if (field.isIterableType()) {
      assertionContent = hasIterableElementsAssertionTemplate.getContent().replaceAll(ELEMENT_TYPE_REGEXP, 
                                                                                      field.getElementTypeName());
    } else if (field.isArrayType()) {
      assertionContent = hasArrayElementsAssertionTemplate.getContent().replaceAll(ELEMENT_TYPE_REGEXP,
                                                                                   field.getElementTypeName());
    } else if (field.isRealNumberType()) {
      assertionContent = hasAssertionTemplateForRealNumber.getContent();
    } else if (field.isPrimitiveType()) {
      assertionContent = hasAssertionTemplateForPrimitive.getContent();
    }

    // we reuse template for properties to have consistent assertins for property and field but 
    // - change the way we get the value since it's a field and not a property:
    assertionContent = assertionContent.replace("get${Property}()", "${property}")
                                       .replace("is${Property}()", "${property}");
    // - remove also ${throws} and ${throws_javadoc} since it make no sense for a field
    assertionContent = remove(assertionContent, "${throws}");
    assertionContent = remove(assertionContent, "${throws_javadoc}");
    
    // replace ${Property} and ${property} by field name (starting with uppercase/lowercase)
    return assertionContent.replaceAll(PROPERTY_WITH_UPPERCASE_FIRST_CHAR_REGEXP, capitalize(field.getName()))
                           .replaceAll(PROPERTY_TYPE_REGEXP, field.getTypeName())
                           .replaceAll(PROPERTY_WITH_LOWERCASE_FIRST_CHAR_REGEXP, field.getName());
  }
  
  private String assertionContentFor(GetterDescription getter) { // TODO refatcor with assertionContentFor field
    // sets default content (most likely case)
    String assertionContent = hasAssertionTemplate.getContent();
    if (getter.isBooleanType()) {
      assertionContent = isAssertionTemplate.getContent();
    } else if (getter.isIterableType()) {
      assertionContent = hasIterableElementsAssertionTemplate.getContent().replaceAll(ELEMENT_TYPE_REGEXP,
                                                                                      getter.getElementTypeName());
    } else if (getter.isArrayType()) {
      assertionContent = hasArrayElementsAssertionTemplate.getContent().replaceAll(ELEMENT_TYPE_REGEXP,
                                                                                   getter.getElementTypeName());
    } else if (getter.isRealNumberType()) {
      assertionContent = hasAssertionTemplateForRealNumber.getContent();
    } else if (getter.isPrimitiveType()) {
      assertionContent = hasAssertionTemplateForPrimitive.getContent();
    }
    
    assertionContent = declareExceptions(getter, assertionContent);
    
    String propertyName = getter.getPropertyName();
    assertionContent = assertionContent.replaceAll(PROPERTY_WITH_UPPERCASE_FIRST_CHAR_REGEXP, capitalize(propertyName));
    assertionContent = assertionContent.replaceAll(PROPERTY_TYPE_REGEXP, getter.getTypeName());
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
      throwsJavaDoc.append(" if actual.").append(getter.isBooleanType() ? "is" : "get")
                   .append("${Property}() throws one.");
    }
    if (!getter.getExceptions().isEmpty()) {
      throwsClause.append(' ');
    }
    assertionContent = assertionContent.replace(THROWS_JAVADOC, throwsJavaDoc.toString());
    assertionContent = assertionContent.replace(THROWS, throwsClause.toString());
    return assertionContent;
  }

  private void fillFile(String customAssertionContent, File assertionJavaFile) throws IOException {
    FileWriter fileWriter = null;
    try {
      fileWriter = new FileWriter(assertionJavaFile);
      fileWriter.write(customAssertionContent);
    } finally {
      closeQuietly(fileWriter);
    }
  }

  private File createFile(String fileContent, String fileName, String targetDirectory) throws IOException {
    File file = new File(targetDirectory, fileName);
    file.createNewFile();
    fillFile(fileContent, file);
    return file;
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

  private static boolean noClassDescriptionsGiven(final Set<ClassDescription> classDescriptionSet) {
    return classDescriptionSet == null || classDescriptionSet.isEmpty();
  }

}
