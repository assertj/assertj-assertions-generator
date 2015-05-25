package org.assertj.assertions.generator;

import static org.assertj.assertions.generator.AssertionGeneratorTest.assertGeneratedAssertClass;

import java.io.File;
import java.io.IOException;

import org.assertj.assertions.generator.data.cars.Car;
import org.assertj.assertions.generator.description.converter.ClassToClassDescriptionConverter;
import org.junit.Before;
import org.junit.Test;

public class AssertionGeneratorOverrideTemplateTest {
  private BaseAssertionGenerator assertionGenerator;
  private ClassToClassDescriptionConverter converter;

  @Before
  public void before() throws IOException {
    assertionGenerator = new BaseAssertionGenerator();
    assertionGenerator.setDirectoryWhereAssertionFilesAreGenerated("target");
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
    assertionGenerator.register(new Template(Template.Type.HAS_FOR_PRIMITIVE,
                                             new File("customtemplates" + File.separator,
                                                      "custom_has_assertion_template_for_primitive.txt")));

    assertionGenerator.generateCustomAssertionFor(converter.convertToClassDescription(Car.class));
    assertGeneratedAssertClass(Car.class, "CarAssert.expected.txt");
  }
}
