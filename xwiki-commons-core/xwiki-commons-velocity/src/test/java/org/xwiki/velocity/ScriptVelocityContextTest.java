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
package org.xwiki.velocity;

import java.util.HashSet;
import java.util.Set;

import javax.script.ScriptContext;
import javax.script.SimpleBindings;
import javax.script.SimpleScriptContext;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for {@link ScriptVelocityContext}.
 *
 * @version $Id$
 */
class ScriptVelocityContextTest
{
    private static final String RESERVED = "reserved";

    /**
     * A script context remembering which attributes were written into it.
     */
    private static final class RecordingScriptContext extends SimpleScriptContext
    {
        private final Set<String> writtenAttributes = new HashSet<>();

        @Override
        public void setAttribute(String name, Object value, int scope)
        {
            this.writtenAttributes.add(name);
            super.setAttribute(name, value, scope);
        }
    }

    private RecordingScriptContext scriptContext;

    private ScriptVelocityContext velocityContext;

    @BeforeEach
    void beforeEach()
    {
        this.scriptContext = new RecordingScriptContext();
        this.scriptContext.setBindings(new SimpleBindings(), ScriptContext.GLOBAL_SCOPE);

        this.velocityContext = new ScriptVelocityContext(null, Set.of(RESERVED));
    }

    @Test
    void setScriptContextCopiesBindings()
    {
        this.scriptContext.setAttribute("global", "globalValue", ScriptContext.GLOBAL_SCOPE);
        this.scriptContext.setAttribute("engine", "engineValue", ScriptContext.ENGINE_SCOPE);
        this.scriptContext.setAttribute(RESERVED, "reservedValue", ScriptContext.ENGINE_SCOPE);

        this.velocityContext.setScriptContext(this.scriptContext);

        assertEquals("globalValue", this.velocityContext.get("global"));
        assertEquals("engineValue", this.velocityContext.get("engine"));
        assertNull(this.velocityContext.get(RESERVED));
    }

    @Test
    void setScriptContextOnlyCopiesChangedBindings()
    {
        this.scriptContext.setAttribute("unchanged", "unchangedValue", ScriptContext.ENGINE_SCOPE);
        this.scriptContext.setAttribute("changed", "oldValue", ScriptContext.ENGINE_SCOPE);
        this.velocityContext.setScriptContext(this.scriptContext);

        this.scriptContext.setAttribute("changed", "newValue", ScriptContext.ENGINE_SCOPE);
        this.scriptContext.setAttribute("added", "addedValue", ScriptContext.ENGINE_SCOPE);
        this.scriptContext.writtenAttributes.clear();

        this.velocityContext.setScriptContext(this.scriptContext);

        assertEquals("unchangedValue", this.velocityContext.get("unchanged"));
        assertEquals("newValue", this.velocityContext.get("changed"));
        assertEquals("addedValue", this.velocityContext.get("added"));
        // The bindings already in sync are not put again, which would write them back into the script context.
        assertEquals(Set.of("changed", "added"), this.scriptContext.writtenAttributes);
    }

    @Test
    void setScriptContextKeepsGlobalBindingOverridingEngineBinding()
    {
        this.scriptContext.setAttribute("shared", "globalValue", ScriptContext.GLOBAL_SCOPE);
        this.scriptContext.setAttribute("shared", "engineValue", ScriptContext.ENGINE_SCOPE);
        this.velocityContext.setScriptContext(this.scriptContext);

        assertEquals("globalValue", this.velocityContext.get("shared"));
        assertEquals("globalValue", this.scriptContext.getAttribute("shared", ScriptContext.ENGINE_SCOPE));

        // Script context initializers set the engine binding again before each script execution.
        this.scriptContext.setAttribute("shared", "newEngineValue", ScriptContext.ENGINE_SCOPE);
        this.velocityContext.setScriptContext(this.scriptContext);

        assertEquals("globalValue", this.velocityContext.get("shared"));
        assertEquals("globalValue", this.scriptContext.getAttribute("shared", ScriptContext.ENGINE_SCOPE));
    }

    @Test
    void setScriptContextSkipsGlobalBindingsInSync()
    {
        this.scriptContext.setAttribute("global", "globalValue", ScriptContext.GLOBAL_SCOPE);
        this.velocityContext.setScriptContext(this.scriptContext);
        this.scriptContext.writtenAttributes.clear();

        this.velocityContext.setScriptContext(this.scriptContext);

        assertEquals("globalValue", this.velocityContext.get("global"));
        assertEquals(Set.of(), this.scriptContext.writtenAttributes);
    }

    @Test
    void velocityVariablesAreWrittenToTheScriptContext()
    {
        this.velocityContext.setScriptContext(this.scriptContext);

        this.velocityContext.put("velocityVariable", "velocityValue");
        this.velocityContext.put(RESERVED, "reservedValue");

        assertEquals("velocityValue", this.scriptContext.getAttribute("velocityVariable", ScriptContext.ENGINE_SCOPE));
        assertNull(this.scriptContext.getAttribute(RESERVED, ScriptContext.ENGINE_SCOPE));

        this.velocityContext.remove("velocityVariable");

        assertNull(this.scriptContext.getAttribute("velocityVariable", ScriptContext.ENGINE_SCOPE));
    }
}
