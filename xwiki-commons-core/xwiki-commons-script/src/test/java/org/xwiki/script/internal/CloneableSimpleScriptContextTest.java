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
package org.xwiki.script.internal;

import javax.script.ScriptContext;
import javax.script.SimpleBindings;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Validate {@link CloneableSimpleScriptContext}.
 * 
 * @version $Id$
 */
class CloneableSimpleScriptContextTest
{
    @Test
    void testClone() throws CloneNotSupportedException
    {
        CloneableSimpleScriptContext context = new CloneableSimpleScriptContext();

        context.setAttribute("engine1", "engine1", ScriptContext.ENGINE_SCOPE);
        CloneableSimpleScriptContext context2 = context.clone();

        assertEquals(context.getAttribute("engine1"), context2.getAttribute("engine1"));

        context.setBindings(new SimpleBindings(), ScriptContext.GLOBAL_SCOPE);
        context.setAttribute("global1", "global1", ScriptContext.GLOBAL_SCOPE);
        context2 = context.clone();

        assertEquals(context.getAttribute("engine1"), context2.getAttribute("engine1"));
        assertEquals(context.getAttribute("global1"), context2.getAttribute("global1"));

        context2.setAttribute("engine2", "engine2", ScriptContext.ENGINE_SCOPE);
        context2.setAttribute("global2", "global2", ScriptContext.GLOBAL_SCOPE);

        assertNull(context.getAttribute("engine2"));
        assertNull(context.getAttribute("global2"));
    }
}
