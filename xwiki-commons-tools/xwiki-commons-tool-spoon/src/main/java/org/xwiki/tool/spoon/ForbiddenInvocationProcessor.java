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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Level;

import spoon.SpoonException;
import spoon.processing.AbstractProcessor;
import spoon.processing.ProcessorProperties;
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
    private Map<String, Set<String>> invocations = new HashMap<>();

    @Override
    public void initProperties(ProcessorProperties properties)
    {
        super.initProperties(properties);

        String property = properties.get(String.class, "invocations");

        if (property != null) {
            String[] propertyList = property.split("[, |\n]+");

            for (String invocation : propertyList) {
                String cleanInvocation = invocation.trim();

                int index = cleanInvocation.indexOf('#');

                if (index == -1) {
                    addInvocation(cleanInvocation, null);
                } else {
                    addInvocation(cleanInvocation.substring(0, index),
                        cleanInvocation.substring(index + 1, cleanInvocation.length()));
                }
            }
        }
    }

    private void addInvocation(String className, String methodName)
    {
        Set<String> methods = this.invocations.get(className);

        if (methods == null) {
            methods = new HashSet<>();
            this.invocations.put(className, methods);
        }

        methods.add(methodName);
    }

    @Override
    public void process(CtInvocation<?> element)
    {
        CtExpression<?> target = element.getTarget();

        if (target != null && target.getType() != null) {
            String type = target.getType().getQualifiedName();
            Set<String> methods = this.invocations.get(type);
            if (methods != null) {
                String method = element.getExecutable().getSimpleName();
                if (methods.contains(method)) {
                    String message = "Forbidden invocation of " + type + "#" + method;

                    getFactory().getEnvironment().report(this, Level.ERROR, element, message);

                    // Really bad but that's how the Spoon plugin deal with that...
                    throw new SpoonException(message);
                }
            }
        }
    }
}
