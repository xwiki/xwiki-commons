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
package org.xwiki.cache.infinispan.internal;

import java.io.InputStream;

import org.infinispan.commons.configuration.io.ConfigurationResourceResolvers;
import org.infinispan.commons.dataconversion.MediaType;
import org.infinispan.configuration.parsing.ConfigurationBuilderHolder;
import org.infinispan.configuration.parsing.ParserRegistry;

/**
 * Various helper for Infinispan.
 * 
 * @version $Id$
 * @since 15.7RC1
 */
public final class InfinispanUtils
{
    private InfinispanUtils()
    {
        // Utility class
    }

    /**
     * Parses the supplied {@link InputStream} returning a new {@link ConfigurationBuilderHolder}.
     * 
     * @param is an {@link InputStream} pointing to a configuration file
     * @return a new {@link ConfigurationBuilderHolder} which contains the parsed configuration
     */
    public static ConfigurationBuilderHolder parse(InputStream is)
    {
        return new ParserRegistry().parse(is, ConfigurationResourceResolvers.DEFAULT, MediaType.APPLICATION_XML);

    }
}
