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
package org.xwiki.configuration.internal;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;

/**
 * Runtime configuration source stored in memory.
 * <p>
 * Can be modified by lookuping the component and using {@link MemoryConfigurationSource#setProperty(String, Object)}.
 *
 * @version $Id$
 * @since 3.5M1
 */
@Component
@Singleton
@Named("memory")
public class MemoryConfigurationSource extends AbstractMemoryConfigurationSource
{
    /**
     * The properties.
     */
    private Map<String, Object> properties = new ConcurrentHashMap<>();

    @Override
    protected Map<String, Object> getProperties()
    {
        return this.properties;
    }
}
