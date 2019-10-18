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
package org.xwiki.context;

import java.util.EmptyStackException;

import org.xwiki.component.annotation.Role;
import org.xwiki.stability.Unstable;

/**
 * Allows setting/retrieving the {@link ExecutionContext}.
 *
 * @version $Id$
 * @since 1.5M2
 */
@Role
public interface Execution
{
    /**
     * @return the current context of the current thread
     */
    ExecutionContext getContext();

    /**
     * @param context the current context of the current thread
     */
    void setContext(ExecutionContext context);

    /**
     * Push a the passed context and make it inherit the existing one.
     * 
     * @param context create a new context level for the current thread and set the provided context
     * @see #popContext()
     */
    void pushContext(ExecutionContext context);

    /**
     * Push a the passed context.
     * 
     * @param context create a new context level for the current thread and set the provided context
     * @param inherit true if the passed context should inherit the existing one
     * @since 11.9RC1
     */
    @Unstable
    default void pushContext(ExecutionContext context, boolean inherit)
    {
        pushContext(context);
    }

    /**
     * Remove a context level in the current thread.
     *
     * @throws EmptyStackException if this stack is empty.
     */
    void popContext();

    /**
     * Remove all context levels for the current thread.
     */
    void removeContext();
}
