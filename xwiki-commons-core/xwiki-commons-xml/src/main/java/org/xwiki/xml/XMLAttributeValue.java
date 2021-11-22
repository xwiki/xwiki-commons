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
package org.xwiki.xml;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

/**
 * Represents an XML attribute value.
 *
 * @version $Id$
 * @since 12.8RC1
 */
public class XMLAttributeValue
{
    /**
     * The separator character used to separate attribute values.
     */
    private static final String CLASS_SEPARATOR = " ";

    private List<String> values = new ArrayList<>();

    /**
     * @param value the initial value of the attribute, can be null if the tag doesn't have any value
     */
    public XMLAttributeValue(String value)
    {
        addValue(value);
    }

    /**
     * @param value the value to add to the existing attribute value
     */
    public void addValue(String value)
    {
        if (value != null && !StringUtils.isEmpty(value.trim())) {
            this.values.add(value);
        }
    }

    /**
     * @param values the values to add to the existing attribute value
     */
    public void addValues(String... values)
    {
        if (values != null) {
            for (String value : values) {
                addValue(value);
            }
        }
    }

    @Override
    public String toString()
    {
        return StringUtils.join(this.values, CLASS_SEPARATOR);
    }
}
