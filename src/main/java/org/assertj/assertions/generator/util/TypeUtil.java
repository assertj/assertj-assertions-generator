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

  private static final Package JAVA_LANG_PACKAGE = Object.class.getPackage();

  /**
   * Checks if the package, {@code child} is under the package {@code parent}.
   * @param child Child package
   * @param parent Parent package
   * @return True iff the child package is under the parent, false if not or either is {@code null}.
   *
   * @see #isInnerPackageOf(String, String) String argument equivalent
   */
  public static boolean isInnerPackageOf(Package child, Package parent) {
    return child != null && parent != null
        && child.getName().startsWith(parent.getName());
  }

  /**
   * Utility version that allows to pass a string and {@link Package}.
   * <br/>
   * Delegates to {@link #isInnerPackageOf(String, String)}.
   *
   * @see #isInnerPackageOf(String, String)
   */
  public static boolean isInnerPackageOf(Package child, String parent) {
    return child != null && isInnerPackageOf(child.getName(), parent);
  }

  /**
   * Checks if the package, {@code child} is under the package {@code parent}.
   * @param parentPackage Parent package
   * @param childPackage Child package
   * @return True iff the child package is under the parent, false if not or either is {@code null}.
   *
   * @see #isInnerPackageOf(String, String) String argument equivalent
   */
  public static boolean isInnerPackageOf(String childPackage, String parentPackage) {
    checkArgument(!Strings.isNullOrEmpty(childPackage), "childPackage is null or empty");
    checkNotNull(parentPackage, "parentPackage is null or empty");

    return childPackage.startsWith(parentPackage);
  }


  /**
   * Checks if the type passed is a member of {@code java.lang} or is a "built-in" type (e.g. primitive or array).
   * @return true if part of java language
   */
  public static boolean isJavaLangType(TypeToken<?> type) {
    return type.isPrimitive() || type.isArray() || Objects.equals(JAVA_LANG_PACKAGE, type.getRawType().getPackage());
  }

  /**
   * Checks if the type passed is a member of {@code java.lang} or is a "built-in" type (e.g. primitive or array).
   * @return true if part of java language
   *
   * @see #isJavaLangType(TypeToken)
   */
  public static boolean isJavaLangType(Type type) {
    return isJavaLangType(TypeToken.of(type));
  }

  /**
   * Generates a "type declaration" that could be used in Java code based on the {@code type} and if it is a parameter,
   * it will try to be as "flexible" as possible.
   *
   * @param type Type to get declaration for
   * @param asParameter True if the type is being used as a parameter
   * @return String representation of the type
   *
   * @see #getTypeDeclaration(TypeToken, boolean, boolean)
   */
  public static String getTypeDeclaration(TypeToken<?> type, final boolean asParameter, boolean fullyQualified) {
    StringBuilder bld = new StringBuilder();
    Class<?> raw = type.getRawType();
    getTypeDeclaration(bld, (raw.getPackage() == null ? null : raw.getPackage().getName()), type, asParameter, fullyQualified);
    return bld.toString();
  }

  /**
   * Uses the package name as a "local package" and tries to discern whether or not to generate
   * fully qualified names.
   * @param type Type to get declaration for
   * @param packageName local package name
   * @param asParameter True if the type is being used as a parameter
   * @return String representation of the type
   *
   * @see #getTypeDeclaration(TypeToken, boolean, boolean)
   */
  public static String getTypeDeclarationWithinPackage(TypeToken<?> type, String packageName, final boolean asParameter) {

    boolean reqFQN = !Objects.equals(packageName, JAVA_LANG_PACKAGE.getName())
        && (!type.isPrimitive() && !type.isArray() && !Objects.equals(packageName, type.getRawType().getPackage().getName()));
    StringBuilder bld = new StringBuilder();
    getTypeDeclaration(bld, packageName, type, asParameter, reqFQN);
    return bld.toString();
  }


  /**
   * helper method for {@code #getTypeDeclarationXXX()}
   * @see #getTypeDeclaration(TypeToken, boolean, boolean)
   * @see #getTypeDeclarationWithinPackage(TypeToken, String, boolean)
   */
  private static void getTypeDeclaration(StringBuilder bld, String basePackage, TypeToken<?> type, boolean asParameter, boolean fullyQualified) {

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

      // Now handle generics
      if (raw.getTypeParameters().length > 0) {
        bld.append("<");
        boolean first = true;
        for (TypeVariable tv : raw.getTypeParameters()) {
          // only append at the end
          if (!first) {
            bld.append(',');
          }
          first = false;

          TypeToken<?> paramType = type.resolveType(tv);
          Class<?> rawParam = paramType.getRawType();
          String typeString = StringUtils.removeAll(paramType.toString(), "capture#\\d+-of\\s+");
          typeString = typeString.replace("(\\?\\s+extends\\s+){2,}", "? extends ");

          boolean isWildCard = typeString.contains("?");

          // Some specializations need to be done to make sure that the arguments
          // are property pulled out and written

          // If its a wild card and it has no boundary other than Object,
          // we just use the wild card
          if (isWildCard && rawParam.equals(Object.class)) {
            bld.append("?");
            continue;
          }

          if (asParameter) {
            // We handle parameters differently so that it's accepted more "flexibility"
            bld.append("? extends ");
          }

          // now we recursively add the type parameter, we set `asParameter` to false
          // because odds are it will become wrong to keep adding the "extends" boundaries
          Package paramPackage = paramType.getRawType().getPackage();

          getTypeDeclaration(bld, basePackage, paramType, false,
              fullyQualified
                  || ((paramPackage != null && !Objects.equals(basePackage, paramPackage.getName()))
                  && Objects.equals(paramPackage, raw.getPackage())));
        }

        bld.append(">");
      }
    }

  }

  /**
   * Gets the name of the class that will be the "assert".
   * @param type Type being tested
   * @param packageName package this type will reside in
   * @return Name for "assert" type
   */
  // used to support navigation assertion
  // https://github.com/joel-costigliola/assertj-assertions-generator/issues/67
  public static String getAssertType(TypeToken<?> type, String packageName) {

    TypeToken<?> wrapped = type.wrap();
    Class<?> raw = wrapped.getRawType();

    String typeName = null;
    if (isJavaLangType(wrapped)) {
      try {
        String builtInName = "org.assertj.core.api." + raw.getSimpleName() + "Assert";
        // try to get the class, if it exists, then we know its valid
        Class.forName(builtInName);

        typeName = builtInName.substring(0, builtInName.length() - "Assert".length());
      } catch (ClassNotFoundException cfne) {
        // it wasn't found, this means the class doesn't exist, so fall through
      }
    }

    if (typeName == null) {
      typeName = type.getRawType().getName();
    }

    return resolveTypeNameInPackage(typeName + "Assert", packageName);
  }

  /**
   * Gets the name of a type without the package if {@code currentPackage} is the same as
   * {@link Class#getPackage() type's package}.
   *
   * @param type Type to import
   * @param currentPackage package context for the string
   * @return String Name resolved within the package
   */
  public static String resolveTypeNameInPackage(TypeToken<?> type, String currentPackage) {
    // we special case java.lang types because they never need a FQN.
    if (isJavaLangType(type)) {
      return type.getRawType().getSimpleName();
    }

    return resolveTypeNameInPackage(type.getRawType().getName(), currentPackage);
  }

  /**
   * Gets the name of a type without the package if {@code currentPackage} is the same as
   * {@link Class#getPackage() type's package}.
   *
   * @param type Type to import
   * @param currentPackage package context for the string
   * @return String Name resolved within the package
   */
  public static String resolveTypeNameInPackage(Type type, String currentPackage) {
    return resolveTypeNameInPackage(TypeToken.of(type), currentPackage);
  }

  /**
   * Gets the name of a type without the package if {@code currentPackage} is the same as
   * {@link Class#getPackage() type's package}.
   *
   * @param type Type to import
   * @param currentPackage package context for the string
   * @return Name resolved within the package
   */
  private static String resolveTypeNameInPackage(String type, String currentPackage) {
    if (!Strings.isNullOrEmpty(currentPackage) && type.startsWith(currentPackage)) {
      return type.substring(currentPackage.length() + 1, type.length());
    } else {
      return type;
    }
  }

  private static final String CAPITAL_LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

  /**
   * Gets a type name without any dots in it if they are present -- this is for nested classes
   * @param typeName String name of the type
   * @return Type without any {@code '.'} characters
   */
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

  /**
   * Checks if a type is a boolean type
   * @param type Type to check
   * @return true iff the type is a boolean.
   */
  public static boolean isBoolean(TypeToken<?> type) {
    TypeToken<?> unwrapped = type.unwrap();
    return unwrapped.isSubtypeOf(boolean.class);
  }
}
