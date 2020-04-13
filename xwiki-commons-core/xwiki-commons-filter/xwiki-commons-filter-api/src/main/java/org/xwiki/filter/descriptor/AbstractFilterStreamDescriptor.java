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
package org.xwiki.filter.descriptor;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @version $Id$
 * @since 6.2M1
 */
public abstract class AbstractFilterStreamDescriptor implements FilterStreamDescriptor
{
    /**
     * @see #getName()
     */
    protected String name;

    /**
     * The description of the macro.
     */
    protected String description;

    /**
     * A map containing the {@link FilterStreamPropertyDescriptor} for each parameters supported for this filter.
     * <p>
     * The {@link Map} keys are lower cased for easier case insensitive search, to get the "real" name of the property
     * use {@link FilterStreamPropertyDescriptor#getName()}.
     */
    protected Map<String, FilterStreamPropertyDescriptor<?>> parameterDescriptorMap =
        new LinkedHashMap<>();

    /**
     * @param name human readable name of filter input source type.
     * @param description the description of the filter
     */
    public AbstractFilterStreamDescriptor(String name, String description)
    {
        this.name = name;
        this.description = description;
    }

    // FilterDescriptor

    @Override
    public String getName()
    {
        return this.name;
    }

    @Override
    public String getDescription()
    {
        return this.description;
    }

    @Override
    public <T> FilterStreamPropertyDescriptor<T> getPropertyDescriptor(String propertyName)
    {
        return (FilterStreamPropertyDescriptor<T>) this.parameterDescriptorMap.get(propertyName);
    }

    @Override
    public Collection<FilterStreamPropertyDescriptor<?>> getProperties()
    {
        return Collections.<FilterStreamPropertyDescriptor<?>>unmodifiableCollection(this.parameterDescriptorMap
            .values());
    }
}
