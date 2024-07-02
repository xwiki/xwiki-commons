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

import java.util.Set;

import spoon.SpoonException;
import spoon.processing.Property;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtTypeAccess;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;

/**
 * Fail the build if some code is calling a forbidden method.
 * 
 * @version $Id$
 * @since 9.9RC2
 */
public class ForbiddenInvocationProcessor extends AbstractXWikiProcessor<CtInvocation<?>>
{
    @Property
    private Set<String> methods;

    @Property
    private Set<String> ignores;

    @Override
    public void process(CtInvocation<?> invocation)
    {
        if (this.methods == null) {
            throw new SpoonException("Processor must be configured with a \"methods\" parameter of type "
                + "\"Map<String, List<String>>\".");
        }

        CtClass<?> ctClass = getCtClass(invocation);

        if (ctClass != null && !this.ignores.contains(ctClass.getQualifiedName())) {
            processInvocation(invocation);
        }
    }

    private void processInvocation(CtInvocation<?> invocation)
    {
        CtExpression<?> target = invocation.getTarget();

        if (target != null) {
            String type = getType(target);

            if (type != null) {
                String shortSignature = type + '#' + invocation.getExecutable().getSimpleName();
                String completeSignature = type + '#' + invocation.getExecutable().getSignature();
                
                if (this.methods.contains(shortSignature) || this.methods.contains(completeSignature)) {
                    String message =
                        String.format("Forbidden call to [%s] at %s", completeSignature, target.getPosition());
                    registerError(message);
                }
            }
        }
    }

    private String getType(CtExpression<?> target)
    {
        String type = null;

        if (target instanceof CtTypeAccess<?> typeAccess) {
            // It's a static call
            if (typeAccess.getAccessedType() != null) {
                type = typeAccess.getAccessedType().getQualifiedName();
            }
        } else {
            if (target.getType() != null) {
                type = target.getType().getQualifiedName();
            }
        }

        return type;
    }

    private CtClass<?> getCtClass(CtInvocation<?> invocation)
    {
        CtElement element = invocation.getParent();
        while (element != null && !(element instanceof CtClass)) {
            element = element.getParent();
        }

        return (CtClass<?>) element;
    }
}
