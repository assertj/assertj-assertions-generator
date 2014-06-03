package org.assertj.assertions.generator.description;

import org.assertj.assertions.generator.NestedClassesTest;
import org.assertj.assertions.generator.data.Player;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;

@RunWith(Theories.class)
public class TypeNameTest implements NestedClassesTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();
  private TypeName typeName;

  @Test
  public void should_create_valid_typename_from_class() {
    typeName = new TypeName(Player.class);
    assertThat(typeName.getSimpleName()).isEqualTo("Player");
    assertThat(typeName.getSimpleNameWithOuterClass()).isEqualTo("Player");
    assertThat(typeName.getSimpleNameWithOuterClassNotSeparatedByDots()).isEqualTo("Player");
    assertThat(typeName.getPackageName()).isEqualTo("org.assertj.assertions.generator.data");
    assertThat(typeName.belongsToJavaLangPackage()).isFalse();
    assertThat(typeName.isPrimitive()).isFalse();
  }

  @Theory
  public void should_create_valid_typename_from_nestedclass(NestedClass nestedClass) {
    typeName = new TypeName(nestedClass.getNestedClass());
    assertThat(typeName.getSimpleName()).isEqualTo(nestedClass.getNestedClass().getSimpleName());
    assertThat(typeName.getSimpleNameWithOuterClass()).isEqualTo(nestedClass.getClassNameWithOuterClass());
    assertThat(typeName.getSimpleNameWithOuterClassNotSeparatedByDots()).isEqualTo(nestedClass.getClassNameWithOuterClassNotSeparatedBytDots());
    assertThat(typeName.getPackageName()).isEqualTo("org.assertj.assertions.generator.data");
    assertThat(typeName.belongsToJavaLangPackage()).isFalse();
    assertThat(typeName.isPrimitive()).isFalse();
  }

  @Test
  public void should_create_valid_typename() {
    typeName = new TypeName("Player", "org.assertj.assertions.generator.data");
    assertThat(typeName.getSimpleName()).isEqualTo("Player");
    assertThat(typeName.getSimpleNameWithOuterClass()).isEqualTo("Player");
    assertThat(typeName.getSimpleNameWithOuterClassNotSeparatedByDots()).isEqualTo("Player");
    assertThat(typeName.getPackageName()).isEqualTo("org.assertj.assertions.generator.data");
    assertThat(typeName.belongsToJavaLangPackage()).isFalse();
    assertThat(typeName.isPrimitive()).isFalse();
  }

  @Test
  public void should_create_valid_typename_from_type_name_string_description() {
    typeName = new TypeName("org.assertj.assertions.generator.data.Player");
    assertThat(typeName.getSimpleName()).isEqualTo("Player");
    assertThat(typeName.getSimpleNameWithOuterClass()).isEqualTo("Player");
    assertThat(typeName.getSimpleNameWithOuterClassNotSeparatedByDots()).isEqualTo("Player");
    assertThat(typeName.getPackageName()).isEqualTo("org.assertj.assertions.generator.data");
    assertThat(typeName.belongsToJavaLangPackage()).isFalse();
    assertThat(typeName.isPrimitive()).isFalse();
  }

  @Test
  public void should_create_valid_typename_for_primitive() {
    typeName = new TypeName(int.class);
    assertThat(typeName.getSimpleName()).isEqualTo("int");
    assertThat(typeName.getSimpleNameWithOuterClass()).isEqualTo("int");
    assertThat(typeName.getSimpleNameWithOuterClassNotSeparatedByDots()).isEqualTo("int");
    assertThat(typeName.getPackageName()).isEmpty();
    assertThat(typeName.belongsToJavaLangPackage()).isFalse();
    assertThat(typeName.isPrimitive()).isTrue();
  }

  @Test
  public void should_create_valid_typename_for_primitive_from_type_name_string_description() {
    typeName = new TypeName("int");
    assertThat(typeName.getSimpleName()).isEqualTo("int");
    assertThat(typeName.getSimpleNameWithOuterClass()).isEqualTo("int");
    assertThat(typeName.getSimpleNameWithOuterClassNotSeparatedByDots()).isEqualTo("int");
    assertThat(typeName.getPackageName()).isEmpty();
    assertThat(typeName.belongsToJavaLangPackage()).isFalse();
    assertThat(typeName.isPrimitive()).isTrue();
    // same
    typeName = new TypeName("int", null);
    assertThat(typeName.getSimpleName()).isEqualTo("int");
    assertThat(typeName.getSimpleNameWithOuterClass()).isEqualTo("int");
    assertThat(typeName.getSimpleNameWithOuterClassNotSeparatedByDots()).isEqualTo("int");
    assertThat(typeName.getPackageName()).isEmpty();
    assertThat(typeName.belongsToJavaLangPackage()).isFalse();
    assertThat(typeName.isPrimitive()).isTrue();
  }

  @Test
  public void should_fail_if_typename_description_is_empty() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("type name should not be blank or null");
    typeName = new TypeName("");
  }

  @Test
  public void should_detect_primitives_typename() {
    assertThat(new TypeName(int.class).isPrimitive()).isTrue();
    assertThat(new TypeName(long.class).isPrimitive()).isTrue();
    assertThat(new TypeName(short.class).isPrimitive()).isTrue();
    assertThat(new TypeName(boolean.class).isPrimitive()).isTrue();
    assertThat(new TypeName(char.class).isPrimitive()).isTrue();
    assertThat(new TypeName(byte.class).isPrimitive()).isTrue();
    assertThat(new TypeName(float.class).isPrimitive()).isTrue();
    assertThat(new TypeName(double.class).isPrimitive()).isTrue();
    assertThat(new TypeName(String.class).isPrimitive()).isFalse();
    assertThat(new TypeName("test.int").isPrimitive()).isFalse();
    assertThat(new TypeName("test.boolean").isPrimitive()).isFalse();
    assertThat(new TypeName("test.Boolean").isPrimitive()).isFalse();
  }

  @Test
  public void should_detect_real_numbers_typename() {
    assertThat(new TypeName(int.class).isRealNumber()).isFalse();
    assertThat(new TypeName(long.class).isRealNumber()).isFalse();
    assertThat(new TypeName(short.class).isRealNumber()).isFalse();
    assertThat(new TypeName(boolean.class).isRealNumber()).isFalse();
    assertThat(new TypeName(char.class).isRealNumber()).isFalse();
    assertThat(new TypeName(byte.class).isRealNumber()).isFalse();
    assertThat(new TypeName(float.class).isRealNumber()).isTrue();
    assertThat(new TypeName(double.class).isRealNumber()).isTrue();
    assertThat(new TypeName(String.class).isRealNumber()).isFalse();
    assertThat(new TypeName("test.int").isRealNumber()).isFalse();
    assertThat(new TypeName("test.boolean").isRealNumber()).isFalse();
    assertThat(new TypeName("test.Boolean").isRealNumber()).isFalse();
  }
  
  @Test
  public void should_detect_boolean_typename() {
    assertThat(new TypeName(boolean.class).isBoolean()).isTrue();
    assertThat(new TypeName("boolean").isBoolean()).isTrue();
    assertThat(new TypeName(char.class).isBoolean()).isFalse();
    assertThat(new TypeName("Boolean").isBoolean()).isFalse();
    assertThat(new TypeName("test.boolean").isBoolean()).isFalse();
  }

  @Test
  public void should_fail_if_type_simple_name_is_null() {
    try {
      typeName = new TypeName(null, "org.assertj.assertions.generator.data");
      failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
    } catch (IllegalArgumentException iae) {
      assertThat(iae).hasMessage("type simple name should not be null");
    }
  }

  @Test
  public void compare_to_should_compare_classes_fully_qualified_names() {
    // compare package before all
    TypeName typeNameData1A = new TypeName("A", "org.assertj.assertions.generator.data1");
    TypeName typeNameData2B = new TypeName("A", "org.assertj.assertions.generator.data2");
    assertThat(typeNameData1A.compareTo(typeNameData1A)).isZero();
    assertThat(typeNameData1A.compareTo(typeNameData2B)).isNegative();
    assertThat(typeNameData2B.compareTo(typeNameData1A)).isPositive();
    // if package are equals, compare class simple name
    TypeName typeNameA = new TypeName("A", "org.assertj.assertions.generator.data");
    TypeName typeNameB = new TypeName("B", "org.assertj.assertions.generator.data");
    assertThat(typeNameA.compareTo(typeNameA)).isZero();
    assertThat(typeNameA.compareTo(typeNameB)).isNegative();
    assertThat(typeNameB.compareTo(typeNameA)).isPositive();
  }

  @Test
  public void should_show_information_in_toString() {
    assertThat(new TypeName("A", "org.mypackage").toString()).isEqualTo("org.mypackage.A");
    assertThat(new TypeName(int.class).toString()).isEqualTo("int");
  }

}
