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

import org.xwiki.stability.Unstable;

/**
 * Extends {@link org.apache.commons.lang3.StringUtils} with some more useful tools. Note that the reason we're
 * extending Commons Lang instead of creating a separate class is because we're using this class as a Velocity tool
 * and we want to offer a single binding to users.
 *
 * @version $Id$
 * @since 6.2
 */
public class StringUtils extends org.apache.commons.lang3.StringUtils
{
    /**
     * An attempt to make doubling a character (usually for escaping purposes) as fast as it can be. A lot faster than
     * the usual <code>mystring.replace("a", "aa")</code> for example.
     *
     * @param str the string to modify
     * @param c the character to double
     * @return the modified string
     */
    public static String doubleChar(final String str, final char c)
    {
        if (isEmpty(str)) {
            return str;
        }

        int start = 0;
        int end = str.indexOf(c, start);
        if (end == INDEX_NOT_FOUND) {
            return str;
        }

        final StringBuilder buf = new StringBuilder(str.length() + 1);

        do {
            end += 1;
            buf.append(str.substring(start, end)).append(c);

            start = end;
            end = str.indexOf(c, start);
        } while (end != INDEX_NOT_FOUND);

        buf.append(str.substring(start));

        return buf.toString();
    }

    /**
     * Removes all non alpha numerical characters from the passed text. First tries to convert accented chars to their
     * alpha numeric representation.
     *
     * @param text the text to convert
     * @return the alpha numeric equivalent
     * @since 10.6RC1
     */
    @Unstable
    public static String convertToAlphaNumeric(String text)
    {
        if (isEmpty(text)) {
            return text;
        }

        String textNoAccents = stripAccents(text);
        StringBuilder result = new StringBuilder(textNoAccents.length());
        for (char textChar : textNoAccents.toCharArray()) {
            if (Character.isLetterOrDigit(textChar) && textChar < 128) {
                result.append(textChar);
            }
        }

        return result.toString();
    }
}
