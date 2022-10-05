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
package org.xwiki.velocity.tools;

import org.apache.commons.lang3.StringUtils;
import org.xwiki.stability.Unstable;

/**
 * @version $Id$
 * @since 14.9RC1
 */
@Unstable
public class XWiki21EscapeTool
{
    private EscapeTool escapeTool;

    /**
     * Default constructor.
     *
     * @param escapeTool the main escape tool
     */
    public XWiki21EscapeTool(EscapeTool escapeTool)
    {
        this.escapeTool = escapeTool;
    }

    /**
     * Escape values to safely use them as parameters of a xwiki macro expecting an array or a collection.
     *
     * @param values the values to escape
     * @return the escaped value
     */
    public String parameterArray(String... values)
    {
        String[] ret = new String[values.length];
        for (int valuesIndex = 0; valuesIndex < values.length; valuesIndex++) {
            ret[valuesIndex] = String.valueOf(parameterArray(values[valuesIndex]));
        }
        return StringUtils.join(ret, ",");
    }

    private char[] parameterArray(String value)
    {
        String javaEscaped = String.format("\"%s\"", this.escapeTool.java(value));
        char[] xwiki21Escaped = new char[javaEscaped.length() * 2];
        for (int i = 0; i < javaEscaped.length(); i++) {
            // TODO: duplicated?
            xwiki21Escaped[i * 2] = '~';
            xwiki21Escaped[(i * 2) + 1] = javaEscaped.charAt(i);
        }
        return xwiki21Escaped;
    }
}
