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
package org.xwiki.cache.internal;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.chrono.ChronoLocalDateTime;
import java.util.Date;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.ObjectUtils;
import org.xwiki.cache.CacheControl;
import org.xwiki.component.annotation.Component;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;

/**
 * Default implementation of {@link CacheControl}.
 * 
 * @version $Id$
 * @since 11.8RC1
 */
@Component
@Singleton
public class DefaultCacheControl implements CacheControl
{
    private static final String MAX_AGE = "cache.maxage";

    @Inject
    private Execution execution;

    private void setProperty(String key, Object value)
    {
        ExecutionContext context = this.execution.getContext();

        if (context != null) {
            if (context.hasProperty(key)) {
                context.setProperty(key, value);
            } else {
                context.newProperty(key).inherited().initial(value).declare();
            }
        }
    }

    private <T> T getProperty(String key, T def)
    {
        ExecutionContext context = this.execution.getContext();

        return context != null ? (T) ObjectUtils.defaultIfNull(context.getProperty(key), def) : def;
    }

    @Override
    public void setCacheReadAllowed(boolean enabled)
    {
        if (enabled) {
            setProperty(MAX_AGE, null);
        } else {
            setProperty(MAX_AGE, LocalDateTime.now());
        }
    }

    @Override
    public boolean isCacheReadAllowed(Date dateTime)
    {
        return isCacheReadAllowed(dateTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
    }

    @Override
    public boolean isCacheReadAllowed(ChronoLocalDateTime<?> dateTime)
    {
        LocalDateTime maxAge = getProperty(MAX_AGE, null);

        if (maxAge != null) {
            return maxAge.isBefore(dateTime);
        }

        return true;
    }

    @Override
    public boolean isCacheReadAllowed()
    {
        return getProperty(MAX_AGE, null) == null;
    }
}
