package org.fest.assertions.generator;

import static org.fest.assertions.generator.util.ClassUtil.collectClasses;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AssertionGeneratorTest {

  
  private static final Logger logger = LoggerFactory.getLogger(AssertionGeneratorTest.class);
  
  @Test
  public void generateCustomAssertionForClass() throws FileNotFoundException, IOException {
    AssertionGenerator customAssertionGenerator = new AssertionGenerator("src/main/resources/templates/");
    customAssertionGenerator.setDirectoryWhereAssertionFilesAreGenerated("target");
    customAssertionGenerator.generateCustomAssertion(ArtWork.class);
  }

  @Test
  public void generateCustomAssertionForPackage() throws FileNotFoundException, IOException, ClassNotFoundException {
    List<Class<?>> classes = collectClasses("org.fest.assertions.generator");
    AssertionGenerator assertionGenerator = new AssertionGenerator("src/main/resources/templates/");
    assertionGenerator.setDirectoryWhereAssertionFilesAreGenerated("target");
    assertionGenerator.generateCustomAssertion(ArtWork.class);
    for (Class<?> clazz : classes) {
      logger.info("Generating assertions for {}", clazz.getName());
      File playerAssertJavaFile = assertionGenerator.generateCustomAssertion(clazz);
      logger.info("Generated {} assertions file -> {}", clazz.getSimpleName(), playerAssertJavaFile.getAbsolutePath());
    }
    
    
  }
}
