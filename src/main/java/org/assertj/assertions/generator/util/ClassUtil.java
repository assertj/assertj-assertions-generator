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
 * Copyright 2012-2021 the original author or authors.
 */
package org.assertj.assertions.generator.util;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newLinkedHashSet;
import static java.lang.reflect.Modifier.isPublic;
import static java.lang.reflect.Modifier.isStatic;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.RegExUtils.removeAll;
import static org.apache.commons.lang3.StringUtils.indexOfAny;
import static org.apache.commons.lang3.StringUtils.remove;
import static org.apache.commons.lang3.StringUtils.removeStart;
import static org.apache.commons.lang3.StringUtils.uncapitalize;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.assertj.assertions.generator.description.Visibility;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;
import com.google.common.reflect.TypeToken;

/**
 * Some utilities methods related to classes and packages.
 */
public class ClassUtil {

  public static final String GET_PREFIX = "get";
  private static final String CLASS_SUFFIX = ".class";
  private static final Comparator<Method> GETTER_COMPARATOR = Comparator.comparing(Method::getName);
  public static final Package JAVA_LANG_PACKAGE = Object.class.getPackage();
  private static final String CAPITAL_LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

  /**
   * Call {@link #collectClasses(ClassLoader, String...)} with <code>Thread.currentThread().getContextClassLoader()
   * </code>
   * @param classOrPackageNames classes or packages to collect.
   * @return the set of {@link TypeToken}s found
   */
  public static Set<TypeToken<?>> collectClasses(String... classOrPackageNames) {
    return collectClasses(Thread.currentThread().getContextClassLoader(), classOrPackageNames);
  }

  /**
   * Collects all the <b>public</b> classes from given classes names or classes belonging to given a package name
   * (recursively).
   *
   * Note that <b>anonymous</b> and <b>local</b> classes are excluded from the returned classes.
   *
   * @param classLoader {@link ClassLoader} used to load classes defines in classOrPackageNames
   * @param classOrPackageNames classes names or packages names we want to collect classes from (recursively for
   *          packages)
   * @return the set of {@link Class}es found
   * @throws RuntimeException if any error occurs
   */
  public static Set<TypeToken<?>> collectClasses(ClassLoader classLoader, String... classOrPackageNames) {
    return collectClasses(classLoader, false, classOrPackageNames);
  }

  /**
   * Collects all the classes from given classes names or classes belonging to given a package name
   * (recursively), with control on private classes including.
   *
   * Note that <b>anonymous</b> and <b>local</b> classes are excluded from the returned classes.
   *
   * @param classLoader {@link ClassLoader} used to load classes defines in classOrPackageNames
   * @param includePrivateClasses {@link ClassLoader} used to include private classes when true
   * @param classOrPackageNames classes names or packages names we want to collect classes from (recursively for
   *          packages)
   * @return the set of {@link Class}es found
   * @throws RuntimeException if any error occurs
   */
  public static Set<TypeToken<?>> collectClasses(ClassLoader classLoader, boolean includePrivateClasses,
                                                 String... classOrPackageNames) {
    Set<TypeToken<?>> classes = newLinkedHashSet();
    for (String classOrPackageName : classOrPackageNames) {
      TypeToken<?> clazz = tryToLoadClass(classOrPackageName, classLoader);
      if (isClassCandidateToAssertionsGeneration(clazz, includePrivateClasses)) {
        classes.add(clazz);
      } else {
        // should be a package
        classes.addAll(getClassesInPackage(classOrPackageName, classLoader));
      }
    }
    return classes;
  }

  /**
   * Retrieves recursively all the classes belonging to a package.
   *
   * @param packageName package name we want to load classes from
   * @param classLoader the class loader used to load the classes in the given package
   * @return the list of Class found
   * @throws RuntimeException if any error occurs
   */
  private static Set<TypeToken<?>> getClassesInPackage(String packageName, ClassLoader classLoader) {
    if (classLoader == null) {
      throw new IllegalArgumentException("Null class loader.");
    }
    // load classes from classpath file system, this won't load classes in jars
    Set<TypeToken<?>> packageClasses = getPackageClassesFromClasspathFiles(packageName, classLoader);
    // load classes from classpath jars
    try {
      packageClasses.addAll(getPackageClassesFromClasspathJars(packageName, classLoader));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return packageClasses;
  }

  private static Set<TypeToken<?>> getPackageClassesFromClasspathJars(String packageName, ClassLoader classLoader)
                                                                                                                   throws IOException {
    ImmutableSet<ClassInfo> classesInfo = ClassPath.from(classLoader).getTopLevelClassesRecursive(packageName);
    Set<TypeToken<?>> classesInPackage = new HashSet<>();
    for (ClassInfo classInfo : classesInfo) {
      classesInPackage.add(TypeToken.of(classInfo.load()));
    }

    Set<TypeToken<?>> filteredClassesInPackage = new HashSet<>();
    for (TypeToken<?> classFromJar : classesInPackage) {
      if (isClassCandidateToAssertionsGeneration(classFromJar, false)) {
        filteredClassesInPackage.add(classFromJar);
      }
    }
    return filteredClassesInPackage;
  }

  private static Set<TypeToken<?>> getPackageClassesFromClasspathFiles(String packageName, ClassLoader classLoader) {
    try {
      String packagePath = packageName.replace('.', File.separatorChar);
      // Ask for all resources for the path
      Enumeration<URL> resources = classLoader.getResources(packagePath);
      Set<TypeToken<?>> classes = newLinkedHashSet();
      while (resources.hasMoreElements()) {
        File directory = new File(URLDecoder.decode(resources.nextElement().getPath(), "UTF-8"));
        if (directory.canRead()) {
          classes.addAll(getClassesInDirectory(directory, packageName, classLoader));
        }
      }
      return classes;
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(packageName + " does not appear to be a valid package (Unsupported encoding)", e);
    } catch (IOException ioException) {
      throw new RuntimeException("IOException was thrown when trying to get all classes for " + packageName,
                                 ioException);
    }
  }

  /**
   * Get <b>public</b> classes in given directory (recursively).
   *
   * Note that <b>anonymous</b> and <b>local</b> classes are excluded from the resulting set.
   *
   * @param directory directory where to look for classes
   * @param packageName package name corresponding to directory
   * @param classLoader used classloader
   * @return Set of all of the types in the directory
   * @throws UnsupportedEncodingException thrown by {@link URLDecoder#decode(String, String)}
   */
  private static Set<TypeToken<?>> getClassesInDirectory(File directory, String packageName, ClassLoader classLoader)
                                                                                                                      throws UnsupportedEncodingException {
    Set<TypeToken<?>> classes = new LinkedHashSet<>();

    // Capture all the .class files in this directory
    // Get the list of the files contained in the package
    File[] files = directory.listFiles();
    checkNotNull(files, "No files were present in directory: %s", directory);
    for (File currentFile : files) {
      String currentFileName = currentFile.getName();
      if (isClass(currentFileName)) {
        // CHECKSTYLE:OFF
        try {
          // removes the .class extension
          String className = packageName + '.' + StringUtils.remove(currentFileName, CLASS_SUFFIX);
          TypeToken<?> loadedClass = loadClass(className, classLoader);
          // we are only interested in public classes that are neither anonymous nor local
          if (isClassCandidateToAssertionsGeneration(loadedClass, false)) {
            classes.add(loadedClass);
          }
        } catch (Throwable e) {
          // do nothing. this class hasn't been found by the loader, and we don't care.
        }
        // CHECKSTYLE:ON
      } else if (currentFile.isDirectory()) {
        // It's another package
        String subPackageName = packageName + ClassUtils.PACKAGE_SEPARATOR + currentFileName;
        // Ask for all resources for the path
        String path = subPackageName.replace('.', File.separatorChar);
        URL resource = classLoader.getResource(path);
        checkNotNull(resource, "resource URL from package is null, package %s", path);
        File subDirectory = new File(URLDecoder.decode(resource.getPath(), "UTF-8"));
        Set<TypeToken<?>> classesForSubPackage = getClassesInDirectory(subDirectory, subPackageName, classLoader);
        classes.addAll(classesForSubPackage);
      }
    }
    return classes;
  }

  private static boolean isClassCandidateToAssertionsGeneration(TypeToken<?> typeToken, boolean includePrivate) {
    if (typeToken == null) return false;
    if (isPackageInfo(typeToken)) return false;
    Class<?> raw = typeToken.getRawType();
    return (includePrivate || isPublic(raw.getModifiers()))
           && !raw.isAnonymousClass()
           && !raw.isLocalClass();
  }

  private static boolean isPackageInfo(TypeToken<?> typeToken) {
    return typeToken.getRawType().getName().contains("package-info");
  }

  private static boolean isClass(String fileName) {
    return fileName.endsWith(CLASS_SUFFIX);
  }

  private static TypeToken<?> tryToLoadClass(String className, ClassLoader classLoader) {
    try {
      return loadClass(className, classLoader);
    } catch (ClassNotFoundException e) {
      return null;
    }
  }

  private static TypeToken<?> loadClass(String className, ClassLoader classLoader) throws ClassNotFoundException {
    return TypeToken.of(Class.forName(className, false, classLoader));
  }

  /**
   * Returns the property name of given getter method, examples :
   *
   *
   * <pre>
   * getName() -&gt; name
   * </pre>
   *
   *
   * <pre>
   * isMostValuablePlayer() -&gt; mostValuablePlayer
   * </pre>
   *
   * @param method getter method to deduce property from.
   * @return the property name of given getter method
   */
  public static String propertyNameOf(Method method) {
    String methodName = method.getName();
    return isPredicate(method) ? booleanPropertyOf(methodName) : getterProperty(methodName);
  }

  /**
   * Returns the property name of given field, examples :
   *
   * <pre>
   * name -&gt; name
   * isMostValuablePlayer -&gt; mostValuablePlayer
   * </pre>
   *
   * @param field field to deduce property from.
   * @return the property name of given field
   */
  public static String propertyNameOf(Field field) {
    String fieldName = field.getName();
    return isBoolean(field.getType()) ? booleanPropertyOf(fieldName) : fieldName;
  }

  public static boolean inheritsCollectionOrIsIterable(Class<?> returnType) {
    return Collection.class.isAssignableFrom(returnType) || Iterable.class.equals(returnType);
  }

  public static boolean isStandardGetter(Method method) {
    return isValidStandardGetterName(method.getName())
           && !Void.TYPE.equals(method.getReturnType())
           && method.getParameterTypes().length == 0;
  }

  public static boolean isPredicate(Method method) {
    return isValidPredicateName(method.getName())
           && isBoolean(method.getReturnType())
           && method.getParameterTypes().length == 0;
  }

  private static boolean isBoolean(Class<?> type) {
    return Boolean.TYPE.equals(type) || Boolean.class.equals(type);
  }

  private static boolean isAnnotated(Method method, Set<Class<?>> includeAnnotations, boolean isClassAnnotated) {
    if (!Void.TYPE.equals(method.getReturnType())
        && method.getParameterTypes().length == 0
        && !isStatic(method.getModifiers())) {
      Annotation[] methodAnnotations = method.getAnnotations();
      return isClassAnnotated || containsAny(methodAnnotations, includeAnnotations);
    }
    return false;
  }

  private static boolean containsAny(Annotation[] methodAnnotations, Set<Class<?>> includeAnnotations) {
    for (Annotation annotation : methodAnnotations) {
      if (includeAnnotations.contains(annotation.annotationType())) {
        return true;
      }
    }
    return false;
  }

  public static boolean isValidGetterName(String methodName) {
    return PREFIX_PATTERN.matcher(methodName).find();
  }

  static private final Pattern PREFIX_PATTERN;

  static public final Map<String, String> PREDICATE_PREFIXES;

  static private final Comparator<String> LONGEST_TO_SHORTEST = (o1, o2) -> {
    final int lengthComp = o2.length() - o1.length();
    return lengthComp == 0 ? o1.compareTo(o2) : lengthComp;
  };

  static {
    String[][] predicates = {
        { "is", "isNot" },
        { "was", "wasNot" },
        { "can", "cannot" },
        { "canBe", "cannotBe" },
        { "should", "shouldNot" },
        { "shouldBe", "shouldNotBe" },
        { "has", "doesNotHave" },
        { "willBe", "willNotBe" },
        { "will", "willNot" },
    };
    StringBuilder pattern = new StringBuilder("^(?:get");
    Map<String, String> map = new HashMap<>();
    for (String[] pair : predicates) {
      map.put(pair[0], pair[1]);
      map.put(pair[1], pair[0]);
    }
    TreeSet<String> sort = new TreeSet<>(LONGEST_TO_SHORTEST);
    sort.addAll(map.keySet());
    for (String prefix : sort) {
      pattern.append('|').append(prefix);
    }
    // next should be an Upper case letter
    pattern.append(")(?=\\p{Upper})");
    PREFIX_PATTERN = Pattern.compile(pattern.toString());
    PREDICATE_PREFIXES = Collections.unmodifiableMap(map);
  }

  private static boolean isValidStandardGetterName(String name) {
    Matcher m = PREFIX_PATTERN.matcher(name);
    return m.find() && m.group().equals(GET_PREFIX);
  }

  public static String getPredicatePrefix(String name) {
    Matcher m = PREFIX_PATTERN.matcher(name);
    return m.find() ? m.group() : null;
  }

  public static boolean isValidPredicateName(String name) {
    Matcher m = PREFIX_PATTERN.matcher(name);
    return m.find() && PREDICATE_PREFIXES.containsKey(m.group());
  }

  public static String getNegativePredicateFor(String name) {
    Matcher m = PREFIX_PATTERN.matcher(name);
    if (m.find()) {
      return m.replaceFirst(PREDICATE_PREFIXES.get(m.group()));
    }
    return null;
  }

  public static Set<Method> declaredGetterMethodsOf(TypeToken<?> type, Set<Class<?>> includeAnnotations) {
    Class<?> clazz = type.getRawType();
    boolean isClassAnnotated = containsAny(clazz.getDeclaredAnnotations(), includeAnnotations);
    return filterGetterMethods(clazz.getDeclaredMethods(), includeAnnotations, isClassAnnotated);
  }

  public static Set<Method> getterMethodsOf(TypeToken<?> type, Set<Class<?>> includeAnnotations) {
    Class<?> clazz = type.getRawType();
    boolean isClassAnnotated = containsAny(clazz.getDeclaredAnnotations(), includeAnnotations);
    return filterGetterMethods(clazz.getMethods(), includeAnnotations, isClassAnnotated);
  }

  private static Set<Method> filterGetterMethods(Method[] methods, Set<Class<?>> includeAnnotations,
                                                 boolean isClassAnnotated) {
    Set<Method> getters = new TreeSet<>(GETTER_COMPARATOR);
    for (Method method : methods) {
      if (isPublic(method.getModifiers())
          && isNotDefinedInObjectClass(method)
          && isGetter(method, includeAnnotations, isClassAnnotated)) {
        getters.add(method);
      }
    }
    return getters;
  }

  private static boolean isGetter(Method method, Set<Class<?>> includeAnnotations, boolean isClassAnnotated) {
    return isStandardGetter(method)
           || isPredicate(method)
           || isAnnotated(method, includeAnnotations, isClassAnnotated);
  }

  public static List<Field> nonStaticFieldsOf(TypeToken<?> clazz) {
    List<Field> fields = getAllFieldsInHierarchy(clazz);
    return filterNonStaticFields(fields);
  }

  private static List<Field> filterNonStaticFields(List<Field> fields) {
    List<Field> nonStaticFields = new ArrayList<>();
    for (Field field : fields) {
      if (isNotStaticField(field)) {
        nonStaticFields.add(field);
      }
    }
    return nonStaticFields;
  }

  public static List<Field> declaredFieldsOf(TypeToken<?> type) {
    Field[] fields = type.getRawType().getDeclaredFields();
    return filterNonStaticFields(asList(fields));
  }

  /**
   * Retrieves all fields (whatever access levels) in the hierarchy of a class up to Object.class excluded.
   *
   * @param clazz the class whose fields should be retrieved
   * @return all fields (whatever access levels) in the hierarchy of a class up to Object.class excluded.
   */
  public static List<Field> getAllFieldsInHierarchy(TypeToken<?> clazz) {
    List<Field> fields = newArrayList(clazz.getRawType().getDeclaredFields());
    Class<?> parentClass = clazz.getRawType().getSuperclass();
    if (parentClass != null && !Object.class.equals(parentClass)) {
      fields.addAll(getAllFieldsInHierarchy(TypeToken.of(parentClass)));
    }
    return fields;
  }

  private static boolean isNotStaticField(Field field) {
    return !Modifier.isStatic(field.getModifiers());
  }

  private static boolean isNotDefinedInObjectClass(Method method) {
    return !Object.class.equals(method.getDeclaringClass());
  }

  public static Set<Class<?>> getClassesRelatedTo(Type type) {
    Set<Class<?>> classes = new HashSet<>();

    // non generic valueType : just add current valueType.
    if (type instanceof Class) {
      classes.add((Class<?>) type);
      return classes;
    }

    // generic valueType : add current valueType and its parameter types
    if (type instanceof ParameterizedType) {
      ParameterizedType parameterizedType = (ParameterizedType) type;
      for (Type actualTypeArgument : parameterizedType.getActualTypeArguments()) {
        if (actualTypeArgument instanceof ParameterizedType) {
          classes.addAll(getClassesRelatedTo(actualTypeArgument));
        } else if (actualTypeArgument instanceof Class) {
          classes.add((Class<?>) actualTypeArgument);
        } else if (actualTypeArgument instanceof GenericArrayType) {
          classes.addAll(getClassesRelatedTo(actualTypeArgument));
        }
      }
      Type rawType = parameterizedType.getRawType();
      if (rawType instanceof Class) {
        classes.add((Class<?>) rawType);
      }
    }
    return classes;
  }

  /**
   * Gets the simple name of the class but, unlike {@link Class#getSimpleName()}, it includes the name of the outer
   * class when <code>clazz</code> is an inner class.
   *
   * @param clazz the class
   * @return the simple name of the class prefixed by the outer class if any (separated by dot)
   */
  public static String getSimpleNameWithOuterClass(Class<?> clazz) {
    if (isNotNestedClass(clazz)) {
      return clazz.getSimpleName();
    }
    String nestedClassName = clazz.getName();
    nestedClassName = nestedClassName.substring(clazz.getPackage().getName().length() + 1);
    nestedClassName = nestedClassName.replace('$', '.');
    return nestedClassName;
  }

  private static boolean isNotNestedClass(Class<?> clazz) {
    return clazz.getDeclaringClass() == null;
  }

  /**
   * Get the underlying class for a valueType, or null if the valueType is a variable valueType.
   *
   * @param type the valueType
   * @return the underlying class
   */
  public static Class<?> getClass(final Type type) {
    if (type instanceof Class) return (Class<?>) type;
    if (type instanceof ParameterizedType) return getClass(((ParameterizedType) type).getRawType());

    if (type instanceof GenericArrayType) {
      final Type componentType = ((GenericArrayType) type).getGenericComponentType();
      final Class<?> componentClass = getClass(componentType);
      return componentClass == null ? null : Array.newInstance(componentClass, 0).getClass();
    } else if (type instanceof WildcardType) {
      final WildcardType wildcardType = (WildcardType) type;
      return wildcardType.getUpperBounds() != null ? getClass(wildcardType.getUpperBounds()[0])
          : wildcardType.getLowerBounds() != null ? getClass(wildcardType.getLowerBounds()[0]) : null;
    } else if (type instanceof TypeVariable) {
      final TypeVariable<?> typeVariable = (TypeVariable<?>) type;
      final Type[] bounds = typeVariable.getBounds();
      return bounds.length > 0 ? getClass(bounds[0]) : Object.class;
    }
    return null;
  }

  /**
   * Checks if the package, {@code child} is under the package {@code parent}.
   * @param child Child package
   * @param parent Parent package
   * @return True iff the child package is under the parent, false if not or either is {@code null}.
   */
  public static boolean isInnerPackageOf(Package child, Package parent) {
    return child != null && parent != null
           && child.getName().startsWith(parent.getName());
  }

  /**
   * Checks if the type passed is a member of {@code java.lang} or is a "built-in" type (e.g. primitive or array).
   * @param type type token
   * @return true if part of java language
   */
  public static boolean isJavaLangType(TypeToken<?> type) {
    return type.isPrimitive() || type.isArray() || Objects.equals(JAVA_LANG_PACKAGE, type.getRawType().getPackage());
  }

  /**
   * Checks if the type passed is a member of {@code java.lang} or is a "built-in" type (e.g. primitive or array).
   * @return true if part of java language
   *
   * @param type type
   * @see #isJavaLangType(TypeToken)
   */
  public static boolean isJavaLangType(Type type) {
    return isJavaLangType(TypeToken.of(type));
  }

  public static String packageOf(String fullyQualifiedType) {
    int indexOfClassName = indexOfAny(fullyQualifiedType, CAPITAL_LETTERS);
    if (indexOfClassName > 0) {
      return fullyQualifiedType.substring(0, indexOfClassName - 1);
    }
    // primitive valueType => no package
    return "";
  }

  /**
   * Generates a "type declaration" that could be used in Java code based on the {@code type}.
   *
   * @param type Type to get declaration for
   * @return String representation of the type
   */
  public static String getTypeDeclaration(TypeToken<?> type) {

    if (type.isArray()) return getTypeDeclaration(type.getComponentType()) + "[]";
    if (type.isPrimitive()) return type.getRawType().toString();

    Class<?> rawClass = type.getRawType();
    StringBuilder typeDeclaration = new StringBuilder("");
    // Now we have some types that could be generic, so we have to do more to serialize it to the declaration
    if (rawClass.isMemberClass()) {
      // inner class
      TypeToken<?> outerType = type.resolveType(rawClass.getEnclosingClass());
      typeDeclaration.append(getTypeDeclaration(outerType))
                     .append(".")
                     .append(rawClass.getSimpleName());
    } else if (type.getType() instanceof TypeVariable) {
      // used to get generic type parameter real name (ex T instead of having Object)
      // TODO: should we do a recursive type inference ?
      @SuppressWarnings("unchecked")
      TypeVariable<GenericDeclaration> typeVariable = (TypeVariable<GenericDeclaration>) type.getType();
      String name = typeVariable.getName();
      name = removeAll(name, "capture#\\d+-of\\s+");
      name = removeAll(name, " class");
      name = removeAll(name, " interface");
      typeDeclaration.append(name);
    } else if (!isJavaLangType(type)) {
      // it's a normal class but not in java.lang => add the package name
      typeDeclaration.append(type.getRawType().getPackage().getName())
                     .append(".")
                     .append(rawClass.getSimpleName());
    } else {
      typeDeclaration.append(rawClass.getSimpleName());
    }

    return typeDeclaration.toString();
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
        // try to get the class, if it exists then we know it's valid
        Class.forName(builtInName);

        typeName = builtInName.substring(0, builtInName.length() - "Assert".length());
      } catch (ClassNotFoundException e) {
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
    }
    return type;
  }

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

  public static String safePackageName(TypeToken<?> typeToken) {
    return typeToken.getRawType().getPackage() == null ? "" : typeToken.getRawType().getPackage().getName();
  }

  public static String packageNameRegex(String packageName) {
    return Pattern.quote(packageName + ".") + "(?=[A-Z])";
  }

  public static Visibility visibilityOf(Field field) {
    int fieldModifiers = field.getModifiers();
    if (isPublic(fieldModifiers)) return Visibility.PUBLIC;
    if (Modifier.isProtected(fieldModifiers)) return Visibility.PROTECTED;
    if (Modifier.isPrivate(fieldModifiers)) return Visibility.PRIVATE;
    return Visibility.PACKAGE;
  }

  private static String booleanPropertyOf(String memberName) {
    String prefixToRemove = getPredicatePrefix(memberName);
    if (prefixToRemove != null && memberName.startsWith(prefixToRemove)) {
      String propertyWithCapitalLetter = removeStart(memberName, prefixToRemove);
      return uncapitalize(propertyWithCapitalLetter);
    }
    return memberName;
  }

  private static String getterProperty(String memberName) {
    if (memberName.startsWith(GET_PREFIX)) {
      String propertyWithCapitalLetter = removeStart(memberName, GET_PREFIX);
      return uncapitalize(propertyWithCapitalLetter);
    }
    return memberName;
  }

}
