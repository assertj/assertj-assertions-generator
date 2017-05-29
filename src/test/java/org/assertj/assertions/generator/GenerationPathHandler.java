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
 * Copyright 2012-2017 the original author or authors.
 */
package org.assertj.assertions.generator;

import com.google.common.base.Joiner;
import com.google.testing.compile.Compilation;
import com.google.testing.compile.CompilationSubject;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;
import org.assertj.core.api.Assertions;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import javax.tools.JavaFileObject;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.assertj.assertions.generator.BaseAssertionGenerator.ABSTRACT_ASSERT_CLASS_PREFIX;
import static org.assertj.assertions.generator.BaseAssertionGenerator.ASSERT_CLASS_FILE_SUFFIX;
import static org.assertj.assertions.generator.util.ClassUtil.getSimpleNameWithOuterClassNotSeparatedByDots;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test {@link org.junit.Rule} used to generate temporary folders per test case so there is no concern of
 * tests interacting with each other. This utilizes the built-in {@link TemporaryFolder} to accomplish this and
 * contains generator specific methods.
 */
public class GenerationPathHandler extends TemporaryFolder {


  public static final Path DEFAULT_GENERATION_ROOT = Paths.get("target/generated-test-output");

  private final Compiler compiler;
  private Path resourcesDir;

  public GenerationPathHandler(final Class<?> owningClass, Path resourcesDir) {
    super(DEFAULT_GENERATION_ROOT.toFile());

    //noinspection ResultOfMethodCallIgnored
    DEFAULT_GENERATION_ROOT.toFile().mkdirs();

    this.resourcesDir = resourcesDir;

    final String currentClasspath = getClasspathFromClassloader(ClassLoader.getSystemClassLoader());
    compiler = Compiler.javac()
        .withOptions("-classpath", currentClasspath);
  }

  public Path getResourcesDir() {
    return resourcesDir;
  }

  @Override
  public Statement apply(final Statement base, final Description description) {

    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        before();

        try {
          base.evaluate();
          after();
        } catch (Exception e) {
          System.err.println("Failed working with folder: " + getRoot());
          throw new AssertionError(e);
        }
      }
    };
  }

  public BaseAssertionGenerator buildAssertionGenerator() throws IOException {
    BaseAssertionGenerator assertionGenerator = new BaseAssertionGenerator();
    assertionGenerator.setDirectoryWhereAssertionFilesAreGenerated(getRoot());
    return assertionGenerator;
  }

  public Path packagePathFor(Class<?> clazz) {
    return getRoot().toPath()
        .resolve(clazz.getPackage().getName().replace('.', File.separatorChar));
  }

  public File fileGeneratedFor(Class<?> clazz) {
    String generatedFileName = getSimpleNameWithOuterClassNotSeparatedByDots(clazz) + ASSERT_CLASS_FILE_SUFFIX;
    return packagePathFor(clazz)
        .resolve(generatedFileName)
        .toFile();
  }

  public File abstractFileGeneratedFor(Class<?> clazz) {
    String generatedFileName = ABSTRACT_ASSERT_CLASS_PREFIX
        + getSimpleNameWithOuterClassNotSeparatedByDots(clazz)
        + ASSERT_CLASS_FILE_SUFFIX;
    return packagePathFor(clazz)
        .resolve(generatedFileName)
        .toFile();
  }

  public void compileGeneratedFiles(Iterable<? extends File> files) {
    List<JavaFileObject> jfiles = new ArrayList<>();
    for (File file: files) {
      try {
        final URL url = file.toURI().toURL();
        jfiles.add(JavaFileObjects.forResource(url));
      } catch (MalformedURLException mfue) {
        Assertions.failBecauseExceptionWasNotThrown(MalformedURLException.class);
      }
    }

    Compilation compilation = compiler.compile(jfiles);

    try {

      CompilationSubject.assertThat(compilation).succeeded();
    } catch (AssertionError ex) {
      throw new AssertionError("Error with compilation. JAVAC options:\n" + compiler.options(), ex);
    }
  }

  public void compileGeneratedFiles(File... files) {
    compileGeneratedFiles(Arrays.asList(files));
  }

  public void compileGeneratedFiles(Class<?>... classes) {
    List<File> files = new ArrayList<>(classes.length);
    for (Class<?> clazz: classes) {
      files.add(fileGeneratedFor(clazz));

      // Handle abstract files, too!
      File abstractFile = abstractFileGeneratedFor(clazz);
      if (abstractFile.exists()) {
        files.add(abstractFile);
      }
    }

    compileGeneratedFiles(files);
  }

  public void assertGeneratedAssertClass(Class<?> clazz, String expectedAssertFile, final boolean compileGenerated) throws IOException {
    File expectedFile = resourcesDir.resolve(expectedAssertFile).toAbsolutePath().toFile();
    File actualFile = fileGeneratedFor(clazz);
    assertThat(actualFile).hasSameContentAs(expectedFile);

    // compile it!
    if (compileGenerated) {
      compileGeneratedFiles(clazz);
    }
  }

  public void assertAbstractGeneratedAssertClass(Class<?> clazz, String expectedAssertFile) {
    File expectedFile = getResourcesDir().resolve(expectedAssertFile).toAbsolutePath().toFile();
    assertThat(abstractFileGeneratedFor(clazz)).hasSameContentAs(expectedFile);
  }

  /**
   * Returns the current classpaths of the given classloader including its parents.
   *
   * @throws IllegalArgumentException if the given classloader had classpaths which we could not
   *     determine or use for compilation.
   */
  private static String getClasspathFromClassloader(ClassLoader currentClassloader) {
    ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();

    // Add all URLClassloaders in the hirearchy till the system classloader.
    List<URLClassLoader> classloaders = new ArrayList<>();
    while(true) {
      if (currentClassloader instanceof URLClassLoader) {
        // We only know how to extract classpaths from URLClassloaders.
        classloaders.add((URLClassLoader) currentClassloader);
      } else {
        throw new IllegalArgumentException("Classpath for compilation could not be extracted "
            + "since given classloader is not an instance of URLClassloader");
      }
      if (currentClassloader == systemClassLoader) {
        break;
      }
      currentClassloader = currentClassloader.getParent();
    }

    Set<String> classpaths = new LinkedHashSet<>();
    for (URLClassLoader classLoader : classloaders) {
      for (URL url : classLoader.getURLs()) {
        if (url.getProtocol().equals("file")) {
          classpaths.add(url.getPath());
        } else {
          throw new IllegalArgumentException("Given classloader consists of classpaths which are "
              + "unsupported for compilation.");
        }
      }
    }

    return Joiner.on(File.pathSeparatorChar).join(classpaths);
  }
}
