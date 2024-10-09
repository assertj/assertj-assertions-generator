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

import org.assertj.assertions.generator.data.cars.Car;
import org.assertj.assertions.generator.description.converter.ClassToClassDescriptionConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.assertions.generator.Template.Type.ABSTRACT_ASSERT_CLASS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThatRuntimeException;

class AssertionGeneratorOverrideTemplateTest {

  private BaseAssertionGenerator assertionGenerator;
  private ClassToClassDescriptionConverter converter;
  private GenerationHandler genHandle;

  @BeforeEach
  void before(@TempDir Path tempDir) throws IOException {
    assertionGenerator = new BaseAssertionGenerator();
    assertionGenerator.setDirectoryWhereAssertionFilesAreGenerated(tempDir.toFile());
    converter = new ClassToClassDescriptionConverter();
    genHandle = new GenerationHandler(tempDir, Paths.get("src/test/resources"));
  }

  @Test
  void should_fail_if_custom_template_is_null() {
    assertThatNullPointerException().isThrownBy(() -> assertionGenerator.register(null));
  }

  @Test
  void should_fail_if_custom_template_content_is_null() {
    assertThatNullPointerException().isThrownBy(() -> assertionGenerator.register(new Template(ABSTRACT_ASSERT_CLASS, (File) null)));
  }

  @Test
  void should_fail_if_custom_template_content_cant_be_read() {
    assertThatRuntimeException().isThrownBy(() -> assertionGenerator.register(new Template(ABSTRACT_ASSERT_CLASS, new File("not_existing.template"))));
  }

  @Test
  void should_generate_assertion_with_custom_template() throws IOException {
    assertionGenerator.register(new Template(Template.Type.HAS_FOR_WHOLE_NUMBER,
                                             new File("customtemplates" + File.separator,
                                                      "custom_has_assertion_template_for_whole_number.txt")));

    assertionGenerator.generateCustomAssertionFor(converter.convertToClassDescription(Car.class));
    File expectedFile = genHandle.getResourcesDir().resolve("CarAssert.expected.txt").toAbsolutePath().toFile();
    File actualFile = genHandle.fileGeneratedFor(Car.class);
    // compile it!
    genHandle.compileGeneratedFilesFor(Car.class);

    assertThat(actualFile).hasSameTextualContentAs(expectedFile);
  }

}
