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

import java.util.Map;
import java.util.Set;

import javax.script.Bindings;
import javax.script.ScriptContext;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.runtime.directive.Scope;

/**
 * Maintains the current ScriptContext in sync with any modification of the VelocityContext.
 * 
 * @version $Id$
 * @since 15.9RC1
 */
public class ScriptVelocityContext extends XWikiVelocityContext
{
    private final Set<String> reservedBindings;

    private ScriptContext scriptContext;

    /**
     * @param parent the initial Velocity context
     * @param reservedBindings the binding that should not be synchronized
     */
    public ScriptVelocityContext(VelocityContext parent, Set<String> reservedBindings)
    {
        super(parent);

        this.reservedBindings = reservedBindings;
    }

    /**
     * @param parent the initial Velocity context
     * @param logDeprecated true if use of deprecated binding should be logged
     * @param reservedBindings the binding that should not be synchronized
     */
    public ScriptVelocityContext(VelocityContext parent, boolean logDeprecated, Set<String> reservedBindings)
    {
        super(parent, logDeprecated);

        this.reservedBindings = reservedBindings;
    }

    /**
     * @return the current script context
     */
    public ScriptContext getScriptContext()
    {
        return this.scriptContext;
    }

    /**
     * @param scriptContext the current script context
     */
    public void setScriptContext(ScriptContext scriptContext)
    {
        this.scriptContext = scriptContext;

        if (this.scriptContext != null) {
            copyScriptContext(ScriptContext.GLOBAL_SCOPE);
            copyScriptContext(ScriptContext.ENGINE_SCOPE);
        }
    }

    private void copyScriptContext(int scope)
    {
        Bindings bindings = this.scriptContext.getBindings(scope);
        if (bindings != null) {
            Bindings engineBindings = this.scriptContext.getBindings(ScriptContext.ENGINE_SCOPE);
            for (Map.Entry<String, Object> entry : bindings.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                if (!this.reservedBindings.contains(key)) {
                    Object currentValue = get(key);
                    // Don't replace internal Velocity bindings, and skip values that are already in sync.
                    if (!(currentValue instanceof Scope)
                        && (currentValue != value || !isInEngineScope(key, value, scope, engineBindings))) {
                        put(key, value);
                    }
                }
            }
        }
    }

    private boolean isInEngineScope(String key, Object value, int scope, Bindings engineBindings)
    {
        // Putting a binding also writes it to the engine scope, so that a global binding overrides an engine binding
        // with the same name. A global binding is thus only in sync when the engine scope holds it too.
        return scope == ScriptContext.ENGINE_SCOPE
            || (engineBindings != null && engineBindings.containsKey(key) && engineBindings.get(key) == value);
    }

    // VelocityContext

    @Override
    public Object internalPut(String key, Object value)
    {
        try {
            return super.internalPut(key, value);
        } finally {
            if (this.scriptContext != null && !this.reservedBindings.contains(key)) {
                this.scriptContext.setAttribute(key, value, ScriptContext.ENGINE_SCOPE);
            }
        }
    }

    @Override
    public Object internalRemove(String key)
    {
        try {
            return super.internalRemove(key);
        } finally {
            if (this.scriptContext != null && !this.reservedBindings.contains(key)) {
                this.scriptContext.removeAttribute(key, ScriptContext.ENGINE_SCOPE);
            }
        }
    }
}
