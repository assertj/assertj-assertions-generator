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
package org.fest.assertions.generator.description.converter;

import static org.fest.assertions.generator.util.ClassUtil.*;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.fest.assertions.generator.description.ClassDescription;
import org.fest.assertions.generator.description.GetterDescription;
import org.fest.assertions.generator.description.TypeDescription;
import org.fest.assertions.generator.description.TypeName;

public class ClassToClassDescriptionConverter implements ClassDescriptionConverter<Class<?>> {

  public ClassDescription convertToClassDescription(Class<?> clazz) {
    ClassDescription classDescription = new ClassDescription(new TypeName(clazz));
    classDescription.addGetterDescriptions(getterDescriptionsOf(clazz));
    classDescription.addTypeToImport(getNeededImportsFor(clazz));
    return classDescription;
  }

  private Set<GetterDescription> getterDescriptionsOf(Class<?> clazz) {
    Set<GetterDescription> getterDescriptions = new TreeSet<GetterDescription>();
    List<Method> getters = getterMethodsOf(clazz);
    for (Method getter : getters) {
      Class<?> propertyType = getter.getReturnType();
      TypeDescription typeDescription = new TypeDescription(new TypeName(propertyType));
      if (propertyType.isArray()) {
        typeDescription.setElementTypeName(new TypeName(propertyType.getComponentType()));
        typeDescription.setArray(true);
      } else if (isIterable(propertyType)) {
        ParameterizedType parameterizedType = (ParameterizedType) getter.getGenericReturnType();
        Class<?> parameterClass = (Class<?>) parameterizedType.getActualTypeArguments()[0];
        typeDescription.setElementTypeName(new TypeName(parameterClass));
        typeDescription.setGeneric(true);
        typeDescription.setIterable(true);
      }
      // TODO what if there is several parameter types ?
      getterDescriptions.add(new GetterDescription(propertyNameOf(getter), typeDescription));
    }
    return getterDescriptions;
  }

  private Set<TypeName> getNeededImportsFor(Class<?> clazz) {
    // collect property types
    Set<Class<?>> typesToImport = new HashSet<Class<?>>();
    for (Method getter : getterMethodsOf(clazz)) {
      Class<?> propertyType = getter.getReturnType();
      if (propertyType.isArray()) {
        // we only need the component type, that is T in T[] array
        typesToImport.add(propertyType.getComponentType());
      } else if (isIterable(propertyType)) {
        // we need the Iterable parameter type, that is T in Iterable<T> 
        // we don't need to import the Iterable since it does not appear directly in generated code, ex :
        // assertThat(actual.getTeamMates()).contains(teamMates); // teamMates -> List
        ParameterizedType parameterizedType = (ParameterizedType) getter.getGenericReturnType();
        Class<?> actualParameterClass = (Class<?>) parameterizedType.getActualTypeArguments()[0];
        typesToImport.add(actualParameterClass);
      } else {
        typesToImport.add(propertyType);
      }
    }
    // imports as String
    Set<TypeName> imports = new TreeSet<TypeName>();
    for (Class<?> propertyType : typesToImport) {
      imports.add(new TypeName(propertyType));
    }
    return imports;
  }

}
