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

import java.util.HashMap;

import javax.script.SimpleBindings;
import javax.script.SimpleScriptContext;

/**
 * A {@link Cloneable} version of SimpleScriptContext.
 * 
 * @version $Id$
 * @since 11.8RC1
 */
public class CloneableSimpleScriptContext extends SimpleScriptContext implements Cloneable
{
    @Override
    public CloneableSimpleScriptContext clone() throws CloneNotSupportedException
    {
        CloneableSimpleScriptContext cloned = (CloneableSimpleScriptContext) super.clone();

        cloned.engineScope = new SimpleBindings(new HashMap<>(this.engineScope));

        if (this.globalScope != null) {
            cloned.globalScope = new SimpleBindings(new HashMap<>(this.globalScope));
        }

        return cloned;
    }
}
