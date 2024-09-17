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
 * Copyright 2012-2021 the original author or authors.
 */
package org.assertj.assertions.generator;

import com.google.common.base.Optional;
import com.google.common.reflect.TypeToken;
import org.assertj.assertions.generator.data.*;
import org.assertj.assertions.generator.data.art.ArtWork;
import org.assertj.assertions.generator.data.nba.Player;
import org.assertj.assertions.generator.data.nba.PlayerAgent;
import org.assertj.assertions.generator.description.ClassDescription;
import org.assertj.assertions.generator.description.converter.AnnotationConfiguration;
import org.assertj.assertions.generator.description.converter.ClassToClassDescriptionConverter;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static java.lang.reflect.Modifier.isPublic;
import static java.nio.charset.Charset.defaultCharset;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.remove;
import static org.apache.commons.lang3.StringUtils.replace;
import static org.assertj.assertions.generator.util.ClassUtil.collectClasses;
import static org.assertj.core.api.Assertions.*;

@RunWith(Theories.class)
public class AssertionGeneratorTest implements NestedClassesTest, BeanWithExceptionsTest {
  private static final String LINE_SEPARATOR = System.lineSeparator();
  private static final Logger logger = LoggerFactory.getLogger(AssertionGeneratorTest.class);
  private ClassToClassDescriptionConverter converter;
  private BaseAssertionGenerator assertionGenerator;

  @Rule
  public final GenerationPathHandler generationPathHandler = new GenerationPathHandler(AssertionGeneratorTest.class,
                                                                                       Paths.get("src/test/resources"));
  private static final Set<TypeToken<?>> EMPTY_HIERARCHY = new HashSet<>();

  @Before
  public void beforeEachTest() throws IOException {
    converter = new ClassToClassDescriptionConverter();
    assertionGenerator = generationPathHandler.buildAssertionGenerator();
  }

  @Test
  public void should_generate_assertion_for_player_class() throws Exception {
    verifyFlatAssertionGenerationFor(Player.class);
    verifyHierarchicalAssertionGenerationFor(Player.class);
  }

  @Test
  public void should_generate_assertions_in_given_package() throws Exception {
    String generatedAssertionPackage = "my.assertions";
    assertionGenerator.setGeneratedAssertionsPackage(generatedAssertionPackage);
    verifyFlatAssertionGenerationFor(Player.class, generatedAssertionPackage);
    verifyHierarchicalAssertionGenerationFor(Player.class, generatedAssertionPackage);
    verifyFlatAssertionGenerationFor(PlayerAgent.class, generatedAssertionPackage);
    verifyHierarchicalAssertionGenerationFor(PlayerAgent.class, generatedAssertionPackage);
  }

  @Test
  public void should_generate_assertion_for_interface() throws Exception {
    verifyFlatAssertionGenerationFor(PlayerAgent.class);
    verifyHierarchicalAssertionGenerationFor(PlayerAgent.class);
  }

  @Test
  public void should_generate_assertion_for_class_with_public_fields() throws Exception {
    verifyFlatAssertionGenerationFor(Team.class);
    verifyHierarchicalAssertionGenerationFor(Team.class);
  }

  @Test
  public void should_generate_assertion_for_comparable_class() throws Exception {
    verifyFlatAssertionGenerationFor(Name.class);
    verifyHierarchicalAssertionGenerationFor(Name.class);
  }

  @Test
  public void should_generate_assertion_for_class_with_private_fields() throws Exception {
    Set<TypeToken<?>> classesInHierarchy = setOfTypeTokens(WithPrivateFieldsParent.class);
    assertionGenerator.setGenerateAssertionsForAllFields(true);
    verifyFlatAssertionGenerationFor(WithPrivateFields.class);
    verifyHierarchicalAssertionGenerationFor(WithPrivateFields.class, classesInHierarchy);
  }

  @Test
  public void should_generate_assertion_for_class_with_properties_that_clash_with_fields() throws Exception {
    verifyFlatAssertionGenerationFor(FieldPropertyClash.class);
    verifyHierarchicalAssertionGenerationFor(FieldPropertyClash.class);
  }

  @Test
  public void should_generate_assertion_for_class_with_properties_that_clash_with_keywords() throws Exception {
    verifyFlatAssertionGenerationFor(Keywords.class);
    verifyHierarchicalAssertionGenerationFor(Keywords.class);
  }

  @Test
  public void should_generate_assertion_for_class_with_predicates() throws Exception {
    verifyFlatAssertionGenerationFor(BooleanPredicates.class);
    verifyHierarchicalAssertionGenerationFor(BooleanPredicates.class);
  }

  @Test
  public void should_generate_assertion_for_class_with_primitives() throws Exception {
    verifyFlatAssertionGenerationFor(Primitives.class);
    verifyHierarchicalAssertionGenerationFor(Primitives.class);
  }

  @Test
  public void should_generate_assertion_for_class_with_interference_primitives() throws Exception {
    verifyFlatAssertionGenerationFor(InterferencePrimitives.class);
    verifyHierarchicalAssertionGenerationFor(InterferencePrimitives.class);
  }

  @Test
  public void should_generate_flat_assertion_for_movie_class() throws Exception {
    verifyFlatAssertionGenerationFor(Movie.class);
    assertThat(generationPathHandler.abstractFileGeneratedFor(Movie.class)).doesNotExist();
  }

  @Test
  public void should_generate_hierarchical_assertion_for_artwork_class() throws Exception {
    verifyHierarchicalAssertionGenerationFor(ArtWork.class);
  }

  @Test
  public void should_generate_hierarchical_assertion_for_artwork_classes() throws Exception {
    Set<TypeToken<?>> artClasses = setOfTypeTokens(Movie.class, ArtWork.class, BlockBuster.class);
    verifyHierarchicalAssertionGenerationFor(BlockBuster.class, artClasses);
  }

  @Theory
  public void should_generate_assertion_for_nested_class(NestedClass nestedClass) throws Exception {
    Class<?> clazz = nestedClass.nestedClass;
    assertionGenerator.generateCustomAssertionFor(converter.convertToClassDescription(clazz));
    generationPathHandler.compileGeneratedFilesFor(clazz);
    assertThat(generationPathHandler.fileGeneratedFor(clazz))
        .hasContent(expectedContentFromTemplate(nestedClass, "NestedClassAssert.template.expected.txt"));
  }

  @Theory
  public void should_generate_assertion_for_nested_class_in_given_package(NestedClass nestedClass) throws Exception {
    // GIVEN
    String generatedAssertionPackage = "my.assertions";
    assertionGenerator.setGeneratedAssertionsPackage(generatedAssertionPackage);
    Class<?> clazz = nestedClass.nestedClass;
    // WHEN
    assertionGenerator.generateCustomAssertionFor(converter.convertToClassDescription(clazz));
    // THEN
    generationPathHandler.compileGeneratedFilesFor(generatedAssertionPackage, clazz);
    assertThat(generationPathHandler.fileGeneratedFor(clazz, generatedAssertionPackage))
        .hasContent(expectedContentFromTemplate(nestedClass, "NestedClassAssert.template.generated.in.custom.package.expected.txt"));
  }

  @Theory
  public void should_generate_hierarchical_assertion_for_nested_class(NestedClass nestedClass) throws Exception {
    Class<?> clazz = nestedClass.nestedClass;
    assertionGenerator.generateHierarchicalCustomAssertionFor(converter.convertToClassDescription(clazz),
                                                              EMPTY_HIERARCHY);
    assertThat(generationPathHandler.fileGeneratedFor(clazz)).hasContent(expectedContentFromTemplate(nestedClass,
                                                                                                     "NestedClassAssert.hierarchical.template.expected.txt"));
  }

  @Theory
  public void should_generate_assertion_for_property_with_exception(TypeToken<?> beanType) throws Exception {
    assertionGenerator.generateCustomAssertionFor(converter.convertToClassDescription(beanType));
    Class<?> clazz = beanType.getRawType();
    String expectedContent = contentOf(generationPathHandler.getResourcesDir()
                                                            .resolve("BeanWithOneException.expected.txt").toFile(),
                                       defaultCharset());
    if (!BEAN_WITH_ONE_EXCEPTION.equals(beanType)) {
      expectedContent = expectedContent.replace(BEAN_WITH_ONE_EXCEPTION.getRawType().getSimpleName(),
                                                clazz.getSimpleName());
      expectedContent = expectedContent.replace(" throws java.io.IOException ",
                                                " throws java.io.IOException, java.sql.SQLException ");

      List<GetterWithException> getters = asList(STRING_1_EXCEPTION, BOOLEAN_1_EXCEPTION, ARRAY_1_EXCEPTION,
                                                 ITERABLE_1_EXCEPTION);
      Collections.sort(getters);
      for (GetterWithException getter : getters) {
        String throwsClause = generateThrowsClause(IOException.class, getter.getPropertyName(), getter.isBooleanType());
        String replacement = throwsClause
                             + generateThrowsClause(SQLException.class, getter.getPropertyName(),
                                                    getter.isBooleanType());
        expectedContent = expectedContent.replace(throwsClause, replacement);
      }
    }
    assertThat(generationPathHandler.fileGeneratedFor(clazz)).hasContent(expectedContent);
  }

  @Test
  public void should_generate_assertion_for_classes_in_package() throws Exception {
    Set<TypeToken<?>> classes = collectClasses("org.assertj.assertions.generator.data");
    for (TypeToken<?> type : classes) {
      Class<?> clazz = type.getRawType();
      assertThat(clazz.isAnonymousClass()).as("check that <" + clazz.getSimpleName() + "> is not anonymous").isFalse();
      assertThat(clazz.isLocalClass()).as("check that " + clazz.getSimpleName() + " is not local").isFalse();
      assertThat(isPublic(clazz.getModifiers())).as("check that " + clazz.getSimpleName() + " is public").isTrue();
      assertThat(clazz.getName()).as("check that " + clazz.getSimpleName() + " is not package-info")
                                 .doesNotContain("package-info");
      logger.info("Generating assertions for {}", clazz.getName());
      final ClassDescription classDescription = converter.convertToClassDescription(clazz);
      File customAssertionFile = assertionGenerator.generateCustomAssertionFor(classDescription);
      logger.info("Generated {} assertions file -> {}", clazz.getSimpleName(), customAssertionFile.getAbsolutePath());
    }
  }

  @Test
  public void should_generate_assertion_for_classes_in_package_using_provided_class_loader() throws Exception {
    ClassLoader customClassLoader = new MyClassLoader(Thread.currentThread().getContextClassLoader());
    Set<TypeToken<?>> types = collectClasses(customClassLoader, "org.assertj.assertions.generator.data");
    for (TypeToken<?> type : types) {
      Class<?> clazz = type.getRawType();
      assertThat(clazz.getName()).as("check that " + clazz.getSimpleName() + " is not package-info")
                                 .doesNotContain("package-info");
      assertThat(clazz.isAnonymousClass()).as("check that " + clazz.getSimpleName() + " is not anonymous").isFalse();
      assertThat(clazz.isLocalClass()).as("check that " + clazz.getSimpleName() + " is not local").isFalse();
      assertThat(isPublic(clazz.getModifiers())).as("check that " + clazz.getSimpleName() + " is public").isTrue();
      logger.info("Generating assertions for {}", clazz.getName());
      final ClassDescription classDescription = converter.convertToClassDescription(clazz);
      File customAssertionFile = assertionGenerator.generateCustomAssertionFor(classDescription);
      logger.info("Generated {} assertions file -> {}", clazz.getSimpleName(), customAssertionFile.getAbsolutePath());
    }
  }

  @Test
  public void should_generate_assertion_for_classes_using_type_with_same_name() throws IOException {
    verifyFlatAssertionGenerationFor(ClassUsingDifferentClassesWithSameName.class);
    verifyHierarchicalAssertionGenerationFor(ClassUsingDifferentClassesWithSameName.class);
  }

  @Test
  public void should_generate_assertion_for_annotated_methods() throws IOException {
    converter = new ClassToClassDescriptionConverter(new AnnotationConfiguration(GenerateAssertion.class));
    verifyFlatAssertionGenerationFor(AnnotatedClass.class);
    verifyHierarchicalAssertionGenerationFor(AnnotatedClass.class);
  }

  @Test
  public void should_generate_assertion_for_methods_annotated_with_GenerateAssertion_by_default() throws IOException {
    verifyFlatAssertionGenerationFor(AnnotatedClass.class);
    verifyHierarchicalAssertionGenerationFor(AnnotatedClass.class);
  }

  @Test
  public void should_generate_assertion_for_annotated_class() throws IOException {
    converter = new ClassToClassDescriptionConverter(new AnnotationConfiguration(AutoValue.class));
    verifyFlatAssertionGenerationFor(AutoValueAnnotatedClass.class);
    verifyHierarchicalAssertionGenerationFor(AutoValueAnnotatedClass.class);
  }

  @Test
  public void should_generate_assertion_for_class_with_$() throws IOException {
    verifyFlatAssertionGenerationFor(Dollar$.class);
    verifyHierarchicalAssertionGenerationFor(Dollar$.class);
  }

  @Test
  public void should_generate_assertion_for_guava_optional_class() throws IOException {
    verifyFlatAssertionGenerationFor(Optional.class);
    verifyHierarchicalAssertionGenerationFor(Optional.class);
  }

  @Test
  public void should_generate_assertion_without_conflict_with_parameters() throws IOException {
    verifyFlatAssertionGenerationFor(ParameterClashWithVariables.class);
  }

  @Test
  public void should_evaluate_package_as_valid() {
    String[] validPackages = { "a", "a.b.c", "my.assertions" };
    for (int i = 0; i < validPackages.length; i++) {
      assertionGenerator.setGeneratedAssertionsPackage(validPackages[i]);
    }
  }

  @Test
  public void should_evaluate_package_as_invalid() {
    String[] invalidPackages = { "", "   ", " com.my.assertions", "com.my.assertions " };
    for (int i = 0; i < invalidPackages.length; i++) {
      try {
        assertionGenerator.setGeneratedAssertionsPackage(invalidPackages[i]);
      } catch (IllegalArgumentException e) {
        assertThat(e).hasMessageStartingWith("The given package");
        continue;
      }
      fail("Expecting '%s' to be evaluated as invalid", invalidPackages[i]);
    }
  }

  private String expectedContentFromTemplate(NestedClass nestedClass, String fileTemplate) throws IOException {
    String template = contentOf(generationPathHandler.getResourcesDir().resolve(fileTemplate).toFile(),
                                defaultCharset());
    String content = replace(template, "${nestedClass}Assert",
                             remove(nestedClass.classNameWithOuterClass, '.') + "Assert");
    content = replace(content, "${nestedClass}", nestedClass.classNameWithOuterClass);
    content = replace(content, "${fullyQualifiedOuterClassName}", nestedClass.fullyQualifiedOuterClassName);
    return content;
  }

  @SuppressWarnings("WeakerAccess") class MyClassLoader extends ClassLoader {
    public MyClassLoader(ClassLoader parent) {
      super(parent);
    }
  }

  private void verifyFlatAssertionGenerationFor(Class<?> clazz) throws IOException {
    logger.info("Generating flat assertions for {}", clazz);
    ClassDescription classDescription = converter.convertToClassDescription(clazz);
    assertionGenerator.generateCustomAssertionFor(classDescription);
    String expectedAssertFile = clazz.getSimpleName() + "Assert.flat.expected.txt";
    generationPathHandler.assertGeneratedAssertClass(clazz, expectedAssertFile, true);
  }

  private void verifyFlatAssertionGenerationFor(Class<?> clazz, String generatedAssertionPackage) throws IOException {
    String expectedAssertFile = clazz.getSimpleName() + "Assert.generated.in.custom.package.flat.expected.txt";
    logger.info("Generating flat assertions for {} in package {}", clazz, generatedAssertionPackage);
    ClassDescription classDescription = converter.convertToClassDescription(clazz);
    assertionGenerator.generateCustomAssertionFor(classDescription);
    generationPathHandler.assertGeneratedAssertClass(clazz, expectedAssertFile, true, generatedAssertionPackage);
  }

  private void verifyHierarchicalAssertionGenerationFor(Class<?> clazz) throws IOException {
    verifyHierarchicalAssertionGenerationFor(clazz, EMPTY_HIERARCHY);
  }

  private void verifyHierarchicalAssertionGenerationFor(Class<?> aClass,
                                                        Set<TypeToken<?>> typeHierarchy) throws IOException {

    List<File> generatedAssertFiles = newArrayList();
    Set<Class<?>> classes = toClasses(aClass, typeHierarchy);
    logger.info("Generating hierarchical assertions for {}", classes);

    for (Class<?> clazz : classes) {
      ClassDescription classDescription = converter.convertToClassDescription(clazz);
      generatedAssertFiles.addAll(asList(assertionGenerator.generateHierarchicalCustomAssertionFor(classDescription, typeHierarchy)));

      String expectedConcreteAssertFile = clazz.getSimpleName() + "Assert.expected.txt";
      generationPathHandler.assertGeneratedAssertClass(clazz, expectedConcreteAssertFile, false);

      String expectedAbstractAssertFile = "Abstract" + clazz.getSimpleName() + "Assert.expected.txt";
      generationPathHandler.assertAbstractGeneratedAssertClass(clazz, expectedAbstractAssertFile);
    }
    generationPathHandler.compileGeneratedFiles(generatedAssertFiles);
  }

  private void verifyHierarchicalAssertionGenerationFor(Class<?> aClass, String generatedAssertionPackage) throws IOException {

    List<File> generatedAssertFiles = newArrayList();
    Set<Class<?>> classes = toClasses(aClass, EMPTY_HIERARCHY);
    logger.info("Generating hierarchical assertions for {} in package {}", aClass, generatedAssertionPackage);

    for (Class<?> clazz : classes) {
      ClassDescription classDescription = converter.convertToClassDescription(clazz);
      generatedAssertFiles.addAll(asList(assertionGenerator.generateHierarchicalCustomAssertionFor(classDescription, EMPTY_HIERARCHY)));

      String expectedConcreteAssertFile = clazz.getSimpleName() + "Assert.generated.in.custom.package.expected.txt";
      generationPathHandler.assertGeneratedAssertClass(clazz, expectedConcreteAssertFile, false, generatedAssertionPackage);

      String expectedAbstractAssertFile = "Abstract" + clazz.getSimpleName() + "Assert.generated.in.custom.package.expected.txt";
      generationPathHandler.assertAbstractGeneratedAssertClass(clazz, expectedAbstractAssertFile, generatedAssertionPackage);
    }
    generationPathHandler.compileGeneratedFiles(generatedAssertFiles);
  }

  private static Set<Class<?>> toClasses(Class<?> clazz, Set<TypeToken<?>> typeHierarchy) {
    Set<Class<?>> classes = new HashSet<>();
    for (TypeToken<?> type : typeHierarchy) {
      classes.add(type.getRawType());
    }
    classes.add(clazz);
    return classes;
  }

  private String generateThrowsClause(Class<?> exception, String property, boolean booleanType) {
    String getter = (booleanType ? "is" : "get") + Character.toUpperCase(property.charAt(0)) + property.substring(1);
    return "   * @throws " + exception.getName() + " if actual." + getter + "() throws one." + LINE_SEPARATOR;
  }

  private static Set<TypeToken<?>> setOfTypeTokens(Class<?>... classes) {
    Set<TypeToken<?>> types = new HashSet<>();
    for (Class<?> clazz : classes) {
      types.add(TypeToken.of(clazz));
    }
    return types;
  }

}
