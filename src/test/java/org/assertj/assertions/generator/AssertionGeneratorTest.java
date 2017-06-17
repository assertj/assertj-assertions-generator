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
import org.apache.commons.lang3.StringUtils;
import org.assertj.assertions.generator.data.*;
import org.assertj.assertions.generator.data.art.ArtWork;
import org.assertj.assertions.generator.data.generic.MultipleGenerics;
import org.assertj.assertions.generator.data.nba.Player;
import org.assertj.assertions.generator.data.nba.PlayerAgent;
import org.assertj.assertions.generator.description.ClassDescription;
import org.assertj.assertions.generator.description.converter.AnnotationConfiguration;
import org.assertj.assertions.generator.description.converter.ClassToClassDescriptionConverter;
import org.junit.Before;
import org.junit.Ignore;
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
import java.util.*;

import static com.google.common.collect.Lists.newArrayList;
import static java.lang.reflect.Modifier.isPublic;
import static java.nio.charset.Charset.defaultCharset;
import static java.util.Collections.EMPTY_SET;
import static org.apache.commons.lang3.StringUtils.replace;
import static org.assertj.assertions.generator.util.ClassUtil.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.contentOf;

@RunWith(Theories.class)
public class AssertionGeneratorTest implements NestedClassesTest, BeanWithExceptionsTest {
  private static final String LINE_SEPARATOR = "\n";
  private static final Logger logger = LoggerFactory.getLogger(AssertionGeneratorTest.class);
  private ClassToClassDescriptionConverter converter;
  private AssertionGenerator assertionGenerator;
  private static final Set<TypeToken<?>> allClasses =
      new HashSet<>(Arrays.<TypeToken<?>>asList(TypeToken.of(Movie.class), TypeToken.of(ArtWork.class)));

  @Rule
  public final GenerationPathHandler generationPathHandler = new GenerationPathHandler(AssertionGeneratorTest.class,
                                                                                       Paths.get("src/test/resources"));

  @Before
  public void beforeEachTest() throws IOException {
    converter = new ClassToClassDescriptionConverter();
    assertionGenerator = generationPathHandler.buildAssertionGenerator();
  }

  @Test
  public void should_generate_assertion_for_player_class() throws Exception {
    assertionGenerator.generateCustomAssertionFor(converter.convertToClassDescription(Player.class));
    generationPathHandler.assertGeneratedAssertClass(Player.class, "PlayerAssert.expected.txt", true);
  }

  @Test
  public void should_generate_assertion_for_interface() throws Exception {
    assertionGenerator.generateCustomAssertionFor(converter.convertToClassDescription(PlayerAgent.class));
    generationPathHandler.assertGeneratedAssertClass(PlayerAgent.class, "PlayerAgentAssert.expected.txt", true);
  }

  @Test
  public void should_generate_assertion_for_class_with_public_fields() throws Exception {
    assertionGenerator.generateCustomAssertionFor(converter.convertToClassDescription(Team.class));
    generationPathHandler.assertGeneratedAssertClass(Team.class, "TeamAssert.expected.txt", true);
  }

  @Test
  public void should_generate_assertion_for_class_with_properties_that_clash_with_fields() throws Exception {
    assertionGenerator.generateCustomAssertionFor(converter.convertToClassDescription(FieldPropertyClash.class));
    generationPathHandler.assertGeneratedAssertClass(FieldPropertyClash.class, "FieldPropertyClash.expected.txt", true);
  }

  @Test
  public void should_generate_assertion_for_class_with_properties_that_clash_with_keywords() throws Exception {
    assertionGenerator.generateCustomAssertionFor(converter.convertToClassDescription(Keywords.class));
    generationPathHandler.assertGeneratedAssertClass(Keywords.class, "Keywords.expected.txt", true);
  }

  @Test
  public void should_generate_assertion_for_class_with_predicates() throws Exception {
    ClassDescription classDescription = converter.convertToClassDescription(BooleanPredicates.class);
    assertionGenerator.generateCustomAssertionFor(classDescription);
    generationPathHandler.assertGeneratedAssertClass(BooleanPredicates.class, "BooleanPredicates.expected.txt", true);
  }

  @Test
  public void should_generate_assertion_for_class_with_primitives() throws Exception {
    assertionGenerator.generateCustomAssertionFor(converter.convertToClassDescription(Primitives.class));
    generationPathHandler.assertGeneratedAssertClass(Primitives.class, "PrimitivesAssert.expected.txt", true);
  }

  @Test
  public void should_generate_assertion_for_class_with_interference_primitives() throws Exception {
    assertionGenerator.generateCustomAssertionFor(converter.convertToClassDescription(InterferencePrimitives.class));
    generationPathHandler.assertGeneratedAssertClass(InterferencePrimitives.class, "InterferencePrimitivesAssert.expected.txt", true);
  }

  @Test
  public void should_generate_flat_assertion_for_movie_class() throws Exception {
    ClassDescription classDescription = converter.convertToClassDescription(Movie.class);
    assertionGenerator.generateCustomAssertionFor(classDescription);
    generationPathHandler.assertGeneratedAssertClass(Movie.class, "MovieAssert.flat.expected.txt", true);
    assertThat(generationPathHandler.abstractFileGeneratedFor(Movie.class)).doesNotExist();
  }

  @Test
  public void should_generate_hierarchical_assertion_for_movie_class() throws Exception {

    File[] movieFiles = assertionGenerator.generateHierarchicalCustomAssertionFor(converter.convertToClassDescription(Movie.class),
                                                                                  allClasses);
    List<File> generated = newArrayList(movieFiles);

    generationPathHandler.assertGeneratedAssertClass(Movie.class, "MovieAssert.expected.txt", false);
    generationPathHandler.assertAbstractGeneratedAssertClass(Movie.class, "AbstractMovieAssert.expected.txt");

    // These should also be generated!
    File[] artWorkFiles = assertionGenerator.generateHierarchicalCustomAssertionFor(converter.convertToClassDescription(ArtWork.class),
                                                                                    allClasses);
    generated.addAll(Arrays.asList(artWorkFiles));
    generationPathHandler.assertGeneratedAssertClass(ArtWork.class, "ArtWorkAssert.expected.txt", false);
    generationPathHandler.assertAbstractGeneratedAssertClass(ArtWork.class, "AbstractArtWorkAssert.expected.txt");

    // compile them
    generationPathHandler.compileGeneratedFiles(generated);
  }

  @Test
  public void should_generate_hierarchical_assertion_for_artwork_class() throws Exception {
    assertionGenerator.generateHierarchicalCustomAssertionFor(converter.convertToClassDescription(ArtWork.class),
                                                              allClasses);

    generationPathHandler.assertAbstractGeneratedAssertClass(ArtWork.class, "AbstractArtWorkAssert.expected.txt");
    generationPathHandler.assertGeneratedAssertClass(ArtWork.class, "ArtWorkAssert.expected.txt", true);
  }

  @Theory
  public void should_generate_assertion_for_nested_class(NestedClass nestedClass) throws Exception {
    Class<?> clazz = nestedClass.nestedClass;
    assertionGenerator.generateCustomAssertionFor(converter.convertToClassDescription(clazz));
    assertThat(generationPathHandler.fileGeneratedFor(clazz)).hasContent(expectedContentFromTemplate(nestedClass,
                                                                                                     "NestedClassAssert.template.expected.txt"));
  }

  @Theory
  public void should_generate_hierarchical_assertion_for_nested_class(NestedClass nestedClass) throws Exception {
    Class<?> clazz = nestedClass.nestedClass;
    assertionGenerator.generateHierarchicalCustomAssertionFor(converter.convertToClassDescription(clazz), EMPTY_SET);
    assertThat(generationPathHandler.fileGeneratedFor(clazz)).hasContent(expectedContentFromTemplate(nestedClass,
                                                                                                     "NestedClassAssert.hierarchical.template.expected.txt"));
  }

  @Theory
  public void should_generate_assertion_for_property_with_exception(TypeToken<?> beanType) throws Exception {
    assertionGenerator.generateCustomAssertionFor(converter.convertToClassDescription(beanType));
    Class<?> clazz = beanType.getRawType();
    String expectedContent = contentOf(generationPathHandler.getResourcesDir().resolve("BeanWithOneException.expected.txt").toFile(),
                                       defaultCharset());
    if (!BEAN_WITH_ONE_EXCEPTION.equals(beanType)) {
      expectedContent = expectedContent.replace(BEAN_WITH_ONE_EXCEPTION.getRawType().getSimpleName(), clazz.getSimpleName());
      expectedContent = expectedContent.replace(" throws java.io.IOException ",
                                                " throws java.io.IOException, java.sql.SQLException ");

      List<GetterWithException> getters = Arrays.asList(STRING_1_EXCEPTION, BOOLEAN_1_EXCEPTION, ARRAY_1_EXCEPTION,
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
    Class<?> clazz = ClassUsingDifferentClassesWithSameName.class;
    assertionGenerator.generateCustomAssertionFor(converter.convertToClassDescription(clazz));
    generationPathHandler.assertGeneratedAssertClass(ClassUsingDifferentClassesWithSameName.class,
                                                     "ClassUsingDifferentClassesWithSameName.expected.txt", true);
  }

  @Test
  public void should_generate_assertion_for_annotated_methods() throws IOException {
    converter = new ClassToClassDescriptionConverter(new AnnotationConfiguration(GenerateAssertion.class));
    assertionGenerator.generateCustomAssertionFor(converter.convertToClassDescription(AnnotatedClass.class));
    generationPathHandler.assertGeneratedAssertClass(AnnotatedClass.class, "AnnotatedClassAssert.expected.txt", true);
  }

  @Test
  public void should_generate_assertion_for_methods_annotated_with_GenerateAssertion_by_default() throws IOException {
    assertionGenerator.generateCustomAssertionFor(converter.convertToClassDescription(AnnotatedClass.class));
    generationPathHandler.assertGeneratedAssertClass(AnnotatedClass.class, "AnnotatedClassAssert.expected.txt", true);
  }

  @Test
  public void should_generate_assertion_for_annotated_class() throws IOException {
    converter = new ClassToClassDescriptionConverter(new AnnotationConfiguration(AutoValue.class));
    assertionGenerator.generateCustomAssertionFor(converter.convertToClassDescription(AutoValueAnnotatedClass.class));
    generationPathHandler.assertGeneratedAssertClass(AutoValueAnnotatedClass.class, "AutoValueAnnotatedClassAssert.expected.txt", true);
  }

  @Test @Ignore
  public void should_generate_assertion_for_generic_class() throws IOException {
    assertionGenerator.generateCustomAssertionFor(converter.convertToClassDescription(MultipleGenerics.class));
    generationPathHandler.assertGeneratedAssertClass(MultipleGenerics.class, "MultipleGenericsClassAssert.expected.txt", true);
  }

  private String expectedContentFromTemplate(NestedClass nestedClass, String fileTemplate) throws IOException {
    String template = contentOf(generationPathHandler.getResourcesDir().resolve(fileTemplate).toFile(), defaultCharset());
    String content = replace(template,
                             "${nestedClass}Assert",
                             StringUtils.remove(nestedClass.classNameWithOuterClass, '.') + "Assert");
    content = replace(content,
                      "${nestedClass}",
                      nestedClass.classNameWithOuterClass);
    return content;
  }

  @SuppressWarnings("WeakerAccess") class MyClassLoader extends ClassLoader {
    public MyClassLoader(ClassLoader parent) {
      super(parent);
    }
  }

  private String generateThrowsClause(Class<?> exception, String property, boolean booleanType) {
    String getter = (booleanType ? "is" : "get") + Character.toUpperCase(property.charAt(0)) + property.substring(1);
    return "   * @throws " + exception.getName() + " if actual." + getter + "() throws one." + LINE_SEPARATOR;
  }

}
