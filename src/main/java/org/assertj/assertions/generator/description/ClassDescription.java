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
package org.assertj.assertions.generator.description;

import com.google.common.reflect.TypeToken;
import org.assertj.assertions.generator.util.ClassUtil;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import static com.google.common.collect.Sets.union;
import static org.apache.commons.lang3.StringUtils.capitalize;
import static org.apache.commons.lang3.StringUtils.removeAll;
import static org.assertj.assertions.generator.util.ClassUtil.*;

/**
 *
 * Stores the information needed to generate assertions for a given class.
 *
 * @author Joel Costigliola
 *
 */
public class ClassDescription implements Comparable<ClassDescription> {

  private static final String ABSTRACT_ASSERT_CLASS_PREFIX = "Abstract";

  private static final String ASSERT_CLASS_SUFFIX = "Assert";

  private Set<GetterDescription> gettersDescriptions;
  private Set<FieldDescription> fieldsDescriptions;
  private Set<GetterDescription> declaredGettersDescriptions;
  private Set<FieldDescription> declaredFieldsDescriptions;
  private TypeToken<?> type;
  private TypeToken<?> superType;

  public ClassDescription(TypeToken<?> type) {
    this.type = type;
    this.superType = null;
    this.gettersDescriptions = new TreeSet<>();
    this.fieldsDescriptions = new TreeSet<>();
    this.declaredGettersDescriptions = new TreeSet<>();
    this.declaredFieldsDescriptions = new TreeSet<>();
  }

  public String getFullyQualifiedClassName() {
    return getTypeDeclaration(type);
  }

  public String getClassNameWithOuterClass() {
    String typeDeclaration = getTypeDeclaration(type);
    return removeAll(typeDeclaration, packageNameRegex(getPackageName()));
  }

  public String getPackageName() {
    return type.getRawType().getPackage().getName();
  }

  public Set<GetterDescription> getGettersDescriptions() {
    return gettersDescriptions;
  }

  public void addGetterDescriptions(Collection<GetterDescription> getterDescriptions) {
    this.gettersDescriptions.addAll(getterDescriptions);
  }

  public void addFieldDescriptions(Set<FieldDescription> fieldDescriptions) {
    this.fieldsDescriptions.addAll(fieldDescriptions);
  }

  public Set<FieldDescription> getFieldsDescriptions() {
    return fieldsDescriptions;
  }

  public Set<GetterDescription> getDeclaredGettersDescriptions() {
    return declaredGettersDescriptions;
  }

  public Set<FieldDescription> getDeclaredFieldsDescriptions() {
    return declaredFieldsDescriptions;
  }

  public void addDeclaredGetterDescriptions(Collection<GetterDescription> declaredGetterDescriptions) {
    this.declaredGettersDescriptions.addAll(declaredGetterDescriptions);
  }

  public void addDeclaredFieldDescriptions(Set<FieldDescription> declaredFieldDescriptions) {
    this.declaredFieldsDescriptions.addAll(declaredFieldDescriptions);
  }

  public boolean hasGetterForField(FieldDescription field) {
    // get all getters with a return type == field type
    Set<String> gettersCompatibleWithFieldType = new HashSet<>();
    for (GetterDescription getter : union(this.gettersDescriptions, this.declaredGettersDescriptions)) {
      if (Objects.equals(field.getValueType(), getter.getValueType())) {
        gettersCompatibleWithFieldType.add(getter.getOriginalMember().getName());
      }
    }
    // check boolean getters
    final String capName = capitalize(field.getName());
    if (field.isPredicate()) {
      for (String prefix : PREDICATE_PREFIXES.keySet()) {
        if (gettersCompatibleWithFieldType.contains(prefix + capName)) return true;
      }
    }
    // standard getter
    return gettersCompatibleWithFieldType.contains("get" + capName);
  }

  public TypeToken<?> getSuperType() {
    return superType;
  }

  @SuppressWarnings("unchecked")
  public void setSuperType(Class<?> superType) {
    // TypeToken#getSupertype(..) checks to make sure it is a super type
    if (superType != null) {
      this.superType = type.getSupertype((Class) superType);
    }
  }

  // assert related methods

  public String getAssertClassName() {
    return assertClassNameOf(type);
  }

  public String getAssertClassFilename() {
    String assertClassName = assertClassNameOf(type);
    return removeGenericFrom(assertClassName) + ".java";
  }

  public String getFullyQualifiedAssertClassName() {
    return getPackageName() + "." + getAssertClassName();
  }

  public String getAbstractAssertClassName() {
    return abstractAssertClassNameOf(type);
  }

  public String getAbstractAssertClassFilename() {
    String abstractAssertClassName = abstractAssertClassNameOf(type);
    return removeGenericFrom(abstractAssertClassName) + ".java";
  }

  public String getFullyQualifiedParentAssertClassName() {
    if (superType.getRawType().equals(Object.class)) return "org.assertj.core.api.AbstractObjectAssert";
    else return superType.getRawType().getPackage().getName() + "." + abstractAssertClassNameOf(superType);
  }

  public String getGenericTypeDeclaration() {
    return ClassUtil.extractGenericFrom(getClassNameWithOuterClass());
  }

  @Override
  public String toString() {
    return "ClassDescription [valueType=" + type + "]";
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (!(o instanceof ClassDescription)) return false;

    final ClassDescription that = (ClassDescription) o;
    return (Objects.equals(type, that.type));
  }

  @Override
  public int hashCode() {
    return Objects.hash(type);
  }

  @Override
  public int compareTo(ClassDescription o) {
    return type.getRawType().getName().compareTo(o.type.getRawType().getName());
  }

  private static String assertClassNameOf(TypeToken<?> type) {
    String typeDeclaration = getTypeDeclaration(type);
    String typeNameWithoutDots = getTypeNameWithoutDots(typeDeclaration);
    return ClassUtil.isGeneric(type) ?
        insertAssertClassSuffixBeforeGenericDeclaration(typeNameWithoutDots) :
        typeNameWithoutDots + ASSERT_CLASS_SUFFIX;
  }

  private static String insertAssertClassSuffixBeforeGenericDeclaration(String assertClassName) {
    return new StringBuilder(assertClassName).insert(assertClassName.indexOf('<'), ASSERT_CLASS_SUFFIX).toString();
  }

  private static String abstractAssertClassNameOf(TypeToken<?> type) {
    return ABSTRACT_ASSERT_CLASS_PREFIX + assertClassNameOf(type);
  }

  public boolean isGeneric() {
    return ClassUtil.isGeneric(type);
  }
}
