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
package org.assertj.assertions.generator.description.converter;

import com.google.common.annotations.VisibleForTesting;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.assertj.assertions.generator.description.ClassDescription;
import org.assertj.assertions.generator.description.GetterDescription;
import org.assertj.assertions.generator.description.TypeDescription;
import org.assertj.assertions.generator.description.TypeName;
import static org.assertj.assertions.generator.description.TypeName.JAVA_LANG_PACKAGE;
import org.assertj.assertions.generator.util.ClassUtil;
import static org.assertj.assertions.generator.util.ClassUtil.getClassesRelatedTo;
import static org.assertj.assertions.generator.util.ClassUtil.getterMethodsOf;
import static org.assertj.assertions.generator.util.ClassUtil.isIterable;
import static org.assertj.assertions.generator.util.ClassUtil.propertyNameOf;

public class ClassToClassDescriptionConverter implements ClassDescriptionConverter<Class<?>> {

  public ClassDescription convertToClassDescription(Class<?> clazz) {
    ClassDescription classDescription = new ClassDescription(new TypeName(clazz));
    classDescription.addGetterDescriptions(getterDescriptionsOf(clazz));
    classDescription.addTypeToImport(getNeededImportsFor(clazz));
    return classDescription;
  }

  @VisibleForTesting
  protected Set<GetterDescription> getterDescriptionsOf(Class<?> clazz) {
    Set<GetterDescription> getterDescriptions = new TreeSet<GetterDescription>();
    List<Method> getters = getterMethodsOf(clazz);
    for (Method getter : getters) {
      final TypeDescription typeDescription = getTypeDescription(getter);
      final List<TypeName> exceptionsTypeNames = getExceptionTypeNames(getter);
      getterDescriptions.add(new GetterDescription(propertyNameOf(getter), typeDescription, exceptionsTypeNames));
    }
    return getterDescriptions;
  }

  private List<TypeName> getExceptionTypeNames(final Method getter) {
    List<TypeName> exceptions = new ArrayList<TypeName>();
    for (Class<?> exception : getter.getExceptionTypes()) {
      exceptions.add(new TypeName(exception));
    }
    return exceptions;
  }

  protected TypeDescription getTypeDescription(Method getter) {
    final Class<?> propertyType = getter.getReturnType();
    final TypeDescription typeDescription = new TypeDescription(new TypeName(propertyType));
    if (propertyType.isArray()) {
      typeDescription.setElementTypeName(new TypeName(propertyType.getComponentType()));
      typeDescription.setArray(true);
    } else if (isIterable(propertyType)) {
      ParameterizedType parameterizedType = (ParameterizedType) getter.getGenericReturnType();
      if (parameterizedType.getActualTypeArguments()[0] instanceof GenericArrayType) {
        GenericArrayType genericArrayType = (GenericArrayType) parameterizedType.getActualTypeArguments()[0];
        Class<?> parameterClass = ClassUtil.getClass(genericArrayType.getGenericComponentType());
        typeDescription.setElementTypeName(new TypeName(parameterClass));
        typeDescription.setArray(true);
      } else {
        // Due to http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=7151486, try to change if java 7 and
        // a real array
        Class internalClass = ClassUtil.getClass(parameterizedType.getActualTypeArguments()[0]);
        if (internalClass.isArray()) {
          typeDescription.setElementTypeName(new TypeName(internalClass.getComponentType()));
          typeDescription.setArray(true);
        } else {
          typeDescription.setElementTypeName(new TypeName(internalClass));
        }
      }
      typeDescription.setIterable(true);
    }
    return typeDescription;
  }

  protected Set<TypeName> getNeededImportsFor(Class<?> clazz) {
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
        if (parameterizedType.getActualTypeArguments()[0] instanceof GenericArrayType) {
          //
          GenericArrayType genericArrayType = (GenericArrayType) parameterizedType.getActualTypeArguments()[0];
          Class<?> parameterClass = ClassUtil.getClass(genericArrayType.getGenericComponentType());
          classesToImport.add(parameterClass);
        } else {
          // Due to http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=7151486, try to change if java 7 and
          // a real array
          Class internalClass = ClassUtil.getClass(parameterizedType.getActualTypeArguments()[0]);
          if (internalClass.isArray()) {
            classesToImport.add(internalClass.getComponentType());
          } else {
            classesToImport.add(internalClass);
          }
        }
      } else if (getter.getGenericReturnType() instanceof ParameterizedType) {
        // return type is generic type, add it and all its parameters type.
        ParameterizedType parameterizedType = (ParameterizedType) getter.getGenericReturnType();
        classesToImport.addAll(getClassesRelatedTo(parameterizedType));
      } else {
        // return type is not generic type, simply add it.
        classesToImport.add(propertyType);
      }

      for (Class<?> exceptionType : getter.getExceptionTypes()) {
        classesToImport.add(exceptionType);
      }
    }
    // convert to TypeName, excluding primitive or types in java.lang that don't need to be imported.
    Set<TypeName> typeToImports = new TreeSet<TypeName>();
    for (Class<?> propertyType : classesToImport) {
      // Package can be null in case of array of primitive.
      if (!propertyType.isPrimitive()
          && (propertyType.getPackage() != null && !JAVA_LANG_PACKAGE.equals(propertyType.getPackage().getName()))) {
        typeToImports.add(new TypeName(propertyType));
      }
    }
    return typeToImports;
  }

}
