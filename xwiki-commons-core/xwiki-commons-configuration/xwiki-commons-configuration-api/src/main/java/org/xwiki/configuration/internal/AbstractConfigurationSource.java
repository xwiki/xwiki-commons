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
import java.util.Properties;

import org.xwiki.configuration.ConfigurationSource;

/**
 * Base class to use to implement {@link ConfigurationSource}.
 *
 * @version $Id$
 * @since 3.5M1
 */
public abstract class AbstractConfigurationSource implements ConfigurationSource
{
    /**
     * @param valueClass the class of the property
     * @param <T> the type of the property
     * @return the default value of a property for the provided class
     */
    protected <T> T getDefault(Class<T> valueClass)
    {
        T result = null;

        if (valueClass != null) {
            if (List.class == valueClass) {
                result = valueClass.cast(Collections.emptyList());
            } else if (Properties.class == valueClass) {
                result = valueClass.cast(new Properties());
            }
        }

        return result;
    }
}
