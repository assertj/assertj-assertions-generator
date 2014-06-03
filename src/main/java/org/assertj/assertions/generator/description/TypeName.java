package org.assertj.assertions.generator.description;

import static org.apache.commons.lang3.ArrayUtils.contains;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.substringAfterLast;
import static org.apache.commons.lang3.StringUtils.substringBeforeLast;

import org.assertj.assertions.generator.util.ClassUtil;

/**
 * Describes a type with package and class/interface simple name.
 * <p>
 * {@link TypeName} is immutable.
 * 
 * @author Joel Costigliola
 * 
 */
public class TypeName implements Comparable<TypeName> {

  private static final String BOOLEAN = "boolean";
  private static final String NO_PACKAGE = "";
  public static final String JAVA_LANG_PACKAGE = "java.lang";
  protected static final String[] PRIMITIVE_TYPES = { "int", "long", "short", "byte", "float", "double", "char", BOOLEAN };
  protected static final String[] REAL_NUMBERS_TYPES = { "float", "double"};
  protected static final String[] REAL_NUMBERS_WRAPPER_TYPES = { "Float", "Double" };

  private String typeSimpleName;
  private String typeSimpleNameWithOuterClass;
  private String typeSimpleNameWithOuterClassNotSeparatedByDots;
  private String packageName;

  public TypeName(String typeSimpleName, String packageName) {
    super();
    if (typeSimpleName == null) throw new IllegalArgumentException("type simple name should not be null");
    this.typeSimpleName = typeSimpleName;
    this.typeSimpleNameWithOuterClass = typeSimpleName;
    this.typeSimpleNameWithOuterClassNotSeparatedByDots = typeSimpleName;
    setPackageName(packageName);
  }

  public TypeName(String typeName) {
    if (isBlank(typeName)) throw new IllegalArgumentException("type name should not be blank or null");
    if (typeName.contains(".")) {
      this.typeSimpleName = substringAfterLast(typeName, ".");
      setPackageName(substringBeforeLast(typeName, "."));
    } else {
      // primitive type => no package
      this.typeSimpleName = typeName;
      setPackageName(NO_PACKAGE);
    }
    this.typeSimpleNameWithOuterClass = typeSimpleName;
    this.typeSimpleNameWithOuterClassNotSeparatedByDots = typeSimpleName;
  }

  public TypeName(Class<?> clazz) {
    super();
    this.typeSimpleName = clazz.getSimpleName();
    this.typeSimpleNameWithOuterClass = ClassUtil.getSimpleNameWithOuterClass(clazz);
    this.typeSimpleNameWithOuterClassNotSeparatedByDots = ClassUtil.getSimpleNameWithOuterClassNotSeparatedByDots(clazz);
    this.packageName = clazz.getPackage() == null ? NO_PACKAGE : clazz.getPackage().getName();
  }

  public String getSimpleName() {
    return typeSimpleName;
  }

  public String getSimpleNameWithOuterClass() {
    return typeSimpleNameWithOuterClass;
  }

  public String getSimpleNameWithOuterClassNotSeparatedByDots() {
    return typeSimpleNameWithOuterClassNotSeparatedByDots;
  }
  
  public String getPackageName() {
    return packageName;
  }

  private void setPackageName(String packageName) {
    this.packageName = packageName == null ? NO_PACKAGE : packageName;
  }

  public boolean isPrimitive() {
    return contains(PRIMITIVE_TYPES, typeSimpleName) && isEmpty(packageName);
  }

  public boolean isRealNumber() {
    return isPrimitiveRealNumber() || isRealNumberWrapper();
  }

  private boolean isPrimitiveRealNumber() {
    return contains(REAL_NUMBERS_TYPES, typeSimpleName) && isEmpty(packageName);
  }
  
  private boolean isRealNumberWrapper() {
    return contains(REAL_NUMBERS_WRAPPER_TYPES, typeSimpleName) && JAVA_LANG_PACKAGE.equals(packageName);
  }
  
  public boolean isBoolean() {
    return BOOLEAN.equals(typeSimpleName) && isEmpty(packageName);
  }
  
  public boolean belongsToJavaLangPackage() {
    return JAVA_LANG_PACKAGE.equals(packageName);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((packageName == null) ? 0 : packageName.hashCode());
    result = prime * result + ((typeSimpleName == null) ? 0 : typeSimpleName.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    TypeName other = (TypeName) obj;
    if (packageName == null) {
      if (other.packageName != null) return false;
    } else if (!packageName.equals(other.packageName)) return false;
    if (typeSimpleName == null) {
      if (other.typeSimpleName != null) return false;
    } else if (!typeSimpleName.equals(other.typeSimpleName)) return false;
    return true;
  }

  @Override
  public String toString() {
    return isEmpty(packageName) ? typeSimpleName : packageName + "." + typeSimpleName;
  }

  @Override
  public int compareTo(TypeName o) {
    return toString().compareTo(o.toString());
  }

  public boolean isArray() {
    return typeSimpleName.contains("[]");
  }
}
