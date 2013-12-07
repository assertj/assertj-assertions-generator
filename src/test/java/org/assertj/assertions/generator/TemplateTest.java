package org.assertj.assertions.generator;

import static org.assertj.assertions.generator.BaseAssertionGenerator.DEFAULT_HAS_ASSERTION_TEMPLATE;
import static org.assertj.assertions.generator.BaseAssertionGenerator.TEMPLATES_DIR;
import static org.assertj.assertions.generator.Template.Type.ASSERT_CLASS;
import static org.assertj.assertions.generator.Template.Type.HAS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.io.File;
import java.net.URL;

import org.junit.Test;

public class TemplateTest {

  @Test
  public void should_throw_an_exception_when_directory_url() {
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
