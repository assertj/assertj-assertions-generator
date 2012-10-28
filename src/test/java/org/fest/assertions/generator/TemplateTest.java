package org.fest.assertions.generator;

import org.junit.Test;

import java.net.URL;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.fail;


public class TemplateTest {

  @Test
  public void should_throw_an_exception_for_directory_url() {

    URL templateLocation = this.getClass().getClassLoader().getResource(Template.TEMPLATES_DIR);
    try {
      Template.from(templateLocation);
      fail("A directory url should throw an exception");
    } catch (RuntimeException e) {
      assertThat(e).hasMessageStartingWith("Failed to read template from");
    }
  }


  @Test
  public void should_create_template_from_url() {

    URL templateLocation = this.getClass().getClassLoader().getResource(Template.TEMPLATES_DIR+Template.DEFAULT_HAS_ASSERTION);
    Template template = Template.from(templateLocation);
    assertThat(template.getContent()).isNotEmpty();
  }


}
