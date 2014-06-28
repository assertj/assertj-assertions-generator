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

import static org.assertj.assertions.generator.description.TypeName.JAVA_LANG_PACKAGE;
import static org.assertj.assertions.generator.util.ClassUtil.getClassesRelatedTo;
import static org.assertj.assertions.generator.util.ClassUtil.getterMethodsAndNonStaticPublicFieldsOf;
import static org.assertj.assertions.generator.util.ClassUtil.getterMethodsOf;
import static org.assertj.assertions.generator.util.ClassUtil.isArray;
import static org.assertj.assertions.generator.util.ClassUtil.isIterable;
import static org.assertj.assertions.generator.util.ClassUtil.propertyNameOf;
import static org.assertj.assertions.generator.util.ClassUtil.nonStaticPublicFieldsOf;

import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.assertj.assertions.generator.description.ClassDescription;
import org.assertj.assertions.generator.description.FieldDescription;
import org.assertj.assertions.generator.description.GetterDescription;
import org.assertj.assertions.generator.description.TypeDescription;
import org.assertj.assertions.generator.description.TypeName;
import org.assertj.assertions.generator.util.ClassUtil;

import com.google.common.annotations.VisibleForTesting;

public class ClassToClassDescriptionConverter implements ClassDescriptionConverter<Class<?>> {

  public ClassDescription convertToClassDescription(Class<?> clazz) {
    ClassDescription classDescription = new ClassDescription(new TypeName(clazz));
    classDescription.addGetterDescriptions(getterDescriptionsOf(clazz));
    classDescription.addFieldDescriptions(fieldDescriptionsOf(clazz));
    classDescription.addTypeToImport(getNeededImportsFor(clazz));
    return classDescription;
  }

  @VisibleForTesting
  protected Set<GetterDescription> getterDescriptionsOf(Class<?> clazz) {
    Set<GetterDescription> getterDescriptions = new TreeSet<GetterDescription>();
    for (Method getter : getterMethodsOf(clazz)) {
      // ignore getDeclaringClass if Enum
      if (isGetDeclaringClassEnumGetter(getter, clazz)) continue;
      final TypeDescription typeDescription = getTypeDescription(getter);
      final List<TypeName> exceptionTypeNames = getExceptionTypeNames(getter);
      String propertyName = propertyNameOf(getter);
      getterDescriptions.add(new GetterDescription(propertyName, typeDescription, exceptionTypeNames));
    }
    return getterDescriptions;
  }
  
  @VisibleForTesting
  protected Set<FieldDescription> fieldDescriptionsOf(Class<?> clazz) {
    Set<FieldDescription> fieldDescriptions = new TreeSet<FieldDescription>();
    for (Field field : nonStaticPublicFieldsOf(clazz)) {
      fieldDescriptions.add(new FieldDescription(field.getName(), getTypeDescription(field)));
    }
    return fieldDescriptions;
  }

  private boolean isGetDeclaringClassEnumGetter(final Method getter, final Class<?> clazz) {
    return clazz.isEnum() && getter.getName().equals("getDeclaringClass");
  }

  private List<TypeName> getExceptionTypeNames(final Method getter) {
    List<TypeName> exceptions = new ArrayList<TypeName>();
    for (Class<?> exception : getter.getExceptionTypes()) {
      exceptions.add(new TypeName(exception));
    }
    return exceptions;
  }

  private TypeDescription getTypeDescription(Member member) {
    final Class<?> type = getTypeOf(member);
    if (isArray(type)) return buildArrayTypeDescription(type);    

    if (isIterable(type)) {
      final TypeDescription typeDescription = new TypeDescription(new TypeName(type));
      typeDescription.setIterable(true);
      ParameterizedType parameterizedType = getParameterizedTypeOf(member);
      if (parameterizedType.getActualTypeArguments()[0] instanceof GenericArrayType) {
        GenericArrayType genericArrayType = (GenericArrayType) parameterizedType.getActualTypeArguments()[0];
        typeDescription.setElementTypeName(new TypeName(genericArrayType.toString()));
      } else {
        // Due to http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=7151486, 
        // java 7 is not able to detect GenericArrayType correctly => let's use a different way to detect array
        Class<?> internalClass = ClassUtil.getClass(parameterizedType.getActualTypeArguments()[0]);
        if (internalClass.isArray()) {
          typeDescription.setElementTypeName(new TypeName(internalClass.getComponentType() + "[]"));
        } else {
          typeDescription.setElementTypeName(new TypeName(internalClass));
        }
      }
      return typeDescription;
    }
    // "simple" type
    return new TypeDescription(new TypeName(type));
  }
  
  private static Class<?> getTypeOf(Member member) {
    if (member instanceof Method) return ((Method)member).getReturnType();
    if (member instanceof Field) return ((Field)member).getType();
    throw new IllegalArgumentException("argument should be a Method or Field but was " + member.getClass());
  }

  private static ParameterizedType getParameterizedTypeOf(Member member) {
    if (member instanceof Method) return (ParameterizedType) ((Method)member).getGenericReturnType();
    if (member instanceof Field) return (ParameterizedType) ((Field)member).getGenericType();
    throw new IllegalArgumentException("argument should be a Method or Field but was " + member.getClass());
  }
  
  private static boolean hasParameterizedType(Member member) {
    if (member instanceof Method) return ((Method)member).getGenericReturnType() instanceof ParameterizedType;
    if (member instanceof Field) return ((Field)member).getGenericType() instanceof ParameterizedType ;
    throw new IllegalArgumentException("argument should be a Method or Field but was " + member.getClass());
  }
  
  private static Class<?>[] getExceptionTypesOf(Member member) {
    if (member instanceof Method) return ((Method)member).getExceptionTypes();
    return new Class<?>[0];
  }
  
  private static TypeDescription buildArrayTypeDescription(final Class<?> arrayType) {
    final TypeDescription typeDescription = new TypeDescription(new TypeName(arrayType));
    typeDescription.setElementTypeName(new TypeName(arrayType.getComponentType()));
    typeDescription.setArray(true);
    return typeDescription;
  }
  
  /**return the import needed for the assertion class corresponding to given class
   * 
   * @param clazz
   * @return
   */
  private Set<TypeName> getNeededImportsFor(Class<?> clazz) {
    // collect property types
    Set<Class<?>> classesToImport = new HashSet<Class<?>>();
    for (Member getter : getterMethodsAndNonStaticPublicFieldsOf(clazz)) {
      Class<?> propertyType = getTypeOf(getter);
      if (isArray(propertyType)) {
        // we only need the component type, that is T in T[] array
        classesToImport.add(propertyType.getComponentType());
      } else if (isIterable(propertyType)) {
        // we need the Iterable parameter type, that is T in Iterable<T>
        // we don't need to import the Iterable since it does not appear directly in generated code, ex :
        // assertThat(actual.getTeamMates()).contains(teamMates); // teamMates -> List
        ParameterizedType parameterizedType = getParameterizedTypeOf(getter);
        if (parameterizedType.getActualTypeArguments()[0] instanceof GenericArrayType) {
          //
          GenericArrayType genericArrayType = (GenericArrayType) parameterizedType.getActualTypeArguments()[0];
          Class<?> parameterClass = ClassUtil.getClass(genericArrayType.getGenericComponentType());
          classesToImport.add(parameterClass);
        } else {
          // Due to http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=7151486, try to change if java 7 and a real array
          Class<?> internalClass = ClassUtil.getClass(parameterizedType.getActualTypeArguments()[0]);
          if (internalClass.isArray()) {
            classesToImport.add(internalClass.getComponentType());
          } else {
            classesToImport.add(internalClass);
          }
        }
      } else if (hasParameterizedType(getter)) {
        // return type is generic type, add it and all its parameters type.
        classesToImport.addAll(getClassesRelatedTo(getParameterizedTypeOf(getter)));
      } else {
        // return type is not generic type, simply add it.
        classesToImport.add(propertyType);
      }
      Collections.addAll(classesToImport, getExceptionTypesOf(getter));
    }
    // convert to TypeName, excluding primitive or types in java.lang that don't need to be imported.
    Set<TypeName> typeNamesToImport = resolveTypesToImport(classesToImport);
    // remove typenames belonging to the given class package, they don't need to be imported since assertion class will be in that package
    return removeTypeNamesInPackage(typeNamesToImport, clazz.getPackage().getName());
  }

  private Set<TypeName> removeTypeNamesInPackage(Set<TypeName> typeNamesToImport, String packageName) {
    Set<TypeName> filteredTypeNames = new TreeSet<TypeName>();
    for (TypeName typeName : typeNamesToImport) {
      if (!typeName.getPackageName().equals(packageName)) filteredTypeNames.add(typeName);
    }
    return filteredTypeNames;
  }

  private Set<TypeName> resolveTypesToImport(Set<Class<?>> classesToImport) {
    Set<TypeName> typeToImports = new TreeSet<TypeName>();
    for (Class<?> propertyType : classesToImport) {
      TypeName typeName = resolveType(propertyType);
      if (typeName != null) typeToImports.add(typeName);
    }
    return typeToImports;
  }

  private TypeName resolveType(Class<?> propertyType) {
    // recursive call for array of arrays
    if (propertyType.isArray()) return resolveType(propertyType.getComponentType());
    return shouldBeImported(propertyType) ? new TypeName(propertyType) : null;
  }

  private boolean shouldBeImported(Class<?> type) {
    // Package can be null in case of array of primitive.
    return !(type.isPrimitive() || type.getPackage() == null || JAVA_LANG_PACKAGE.equals(type.getPackage().getName()));
  }

}
