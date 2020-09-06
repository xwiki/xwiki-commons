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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link XMLAttributeValue}.
 *
 * @version $Id$d
 */
class XMLAttributeValueTest
{
    @Test
    void addValueToEmptyAttributeSpecifiedWithNull()
    {
        XMLAttributeValue value = new XMLAttributeValue(null);
        value.addValue("one");
        assertEquals("one", value.toString());
    }

    @Test
    void addValueToNonEmptyAttribute()
    {
        XMLAttributeValue value = new XMLAttributeValue("one");
        value.addValue("two");
        assertEquals("one two", value.toString());
    }

    @Test
    void addValuesToEmptyAttributeSpecifiedWithEmptyString()
    {
        XMLAttributeValue value = new XMLAttributeValue("");
        value.addValue("one");
        assertEquals("one", value.toString());
    }

    @Test
    void addValuesToNonEmptyAttribute()
    {
        XMLAttributeValue value = new XMLAttributeValue("one");
        value.addValue("two");
        value.addValues("three", "four");
        assertEquals("one two three four", value.toString());
    }

    @Test
    void addValuesWhenNull()
    {
        XMLAttributeValue value = new XMLAttributeValue("one");
        value.addValues(null);
        assertEquals("one", value.toString());
    }
}
