package org.assertj.assertions.generator;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.assertj.assertions.generator.AssertionsEntryPointType;
import org.assertj.assertions.generator.description.ClassDescription;

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
   * @param assertionsEntryPointType the type of entry point class to generate
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
   * @throws java.io.IOException if ebtry point file can't be created.
   */
  File generateAssertionsEntryPointClassFor(Set<ClassDescription> classDescriptionSet,
                                            AssertionsEntryPointType assertionsEntryPointType,
                                            String entryPointClassPackage) throws IOException;

}