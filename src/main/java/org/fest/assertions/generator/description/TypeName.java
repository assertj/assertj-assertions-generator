package org.fest.assertions.generator.description;

import static org.apache.commons.lang3.ArrayUtils.contains;
import static org.apache.commons.lang3.StringUtils.*;

/**
 * Describes a type with package and class/interface simple name.
 * <p>
 * {@link TypeName} is immutabel.
 * 
 * @author Joel Costigliola
 * 
 */
public class TypeName implements Comparable<TypeName> {

  private static final String BOOLEAN = "boolean";
  private static final String NO_PACKAGE = "";
  protected static final String JAVA_LANG_PACKAGE = "java.lang";
  protected static final String[] PRIMITIVE_TYPES = { "int", "long", "short", "byte", "float", "double", "char", BOOLEAN };

  private String typeSimpleName;
  private String packageName;

  public TypeName(String typeSimpleName, String packageName) {
    super();
    if (typeSimpleName == null) throw new IllegalArgumentException("type simple name should not be null");
    setTypeSimpleName(typeSimpleName);
    setPackageName(packageName);
  }

  public TypeName(String typeName) {
    if (isBlank(typeName)) throw new IllegalArgumentException("type name should not be blank or null");
    if (typeName.contains(".")) {
      setTypeSimpleName(substringAfterLast(typeName, "."));
      setPackageName(substringBeforeLast(typeName, "."));
    } else {
      // primitive type => no package
      setTypeSimpleName(typeName);
      setPackageName(NO_PACKAGE);
    }
  }

  public TypeName(Class<?> clazz) {
    super();
    this.typeSimpleName = clazz.getSimpleName();
    this.packageName = clazz.getPackage() == null ? NO_PACKAGE : clazz.getPackage().getName();
  }

  public String getSimpleName() {
    return typeSimpleName;
  }

  public String getPackageName() {
    return packageName;
  }

  public boolean isPrimitive() {
    return contains(PRIMITIVE_TYPES, typeSimpleName) && isEmpty(packageName);
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

  public int compareTo(TypeName o) {
    // TODO Auto-generated method stub
    return 0;
  }

  private void setPackageName(String packageName) {
    this.packageName = packageName == null ? NO_PACKAGE : packageName;
  }

  private void setTypeSimpleName(String typeSimpleName) {
    this.typeSimpleName = typeSimpleName;
  }

}
