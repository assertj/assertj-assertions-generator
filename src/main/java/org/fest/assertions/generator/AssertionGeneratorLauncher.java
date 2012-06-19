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
package org.fest.assertions.generator;

import static org.fest.assertions.generator.util.ClassUtil.collectClasses;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AssertionGeneratorLauncher {

  private static final Logger logger = LoggerFactory.getLogger(AssertionGeneratorLauncher.class);
  
  public static void main(String[] classesOrPackagesNames) throws FileNotFoundException, IOException, ClassNotFoundException {
    List<Class<?>> classes = collectClasses(classesOrPackagesNames);
    AssertionGenerator customAssertionGenerator = new AssertionGenerator();
    for (Class<?> clazz : classes) {
      logger.info("Generating assertions for {}", clazz.getName());
      File playerAssertJavaFile = customAssertionGenerator.generateCustomAssertion(clazz);
      logger.info("Generated {} assertions file -> {}", clazz.getSimpleName(), playerAssertJavaFile.getAbsolutePath());
    }
  }
}
