package org.assertj.assertions.generator;

import java.io.File;
import java.io.IOException;

import org.assertj.assertions.generator.description.ClassDescription;


public interface AssertionGenerator {

  /**
   * Builds and returns the the custom assertion java file for the given {@link ClassDescription}.
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
   *     String errorMessage = format("Expected Race's name to be <%s> but was <%s>", name, actual.getName());
   *     // check
   *     if (!actual.getName().equals(name)) { throw new AssertionError(errorMessage); }
   * 
   *     // return the current assertion for method chaining
   *     return this;
   *   }
   * 
   *   public RaceAssert isImmortal() {
   *     // check that actual Race we want to make assertions on is not null.
   *     isNotNull();
   *     // we overrides the default error message with a more explicit one
   *     String errorMessage = format("Expected actual Race to be immortal but was not.", actual);
   *     // check
   *     if (!actual.isImmortal()) throw new AssertionError(errorMessage);
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

}