package org.fest.assertions.generator;

import org.junit.Test;

import java.net.URL;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.fail;


public class TemplateFactoryTest {

  @Test
  public void should_throw_an_exception_when_directory_url() {

    URL templateLocation = this.getClass().getClassLoader().getResource(Template.TEMPLATES_DIR);
    try {
      new Template.Factory(Template.Type.HAS).from(templateLocation);
      fail("A directory url should throw an exception");
    } catch (RuntimeException e) {
      assertThat(e).hasMessageStartingWith("Failed to read template from");
    }
  }


  @Test
  public void should_create_template_from_url() {

    URL templateLocation = this.getClass().getClassLoader().getResource(Template.TEMPLATES_DIR + Template.Type.CUSTOM.defaultFileName);
    Template template = new Template.Factory(Template.Type.CUSTOM).from(templateLocation).create();
    assertThat(template.getContent()).isNotEmpty();
  }

  @Test
  public void should_create_template_from_classpath(){

    String templateLocation = Template.TEMPLATES_DIR + Template.Type.CUSTOM.defaultFileName;
    Template template = new Template.Factory(Template.Type.CUSTOM).fromClasspath(templateLocation).create();
    assertThat(template.getContent()).isNotEmpty();
  }


}
