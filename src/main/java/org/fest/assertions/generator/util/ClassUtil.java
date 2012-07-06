package org.fest.assertions.generator.util;

import static org.apache.commons.lang3.StringUtils.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;


/**
 * 
 * Some utilities methods related to classes and packages. 
 *
 * @author Joel Costigliola
 *
 */
public class ClassUtil {

  public static final String IS_PREFIX = "is";
  public static final String GET_PREFIX = "get";

  /**
   * Retrieves recursively all the classes belonging to a package.
   * @param packageName
   * @return the list of Class found
   * @throws ClassNotFoundException if any error occurs
   */
  public static List<Class<?>> getClassesInPackage(String packageName) throws ClassNotFoundException {
    return getClassesInPackage(packageName, Thread.currentThread().getContextClassLoader());
  }

  /**
   * Retrieves recursively all the classes belonging to a package.
   * @param packageName
   * @param classLoader the class loader used to load the class in the given package
   * @return the list of Class found
   * @throws ClassNotFoundException if any error occurs
   */
  public static List<Class<?>> getClassesInPackage(String packageName, ClassLoader classLoader)
      throws ClassNotFoundException {
    try {
      if (classLoader == null) { throw new ClassNotFoundException("Can't get class loader."); }
      String path = packageName.replace('.', '/');
      // Ask for all resources for the path
      Enumeration<URL> resources = classLoader.getResources(path);
      List<Class<?>> classes = new ArrayList<Class<?>>();
      while (resources.hasMoreElements()) {
        URL resource = resources.nextElement();
        File directory = new File(URLDecoder.decode(resource.getPath(), "UTF-8"));
        if (directory.canRead()) {
          classes.addAll(getClassesInDirectory(directory, packageName, classLoader));
        } else {
          // it's a jar file
          classes.addAll(getClassesInJarFile(directory.getPath().substring(5, directory.getPath().indexOf(".jar") + 4),
              packageName));
        }
      }
      return classes;
    } catch (NullPointerException x) {
      throw new ClassNotFoundException(packageName + " does not appear to be a valid package (Null pointer exception)", x);
    } catch (UnsupportedEncodingException encex) {
      throw new ClassNotFoundException(packageName + " does not appear to be a valid package (Unsupported encoding)", encex);
    } catch (IOException ioex) {
      throw new ClassNotFoundException("IOException was thrown when trying to get all resources for " + packageName, ioex);
    }
  }

  private static List<Class<?>> getClassesInDirectory(File directory, String packageName, ClassLoader classLoader)
      throws UnsupportedEncodingException {
    List<Class<?>> classes = new ArrayList<Class<?>>();
    // Capture all the .class files in this directory
    // Get the list of the files contained in the package
    String[] files = directory.list();
    for (String currentFile : files) {
      // we are only interested in .class files
      if (currentFile.endsWith(".class")) {
        // removes the .class extension
        // CHECKSTYLE:OFF
        try {
          classes.add(Class.forName(packageName + '.' + currentFile.substring(0, currentFile.length() - 6)));
        } catch (Throwable e) {
          // do nothing. this class hasn't been found by the loader, and we don't care.
        }
        // CHECKSTYLE:ON
      } else {
        // It's another package
        String subPackageName = packageName + '.' + currentFile;
        // Ask for all resources for the path
        URL resource = classLoader.getResource(subPackageName.replace('.', '/'));
        File subDirectory = new File(URLDecoder.decode(resource.getPath(), "UTF-8"));
        List<Class<?>> classesForSubPackage = getClassesInDirectory(subDirectory, subPackageName, classLoader);
        classes.addAll(classesForSubPackage);
      }
    }
    return classes;
  }

  private static List<Class<?>> getClassesInJarFile(String jar, String packageName) throws IOException {
    List<Class<?>> classes = new ArrayList<Class<?>>();
    JarInputStream jarFile = null;
    jarFile = new JarInputStream(new FileInputStream(jar));
    JarEntry jarEntry;
    while ((jarEntry = jarFile.getNextJarEntry()) != null) {
      if (jarEntry != null) {
        String className = jarEntry.getName();
        if (className.endsWith(".class")) {
          className = className.substring(0, className.length() - ".class".length()).replace('/', '.');
          if (className.startsWith(packageName)) {
            // CHECKSTYLE:OFF
            try {
              classes.add(Class.forName(className.replace('/', '.')));
            } catch (Throwable e) {
              // do nothing. this class hasn't been found by the loader, and we don't care.
            }
            // CHECKSTYLE:ON
          }
        }
      }
    }
    closeJarFile(jarFile);
    return classes;
  }

  private static void closeJarFile(final JarInputStream jarFile) throws IOException {
    if (jarFile != null) {
      jarFile.close();
    }
  }

  public static List<Class<?>> collectClasses(String... classOrPackageNames) throws ClassNotFoundException {
    List<Class<?>> classes = new ArrayList<Class<?>>();
    for (String classOrPackageName : classOrPackageNames) {
      Class<?> clazz = tryToLoadClass(classOrPackageName);
      if (clazz != null) {
        classes.add(clazz);
      } else {
        // should be a package
        classes.addAll(getClassesInPackage(classOrPackageName));
      }
    }
    return classes;
  }

  public static Class<?> tryToLoadClass(String className)  {
    try {
      return Class.forName(className);
    } catch (ClassNotFoundException e) {
      return null;
    }
  }

  /**
   * Returns the property name of given getter method, examples :
   * 
   * <pre>getName -> name</pre>
   * 
   * <pre>isMostValuablePlayer -> mostValuablePlayer</pre>
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

  public static boolean isStandardGetter(Method method) {
    return method.getName().startsWith(GET_PREFIX) && method.getReturnType() != null && method.getParameterTypes().length == 0;
  }

  public static boolean isBooleanGetter(Method method) {
    return method.getName().startsWith(IS_PREFIX) && Boolean.TYPE.equals(method.getReturnType())
        && method.getParameterTypes().length == 0;
  }

  public static List<Method> getterMethodsOf(Class<?> clazz) {
    Method[] declaredMethods = clazz.getDeclaredMethods();
    List<Method> getters = new ArrayList<Method>();
    for (int i = 0; i < declaredMethods.length; i++) {
      Method method = declaredMethods[i];
      if (isStandardGetter(method) || isBooleanGetter(method)) {
        // probably a getter
        getters.add(method);
      }
    }
    return getters;
  }

}
