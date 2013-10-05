package org.assertj.assertions.generator.util;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;

import static java.lang.Character.isUpperCase;
import static java.lang.reflect.Modifier.isPublic;
import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.apache.commons.lang3.StringUtils.uncapitalize;

/**
 * 
 * Some utilities methods related to classes and packages.
 * 
 * @author Joel Costigliola
 * 
 */
public class ClassUtil {

  private static final String CLASS_SUFFIX = ".class";
  public static final String IS_PREFIX = "is";
  public static final String GET_PREFIX = "get";

  /**
   * Call {@link #collectClasses(ClassLoader, String...)} with
   * <code>Thread.currentThread().getContextClassLoader()</code>
   */
  public static List<Class<?>> collectClasses(String... classOrPackageNames) throws ClassNotFoundException {
    return collectClasses(Thread.currentThread().getContextClassLoader(), classOrPackageNames);
  }

  /**
   * Collects all the <b>public</b> classes from given classes names or classes belonging to given a package name
   * (recursively).
   * <p>
   * Note that <b>anonymous</b> and <b>local</b> classes are excluded from the resulting list.
   * 
   * @param classLoader {@link ClassLoader} used to load classes defines in classOrPackageNames
   * @param classOrPackageNames classes names or packages names we want to collect classes from (recursively for
   *          packages)
   * @return the list of {@link Class}es found
   * @throws RuntimeException if any error occurs
   */
  public static List<Class<?>> collectClasses(ClassLoader classLoader, String... classOrPackageNames) {
    List<Class<?>> classes = new ArrayList<Class<?>>();
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
    Set<Class<?>> packageClassesFromClasspathJars = getPackageClassesFromClasspathJars(packageName, classLoader);
    packageClasses.addAll(packageClassesFromClasspathJars);
    return packageClasses;
  }

  private static Set<Class<?>> getPackageClassesFromClasspathJars(String packageName, ClassLoader classLoader) {
    List<ClassLoader> classLoadersList = new LinkedList<ClassLoader>();
    classLoadersList.add(ClasspathHelper.contextClassLoader());
    classLoadersList.add(ClasspathHelper.staticClassLoader());
    classLoadersList.add(classLoader);

    Reflections reflections = new Reflections(
                                              new ConfigurationBuilder()
                                                                        .setScanners(new SubTypesScanner(false /*
                                                                                                                * don't
                                                                                                                * exclude
                                                                                                                * Object
                                                                                                                * .class
                                                                                                                */),
                                                                                     new ResourcesScanner())
                                                                        .setUrls(ClasspathHelper.forClassLoader(classLoadersList.toArray(new ClassLoader[0])))
                                                                        .filterInputsBy(new FilterBuilder().include(FilterBuilder.prefix(packageName))));
    Set<Class<?>> classesInPackage = reflections.getSubTypesOf(Object.class);
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
      Set<Class<?>> classes = new HashSet<Class<?>>();
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
   * <p>
   * Note that <b>anonymous</b> and <b>local</b> classes are excluded from the resulting list.
   * 
   * @param directory directory where to look for classes
   * @param packageName package name corresponding to directory
   * @param classLoader used classloader
   * @return
   * @throws UnsupportedEncodingException
   */
  private static List<Class<?>> getClassesInDirectory(File directory, String packageName, ClassLoader classLoader)
      throws UnsupportedEncodingException {
    List<Class<?>> classes = new ArrayList<Class<?>>();
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
        List<Class<?>> classesForSubPackage = getClassesInDirectory(subDirectory, subPackageName, classLoader);
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
    return Class.forName(className, true, classLoader);
  }

  /**
   * Returns the property name of given getter method, examples :
   * 
   * <pre>
   * getName -> name
   * </pre>
   * 
   * <pre>
   * isMostValuablePlayer -> mostValuablePlayer
   * </pre>
   * 
   * @param getter getter method to deduce property from.
   * @return the property name of given getter method
   */
  public static String propertyNameOf(Method getter) {
    String prefixToRemove = isBooleanGetter(getter) ? IS_PREFIX : GET_PREFIX;
    String propertyWithCapitalLetter = substringAfter(getter.getName(), prefixToRemove);
    return uncapitalize(propertyWithCapitalLetter);
  }

  public static boolean isIterable(Class<?> returnType) {
    return Iterable.class.isAssignableFrom(returnType);
  }

  public static boolean isArray(Class<?> returnType) {
    return returnType.isArray();
  }

  public static boolean isStandardGetter(Method method) {
    return isValidStandardGetterName(method.getName())
        && !Void.TYPE.equals(method.getReturnType()) 
        && method.getParameterTypes().length == 0;
  }

  public static boolean isBooleanGetter(Method method) {
    return isValidBooleanGetterName(method.getName()) 
        && Boolean.TYPE.equals(method.getReturnType())
        && method.getParameterTypes().length == 0;
  }

  public static boolean isValidGetterName(String methodName) {
    return isValidStandardGetterName(methodName) || isValidBooleanGetterName(methodName);
  }

  private static boolean isValidStandardGetterName(String name) {
    return name.length() >= GET_PREFIX.length() + 1 
        && isUpperCase(name.charAt(GET_PREFIX.length()))
        && name.startsWith(GET_PREFIX);
  }

  private static boolean isValidBooleanGetterName(String name) {
    return name.length() >= IS_PREFIX.length() + 1
        && isUpperCase(name.charAt(IS_PREFIX.length()))
        && name.startsWith(IS_PREFIX);
  }

  public static List<Method> getterMethodsOf(Class<?> clazz) {
    Method[] methods = clazz.getMethods();
    List<Method> getters = new ArrayList<Method>();
    for (int i = 0; i < methods.length; i++) {
      Method method = methods[i];
      if (isNotDefinedInObjectClass(method) && (isStandardGetter(method) || isBooleanGetter(method))) {
        getters.add(method);
      }
    }
    return getters;
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
//          throw new IllegalArgumentException("cannot find type " + actualTypeArgument);
        // I'm almost sure we should not arrive here !
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
   * @param clazz
   * @return
   */
  public static String getSimpleNameWithOuterClass(Class<?> clazz) {
    String nestedClassName = null;
    if (clazz.getDeclaringClass() != null) {
      nestedClassName = clazz.getName();
      nestedClassName = nestedClassName.substring(clazz.getPackage().getName().length() + 1);
      nestedClassName = nestedClassName.replace('$', '.');
    }
    return nestedClassName;
  }
}
