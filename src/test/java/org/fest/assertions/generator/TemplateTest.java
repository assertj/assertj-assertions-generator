package org.fest.assertions.generator;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.fail;
import static org.fest.assertions.generator.BaseAssertionGenerator.DEFAULT_HAS_ASSERTION_TEMPLATE;
import static org.fest.assertions.generator.BaseAssertionGenerator.TEMPLATES_DIR;

import java.io.File;
import java.net.URL;

import org.junit.Test;

public class TemplateTest {

  @Test
  public void should_throw_an_exception_when_directory_url() {
    URL templateURL = getClass().getClassLoader().getResource(TEMPLATES_DIR);
    try {
      new Template(templateURL);
      fail("A directory url should throw an exception");
    } catch (RuntimeException e) {
      assertThat(e).hasMessageStartingWith("Failed to read template from");
    }
  }

  @Test
  public void should_create_template_from_url() {
    URL templateURL = getClass().getClassLoader().getResource(TEMPLATES_DIR + DEFAULT_HAS_ASSERTION_TEMPLATE);
    Template template = new Template(templateURL);
    assertThat(template.getContent()).isNotEmpty();
  }

  @Test
  public void should_create_template_from_file() {
    File templateFile = new File(TEMPLATES_DIR, DEFAULT_HAS_ASSERTION_TEMPLATE);
    Template template = new Template(templateFile);
    assertThat(template.getContent()).isNotEmpty();
  }

}
