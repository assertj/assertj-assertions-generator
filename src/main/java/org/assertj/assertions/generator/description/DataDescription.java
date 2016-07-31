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
 * Copyright 2012-2017 the original author or authors.
 */
package org.assertj.assertions.generator.description;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Primitives;
import com.google.common.reflect.TypeToken;
import org.apache.commons.lang3.StringUtils;
import org.assertj.assertions.generator.util.ClassUtil;

import java.lang.reflect.Member;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.apache.commons.lang3.StringUtils.removeAll;
import static org.apache.commons.lang3.StringUtils.removeStart;
import static org.assertj.assertions.generator.util.ClassUtil.*;
import static org.assertj.assertions.generator.util.StringUtil.camelCaseToWords;

/**
 * base class to describe a field or a property/getter
 */
public abstract class DataDescription {

  private static final Map<String, String> PREDICATE_PREFIXES_FOR_JAVADOC =
      new ImmutableMap.Builder<String, String>().put("is", "is")
                                                .put("isNot", "is not")
                                                .put("was", "was")
                                                .put("wasNot", "was not")
                                                .put("can", "can")
                                                .put("canBe", "can be")
                                                .put("cannot", "cannot")
                                                .put("cannotBe", "cannot be")
                                                .put("should", "should")
                                                .put("shouldBe", "should be")
                                                .put("shouldNot", "should not")
                                                .put("shouldNotBe", "should not be")
                                                .put("has", "has")
                                                .put("doesNotHave", "does not have")
                                                .put("will", "will")
                                                .put("willBe", "will be")
                                                .put("willNot", "will not")
                                                .put("willNotBe", "will not be")
                                                .build();

  private static final Map<String, String> PREDICATE_PREFIXES_FOR_ERROR_MESSAGE_PART1 =
      new ImmutableMap.Builder<String, String>().put("is", "is")
                                                .put("isNot", "is not")
                                                .put("was", "was")
                                                .put("wasNot", "was not")
                                                .put("can", "can")
                                                .put("canBe", "can be")
                                                .put("cannot", "cannot")
                                                .put("cannotBe", "cannot be")
                                                .put("should", "should")
                                                .put("shouldBe", "should be")
                                                .put("shouldNot", "should not")
                                                .put("shouldNotBe", "should not be")
                                                .put("has", "has")
                                                .put("doesNotHave", "does not have")
                                                .put("will", "will")
                                                .put("willBe", "will be")
                                                .put("willNot", "will not")
                                                .put("willNotBe", "will not be")
                                                .build();

  private static final Map<String, String> PREDICATE_PREFIXES_FOR_ERROR_MESSAGE_PART2 =
      new ImmutableMap.Builder<String, String>().put("is", "is not")
                                                .put("isNot", "is")
                                                .put("was", "was not")
                                                .put("wasNot", "was")
                                                .put("can", "cannot")
                                                .put("canBe", "is not")
                                                .put("cannot", "can")
                                                .put("cannotBe", "is not")
                                                .put("should", "should not")
                                                .put("shouldBe", "is not")
                                                .put("shouldNot", "should")
                                                .put("shouldNotBe", "should be")
                                                .put("has", "does not have")
                                                .put("doesNotHave", "has")
                                                .put("will", "will not")
                                                .put("willBe", "will not be")
                                                .put("willNot", "will")
                                                .put("willNotBe", "will be")
                                                .build();

  private static final Ordering<String> BY_BIGGER_LENGTH_ORDERING = new Ordering<String>() {
    @Override
    public int compare(String left, String right) {
      return -Ints.compare(left.length(), right.length());
    }
  };

  private final String name;
  final Member originalMember;
  final TypeToken<?> valueType;
  private final TypeToken<?> owningType;
  protected final Visibility visibility;

  DataDescription(String name, Member originalMember, Visibility visibility, TypeToken<?> type, TypeToken<?> owningType) {
    this.name = name;
    this.originalMember = originalMember;
    this.visibility = visibility;
    this.valueType = type;
    this.owningType = owningType;
  }

  public String getName() {
    return name;
  }

  public Member getOriginalMember() {
    return originalMember;
  }

  TypeToken<?> getValueType() {
    return valueType;
  }

  public String getTypeName() {
    String typeName = getTypeDeclaration(valueType);
    return removeOwningTypePackageNameIn(typeName);
  }

  public boolean isIterableType() {
    return valueType.isSubtypeOf(Iterable.class);
  }

  public boolean isArrayType() {
    return valueType.isArray();
  }

  public boolean isPrimitiveType() {
    return valueType.isPrimitive();
  }

  public boolean isRealNumberType() {
    TypeToken<?> unwrapped = valueType.unwrap();
    return unwrapped.isSubtypeOf(double.class) || unwrapped.isSubtypeOf(float.class);
  }

  public boolean isWholeNumberType() {
    TypeToken<?> unwrapped = valueType.unwrap();
    return unwrapped.isSubtypeOf(int.class) || unwrapped.isSubtypeOf(long.class)
           || unwrapped.isSubtypeOf(byte.class) || unwrapped.isSubtypeOf(short.class);
  }

  public boolean isCharType() {
    TypeToken<?> unwrapped = valueType.unwrap();
    return unwrapped.isSubtypeOf(char.class);
  }

  public boolean isPrimitiveWrapperType() {
    return Primitives.isWrapperType(valueType.getRawType());
  }

  public abstract boolean isPredicate();

  public String getPredicate() {
    return originalMember.getName();
  }

  public String getNegativePredicate() {
    return getNegativePredicateFor(originalMember.getName());
  }

  boolean hasNegativePredicate() {
    return getNegativePredicateFor(originalMember.getName()) != null;
  }

  /**
   * return the simple element valueType name if element valueType belongs to given the package and the fully qualified element
   * valueType name otherwise.
   *
   * @return the simple element valueType name if element valueType belongs to given the package and the fully qualified element
   *         valueType name otherwise.
   */
  public String getElementTypeName() {
    String elementTypeName = null;
    if (valueType.isArray()) {
      elementTypeName = getTypeDeclaration(valueType.getComponentType());
    } else if (valueType.isSubtypeOf(Iterable.class)) {
      TypeToken<?> componentType = valueType.resolveType(Iterable.class.getTypeParameters()[0]);
      elementTypeName = getTypeDeclaration(componentType);
      // remove any generic type boundaries
      elementTypeName = removeAll(elementTypeName, "\\? extends").trim();
    }
    return removeOwningTypePackageNameIn(elementTypeName);
  }

  public String getElementAssertTypeName() {
    String packageName = owningTypePackageName();
    TypeToken<?> elementType = valueType.getComponentType();
    return elementType == null ? null : getAssertType(elementType, packageName);
  }

  public String getAssertTypeName(String packageName) {
    return getAssertType(valueType, packageName);
  }

  public String getPredicateForJavadoc() {
    String predicatePrefix = getPredicatePrefix(getPredicate());
    return PREDICATE_PREFIXES_FOR_JAVADOC.get(predicatePrefix) + " " + readablePropertyName();
  }

  public String getNegativePredicateForJavadoc() {
    String predicatePrefix = getPredicatePrefix(getNegativePredicate());
    return PREDICATE_PREFIXES_FOR_JAVADOC.get(predicatePrefix) + " " + readablePropertyName();
  }

  public String getPredicateForErrorMessagePart1() {
    String predicatePrefix = getPredicatePrefix(getPredicate());
    return PREDICATE_PREFIXES_FOR_ERROR_MESSAGE_PART1.get(predicatePrefix) + " " + readablePropertyName();
  }

  public String getPredicateForErrorMessagePart2() {
    String predicatePrefix = getPredicatePrefix(getPredicate());
    return PREDICATE_PREFIXES_FOR_ERROR_MESSAGE_PART2.get(predicatePrefix);
  }

  public String getNegativePredicateForErrorMessagePart1() {
    String predicatePrefix = getPredicatePrefix(getNegativePredicate());
    return PREDICATE_PREFIXES_FOR_ERROR_MESSAGE_PART1.get(predicatePrefix) + " " + readablePropertyName();
  }

  public String getNegativePredicateForErrorMessagePart2() {
    String predicatePrefix = getPredicatePrefix(getNegativePredicate());
    return PREDICATE_PREFIXES_FOR_ERROR_MESSAGE_PART2.get(predicatePrefix);
  }

  private String readablePropertyName() {
    // sort by bigger length so that when cannotPlay is given, prefix found is "cannot" instead of "can"
    List<String> prefixesSortedByBiggerLength = BY_BIGGER_LENGTH_ORDERING.immutableSortedCopy(PREDICATE_PREFIXES_FOR_JAVADOC.keySet());

    for (String predicatePrefix : prefixesSortedByBiggerLength) {
      if (originalMember.getName().startsWith(predicatePrefix)) {
        // get rid of prefix
        String propertyName = removeStart(originalMember.getName(), predicatePrefix);
        // make it human readable
        return camelCaseToWords(propertyName);
      }
    }

    // should not arrive here ! return for best effort.
    return name;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    DataDescription that = (DataDescription) o;
    return Objects.equals(name, that.name) &&
           Objects.equals(originalMember, that.originalMember) &&
           Objects.equals(valueType, that.valueType);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, originalMember, valueType);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "[name=" + getName()
           + ", valueType=" + valueType
           + ", member=" + originalMember + "]";
  }

  public boolean isPublic() {
    return visibility == Visibility.PUBLIC;
  }

  int compareTo(final DataDescription other) {
    // Use the property name to remove duplicates
    int cmp = getName().compareTo(other.getName());
    if (cmp == 0) {
      cmp = getOriginalMember().getName().compareTo(other.getOriginalMember().getName());
    }

    return cmp;
  }

  private String owningTypePackageName() {
    return safePackageName(owningType);
  }

  private String removeOwningTypePackageNameIn(String value) {
    String owningTypePackageName = owningTypePackageName();
    return owningTypePackageName.isEmpty() ? value : removeAll(value, packageNameRegex(owningTypePackageName));
  }

}