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
package org.xwiki.velocity.internal;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.runtime.directive.ForeachScope;

/**
 * Extends the standard {@link VelocityContext} to add some retro compatibility (for example support for $velocityCount
 * and $velocityHasNext).
 * 
 * @version $Id$
 * @since 10.6RC1
 */
public class RetroVelocityContext extends VelocityContext
{
    private ForeachScope getForeachScope()
    {
        return (ForeachScope) get("foreach");
    }

    private Integer getVelocityCount()
    {
        ForeachScope foreachScope = getForeachScope();

        return foreachScope != null ? foreachScope.getCount() : null;
    }

    private Boolean getVelocityHasNext()
    {
        ForeachScope foreachScope = getForeachScope();

        return foreachScope != null ? foreachScope.hasNext() : null;
    }

    @Override
    public Object get(String key)
    {
        Object value;

        if (!containsKey(key)) {
            // Retro compatibility
            switch (key) {
                // Replaced by $foreach.count
                case "velocityCount":
                    value = getVelocityCount();
                    if (value != null) {
                        return value;
                    }

                    break;

                // Replaced by $foreach.hasNext
                case "velocityHasNext":
                    value = getVelocityHasNext();
                    if (value != null) {
                        return value;
                    }

                    break;

                default:
                    break;
            }
        }

        return super.get(key);
    }
}
