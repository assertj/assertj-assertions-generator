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
package org.assertj.assertions.generator.description;

import static com.google.common.base.Objects.equal;
import static org.apache.commons.lang3.ArrayUtils.contains;
import static org.apache.commons.lang3.StringUtils.indexOfAny;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.remove;
import static org.apache.commons.lang3.StringUtils.substringAfterLast;
import static org.apache.commons.lang3.StringUtils.substringBefore;

import org.assertj.assertions.generator.util.ClassUtil;

/**
 * Describes a type with package and class/interface simple name.
 * <p>
 * {@link TypeName} is immutable.
 */
public class TypeName implements Comparable<TypeName> {

  private static final String CAPITAL_LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
  private static final String BOOLEAN = "boolean";
  private static final String BOOLEAN_WRAPPER = "Boolean";
  private static final String CHAR = "char";
  private static final String CHARRACTER = "Character";
  private static final String NO_PACKAGE = "";
  public static final String JAVA_LANG_PACKAGE = "java.lang";
  protected static final String[] PRIMITIVE_TYPES = { "int", "long", "short", "byte", "float", "double", "char", BOOLEAN };
  protected static final String[] REAL_NUMBERS_TYPES = { "float", "double"};
  protected static final String[] REAL_NUMBERS_WRAPPER_TYPES = { "Float", "Double" };
  protected static final String[] WHOLE_NUMBERS_TYPES = { "int", "long", "short", "byte" };
  protected static final String[] WHOLE_NUMBERS_WRAPPER_TYPES = { "Integer", "Long", "Short", "Byte" };

  private String typeSimpleName;
  private String typeSimpleNameWithOuterClass;
  private String typeSimpleNameWithOuterClassNotSeparatedByDots;
  private String packageName;

  public TypeName(String typeSimpleName, String packageName) {
    if (typeSimpleName == null) throw new IllegalArgumentException("type simple name should not be null");
    this.typeSimpleName = typeSimpleName;
    this.typeSimpleNameWithOuterClass = typeSimpleName;
    this.typeSimpleNameWithOuterClassNotSeparatedByDots = typeSimpleName;
    setPackageName(packageName);
  }

  /**
   * WARNING : does not work for nested class like com.books.Author.Name, 
   * @param typeName
   */
  public TypeName(String typeName) {
    if (isBlank(typeName)) throw new IllegalArgumentException("type name should not be blank or null");
    int indexOfClassName = indexOfAny(typeName, CAPITAL_LETTERS);
    if (indexOfClassName > 0) {
      this.typeSimpleNameWithOuterClass = typeName.substring(indexOfClassName);
      setPackageName(remove(typeName, "." + typeSimpleNameWithOuterClass));
    } else {
      // primitive type => no package
      this.typeSimpleNameWithOuterClass = typeName;
      setPackageName(NO_PACKAGE);
    }
    this.typeSimpleName = typeSimpleNameWithOuterClass.contains(".") ? 
        substringAfterLast(typeSimpleNameWithOuterClass, ".") : typeSimpleNameWithOuterClass;
    this.typeSimpleNameWithOuterClassNotSeparatedByDots = remove(typeSimpleNameWithOuterClass, ".");
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

  public boolean isWholeNumber() {
    return isPrimitiveWholeNumber() || isWholeNumberWrapper();
  }
  private boolean isPrimitiveRealNumber() {
    return contains(REAL_NUMBERS_TYPES, typeSimpleName) && isEmpty(packageName);
  }
  
  private boolean isRealNumberWrapper() {
    return contains(REAL_NUMBERS_WRAPPER_TYPES, typeSimpleName) && JAVA_LANG_PACKAGE.equals(packageName);
  }

  private boolean isPrimitiveWholeNumber() {
    return contains(WHOLE_NUMBERS_TYPES, typeSimpleName) && isEmpty(packageName);
  }
  
  private boolean isWholeNumberWrapper() {
    return contains(WHOLE_NUMBERS_WRAPPER_TYPES, typeSimpleName) && JAVA_LANG_PACKAGE.equals(packageName);
  }

  public boolean isBoolean() {
    return isPrimitiveBoolean() || isBooleanWrapper();
  }

  private boolean isPrimitiveBoolean() {
    return BOOLEAN.equals(typeSimpleName) && isEmpty(packageName);
  }
  
  private boolean isBooleanWrapper() {
    return BOOLEAN_WRAPPER.equals(typeSimpleName) && JAVA_LANG_PACKAGE.equals(packageName);
  }

  public boolean isChar() {
    return isPrimitiveChar() || isCharacter();
  }

  private boolean isCharacter() {
    return CHARRACTER.equals(typeSimpleName) && JAVA_LANG_PACKAGE.equals(packageName);
  }

  private boolean isPrimitiveChar() {
    return CHAR.equals(typeSimpleName) && isEmpty(packageName);
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
    return getFullyQualifiedClassName();
  }

  @Override
  public int compareTo(TypeName o) {
    return getFullyQualifiedClassName().compareTo(o.getFullyQualifiedClassName());
  }

  public boolean isArray() {
    return typeSimpleName.contains("[]");
  }

  public boolean isNested() {
    return typeSimpleNameWithOuterClass.contains(".");
  }
  
  public TypeName getOuterClassTypeName() {
    if (!isNested()) return null;
    return new TypeName(substringBefore(typeSimpleNameWithOuterClass, ".") , packageName);
  }

  public String getFullyQualifiedClassName() {
    return isEmpty(packageName) ? typeSimpleNameWithOuterClass : packageName + "." + typeSimpleNameWithOuterClass;
  }
  
  public String getFullyQualifiedTypeNameIfNeeded(String targetPackage) {
	return belongsToJavaLangPackage() || equal(targetPackage, packageName) ? getSimpleNameWithOuterClass() : getFullyQualifiedClassName();
  }

  public boolean isPrimitiveWrapper() {
    return isWholeNumberWrapper() || isRealNumberWrapper() || isBooleanWrapper() || isCharacter();
  }

  public String getAssertTypeName(String packageName) {
    String fullName = getFullyQualifiedClassName();
    if (fullName.startsWith("java.")) {
      // lets assume the name is an assertj wrapper
      return "org.assertj.core.api." + getSimpleName() + "Assert";
    } else {
      String prefix = fullName;
      if (packageName != null && packageName.equals(getPackageName())) {
        prefix = getSimpleName();
      }
      return prefix + "Assert";
    }
  }
}
