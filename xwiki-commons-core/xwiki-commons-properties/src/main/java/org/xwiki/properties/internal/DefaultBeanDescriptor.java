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
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.util.DefaultParameterizedType;
import org.xwiki.properties.BeanDescriptor;
import org.xwiki.properties.PropertyDescriptor;
import org.xwiki.properties.PropertyGroupDescriptor;
import org.xwiki.properties.annotation.PropertyAdvanced;
import org.xwiki.properties.annotation.PropertyDescription;
import org.xwiki.properties.annotation.PropertyDisplayHidden;
import org.xwiki.properties.annotation.PropertyDisplayType;
import org.xwiki.properties.annotation.PropertyFeature;
import org.xwiki.properties.annotation.PropertyGroup;
import org.xwiki.properties.annotation.PropertyHidden;
import org.xwiki.properties.annotation.PropertyId;
import org.xwiki.properties.annotation.PropertyMandatory;
import org.xwiki.properties.annotation.PropertyName;
import org.xwiki.properties.annotation.PropertyOrder;

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

    private static final List<Class<? extends Annotation>> COMMON_ANNOTATION_CLASSES = Arrays.asList(
        PropertyMandatory.class, Deprecated.class, PropertyAdvanced.class, PropertyGroup.class,
        PropertyFeature.class, PropertyDisplayType.class, PropertyDisplayHidden.class, PropertyOrder.class);

    /**
     * @see #getBeanClass()
     */
    private final Class<?> beanClass;

    /**
     * The properties of the bean.
     */
    private final Map<String, PropertyDescriptor> parameterDescriptorMap = new LinkedHashMap<>();
    private final SortedSet<PropertyDescriptor> descriptorSortedSet = new TreeSet<>((d1, d2) -> {
        int o1 = d1.getOrder();
        int o2 = d2.getOrder();
        if (o1 >= 0 && o2 >= 0) {
            return Integer.compare(o1, o2);
        } else if (o1 >= 0) {
            return -1;
        } else if (o2 >= 0) {
            return 1;
        } else {
            return d1.getId().compareTo(d2.getId());
        }
    });

    private final Map<PropertyGroup, PropertyGroupDescriptor> groups = new HashMap<>();

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

        // Try to get an instance of the bean class to get default values of the bean properties in the property
        // descriptor. Note that a java bean is supposed to always have an empty public constructor but we want to
        // support the use cases where it's not the case and still make it work. In this case, we simply don't
        // set default values.
        Constructor<?> constructor = ConstructorUtils.getAccessibleConstructor(getBeanClass());
        if (constructor != null) {
            try {
                defaultInstance = constructor.newInstance();
            } catch (Exception e) {
                LOGGER.debug("Failed to create a new default instance for class [{}]. The BeanDescriptor will not "
                    + "contains any default value information.", getBeanClass().getName(), e);
            }
        }

        try {
            // Get public fields
            for (Class<?> currentClass = getBeanClass(); currentClass != null; currentClass =
                    currentClass.getSuperclass()) {
                Field[] fields = currentClass.getFields();
                for (Field field : fields) {
                    if (!Modifier.isStatic(field.getModifiers())) {
                        extractPropertyDescriptor(field, defaultInstance);
                    }
                }
            }

            // Get getter/setter based properties
            BeanInfo beanInfo = Introspector.getBeanInfo(getBeanClass());
            java.beans.PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
            if (propertyDescriptors != null) {
                for (java.beans.PropertyDescriptor propertyDescriptor : propertyDescriptors) {
                    if (propertyDescriptor != null) {
                        extractPropertyDescriptor(propertyDescriptor, defaultInstance);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to load bean descriptor for class [{}]. Ignoring it. Root cause: [{}]",
                getBeanClass().getName(), ExceptionUtils.getRootCauseMessage(e));
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

                Map<Class, Annotation> annotations = new HashMap<>();
                COMMON_ANNOTATION_CLASSES.forEach(aClass ->
                        annotations.put(aClass, extractPropertyAnnotation(writeMethod, readMethod, aClass))
                );

                setCommonProperties(desc, annotations);

                if (defaultInstance != null && readMethod != null) {
                    // get default value
                    try {
                        desc.setDefaultValue(readMethod.invoke(defaultInstance));
                    } catch (Exception e) {
                        LOGGER.warn("Failed to get default property value from getter [{}] in class [{}]. Ignoring it. "
                            + "Root cause [{}]", readMethod.getName(), getBeanClass(),
                            ExceptionUtils.getRootCauseMessage(e));
                    }
                }

                desc.setWriteMethod(writeMethod);

                desc.setReadMethod(readMethod);

                this.parameterDescriptorMap.put(desc.getId(), desc);
                this.descriptorSortedSet.add(desc);
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

            Map<Class, Annotation> annotations = new HashMap<>();
            COMMON_ANNOTATION_CLASSES.forEach(aClass ->
                    annotations.put(aClass, field.getAnnotation(aClass))
            );

            setCommonProperties(desc, annotations);

            if (defaultInstance != null) {
                // get default value
                try {
                    desc.setDefaultValue(field.get(defaultInstance));
                } catch (Exception e) {
                    LOGGER.warn("Failed to get default property value from field [{}] in class [{}]. Ignoring it. "
                        + "Root cause: [{}]", field.getName(), getBeanClass(), ExceptionUtils.getRootCauseMessage(e));
                }
            }

            desc.setField(field);

            this.parameterDescriptorMap.put(desc.getId(), desc);
            this.descriptorSortedSet.add(desc);
        }
    }

    private void setCommonProperties(DefaultPropertyDescriptor desc, Map<Class, Annotation> annotations)
    {
        desc.setMandatory(annotations.get(PropertyMandatory.class) != null);
        desc.setDeprecated(annotations.get(Deprecated.class) != null);
        desc.setAdvanced(annotations.get(PropertyAdvanced.class) != null);
        handlePropertyFeatureAndGroupAnnotations(desc, annotations);
        handlePropertyDisplayTypeAnnotation(desc, annotations);
        desc.setDisplayHidden(annotations.get(PropertyDisplayHidden.class) != null);
        handlePropertyOrder(desc, annotations);
    }

    private void handlePropertyFeatureAndGroupAnnotations(DefaultPropertyDescriptor desc, Map<Class,
        Annotation> annotations)
    {
        PropertyGroup parameterGroup = (PropertyGroup) annotations.get(PropertyGroup.class);
        PropertyGroupDescriptor group = this.groups.get(parameterGroup);
        if (group == null && parameterGroup != null) {
            group = new PropertyGroupDescriptor(Arrays.asList(parameterGroup.value()));
        } else if (group == null) {
            group = new PropertyGroupDescriptor(null);
        }
        desc.setGroupDescriptor(group);
        if (parameterGroup != null) {
            this.groups.put(parameterGroup, group);
        }

        PropertyFeature parameterFeature = (PropertyFeature) annotations.get(PropertyFeature.class);
        if (parameterFeature != null) {
            if (group.getFeature() != null) {
                throw new RuntimeException(String.format("Property [%s] has overridden a feature "
                    + "(previous: [%s], new: [%s])", desc.getId(), group.getFeature(), parameterFeature.value()));
            }
            group.setFeature(parameterFeature.value());
            group.setFeatureMandatory(parameterFeature.mandatory());
        }
    }

    private void handlePropertyDisplayTypeAnnotation(DefaultPropertyDescriptor desc, Map<Class, Annotation> annotations)
    {
        PropertyDisplayType displayTypeAnnotation = (PropertyDisplayType) annotations.get(PropertyDisplayType.class);
        Type displayType;
        if (displayTypeAnnotation != null && displayTypeAnnotation.value().length > 0) {
            Class[] types = displayTypeAnnotation.value();
            if (types.length > 1) {
                displayType = new DefaultParameterizedType(null, types[0], ArrayUtils.remove(types, 0));
            } else {
                displayType = types[0];
            }
        } else {
            displayType = desc.getPropertyType();
        }
        desc.setDisplayType(displayType);
    }

    private void handlePropertyOrder(DefaultPropertyDescriptor desc, Map<Class, Annotation> annotations)
    {
        PropertyOrder propertyOrderAnnotation = (PropertyOrder) annotations.get(PropertyOrder.class);
        if (propertyOrderAnnotation != null && propertyOrderAnnotation.value() > 0) {
            desc.setOrder(propertyOrderAnnotation.value());
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
        return this.descriptorSortedSet;
    }

    @Override
    public PropertyDescriptor getProperty(String propertyName)
    {
        return this.parameterDescriptorMap.get(propertyName);
    }
}
