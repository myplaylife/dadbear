/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.littlepanda.dadbear.core.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.configuration.Configuration;

import me.littlepanda.dadbear.core.config.Configurable;

/**
 * General reflection utils
 */
public class ReflectionUtils {
    
  private static final Class<?>[] EMPTY_ARRAY = new Class[]{};

  /** 
   * Cache of constructors for each class. Pins the classes so they
   * can't be garbage collected until ReflectionUtils can be collected.
   */
  private static final Map<Class<?>, Constructor<?>> CONSTRUCTOR_CACHE = 
    new ConcurrentHashMap<Class<?>, Constructor<?>>();

  /** Create an object for the given class and initialize it from conf
   * 
   * @param theClass class of which an object is created
   * @param conf Configuration
   * @return a new object
   */
  @SuppressWarnings("unchecked")
  public static <T> T newInstance(Class<T> theClass, Configuration conf) {
    T result;
    try {
      Constructor<T> meth = (Constructor<T>) CONSTRUCTOR_CACHE.get(theClass);
      if (meth == null) {
        meth = theClass.getDeclaredConstructor(EMPTY_ARRAY);
        meth.setAccessible(true);
        CONSTRUCTOR_CACHE.put(theClass, meth);
      }
      result = meth.newInstance();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    setConf(result, conf);
    return result;
  }
  
  	/**
	 * @param className
	 * @param config 
	 * @return 根据类全名获取实例
	 */
    @SuppressWarnings("unchecked")
	public static<T> T newInstance(String className, Configuration conf){
		if(null == className){
			throw new RuntimeException("className is null.");
		}
		try {
			Class theClass = Class.forName(className);
			return (T)newInstance(theClass, conf);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("class " + className + " has not found.", e);
		}
	}

  /**
   * Return the correctly-typed {@link Class} of the given object.
   *  
   * @param o object whose correctly-typed <code>Class</code> is to be obtained
   * @return the correctly typed <code>Class</code> of the given object.
   */
  @SuppressWarnings("unchecked")
  public static <T> Class<T> getClass(T o) {
    return (Class<T>)o.getClass();
  }
  
  @SuppressWarnings("unchecked")
  public static <T> Class<T> getClass(String className) {
    try {
		return (Class<T>)Class.forName(className);
	} catch (ClassNotFoundException e) {
		throw new RuntimeException("class " + className + " has not found.", e);
	}
  }
  
  // methods to support testing
  static void clearCache() {
    CONSTRUCTOR_CACHE.clear();
  }
    
  static int getCacheSize() {
    return CONSTRUCTOR_CACHE.size();
  }
  
  /**
   * Gets all the declared fields of a class including fields declared in
   * superclasses.
   */
  public static List<Field> getDeclaredFieldsIncludingInherited(Class<?> clazz) {
    List<Field> fields = new ArrayList<Field>();
    while (clazz != null) {
      for (Field field : clazz.getDeclaredFields()) {
        fields.add(field);
      }
      clazz = clazz.getSuperclass();
    }
    
    return fields;
  }
  /**
   * Gets all the declared fields of a class 
   * superclasses.
   */
  public static List<Field> getDeclaredFields(Class<?> clazz) {
    List<Field> fields = new ArrayList<Field>();
    if(clazz != null) {
      for (Field field : clazz.getDeclaredFields()) {
        fields.add(field);
      }
    }
    
    return fields;
  }
  
  /**
   * Gets all the declared methods of a class including methods declared in
   * superclasses.
   */
  public static List<Method> getDeclaredMethodsIncludingInherited(Class<?> clazz) {
    List<Method> methods = new ArrayList<Method>();
    while (clazz != null) {
      for (Method method : clazz.getDeclaredMethods()) {
        methods.add(method);
      }
      clazz = clazz.getSuperclass();
    }
    
    return methods;
  }
  /**
   * Gets all the declared methods of a class 
   * superclasses.
   */
  public static List<Method> getDeclaredMethods(Class<?> clazz) {
    List<Method> methods = new ArrayList<Method>();
    if(clazz != null) {
      for (Method method : clazz.getDeclaredMethods()) {
        methods.add(method);
      }
    }
    
    return methods;
  }
    /**
	 * Check and set 'configuration' if necessary.
	 * 
	 * @param theObject object for which to set configuration
	 * @param conf Configuration
	 */
	private static void setConf(Object theObject, Configuration conf) {
	  if (conf != null) {
	    if (theObject instanceof Configurable) {
	      ((Configurable) theObject).setConf(conf);
	    }
	  }
	}
}
