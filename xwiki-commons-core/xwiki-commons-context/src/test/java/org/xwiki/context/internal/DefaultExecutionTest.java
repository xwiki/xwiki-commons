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
package org.xwiki.context.internal;

import org.junit.jupiter.api.Test;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Unit tests for {@link DefaultExecution}.
 *
 * @version $Id$
 */
public class DefaultExecutionTest
{
    @Test
    public void setContext() throws Exception
    {
        Execution execution = new DefaultExecution();

        assertNull(execution.getContext());

        ExecutionContext context1 = new ExecutionContext();

        execution.setContext(context1);

        assertSame(context1, execution.getContext());

        ExecutionContext context2 = new ExecutionContext();

        // Add new level 1

        execution.pushContext(context2);

        assertSame(context2, execution.getContext());

        // Change level 1

        ExecutionContext context3 = new ExecutionContext();

        execution.setContext(context3);

        assertSame(context3, execution.getContext());

        // Go back to level 0

        execution.popContext();

        assertSame(context1, execution.getContext());

        // Go back to no context

        execution.popContext();

        assertNull(execution.getContext());
    }

    @Test
    public void removeContext() throws Exception
    {
        Execution execution = new DefaultExecution();

        execution.pushContext(new ExecutionContext());
        execution.pushContext(new ExecutionContext());
        execution.pushContext(new ExecutionContext());

        execution.removeContext();

        assertNull(execution.getContext());
    }

    @Test
    public void pushContext()
    {
        Execution execution = new DefaultExecution();

        execution.pushContext(new ExecutionContext());

        execution.getContext().newProperty("inherited").inherited().initial("value").declare();

        execution.pushContext(new ExecutionContext());

        assertNotNull(execution.getContext().getProperty("inherited"));

        execution.pushContext(new ExecutionContext(), true);

        assertNotNull(execution.getContext().getProperty("inherited"));

        execution.pushContext(new ExecutionContext(), false);

        assertNull(execution.getContext().getProperty("inherited"));
    }
}
