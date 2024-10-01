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

import org.assertj.assertions.generator.data.cars.Car;
import org.assertj.assertions.generator.description.converter.ClassToClassDescriptionConverter;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

public class AssertionGeneratorOverrideTemplateTest {

  private BaseAssertionGenerator assertionGenerator;
  private ClassToClassDescriptionConverter converter;

  @Rule
  public final GenerationPathHandler genHandle = new GenerationPathHandler(
          Paths.get("src/test/resources"));

  @Before
  public void before() throws IOException {
    assertionGenerator = new BaseAssertionGenerator();
    assertionGenerator.setDirectoryWhereAssertionFilesAreGenerated(genHandle.getRoot());
    converter = new ClassToClassDescriptionConverter();
  }

  @Test(expected = NullPointerException.class)
  public void should_fail_if_custom_template_is_null() {
    assertionGenerator.register(null);
  }

  @Test(expected = NullPointerException.class)
  public void should_fail_if_custom_template_content_is_null() {
    assertionGenerator.register(new Template(Template.Type.ABSTRACT_ASSERT_CLASS, (File) null));
  }

  @Test(expected = RuntimeException.class)
  public void should_fail_if_custom_template_content_cant_be_read() {
    assertionGenerator.register(new Template(Template.Type.ABSTRACT_ASSERT_CLASS, new File("not_existing.template")));
  }

  @Test
  public void should_generate_assertion_with_custom_template() throws IOException {
    assertionGenerator.register(new Template(Template.Type.HAS_FOR_WHOLE_NUMBER,
                                             new File("customtemplates" + File.separator,
                                                      "custom_has_assertion_template_for_whole_number.txt")));

    assertionGenerator.generateCustomAssertionFor(converter.convertToClassDescription(Car.class));
    File expectedFile = genHandle.getResourcesDir().resolve("CarAssert.expected.txt").toAbsolutePath().toFile();
    File actualFile = genHandle.fileGeneratedFor(Car.class);
    // compile it!
    genHandle.compileGeneratedFilesFor(Car.class);

    assertThat(actualFile).hasSameContentAs(expectedFile);
  }

}
