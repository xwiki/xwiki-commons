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
package org.xwiki.tool.spoon;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import spoon.reflect.declaration.CtAnnotation;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.reference.CtTypeReference;

/**
 * Help methods for Spoon.
 *
 * @version $Id$
 * @since 12.4RC1
 */
public final class SpoonUtils
{
    private SpoonUtils()
    {
        // Empty voluntarily
    }

    /**
     * @param ctClass the class for which to find the annotations for
     * @return the list of annotations for the passed class and its superclasses
     */
    public static Set<CtAnnotation<? extends Annotation>> getAnnotationsIncludingFromSuperclasses(
        CtClass<?> ctClass)
    {
        Set<CtAnnotation<? extends Annotation>> annotations = new HashSet<>();
        Set<String> annotationNames = new HashSet<>();
        CtClass<?> current = ctClass;
        do {
            for (CtAnnotation<? extends Annotation> ctAnnotation : current.getAnnotations()) {
                String annotationName = ctAnnotation.getType().getQualifiedName();
                // Note: When they have the same name, we consider that the annotation in the extending class has
                // priority over the the annotation in the super class.
                if (!annotationNames.contains(annotationName)) {
                    annotations.add(ctAnnotation);
                    annotationNames.add(annotationName);
                }
            }
            current = current.getSuperclass() == null ? null
                : (CtClass<?>) current.getSuperclass().getTypeDeclaration();
        } while (current != null);
        return annotations;
    }

    /**
     * @param ctTypeReference the type for which to find the annotation for
     * @param annotationFQN the FQN class name of the annotation
     * @return true if the annotation exists on the type, false otherwise
     */
    public static boolean hasAnnotation(CtTypeReference<?> ctTypeReference, String annotationFQN)
    {
        return getAnnotation(ctTypeReference, annotationFQN) != null;
    }

    /**
     * @param ctTypeReference the type for which to find the annotation for
     * @param annotationFQN the FQN class name of the annotation
     * @return the annotation
     */
    public static Optional<CtAnnotation<? extends Annotation>> getAnnotation(CtTypeReference<?> ctTypeReference,
        String annotationFQN)
    {
        CtAnnotation<? extends Annotation> result = null;
        for (CtAnnotation<? extends Annotation> ctAnnotation : ctTypeReference.getTypeDeclaration().getAnnotations()) {
            if (ctAnnotation.getType().getQualifiedName().equals(annotationFQN)) {
                result = ctAnnotation;
                break;
            }
        }
        return Optional.ofNullable(result);
    }
}
