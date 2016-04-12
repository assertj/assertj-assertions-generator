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
package org.assertj.assertions.generator.description;

import static org.apache.commons.lang3.StringUtils.removeStart;
import static org.assertj.assertions.generator.util.ClassUtil.getNegativePredicateFor;
import static org.assertj.assertions.generator.util.ClassUtil.getPredicatePrefix;
import static org.assertj.assertions.generator.util.StringUtil.camelCaseToWords;

import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Ints;

/**
 * base class to describe a field or a property/getter
 */
public abstract class DataDescription {

  protected final String name;
  protected final String originalMember;
  protected final TypeDescription typeDescription;

  public static final Map<String, String> PREDICATE_PREFIXES_FOR_JAVADOC =
      new ImmutableMap.Builder<String, String>().put("is", "is")
                                                .put("isNot", "is not")
                                                .put("was", "was")
                                                .put("wasNot", "was not")
                                                .put("can", "can")
                                                .put("cannot", "cannot")
                                                .put("should", "should")
                                                .put("shouldNot", "should not")
                                                .put("has", "has")
                                                .put("doesNotHave", "does not have")
                                                .put("will", "will")
                                                .put("willNot", "will not")
                                                .build();

  public static final Map<String, String> PREDICATE_PREFIXES_FOR_ERROR_MESSAGE_PART1 =
      new ImmutableMap.Builder<String, String>().put("is", "is")
                                                .put("isNot", "is not")
                                                .put("was", "was")
                                                .put("wasNot", "was not")
                                                .put("can", "can")
                                                .put("cannot", "cannot")
                                                .put("should", "should")
                                                .put("shouldNot", "should not")
                                                .put("has", "has")
                                                .put("doesNotHave", "does not have")
                                                .put("will", "will")
                                                .put("willNot", "will not")
                                                .build();

  public static final Map<String, String> PREDICATE_PREFIXES_FOR_ERROR_MESSAGE_PART2 =
      new ImmutableMap.Builder<String, String>().put("is", "is not")
                                                .put("isNot", "is")
                                                .put("was", "was not")
                                                .put("wasNot", "was")
                                                .put("can", "cannot")
                                                .put("cannot", "can")
                                                .put("should", "should not")
                                                .put("shouldNot", "should")
                                                .put("has", "does not have")
                                                .put("doesNotHave", "has")
                                                .put("will", "will not")
                                                .put("willNot", "will")
                                                .build();

  private static final Ordering<String> BY_BIGGER_LENGTH_ORDERING = new Ordering<String>() {
    @Override
    public int compare(String left, String right) {
      return -Ints.compare(left.length(), right.length());
    }
  };
  
  public DataDescription(String name, String originalMember, TypeDescription typeDescription) {
    super();
    this.name = name;
    this.originalMember = originalMember;
    this.typeDescription = typeDescription;
  }

  public String getName() {
    return name;
  }

  public String getOriginalMember() {
    return originalMember;
  }

  public TypeDescription getTypeDescription() {
    return typeDescription;
  }

  public String getTypeName() {
    return typeDescription.getSimpleNameWithOuterClass();
  }

  public boolean isIterableType() {
    return typeDescription.isIterable();
  }

  public boolean isArrayType() {
    return typeDescription.isArray();
  }

  public boolean isPrimitiveType() {
    return typeDescription.isPrimitive();
  }

  public boolean isRealNumberType() {
    return typeDescription.isRealNumber();
  }

  public boolean isWholeNumberType() {
    return typeDescription.isWholeNumber();
  }

  public boolean isCharType() {
    return typeDescription.isChar();
  }

  public boolean isPrimitiveWrapperType() {
    return typeDescription.isPrimitiveWrapper();
  }

  public abstract boolean isPredicate();

  public String getPredicate() {
    return originalMember;
  }

  public String getNegativePredicate() {
    return getNegativePredicateFor(originalMember);
  }

  /**
   * return the simple element type name if element type belongs to given the package and the fully qualified element
   * type name otherwise.
   * 
   * @param packageName typically the package of the enclosing Class
   * @return the simple element type name if element type belongs to given the package and the fully qualified element
   *         type name otherwise.
   */
  public String getElementTypeName(String packageName) {
    return typeDescription.getElementTypeName() == null ? null
        : typeDescription.getElementTypeName().getFullyQualifiedTypeNameIfNeeded(packageName);
  }

  public String getElementAssertTypeName(String packageName) {
    TypeName elementTypeName = typeDescription.getElementTypeName();
    return elementTypeName == null ? null
        : elementTypeName.getAssertTypeName(packageName);
  }

  public String getFullyQualifiedTypeNameIfNeeded(String packageName) {
    return typeDescription.getFullyQualifiedTypeNameIfNeeded(packageName);
  }

  public String getAssertTypeName(String packageName) {
    return typeDescription.getAssertTypeName(packageName);
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

  protected String readablePropertyName() {
    // sort by bigger length so that when cannotPlay is given, prefix found is "cannot" instead of "can"
    List<String> prefixesSortedByBiggerLength = BY_BIGGER_LENGTH_ORDERING.immutableSortedCopy(PREDICATE_PREFIXES_FOR_JAVADOC.keySet());

    for (String predicatePrefix : prefixesSortedByBiggerLength) {
      if (originalMember.startsWith(predicatePrefix)) {
        // get rid of prefix
        String propertyName = removeStart(originalMember, predicatePrefix);
        // make it human readable
        return camelCaseToWords(propertyName);
      }
    }
    // should not arrive here ! return for best effort.
    return name;
  }

}