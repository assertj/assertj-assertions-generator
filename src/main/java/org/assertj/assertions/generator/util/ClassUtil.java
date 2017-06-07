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
package org.assertj.assertions.generator.util;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;
import com.google.common.reflect.TypeToken;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.StrBuilder;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Sets.newLinkedHashSet;
import static java.lang.reflect.Modifier.isPublic;
import static java.lang.reflect.Modifier.isStatic;
import static org.apache.commons.lang3.StringUtils.indexOfAny;
import static org.apache.commons.lang3.StringUtils.remove;
import static org.apache.commons.lang3.StringUtils.uncapitalize;

/**
 * Some utilities methods related to classes and packages.
 */
@SuppressWarnings("WeakerAccess")
public class ClassUtil {

  public static final String GET_PREFIX = "get";
  private static final String CLASS_SUFFIX = ".class";
  private static final Comparator<Method> GETTER_COMPARATOR = new Comparator<Method>() {
    @Override
    public int compare(Method m1, Method m2) {
      return m1.getName().compareTo(m2.getName());
    }
  };
  private static final Package JAVA_LANG_PACKAGE = Object.class.getPackage();
  private static final String CAPITAL_LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

  /**
   * Call {@link #collectClasses(ClassLoader, String...)} with <code>Thread.currentThread().getContextClassLoader()
   * </code>
   */
  public static Set<TypeToken<?>> collectClasses(String... classOrPackageNames) {
    return collectClasses(Thread.currentThread().getContextClassLoader(), classOrPackageNames);
  }

  /**
   * Collects all the <b>public</b> classes from given classes names or classes belonging to given a package name
   * (recursively).
   * <p/>
   * Note that <b>anonymous</b> and <b>local</b> classes are excluded from the returned classes.
   *
   * @param classLoader {@link ClassLoader} used to load classes defines in classOrPackageNames
   * @param classOrPackageNames classes names or packages names we want to collect classes from (recursively for
   *          packages)
   * @return the set of {@link Class}es found
   * @throws RuntimeException if any error occurs
   */
  public static Set<TypeToken<?>> collectClasses(ClassLoader classLoader, String... classOrPackageNames) {
    Set<TypeToken<?>> classes = newLinkedHashSet();
    for (String classOrPackageName : classOrPackageNames) {
      TypeToken<?> clazz = tryToLoadClass(classOrPackageName, classLoader);
      if (clazz != null) {
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
      if (isClassCandidateToAssertionsGeneration(classFromJar)) {
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
    } catch (UnsupportedEncodingException encex) {
      throw new RuntimeException(packageName + " does not appear to be a valid package (Unsupported encoding)", encex);
    } catch (IOException ioex) {
      throw new RuntimeException("IOException was thrown when trying to get all classes for " + packageName, ioex);
    }
  }

  /**
   * Get <b>public</b> classes in given directory (recursively).
   * <p/>
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
          if (isClassCandidateToAssertionsGeneration(loadedClass)) {
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

  private static boolean isClassCandidateToAssertionsGeneration(TypeToken<?> typeToken) {
    if (typeToken == null) return false;
    Class<?> raw = typeToken.getRawType();
    return isPublic(raw.getModifiers())
           && !raw.isAnonymousClass()
           && !raw.isLocalClass();
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
   * <p/>
   *
   * <pre>
   * getName -> name
   * </pre>
   * <p/>
   *
   * <pre>
   * isMostValuablePlayer -> mostValuablePlayer
   * </pre>
   *
   * @param member getter method to deduce property from.
   * @return the property name of given getter method
   */
  public static String propertyNameOf(Member member) {
    String memberName = member.getName();
    String predicatePrefix = getPredicatePrefix(memberName);
    String prefixToRemove = predicatePrefix != null ? predicatePrefix : GET_PREFIX;

    int pos = memberName.indexOf(prefixToRemove);
    if (pos != StringUtils.INDEX_NOT_FOUND) {
      String propertyWithCapitalLetter = memberName.substring(pos + prefixToRemove.length());
      return uncapitalize(propertyWithCapitalLetter);
    } else {
      return memberName;
    }
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
           && (Boolean.TYPE.equals(method.getReturnType()) || Boolean.class.equals(method.getReturnType()))
           && method.getParameterTypes().length == 0;
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

  static private final Comparator<String> LONGEST_TO_SHORTEST = new Comparator<String>() {
    @Override
    public int compare(String o1, String o2) {
      final int lengthComp = o2.length() - o1.length();
      return lengthComp == 0 ? o1.compareTo(o2) : lengthComp;
    }
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

  public static List<Field> nonStaticPublicFieldsOf(TypeToken<?> type) {
    Field[] fields = type.getRawType().getFields();
    List<Field> nonStaticPublicFields = new ArrayList<>();
    for (Field field : fields) {
      if (isNotStaticPublicField(field)) {
        nonStaticPublicFields.add(field);
      }
    }
    return nonStaticPublicFields;
  }

  public static List<Field> declaredPublicFieldsOf(TypeToken<?> type) {
    Field[] fields = type.getRawType().getDeclaredFields();
    List<Field> nonStaticPublicFields = new ArrayList<>();
    for (Field field : fields) {
      if (isNotStaticPublicField(field)) {
        nonStaticPublicFields.add(field);
      }
    }
    return nonStaticPublicFields;
  }

  private static boolean isNotStaticPublicField(Field field) {
    final int modifiers = field.getModifiers();
    return !Modifier.isStatic(modifiers) && Modifier.isPublic(modifiers);
  }

  private static boolean isNotDefinedInObjectClass(Method method) {
    return !method.getDeclaringClass().equals(Object.class);
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

  /**
   * Gets the simple name of the class but, unlike {@link Class#getSimpleName()}, it includes the name of the outer
   * class when <code>clazz</code> is an inner class, both class names are concatenated.
   * <p>
   * Example:
   *
   * <pre>
   * Outer.Inner -> OuterInner
   * </pre>
   *
   * @param clazz the class
   * @return the simple name of the class prefixed by the outer class if any
   */
  public static String getSimpleNameWithOuterClassNotSeparatedByDots(Class<?> clazz) {
    if (isNotNestedClass(clazz)) {
      return clazz.getSimpleName();
    }
    String nestedClassName = clazz.getName();
    nestedClassName = nestedClassName.substring(clazz.getPackage().getName().length() + 1);
    nestedClassName = StringUtils.remove(nestedClassName, '$');
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
   *
   * @see #isInnerPackageOf(String, String) String argument equivalent
   */
  public static boolean isInnerPackageOf(Package child, Package parent) {
    return child != null && parent != null
           && child.getName().startsWith(parent.getName());
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
    Class<?> raw = type.getRawType();
    String basePackage = raw.getPackage() == null ? null : raw.getPackage().getName();
    return getTypeDeclaration(basePackage, type, asParameter, fullyQualified);
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
    return getTypeDeclaration(packageName, type, asParameter, reqFQN);
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
   * helper method for {@code #getTypeDeclarationXXX()}
   * @see #getTypeDeclaration(TypeToken, boolean, boolean)
   * @see #getTypeDeclarationWithinPackage(TypeToken, String, boolean)
   */
  private static String getTypeDeclaration(String basePackage, TypeToken<?> type, boolean asParameter, boolean fullyQualified) {

    if (type.isArray()) return getTypeDeclaration(basePackage, type.getComponentType(), asParameter, fullyQualified) + "[]";
    if (type.isPrimitive()) return type.getRawType().toString();

    Class<?> rawClass = type.getRawType();
    StringBuilder typeDeclaration = new StringBuilder("");
    // Now we have some types that could be generic, so we have to do more to serialize it to the declaration
    if (rawClass.isMemberClass()) {
      // inner class
      TypeToken<?> outerType = type.resolveType(rawClass.getEnclosingClass());
      typeDeclaration.append(getTypeDeclaration(basePackage, outerType, asParameter, fullyQualified))
                     .append(".");
    } else if (fullyQualified && !isJavaLangType(type)) {
      // it's a normal class but not in java.lang => add the package name
      typeDeclaration.append(type.getRawType().getPackage().getName())
                     .append(".");
    }

    typeDeclaration.append(rawClass.getSimpleName());

    if (isGeneric(type)) typeDeclaration.append(getGenericTypeDeclaration(basePackage, type, asParameter, fullyQualified));

    return typeDeclaration.toString();
  }

  private static boolean isGeneric(TypeToken<?> type) {
    return type.getRawType().getTypeParameters().length > 0;
  }

  private static String getGenericTypeDeclaration(String basePackage, TypeToken<?> type, boolean asParameter, boolean fullyQualified) {
    StringBuilder typeDeclaration = new StringBuilder("<");

    boolean first = true;
    for (TypeVariable typeParameterVariable : type.getRawType().getTypeParameters()) {
      if (!first) typeDeclaration.append(",");
      first = false;
      TypeToken<?> paramType = type.resolveType(typeParameterVariable);
      typeDeclaration.append(getParamTypeDeclaration(basePackage, asParameter, fullyQualified, paramType));
    }

    return typeDeclaration.append(">").toString();
  }

  private static String getParamTypeDeclaration(String basePackage, boolean asParameter, boolean fullyQualified, TypeToken<?> paramType) {
    Class<?> rawParam = paramType.getRawType();
    String typeString = StringUtils.removeAll(paramType.toString(), "capture#\\d+-of\\s+");
    typeString = typeString.replace("(\\?\\s+extends\\s+){2,}", "? extends ");

    boolean isWildCard = typeString.contains("?");
    // Some specializations need to be done to make sure that the arguments are property pulled out and written
    if (isWildCard && rawParam.equals(Object.class)) {
      // it's a wild card and without boundary other than Object => we just use the wild card
      return "?";
    }

    // We handle parameters differently so that it's accepted more "flexibility"
    String typeDeclaration = asParameter ? "? extends " : "";

    // now we recursively add the type parameter, we set `asParameter` to false
    // because odds are it will become wrong to keep adding the "extends" boundaries
    Package paramPackage = paramType.getRawType().getPackage();
    boolean notInSamePackage = paramPackage != null && !Objects.equals(basePackage, paramPackage.getName());
    return typeDeclaration + getTypeDeclaration(basePackage, paramType, false, fullyQualified || notInSamePackage);
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
    } else {
      return type;
    }
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
}
