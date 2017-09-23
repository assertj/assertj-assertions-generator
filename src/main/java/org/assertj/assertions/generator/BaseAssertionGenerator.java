/**
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 * Copyright 2012-2017 the original author or authors.
 */
package org.assertj.assertions.generator;

import static com.google.common.collect.Sets.newHashSet;
import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.*;
import static org.assertj.assertions.generator.Template.Type.ABSTRACT_ASSERT_CLASS;
import static org.assertj.assertions.generator.Template.Type.ASSERT_CLASS;
import static org.assertj.assertions.generator.Template.Type.HIERARCHICAL_ASSERT_CLASS;
import static org.assertj.assertions.generator.util.ClassUtil.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.reflect.TypeToken;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.assertj.assertions.generator.Template.Type;
import org.assertj.assertions.generator.description.ClassDescription;
import org.assertj.assertions.generator.description.DataDescription;
import org.assertj.assertions.generator.description.FieldDescription;
import org.assertj.assertions.generator.description.GetterDescription;

@SuppressWarnings("WeakerAccess")
public class BaseAssertionGenerator implements AssertionGenerator, AssertionsEntryPointGenerator {

  static final String TEMPLATES_DIR = "templates" + File.separator;
  private static final String IMPORT_LINE = "import %s;%s";
  private static final String PREDICATE = "${predicate}";
  private static final String PREDICATE_NEG = "${neg_predicate}";
  private static final String PREDICATE_FOR_JAVADOC = "${predicate_for_javadoc}";
  private static final String NEGATIVE_PREDICATE_FOR_JAVADOC = "${negative_predicate_for_javadoc}";
  private static final String PREDICATE_FOR_FOR_ERROR_MESSAGE_PART1 = "${predicate_for_error_message_part1}";
  private static final String PREDICATE_FOR_FOR_ERROR_MESSAGE_PART2 = "${predicate_for_error_message_part2}";
  private static final String NEGATIVE_PREDICATE_FOR_FOR_ERROR_MESSAGE_PART1 = "${negative_predicate_for_error_message_part1}";
  private static final String NEGATIVE_PREDICATE_FOR_FOR_ERROR_MESSAGE_PART2 = "${negative_predicate_for_error_message_part2}";
  private static final String PROPERTY_WITH_UPPERCASE_FIRST_CHAR = "${Property}";
  private static final String PROPERTY_GETTER_CALL = "${getter}";
  private static final String PROPERTY_WITH_LOWERCASE_FIRST_CHAR = "${property}";
  private static final String PROPERTY_WITH_SAFE = "${property_safe}";
  private static final String PACKAGE = "${package}";
  private static final String PROPERTY_TYPE = "${propertyType}";
  private static final String PROPERTY_SIMPLE_TYPE = "${propertySimpleType}";
  private static final String PROPERTY_ASSERT_TYPE = "${propertyAssertType}";
  private static final String CLASS_TO_ASSERT = "${class_to_assert}";
  private static final String CUSTOM_ASSERTION_CLASS = "${custom_assertion_class}";
  private static final String ABSTRACT_SUPER_ASSERTION_CLASS = "${super_assertion_class}";
  private static final String SELF_TYPE = "${self_type}";
  private static final String MYSELF = "${myself}";
  private static final String ELEMENT_TYPE = "${elementType}";
  private static final String ELEMENT_ASSERT_TYPE = "${elementAssertType}";
  private static final String ALL_ASSERTIONS_ENTRY_POINTS = "${all_assertions_entry_points}";
  private static final String IMPORTS = "${imports}";
  private static final String THROWS = "${throws}";
  private static final String THROWS_JAVADOC = "${throws_javadoc}";
  private static final String LINE_SEPARATOR = "\n";

  private static final Comparator<String> ORDER_BY_INCREASING_LENGTH = new Comparator<String>() {
    @Override
    public int compare(final String o1, final String o2) {
      return o1.length() - o2.length();
    }
  };

  private static final Set<String> JAVA_KEYWORDS = newHashSet("abstract",
                                                              "assert",
                                                              "boolean",
                                                              "break",
                                                              "byte",
                                                              "case",
                                                              "catch",
                                                              "char",
                                                              // This one's not strictly required because you can't have
                                                              // a property called "class"
                                                              "class",
                                                              "const",
                                                              "continue",
                                                              "default",
                                                              "do",
                                                              "double",
                                                              "else",
                                                              "enum",
                                                              "extends",
                                                              "false",
                                                              "final",
                                                              "finally",
                                                              "float",
                                                              "for",
                                                              "goto",
                                                              "if",
                                                              "implements",
                                                              "import",
                                                              "instanceof",
                                                              "int",
                                                              "interface",
                                                              "long",
                                                              "native",
                                                              "new",
                                                              "null",
                                                              "package",
                                                              "protected",
                                                              "private",
                                                              "public",
                                                              "return",
                                                              "short",
                                                              "static",
                                                              "strictfp",
                                                              "super",
                                                              "switch",
                                                              "synchronized",
                                                              "this",
                                                              "throw",
                                                              "throws",
                                                              "transient",
                                                              "true",
                                                              "try",
                                                              "void",
                                                              "volatile",
                                                              "while");

  /**
   * This regexp shall match a java class's name inside an user template.
   * <p>
   * For this, we use the two character class {@code javaJavaIdentifierStart} and {@code javaJavaIdentifierPart} to match
   * a valid name.
   * <p>
   * <code>(?m)^public class</code> is needed to match the class at the beginning of a line and avoid matching
   * "public class"  inside javadoc comment as in templates/junit_soft_assertions_entry_point_class_template.txt.
   * <p>
   * <i>Description of the pattern:</i>
   * <ol>
   * <li><code>(?m)^</code> beginning of line in multi-line mode</li>
   * <li><code>public class[\\s]+</code> the "public class" followed by one or more whitespace (either tabs, space or new lines).</li>
   * <li><code>(?&lt;CLASSNAME&gt;...)</code> create a named group that would match a Java identifier (here the class name).</li>
   * <li><code>\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*</code> match said identifier using character class.</li>
   * </ol>
   *
   * @see java.util.regex.Pattern
   * @see Character#isJavaIdentifierStart
   * @see Character#isJavaIdentifierPart
   */
  private static final Pattern CLASS_NAME_PATTERN = Pattern
      .compile("(?m)^public class[\\s]+(?<CLASSNAME>\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*)\\b");

  private static final Set<TypeToken<?>> EMPTY_HIERARCHY = new HashSet<>();

  private static final String NON_PUBLIC_FIELD_VALUE_EXTRACTION = "org.assertj.core.util.introspection.FieldSupport.EXTRACTION.fieldValue(\"%s\", %s.class, actual)";
  // S is used in custom_abstract_assertion_class_template.txt
  private static final String ABSTRACT_ASSERT_SELF_TYPE = "S";

  // assertions classes are generated in their package directory starting from targetBaseDirectory.
  // ex : com.nba.Player -> targetBaseDirectory/com/nba/PlayerAssert.java
  private File targetBaseDirectory = Paths.get(".").toFile();
  private TemplateRegistry templateRegistry;// the pattern to search for
  private boolean generateAssertionsForAllFields = false;
  private String generatedAssertionsPackage = null;

  /**
   * Creates a new </code>{@link BaseAssertionGenerator}</code> with default templates directory.
   *
   * @throws IOException if some template file could not be found or read
   */
  public BaseAssertionGenerator() throws IOException {
    this(TEMPLATES_DIR);
  }

  /**
   * Creates a new </code>{@link BaseAssertionGenerator}</code> with the templates from the given directory.
   *
   * @param templatesDirectory path where to find templates
   * @throws IOException if some template file could not be found or read
   */
  public BaseAssertionGenerator(String templatesDirectory) throws IOException {
    templateRegistry = DefaultTemplateRegistryProducer.create(templatesDirectory);
  }

  public void setDirectoryWhereAssertionFilesAreGenerated(File targetBaseDirectory) {
    this.targetBaseDirectory = targetBaseDirectory;
  }

  public void setGenerateAssertionsForAllFields(boolean generateAssertionsForAllFields) {
    this.generateAssertionsForAllFields = generateAssertionsForAllFields;
  }

  public void setGeneratedAssertionsPackage(String generatedAssertionsPackage) {
    checkGivenPackageIsValid(generatedAssertionsPackage);
    this.generatedAssertionsPackage = generatedAssertionsPackage;
  }

  private void checkGivenPackageIsValid(String generatedAssertionsPackage) {
    Validate.isTrue(isNotBlank(generatedAssertionsPackage), "The given package '%s' must not be blank", generatedAssertionsPackage);
    Validate.isTrue(!containsWhitespace(generatedAssertionsPackage), "The given package '%s' must not contain blank character", generatedAssertionsPackage);
  }

  @Override
  public File generateCustomAssertionFor(ClassDescription classDescription) throws IOException {
    // Assertion content
    String assertionFileContent = generateCustomAssertionContentFor(classDescription);
    // Create the assertion file in targetBaseDirectory + either the given package or in the class to assert package
    String directoryWhereToCreateAssertFiles = getDirectoryWhereToCreateAssertFilesFor(classDescription);
    buildDirectory(directoryWhereToCreateAssertFiles);
    return createFile(assertionFileContent, classDescription.getAssertClassFilename(), directoryWhereToCreateAssertFiles);
  }

  private String getDirectoryWhereToCreateAssertFilesFor(ClassDescription classDescription) {
    return getDirectoryPathCorrespondingToPackage(determinePackageName(classDescription));
  }

  @Override
  public File[] generateHierarchicalCustomAssertionFor(ClassDescription classDescription, Set<TypeToken<?>> allClasses) throws IOException {
    // Assertion content
    String[] assertionFileContent = generateHierarchicalCustomAssertionContentFor(classDescription, allClasses);
    // Create the assertion file in targetBaseDirectory + either the given package or in the class to assert package
    String directoryWhereToCreateAssertFiles = getDirectoryWhereToCreateAssertFilesFor(classDescription);
    buildDirectory(directoryWhereToCreateAssertFiles);
    // create assertion files
    File[] assertionClassFiles = new File[2];
    final String concreteAssertClassFileName = classDescription.getAssertClassFilename();
    final String abstractAssertClassFileName = classDescription.getAbstractAssertClassFilename();
    assertionClassFiles[0] = createFile(assertionFileContent[0], abstractAssertClassFileName, directoryWhereToCreateAssertFiles);
    assertionClassFiles[1] = createFile(assertionFileContent[1], concreteAssertClassFileName, directoryWhereToCreateAssertFiles);
    return assertionClassFiles;
  }

  @Override
  public String[] generateHierarchicalCustomAssertionContentFor(ClassDescription classDescription,
                                                                Set<TypeToken<?>> classes) {
    // use abstract class template first
    String abstractAssertClassContent = templateRegistry.getTemplate(ABSTRACT_ASSERT_CLASS).getContent();
    StringBuilder abstractAssertClassContentBuilder = new StringBuilder(abstractAssertClassContent);

    // generate assertion method for each property with a public getter or field
    generateAssertionsForDeclaredGettersOf(abstractAssertClassContentBuilder, classDescription);
    generateAssertionsForDeclaredFieldsOf(abstractAssertClassContentBuilder, classDescription);

    // close class with }
    abstractAssertClassContentBuilder.append(LINE_SEPARATOR).append("}").append(LINE_SEPARATOR);

    // use concrete class template for the subclass of the generated abstract assert
    String concreteAssertClassContent = templateRegistry.getTemplate(HIERARCHICAL_ASSERT_CLASS).getContent();

    // return a String array with the actual generated content of the assertion class hierarchy
    String[] assertionClassesContent = new String[2];
    assertionClassesContent[0] = fillAbstractAssertClassTemplate(abstractAssertClassContentBuilder.toString(), classDescription, classes);
    assertionClassesContent[1] = fillConcreteAssertClassTemplate(concreteAssertClassContent, classDescription);
    return assertionClassesContent;
  }

  private String switchToComparableAssertIfPossible(String content, ClassDescription classDescription) {
    return classDescription.implementsComparable()
        ? replace(content, "AbstractObjectAssert", "AbstractComparableAssert")
        : content;
  }

  private String fillAbstractAssertClassTemplate(String abstractAssertClassTemplate, ClassDescription classDescription,
                                                 Set<TypeToken<?>> classes) {
    return fillAssertClassTemplate(abstractAssertClassTemplate, classDescription, classes, false);
  }

  private String fillAssertClassTemplate(String template, ClassDescription classDescription,
                                         Set<TypeToken<?>> classesHierarchy, boolean concrete) {
    // Add any AssertJ needed imports only, other types are used with their fully qualified names to avoid a compilation
    // error when two types have the same name.
    TreeSet<String> classesToImport = new TreeSet<>();
    // we import the class to assert in case the generated assertions are put in a different package than the class to assert,
    // listNeededImports will remove it if if was not needed.
    // in case of nested class, we must only import the outer class !
    classesToImport.add(classDescription.getFullyQualifiedOuterClassName());
    if (template.contains("Assertions.")) classesToImport.add("org.assertj.core.api.Assertions");
    if (template.contains("Objects.")) classesToImport.add("org.assertj.core.util.Objects");
    if (template.contains("Iterables.")) classesToImport.add("org.assertj.core.internal.Iterables");

    // Add assertion supertype to imports if needed (for abstract assertions hierarchy)
    // we need a FQN if the parent class is in a different package than the child class, if not listNeededImports will optimize it
    final String parentAssertClassName = classesHierarchy.contains(classDescription.getSuperType())
        ? classDescription.getFullyQualifiedParentAssertClassName()
        : "org.assertj.core.api.AbstractObjectAssert";
    if (classesHierarchy.contains(classDescription.getSuperType())) {
      classesToImport.add(parentAssertClassName);
    }

    final String customAssertionClass = concrete ? classDescription.getAssertClassName() : classDescription.getAbstractAssertClassName();
    final String selfType = concrete ? customAssertionClass : ABSTRACT_ASSERT_SELF_TYPE;
    final String myself = concrete ? "this" : "myself";

    template = replace(template, PACKAGE, determinePackageName(classDescription));
    template = replace(template, CUSTOM_ASSERTION_CLASS, customAssertionClass);
    // use a simple parent class name as we have already imported it
    // className could be a nested class like "OuterClass.NestedClass", in that case assert class will be OuterClassNestedClass
    template = replace(template, ABSTRACT_SUPER_ASSERTION_CLASS, getTypeNameWithoutDots(parentAssertClassName));
    if (template.contains("AbstractObjectAssert")) classesToImport.add("org.assertj.core.api.AbstractObjectAssert");

    template = replace(template, CLASS_TO_ASSERT, classDescription.getClassNameWithOuterClass());
    template = replace(template, SELF_TYPE, selfType);
    template = replace(template, MYSELF, myself);
    String neededImports = listNeededImports(classesToImport, determinePackageName(classDescription));
    template = replace(template, IMPORTS, neededImports.isEmpty() ? "" : LINE_SEPARATOR + neededImports);

    // in case the domain class is Comparable we want the assert class to inherit from AbstractComparableAssert
    template = switchToComparableAssertIfPossible(template, classDescription);

    return template;
  }

  private String determinePackageName(ClassDescription classDescription) {
    return generatedAssertionsPackage == null ? classDescription.getPackageName() : generatedAssertionsPackage;
  }

  private String fillConcreteAssertClassTemplate(String template, ClassDescription classDescription) {
    return fillAssertClassTemplate(template, classDescription, EMPTY_HIERARCHY, true);
  }

  @Override
  public String generateCustomAssertionContentFor(ClassDescription classDescription) {

    // use class template first
    String classTemplateContent = templateRegistry.getTemplate(ASSERT_CLASS).getContent();
    StringBuilder assertionFileContentBuilder = new StringBuilder(classTemplateContent);

    // generate assertion method for each property with a public getter
    generateAssertionsForGettersOf(assertionFileContentBuilder, classDescription);
    generateAssertionsForFieldsOf(assertionFileContentBuilder, classDescription);

    // close class with }
    assertionFileContentBuilder.append(LINE_SEPARATOR).append("}").append(LINE_SEPARATOR);

    return fillConcreteAssertClassTemplate(assertionFileContentBuilder.toString(), classDescription);
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
    case SOFT:
    case JUNIT_SOFT:
    case AUTO_CLOSEABLE_SOFT:
      return templateRegistry.getTemplate(Type.SOFT_ENTRY_POINT_METHOD_ASSERTION);
    case BDD:
      return templateRegistry.getTemplate(Type.BDD_ENTRY_POINT_METHOD_ASSERTION);
    case BDD_SOFT:
    case JUNIT_BDD_SOFT:
    case AUTO_CLOSEABLE_BDD_SOFT:
      return templateRegistry.getTemplate(Type.BDD_SOFT_ENTRY_POINT_METHOD_ASSERTION);
    default:
      return templateRegistry.getTemplate(Type.ASSERTION_ENTRY_POINT);
    }
  }

  private Template chooseAssertionEntryPointClassTemplate(final AssertionsEntryPointType assertionsEntryPointType) {
    switch (assertionsEntryPointType) {
    case SOFT:
      return templateRegistry.getTemplate(Type.SOFT_ASSERTIONS_ENTRY_POINT_CLASS);
    case JUNIT_SOFT:
      return templateRegistry.getTemplate(Type.JUNIT_SOFT_ASSERTIONS_ENTRY_POINT_CLASS);
    case AUTO_CLOSEABLE_SOFT:
      return templateRegistry.getTemplate(Type.AUTO_CLOSEABLE_SOFT_ASSERTIONS_ENTRY_POINT_CLASS);
    case BDD:
      return templateRegistry.getTemplate(Type.BDD_ASSERTIONS_ENTRY_POINT_CLASS);
    case BDD_SOFT:
      return templateRegistry.getTemplate(Type.BDD_SOFT_ASSERTIONS_ENTRY_POINT_CLASS);
    case JUNIT_BDD_SOFT:
      return templateRegistry.getTemplate(Type.JUNIT_BDD_SOFT_ASSERTIONS_ENTRY_POINT_CLASS);
    case AUTO_CLOSEABLE_BDD_SOFT:
      return templateRegistry.getTemplate(Type.AUTO_CLOSEABLE_BDD_SOFT_ASSERTIONS_ENTRY_POINT_CLASS);
    default:
      return templateRegistry.getTemplate(Type.ASSERTIONS_ENTRY_POINT_CLASS);
    }
  }

  @Override
  public File generateAssertionsEntryPointClassFor(final Set<ClassDescription> classDescriptionSet,
                                                   AssertionsEntryPointType assertionsEntryPointType,
                                                   String entryPointClassPackage) throws IOException {
    if (noClassDescriptionsGiven(classDescriptionSet)) return null;
    String assertionsEntryPointFileContent = generateAssertionsEntryPointClassContentFor(classDescriptionSet,
                                                                                         assertionsEntryPointType,
                                                                                         entryPointClassPackage);
    String fileName = determineFileName(assertionsEntryPointFileContent, assertionsEntryPointType);
    return createAssertionsFileFor(classDescriptionSet, assertionsEntryPointFileContent, fileName,
                                   entryPointClassPackage);
  }

  private String determineFileName(String assertionsEntryPointFileContent,
                                   AssertionsEntryPointType assertionsEntryPointType) {
    // expecting the class name to be here : "class <class name> "
    Matcher classNameMatcher = CLASS_NAME_PATTERN.matcher(assertionsEntryPointFileContent);
    // if we find a match return it
    if (classNameMatcher.find()) return classNameMatcher.group("CLASSNAME") + ".java";
    // otherwise use the default name
    return assertionsEntryPointType.getFileName();
  }

  private String generateAssertionsEntryPointClassContent(final Set<ClassDescription> classDescriptionSet,
                                                          final Template entryPointAssertionsClassTemplate,
                                                          final Template entryPointAssertionMethodTemplate,
                                                          String entryPointClassPackage) {
    String entryPointAssertionsClassContent = entryPointAssertionsClassTemplate.getContent();
    // resolve template markers
    String classPackage = isEmpty(entryPointClassPackage)
        ? determineBestEntryPointsAssertionsClassPackage(classDescriptionSet)
        : entryPointClassPackage;
    entryPointAssertionsClassContent = replace(entryPointAssertionsClassContent, PACKAGE, classPackage);

    String allEntryPointsAssertionContent = generateAssertionEntryPointMethodsFor(classDescriptionSet,
                                                                                  entryPointAssertionMethodTemplate);
    entryPointAssertionsClassContent = replace(entryPointAssertionsClassContent, ALL_ASSERTIONS_ENTRY_POINTS,
                                               allEntryPointsAssertionContent);
    return entryPointAssertionsClassContent;
  }

  /**
   * create the assertions entry point file, located in its package directory starting from targetBaseDirectory.
   * <p>
   * If assertionsClassPackage is not set, we use the common base package of the given classes, if some classe are in
   * a.b.c package and others in a.b.c.d, then entry point class will be in a.b.c.
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
    String classPackage = isEmpty(assertionsClassPackage)
        ? determineBestEntryPointsAssertionsClassPackage(classDescriptionSet) : assertionsClassPackage;
    String assertionsDirectory = getDirectoryPathCorrespondingToPackage(classPackage);
    // build any needed directories
    buildDirectory(assertionsDirectory);
    return createFile(fileContent, fileName, assertionsDirectory);
  }

  private String generateAssertionEntryPointMethodsFor(final Set<ClassDescription> classDescriptionSet,
                                                       Template assertionEntryPointMethodTemplate) {
    // sort ClassDescription according to their class name.
    SortedSet<ClassDescription> sortedClassDescriptionSet = new TreeSet<>(classDescriptionSet);
    // generate for each classDescription the entry point method, e.g. assertThat(MyClass) or then(MyClass)
    StringBuilder allAssertThatsContentBuilder = new StringBuilder();
    final char lineSeparator = '\n';
    for (ClassDescription classDescription : sortedClassDescriptionSet) {
      String assertionEntryPointMethodContent = assertionEntryPointMethodTemplate.getContent();
      // resolve class assert (ex: PlayerAssert)
      // in case of inner classes like Movie.PublicCategory, class assert will be MoviePublicCategoryAssert
      assertionEntryPointMethodContent = replace(assertionEntryPointMethodContent, CUSTOM_ASSERTION_CLASS,
                                                 classDescription.getFullyQualifiedAssertClassName());
      // resolve class (ex: Player)
      // in case of inner classes like Movie.PublicCategory use class name with outer class i.e. Movie.PublicCategory.
      assertionEntryPointMethodContent = replace(assertionEntryPointMethodContent, CLASS_TO_ASSERT,
                                                 classDescription.getFullyQualifiedClassName());

      allAssertThatsContentBuilder.append(lineSeparator).append(assertionEntryPointMethodContent);
    }
    return allAssertThatsContentBuilder.toString();
  }

  private String determineBestEntryPointsAssertionsClassPackage(final Set<ClassDescription> classDescriptionSet) {
    if (generatedAssertionsPackage != null) {
      return generatedAssertionsPackage;
    }

    SortedSet<String> packages = new TreeSet<>(ORDER_BY_INCREASING_LENGTH);
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
   * @param packageName package name 
   * @return the target directory path corresponding to the given package.
   */
  private String getDirectoryPathCorrespondingToPackage(final String packageName) {
    return targetBaseDirectory + File.separator + packageName.replace('.', File.separatorChar);
  }

  private static String listNeededImports(Set<String> typesToImport, String classPackage) {
    StringBuilder imports = new StringBuilder();
    for (String type : typesToImport) {
      if (isImportNeeded(type, classPackage)) {
        imports.append(format(IMPORT_LINE, type, LINE_SEPARATOR));
      }
    }
    return imports.toString();
  }

  private static boolean isImportNeeded(String type, String classPackage) {
    // no need to import type belonging to the same package
    if (Objects.equals(classPackage, packageOf(type))) return false;
    try {
      Class<?> clazz = Class.forName(type);
      // primitive and java.lang.* are available by default
      return !(clazz.isPrimitive() || isJavaLangType(clazz));
    } catch (ClassNotFoundException e) {
      // occurs for abstract types (ex: AbstractXXXAssert) or types to generate that are unknown
      return true;
    }
  }

  protected void generateAssertionsForGettersOf(StringBuilder contentBuilder, ClassDescription classDescription) {
    generateAssertionsForGetters(contentBuilder, classDescription.getGettersDescriptions(), classDescription);
  }

  protected void generateAssertionsForDeclaredGettersOf(StringBuilder contentBuilder,
                                                        ClassDescription classDescription) {
    generateAssertionsForGetters(contentBuilder, classDescription.getDeclaredGettersDescriptions(), classDescription);
  }

  protected void generateAssertionsForGetters(StringBuilder assertionsForGetters, Set<GetterDescription> getters,
                                              ClassDescription classDescription) {
    for (GetterDescription getter : getters) {
      String assertionContent = assertionContentForProperty(getter, classDescription);
      assertionsForGetters.append(assertionContent).append(LINE_SEPARATOR);
    }
  }

  protected void generateAssertionsForFieldsOf(StringBuilder contentBuilder, ClassDescription classDescription) {
    generateAssertionsForFields(contentBuilder, classDescription.getFieldsDescriptions(), classDescription);
  }

  protected void generateAssertionsForDeclaredFieldsOf(StringBuilder contentBuilder,
                                                       ClassDescription classDescription) {
    generateAssertionsForFields(contentBuilder, classDescription.getDeclaredFieldsDescriptions(),
                                classDescription);
  }

  protected void generateAssertionsForFields(StringBuilder assertionsForPublicFields,
                                             Set<FieldDescription> fields, ClassDescription classDescription) {
    for (FieldDescription field : fields) {
      if (generateAssertionsForAllFields || field.isPublic()) {
        String assertionContent = assertionContentForField(field, classDescription);
        // assertion can be empty if we have a getter for the field
        if (!assertionContent.isEmpty()) {
          assertionsForPublicFields.append(assertionContent).append(LINE_SEPARATOR);
        }
      }
    }
  }

  private String assertionContentForField(FieldDescription field, ClassDescription classDescription) {

    if (classDescription.hasGetterForField(field)) {
      // the assertion has already been generated using the getter to read the field
      return "";
    }

    final String fieldName = field.getName();
    String assertionContent = baseAssertionContentFor(field, classDescription);

    // we reuse template for properties to have consistent assertions for property and field but change the way we get
    // the value since it's a field and not a property:
    assertionContent = assertionContent.replace("${getter}()", PROPERTY_WITH_LOWERCASE_FIRST_CHAR);
    // - remove also ${throws} and ${throws_javadoc} as it does not make sense for a field
    assertionContent = remove(assertionContent, THROWS);
    assertionContent = remove(assertionContent, THROWS_JAVADOC);

    if (!field.isPublic()) {
      // if field is not public, we need to use reflection to get its value, ex :
      // org.assertj.core.util.introspection.FieldSupport.EXTRACTION.fieldValue("grade", Grade.class, actual);
      assertionContent = assertionContent.replace("actual." + PROPERTY_WITH_LOWERCASE_FIRST_CHAR,
                                                  format(NON_PUBLIC_FIELD_VALUE_EXTRACTION,
                                                         PROPERTY_WITH_LOWERCASE_FIRST_CHAR, PROPERTY_TYPE));
    }
    if (field.isPredicate()) {
      assertionContent = fillAssertionContentForPredicateField(field, assertionContent);
    }
    assertionContent = replace(assertionContent, PROPERTY_WITH_UPPERCASE_FIRST_CHAR, capitalize(field.getName()));
    assertionContent = replace(assertionContent, PROPERTY_SIMPLE_TYPE, getTypeName(field));
    assertionContent = replace(assertionContent, PROPERTY_ASSERT_TYPE,
                               field.getAssertTypeName(determinePackageName(classDescription)));
    assertionContent = replace(assertionContent, PROPERTY_TYPE, getTypeName(field));
    assertionContent = replace(assertionContent, PROPERTY_WITH_LOWERCASE_FIRST_CHAR, fieldName);
    // It should not be possible to have a field that is a keyword - compiler won't allow it.
    assertionContent = replace(assertionContent, PROPERTY_WITH_SAFE, fieldName);
    return assertionContent;
  }

  private String getTypeName(DataDescription fieldOrGetter) {
    if (generatedAssertionsPackage != null) {
      // if the user has chosen to generate assertions in a given package we assume that 
      return fieldOrGetter.getFullyQualifiedTypeName();
    }
    // returns a simple class name if the field or getter type is in the same package as its owning type which is the package where the 
    // Assert class is generated. 
    return fieldOrGetter.getTypeName();
  }

  private String fillAssertionContentForPredicateField(FieldDescription field, String assertionContent) {
    if (field.isPublic()) {
      assertionContent = assertionContent.replace("actual." + PREDICATE + "()",
                                                  "actual." + field.getOriginalMember().getName());
    } else {
      // if field is not public, we need to use reflection to get its value, ex :
      // org.assertj.core.util.introspection.FieldSupport.EXTRACTION.fieldValue("active", Boolean.class, actual);
      assertionContent = assertionContent.replace("actual." + PREDICATE + "()",
                                                  format(NON_PUBLIC_FIELD_VALUE_EXTRACTION,
                                                         field.getOriginalMember().getName(), "Boolean"));
    }
    assertionContent = assertionContent.replace(PREDICATE_FOR_JAVADOC,
                                                field.getPredicateForJavadoc());
    assertionContent = assertionContent.replace(NEGATIVE_PREDICATE_FOR_JAVADOC,
                                                field.getNegativePredicateForJavadoc());
    assertionContent = assertionContent.replace(PREDICATE_FOR_FOR_ERROR_MESSAGE_PART1,
                                                field.getPredicateForErrorMessagePart1());
    assertionContent = assertionContent.replace(PREDICATE_FOR_FOR_ERROR_MESSAGE_PART2,
                                                field.getPredicateForErrorMessagePart2());
    assertionContent = assertionContent.replace(NEGATIVE_PREDICATE_FOR_FOR_ERROR_MESSAGE_PART1,
                                                field.getNegativePredicateForErrorMessagePart1());
    assertionContent = assertionContent.replace(NEGATIVE_PREDICATE_FOR_FOR_ERROR_MESSAGE_PART2,
                                                field.getNegativePredicateForErrorMessagePart2());
    assertionContent = replace(assertionContent, PREDICATE, field.getPredicate());
    assertionContent = replace(assertionContent, PREDICATE_NEG, field.getNegativePredicate());
    return assertionContent;
  }

  static private String getSafeProperty(String unsafe) {
    return JAVA_KEYWORDS.contains(unsafe) ? "expected" + capitalize(unsafe) : unsafe;
  }

  private String assertionContentForProperty(GetterDescription getter, ClassDescription classDescription) {
    String assertionContent = baseAssertionContentFor(getter, classDescription);

    assertionContent = declareExceptions(getter, assertionContent);

    String propertyName = getter.getName();
    if (getter.isPredicate()) {
      assertionContent = assertionContent.replace(PREDICATE_FOR_JAVADOC,
                                                  getter.getPredicateForJavadoc());
      assertionContent = assertionContent.replace(NEGATIVE_PREDICATE_FOR_JAVADOC,
                                                  getter.getNegativePredicateForJavadoc());
      assertionContent = assertionContent.replace(PREDICATE_FOR_FOR_ERROR_MESSAGE_PART1,
                                                  getter.getPredicateForErrorMessagePart1());
      assertionContent = assertionContent.replace(PREDICATE_FOR_FOR_ERROR_MESSAGE_PART2,
                                                  getter.getPredicateForErrorMessagePart2());
      assertionContent = assertionContent.replace(NEGATIVE_PREDICATE_FOR_FOR_ERROR_MESSAGE_PART1,
                                                  getter.getNegativePredicateForErrorMessagePart1());
      assertionContent = assertionContent.replace(NEGATIVE_PREDICATE_FOR_FOR_ERROR_MESSAGE_PART2,
                                                  getter.getNegativePredicateForErrorMessagePart2());
      assertionContent = replace(assertionContent, PREDICATE, getter.getOriginalMember().getName());
      assertionContent = replace(assertionContent, PREDICATE_NEG, getter.getNegativePredicate());
    }
    assertionContent = replace(assertionContent, PROPERTY_GETTER_CALL, getter.getOriginalMember().getName());
    assertionContent = replace(assertionContent, PROPERTY_WITH_UPPERCASE_FIRST_CHAR, capitalize(propertyName));
    assertionContent = replace(assertionContent, PROPERTY_SIMPLE_TYPE, getTypeName(getter));
    assertionContent = replace(assertionContent, PROPERTY_ASSERT_TYPE,
                               getter.getAssertTypeName(determinePackageName(classDescription)));
    assertionContent = replace(assertionContent, PROPERTY_TYPE, getTypeName(getter));
    assertionContent = replace(assertionContent, PROPERTY_WITH_LOWERCASE_FIRST_CHAR, propertyName);
    assertionContent = replace(assertionContent, PROPERTY_WITH_SAFE, getSafeProperty(propertyName));
    return assertionContent;
  }

  /**
   * The assertion content that is common to field and property (getter), the specific content part is handled
   * afterwards.
   *
   * @param fieldOrProperty field or property
   * @return the base assertion content
   */
  private String baseAssertionContentFor(DataDescription fieldOrProperty, ClassDescription classDescription) {
    String assertionContent = templateRegistry.getTemplate(Type.HAS).getContent();
    if (fieldOrProperty.isPredicate()) {
      Type type = determinePredicateType(fieldOrProperty, classDescription);
      assertionContent = templateRegistry.getTemplate(type).getContent();
    } else if (fieldOrProperty.isIterableType()) {
      assertionContent = replace(templateRegistry.getTemplate(Type.HAS_FOR_ITERABLE).getContent(), ELEMENT_TYPE,
                                 fieldOrProperty.getElementTypeName());
      assertionContent = replace(assertionContent, ELEMENT_ASSERT_TYPE,
                                 fieldOrProperty.getElementAssertTypeName());
    } else if (fieldOrProperty.isArrayType()) {
      assertionContent = replace(templateRegistry.getTemplate(Type.HAS_FOR_ARRAY).getContent(), ELEMENT_TYPE,
                                 fieldOrProperty.getElementTypeName());
      assertionContent = replace(assertionContent, ELEMENT_ASSERT_TYPE,
                                 fieldOrProperty.getElementAssertTypeName());
    } else if (fieldOrProperty.isRealNumberType()) {
      Type type = fieldOrProperty.isPrimitiveWrapperType() ? Type.HAS_FOR_REAL_NUMBER_WRAPPER
          : Type.HAS_FOR_REAL_NUMBER;
      assertionContent = templateRegistry.getTemplate(type).getContent();
    } else if (fieldOrProperty.isWholeNumberType()) {
      Type type = fieldOrProperty.isPrimitiveWrapperType() ? Type.HAS_FOR_WHOLE_NUMBER_WRAPPER
          : Type.HAS_FOR_WHOLE_NUMBER;
      assertionContent = templateRegistry.getTemplate(type).getContent();
    } else if (fieldOrProperty.isCharType()) {
      Type type = fieldOrProperty.isPrimitiveWrapperType() ? Type.HAS_FOR_CHARACTER : Type.HAS_FOR_CHAR;
      assertionContent = templateRegistry.getTemplate(type).getContent();
    } else if (fieldOrProperty.isPrimitiveType()) {
      // use case : boolean getFoo -> not a predicate, but a primitive valueType
      Type type = fieldOrProperty.isPrimitiveWrapperType() ? Type.HAS_FOR_PRIMITIVE_WRAPPER : Type.HAS_FOR_PRIMITIVE;
      assertionContent = templateRegistry.getTemplate(type).getContent();
    }
    return assertionContent;
  }

  /**
   * Determine whether we need to generate negative predicate assertions, for example if the class contains isValid and
   * isNotValid methods, we must not generate the negative assertion for isValid as it will be done when generating
   * assertions for isNotValid
   */
  private Type determinePredicateType(final DataDescription fieldOrProperty, final ClassDescription classDescription) {
    if (hasAlreadyNegativePredicate(fieldOrProperty, classDescription)) {
      return fieldOrProperty.isPrimitiveWrapperType() ? Type.IS_WRAPPER_WITHOUT_NEGATION : Type.IS_WITHOUT_NEGATION;
    }
    return fieldOrProperty.isPrimitiveWrapperType() ? Type.IS_WRAPPER : Type.IS;
  }

  private boolean hasAlreadyNegativePredicate(final DataDescription fieldOrProperty,
                                              final ClassDescription classDescription) {
    for (final GetterDescription getterDescription : classDescription.getGettersDescriptions()) {
      if (getterDescription.getOriginalMember().getName().equals(fieldOrProperty.getNegativePredicate())) return true;
    }
    return false;
  }

  /**
   * Handle case where getter throws an exception.
   *
   * @param getter method we want to declare exception for
   * @param assertionContent the assertion content to enrich
   * @return assertion content with thrown exceptions
   */
  private String declareExceptions(GetterDescription getter, String assertionContent) {
    StringBuilder throwsClause = new StringBuilder();
    StringBuilder throwsJavaDoc = new StringBuilder();
    boolean first = true;
    for (TypeToken<?> exception : getter.getExceptions()) {
      if (first) throwsClause.append("throws ");
      else throwsClause.append(", ");
      first = false;
      String exceptionName = getTypeDeclaration(exception);
      throwsClause.append(exceptionName);
      throwsJavaDoc.append(LINE_SEPARATOR).append("   * @throws ").append(exceptionName);
      throwsJavaDoc.append(" if actual.").append("${getter}() throws one.");
    }
    if (!getter.getExceptions().isEmpty()) throwsClause.append(' ');

    assertionContent = assertionContent.replace(THROWS_JAVADOC, throwsJavaDoc.toString());
    assertionContent = assertionContent.replace(THROWS, throwsClause.toString());
    return assertionContent;
  }

  private void fillFile(String customAssertionContent, File assertionJavaFile) throws IOException {
    try (FileWriter fileWriter = new FileWriter(assertionJavaFile, false)) {
      fileWriter.write(customAssertionContent);
    }
  }

  private File createFile(String fileContent, String fileName, String targetDirectory) throws IOException {
    File file = new File(targetDirectory, fileName);

    // Ignore the result as it only returns false when the file existed previously which is not wrong.
    // noinspection ResultOfMethodCallIgnored
    file.createNewFile();
    fillFile(fileContent, file);
    return file;
  }

  private static boolean noClassDescriptionsGiven(final Set<ClassDescription> classDescriptionSet) {
    return classDescriptionSet == null || classDescriptionSet.isEmpty();
  }

  private static void buildDirectory(String directoryName) {
    // Ignore the result as it only returns true iff the dir was created, false is not bad.
    File directory = new File(directoryName);
    if (!directory.exists()) directory.mkdirs();
  }

  @Override
  public void register(Template template) {
    templateRegistry.register(template);
  }
}
