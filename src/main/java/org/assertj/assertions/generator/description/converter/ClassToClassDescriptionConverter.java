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
package org.assertj.assertions.generator.description.converter;

import static org.apache.commons.lang3.StringUtils.remove;
import static org.assertj.assertions.generator.util.ClassUtil.declaredGetterMethodsOf;
import static org.assertj.assertions.generator.util.ClassUtil.declaredPublicFieldsOf;
import static org.assertj.assertions.generator.util.ClassUtil.getterMethodsOf;
import static org.assertj.assertions.generator.util.ClassUtil.inheritsCollectionOrIsIterable;
import static org.assertj.assertions.generator.util.ClassUtil.isArray;
import static org.assertj.assertions.generator.util.ClassUtil.nonStaticPublicFieldsOf;
import static org.assertj.assertions.generator.util.ClassUtil.propertyNameOf;

import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.assertj.assertions.generator.GenerateAssertion;
import org.assertj.assertions.generator.description.ClassDescription;
import org.assertj.assertions.generator.description.FieldDescription;
import org.assertj.assertions.generator.description.GetterDescription;
import org.assertj.assertions.generator.description.TypeDescription;
import org.assertj.assertions.generator.description.TypeName;
import org.assertj.assertions.generator.util.ClassUtil;

public class ClassToClassDescriptionConverter implements ClassDescriptionConverter<Class<?>> {

  private final AnnotationConfiguration annotationConfiguration;

  public ClassToClassDescriptionConverter() {
    this(new AnnotationConfiguration(GenerateAssertion.class));
  }

  public ClassToClassDescriptionConverter(AnnotationConfiguration annotationConfiguration) {
    this.annotationConfiguration = annotationConfiguration;
  }

  public ClassDescription convertToClassDescription(Class<?> clazz) {
    ClassDescription classDescription = new ClassDescription(new TypeName(clazz));
    classDescription.addGetterDescriptions(getterDescriptionsOf(clazz));
    classDescription.addFieldDescriptions(fieldDescriptionsOf(clazz));
    classDescription.addDeclaredGetterDescriptions(declaredGetterDescriptionsOf(clazz));
    classDescription.addDeclaredFieldDescriptions(declaredFieldDescriptionsOf(clazz));
    classDescription.setSuperType(clazz.getSuperclass());
    return classDescription;
  }

  private Set<GetterDescription> getterDescriptionsOf(Class<?> clazz) {
    return doGetterDescriptionsOf(getterMethodsOf(clazz, annotationConfiguration.includedAnnotations()), clazz);
  }

  private Set<GetterDescription> declaredGetterDescriptionsOf(Class<?> clazz) {
    return doGetterDescriptionsOf(declaredGetterMethodsOf(clazz, annotationConfiguration.includedAnnotations()), clazz);
  }

  private Set<GetterDescription> doGetterDescriptionsOf(Set<Method> getters, Class<?> clazz) {
    Set<GetterDescription> getterDescriptions = new TreeSet<>();
    for (Method getter : getters) {
      // ignore getDeclaringClass if Enum
      if (isGetDeclaringClassEnumGetter(getter, clazz)) continue;
      final TypeDescription typeDescription = getTypeDescription(getter);
      final List<TypeName> exceptionTypeNames = getExceptionTypeNames(getter);
      String propertyName = propertyNameOf(getter);
      getterDescriptions.add(new GetterDescription(propertyName, getter.getName(), typeDescription,
                                                   exceptionTypeNames));
    }
    return getterDescriptions;
  }

  private Set<FieldDescription> declaredFieldDescriptionsOf(Class<?> clazz) {
    return doFieldDescriptionsOf(declaredPublicFieldsOf(clazz));
  }

  private Set<FieldDescription> fieldDescriptionsOf(Class<?> clazz) {
    return doFieldDescriptionsOf(nonStaticPublicFieldsOf(clazz));
  }

  private Set<FieldDescription> doFieldDescriptionsOf(List<Field> fields) {
    Set<FieldDescription> fieldDescriptions = new TreeSet<>();
    for (Field field : fields) {
      fieldDescriptions.add(new FieldDescription(field.getName(), getTypeDescription(field)));
    }
    return fieldDescriptions;
  }

  private boolean isGetDeclaringClassEnumGetter(final Method getter, final Class<?> clazz) {
    return clazz.isEnum() && getter.getName().equals("getDeclaringClass");
  }

  private List<TypeName> getExceptionTypeNames(final Method getter) {
    List<TypeName> exceptions = new ArrayList<>();
    for (Class<?> exception : getter.getExceptionTypes()) {
      exceptions.add(new TypeName(exception));
    }
    return exceptions;
  }

  private TypeDescription getTypeDescription(Member member) {
    final Class<?> type = getTypeOf(member);
    if (isArray(type)) return buildArrayTypeDescription(type);
    // we are interested in collections and iterable but not subtype of Iterable that are not collection.
    // e.g. java.file.nio.Path that implements Iterable but has no ParameterizedType (ex : Path.getParent() -> Path)
    if (inheritsCollectionOrIsIterable(type)) return buildIterableTypeDescription(member, type);
    // "simple" type
    return new TypeDescription(new TypeName(type));
  }

  private TypeDescription buildIterableTypeDescription(Member member, final Class<?> type) {
    final TypeDescription typeDescription = new TypeDescription(new TypeName(type));
    typeDescription.setIterable(true);
    if (methodReturnTypeHasNoParameterInfo(member)) {
      // not a ParameterizedType, i.e. no parameter information => use Object as element type.
      typeDescription.setElementTypeName(new TypeName(Object.class));
      return typeDescription;
    }
    ParameterizedType parameterizedType = getParameterizedTypeOf(member);
    if (parameterizedType.getActualTypeArguments()[0] instanceof GenericArrayType) {
      GenericArrayType genericArrayType = (GenericArrayType) parameterizedType.getActualTypeArguments()[0];
      typeDescription.setElementTypeName(new TypeName(genericArrayType.toString()));
      return typeDescription;
    }
    // Due to http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=7151486,
    // java 7 is not able to detect GenericArrayType correctly => let's use a different way to detect array
    Class<?> internalClass = ClassUtil.getClass(parameterizedType.getActualTypeArguments()[0]);
    if (internalClass.isArray()) {
      String componentTypeWithoutClassPrefix = remove(internalClass.getComponentType().toString(), "class ");
      typeDescription.setElementTypeName(new TypeName(componentTypeWithoutClassPrefix + "[]"));
    } else {
      typeDescription.setElementTypeName(new TypeName(internalClass));
    }
    return typeDescription;
  }

  private static boolean methodReturnTypeHasNoParameterInfo(Member member) {
    // java loose generic info if getter is overridden :(
    return member instanceof Method && !(((Method) member).getGenericReturnType() instanceof ParameterizedType);
  }

  private static Class<?> getTypeOf(Member member) {
    if (member instanceof Method) return ((Method) member).getReturnType();
    if (member instanceof Field) return ((Field) member).getType();
    throw new IllegalArgumentException("argument should be a Method or Field but was " + member.getClass());
  }

  private static ParameterizedType getParameterizedTypeOf(Member member) {
    if (member instanceof Method) return (ParameterizedType) ((Method) member).getGenericReturnType();
    if (member instanceof Field) return (ParameterizedType) ((Field) member).getGenericType();
    throw new IllegalArgumentException("argument should be a Method or Field but was " + member.getClass());
  }

  private static TypeDescription buildArrayTypeDescription(final Class<?> arrayType) {
    final TypeDescription typeDescription = new TypeDescription(new TypeName(arrayType));
    typeDescription.setElementTypeName(new TypeName(arrayType.getComponentType()));
    typeDescription.setArray(true);
    return typeDescription;
  }

}
