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

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;
import com.google.common.reflect.TypeToken;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;

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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Sets.newLinkedHashSet;
import static java.lang.reflect.Modifier.isPublic;
import static java.lang.reflect.Modifier.isStatic;
import static org.apache.commons.lang3.StringUtils.uncapitalize;

/**
 * Some utilities methods related to classes and packages.
 *
 * @author Joel Costigliola
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

  /**
   * @param loadedClass
   * @return
   */
  private static boolean isClassCandidateToAssertionsGeneration(TypeToken<?> loadedClass) {
    if (loadedClass == null) {
      return false;
    }

    Class<?> raw = loadedClass.getRawType();
    return isPublic(raw.getModifiers()) && !raw.isAnonymousClass() && !raw.isLocalClass();
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

  static private final Map<String, String> PREDICATE_PREFIXES;

  static private final Comparator<String> LONGEST_TO_SHORTEST = new Comparator<String>() {
    @Override
    public int compare(String o1, String o2) {
      final int lengthComp = o2.length() - o1.length();
      return lengthComp == 0 ? o1.compareTo(o2) : lengthComp;
    }
  };

  static {
    String[][] predicates = new String[][] {
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
   * @param clazz
   * @return
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
   * @param clazz
   * @return
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

}
