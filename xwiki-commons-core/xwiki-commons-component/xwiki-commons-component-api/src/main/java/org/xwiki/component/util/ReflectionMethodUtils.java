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
package org.xwiki.component.util;

import java.lang.annotation.Annotation;
import java.lang.annotation.Inherited;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Various Reflection tools related to {@link Method}s.
 *
 * @version $Id$
 * @since 5.2M1
 */
public final class ReflectionMethodUtils
{
    /**
     * Utility class.
     */
    private ReflectionMethodUtils()
    {

    }

    /**
     * Get {@link Annotation}s of the provided class associated to the the provided method parameter.
     *
     * @param <A> the actual {@link Annotation} type
     * @param method the method
     * @param index the index of the parameter in the method
     * @param annotationClass the class of the annotation
     * @return the annotations
     */
    public static <A extends Annotation> List<A> getMethodParameterAnnotations(Method method, int index,
        Class<A> annotationClass)
    {
        Annotation[][] parametersAnnotations = method.getParameterAnnotations();

        Annotation[] parameterAnnotations = parametersAnnotations[index];

        List<A> foundAnnotations = new ArrayList<>();
        for (Annotation annotation : parameterAnnotations) {
            if (annotationClass.isInstance(annotation)) {
                foundAnnotations.add((A) annotation);
            }
        }

        return foundAnnotations;
    }

    /**
     * Get {@link Annotation}s of the provided class associated to the the provided method parameter.
     *
     * @param <A> the actual {@link Annotation} type
     * @param method the method
     * @param index the index of the parameter in the method
     * @param annotationClass the class of the annotation
     * @param inherits if true also search on overwritten methods from interfaces and super classes
     * @return the annotations
     */
    public static <A extends Annotation> List<A> getMethodParameterAnnotations(Method method, int index,
        Class<A> annotationClass, boolean inherits)
    {
        List<A> annotations = getMethodParameterAnnotations(method, index, annotationClass);

        if (inherits && annotationClass.getAnnotation(Inherited.class) != null) {
            Class<?>[] ifaces = method.getDeclaringClass().getInterfaces();

            for (Class<?> iface : ifaces) {
                Method interfaceMethod;
                try {
                    interfaceMethod = iface.getMethod(method.getName(), method.getParameterTypes());

                    if (interfaceMethod != null) {
                        annotations
                            .addAll(getMethodParameterAnnotations(interfaceMethod, index, annotationClass, true));
                    }
                } catch (Exception e) {
                    // Ignore it
                }
            }

            Class<?> superClass = method.getDeclaringClass().getSuperclass();
            if (superClass != null) {
                try {
                    Method superMethod = superClass.getMethod(method.getName(), method.getParameterTypes());

                    if (superMethod != null) {
                        annotations.addAll(getMethodParameterAnnotations(superMethod, index, annotationClass, true));
                    }
                } catch (Exception e) {
                    // Ignore it
                }
            }
        }

        return annotations;
    }
}
