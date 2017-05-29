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

import com.google.common.reflect.TypeToken;
import org.assertj.assertions.generator.Template.Type;
import org.assertj.assertions.generator.description.ClassDescription;
import org.assertj.assertions.generator.description.DataDescription;
import org.assertj.assertions.generator.description.FieldDescription;
import org.assertj.assertions.generator.description.GetterDescription;
import org.assertj.assertions.generator.util.ClassUtil;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.*;
import static org.assertj.assertions.generator.Template.Type.ASSERT_CLASS;

@SuppressWarnings("WeakerAccess")
public class BaseAssertionGenerator implements AssertionGenerator, AssertionsEntryPointGenerator {

  static final String ABSTRACT_ASSERT_CLASS_PREFIX = "Abstract";
  static final String ASSERT_CLASS_SUFFIX = "Assert";
  static final String ASSERT_CLASS_FILE_SUFFIX = ASSERT_CLASS_SUFFIX + ".java";
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
  private static final String PROPERTY_TYPE_PARAM = "${propertyType_parameter}";
  private static final String PROPERTY_SIMPLE_TYPE = "${propertySimpleType}";
  private static final String PROPERTY_ASSERT_TYPE = "${propertyAssertType}";
  private static final String CLASS_TO_ASSERT = "${class_to_assert}";
  private static final String CUSTOM_ASSERTION_CLASS = "${custom_assertion_class}";
  private static final String SUPER_ASSERTION_CLASS = "${super_assertion_class}";
  private static final String SELF_TYPE = "${self_type}";
  private static final String MYSELF = "${myself}";
  private static final String ELEMENT_TYPE = "${elementType}";
  private static final String ELEMENT_ASSERT_TYPE = "${elementAssertType}";
  private static final String ALL_ASSERTIONS_ENTRY_POINTS = "${all_assertions_entry_points}";
  private static final String IMPORTS = "${imports}";
  private static final String THROWS = "${throws}";
  private static final String THROWS_JAVADOC = "${throws_javadoc}";
  private static final String LINE_SEPARATOR = "\n";
  // assertions classes are generated in their package directory starting from targetBaseDirectory.
  // ex : com.nba.Player -> targetBaseDirectory/com/nba/PlayerAssert.java
  private File targetBaseDirectory = Paths.get(".").toFile();
  private TemplateRegistry templateRegistry;// the pattern to search for

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

  @Override
  public File generateCustomAssertionFor(ClassDescription classDescription) throws IOException {
    // Assertion content
    String assertionFileContent = generateCustomAssertionContentFor(classDescription);
    // finally create the assertion file, located in its package directory starting from targetBaseDirectory
    String targetDirectory = getDirectoryPathCorrespondingToPackage(classDescription.getPackageName());
    // build any needed directories
    buildTargetDirectory(targetDirectory);
    return createFile(assertionFileContent, assertClassNameOf(classDescription) + ".java", targetDirectory);
  }

  @Override
  public File[] generateHierarchicalCustomAssertionFor(ClassDescription classDescription, Set<TypeToken<?>> allClasses)
      throws IOException {

    // Assertion content
    String[] assertionFileContent = generateHierarchicalCustomAssertionContentFor(classDescription, allClasses);
    // finally create the assertion file, located in its package directory starting from targetBaseDirectory
    String targetDirectory = getDirectoryPathCorrespondingToPackage(classDescription.getPackageName());
    // build any needed directories
    buildTargetDirectory(targetDirectory);
    File[] assertionClassesFile = new File[2];
    final String concreteAssertClassFileName = assertClassNameOf(classDescription) + ".java";
    final String abstractAssertClassFileName = abstractAssertClassNameOf(classDescription) + ".java";
    assertionClassesFile[0] = createFile(assertionFileContent[0], abstractAssertClassFileName, targetDirectory);
    assertionClassesFile[1] = createFile(assertionFileContent[1], concreteAssertClassFileName, targetDirectory);
    return assertionClassesFile;
  }

  @Override
  public String[] generateHierarchicalCustomAssertionContentFor(ClassDescription classDescription,
                                                                Set<TypeToken<?>> allClasses) {
    // use abstract class template first
    String abstractClassTemplateContent = templateRegistry.getTemplate(Type.ABSTRACT_ASSERT_CLASS).getContent();
    StringBuilder abstractAssertClassContentBuilder = new StringBuilder(abstractClassTemplateContent);

    // generate assertion method for each property with a public getter
    generateAssertionsForDeclaredGettersOf(abstractAssertClassContentBuilder, classDescription);
    generateAssertionsForDeclaredPublicFieldsOf(abstractAssertClassContentBuilder, classDescription);

    // close class with }
    abstractAssertClassContentBuilder.append(LINE_SEPARATOR).append("}").append(LINE_SEPARATOR);

    // use concrete class template for the subclass of the generated abstract assert
    String concreteAssertClassContent = templateRegistry.getTemplate(Type.HIERARCHICAL_ASSERT_CLASS).getContent();

    // return a String array with the actual generated of the assertion class hierarchy
    String[] assertionClassesContent = new String[2];
    assertionClassesContent[0] = fillAssertClassTemplate(abstractAssertClassContentBuilder.toString(),
                                                         classDescription, allClasses, false);
    assertionClassesContent[1] = fillAssertClassTemplate(concreteAssertClassContent,
                                                         classDescription, null, true);
    return assertionClassesContent;
  }

  private String fillAssertClassTemplate(String template, ClassDescription classDescription,
                                         Set<TypeToken<?>> classesHierarchy, boolean concrete) {
    // Add any AssertJ needed imports only, other types are used with their fully qualified names to avoid a compilation
    // error when two types have the same
    TreeSet<String> assertjImports = new TreeSet<>();
    if (template.contains("Assertions.")) assertjImports.add("org.assertj.core.api.Assertions");
    if (template.contains("Objects.")) assertjImports.add("org.assertj.core.util.Objects");
    if (template.contains("Iterables.")) assertjImports.add("org.assertj.core.internal.Iterables");

    final String superAssertionClassName;
    // Add assertion supertype to imports if needed
    if (classesHierarchy == null || !classesHierarchy.contains(classDescription.getSuperType())) {
      superAssertionClassName = "org.assertj.core.api.AbstractObjectAssert";
    } else {
      superAssertionClassName = abstractAssertClassNameOf(classDescription.getSuperType());
    }
    assertjImports.add(superAssertionClassName);

    final String customAssertionClass = concrete ? assertClassNameOf(classDescription)
        : abstractAssertClassNameOf(classDescription);
    final String selfType = concrete ? customAssertionClass : "S";
    final String myself = concrete ? "this" : "myself";

    template = replace(template, PACKAGE, classDescription.getPackageName());
    template = replace(template, CUSTOM_ASSERTION_CLASS, customAssertionClass);
    // className could be a nested class like "OuterClass.NestedClass", in that case assert class will be
    // OuterClassNestedClass
    template = replace(template, SUPER_ASSERTION_CLASS,
                       ClassUtil.getTypeNameWithoutDots(superAssertionClassName));
    template = replace(template, CLASS_TO_ASSERT, classDescription.getClassNameWithOuterClass());
    template = replace(template, SELF_TYPE, selfType);
    template = replace(template, MYSELF, myself);
    template = replace(template, IMPORTS, listNeededImports(assertjImports, classDescription.getPackageName()));

    return template;
  }

  private String fillAssertClassTemplate(String template, ClassDescription classDescription) {
    return fillAssertClassTemplate(template, classDescription, null, true);
  }

  @Override
  public String generateCustomAssertionContentFor(ClassDescription classDescription) {

    // use class template first
    String classTemplateContent = templateRegistry.getTemplate(ASSERT_CLASS).getContent();
    StringBuilder assertionFileContentBuilder = new StringBuilder(classTemplateContent);

    // generate assertion method for each property with a public getter
    generateAssertionsForGettersOf(assertionFileContentBuilder, classDescription);
    generateAssertionsForPublicFieldsOf(assertionFileContentBuilder, classDescription);

    // close class with }
    assertionFileContentBuilder.append(LINE_SEPARATOR).append("}").append(LINE_SEPARATOR);

    return fillAssertClassTemplate(assertionFileContentBuilder.toString(), classDescription);
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
    buildTargetDirectory(assertionsDirectory);
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
                                                 fullyQualifiedAssertClassName(classDescription));
      // resolve class (ex: Player)
      // in case of inner classes like Movie.PublicCategory use class name with outer class i.e. Movie.PublicCategory.
      assertionEntryPointMethodContent = replace(assertionEntryPointMethodContent, CLASS_TO_ASSERT,
                                                 classDescription.getFullyQualifiedClassName());
      allAssertThatsContentBuilder.append(lineSeparator).append(assertionEntryPointMethodContent);
    }
    return allAssertThatsContentBuilder.toString();
  }

  private String determineBestEntryPointsAssertionsClassPackage(final Set<ClassDescription> classDescriptionSet) {
    SortedSet<String> packages = new TreeSet<>(new Comparator<String>() {
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

  private static String assertClassNameOf(ClassDescription classDescription) {
    return assertClassNameOf(classDescription.getType());
  }

  private static String assertClassNameOf(TypeToken<?> type) {
    return ClassUtil.getTypeNameWithoutDots(ClassUtil.getTypeDeclaration(type, false, false)) + ASSERT_CLASS_SUFFIX;
  }

  private static String abstractAssertClassNameOf(ClassDescription classDescription) {
    return abstractAssertClassNameOf(classDescription.getType());
  }

  private static String abstractAssertClassNameOf(TypeToken<?> type) {
    return ABSTRACT_ASSERT_CLASS_PREFIX + assertClassNameOf(type);
  }

  private static String fullyQualifiedAssertClassName(ClassDescription classDescription) {
    return classDescription.getPackageName() + "." + classDescription.getClassNameWithOuterClassNotSeparatedByDots()
           + ASSERT_CLASS_SUFFIX;
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

  private static String listNeededImports(Set<String> typesToImport, String classPackage) {
    StringBuilder importsBuilder = new StringBuilder();
    for (String type : typesToImport) {
      try {
        Class<?> clazz = Class.forName(type);
        if (!clazz.isPrimitive() && !ClassUtil.isJavaLangType(clazz)
            && !ClassUtil.isInnerPackageOf(type, classPackage) && !Objects.equals(classPackage, type)) {
          importsBuilder.append(format(IMPORT_LINE, type, LINE_SEPARATOR));
        }
      } catch (ClassNotFoundException cfne) {
        // continue iteration;
      }
    }
    return importsBuilder.toString();
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

  protected void generateAssertionsForPublicFieldsOf(StringBuilder contentBuilder, ClassDescription classDescription) {
    generateAssertionsForPublicFields(contentBuilder, classDescription.getFieldsDescriptions(), classDescription);
  }

  protected void generateAssertionsForDeclaredPublicFieldsOf(StringBuilder contentBuilder,
                                                             ClassDescription classDescription) {
    generateAssertionsForPublicFields(contentBuilder, classDescription.getDeclaredFieldsDescriptions(),
                                      classDescription);
  }

  protected void generateAssertionsForPublicFields(StringBuilder assertionsForPublicFields,
                                                   Set<FieldDescription> fields, ClassDescription classDescription) {
    for (FieldDescription field : fields) {
      String assertionContent = assertionContentForField(field, classDescription);
      assertionsForPublicFields.append(assertionContent).append(LINE_SEPARATOR);
    }
  }

  private String assertionContentForField(FieldDescription field, ClassDescription classDescription) {
    final TypeToken<?> owningType = field.getOwningType();

    final String fieldName = field.getName();
    final String fieldNameCap = capitalize(field.getName());
    try {
      Method m = owningType.getRawType().getMethod("get" + fieldNameCap);
      if (classDescription.getGettersDescriptions().contains(new GetterDescription(fieldName, owningType, m))) {
        return "";
      }
    } catch (NoSuchMethodException nsme) {
      // ignore it, let flow keep going
    }

    String assertionContent = baseAssertionContentFor(field, classDescription);

    // we reuse template for properties to have consistent assertions for property and field but change the way we get
    // the value since it's a field and not a property:
    assertionContent = assertionContent.replace("${getter}()", PROPERTY_WITH_LOWERCASE_FIRST_CHAR);
    // - remove also ${throws} and ${throws_javadoc} since it does not make any sense for a field
    assertionContent = remove(assertionContent, "${throws}");
    assertionContent = remove(assertionContent, "${throws_javadoc}");

    // replace ${Property} and ${property} by field name (starting with uppercase/lowercase)
    if (field.isPredicate()) {
      assertionContent = assertionContent
          .replace("actual." + PREDICATE + "()", "actual." + field.getOriginalMember().getName());
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
    }
    assertionContent = replace(assertionContent, PROPERTY_WITH_UPPERCASE_FIRST_CHAR, fieldNameCap);
    assertionContent = replace(assertionContent, PROPERTY_SIMPLE_TYPE,
                               field.getTypeName(false, false));
    assertionContent = replace(assertionContent, PROPERTY_ASSERT_TYPE,
                               field.getAssertTypeName(classDescription.getPackageName()));
    assertionContent = replace(assertionContent, PROPERTY_TYPE,
                               field.getFullyQualifiedTypeNameIfNeeded(classDescription.getPackageName(), false));
    assertionContent = replace(assertionContent, PROPERTY_TYPE_PARAM,
                               field.getFullyQualifiedTypeNameIfNeeded(classDescription.getPackageName(), true));
    assertionContent = replace(assertionContent, PROPERTY_WITH_LOWERCASE_FIRST_CHAR, fieldName);
    // It should not be possible to have a field that is a keyword - compiler won't allow it.
    assertionContent = replace(assertionContent, PROPERTY_WITH_SAFE, fieldName);
    return assertionContent;
  }

  static private final Set<String> JAVA_KEYWORDS = new HashSet<>();

  static {
    String[] keywords = new String[] {
        "abstract",
        "assert",
        "boolean",
        "break",
        "byte",
        "case",
        "catch",
        "char",
        // This one's not strictly required because you can't have a property called "class"
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
        "while",
    };
    Collections.addAll(JAVA_KEYWORDS, keywords);
  }

  static private String getSafeProperty(String unsafe) {
    return JAVA_KEYWORDS.contains(unsafe) ? "expected" + capitalize(unsafe) : unsafe;
  }

  private String assertionContentForProperty(GetterDescription getter, ClassDescription classDescription) {
    String assertionContent = baseAssertionContentFor(getter, classDescription);

    assertionContent = declareExceptions(getter, assertionContent, classDescription);

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
    assertionContent = replace(assertionContent, PROPERTY_SIMPLE_TYPE,
                               getter.getTypeName(false, false));
    assertionContent = replace(assertionContent, PROPERTY_ASSERT_TYPE,
                               getter.getAssertTypeName(classDescription.getPackageName()));
    assertionContent = replace(assertionContent, PROPERTY_TYPE,
                               getter.getFullyQualifiedTypeNameIfNeeded(classDescription.getPackageName(), false));
    assertionContent = replace(assertionContent, PROPERTY_TYPE_PARAM,
                               getter.getFullyQualifiedTypeNameIfNeeded(classDescription.getPackageName(), true));
    assertionContent = replace(assertionContent, PROPERTY_WITH_LOWERCASE_FIRST_CHAR, propertyName);
    assertionContent = replace(assertionContent, PROPERTY_WITH_SAFE, getSafeProperty(propertyName));
    return assertionContent;
  }

  /**
   * The assertion content that is common to field and property (getter), the specific content part is handled
   * afterwards.
   *
   * @param fieldOrProperty
   * @return the base assertion content
   */
  private String baseAssertionContentFor(DataDescription fieldOrProperty, ClassDescription classDescription) {
    String assertionContent = templateRegistry.getTemplate(Type.HAS).getContent();
    if (fieldOrProperty.isPredicate()) {
      Type type = determinePredicateType(fieldOrProperty, classDescription);
      assertionContent = templateRegistry.getTemplate(type).getContent();
    } else if (fieldOrProperty.isIterableType()) {
      assertionContent = replace(templateRegistry.getTemplate(Type.HAS_FOR_ITERABLE).getContent(), ELEMENT_TYPE,
                                 fieldOrProperty.getElementTypeName(classDescription.getPackageName()));
      assertionContent = replace(assertionContent, ELEMENT_ASSERT_TYPE,
                                 fieldOrProperty.getElementAssertTypeName(classDescription.getPackageName()));
    } else if (fieldOrProperty.isArrayType()) {
      assertionContent = replace(templateRegistry.getTemplate(Type.HAS_FOR_ARRAY).getContent(), ELEMENT_TYPE,
                                 fieldOrProperty.getElementTypeName(classDescription.getPackageName()));
      assertionContent = replace(assertionContent, ELEMENT_ASSERT_TYPE,
                                 fieldOrProperty.getElementAssertTypeName(classDescription.getPackageName()));
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
   * @param classDescription use to resolve exception
   * @return assertion content with thrown exceptions
   */
  private String declareExceptions(GetterDescription getter, String assertionContent,
                                   ClassDescription classDescription) {
    StringBuilder throwsClause = new StringBuilder();
    StringBuilder throwsJavaDoc = new StringBuilder();
    boolean first = true;
    for (TypeToken<?> exception : getter.getExceptions()) {
      if (first) throwsClause.append("throws ");
      else throwsClause.append(", ");
      first = false;
      String exceptionName = ClassUtil
          .getTypeDeclarationWithinPackage(exception, classDescription.getPackageName(), false);
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

    // Ignore the result as it only returns false when the file existed previously
    // which is not _wrong_.
    //noinspection ResultOfMethodCallIgnored
    file.createNewFile();
    fillFile(fileContent, file);
    return file;
  }

  private static boolean noClassDescriptionsGiven(final Set<ClassDescription> classDescriptionSet) {
    return classDescriptionSet == null || classDescriptionSet.isEmpty();
  }

  private static void buildTargetDirectory(String targetDirectory) {
    // Ignore the result as it only returns true iff the dir was created, false is
    // not bad.
    //noinspection ResultOfMethodCallIgnored
    new File(targetDirectory).mkdirs();
  }

  @Override
  public void register(Template template) {
    templateRegistry.register(template);
  }
}
