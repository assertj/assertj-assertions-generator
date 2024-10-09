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
 * Copyright 2012-2024 the original author or authors.
 */
package org.assertj.assertions.generator;

import com.google.common.base.Optional;
import com.google.common.reflect.TypeToken;
import org.assertj.assertions.generator.data.Movie;
import org.assertj.assertions.generator.data.Name;
import org.assertj.assertions.generator.data.TreeEnum;
import org.assertj.assertions.generator.data.art.ArtWork;
import org.assertj.assertions.generator.data.lotr.Race;
import org.assertj.assertions.generator.data.lotr.Ring;
import org.assertj.assertions.generator.data.lotr.TolkienCharacter;
import org.assertj.assertions.generator.data.nba.Player;
import org.assertj.assertions.generator.data.nba.team.Team;
import org.assertj.assertions.generator.description.ClassDescription;
import org.assertj.assertions.generator.description.converter.ClassToClassDescriptionConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;

import static com.google.common.collect.Sets.newLinkedHashSet;
import static org.assertj.assertions.generator.AssertionsEntryPointType.AUTO_CLOSEABLE_BDD_SOFT;
import static org.assertj.assertions.generator.AssertionsEntryPointType.AUTO_CLOSEABLE_SOFT;
import static org.assertj.assertions.generator.AssertionsEntryPointType.BDD;
import static org.assertj.assertions.generator.AssertionsEntryPointType.BDD_SOFT;
import static org.assertj.assertions.generator.AssertionsEntryPointType.JUNIT_BDD_SOFT;
import static org.assertj.assertions.generator.AssertionsEntryPointType.JUNIT_SOFT;
import static org.assertj.assertions.generator.AssertionsEntryPointType.SOFT;
import static org.assertj.assertions.generator.AssertionsEntryPointType.STANDARD;
import static org.assertj.assertions.generator.util.ClassUtil.collectClasses;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.contentOf;

class AssertionsEntryPointGeneratorTest {

  @TempDir
  private Path tempDir;

  private BaseAssertionGenerator generator;

  @BeforeEach
  void before() throws IOException {
    generator = new BaseAssertionGenerator();
    generator.setDirectoryWhereAssertionFilesAreGenerated(tempDir.toFile());
  }

  @Test
  void should_generate_standard_assertions_entry_point_class_file() throws Exception {
    // GIVEN : classes we want to have entry point assertions for
    Set<ClassDescription> classDescriptionSet = getClassDescriptionsOf(Ring.class, Race.class, ArtWork.class,
                                                                       Name.class, Player.class, Movie.class,
                                                                       TolkienCharacter.class, TreeEnum.class,
                                                                       Movie.PublicCategory.class, Optional.class);
    // WHEN
    File assertionsEntryPointFile = generator.generateAssertionsEntryPointClassFor(classDescriptionSet, STANDARD, null);
    // THEN
    String expectedContent = readExpectedContentFromFile("Assertions.expected.txt");
    assertThat(assertionsEntryPointFile).as("check entry point class content").hasContent(expectedContent);

    // TODO compile with genHandle.compileGeneratedFiles();
  }

  @Test
  void should_generate_correctly_standard_assertions_entry_point_class_for_classes_with_same_name()
                                                                                                           throws Exception {
    // GIVEN : classes we want to have entry point assertions for
    Set<ClassDescription> classDescriptionSet = getClassDescriptionsOf(Team.class,
                                                                       org.assertj.assertions.generator.data.Team.class);
    // WHEN
    String assertionsEntryPointContent = generator.generateAssertionsEntryPointClassContentFor(classDescriptionSet,
                                                                                               STANDARD, "org");
    // THEN
    String expectedContent = readExpectedContentFromFile("AssertionsForClassesWithSameName.expected.txt");
    assertThat(assertionsEntryPointContent).isEqualTo(expectedContent);
  }

  @Test
  void should_generate_assertion_entry_point_class_file_with_custom_package() throws Exception {
    // GIVEN : classes we want to have entry point assertions for
    Set<ClassDescription> classDescriptionSet = getClassDescriptionsOf(Ring.class, Race.class, ArtWork.class,
                                                                       Name.class, Player.class, Movie.class,
                                                                       TolkienCharacter.class, TreeEnum.class,
                                                                       Movie.PublicCategory.class);
    // WHEN
    File assertionsEntryPointFile = generator.generateAssertionsEntryPointClassFor(classDescriptionSet,
                                                                                   STANDARD, "my.custom.package");
    // THEN
    String expectedContent = readExpectedContentFromFile("AssertionsWithCustomPackage.expected.txt");
    assertThat(assertionsEntryPointFile).as("check entry point class content")
                                        .hasContent(expectedContent)
                                        .hasParent(tempDir.resolve("my/custom/package").toFile());
  }

  @Test
  void should_generate_bdd_assertion_entry_point_class_file() throws Exception {
    // GIVEN : classes we want to have entry point assertions for
    Set<ClassDescription> classDescriptionSet = getClassDescriptionsOf(Ring.class, Race.class, ArtWork.class,
                                                                       Name.class, Player.class, Movie.class,
                                                                       TolkienCharacter.class, TreeEnum.class,
                                                                       Movie.PublicCategory.class);
    // WHEN
    File assertionsEntryPointFile = generator.generateAssertionsEntryPointClassFor(classDescriptionSet, BDD, null);
    // THEN
    String expectedContent = readExpectedContentFromFile("BddAssertions.expected.txt");
    assertThat(assertionsEntryPointFile).as("check BDD entry point class content").hasContent(expectedContent);
  }

  @Test
  void should_generate_soft_assertions_entry_point_class_file() throws Exception {
    // GIVEN : classes we want to have entry point assertions for
    Set<ClassDescription> classDescriptionSet = getClassDescriptionsOf(Ring.class, Race.class, ArtWork.class,
                                                                       Name.class, Player.class, Movie.class,
                                                                       TolkienCharacter.class, TreeEnum.class,
                                                                       Movie.PublicCategory.class);
    // WHEN
    File assertionsEntryPointFile = generator.generateAssertionsEntryPointClassFor(classDescriptionSet, SOFT, null);
    // THEN
    String expectedContent = readExpectedContentFromFile("SoftAssertions.expected.txt");
    assertThat(assertionsEntryPointFile).as("check soft assertions entry point class content")
                                        .hasContent(expectedContent);
  }

  @Test
  void should_generate_junit_soft_assertions_entry_point_class_file() throws Exception {
    // GIVEN : classes we want to have entry point assertions for
    Set<ClassDescription> classDescriptionSet = getClassDescriptionsOf(Ring.class, Race.class, ArtWork.class,
                                                                       Name.class, Player.class, Movie.class,
                                                                       TolkienCharacter.class, TreeEnum.class,
                                                                       Movie.PublicCategory.class);
    // WHEN
    File assertionsEntryPointFile = generator.generateAssertionsEntryPointClassFor(classDescriptionSet, JUNIT_SOFT,
                                                                                   null);
    // THEN
    String expectedContent = readExpectedContentFromFile("JUnitSoftAssertions.expected.txt");
    assertThat(assertionsEntryPointFile).as("check JUnit soft assertions entry point class content")
                                        .hasContent(expectedContent)
                                        .hasName("JUnitSoftAssertions.java");
  }

  @Test
  void should_generate_bdd_soft_assertions_entry_point_class_file() throws Exception {
    // GIVEN : classes we want to have entry point assertions for
    Set<ClassDescription> classDescriptionSet = getClassDescriptionsOf(Ring.class, Race.class, ArtWork.class,
                                                                       Name.class, Player.class, Movie.class,
                                                                       TolkienCharacter.class, TreeEnum.class,
                                                                       Movie.PublicCategory.class);
    // WHEN
    File assertionsEntryPointFile = generator.generateAssertionsEntryPointClassFor(classDescriptionSet, BDD_SOFT,
                                                                                   null);
    // THEN
    String expectedContent = readExpectedContentFromFile("BDDSoftAssertions.expected.txt");
    assertThat(assertionsEntryPointFile).as("check BDD soft assertions entry point class content")
                                        .hasContent(expectedContent);
  }

  @Test
  void should_generate_junit_bdd_soft_assertions_entry_point_class_file() throws Exception {
    // GIVEN : classes we want to have entry point assertions for
    Set<ClassDescription> classDescriptionSet = getClassDescriptionsOf(Ring.class, Race.class, ArtWork.class,
                                                                       Name.class, Player.class, Movie.class,
                                                                       TolkienCharacter.class, TreeEnum.class,
                                                                       Movie.PublicCategory.class);
    // WHEN
    File assertionsEntryPointFile = generator.generateAssertionsEntryPointClassFor(classDescriptionSet, JUNIT_BDD_SOFT,
                                                                                   null);
    // THEN
    String expectedContent = readExpectedContentFromFile("JUnitBDDSoftAssertions.expected.txt");
    assertThat(assertionsEntryPointFile).as("check JUnit BDD soft assertions entry point class content")
                                        .hasContent(expectedContent);
  }

  @Test
  void should_generate_auto_closeable_soft_assertions_entry_point_class_file() throws Exception {
    // GIVEN : classes we want to have entry point assertions for
    Set<ClassDescription> classDescriptionSet = getClassDescriptionsOf(Ring.class, Race.class, ArtWork.class,
                                                                       Name.class, Player.class, Movie.class,
                                                                       TolkienCharacter.class, TreeEnum.class,
                                                                       Movie.PublicCategory.class);
    // WHEN
    File assertionsEntryPointFile = generator.generateAssertionsEntryPointClassFor(classDescriptionSet,
                                                                                   AUTO_CLOSEABLE_SOFT,
                                                                                   null);
    // THEN
    String expectedContent = readExpectedContentFromFile("AutoCloseableSoftAssertions.expected.txt");
    assertThat(assertionsEntryPointFile).as("check auto closeable soft assertions entry point class content")
                                        .hasContent(expectedContent);
  }

  @Test
  void should_generate_auto_closeable_bdd_soft_assertions_entry_point_class_file() throws Exception {
    // GIVEN : classes we want to have entry point assertions for
    Set<ClassDescription> classDescriptionSet = getClassDescriptionsOf(Ring.class, Race.class, ArtWork.class,
                                                                       Name.class, Player.class, Movie.class,
                                                                       TolkienCharacter.class, TreeEnum.class,
                                                                       Movie.PublicCategory.class);
    // WHEN
    File assertionsEntryPointFile = generator.generateAssertionsEntryPointClassFor(classDescriptionSet,
                                                                                   AUTO_CLOSEABLE_BDD_SOFT,
                                                                                   null);
    // THEN
    String expectedContent = readExpectedContentFromFile("AutoCloseableBDDSoftAssertions.expected.txt");
    assertThat(assertionsEntryPointFile).as("check auto closeable BDD soft assertions entry point class content")
                                        .hasContent(expectedContent);
  }

  @Test
  void should_generate_an_assertions_entry_point_class_file_that_matches_given_class_name() throws Exception {
    // GIVEN : custom entry point class template changing the class name.
    Set<ClassDescription> classDescriptionSet = getClassDescriptionsOf(Ring.class);
    generator.register(new Template(Template.Type.ASSERTIONS_ENTRY_POINT_CLASS,
                                    new File("customtemplates" + File.separator,
                                             "my_assertion_entry_point_class.txt")));
    // WHEN
    File assertionsEntryPointFile = generator.generateAssertionsEntryPointClassFor(classDescriptionSet, STANDARD, null);

    // THEN
    String expectedContent = readExpectedContentFromFile("MyAssertions.expected.txt");
    assertThat(assertionsEntryPointFile).as("check custom assertions entry point class")
                                        .hasContent(expectedContent)
                                        .hasName("MyAssertions.java");
  }

  @Test
  void should_return_null_assertion_entry_point_file_if_no_classes_description_are_given() throws Exception {
    // GIVEN no ClassDescription
    Set<ClassDescription> classDescriptionSet = newLinkedHashSet();
    // THEN generated entry points file are null
    for (AssertionsEntryPointType assertionsEntryPointType : AssertionsEntryPointType.values()) {
      assertThat(generator.generateAssertionsEntryPointClassFor(classDescriptionSet, assertionsEntryPointType,
                                                                null)).isNull();
    }
  }

  @Test
  void should_return_empty_assertion_entry_point_class_content_if_no_classes_description_are_given()
                                                                                                            throws Exception {
    // GIVEN no ClassDescription
    Set<ClassDescription> emptySet = newLinkedHashSet();
    // THEN generated entry points content are empty
    for (AssertionsEntryPointType assertionsEntryPointType : AssertionsEntryPointType.values()) {
      assertThat(generator.generateAssertionsEntryPointClassContentFor(emptySet, assertionsEntryPointType,
                                                                       null)).isEmpty();
    }
  }

  @Test
  void should_return_null_assertion_entry_point_file_if_null_classes_description_are_given() throws Exception {
    // GIVEN no ClassDescription
    // THEN generated entry points file are null
    for (AssertionsEntryPointType assertionsEntryPointType : AssertionsEntryPointType.values()) {
      assertThat(generator.generateAssertionsEntryPointClassFor(null, assertionsEntryPointType, null)).isNull();
    }
  }

  @Test
  void should_return_empty_assertion_entry_point_class_if_null_classes_description_are_given() throws Exception {
    // GIVEN no ClassDescription
    // THEN generated entry points file are null
    for (AssertionsEntryPointType assertionsEntryPointType : AssertionsEntryPointType.values()) {
      assertThat(generator.generateAssertionsEntryPointClassContentFor(null, assertionsEntryPointType, null)).isEmpty();
    }
  }

  @Test
  void should_not_generate_assertion_entry_point_for_non_public_class_in_package() throws Exception {
    // GIVEN package including a package private class
    Set<ClassDescription> classDescriptions = getClassDescriptionsOf(collectClasses("org.assertj.assertions.generator.data"));
    // WHEN
    String assertionsEntryPointContent = generator.generateAssertionsEntryPointClassContentFor(classDescriptions,
                                                                                               STANDARD,
                                                                                               "org.assertj.assertions.generator.data");
    // THEN
    assertThat(assertionsEntryPointContent).doesNotContain("PackagePrivate");
  }

  @Test
  void should_not_generate_assertion_entry_point_for_non_public_class() throws Exception {
    // GIVEN package including a package private class
    Set<ClassDescription> classDescriptions = getClassDescriptionsOf(collectClasses("org.assertj.assertions.generator.data.inner.PackagePrivate"));
    // WHEN
    String assertionsEntryPointContent = generator.generateAssertionsEntryPointClassContentFor(classDescriptions,
                                                                                               STANDARD,
                                                                                               "org.assertj.assertions.generator.data");
    // THEN
    assertThat(assertionsEntryPointContent).doesNotContain("PackagePrivate");
  }

  private Set<ClassDescription> getClassDescriptionsOf(Class<?>... classes) {
    Set<ClassDescription> classDescriptionSet = new LinkedHashSet<>(classes.length);
    ClassToClassDescriptionConverter converter = new ClassToClassDescriptionConverter();
    for (Class<?> clazz : classes) {
      classDescriptionSet.add(converter.convertToClassDescription(TypeToken.of(clazz)));
    }
    return classDescriptionSet;
  }

  private Set<ClassDescription> getClassDescriptionsOf(Set<TypeToken<?>> typeTokens) {
    Set<ClassDescription> classDescriptionSet = new LinkedHashSet<>(typeTokens.size());
    ClassToClassDescriptionConverter converter = new ClassToClassDescriptionConverter();
    for (TypeToken<?> typeToken : typeTokens) {
      classDescriptionSet.add(converter.convertToClassDescription(typeToken));
    }
    return classDescriptionSet;
  }

  private String readExpectedContentFromFile(String fileWithExpectedContent) {
    return contentOf(new File("src/test/resources", fileWithExpectedContent));
  }

}
