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
package org.xwiki.text;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link XWikiToStringBuilder}.
 *
 * @version $Id$
 * @since 4.0M2
 */
class XWikiToStringBuilderTest
{
    public class TestClass
    {
        private String field1 = "value1";
        private Integer field2 = 100;
        private Map<String, String> field3 = new LinkedHashMap<>();
        private List<String> field4 = Arrays.asList("value");

        public TestClass()
        {
            this.field3.put("key1", "value1");
            this.field3.put("key2", "value2");
        }

        @Override
        public String toString()
        {
            return new XWikiToStringBuilder(this)
                .append("field1", field1)
                .append("field2", field2)
                .append("field3", field3)
                .append("field4", field4)
                .toString();
        }
    }

    @Test
    void toStringBuilder()
    {
        assertEquals("field1 = [value1], field2 = [100], field3 = [[key1] = [value1], [key2] = [value2]], "
            + "field4 = [[value]]", new TestClass().toString());
    }
}
