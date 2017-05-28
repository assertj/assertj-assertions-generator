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

import static com.google.common.collect.Sets.newLinkedHashSet;
import static java.lang.reflect.Modifier.isPublic;
import static java.lang.reflect.Modifier.isStatic;
import static org.apache.commons.lang3.StringUtils.uncapitalize;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;

/**
 * Some utilities methods related to classes and packages.
 *
 * @author Joel Costigliola
 */
public class ClassUtil {

  public static final String IS_PREFIX = "is";
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
  public static Set<Class<?>> collectClasses(String... classOrPackageNames) {
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
  public static Set<Class<?>> collectClasses(ClassLoader classLoader, String... classOrPackageNames) {
    Set<Class<?>> classes = newLinkedHashSet();
    for (String classOrPackageName : classOrPackageNames) {
      Class<?> clazz = tryToLoadClass(classOrPackageName, classLoader);
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
  private static Set<Class<?>> getClassesInPackage(String packageName, ClassLoader classLoader) {
    if (classLoader == null) {
      throw new IllegalArgumentException("Null class loader.");
    }
    // load classes from classpath file system, this won't load classes in jars
    Set<Class<?>> packageClasses = getPackageClassesFromClasspathFiles(packageName, classLoader);
    // load classes from classpath jars
    try {
      packageClasses.addAll(getPackageClassesFromClasspathJars(packageName, classLoader));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return packageClasses;
  }

  private static Set<Class<?>> getPackageClassesFromClasspathJars(String packageName, ClassLoader classLoader)
      throws IOException {
    ImmutableSet<ClassInfo> classesInfo = ClassPath.from(classLoader).getTopLevelClassesRecursive(packageName);
    Set<Class<?>> classesInPackage = new HashSet<Class<?>>();
    for (ClassInfo classInfo : classesInfo) {
      classesInPackage.add(classInfo.load());
    }

    Set<Class<?>> filteredClassesInPackage = new HashSet<Class<?>>();
    for (Class<?> classFromJar : classesInPackage) {
      if (isClassCandidateToAssertionsGeneration(classFromJar)) {
        filteredClassesInPackage.add(classFromJar);
      }
    }
    return filteredClassesInPackage;
  }

  private static Set<Class<?>> getPackageClassesFromClasspathFiles(String packageName, ClassLoader classLoader) {
    try {
      String packagePath = packageName.replace('.', File.separatorChar);
      // Ask for all resources for the path
      Enumeration<URL> resources = classLoader.getResources(packagePath);
      Set<Class<?>> classes = newLinkedHashSet();
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
   * @return
   * @throws UnsupportedEncodingException
   */
  private static Set<Class<?>> getClassesInDirectory(File directory, String packageName, ClassLoader classLoader)
      throws UnsupportedEncodingException {
    Set<Class<?>> classes = newLinkedHashSet();
    // Capture all the .class files in this directory
    // Get the list of the files contained in the package
    File[] files = directory.listFiles();
    for (File currentFile : files) {
      String currentFileName = currentFile.getName();
      if (isClass(currentFileName)) {
        // CHECKSTYLE:OFF
        try {
          // removes the .class extension
          String className = packageName + '.' + StringUtils.remove(currentFileName, CLASS_SUFFIX);
          Class<?> loadedClass = loadClass(className, classLoader);
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
        URL resource = classLoader.getResource(subPackageName.replace('.', File.separatorChar));
        File subDirectory = new File(URLDecoder.decode(resource.getPath(), "UTF-8"));
        Set<Class<?>> classesForSubPackage = getClassesInDirectory(subDirectory, subPackageName, classLoader);
        classes.addAll(classesForSubPackage);
      }
    }
    return classes;
  }

  /**
   * @param loadedClass
   * @return
   */
  private static boolean isClassCandidateToAssertionsGeneration(Class<?> loadedClass) {
    return loadedClass != null && isPublic(loadedClass.getModifiers()) && !loadedClass.isAnonymousClass()
           && !loadedClass.isLocalClass();
  }

  private static boolean isClass(String fileName) {
    return fileName.endsWith(CLASS_SUFFIX);
  }

  private static Class<?> tryToLoadClass(String className, ClassLoader classLoader) {
    try {
      return loadClass(className, classLoader);
    } catch (ClassNotFoundException e) {
      return null;
    }
  }

  private static Class<?> loadClass(String className, ClassLoader classLoader) throws ClassNotFoundException {
    return Class.forName(className, false, classLoader);
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
   * @param getter getter method to deduce property from.
   * @return the property name of given getter method
   */
  public static String propertyNameOf(Method getter) {
    String methodName = getter.getName();
    String prefixToRemove = isPredicate(getter) ? IS_PREFIX : GET_PREFIX;
    int pos = methodName.indexOf(prefixToRemove);
    if (pos != StringUtils.INDEX_NOT_FOUND) {
      String propertyWithCapitalLetter = methodName.substring(pos + prefixToRemove.length());
      return uncapitalize(propertyWithCapitalLetter);
    } else {
      return methodName;
    }
  }

  public static boolean inheritsCollectionOrIsIterable(Class<?> returnType) {
    return Collection.class.isAssignableFrom(returnType) || Iterable.class.equals(returnType);
  }

  public static boolean isArray(Class<?> returnType) {
    return returnType.isArray();
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
        { "should", "shouldNot" },
        { "has", "doesNotHave" },
        { "will", "willNot" },
    };
    StringBuilder pattern = new StringBuilder("^(?:get");
    Map<String, String> map = new HashMap<String, String>();
    for (String[] pair : predicates) {
      map.put(pair[0], pair[1]);
      map.put(pair[1], pair[0]);
    }
    TreeSet<String> sort = new TreeSet<String>(LONGEST_TO_SHORTEST);
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

  public static Set<Method> declaredGetterMethodsOf(Class<?> clazz, Set<Class<?>> includeAnnotations) {
    boolean isClassAnnotated = containsAny(clazz.getDeclaredAnnotations(), includeAnnotations);
    return filterGetterMethods(clazz.getDeclaredMethods(), includeAnnotations, isClassAnnotated);
  }

  public static Set<Method> getterMethodsOf(Class<?> clazz, Set<Class<?>> includeAnnotations) {
    boolean isClassAnnotated = containsAny(clazz.getDeclaredAnnotations(), includeAnnotations);
    return filterGetterMethods(clazz.getMethods(), includeAnnotations, isClassAnnotated);
  }

  private static Set<Method> filterGetterMethods(Method[] methods, Set<Class<?>> includeAnnotations,
                                                 boolean isClassAnnotated) {
    Set<Method> getters = new TreeSet<Method>(GETTER_COMPARATOR);
    for (int i = 0; i < methods.length; i++) {
      Method method = methods[i];
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

  public static List<Field> nonStaticPublicFieldsOf(Class<?> clazz) {
    Field[] fields = clazz.getFields();
    List<Field> nonStaticPublicFields = new ArrayList<Field>();
    for (Field field : fields) {
      if (isNotStaticPublicField(field)) {
        nonStaticPublicFields.add(field);
      }
    }
    return nonStaticPublicFields;
  }

  public static List<Field> declaredPublicFieldsOf(Class<?> clazz) {
    Field[] fields = clazz.getDeclaredFields();
    List<Field> nonStaticPublicFields = new ArrayList<Field>();
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
    Set<Class<?>> classes = new HashSet<Class<?>>();

    // non generic type : just add current type.
    if (type instanceof Class) {
      classes.add((Class<?>) type);
      return classes;
    }

    // generic type : add current type and its parameter types
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
    String nestedClassName = null;
    nestedClassName = clazz.getName();
    nestedClassName = nestedClassName.substring(clazz.getPackage().getName().length() + 1);
    nestedClassName = nestedClassName.replace('$', '.');
    return nestedClassName;
  }

  /**
   * Gets the simple name of the outer class, if the class is not nested then this is the same as
   * {@link Class#getSimpleName()}.
   * <p>
   * Example:
   *
   *  <pre>
   *    OnlyOuter -> OnlyOuter
   *    Outer.Inner -> Outer
   *    Outer.Inner1.Inner2 -> Outer
   *  </pre>
   * @param clazz for which the outer class name should be found
   * @return see description
   */
  public static String getSimpleNameOuterClass(Class<?> clazz) {
    if (isNotNestedClass(clazz)) {
      return clazz.getSimpleName();
    }
    String outerClassName = clazz.getName();
    outerClassName = outerClassName.substring(clazz.getPackage().getName().length() + 1);
    outerClassName = outerClassName.substring(0, outerClassName.indexOf('$'));
    return outerClassName;
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
    String nestedClassName = null;
    nestedClassName = clazz.getName();
    nestedClassName = nestedClassName.substring(clazz.getPackage().getName().length() + 1);
    nestedClassName = StringUtils.remove(nestedClassName, '$');
    return nestedClassName;
  }

  private static boolean isNotNestedClass(Class<?> clazz) {
    return clazz.getDeclaringClass() == null;
  }

  /**
   * Get the underlying class for a type, or null if the type is a variable type.
   *
   * @param type the type
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
