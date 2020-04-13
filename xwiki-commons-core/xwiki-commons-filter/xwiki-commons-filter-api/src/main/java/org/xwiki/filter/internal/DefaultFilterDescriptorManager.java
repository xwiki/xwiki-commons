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
package org.xwiki.filter.internal;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.ClassUtils.Interfaces;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.util.ReflectionMethodUtils;
import org.xwiki.component.util.ReflectionUtils;
import org.xwiki.filter.FilterDescriptor;
import org.xwiki.filter.FilterDescriptorManager;
import org.xwiki.filter.FilterElementDescriptor;
import org.xwiki.filter.FilterElementParameterDescriptor;
import org.xwiki.filter.IncompatibleFilterException;
import org.xwiki.filter.annotation.Default;
import org.xwiki.filter.annotation.Name;
import org.xwiki.properties.ConverterManager;
import org.xwiki.properties.converter.ConversionException;

/**
 * Default implementation of {@link FilterDescriptorManager}.
 *
 * @version $Id$
 * @since 5.2M1
 */
@Component
@Singleton
public class DefaultFilterDescriptorManager implements FilterDescriptorManager
{
    /**
     * The prefix of the begin events.
     */
    public static final String PREFIX_BEGIN = "begin";

    /**
     * The prefix of the end events.
     */
    public static final String PREFIX_END = "end";

    /**
     * The prefix of the on events.
     */
    public static final String PREFIX_ON = "on";

    private static final Class<?>[] CLASS_ARRAY = new Class<?>[0];

    /**
     * The descriptors.
     */
    private Map<Class<?>, FilterDescriptor> descriptors = new ConcurrentHashMap<>();

    /**
     * Used to convert default values from {@link String}.
     */
    @Inject
    private ConverterManager converter;

    @Inject
    private Logger logger;

    @Override
    public FilterDescriptor getFilterDescriptor(Class<?>... interfaces)
    {
        FilterDescriptor totalDescriptor = null;

        for (Class<?> i : interfaces) {
            FilterDescriptor descriptor = this.descriptors.get(i);

            if (descriptor == null) {
                try {
                    descriptor = createDescriptor(i);
                } catch (IncompatibleFilterException e) {
                    this.logger.error("Failed to create descriptor for filter [{}]", i, e);

                    continue;
                }
                this.descriptors.put(i, descriptor);
            }

            if (totalDescriptor == null) {
                totalDescriptor = descriptor;
            } else {
                totalDescriptor.add(descriptor);
            }
        }

        return totalDescriptor;
    }

    /**
     * @param method the method
     * @param searchTopMethod search for top most overridden method
     * @return the corresponding element name
     */
    public static String getElementName(Method method, boolean searchTopMethod)
    {
        Method topMethod = method;

        if (searchTopMethod) {
            // Get top most method declaration
            Set<Method> hierarchy = MethodUtils.getOverrideHierarchy(method, Interfaces.INCLUDE);
            topMethod = IterableUtils.get(hierarchy, hierarchy.size() - 1);
        }

        // Get element name from method
        return getElementName(topMethod);
    }

    /**
     * @param method the method
     * @return the corresponding element name
     */
    public static String getElementName(Method method)
    {
        Name name = method.getAnnotation(Name.class);

        if (name != null) {
            return name.value();
        }

        return getElementName(method.getName());
    }

    /**
     * @param methodName the method name
     * @return the corresponding element name
     */
    public static String getElementName(String methodName)
    {
        String elementName;
        if (methodName.startsWith(PREFIX_BEGIN)) {
            elementName = methodName.substring(PREFIX_BEGIN.length(), methodName.length());
        } else if (methodName.startsWith(PREFIX_END)) {
            elementName = methodName.substring(PREFIX_END.length(), methodName.length());
        } else if (methodName.startsWith(PREFIX_ON)) {
            elementName = methodName.substring(PREFIX_ON.length(), methodName.length());
        } else {
            elementName = null;
        }

        if (elementName != null) {
            elementName = Character.toLowerCase(elementName.charAt(0)) + elementName.substring(1, elementName.length());
        }

        return elementName;
    }

    /**
     * @param type the class of the filter
     * @return the descriptor of the filter
     * @throws IncompatibleFilterException when several methods/events are incompatibles
     */
    private FilterDescriptor createDescriptor(Class<?> type) throws IncompatibleFilterException
    {
        // Proxy "loose" various reflection informations (like method parameter names)
        if (Proxy.isProxyClass(type)) {
            return getFilterDescriptor(type.getInterfaces());
        } else {
            FilterDescriptor descriptor = new FilterDescriptor();

            for (Method method : type.getMethods()) {
                // Get top most method declaration
                Set<Method> hierarchy = MethodUtils.getOverrideHierarchy(method, Interfaces.INCLUDE);
                Method topMethod = IterableUtils.get(hierarchy, hierarchy.size() - 1);

                // Get element name from method
                String elementName = getElementName(topMethod);

                // If a name can be found, continue
                if (elementName != null) {
                    addElement(elementName, descriptor, topMethod);
                }
            }

            return descriptor;
        }
    }

    /**
     * @param elementName the name of the element
     * @param descriptor the descriptor in which to add the element
     * @param method the method associated to the element
     * @throws IncompatibleFilterException when passed method is not compatible with matching filter(s)
     */
    private void addElement(String elementName, FilterDescriptor descriptor, Method method)
        throws IncompatibleFilterException
    {
        String lowerElementName = elementName.toLowerCase();

        FilterElementDescriptor element = descriptor.getElements().get(lowerElementName);

        Type[] methodTypes = method.getGenericParameterTypes();

        if (element == null || methodTypes.length > element.getParameters().length) {
            FilterElementParameterDescriptor<?>[] parameters =
                new FilterElementParameterDescriptor<?>[methodTypes.length];

            for (int i = 0; i < methodTypes.length; ++i) {
                parameters[i] = createFilterElementParameter(method, i, methodTypes[i]);
            }

            // Make sure those parameters are compatible with any other matching element
            if (element != null) {
                checkCompatible(element, parameters);
            }

            element = new FilterElementDescriptor(elementName, parameters);

            descriptor.getElements().put(lowerElementName, element);
        }

        addMethod(element, method);
    }

    private void checkCompatible(FilterElementDescriptor element, FilterElementParameterDescriptor<?>[] parameters)
        throws IncompatibleFilterException
    {
        for (FilterElementParameterDescriptor<?> parameter : parameters) {
            FilterElementParameterDescriptor<?> elementParameter = element.getParameter(parameter.getName());

            if (elementParameter != null && !elementParameter.getType().equals(parameter.getType())) {
                throw new IncompatibleFilterException("Parameter [" + parameter + "] is not compatible with parameter ["
                    + elementParameter + "] (different types)");
            }
        }
    }

    /**
     * @param method the method associated to the element
     * @param index the method parameter index
     * @param type the method parameter type
     * @return the associated {@link FilterElementParameterDescriptor}
     */
    private FilterElementParameterDescriptor<?> createFilterElementParameter(Method method, int index, Type type)
    {
        // @Name
        List<Name> nameAnnotations =
            ReflectionMethodUtils.getMethodParameterAnnotations(method, index, Name.class, true);

        String name;
        if (!nameAnnotations.isEmpty()) {
            name = nameAnnotations.get(0).value();
        } else {
            // Fallback on reflection to get the parameter name
            Parameter parameter = method.getParameters()[index];
            name = parameter.isNamePresent() ? method.getParameters()[index].getName() : null;
        }

        // @Default
        List<Default> defaultAnnotations =
            ReflectionMethodUtils.getMethodParameterAnnotations(method, index, Default.class, true);

        Object defaultValue;
        if (!defaultAnnotations.isEmpty()) {
            defaultValue = defaultAnnotations.get(0).value();

            if (defaultValue != null) {
                try {
                    defaultValue = this.converter.convert(type, defaultValue);
                } catch (ConversionException e) {
                    // TODO: remove that hack when String -> Map support is added to xwiki-properties
                    if (ReflectionUtils.getTypeClass(type) == Map.class && ((String) defaultValue).isEmpty()) {
                        defaultValue = Collections.EMPTY_MAP;
                    } else {
                        throw e;
                    }
                }
            }
        } else {
            defaultValue = null;
        }

        return new FilterElementParameterDescriptor<>(index, name, type, defaultValue);
    }

    /**
     * @param element the element
     * @param method the method to add to the element
     */
    private void addMethod(FilterElementDescriptor element, Method method)
    {
        String methodName = method.getName();
        Type[] methodTypes = method.getGenericParameterTypes();

        if (methodName.startsWith(PREFIX_BEGIN)) {
            if (element.getBeginMethod() == null
                || element.getBeginMethod().getGenericParameterTypes().length < methodTypes.length) {
                element.setBeginMethod(method);
            }
        } else if (methodName.startsWith(PREFIX_END)) {
            if (element.getEndMethod() == null
                || element.getEndMethod().getGenericParameterTypes().length < methodTypes.length) {
                element.setEndMethod(method);
            }
        } else {
            if (element.getOnMethod() == null
                || element.getOnMethod().getGenericParameterTypes().length < methodTypes.length) {
                element.setOnMethod(method);
            }
        }
    }

    @Override
    public <F> F createFilterProxy(Object targetFilter, Class<?>... interfaces)
    {
        return createFilterProxy(targetFilter, Thread.currentThread().getContextClassLoader(), interfaces);
    }

    @Override
    public <F> F createFilterProxy(Object targetFilter, ClassLoader loader, Class<?>... interfaces)
    {
        for (Class<?> i : interfaces) {
            if (!i.isInstance(targetFilter)) {
                return (F) Proxy.newProxyInstance(loader, interfaces,
                    new FilterProxy(targetFilter, getFilterDescriptor(interfaces)));
            }
        }

        return (F) targetFilter;
    }

    @Override
    public <F> F createCompositeFilter(Object... filters)
    {
        return createCompositeFilter(Thread.currentThread().getContextClassLoader(), filters);
    }

    @Override
    public <F> F createCompositeFilter(ClassLoader loader, Object... filters)
    {
        Set<Class<?>> interfaces = new HashSet<>();
        for (Object filter : filters) {
            interfaces.addAll(ClassUtils.getAllInterfaces(filter.getClass()));
        }

        return (F) Proxy.newProxyInstance(loader, interfaces.toArray(CLASS_ARRAY), new CompositeFilter(this, filters));
    }
}
