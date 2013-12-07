package org.assertj.assertions.generator;

import static com.google.common.collect.Lists.*;
import static com.google.common.collect.Sets.newLinkedHashSet;
import static java.lang.reflect.Modifier.isPublic;
import static org.apache.commons.io.FileUtils.readFileToString;
import static org.assertj.assertions.generator.BaseAssertionGenerator.ASSERT_CLASS_FILE_SUFFIX;
import static org.assertj.assertions.generator.util.ClassUtil.collectClasses;
import static org.assertj.core.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

import org.assertj.assertions.generator.data.ArtWork;
import org.assertj.assertions.generator.data.Movie;
import org.assertj.assertions.generator.data.Name;
import org.assertj.assertions.generator.data.Player;
import org.assertj.assertions.generator.data.TreeEnum;
import org.assertj.assertions.generator.data.lotr.Race;
import org.assertj.assertions.generator.data.lotr.Ring;
import org.assertj.assertions.generator.data.lotr.TolkienCharacter;
import org.assertj.assertions.generator.description.ClassDescription;
import org.assertj.assertions.generator.description.converter.ClassToClassDescriptionConverter;
import org.assertj.assertions.generator.util.ClassUtil;

@RunWith(Theories.class)
public class AssertionGeneratorTest implements NestedClassesTest, BeanWithExceptionsTest {
  private static final String LINE_SEPARATOR = System.getProperty("line.separator");
  private static final String TARGET_DIRECTORY = "target";
  private static final Logger logger = LoggerFactory.getLogger(AssertionGeneratorTest.class);
  private ClassToClassDescriptionConverter converter;
  private BaseAssertionGenerator customAssertionGenerator;

  @Before
  public void beforeEachTest() throws IOException {
    converter = new ClassToClassDescriptionConverter();
    customAssertionGenerator = new BaseAssertionGenerator();
    customAssertionGenerator.setDirectoryWhereAssertionFilesAreGenerated(TARGET_DIRECTORY);
  }

  @Test
  public void should_generate_assertion_for_player_class() throws Exception {
    customAssertionGenerator.generateCustomAssertionFor(converter.convertToClassDescription(Player.class));
    assertThat(fileGeneratedFor(Player.class)).hasContentEqualTo(
        new File("src/test/resources/PlayerAssert.expected" + ".txt").getAbsoluteFile());
  }

  @Theory
  public void should_generate_assertion_for_nestedclass(NestedClass nestedClass) throws Exception {
    Class clazz = nestedClass.getNestedClass();
    customAssertionGenerator.generateCustomAssertionFor(converter.convertToClassDescription(clazz));
    assertThat(fileGeneratedFor(clazz)).hasContent(expectedContentFromTemplate(clazz));
  }

  @Theory
  public void should_generate_assertion_for_property_with_exception(Class<?> beanClass) throws Exception {
    customAssertionGenerator.generateCustomAssertionFor(converter.convertToClassDescription(beanClass));
    String expectedContent = readFileToString(new File("src/test/resources/BeanWithOneException.expected.txt"));
    if (!BEAN_WITH_ONE_EXCEPTION.equals(beanClass)) {
      String importException = "import java.io.IOException;" + LINE_SEPARATOR;
      expectedContent = expectedContent.replace(importException, importException + "import java.sql.SQLException;"
          + LINE_SEPARATOR);

      expectedContent = expectedContent.replace(BEAN_WITH_ONE_EXCEPTION.getSimpleName(), beanClass.getSimpleName());
      expectedContent = expectedContent.replace(" throws IOException ", " throws IOException, SQLException ");

      GetterWithException[] getters = { STRING_1_EXCEPTION, BOOLEAN_1_EXCEPTION, ARRAY_1_EXCEPTION,
          ITERABLE_1_EXCEPTION };
      for (GetterWithException getter : getters) {
        String throwsClause = generateThrowsClause(IOException.class, getter.getPropertyName(), getter.isBooleanType());
        String replacement = throwsClause
            + generateThrowsClause(SQLException.class, getter.getPropertyName(), getter.isBooleanType());
        expectedContent = expectedContent.replace(throwsClause, replacement);
      }
    }
    assertThat(fileGeneratedFor(beanClass)).hasContent(expectedContent);
  }

  private String generateThrowsClause(Class<?> exception, String property, boolean booleanType) {
    String getter = (booleanType ? "is" : "get") + Character.toUpperCase(property.charAt(0)) + property.substring(1);
    return "   * @throws " + exception.getSimpleName() + " if actual." + getter + "() throws one." + LINE_SEPARATOR;
  }

  @Test
  public void should_generate_assertion_for_classes_in_package() throws Exception {
    List<Class<?>> classes = collectClasses("org.assertj.assertions.generator.data");
    for (Class<?> clazz : classes) {
      assertThat(clazz.isAnonymousClass()).as("check that <" + clazz.getSimpleName() + "> is not anonymous").isFalse();
      assertThat(clazz.isLocalClass()).as("check that " + clazz.getSimpleName() + " is not local").isFalse();
      assertThat(isPublic(clazz.getModifiers())).as("check that " + clazz.getSimpleName() + " is public").isTrue();
      logger.info("Generating assertions for {}", clazz.getName());
      final ClassDescription classDescription = converter.convertToClassDescription(clazz);
      File customAssertionFile = customAssertionGenerator.generateCustomAssertionFor(classDescription);
      logger.info("Generated {} assertions file -> {}", clazz.getSimpleName(), customAssertionFile.getAbsolutePath());
    }
  }

  @Test
  public void should_generate_assertion_for_classes_in_package_using_provided_class_loader() throws Exception {
    ClassLoader customClassLoader = new MyClassLoader(Thread.currentThread().getContextClassLoader());
    List<Class<?>> classes = collectClasses(customClassLoader, "org.assertj.assertions.generator.data");
    for (Class<?> clazz : classes) {
      assertThat(clazz.isAnonymousClass()).as("check that " + clazz.getSimpleName() + " is not anonymous").isFalse();
      assertThat(clazz.isLocalClass()).as("check that " + clazz.getSimpleName() + " is not local").isFalse();
      assertThat(isPublic(clazz.getModifiers())).as("check that " + clazz.getSimpleName() + " is public").isTrue();
      logger.info("Generating assertions for {}", clazz.getName());
      final ClassDescription classDescription = converter.convertToClassDescription(clazz);
      File customAssertionFile = customAssertionGenerator.generateCustomAssertionFor(classDescription);
      logger.info("Generated {} assertions file -> {}", clazz.getSimpleName(), customAssertionFile.getAbsolutePath());
    }
  }

  @Test
  public void should_generate_assertion_entry_point_class_file() throws Exception {
    // GIVEN : classes we want to have entry point assertions for
    List<Class<?>> classes = newArrayList(Ring.class, Race.class, ArtWork.class, Name.class, Player.class, Movie.class,
        TolkienCharacter.class, TreeEnum.class, Movie.PublicCategory.class);
    Set<ClassDescription> classDescriptionSet = new LinkedHashSet<ClassDescription>(classes.size());
    for (Class<?> clazz : classes) {
      classDescriptionSet.add(converter.convertToClassDescription(clazz));
    }
    // WHEN
    final File assertionsEntryPointFile = customAssertionGenerator.generateAssertionsEntryPointFor(classDescriptionSet);
    // THEN
    String expectedContent = readFileToString(new File("src/test/resources/Assertions.expected.txt"));
    assertThat(assertionsEntryPointFile).as("check entry point class content").hasContent(expectedContent);
  }

  @Test
  public void should_return_null_assertion_entry_point_file_if_no_classes_description_are_given() throws Exception {
    // GIVEN no ClassDescription
    Set<ClassDescription> classDescriptionSet = newLinkedHashSet();
    // WHEN
    final File assertionsEntryPointFile = customAssertionGenerator.generateAssertionsEntryPointFor(classDescriptionSet);
    // THEN
    assertThat(assertionsEntryPointFile).isNull();
  }

  @Test
  public void should_return_empty_assertion_entry_point_class_if_no_classes_description_are_given() throws Exception {
    // GIVEN no ClassDescription
    Set<ClassDescription> classDescriptionSet = newLinkedHashSet();
    // WHEN
    final String content = customAssertionGenerator.generateAssertionsEntryPointContentFor(classDescriptionSet);
    // THEN
    assertThat(content).isEmpty();
  }

  @Test
  public void should_check_template_type() throws Exception {
    customAssertionGenerator.setHasAssertionTemplate(new Template(Template.Type.HAS, "template content"));
    try {
      customAssertionGenerator.setHasAssertionTemplate(new Template(Template.Type.IS, "template content"));
      failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
    } catch (IllegalArgumentException e) {
      assertThat(e).hasMessage("Expecting a Template type to be 'HAS' but was 'IS'");
    }
  }

  private static String expectedContentFromTemplate(Class<?> clazz) throws IOException {
    String template = readFileToString(new File("src/test/resources/NestedClassAssert.template.expected.txt"));
    String content = template.replace("${nestedClass}Assert", clazz.getSimpleName() + "Assert");
    content = content.replace("${nestedClass}", ClassUtil.getSimpleNameWithOuterClass(clazz));
    return content;
  }

  private static File fileGeneratedFor(Class<?> clazz) {
    String dirName = TARGET_DIRECTORY + File.separatorChar
        + clazz.getPackage().getName().replace('.', File.separatorChar);
    String generatedFileName = clazz.getSimpleName() + ASSERT_CLASS_FILE_SUFFIX;
    return new File(dirName, generatedFileName);
  }

  class MyClassLoader extends ClassLoader {
    public MyClassLoader(ClassLoader parent) {
      super(parent);
    }
  }

}
