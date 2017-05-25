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
 * Copyright 2012-2015 the original author or authors.
 */
package org.assertj.assertions.generator.util;

import com.google.common.base.Strings;
import com.google.common.reflect.TypeToken;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.lang3.StringUtils.indexOfAny;
import static org.apache.commons.lang3.StringUtils.remove;

/**
 * Includes useful utilities for types.
 */
public class TypeUtil {

  public static final Package JAVA_LANG_PACKAGE = Object.class.getPackage();

  /**
   * return the simple element valueType name if element valueType belongs to given the package and the fully qualified element
   * valueType name otherwise.
   *
   * @param packageName typically the package of the enclosing Class
   * @return the simple element valueType name if element valueType belongs to given the package and the fully qualified element
   *         valueType name otherwise.
   */
  public static String getFullyQualifiedTypeNameIfNeeded(TypeToken<?> type, String packageName) {
    Package toCheck = Package.getPackage(packageName);
    return getFullyQualifiedTypeNameIfNeeded(type, toCheck);
  }

  /**
   * return the simple element valueType name if element valueType belongs to given the package and the fully qualified element
   * valueType name otherwise.
   *
   * @param toCheck typically the package of the enclosing Class
   * @return the simple element valueType name if element valueType belongs to given the package and the fully qualified element
   *         valueType name otherwise.
   */
  public static String getFullyQualifiedTypeNameIfNeeded(TypeToken<?> type, Package toCheck) {
    return getTypeDeclarationWithinPackage(type, (toCheck == null ? null : toCheck.getName()), false);
  }

  public static boolean isInnerPackageOf(Package child, Package parent) {
    return child != null && parent != null && child.getName().startsWith(parent.getName());

  }

  public static boolean isInnerPackageOf(String childPackage, String parentPackage) {
    checkArgument(!Strings.isNullOrEmpty(childPackage), "childPackage is null or empty");
    checkNotNull(parentPackage, "parentPackage is null or empty");

    return childPackage.startsWith(parentPackage);
  }


  public static boolean isJavaLangType(TypeToken<?> type) {
    return type.isPrimitive() || type.isArray() || Objects.equals(JAVA_LANG_PACKAGE, type.getRawType().getPackage());
  }

  public static boolean isJavaLangType(Type type) {
    return isJavaLangType(TypeToken.of(type));
  }


  public static String getTypeDeclaration(TypeToken<?> type, final boolean asParameter, boolean fullyQualified) {
    StringBuilder bld = new StringBuilder();
    Class<?> raw = type.getRawType();
    getTypeDeclaration(bld, (raw.getPackage() == null ? null : raw.getPackage().getName()), type, asParameter, fullyQualified);
    return bld.toString();
  }

  /**
   * Uses the package name as a "local package" and tries to discern whether or not to generate
   * fully qualified names.
   * @param type
   * @param packageName
   * @param asParameter
   * @return
   */
  public static String getTypeDeclarationWithinPackage(TypeToken<?> type, String packageName, final boolean asParameter) {

    boolean reqFQN = !Objects.equals(packageName, JAVA_LANG_PACKAGE.getName())
        && (!type.isPrimitive() && !type.isArray() && !Objects.equals(packageName, type.getRawType().getPackage().getName()));
    StringBuilder bld = new StringBuilder();
    getTypeDeclaration(bld, packageName, type, asParameter, reqFQN);
    return bld.toString();
  }


  public static void getTypeDeclaration(StringBuilder bld, String basePackage, TypeToken<?> type, boolean asParameter, boolean fullyQualified) {

    Class<?> raw = type.getRawType();

    // Gotta do some special casing
    if (type.isArray()) {
      getTypeDeclaration(bld, basePackage, type.getComponentType(), asParameter, fullyQualified);
      bld.append("[]");
    } else if (type.isPrimitive()) {
      bld.append(raw.toString());
    } else {
      // Now we have some types that could be generic, so we have to do more
      // to serialize it to the declaration

      if (raw.isMemberClass()) { // inner class
        TypeToken<?> outerClass = type.resolveType(raw.getEnclosingClass());
        getTypeDeclaration(bld, basePackage, outerClass, asParameter, fullyQualified);
        bld.append(".");

      } else {
        // it's a normal class, so just append the package here if needed
        if (fullyQualified && !isJavaLangType(type)) {
          bld.append(type.getRawType().getPackage().getName());
          bld.append(".");
        }
      }

      bld.append(raw.getSimpleName());

      if (raw.getTypeParameters().length > 0) {
        bld.append("<");
        for (TypeVariable tv : raw.getTypeParameters()) {
          TypeToken<?> paramType = type.resolveType(tv);
          Class<?> rawParam = paramType.getRawType();

          if (rawParam.equals(Object.class)) {
            bld.append("?");
          } else {
            if (asParameter && !rawParam.equals(Object.class) && tv.getBounds().length > 0) {
              String typeString = StringUtils.removeAll(paramType.toString(), "capture#\\d+-of\\s+");
              typeString = typeString.replace("(\\?\\s+extends\\s+){2,}", "? extends ");

              if (!typeString.contains("?")) {
                bld.append("? extends ");
              }

              // fall through
            }

            Package paramPackage = paramType.getRawType().getPackage();

            getTypeDeclaration(bld, basePackage, paramType, asParameter,
                fullyQualified
                    || ((paramPackage != null && !Objects.equals(basePackage, paramPackage.getName()))
                    && Objects.equals(paramPackage, raw.getPackage())));
          }

          bld.append(",");
        }

        bld.deleteCharAt(bld.length() - 1);
        bld.append(">");
      }
    }

  }

  // used to support navigation assertion
  // https://github.com/joel-costigliola/assertj-assertions-generator/issues/67
  public static String getAssertType(TypeToken<?> type, String packageName) {

    Class<?> raw = type.getRawType();
    Package typePackage = raw.getPackage();

    if (isInnerPackageOf(typePackage, Package.getPackage("java"))) {
      try {
        String name = "org.assertj.core.api." + raw.getSimpleName() + "Assert";
        // try to get the class, if it exists, then we know its valid
        Class.forName(name);

        return name;
      } catch (ClassNotFoundException cfne) {
        // it wasn't found, this means the class doesn't exist, so fall back
      }
    }

    String typeName = resolveTypeNameInPackage(type, packageName);
    return typeName + "Assert";
  }

  public static String resolveTypeNameInPackage(TypeToken<?> type, Package currentPackage) {
    Class<?> raw = type.getRawType();

    if (Objects.equals(raw.getPackage(), currentPackage)) {
      return raw.getSimpleName();
    } else {
      return raw.getName();
    }
  }

  public static String resolveTypeNameInPackage(TypeToken<?> type, String currentPackage) {
    return resolveTypeNameInPackage(type,
        checkNotNull(Package.getPackage(currentPackage),
            "Package %s does not exist", currentPackage));
  }

  public static String resolveTypeNameInPackage(String type, String currentPackage) {
    if (type.startsWith(currentPackage)) {
      return type.substring(currentPackage.length() + 1, type.length());
    } else {
      return type;
    }
  }

  private static final String CAPITAL_LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

  public static String getTypeNameWithoutDots(String typeName) {
    int indexOfClassName = indexOfAny(typeName, CAPITAL_LETTERS);
    final String typeSimpleNameWithOuterClass;
    if (indexOfClassName > 0) {
      typeSimpleNameWithOuterClass = typeName.substring(indexOfClassName);
    } else {
      // primitive valueType => no package
      typeSimpleNameWithOuterClass = typeName;
    }

    return remove(typeSimpleNameWithOuterClass, ".");
  }

  public static boolean isBoolean(TypeToken<?> type) {
    TypeToken<?> unwrapped = type.unwrap();
    return unwrapped.isSubtypeOf(boolean.class);
  }
}
