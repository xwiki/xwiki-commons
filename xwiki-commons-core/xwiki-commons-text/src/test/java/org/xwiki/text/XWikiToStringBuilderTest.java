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

import org.junit.Test;

import junit.framework.Assert;

/**
 * Unit tests for {@link XWikiToStringBuilder}.
 *
 * @version $Id$
 * @since 4.0M2
 */
public class XWikiToStringBuilderTest
{
    public class TestClass
    {
        public String field1 = "value1";
        public Integer field2 = 100;

        @Override
        public String toString()
        {
            return new XWikiToStringBuilder(this).append("field1", field1).append("field2", field2).toString();
        }
    }

    @Test
    public void toStringBuilder()
    {
        Assert.assertEquals("field1 = [value1], field2 = [100]", new TestClass().toString());
    }
}
