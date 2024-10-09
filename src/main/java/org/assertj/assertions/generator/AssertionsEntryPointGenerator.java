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
 * Copyright 2012-2024 the original author or authors.
 */
package org.assertj.assertions.generator;

import org.assertj.assertions.generator.description.ClassDescription;

import java.io.File;
import java.io.IOException;
import java.util.Set;

public interface AssertionsEntryPointGenerator {

  /**
   * Returns the assertions entry point class content for the given
   * {@link org.assertj.assertions.generator.description.ClassDescription} set.
   * <p>
   * The idea is to generate an equivalent of assertj-core Assertions class to give easy access to all generated
   * assertions. With {@link AssertionsEntryPointType} parameter one can generate standard, BDD or Soft assertions entry
   * point class (default is {@link AssertionsEntryPointType#STANDARD}).
   * <p>
   * You can define the package of the generated entry point class, if null the common base package of the given
   * classes, will be used, i.e if some classe are in a.b.c and others in a.b.c.d, then entry point class will be in
   * a.b.c.
   * 
   * @param classDescriptionSet the set of ClassDescription we want to generate an entry point for.
   * @param assertionsEntryPointType the valueType of entry point class to generate
   * @param entryPointClassPackage the package of the generated entry point class
   * @return the assertions entry point class content
   */
  String generateAssertionsEntryPointClassContentFor(Set<ClassDescription> classDescriptionSet,
                                                     AssertionsEntryPointType assertionsEntryPointType,
                                                     String entryPointClassPackage);

  /**
   * Same as {@link #generateAssertionsEntryPointClassContentFor(Set, AssertionsEntryPointType, String)} but in addition
   * create the corresponding java class file.
   * 
   * @param classDescriptionSet class descriptions
   * @param assertionsEntryPointType entry point type
   * @param entryPointClassPackage entry point class package name
   * @throws java.io.IOException if entry point file can't be created.
   * @return the generated file
   */
  File generateAssertionsEntryPointClassFor(Set<ClassDescription> classDescriptionSet,
                                            AssertionsEntryPointType assertionsEntryPointType,
                                            String entryPointClassPackage) throws IOException;

}