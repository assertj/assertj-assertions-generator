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

import static org.assertj.assertions.generator.BaseAssertionGenerator.TEMPLATES_DIR;
import static org.assertj.assertions.generator.DefaultTemplateRegistryProducer.DEFAULT_HAS_ASSERTION_TEMPLATE;
import static org.assertj.assertions.generator.Template.Type.ASSERT_CLASS;
import static org.assertj.assertions.generator.Template.Type.HAS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.io.File;
import java.net.URL;

import org.junit.Test;

public class TemplateTest {

  @SuppressWarnings("unused")
  @Test
  public void should_throw_an_exception_when_url_is_a_directory() {
    URL templateURL = getClass().getClassLoader().getResource(TEMPLATES_DIR);
    try {
      new Template(ASSERT_CLASS, templateURL);
      fail("A directory url should throw an exception");
    } catch (RuntimeException e) {
      assertThat(e).hasMessageStartingWith("Failed to read template from an URL which is not a file");
    }
  }

  @Test
  public void should_create_template_from_url() {
    URL templateURL = getClass().getClassLoader().getResource(TEMPLATES_DIR + DEFAULT_HAS_ASSERTION_TEMPLATE);
    Template template = new Template(ASSERT_CLASS, templateURL);
    assertThat(template.getContent()).isNotEmpty();
  }

  @Test
  public void should_create_template_from_file() {
    File templateFile = new File(TEMPLATES_DIR, DEFAULT_HAS_ASSERTION_TEMPLATE);
    Template template = new Template(HAS, templateFile);
    assertThat(template.getContent()).isNotEmpty();
  }

}
