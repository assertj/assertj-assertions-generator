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
  private TypeName classTypeName;

  public ClassDescription(TypeName typeName) {
    super();
    this.classTypeName = typeName;
    this.typesToImports = new TreeSet<TypeName>();
    this.gettersDescriptions = new TreeSet<GetterDescription>();
  }

  public String getClassName() {
    return classTypeName.getSimpleName();
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

  public Set<GetterDescription> getGetters() {
    return gettersDescriptions;
  }

  public void addGetterDescriptions(Collection<GetterDescription> getterDescriptions) {
    gettersDescriptions.addAll(getterDescriptions);
  }

  @Override
  public String toString() {
    return "ClassDescription [classTypeName=" + classTypeName + ", typesToImports=" + typesToImports + "]";
  }

}
