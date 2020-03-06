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

import spoon.reflect.declaration.CtAnnotation;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtTypeMember;

/**
 * Verifies that we don't mix JUnit4 and JUnit5 APIs.
 *
 * @version $Id$
 */
public class JUnit5JUnit4Processor extends AbstractXWikiProcessor<CtClass<?>>
{
    @Override
    public void process(CtClass<?> ctClass)
    {
        boolean hasJunit5Types = false;
        boolean hasJunit4Types = false;
        for (CtTypeMember typeMember : ctClass.getTypeMembers()) {
            for (CtAnnotation annotation : typeMember.getAnnotations()) {
                if (annotation.getAnnotationType().getQualifiedName().startsWith("org.junit.jupiter")) {
                    hasJunit5Types = true;
                    if (hasJunit4Types) {
                        break;
                    }
                } else if (annotation.getAnnotationType().getQualifiedName().startsWith("org.junit")) {
                    hasJunit4Types = true;
                    if (hasJunit5Types) {
                        break;
                    }
                }
            }
        }

        if (hasJunit4Types && hasJunit5Types) {
            registerError(String.format("There's a mix of JUnit4 and JUnit5 APIs at [%s]", ctClass.getPosition()));
        }
    }
}
