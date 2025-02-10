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
package org.xwiki.configuration.internal;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;

/**
 * Configuration source that reads from the execution context.
 * 
 * @version $Id$
 * @since 16.1.0RC1
 * @since 15.10.6
 */
@Component
@Named("executionContext")
@Singleton
public class ExecutionContextConfigurationSource extends AbstractMemoryConfigurationSource
{
    @Inject
    private Execution execution;

    @SuppressWarnings("unchecked")
    @Override
    protected Map<String, Object> getProperties()
    {
        ExecutionContext executionContext = this.execution.getContext();
        if (executionContext != null) {
            String key = this.getClass().getName();
            if (!executionContext.hasProperty(key)) {
                // Initialize with an empty map that ca be modified.
                executionContext.newProperty(key).inherited().initial(new LinkedHashMap<>()).makeFinal().nonNull()
                    .declare();
            }
            return (Map<String, Object>) executionContext.getProperty(key);
        } else {
            // Return an empty map that can't be modified.
            return Collections.emptyMap();
        }
    }
}
