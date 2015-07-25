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
 * Copyright 2012-2015 the original author or authors.
 */
package org.assertj.assertions.generator;

import static com.google.common.collect.Sets.newLinkedHashSet;
import static org.apache.commons.io.FileUtils.readFileToString;
import static org.assertj.assertions.generator.AssertionsEntryPointType.BDD;
import static org.assertj.assertions.generator.AssertionsEntryPointType.JUNIT_SOFT;
import static org.assertj.assertions.generator.AssertionsEntryPointType.SOFT;
import static org.assertj.assertions.generator.AssertionsEntryPointType.STANDARD;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.assertj.assertions.generator.data.ArtWork;
import org.assertj.assertions.generator.data.Movie;
import org.assertj.assertions.generator.data.Name;
import org.assertj.assertions.generator.data.TreeEnum;
import org.assertj.assertions.generator.data.lotr.Race;
import org.assertj.assertions.generator.data.lotr.Ring;
import org.assertj.assertions.generator.data.lotr.TolkienCharacter;
import org.assertj.assertions.generator.data.nba.Player;
import org.assertj.assertions.generator.data.nba.Team;
import org.assertj.assertions.generator.description.ClassDescription;
import org.assertj.assertions.generator.description.converter.ClassToClassDescriptionConverter;

public class AssertionsEntryPointGeneratorTest {
  private static final String TARGET_DIRECTORY = "target";
  private AssertionsEntryPointGenerator generator;

  @Before
  public void beforeEachTest() throws IOException {
	generator = buildAssertionGenerator();
  }

  @Test
  public void should_generate_standard_assertions_entry_point_class_file() throws Exception {
	// GIVEN : classes we want to have entry point assertions for
	Set<ClassDescription> classDescriptionSet = getClassDescriptionsOf(Ring.class, Race.class, ArtWork.class,
	                                                                   Name.class, Player.class, Movie.class,
	                                                                   TolkienCharacter.class, TreeEnum.class,
	                                                                   Movie.PublicCategory.class);
	// WHEN
	File assertionsEntryPointFile = generator.generateAssertionsEntryPointClassFor(classDescriptionSet, STANDARD, null);
	// THEN
	String expectedContent = readFileToString(new File("src/test/resources/Assertions.expected.txt"));
	assertThat(assertionsEntryPointFile).as("check entry point class content").hasContent(expectedContent);
  }

  @Test
  public void should_generate_correctly_standard_assertions_entry_point_class_for_classes_with_same_name()
	  throws Exception {
	// GIVEN : classes we want to have entry point assertions for
	Set<ClassDescription> classDescriptionSet = getClassDescriptionsOf(Team.class,
	                                                                   org.assertj.assertions.generator.data.Team.class);
	// WHEN
	String assertionsEntryPointContent = generator.generateAssertionsEntryPointClassContentFor(classDescriptionSet,
	                                                                                           STANDARD, "org");
	// THEN
	String expectedContent = readFileToString(new File(
	                                                   "src/test/resources/AssertionsForClassesWithSameName.expected.txt"));
	assertThat(assertionsEntryPointContent).isEqualTo(expectedContent);
  }

  @Test
  public void should_generate_assertion_entry_point_class_file_with_custom_package() throws Exception {
	// GIVEN : classes we want to have entry point assertions for
	Set<ClassDescription> classDescriptionSet = getClassDescriptionsOf(Ring.class, Race.class, ArtWork.class,
	                                                                   Name.class, Player.class, Movie.class,
	                                                                   TolkienCharacter.class, TreeEnum.class,
	                                                                   Movie.PublicCategory.class);
	// WHEN
	File assertionsEntryPointFile = generator.generateAssertionsEntryPointClassFor(classDescriptionSet,
	                                                                               STANDARD, "my.custom.package");
	// THEN
	String expectedContent = readFileToString(new File("src/test/resources/AssertionsWithCustomPackage.expected.txt"));
	assertThat(assertionsEntryPointFile).as("check entry point class content")
	                                    .hasContent(expectedContent)
	                                    .hasParent("target/my/custom/package");
  }

  @Test
  public void should_generate_bdd_assertion_entry_point_class_file() throws Exception {
	// GIVEN : classes we want to have entry point assertions for
	Set<ClassDescription> classDescriptionSet = getClassDescriptionsOf(Ring.class, Race.class, ArtWork.class,
	                                                                   Name.class, Player.class, Movie.class,
	                                                                   TolkienCharacter.class, TreeEnum.class,
	                                                                   Movie.PublicCategory.class);
	// WHEN
	File assertionsEntryPointFile = generator.generateAssertionsEntryPointClassFor(classDescriptionSet, BDD, null);
	// THEN
	String expectedContent = readFileToString(new File("src/test/resources/BddAssertions.expected.txt"));
	assertThat(assertionsEntryPointFile).as("check BDD entry point class content").hasContent(expectedContent);
  }

  @Test
  public void should_generate_soft_assertions_entry_point_class_file() throws Exception {
	// GIVEN : classes we want to have entry point assertions for
	Set<ClassDescription> classDescriptionSet = getClassDescriptionsOf(Ring.class, Race.class, ArtWork.class,
	                                                                   Name.class, Player.class, Movie.class,
	                                                                   TolkienCharacter.class, TreeEnum.class,
	                                                                   Movie.PublicCategory.class);
	// WHEN
	File assertionsEntryPointFile = generator.generateAssertionsEntryPointClassFor(classDescriptionSet, SOFT, null);
	// THEN
	String expectedContent = readFileToString(new File("src/test/resources/SoftAssertions.expected.txt"));
	assertThat(assertionsEntryPointFile).as("check soft assertions entry point class content")
	                                    .hasContent(expectedContent);
  }

  @Test
  public void should_generate_junit_soft_assertions_entry_point_class_file() throws Exception {
	// GIVEN : classes we want to have entry point assertions for
	Set<ClassDescription> classDescriptionSet = getClassDescriptionsOf(Ring.class, Race.class, ArtWork.class,
	                                                                   Name.class, Player.class, Movie.class,
	                                                                   TolkienCharacter.class, TreeEnum.class,
	                                                                   Movie.PublicCategory.class);
	// WHEN
	File assertionsEntryPointFile = generator.generateAssertionsEntryPointClassFor(classDescriptionSet, JUNIT_SOFT,
	                                                                               null);
	// THEN
	String expectedContent = readFileToString(new File("src/test/resources/JUnitSoftAssertions.expected.txt"));
	assertThat(assertionsEntryPointFile).as("check JUnit soft assertions entry point class content")
	                                    .hasContent(expectedContent);
  }

  @Test
  public void should_return_null_assertion_entry_point_file_if_no_classes_description_are_given() throws Exception {
	// GIVEN no ClassDescription
	Set<ClassDescription> classDescriptionSet = newLinkedHashSet();
	// THEN generated entry points file are null
	for (AssertionsEntryPointType assertionsEntryPointType : AssertionsEntryPointType.values()) {
	  assertThat(generator.generateAssertionsEntryPointClassFor(classDescriptionSet, assertionsEntryPointType, null)).isNull();
	}
  }

  @Test
  public void should_return_empty_assertion_entry_point_class_if_no_classes_description_are_given() throws Exception {
	// GIVEN no ClassDescription
	Set<ClassDescription> emptySet = newLinkedHashSet();
	// THEN generated entry points content are empty
	for (AssertionsEntryPointType assertionsEntryPointType : AssertionsEntryPointType.values()) {
	  assertThat(generator.generateAssertionsEntryPointClassContentFor(emptySet, assertionsEntryPointType, null)).isEmpty();
	}
  }

  @Test
  public void should_return_null_assertion_entry_point_file_if_null_classes_description_are_given() throws Exception {
	// GIVEN no ClassDescription
	// THEN generated entry points file are null
	for (AssertionsEntryPointType assertionsEntryPointType : AssertionsEntryPointType.values()) {
	  assertThat(generator.generateAssertionsEntryPointClassFor(null, assertionsEntryPointType, null)).isNull();
	}
  }

  @Test
  public void should_return_empty_assertion_entry_point_class_if_null_classes_description_are_given() throws Exception {
	// GIVEN no ClassDescription
	// THEN generated entry points file are null
	for (AssertionsEntryPointType assertionsEntryPointType : AssertionsEntryPointType.values()) {
	  assertThat(generator.generateAssertionsEntryPointClassContentFor(null, assertionsEntryPointType, null)).isEmpty();
	}
  }

  private Set<ClassDescription> getClassDescriptionsOf(Class<?>... classes) {
	Set<ClassDescription> classDescriptionSet = new LinkedHashSet<ClassDescription>(classes.length);
	ClassToClassDescriptionConverter converter = new ClassToClassDescriptionConverter();
	for (Class<?> clazz : classes) {
	  classDescriptionSet.add(converter.convertToClassDescription(clazz));
	}
	return classDescriptionSet;
  }

  private AssertionsEntryPointGenerator buildAssertionGenerator() throws IOException {
	BaseAssertionGenerator assertionGenerator = new BaseAssertionGenerator();
	assertionGenerator.setDirectoryWhereAssertionFilesAreGenerated(TARGET_DIRECTORY);
	return assertionGenerator;
  }

}
