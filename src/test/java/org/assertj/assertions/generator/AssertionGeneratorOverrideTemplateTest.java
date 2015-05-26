package org.assertj.assertions.generator;

import org.assertj.assertions.generator.data.cars.Car;
import org.assertj.assertions.generator.description.converter.ClassToClassDescriptionConverter;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.assertj.assertions.generator.AssertionGeneratorTest.assertGeneratedAssertClass;

/**
 * Created by Alexander Bischof on 26.05.15.
 */
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
    public void should_fail_if_custom_template_content_is_not_found() {
        assertionGenerator.register(new Template(Template.Type.ABSTRACT_ASSERT_CLASS, new File("not_existing.template")));
    }

    @Test
    public void should_pass_if_custom_template_is_valid() throws IOException {
        assertionGenerator.register(new Template(Template.Type.HAS_FOR_PRIMITIVE,
                                                 new File("customtemplates" + File.separator,
                                                          "custom_has_assertion_template_for_primitive.txt")));

        assertionGenerator.generateCustomAssertionFor(converter.convertToClassDescription(Car.class));
        assertGeneratedAssertClass(Car.class, "CarAssert.expected.txt");
    }
}
