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
import java.util.Map;
import java.util.Properties;

import org.apache.velocity.context.Context;
import org.xwiki.component.annotation.Role;

/**
 * Initialize a Velocity Engine and make Velocity services available.
 * <p>
 * It's highly recommended to use {@link VelocityManager} to get a {@link VelocityEngine} instance. Directly injecting
 * {@link VelocityEngine} never provided a fully initialized instance.
 *
 * @version $Id$
 */
@Role
public interface VelocityEngine
{
    /**
     * Initializes the Velocity engine by setting its configuration both from the component's configuration and from the
     * passed properties. This method must be called before any other method from this class can be executed.
     *
     * @param properties the properties that will override the static properties defined in the component's
     *            configuration
     * @throws XWikiVelocityException in case of error
     * @deprecated get {@link VelocityEngine} through {@link VelocityManager} instead
     */
    @Deprecated(since = "15.9RC1")
    default void initialize(Properties properties) throws XWikiVelocityException
    {

    }

    /**
     * Evaluate the input string using the context into the output writer.
     *
     * @param context the Velocity context to use in rendering the input string, it's recommended to pass a
     *            {@link XWikiVelocityContext} to have retro compatibility support (like old $velocitycount binding)
     * @param out the writer in which to render the output
     * @param namespace the string to be used as the template name for log messages in case of error. Also used as
     *            namespace for the macros. Empty string means global namespace.
     * @param source the input string containing the VTL to be rendered
     * @return true if successful, false otherwise. If false, see the Velocity runtime log
     * @throws XWikiVelocityException in case of error
     */
    boolean evaluate(Context context, Writer out, String namespace, String source) throws XWikiVelocityException;

    /**
     * Evaluate the input string using the context into the output writer.
     *
     * @param context the Velocity context to use in rendering the input string
     * @param out the writer in which to render the output
     * @param namespace the string to be used as the template name for log messages in case of error. Also used as
     *            namespace for the macros. Empty string means global namespace.
     * @param source the input containing the VTL to be rendered, as a Reader
     * @return false if empty, true otherwise
     * @throws XWikiVelocityException in case of error
     */
    boolean evaluate(Context context, Writer out, String namespace, Reader source) throws XWikiVelocityException;

    /**
     * Evaluate the template using the context into the output writer.
     *
     * @param context the Velocity context to use in rendering the input string
     * @param out the writer in which to render the output
     * @param namespace the string to be used as the template name for log messages in case of error. Also used as
     *            namespace for the macros. Empty string means global namespace.
     * @param template the compiled template to execute
     * @throws XWikiVelocityException in case of error
     * @since 15.8RC1
     */
    void evaluate(Context context, Writer out, String namespace, VelocityTemplate template)
        throws XWikiVelocityException;

    /**
     * Clear the internal Velocity Macro cache for the passed namespace.
     *
     * @param namespace the namespace for which to remove all cached Velocity macros
     * @since 2.4M2
     * @deprecated since 10.5RC1, the macros are now stored in the execution context so this method does not make much
     *             sense anymore
     */
    @Deprecated
    default void clearMacroNamespace(String namespace)
    {
        
    }

    /**
     * Notify that a rendering action is starting in the given namespace.
     *
     * @param namespace the namespace being used
     * @since 2.4RC1
     */
    void startedUsingMacroNamespace(String namespace);

    /**
     * Notify that a rendering action in the given namespace just finished.
     *
     * @param namespace the namespace which was used
     * @since 2.4RC1
     */
    void stoppedUsingMacroNamespace(String namespace);

    /**
     * @param macros the macros to reuse with scripts executed by this engine
     * @since 15.8RC1
     */
    default void addGlobalMacros(Map<String, Object> macros)
    {

    }
}
