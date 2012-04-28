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

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang3.builder.StandardToStringStyle;

/**
 * Custom XWiki Style for {@link org.apache.commons.lang3.builder.ToStringBuilder}.
 * Generates {@code toString()} result of the format:
 * <pre><code>
 * Typed = [true] Type = [doc] Reference = [reference] Base References = [[baseref1], [baseref2]],
 *   Parameters = [[name1] = [value1], [name2] = [value2]]
 * </code></pre>
 *
 * @version $Id$
 * @since 4.0M2
 */
public class XWikiToStringStyle extends StandardToStringStyle
{
    /**
     * Sets the XWiki style.
     */
    public XWikiToStringStyle()
    {
        super();
        setUseClassName(false);
        setUseIdentityHashCode(false);
        setContentStart("");
        setContentEnd("]");
        setFieldNameValueSeparator(" = [");
        setSeparator(",");
    }

    /**
     * @param separator the separator to use between fields
     */
    public void setSeparator(String separator)
    {
        setFieldSeparator(String.format("]%s ", separator));
    }

    @Override
    protected void appendDetail(StringBuffer buffer, String fieldName, Collection<?> coll)
    {
        Iterator<?> it = coll.iterator();
        while (it.hasNext()) {
            Object value = it.next();
            buffer.append('[');
            buffer.append(value);
            buffer.append(']');
            if (it.hasNext()) {
                buffer.append(',');
                buffer.append(' ');
            }
        }
    }

    @Override
    protected void appendDetail(StringBuffer buffer, String fieldName, Map map)
    {
        Iterator it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            buffer.append('[');
            buffer.append(entry.getKey());
            buffer.append(']');
            buffer.append(' ');
            buffer.append('=');
            buffer.append(' ');
            buffer.append('[');
            buffer.append(entry.getValue());
            buffer.append(']');
            if (it.hasNext()) {
                buffer.append(',');
                buffer.append(' ');
            }
        }
    }
}
