/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * 
 * Copyright @2010-2011 the original author or authors.
 */
package org.assertj.assertions.generator.cli;

import static org.assertj.assertions.generator.util.ClassUtil.collectClasses;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.assertj.assertions.generator.BaseAssertionGenerator;
import org.assertj.assertions.generator.description.ClassDescription;
import org.assertj.assertions.generator.description.converter.ClassToClassDescriptionConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AssertionGeneratorLauncher {

  private static final Logger logger = LoggerFactory.getLogger(AssertionGeneratorLauncher.class);
  private static ClassToClassDescriptionConverter classDescriptionConverter = new ClassToClassDescriptionConverter();

  public static void main(String[] classesOrPackagesNames) throws IOException, ClassNotFoundException {
    List<Class<?>> classes = collectClasses(classesOrPackagesNames);
    logger.info("Generating assertions for classes {}", classes);
    BaseAssertionGenerator customAssertionGenerator = new BaseAssertionGenerator();
    for (Class<?> clazz : classes) {
      logger.info("Generating assertions for class : {}", clazz.getName());
      File customAssertionFile = customAssertionGenerator.generateCustomAssertionFor(toClassDescription(clazz));
      logger.info("Generated {} assertions file -> {}", clazz.getSimpleName(), customAssertionFile.getAbsolutePath());
    }
  }

  private static ClassDescription toClassDescription(Class<?> clazz) {
    return classDescriptionConverter.convertToClassDescription(clazz);
  }
}
