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

import java.io.Reader;
import java.io.Writer;

import javax.script.ScriptContext;

import org.apache.velocity.VelocityContext;
import org.xwiki.component.annotation.Role;

/**
 * Provides access to the main XWiki Velocity objects.
 *
 * @since 1.5M2
 * @version $Id$
 */
@Role
public interface VelocityManager
{
    /**
     * @return the up to date current Velocity Context
     */
    VelocityContext getVelocityContext();

    /**
     * @return the current Velocity Context without any modification
     * @since 8.3M1
     */
    default VelocityContext getCurrentVelocityContext()
    {
        return getVelocityContext();
    }

    /**
     * Get the current Velocity Engine or create one if none has been created.
     *
     * @return the current Velocity Engine retrieved from the Execution Context
     * @throws XWikiVelocityException if the Velocity Engine cannot be created
     */
    // TODO: Move the engine creation to some initialization method instead and remove the need for throwing an
    // exception
    VelocityEngine getVelocityEngine() throws XWikiVelocityException;

    /**
     * Renders the input string using the context into the output writer.
     * <p>
     * The current {@link ScriptContext} will be used and updated after the execution.
     * <p>
     * Anything set in the current {@link VelocityContext} will also be taken into account.
     *
     * @param out the writer in which to render the output
     * @param templateName the string to be used as the template name for log messages in case of error. Also used
     *            internally by Velocity as a cache index key for caching macros.
     * @param source the input containing the VTL to be rendered, as a Reader
     * @return false if empty, true otherwise
     * @throws XWikiVelocityException in case of error
     * @since 8.3M1
     */
    default boolean evaluate(Writer out, String templateName, Reader source) throws XWikiVelocityException
    {
        return getVelocityEngine().evaluate(getVelocityContext(), out, templateName, source);
    }
}
