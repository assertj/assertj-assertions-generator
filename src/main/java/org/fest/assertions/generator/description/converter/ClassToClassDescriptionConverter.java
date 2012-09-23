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

import static org.fest.assertions.generator.description.TypeName.JAVA_LANG_PACKAGE;
import static org.fest.assertions.generator.util.ClassUtil.getClassesRelatedTo;
import static org.fest.assertions.generator.util.ClassUtil.getterMethodsOf;
import static org.fest.assertions.generator.util.ClassUtil.isIterable;
import static org.fest.assertions.generator.util.ClassUtil.propertyNameOf;

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
        typeDescription.setIterable(true);
      }
      getterDescriptions.add(new GetterDescription(propertyNameOf(getter), typeDescription));
    }
    return getterDescriptions;
  }

  private Set<TypeName> getNeededImportsFor(Class<?> clazz) {
    // collect property types
    Set<Class<?>> classesToImport = new HashSet<Class<?>>();
    for (Method getter : getterMethodsOf(clazz)) {
      Class<?> propertyType = getter.getReturnType();
      if (propertyType.isArray()) {
        // we only need the component type, that is T in T[] array
        classesToImport.add(propertyType.getComponentType());
      } else if (isIterable(propertyType)) {
        // we need the Iterable parameter type, that is T in Iterable<T> 
        // we don't need to import the Iterable since it does not appear directly in generated code, ex :
        // assertThat(actual.getTeamMates()).contains(teamMates); // teamMates -> List
        ParameterizedType parameterizedType = (ParameterizedType) getter.getGenericReturnType();
        Class<?> actualParameterClass = (Class<?>) parameterizedType.getActualTypeArguments()[0];
        classesToImport.add(actualParameterClass);
      } else if (getter.getGenericReturnType() instanceof ParameterizedType) {
        // return type is generic type, add it and all its parameters type.
        ParameterizedType parameterizedType = (ParameterizedType) getter.getGenericReturnType();
        classesToImport.addAll(getClassesRelatedTo(parameterizedType));
      } else {
        // return type is not generic type, simply add it.
        classesToImport.add(propertyType);
      }
    }
    // convert to TypeName, excluding primitive or types in java.lang that don't need to be imported.
    Set<TypeName> typeToImports = new TreeSet<TypeName>();
    for (Class<?> propertyType : classesToImport) {
      if (!propertyType.isPrimitive() && !JAVA_LANG_PACKAGE.equals(propertyType.getPackage().getName())) {
        typeToImports.add(new TypeName(propertyType));
      }
    }
    return typeToImports;
  }

}
