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
package org.xwiki.velocity.internal.util;

import java.net.URI;

import org.apache.velocity.app.event.IncludeEventHandler;
import org.apache.velocity.context.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Velocity event handler that filters #parse calls to forbid including files outside the templates directory.
 *
 * @version $Id$
 * @since 3.5M1
 */
public class RestrictParseLocationEventHandler implements IncludeEventHandler
{
    /** Logging helper object. */
    private static final Logger LOGGER = LoggerFactory.getLogger(RestrictParseLocationEventHandler.class);

    /**
     * Base template directory from where template inclusion is allowed.
     */
    private static final String BASE_TEMPLATE_DIRECTORY = "/templates/";

    @Override
    public String includeEvent(Context context, String includeResourcePath, String currentResourcePath,
        String directiveName)
    {
        LOGGER.trace("Velocity include event: include [{}] from [{}] using [{}]", includeResourcePath,
            currentResourcePath, directiveName);
        String template = URI.create(BASE_TEMPLATE_DIRECTORY + includeResourcePath).normalize().toString();
        if (!template.startsWith(BASE_TEMPLATE_DIRECTORY)) {
            LOGGER.warn("Direct access to template file [{}] refused. Possible break-in attempt!", template);

            return null;
        }

        return template;
    }
}
