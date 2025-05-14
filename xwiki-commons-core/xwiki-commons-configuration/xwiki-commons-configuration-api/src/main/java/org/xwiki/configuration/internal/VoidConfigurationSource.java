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

import java.util.Collections;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;

/**
 * Implementation of {@link org.xwiki.configuration.ConfigurationSource} mimicking an empty configuration.
 *
 * @version $Id$
 * @since 3.5M1
 */
@Component
@Singleton
@Named("void")
public class VoidConfigurationSource extends AbstractConfigurationSource
{
    @Override
    public <T> T getProperty(String key, T defaultValue)
    {
        return defaultValue;
    }

    @Override
    public <T> T getProperty(String key, Class<T> valueClass)
    {
        return getDefault(valueClass);
    }

    @Override
    public <T> T getProperty(String key)
    {
        return null;
    }

    @Override
    public List<String> getKeys()
    {
        return Collections.emptyList();
    }

    @Override
    public List<String> getKeys(String prefix)
    {
        return Collections.emptyList();
    }

    @Override
    public boolean containsKey(String key)
    {
        return false;
    }

    @Override
    public boolean isEmpty()
    {
        return true;
    }

    @Override
    public boolean isEmpty(String prefix)
    {
        return true;
    }
}
