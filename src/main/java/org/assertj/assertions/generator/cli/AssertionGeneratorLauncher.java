/**
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 * Copyright 2012-2014 the original author or authors.
 */
package org.assertj.assertions.generator.cli;

import static com.google.common.collect.Sets.newLinkedHashSet;
import static org.assertj.assertions.generator.util.ClassUtil.collectClasses;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.assertj.assertions.generator.BaseAssertionGenerator;
import org.assertj.assertions.generator.description.ClassDescription;
import org.assertj.assertions.generator.description.converter.ClassToClassDescriptionConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AssertionGeneratorLauncher {

  private static final Logger logger = LoggerFactory.getLogger(AssertionGeneratorLauncher.class);
  private static ClassToClassDescriptionConverter classDescriptionConverter = new ClassToClassDescriptionConverter();

  public static void main(String[] args) throws IOException, ClassNotFoundException {
    Options options = new Options();
    options.addOption("H", "hierarchical", false, "Generate a hierarchy of assertions that follows the hierarchy of classes to assert");
    options.addOption("h", "help", false, "Print this help message");
    CommandLineParser parser = new BasicParser();
    
    try {
      CommandLine line = parser.parse(options, args);

      if (line.hasOption('h')) {
        printHelp(options);
        return;
      }

      Set<Class<?>> classes = collectClasses(line.getArgs());

      if (line.hasOption('H')) {
        generateHierarchicalAssertions(classes);
      } else {
        generateFlatAssertions(classes);
      }
    } catch (ParseException e) {
      System.err.println("Error trying to parse command-line arguments: " + e.getMessage());
      printHelp(options);
    }
    
  }

  private static void printHelp(Options options) {
    HelpFormatter help = new HelpFormatter();
    final String cmdLine = "java " + AssertionGeneratorLauncher.class.getCanonicalName() + " [--help] [--hierarchical] <classes/packages>";
    help.printHelp(cmdLine, "Generate AssertJ-style assertions for the specified classes", options, "The list of classes can either be package names (which includes all packges in the class) or fully-qualified class names.");
  }
  
  private static void generateHierarchicalAssertions(Set<Class<?>> classes) throws IOException {
    // Create a hashset of the classes for efficient lookup.
    Set<Class<?>> classSet = newLinkedHashSet(classes);
    logger.info("Generating hierarchical assertions for classes {}", classes);
    BaseAssertionGenerator customAssertionGenerator = new BaseAssertionGenerator();
    
    for (Class<?> clazz : classes) {
      logger.info("Generating hierarchical assertions for class : {}", clazz.getName());
      File[] customAssertionFiles = customAssertionGenerator.generateHierarchicalCustomAssertionFor(toClassDescription(clazz), classSet);
      logger.info("Generated {} hierarchical assertions files -> {}, {}", clazz.getSimpleName(),
                  customAssertionFiles[0].getAbsolutePath(),
                  customAssertionFiles[1].getAbsolutePath());
    }
  }
  
  private static void generateFlatAssertions(Set<Class<?>> classes) throws IOException {
    logger.info("Generating assertions for classes {}", classes);
    BaseAssertionGenerator customAssertionGenerator = new BaseAssertionGenerator();
    
    for (Class<?> clazz : classes) {
      logger.info("Generating assertions for class : {}", clazz.getName());
      File customAssertionFile = customAssertionGenerator.generateCustomAssertionFor(toClassDescription(clazz));
      logger.info("Generated {} assertions file -> {}", clazz.getSimpleName(),
                  customAssertionFile.getAbsolutePath());
    }
  }

  private static ClassDescription toClassDescription(Class<?> clazz) {
    return classDescriptionConverter.convertToClassDescription(clazz);
  }
}
