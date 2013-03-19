package org.assertj.assertions.generator.description;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;

import org.assertj.assertions.generator.data.Player;
import org.assertj.assertions.generator.description.TypeName;
import org.junit.Test;


public class TypeNameTest {

  private TypeName typeName;

  @Test
  public void should_create_valid_typename_from_class() {
    typeName = new TypeName(Player.class);
    assertThat(typeName.getSimpleName()).isEqualTo("Player");
    assertThat(typeName.getPackageName()).isEqualTo("org.assertj.assertions.generator.data");
    assertThat(typeName.belongsToJavaLangPackage()).isFalse();
    assertThat(typeName.isPrimitive()).isFalse();
  }

  @Test
  public void should_create_valid_typename() {
    typeName = new TypeName("Player", "org.assertj.assertions.generator.data");
    assertThat(typeName.getSimpleName()).isEqualTo("Player");
    assertThat(typeName.getPackageName()).isEqualTo("org.assertj.assertions.generator.data");
    assertThat(typeName.belongsToJavaLangPackage()).isFalse();
    assertThat(typeName.isPrimitive()).isFalse();
  }

  @Test
  public void should_create_valid_typename_from_type_name_string_description() {
    typeName = new TypeName("org.assertj.assertions.generator.data.Player");
    assertThat(typeName.getSimpleName()).isEqualTo("Player");
    assertThat(typeName.getPackageName()).isEqualTo("org.assertj.assertions.generator.data");
    assertThat(typeName.belongsToJavaLangPackage()).isFalse();
    assertThat(typeName.isPrimitive()).isFalse();
  }

  @Test
  public void should_create_valid_typename_for_primitive() {
    typeName = new TypeName(int.class);
    assertThat(typeName.getSimpleName()).isEqualTo("int");
    assertThat(typeName.getPackageName()).isEmpty();
    assertThat(typeName.belongsToJavaLangPackage()).isFalse();
    assertThat(typeName.isPrimitive()).isTrue();
  }

  @Test
  public void should_create_valid_typename_for_primitive_from_type_name_string_description() {
    typeName = new TypeName("int");
    assertThat(typeName.getSimpleName()).isEqualTo("int");
    assertThat(typeName.getPackageName()).isEmpty();
    assertThat(typeName.belongsToJavaLangPackage()).isFalse();
    assertThat(typeName.isPrimitive()).isTrue();
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
  public void compare_to_should_compare_classes_fully_qaulified_names() {
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

}
