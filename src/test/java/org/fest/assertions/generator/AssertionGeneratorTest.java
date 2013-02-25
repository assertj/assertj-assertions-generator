package org.fest.assertions.generator;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.fest.assertions.generator.BaseAssertionGenerator.ASSERT_CLASS_SUFFIX;
import static org.fest.assertions.generator.util.ClassUtil.collectClasses;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.fest.assertions.generator.data.Player;
import org.fest.assertions.generator.description.converter.ClassToClassDescriptionConverter;

public class AssertionGeneratorTest {

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
  
  @Test
  public void should_generate_assertion_for_classes_in_package() throws Exception {
    List<Class<?>> classes = collectClasses("org.fest.assertions.generator.data");
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
  public void should_generate_assertion_for_classes_in_package_using_provided_class_loader() throws Exception {
    ClassLoader customClassLoader = new MyClassLoader(Thread.currentThread().getContextClassLoader());
    List<Class<?>> classes = collectClasses(customClassLoader, "org.fest.assertions.generator.data");
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

  private static File fileGeneratedFor(Class<?> clazz) {
    String dirName = TARGET_DIRECTORY + File.separatorChar
        + clazz.getPackage().getName().replace('.', File.separatorChar);
    String generatedFileName = clazz.getSimpleName() + ASSERT_CLASS_SUFFIX;
    return new File(dirName, generatedFileName);
  }

  class MyClassLoader extends ClassLoader {
    public MyClassLoader(ClassLoader parent) {
      super(parent);
    }
  }

}
