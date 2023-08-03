package org.luncert.tinystorage.storemodule.util;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ReflectUtils {

  public static Method[] getPropertyMethods(PropertyDescriptor[] properties, boolean read, boolean write) {
    Set<Method> methods = new HashSet<>();
    for (PropertyDescriptor pd : properties) {
      if (read) {
        methods.add(pd.getReadMethod());
      }
      if (write) {
        methods.add(pd.getWriteMethod());
      }
    }
    methods.remove(null);
    return methods.toArray(new Method[0]);
  }

  public static PropertyDescriptor[] getBeanProperties(Class<?> type) {
    return getPropertiesHelper(type, true, true);
  }

  public static PropertyDescriptor[] getBeanGetters(Class<?> type) {
    return getPropertiesHelper(type, true, false);
  }

  public static PropertyDescriptor[] getBeanSetters(Class<?> type) {
    return getPropertiesHelper(type, false, true);
  }

  private static PropertyDescriptor[] getPropertiesHelper(Class<?> type, boolean read, boolean write) {
    try {
      BeanInfo info = Introspector.getBeanInfo(type, Object.class);
      PropertyDescriptor[] all = info.getPropertyDescriptors();
      if (read && write) {
        return all;
      }
      List<PropertyDescriptor> properties = new ArrayList<>(all.length);
      for (int i = 0; i < all.length; i++) {
        PropertyDescriptor pd = all[i];
        if ((read && pd.getReadMethod() != null) || (write && pd.getWriteMethod() != null)) {
          properties.add(pd);
        }
      }
      return properties.toArray(new PropertyDescriptor[0]);
    } catch (IntrospectionException e) {
      throw new RuntimeException(e);
    }
  }
}
