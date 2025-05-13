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
 * Copyright 2012-2025 the original author or authors.
 */
package org.assertj.assertions.generator.description.converter;

import com.google.common.reflect.TypeToken;
import org.assertj.assertions.generator.GenerateAssertion;
import org.assertj.assertions.generator.description.ClassDescription;
import org.assertj.assertions.generator.description.FieldDescription;
import org.assertj.assertions.generator.description.GetterDescription;
import org.assertj.assertions.generator.util.ClassUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static com.google.common.base.Preconditions.checkArgument;
import static org.assertj.assertions.generator.util.ClassUtil.*;

public class ClassToClassDescriptionConverter implements ClassDescriptionConverter<TypeToken<?>> {

  private final AnnotationConfiguration annotationConfiguration;

  public ClassToClassDescriptionConverter() {
    this(new AnnotationConfiguration(GenerateAssertion.class));
  }

  public ClassToClassDescriptionConverter(AnnotationConfiguration annotationConfiguration) {
    this.annotationConfiguration = annotationConfiguration;
  }

  @Override
  public ClassDescription convertToClassDescription(TypeToken<?> type) {
    checkArgument(!type.getRawType().isLocalClass(), "Can not support Local class %s", type);
    ClassDescription classDescription = new ClassDescription(type);
    classDescription.addGetterDescriptions(getterDescriptionsOf(type));
    classDescription.addFieldDescriptions(fieldDescriptionsOf(type));
    classDescription.addDeclaredGetterDescriptions(declaredGetterDescriptionsOf(type));
    classDescription.addDeclaredFieldDescriptions(declaredFieldDescriptionsOf(type));
    classDescription.setSuperType(type.getRawType().getSuperclass());
    return classDescription;
  }

  public ClassDescription convertToClassDescription(Class<?> clazz) {
    checkArgument(!clazz.isLocalClass(), "Can not support Local class %s", clazz);
    return convertToClassDescription(TypeToken.of(clazz));
  }

  private Set<GetterDescription> getterDescriptionsOf(TypeToken<?> type) {
    return doGetterDescriptionsOf(getterMethodsOf(type, annotationConfiguration.includedAnnotations()), type);
  }

  private Set<GetterDescription> declaredGetterDescriptionsOf(TypeToken<?> type) {
    return doGetterDescriptionsOf(declaredGetterMethodsOf(type, annotationConfiguration.includedAnnotations()), type);
  }

  private Set<GetterDescription> doGetterDescriptionsOf(Set<Method> getters, TypeToken<?> type) {
    Set<GetterDescription> getterDescriptions = new TreeSet<>();
    for (Method getter : getters) {
      // ignore getDeclaringClass if Enum
      if (isGetDeclaringClassEnumGetter(getter, type.getRawType())) continue;

      String propertyName = propertyNameOf(getter);

      getterDescriptions.add(new GetterDescription(propertyName, type, getter));
    }
    return getterDescriptions;
  }

  private Set<FieldDescription> declaredFieldDescriptionsOf(TypeToken<?> type) {
    return doFieldDescriptionsOf(type, declaredFieldsOf(type));
  }

  private Set<FieldDescription> fieldDescriptionsOf(TypeToken<?> type) {
    return doFieldDescriptionsOf(type, nonStaticFieldsOf(type));
  }

  private Set<FieldDescription> doFieldDescriptionsOf(TypeToken<?> type, List<Field> fields) {
    Set<FieldDescription> fieldDescriptions = new TreeSet<>();
    for (Field field : fields) {
      fieldDescriptions.add(new FieldDescription(field, ClassUtil.visibilityOf(field), type));
    }
    return fieldDescriptions;
  }

  private boolean isGetDeclaringClassEnumGetter(final Method getter, final Class<?> clazz) {
    return clazz.isEnum() && getter.getName().equals("getDeclaringClass");
  }

}
