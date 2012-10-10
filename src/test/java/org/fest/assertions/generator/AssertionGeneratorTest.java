package org.fest.assertions.generator;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.generator.util.ClassUtil.collectClasses;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
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
    assertThat(new File(TARGET_DIRECTORY, "PlayerAssert.java")).hasContentEqualTo(
        new File("src/test/resources/PlayerAssert.expected.txt"));
  }

  @Test
  public void should_generate_assertion_for_classes_in_package() throws Exception {
    List<Class<?>> classes = collectClasses("org.fest.assertions.generator.data");
    for (Class<?> clazz : classes) {
      logger.info("Generating assertions for {}", clazz.getName());
      File customAssertionFile = customAssertionGenerator.generateCustomAssertionFor(converter.convertToClassDescription(clazz));
      logger.info("Generated {} assertions file -> {}", clazz.getSimpleName(), customAssertionFile.getAbsolutePath());
    }
  }

  @Test
  public void should_generate_assertion_for_classes_in_package_using_provided_class_loader() throws Exception {
    ClassLoader customClassLoader = new MyClassLoader(Thread.currentThread().getContextClassLoader());
    List<Class<?>> classes = collectClasses(customClassLoader, "org.fest.assertions.generator.data");
    for (Class<?> clazz : classes) {
      logger.info("Generating assertions for {}", clazz.getName());
      File customAssertionFile = customAssertionGenerator.generateCustomAssertionFor(converter.convertToClassDescription(clazz));
      logger.info("Generated {} assertions file -> {}", clazz.getSimpleName(), customAssertionFile.getAbsolutePath());
    }
  }

  class MyClassLoader extends ClassLoader {
    public MyClassLoader(ClassLoader parent) {
      super(parent);
    }
  }

}
