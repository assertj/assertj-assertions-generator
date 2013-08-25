package org.assertj.assertions.generator;

import org.apache.commons.io.FileUtils;
import org.assertj.assertions.generator.data.Player;
import org.assertj.assertions.generator.description.converter.ClassToClassDescriptionConverter;
import org.assertj.assertions.generator.util.ClassUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.List;

import static org.assertj.assertions.generator.BaseAssertionGenerator.ASSERT_CLASS_FILE_SUFFIX;
import static org.assertj.assertions.generator.util.ClassUtil.collectClasses;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;


@RunWith(Theories.class)
public class AssertionGeneratorTest implements NestedClassesTest {

  private static final String TARGET_DIRECTORY = "target";
  private static final Logger logger = LoggerFactory.getLogger(AssertionGeneratorTest.class);
  private ClassToClassDescriptionConverter converter;
  private BaseAssertionGenerator customAssertionGenerator;

  @Before
  public void beforeEachTest() throws FileNotFoundException, IOException {
    converter = new ClassToClassDescriptionConverter();
    customAssertionGenerator = new BaseAssertionGenerator();
    customAssertionGenerator.setDirectoryWhereAssertionFilesAreGenerated(TARGET_DIRECTORY);
  }

  @Test
  public void should_generate_assertion_for_player_class() throws Exception {
    customAssertionGenerator.generateCustomAssertionFor(converter.convertToClassDescription(Player.class));
    assertThat(fileGeneratedFor(Player.class)).hasContentEqualTo(
            new File("src/test/resources/PlayerAssert.expected.txt"));
  }

  @Theory
  public void should_generate_assertion_for_nestedclass(NestedClass nestedClass) throws Exception {
    Class clazz = nestedClass.getNestedClass();
    customAssertionGenerator.generateCustomAssertionFor(converter.convertToClassDescription(clazz));
    assertThat(fileGeneratedFor(clazz)).hasContent(expectedContentFromTemplate(clazz));
  }

  @Test
  public void should_generate_assertion_for_classes_in_package() throws Exception {
    List<Class<?>> classes = collectClasses("org.assertj.assertions.generator.data");
    for (Class<?> clazz : classes) {
      assertThat(clazz.isAnonymousClass()).as("check that <" + clazz.getSimpleName() +"> is not anonymous").isFalse();
      assertThat(clazz.isLocalClass()).as("check that " + clazz.getSimpleName() +" is not local").isFalse();
      assertThat(Modifier.isPublic(clazz.getModifiers())).as("check that " + clazz.getSimpleName() +" is public").isTrue();
      logger.info("Generating assertions for {}", clazz.getName());
      File customAssertionFile = customAssertionGenerator.generateCustomAssertionFor(converter
          .convertToClassDescription(clazz));
      logger.info("Generated {} assertions file -> {}", clazz.getSimpleName(), customAssertionFile.getAbsolutePath());
    }
  }

  @Test
  public void should_generate_assertion_for_classes_in_package_using_provided_class_loader() throws Exception {
    ClassLoader customClassLoader = new MyClassLoader(Thread.currentThread().getContextClassLoader());
    List<Class<?>> classes = collectClasses(customClassLoader, "org.assertj.assertions.generator.data");
    for (Class<?> clazz : classes) {
      assertThat(clazz.isAnonymousClass()).as("check that " + clazz.getSimpleName() +" is not anonymous").isFalse();
      assertThat(clazz.isLocalClass()).as("check that " + clazz.getSimpleName() +" is not local").isFalse();
      assertThat(Modifier.isPublic(clazz.getModifiers())).as("check that " + clazz.getSimpleName() +" is public").isTrue();
      logger.info("Generating assertions for {}", clazz.getName());
      File customAssertionFile = customAssertionGenerator.generateCustomAssertionFor(converter
          .convertToClassDescription(clazz));
      logger.info("Generated {} assertions file -> {}", clazz.getSimpleName(), customAssertionFile.getAbsolutePath());
    }
  }

  @Test
  public void should_check_template_type() throws Exception {
    customAssertionGenerator.setHasAssertionTemplate(new Template(Template.Type.HAS, "template content"));
    try {
      customAssertionGenerator.setHasAssertionTemplate(new Template(Template.Type.IS, "template content"));
      failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
    } catch (IllegalArgumentException e) {
      assertThat(e).hasMessage("Expecting a Template type to be 'HAS' but was 'IS'");
    }

  }

  private static String expectedContentFromTemplate(Class<?> clazz) throws IOException {
    String template = FileUtils.readFileToString(new File("src/test/resources/NestedClassAssert.template.expected.txt"));
    String content = template.replace("${nestedClass}Assert", clazz.getSimpleName() + "Assert");
    content = content.replace("${nestedClass}", ClassUtil.getSimpleNameWithOuterClass(clazz));
    return content;
  }

  private static File fileGeneratedFor(Class<?> clazz) {
    String dirName = TARGET_DIRECTORY + File.separatorChar
        + clazz.getPackage().getName().replace('.', File.separatorChar);
    String generatedFileName = clazz.getSimpleName() + ASSERT_CLASS_FILE_SUFFIX;
    return new File(dirName, generatedFileName);
  }

  class MyClassLoader extends ClassLoader {
    public MyClassLoader(ClassLoader parent) {
      super(parent);
    }
  }

}
