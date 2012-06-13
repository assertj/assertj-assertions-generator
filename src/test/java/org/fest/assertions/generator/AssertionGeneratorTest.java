package org.fest.assertions.generator;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Test;

public class AssertionGeneratorTest {

  @Test
  public void test() throws FileNotFoundException, IOException {
    AssertionGenerator customAssertionGenerator = new AssertionGenerator("src/main/resources/templates/");
    customAssertionGenerator.setDirectoryWhereAssertionFilesAreGenerated("target");
    customAssertionGenerator.generateCustomAssertion(Player.class);
  }

}
