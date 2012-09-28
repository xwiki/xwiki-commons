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
package org.xwiki.script.service;

import javax.inject.Inject;

import org.xwiki.context.Execution;

/**
 * Helper class to implement {@link ScriptService}.
 * 
 * @version $Id$
 * @since 4.3M1
 */
public abstract class AbstractScriptService implements ScriptService
{
    /**
     * The unique identifier of the script service.
     */
    private final String id;

    /**
     * The key used with the {@link Execution} to store the last error.
     */
    private final String exceptionKey;

    /**
     * Provides access to the current context.
     */
    @Inject
    private Execution execution;

    /**
     * @param id the unique identifier of the script service
     */
    public AbstractScriptService(String id)
    {
        this.id = id;

        this.exceptionKey = "scriptservice." + this.id + ".exception";

    }

    // Error management

    /**
     * Get the exception generated while performing the previously called action.
     * 
     * @return an eventual exception or {@code null} if no exception was thrown
     */
    public Exception getLastException()
    {
        return (Exception) this.execution.getContext().getProperty(this.exceptionKey);
    }

    /**
     * Store a caught exception in the context, so that it can be later retrieved using {@link #getLastException()}.
     * 
     * @param e the exception to store, can be {@code null} to clear the previously stored exception
     * @see #getLastException()
     */
    protected void setLastException(Exception e)
    {
        this.execution.getContext().setProperty(this.exceptionKey, e);
    }
}
