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
 * Copyright @2010-2011 the original author or authors.
 */
package org.assertj.assertions.generator.description;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

/**
 * 
 * Stores the information needed to generate assertions for a given class.
 * 
 * @author Joel Costigliola
 * 
 */
public class ClassDescription {

  private Set<TypeName> typesToImports;
  private Set<GetterDescription> gettersDescriptions;
  private Set<FieldDescription> fieldsDescriptions;
  private TypeName classTypeName;
  private TypeName superTypeName;

  public ClassDescription(TypeName typeName) {
    super();
    this.classTypeName = typeName;
    this.typesToImports = new TreeSet<TypeName>();
    this.gettersDescriptions = new TreeSet<GetterDescription>();
    this.fieldsDescriptions = new TreeSet<FieldDescription>();
  }

  public String getClassName() {
    return classTypeName.getSimpleName();
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

  public Set<TypeName> getImports() {
    return typesToImports;
  }

  public void addTypeToImport(Collection<TypeName> typesToImport) {
    this.typesToImports.addAll(typesToImport);
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

  @Override
  public String toString() {
    return "ClassDescription [classTypeName=" + classTypeName + ", typesToImports=" + typesToImports + "]";
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

  public TypeName getSuperType() {
    return superTypeName;
  }

  public void setSuperType(TypeName superTypeName) {
    this.superTypeName = superTypeName;
  }
}
