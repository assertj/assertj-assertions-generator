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
 * Copyright 2012-2015 the original author or authors.
 */
package org.assertj.assertions.generator;

import com.google.common.reflect.TypeToken;
import org.assertj.assertions.generator.description.ClassDescription;

import java.io.File;
import java.io.IOException;
import java.util.Set;


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
   * import org.assertj.core.api.AbstractObjectAssert;
   * import org.assertj.core.api.Assertions;
   *
   * public class RaceAssert extends AbstractObjectAssert<RaceAssert, Race> {
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
   * @param classDescription the {@link ClassDescription} used to generate the assertions class.
   * @return the custom assertion java file for the given class
   * @throws IOException if something went wrong when creating the assertion file.
   */

  File generateCustomAssertionFor(ClassDescription classDescription) throws IOException;

  /**
   * Generates hierarchical assertion classes for the class represented by the given classDescription. Two classes are
   * generated:
   * 
   * <ul>
   * <li>An abstract base class containing the assertion definitions, eg
   * 
   * <pre>public abstract class AbstractRaceAssert&lt;S extends AbstractRaceAssert, T extends Race&gt;
   * </pre>
   * 
   * </li>
   * <li>A concrete final class which inherits from the abstract base class, eg
   * 
   * <pre>
   * public final class RaceAssert extends AbstractRaceAssert&lt;RaceAssert, Race&gt;
   * </pre>
   * 
   * </li>
   * </ul>
   * 
   * If the <code>classDescription</code> has a supertype with a known assertion class, then the generated abstract
   * assertion class will inherit from the superclass' abstract assertion class. Otherwise, it will inherit from
   * <code>AbstractObjectAssert&lt;S, T&gt;</code>.
   * 
   * @param classDescription the {@link ClassDescription} used to generate the assertions class.
   * @param allClasses set of all classes that we are currently generating assertions for. Used to find superclass
   *          assertions.
   * @return two-element File array with the first containing the file for the abstract base assertion and the second
   *         containing the file for the concrete final assertion.
   * @throws IOException if something went wrong when creating the assertion files.
   */
  File[] generateHierarchicalCustomAssertionFor(ClassDescription classDescription, Set<TypeToken<?>> allClasses) throws IOException;

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
   * import org.assertj.core.api.AbstractObjectAssert;
   * import org.assertj.core.api.Assertions;
   *
   * public class RaceAssert extends AbstractObjectAssert<RaceAssert, Race> {
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
   * @param classDescription the {@link ClassDescription} used to generate the assertions class.
   * @return the custom assertion content.
   * @throws RuntimeException
   *             if something went wrong when creating the assertion content.
   */
  String generateCustomAssertionContentFor(ClassDescription classDescription) throws IOException;

  /**
   * Generates hierarchical assertion classes for the class represented by the
   * given classDescription. Two classes are generated:
   * 
   * <ul>
   * <li>An abstract base class containing the assertion definitions, eg
   * 
   * <pre>public abstract class AbstractRaceAssert&lt;S extends AbstractRaceAssert, T extends Race&gt;
   * </pre>
   * 
   * </li>
   * <li>A concrete final class which inherits from the abstract base class, eg
   * 
   * <pre>
   * public final class RaceAssert extends AbstractRaceAssert&lt;RaceAssert, Race&gt;
   * </pre>
   * 
   * </li>
   * </ul>
   * 
   * If the <code>classDescription</code> has a supertype with a known assertion class, then the generated abstract
   * assertion class will inherit from the superclass' abstract assertion class. Otherwise, it will inherit from
   * <code>AbstractObjectAssert&lt;S, T&gt;</code>.
   * 
   * @param classDescription
   *            the {@link ClassDescription} used to generate the assertions
   *            class.
   * @param allClasses
   *            set of all classes that we are currently generating assertions
   *            for. Used to find superclass assertions.
   * @return two-element String array with the first containing the content of
   *         the abstract base assertion and the second containing the
   *         concrete final assertion.
   * @throws RuntimeException
   *             if something went wrong when creating the assertion content.
   */
  String[] generateHierarchicalCustomAssertionContentFor(ClassDescription classDescription, Set<TypeToken<?>> allClasses);

    /**
     * Registers a template in the internal TemplateRegistry so that customers can override default templates.
     *
     * @param template
     * @throws java.lang.NullPointerException if template is null
     * @throws java.lang.NullPointerException if template.getContent is null
     */
  void register(Template template);
}