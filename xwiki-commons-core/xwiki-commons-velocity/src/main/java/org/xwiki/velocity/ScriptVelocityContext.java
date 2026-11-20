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
     * Set the current script context and copy its bindings into this Velocity context. As in the JSR-223 attribute
     * lookup, an engine scope binding hides a global scope binding with the same name.
     *
     * @param scriptContext the current script context
     */
    public void setScriptContext(ScriptContext scriptContext)
    {
        this.scriptContext = scriptContext;

        if (this.scriptContext != null) {
            Bindings engineBindings = this.scriptContext.getBindings(ScriptContext.ENGINE_SCOPE);
            copyBindings(engineBindings, null);
            copyBindings(this.scriptContext.getBindings(ScriptContext.GLOBAL_SCOPE), engineBindings);
        }
    }

    private void copyBindings(Bindings bindings, Bindings hidingBindings)
    {
        if (bindings != null) {
            for (Map.Entry<String, Object> entry : bindings.entrySet()) {
                String key = entry.getKey();
                if (!this.reservedBindings.contains(key) && !isHidden(key, hidingBindings)) {
                    Object value = entry.getValue();
                    Object currentValue = get(key);
                    // Don't replace internal Velocity bindings, and skip values that are already in sync.
                    if (currentValue != value && !(currentValue instanceof Scope)) {
                        // The value comes from the script context, so only the Velocity context needs to be updated.
                        super.internalPut(key, value);
                    }
                }
            }
        }
    }

    private boolean isHidden(String key, Bindings hidingBindings)
    {
        return hidingBindings != null && hidingBindings.containsKey(key);
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
