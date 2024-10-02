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
 * Copyright 2012-2021 the original author or authors.
 */
package org.assertj.assertions.generator;

import com.google.common.base.Joiner;
import com.google.testing.compile.Compilation;
import com.google.testing.compile.CompilationSubject;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;
import org.assertj.assertions.generator.description.converter.ClassToClassDescriptionConverter;

import javax.tools.JavaFileObject;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class GenerationHandler {

  private static final ClassToClassDescriptionConverter CONVERTER = new ClassToClassDescriptionConverter();

  private final Path root;
  private final Path resourcesDir;
  private final Compiler compiler;

  GenerationHandler(Path root, Path resourcesDir) {
    this.root = root;
    this.resourcesDir = resourcesDir;
    this.compiler = Compiler.javac()
                            .withOptions("-classpath", getClasspath(ClassLoader.getSystemClassLoader()));
  }

  Path getResourcesDir() {
    return resourcesDir;
  }

  private Path packagePathFor(Class<?> clazz) {
    return pathFromRoot(clazz.getPackage().getName().replace('.', File.separatorChar));
  }

  File fileGeneratedFor(Class<?> clazz) {
    String generatedFileName = CONVERTER.convertToClassDescription(clazz).getAssertClassFilename();
    return packagePathFor(clazz).resolve(generatedFileName).toFile();
  }

  File fileGeneratedFor(Class<?> clazz, String generatedAssertionPackage) {
    String generatedFileName = CONVERTER.convertToClassDescription(clazz).getAssertClassFilename();
    return pathFromRoot(generatedAssertionPackage).resolve(generatedFileName).toFile();
  }

  File abstractFileGeneratedFor(Class<?> clazz) {
    String generatedFileName = CONVERTER.convertToClassDescription(clazz).getAbstractAssertClassFilename();
    return packagePathFor(clazz).resolve(generatedFileName).toFile();
  }

  File abstractFileGeneratedFor(Class<?> clazz, String generatedAssertionPackage) {
    String generatedFileName = CONVERTER.convertToClassDescription(clazz).getAbstractAssertClassFilename();
    return pathFromRoot(generatedAssertionPackage).resolve(generatedFileName).toFile();
  }

  private Path pathFromRoot(String generatedAssertionPackage) {
    return root.resolve(generatedAssertionPackage.replace('.', File.separatorChar));
  }

  void compileGeneratedFiles(Iterable<? extends File> files) {
    List<JavaFileObject> javaFileObjects = toJavaFileObjects(files);
    Compilation compilation = compiler.compile(javaFileObjects);
    try {
      CompilationSubject.assertThat(compilation).succeeded();
    } catch (AssertionError ex) {
      throw new AssertionError("Error with compilation. JAVAC options:\n" + compiler.options(), ex);
    }
  }

  private List<JavaFileObject> toJavaFileObjects(Iterable<? extends File> files) {
    List<JavaFileObject> javaFileObjects = new ArrayList<>();
    for (File file : files) {
      try {
        final URL url = file.toURI().toURL();
        javaFileObjects.add(JavaFileObjects.forResource(url));
      } catch (MalformedURLException e) {
        throw new RuntimeException(e);
      }
    }
    return javaFileObjects;
  }

  void compileGeneratedFilesFor(Class<?>... classes) {
    List<File> files = new ArrayList<>(classes.length);
    for (Class<?> clazz : classes) {
      files.add(fileGeneratedFor(clazz));
      // Handle abstract files, too!
      File abstractFile = abstractFileGeneratedFor(clazz);
      if (abstractFile.exists()) {
        files.add(abstractFile);
      }
    }
    compileGeneratedFiles(files);
  }

  void compileGeneratedFilesFor(String generatedAssertionPackage, Class<?>... classes) {
    List<File> files = new ArrayList<>(classes.length);
    for (Class<?> clazz : classes) {
      files.add(fileGeneratedFor(clazz, generatedAssertionPackage));
      // Handle abstract files, too!
      File abstractFile = abstractFileGeneratedFor(clazz, generatedAssertionPackage);
      if (abstractFile.exists()) {
        files.add(abstractFile);
      }
    }
    compileGeneratedFiles(files);
  }

  /**
   * Returns the current classpaths of the given classloader including its parents.
   *
   * @throws IllegalArgumentException if the given classloader had classpaths which we could not
   *     determine or use for compilation.
   */
  private static String getClasspath(ClassLoader currentClassloader) {
    ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();

    // Add all URLClassloaders in the hierarchy till the system classloader.
    List<URLClassLoader> classloaders = new ArrayList<>();
    while (true) {
      // We only know how to extract classpaths from URLClassloaders.
      if (currentClassloader instanceof URLClassLoader) classloaders.add((URLClassLoader) currentClassloader);
      else throw new IllegalArgumentException("Classpath for compilation could not be extracted as classloader is not a URLClassloader");

      if (currentClassloader == systemClassLoader) break;
      else currentClassloader = currentClassloader.getParent();
    }

    Set<String> classpaths = new LinkedHashSet<>();
    for (URLClassLoader classLoader : classloaders) {
      for (URL url : classLoader.getURLs()) {
        if (url.getProtocol().equals("file")) classpaths.add(url.getPath());
        else throw new IllegalArgumentException("Given classloader consists of classpaths which are unsupported for compilation.");
      }
    }

    return Joiner.on(File.pathSeparatorChar).join(classpaths);
  }

}
