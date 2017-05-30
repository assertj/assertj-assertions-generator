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

import static org.assertj.assertions.generator.util.ClassUtil.propertyNameOf;

import java.util.*;

import org.apache.commons.lang3.StringUtils;
import org.assertj.assertions.generator.util.ClassUtil;

/**
 * 
 * Stores the information needed to generate assertions for a given class.
 * 
 * @author Joel Costigliola
 * 
 */
public class ClassDescription implements Comparable<ClassDescription> {

  private Set<GetterDescription> gettersDescriptions;
  private Set<FieldDescription> fieldsDescriptions;
  private Set<GetterDescription> declaredGettersDescriptions;
  private Set<FieldDescription> declaredFieldsDescriptions;
  private TypeName classTypeName;
  private Class<?> superType;

  public ClassDescription(TypeName typeName) {
    super();
    this.classTypeName = typeName;
    this.gettersDescriptions = new TreeSet<GetterDescription>();
    this.fieldsDescriptions = new TreeSet<FieldDescription>();
    this.declaredGettersDescriptions = new TreeSet<GetterDescription>();
    this.declaredFieldsDescriptions = new TreeSet<FieldDescription>();
  }

  public String getClassName() {
    return classTypeName.getSimpleName();
  }
  
  public String getFullyQualifiedClassName() {
    return classTypeName.getFullyQualifiedClassName();
  }

  public TypeName getTypeName() {
    return classTypeName;
  }

  public String getClassNameWithOuterClass() {
    return classTypeName.getSimpleNameWithOuterClass();
  }

  public String getClassNameWithOuterClassNotSeparatedByDots() {
    return classTypeName.getSimpleNameWithOuterClassNotSeparatedByDots();
  }
  
  public String getPackageName() {
    return classTypeName.getPackageName();
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
  
  @Override
  public String toString() {
    return "ClassDescription [classTypeName=" + classTypeName + "]";
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (!(o instanceof ClassDescription)) return false;

    final ClassDescription that = (ClassDescription) o;
    if (classTypeName != null ? !classTypeName.equals(that.classTypeName) : that.classTypeName != null) return false;
    return true;
  }

  @Override
  public int hashCode() {
    return classTypeName != null ? classTypeName.hashCode() : 0;
  }
  
  @Override
  public int compareTo(ClassDescription o) {
    return classTypeName.compareTo(o.classTypeName);
  }

  public Class<?> getSuperType() {
    return superType;
  }

  public void setSuperType(Class<?> superType) {
    this.superType = superType;
  }

  public GetterDescription findGetterDescriptionForField(FieldDescription base) {
    // Build a map for better look-up
    Map<String, GetterDescription> fieldMap = new HashMap<>();
    for (GetterDescription getter: this.gettersDescriptions) {
      fieldMap.put(getter.getOriginalMember(), getter);
    }
    for (GetterDescription getter: this.declaredGettersDescriptions) {
      fieldMap.put(getter.getOriginalMember(), getter);
    }

    final String capName = StringUtils.capitalize(propertyNameOf(base.getName()));
    if (base.getTypeDescription().isBoolean()) {
      // deal with predicates
      for (String prefix: ClassUtil.PREDICATE_PREFIXES.keySet()) {
        String propName = prefix + capName;

        GetterDescription getterDesc = fieldMap.get(propName);
        if (getterDesc != null) {
          return getterDesc;
        }
      }
    } else {

      String propName = "get" + capName;

      GetterDescription getterDesc = fieldMap.get(propName);
      if (getterDesc != null) {
        return getterDesc;
      }
    }

    // wasn't found
    return null;
  }
}
