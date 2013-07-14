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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.xwiki.component.annotation.Component;
import org.xwiki.filter.FilterDescriptor;
import org.xwiki.filter.FilterDescriptorManager;
import org.xwiki.filter.FilterElement;
import org.xwiki.filter.FilterElementParameter;
import org.xwiki.filter.annotation.Name;

/**
 * Default implementation of {@link FilterDescriptorManager}.
 * 
 * @version $Id$
 * @since 5.2M1
 */
@Component
public class DefaultFilterDescriptorManager implements FilterDescriptorManager
{
    /**
     * The prefix of the begin events.
     */
    private static final String PREFIX_BEGIN = "begin";

    /**
     * The prefix of the end events.
     */
    private static final String PREFIX_END = "end";

    /**
     * The prefix of the on events.
     */
    private static final String PREFIX_ON = "on";

    /**
     * The descriptors.
     */
    private Map<Class< ? >, FilterDescriptor> descriptors = new ConcurrentHashMap<Class< ? >, FilterDescriptor>();

    @Override
    public FilterDescriptor getFilterDescriptor(Class< ? > type)
    {
        FilterDescriptor descriptor = this.descriptors.get(type);
        if (descriptor == null) {
            descriptor = createDescriptor(type);
            this.descriptors.put(type, descriptor);
        }

        return descriptor;
    }

    /**
     * @param clazz the class
     * @return the methods of the passed class
     */
    private Method[] getMethods(Class< ? > clazz)
    {
        if (Proxy.isProxyClass(clazz)) {
            // In case of a proxy we can't count of Class#getMethods() since we would loose method parameters
            // annotations
            Class< ? >[] ifaces = clazz.getInterfaces();
            if (ifaces.length > 1) {
                Set<Method> methods = new LinkedHashSet<Method>();
                for (Class< ? > iface : clazz.getInterfaces()) {
                    for (Method method : iface.getMethods()) {
                        methods.add(method);
                    }
                }

                return methods.toArray(new Method[0]);
            } else {
                return ifaces[0].getMethods();
            }
        } else {
            return clazz.getMethods();
        }
    }

    /**
     * @param type the class of the filter
     * @return the descriptor of the filter
     */
    public FilterDescriptor createDescriptor(Class< ? > type)
    {
        FilterDescriptor descriptor = new FilterDescriptor();

        for (Method method : getMethods(type)) {
            String methodName = method.getName();

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
                elementName =
                    Character.toLowerCase(elementName.charAt(0)) + elementName.substring(1, elementName.length());

                addElement(elementName, descriptor, method);
            }

        }

        return descriptor;
    }

    /**
     * @param elementName the name of the element
     * @param descriptor the descriptor in which to add the element
     * @param method the method associated to the element
     */
    private void addElement(String elementName, FilterDescriptor descriptor, Method method)
    {
        String lowerElementName = elementName.toLowerCase();

        FilterElement element = descriptor.getElements().get(lowerElementName);

        Type[] methodTypes = method.getGenericParameterTypes();
        // TODO: add support for multiple methods
        if (element == null || methodTypes.length > element.getParameters().length) {
            FilterElementParameter[] parameters = new FilterElementParameter[methodTypes.length];

            Annotation[][] parametersAnnotations = method.getParameterAnnotations();

            for (int i = 0; i < methodTypes.length; ++i) {
                Annotation[] annotations = parametersAnnotations[i];

                Type type = methodTypes[i];
                String name = null;

                for (Annotation annotation : annotations) {
                    if (annotation instanceof Name) {
                        name = ((Name) annotation).value();
                        break;
                    }
                }

                parameters[i] = new FilterElementParameter(i, name, type);
            }

            element = new FilterElement(elementName, parameters);

            descriptor.getElements().put(lowerElementName, element);
        }

        addMethod(element, method);
    }

    /**
     * @param element the element
     * @param method the method to add to the element
     */
    private void addMethod(FilterElement element, Method method)
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
}
