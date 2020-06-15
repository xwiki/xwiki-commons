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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import spoon.processing.Property;
import spoon.reflect.declaration.CtAnnotation;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtField;
import spoon.reflect.reference.CtTypeReference;

/**
 * Performs the following checks.
 * <ul>
 *   <li>The {@code @Inject} annotation is only used with component roles. This is to prevent errors when we
 *       inject the component implementation class instead of the role.</li>
 *   <li>Only fields can use the {@code @Inject} annotation.</li>
 * </ul>
 *
 * @version $Id$
 * @since 12.4RC1
 */
public class InjectAnnotationProcessor extends AbstractXWikiProcessor<CtAnnotation<? extends Annotation>>
{
    private static final List<String> SPECIAL_INJECT_INTERFACES = Arrays.asList(
        "org.slf4j.Logger",
        "java.util.List",
        "java.util.Map",
        "javax.inject.Provider",
        "org.xwiki.component.descriptor.ComponentDescriptor"
    );

    @Property
    private List<String> excludedFieldTypes;

    @Override
    public void process(CtAnnotation<? extends Annotation> ctAnnotation)
    {
        if (ctAnnotation.getAnnotationType().getQualifiedName().equals("javax.inject.Inject")) {
            CtElement element = ctAnnotation.getAnnotatedElement();
            if (element instanceof CtField) {
                CtField<?> ctField = (CtField<?>) element;
                if (!isExcluded(ctField)) {
                    process(ctField);
                }
            } else {
                registerError(String.format("Only fields should use the @Inject annotation. Problem at %s",
                    element.getPosition()));
            }
        }
    }

    private void process(CtField<?> ctField)
    {
        // The following 2 cases are supported:
        // - The field refers to an interface that has the @Role annotation
        // - The field refers to a class that has the @Component annotation but with a role specifying the field class
        if (!isValidInterface(ctField.getType()) && !isComponentAnnotationWithRoleToSelf(ctField.getType())) {
            registerError(
                String.format("You must inject a component role. Got [%s] at %s", ctField.getType(),
                    ctField.getPosition()));
        }
    }

    private boolean isExcluded(CtField<?> ctField)
    {
        return this.excludedFieldTypes != null
            && this.excludedFieldTypes.contains(ctField.getType().getQualifiedName());
    }

    private boolean isValidInterface(CtTypeReference<?> ctTypeReference)
    {
        return ctTypeReference.isInterface() && (SPECIAL_INJECT_INTERFACES.contains(ctTypeReference.getQualifiedName())
            || hasRoleAnnotation(ctTypeReference));
    }

    private boolean isComponentAnnotationWithRoleToSelf(CtTypeReference<?> ctTypeReference)
    {
        boolean result = false;
        Optional<CtAnnotation<? extends Annotation>> ctAnnotation =
            SpoonUtils.getAnnotation(ctTypeReference, "org.xwiki.component.annotation.Component");
        if (ctAnnotation.isPresent()) {
            CtElement ctElement = ctAnnotation.get().getValue("roles");
            if (ctElement != null && ctElement.getReferencedTypes().contains(ctTypeReference)) {
                result = true;
            }
        }
        return result;
    }

    private boolean hasRoleAnnotation(CtTypeReference<?> ctTypeReference)
    {
        return SpoonUtils.hasAnnotation(ctTypeReference, "org.xwiki.component.annotation.Role")
            || SpoonUtils.hasAnnotation(ctTypeReference, "org.xwiki.component.annotation.ComponentRole");
    }
}
