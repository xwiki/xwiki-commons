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
package org.xwiki.cache;

import java.time.chrono.ChronoLocalDateTime;
import java.util.Date;

import org.xwiki.component.annotation.Role;
import org.xwiki.stability.Unstable;

/**
 * Control the caches behavior in the current context.
 * 
 * @version $Id$
 * @since 11.8RC1
 */
@Unstable
@Role
public interface CacheControl
{
    /**
     * @param enabled true if it's allowed to get a resource from a cache
     */
    void setCacheReadAllowed(boolean enabled);

    /**
     * @param dateTime the date and time of the resource
     * @return true if it's allowed to use the resource cached at the passed {@link Date}
     */
    boolean isCacheReadAllowed(Date dateTime);

    /**
     * @param dateTime the date and time of the resource
     * @return true if it's allowed to use the resource cached at the passed {@link Date}
     */
    boolean isCacheReadAllowed(ChronoLocalDateTime<?> dateTime);

    /**
     * @return true if it's allowed to use the cache resources
     */
    boolean isCacheReadAllowed();

    // TODO: add isCacheStoreAllowed() ?
}
