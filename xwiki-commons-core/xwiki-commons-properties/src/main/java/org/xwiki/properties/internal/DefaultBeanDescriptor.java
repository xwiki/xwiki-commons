/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.properties.internal;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.properties.BeanDescriptor;
import org.xwiki.properties.PropertyDescriptor;
import org.xwiki.properties.annotation.PropertyDescription;
import org.xwiki.properties.annotation.PropertyHidden;
import org.xwiki.properties.annotation.PropertyId;
import org.xwiki.properties.annotation.PropertyMandatory;
import org.xwiki.properties.annotation.PropertyName;

/**
 * Default implementation for BeanDescriptor.
 *
 * @version $Id$
 * @since 2.0M2
 */
public class DefaultBeanDescriptor implements BeanDescriptor
{
    /**
     * The logger to use to log.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultBeanDescriptor.class);

    /**
     * @see #getBeanClass()
     */
    private Class<?> beanClass;

    /**
     * The properties of the bean.
     */
    private Map<String, PropertyDescriptor> parameterDescriptorMap = new LinkedHashMap<String, PropertyDescriptor>();

    /**
     * @param beanClass the class of the JAVA bean.
     */
    public DefaultBeanDescriptor(Class<?> beanClass)
    {
        this.beanClass = beanClass;

        extractBeanDescriptor();
    }

    /**
     * Extract informations form the bean.
     */
    protected void extractBeanDescriptor()
    {
        Object defaultInstance = null;

        try {
            defaultInstance = getBeanClass().newInstance();
        } catch (Exception e) {
            LOGGER.debug("Failed to create a new default instance for class " + this.beanClass
                + ". The BeanDescriptor will not contains any default value information.", e);
        }

        try {
            // Get public fields
            for (Class<?> currentClass = this.beanClass; currentClass != null; currentClass =
                    currentClass.getSuperclass()) {
                Field[] fields = currentClass.getFields();
                for (Field field : fields) {
                    if (!Modifier.isStatic(field.getModifiers())) {
                        extractPropertyDescriptor(field, defaultInstance);
                    }
                }
            }

            // Get getter/setter based properties
            BeanInfo beanInfo = Introspector.getBeanInfo(this.beanClass);
            java.beans.PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
            if (propertyDescriptors != null) {
                for (java.beans.PropertyDescriptor propertyDescriptor : propertyDescriptors) {
                    if (propertyDescriptor != null) {
                        extractPropertyDescriptor(propertyDescriptor, defaultInstance);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Failed to load bean descriptor for class " + this.beanClass, e);
        }
    }

    /**
     * Extract provided properties information and insert it in {@link #parameterDescriptorMap}.
     *
     * @param propertyDescriptor the JAVA bean property descriptor.
     * @param defaultInstance the default instance of bean class.
     */
    protected void extractPropertyDescriptor(java.beans.PropertyDescriptor propertyDescriptor, Object defaultInstance)
    {
        DefaultPropertyDescriptor desc = new DefaultPropertyDescriptor();

        Method writeMethod = propertyDescriptor.getWriteMethod();

        if (writeMethod != null) {
            Method readMethod = propertyDescriptor.getReadMethod();

            // is parameter hidden
            PropertyHidden parameterHidden = extractPropertyAnnotation(writeMethod, readMethod, PropertyHidden.class);

            if (parameterHidden == null) {
                // get parameter id
                PropertyId propertyId = extractPropertyAnnotation(writeMethod, readMethod, PropertyId.class);
                desc.setId(propertyId != null ? propertyId.value() : propertyDescriptor.getName());

                // set parameter type
                Type propertyType;
                if (readMethod != null) {
                    propertyType = readMethod.getGenericReturnType();
                } else {
                    propertyType = writeMethod.getGenericParameterTypes()[0];
                }
                desc.setPropertyType(propertyType);

                // get parameter display name
                PropertyName parameterName = extractPropertyAnnotation(writeMethod, readMethod, PropertyName.class);

                desc.setName(parameterName != null ? parameterName.value() : desc.getId());

                // get parameter description
                PropertyDescription parameterDescription =
                    extractPropertyAnnotation(writeMethod, readMethod, PropertyDescription.class);

                desc.setDescription(parameterDescription != null ? parameterDescription.value() : propertyDescriptor
                    .getShortDescription());

                // is parameter mandatory
                PropertyMandatory parameterMandatory =
                    extractPropertyAnnotation(writeMethod, readMethod, PropertyMandatory.class);

                desc.setMandatory(parameterMandatory != null);

                if (defaultInstance != null && readMethod != null) {
                    // get default value
                    try {
                        desc.setDefaultValue(readMethod.invoke(defaultInstance));
                    } catch (Exception e) {
                        LOGGER.error(MessageFormat.format(
                            "Failed to get default property value from getter {0} in class {1}", readMethod.getName(),
                            this.beanClass), e);
                    }
                }

                desc.setWriteMethod(writeMethod);

                desc.setReadMethod(readMethod);

                this.parameterDescriptorMap.put(desc.getId(), desc);
            }
        }
    }

    /**
     * Extract provided properties informations and insert it in {@link #parameterDescriptorMap}.
     *
     * @param field the JAVA bean property descriptor.
     * @param defaultInstance the default instance of bean class.
     */
    protected void extractPropertyDescriptor(Field field, Object defaultInstance)
    {
        DefaultPropertyDescriptor desc = new DefaultPropertyDescriptor();

        // is parameter hidden
        PropertyHidden parameterHidden = field.getAnnotation(PropertyHidden.class);

        if (parameterHidden == null) {
            // get parameter id
            PropertyId propertyId = field.getAnnotation(PropertyId.class);
            desc.setId(propertyId != null ? propertyId.value() : field.getName());

            // set parameter type
            desc.setPropertyType(field.getGenericType());

            // get parameter name
            PropertyName parameterName = field.getAnnotation(PropertyName.class);

            desc.setName(parameterName != null ? parameterName.value() : desc.getId());

            // get parameter description
            PropertyDescription parameterDescription = field.getAnnotation(PropertyDescription.class);

            desc.setDescription(parameterDescription != null ? parameterDescription.value() : desc.getId());

            // is parameter mandatory
            PropertyMandatory parameterMandatory = field.getAnnotation(PropertyMandatory.class);

            desc.setMandatory(parameterMandatory != null);

            if (defaultInstance != null) {
                // get default value
                try {
                    desc.setDefaultValue(field.get(defaultInstance));
                } catch (Exception e) {
                    LOGGER.error(
                        MessageFormat.format("Failed to get default property value from field {0} in class {1}",
                            field.getName(), this.beanClass), e);
                }
            }

            desc.setField(field);

            this.parameterDescriptorMap.put(desc.getId(), desc);
        }
    }

    /**
     * Get the parameter annotation. Try first on the setter then on the getter if no annotation has been found.
     *
     * @param <T> the Class object corresponding to the annotation type.
     * @param writeMethod the method that should be used to write the property value.
     * @param readMethod the method that should be used to read the property value.
     * @param annotationClass the Class object corresponding to the annotation type.
     * @return this element's annotation for the specified annotation type if present on this element, else null.
     */
    protected <T extends Annotation> T extractPropertyAnnotation(Method writeMethod, Method readMethod,
        Class<T> annotationClass)
    {
        T parameterDescription = writeMethod.getAnnotation(annotationClass);

        if (parameterDescription == null && readMethod != null) {
            parameterDescription = readMethod.getAnnotation(annotationClass);
        }

        return parameterDescription;
    }

    @Override
    public Class<?> getBeanClass()
    {
        return this.beanClass;
    }

    @Override
    public Collection<PropertyDescriptor> getProperties()
    {
        return this.parameterDescriptorMap.values();
    }

    @Override
    public PropertyDescriptor getProperty(String propertyName)
    {
        return this.parameterDescriptorMap.get(propertyName);
    }
}
