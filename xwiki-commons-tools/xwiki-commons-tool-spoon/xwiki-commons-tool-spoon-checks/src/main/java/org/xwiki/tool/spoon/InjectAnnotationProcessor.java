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

import spoon.reflect.declaration.CtAnnotation;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtField;

/**
 * Verifies that the {@code @Inject} annotation is only used with interfaces.
 *
 * @version $Id$
 * @since 12.4RC1
 */
public class InjectAnnotationProcessor extends AbstractXWikiProcessor<CtAnnotation<? extends Annotation>>
{
    @Override
    public void process(CtAnnotation<? extends Annotation> ctAnnotation)
    {
        if (ctAnnotation.getAnnotationType().getQualifiedName().equals("javax.inject.Inject")) {
            CtElement element = ctAnnotation.getAnnotatedElement();
            if (element instanceof CtField) {
                CtField field = (CtField) element;
                if (field.getType().isClass()) {
                    registerError(String.format("Only interfaces should have the @Inject annotation. Problem at [%s]",
                        ctAnnotation.getPosition()));
                }
            } else {
                registerError(String.format("Only fields should use the @Inject annotation. Problem at [%s]",
                    ctAnnotation.getPosition()));
            }
        }
    }
}
