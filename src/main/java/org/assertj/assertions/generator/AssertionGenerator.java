package org.assertj.assertions.generator;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.assertj.assertions.generator.description.ClassDescription;


public interface AssertionGenerator {

  /**
   * Builds and returns the custom assertion java file for the given {@link ClassDescription}.
   * <p>
   * Let's say we have the {@link ClassDescription} corresponding to :
   *
   * <pre>
   * public class Race {
   *
   *   private final String name;
   *   private final boolean immortal;
   *
   *   public Race(String name, boolean immortal) {
   *     this.name = name;
   *     this.immortal = immortal;
   *   }
   *
   *   public String getName() {
   *     return name;
   *   }
   *
   *   public boolean isImmortal() {
   *     return immortal;
   *   }
   * }
   * </pre>
   *
   * We will generate assertions specific to <code>Race</code> in <code>RaceAssert</code> class, like :
   *
   * <pre>
   * import static java.lang.String.format;
   *
   * import org.assertj.core.api.AbstractAssert;
   * import org.assertj.core.api.Assertions;
   *
   * public class RaceAssert extends AbstractAssert<RaceAssert, Race> {
   *
   *   public RaceAssert(Race actual) {
   *     super(actual, RaceAssert.class);
   *   }
   *
   *   public static RaceAssert assertThat(Race actual) {
   *     return new RaceAssert(actual);
   *   }
   *
   *   public RaceAssert hasName(String name) {
   *     // check that actual Race we want to make assertions on is not null.
   *     isNotNull();
   *     // we overrides the default error message with a more explicit one
   *     String assertjErrorMessage = format("Expected Race's name to be <%s> but was <%s>", name, actual.getName());
   *     // check
   *     if (!actual.getName().equals(name)) { throw new AssertionError(assertjErrorMessage); }
   *
   *     // return the current assertion for method chaining
   *     return this;
   *   }
   *
   *   public RaceAssert isImmortal() {
   *     // check that actual Race we want to make assertions on is not null.
   *     isNotNull();
   *     // we overrides the default error message with a more explicit one
   *     String assertjErrorMessage = format("Expected actual Race to be immortal but was not.", actual);
   *     // check
   *     if (!actual.isImmortal()) throw new AssertionError(assertjErrorMessage);
   *     // return the current assertion for method chaining
   *     return this;
   *   }
   * </pre>
   *
   * @param classDescription the {@link ClassDescription} used to generate tha assertions class.
   * @return the custom assertion java file for the given class
   * @throws IOException if something went wrong when creating the assertion file.
   */

  File generateCustomAssertionFor(ClassDescription classDescription) throws IOException;

  /**
   * Builds and returns the custom assertion java file content for the given {@link ClassDescription}.
   * <p>
   * Let's say we have the {@link ClassDescription} corresponding to :
   *
   * <pre>
   * public class Race {
   *
   *   private final String name;
   *   private final boolean immortal;
   *
   *   public Race(String name, boolean immortal) {
   *     this.name = name;
   *     this.immortal = immortal;
   *   }
   *
   *   public String getName() {
   *     return name;
   *   }
   *
   *   public boolean isImmortal() {
   *     return immortal;
   *   }
   * }
   * </pre>
   *
   * We will generate assertions specific to <code>Race</code> in <code>RaceAssert</code> class, like :
   *
   * <pre>
   * import static java.lang.String.format;
   *
   * import org.assertj.core.api.AbstractAssert;
   * import org.assertj.core.api.Assertions;
   *
   * public class RaceAssert extends AbstractAssert<RaceAssert, Race> {
   *
   *   public RaceAssert(Race actual) {
   *     super(actual, RaceAssert.class);
   *   }
   *
   *   public static RaceAssert assertThat(Race actual) {
   *     return new RaceAssert(actual);
   *   }
   *
   *   public RaceAssert hasName(String name) {
   *     // check that actual Race we want to make assertions on is not null.
   *     isNotNull();
   *     // we overrides the default error message with a more explicit one
   *     String assertjErrorMessage = format("Expected Race's name to be <%s> but was <%s>", name, actual.getName());
   *     // check
   *     if (!actual.getName().equals(name)) { throw new AssertionError(assertjErrorMessage); }
   *
   *     // return the current assertion for method chaining
   *     return this;
   *   }
   *
   *   public RaceAssert isImmortal() {
   *     // check that actual Race we want to make assertions on is not null.
   *     isNotNull();
   *     // we overrides the default error message with a more explicit one
   *     String assertjErrorMessage = format("Expected actual Race to be immortal but was not.", actual);
   *     // check
   *     if (!actual.isImmortal()) throw new AssertionError(assertjErrorMessage);
   *     // return the current assertion for method chaining
   *     return this;
   *   }
   * </pre>
   *
   * @param classDescription the {@link ClassDescription} used to generate tha assertions class.
   * @return the custom assertion content.
   * @throws IOException if something went wrong when creating the assertion content.
   */
  String generateCustomAssertionContentFor(ClassDescription classDescription) throws IOException;

  /**
   * Returns the assertions entry point class content for the given {@link ClassDescription} set.
   * <p>
   * The idea is to generate an equivalent of assertj-core Assertions class to give easy access to all generated
   * assertions.
   * <p>
   * You can define the package of the generated entry point class, if null the common base package of the given classes,
   * will be used, i.e if some classe are in a.b.c and others in a.b.c.d, then entry point class will be in a.b.c.
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
   * Returns the assertions entry point class file for the given {@link ClassDescription} set.
   * <p>
   * The idea is to generate an equivalent of assertj-core Assertions class to give easy access to all generated
   * assertions.
   * <p>
   * You can define the package of the generated entry point class, if null the common base package of the given classes,
   * will be used, i.e if some classe are in a.b.c and others in a.b.c.d, then entry point class will be in a.b.c.
   *
   * @param classDescriptionSet the set of ClassDescription we want to generate an entry point for.
   * @param assertionsEntryPointType the type of entry point class to generate
   * @param entryPointClassPackage the package of the generated entry point class
   * @return the assertions entry point class content
   * @throws IOException if ebtry point file can't be created.
   */
  File generateAssertionsEntryPointClassFor(Set<ClassDescription> classDescriptionSet,
                                            AssertionsEntryPointType assertionsEntryPointType,
                                            String entryPointClassPackage) throws IOException;

  /**
   * Returns the content of the assertions entry point class for the given {@link ClassDescription} set.
   * <p>
   * The idea is to generate an equivalent of assertj-core Assertions class to give easy access to all generated
   * assertions.
   * <p>
   * The entry point class package is the common base package of the given classes, if some classe are in a.b.c package
   * and others in a.b.c.d, then entry point class will be in a.b.c.
   *
   * @param classDescriptionSet the set of ClassDescription we want to generate an entry point for.
   * @return the assertions entry point class content
   */
  String generateStandardAssertionsEntryPointClassContentFor(Set<ClassDescription> classDescriptionSet);

  /**
   * Same as {@link #generateStandardAssertionsEntryPointClassFor(java.util.Set)} but you define the package of the
   * generated entry point class.
   *
   * @param classDescriptionSet the set of ClassDescription we want to generate an entry point for.
   * @param entryPointClassPackage the package of the generated entry point class
   * @return the assertions entry point class content
   * @throws IOException if ebtry point file can't be created.
   */
  String generateStandardAssertionsEntryPointClassContentFor(Set<ClassDescription> classDescriptionSet, String entryPointClassPackage);
  File generateStandardAssertionsEntryPointClassFor(Set<ClassDescription> classDescriptionSet, String entryPointClassPackage) throws IOException;

  /**
   * Returns the assertions entry point class file for the given {@link ClassDescription} set.
   * <p>
   * The idea is to generate an equivalent of assertj-core Assertions class to give easy access to all generated
   * assertions.
   *
   * @param classDescriptionSet the set of ClassDescription we want to generate entry point.
   * @return the assertions entry point class file.
   * @throws IOException if file can't be created.
   */
  File generateStandardAssertionsEntryPointClassFor(Set<ClassDescription> classDescriptionSet) throws IOException;

  /**
   * Returns the content of the BDD assertions entry point class for the given {@link ClassDescription} set.
   * <p>
   * The idea is to generate an equivalent of assertj-core BDD Assertions class that gives easy access to all AssertJ Core
   * assertions.
   *
   * @param classDescriptionSet the set of ClassDescription whose assertion calls will be in the generated entry point
   *                            class content.
   * @return the BDD assertions entry point class content
   */
  String generateBddAssertionsEntryPointContentFor(Set<ClassDescription> classDescriptionSet);

  /**
   * Returns the BDD assertions entry point class file for the given {@link ClassDescription} set.
   * <p>
   * The idea is to generate an equivalent of assertj-core BDD Assertions class that gives easy access to all AssertJ Core
   * assertions.
   *
   * @param classDescriptionSet the set of ClassDescription whose assertion calls will be in the generated entry point
   *                            class.
   * @return the BDD assertions entry point class file.
   * @throws IOException if assertions entry point class file can't be created.
   */
  File generateBddAssertionsEntryPointFor(Set<ClassDescription> classDescriptionSet) throws IOException;

  /**
   * Returns the content of the soft assertions entry point class for the given {@link ClassDescription} set.
   * <p>
   * The idea is to generate an equivalent of assertj-core SoftAssertions class for generated assertions.
   *
   * @param classDescriptionSet the set of ClassDescription we want to generate entry point.
   * @return the assertions entry point class content
   */
  String generateSoftAssertionsEntryPointClassContentFor(Set<ClassDescription> classDescriptionSet);

  /**
   * Returns the soft assertions entry point class file for the given {@link ClassDescription} set.
   * <p>
   * The idea is to generate an equivalent of assertj-core SoftAssertions class for generated assertions.
   *
   * @param classDescriptionSet the set of ClassDescription we want to generate entry point.
   * @return the soft assertions entry point class file.
   * @throws IOException if file can't be created.
   */
  File generateSoftAssertionsEntryPointClassFor(Set<ClassDescription> classDescriptionSet) throws IOException;

}