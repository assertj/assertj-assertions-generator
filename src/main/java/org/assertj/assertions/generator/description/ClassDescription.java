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
import org.apache.commons.lang3.StringUtils;
import org.assertj.assertions.generator.util.ClassUtil;
import org.assertj.assertions.generator.util.StringUtil;

import java.util.*;

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
  private TypeToken<?> type;
  private TypeToken<?> superType;

  public ClassDescription(TypeToken<?> type) {
    super();
    this.type = type;
    this.superType = null;
    this.gettersDescriptions = new TreeSet<>();
    this.fieldsDescriptions = new TreeSet<>();
    this.declaredGettersDescriptions = new TreeSet<>();
    this.declaredFieldsDescriptions = new TreeSet<>();
  }

  public String getClassName() {
    return type.getRawType().getName();
  }
  
  public String getFullyQualifiedClassName() {
    return ClassUtil.getTypeDeclaration(type, false, true);
  }

  public TypeToken<?> getType() {
    return type;
  }

  public String getClassNameWithOuterClass() {
    return ClassUtil.getTypeDeclaration(type, false, false);
  }

  public String getClassNameWithOuterClassNotSeparatedByDots() {
    return ClassUtil.getTypeNameWithoutDots(getClassNameWithOuterClass()); //classTypeName.getSimpleNameWithOuterClassNotSeparatedByDots();
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

  public GetterDescription findGetterDescriptionForField(FieldDescription base) {

    final String capName = StringUtils.capitalize(base.getName());
    if (ClassUtil.isBoolean(base.getValueType())) {
      // deal with predicates

      // Build a map for better look-up
      Map<String, GetterDescription> fieldMap = new HashMap<>();
      for (GetterDescription getter: this.gettersDescriptions) {
        fieldMap.put(getter.getOriginalMember().getName(), getter);
      }
      for (GetterDescription getter: this.declaredGettersDescriptions) {
        fieldMap.put(getter.getOriginalMember().getName(), getter);
      }

      for (String prefix: ClassUtil.PREDICATE_PREFIXES.keySet()) {
        String propName = prefix + capName;

        GetterDescription getterDesc = fieldMap.get(propName);
        if (getterDesc != null) {
          return getterDesc;
        }
      }
    } else {

      String propName = "get" + capName;

      for (GetterDescription desc: this.gettersDescriptions) {
        if (Objects.equals(desc.getOriginalMember().getName(), propName)) {
          return desc;
        }
      }

      for (GetterDescription desc: this.declaredGettersDescriptions) {
        if (Objects.equals(desc.getOriginalMember().getName(), propName)) {
          return desc;
        }
      }
    }

    // wasn't found
    return null;
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

  public TypeToken<?> getSuperType() {
    return superType;
  }

  @SuppressWarnings("unchecked")
  public void setSuperType(Class<?> superType) {
    // TypeToken#getSupertype(..) checks to make sure it is a super type
    if (superType != null) {
      this.superType = type.getSupertype((Class)superType);
    }
  }
}
