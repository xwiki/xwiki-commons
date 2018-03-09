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

import java.util.Map;
import java.util.Set;

import org.apache.log4j.Level;

import spoon.processing.AbstractProcessor;
import spoon.processing.Property;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;

/**
 * Failed the build if some code is calling a forbidden method.
 * 
 * @version $Id$
 * @since 9.9RC2
 */
public class ForbiddenInvocationProcessor extends AbstractProcessor<CtInvocation<?>>
{
    @Property
    private Map<String, Set<String>> methods;

    @Override
    public void process(CtInvocation<?> element)
    {
        CtExpression<?> target = element.getTarget();

        if (target != null && target.getType() != null) {
            String type = target.getType().getQualifiedName();
            Set<String> methodSet = methods.get(type);
            if (methodSet != null) {
                String method = element.getExecutable().getSimpleName();
                if (methodSet.contains(method)) {
                    getFactory().getEnvironment().report(this, Level.ERROR, element,
                        "Forbidden call to " + type + "#" + method);

                    // Forcing the build to stop
                    // FIXME: Remove that when https://github.com/INRIA/spoon/issues/1534 is implemented
                    throw new RuntimeException("Forbidden call to " + type + "#" + method);
                }
            }
        }
    }
}
