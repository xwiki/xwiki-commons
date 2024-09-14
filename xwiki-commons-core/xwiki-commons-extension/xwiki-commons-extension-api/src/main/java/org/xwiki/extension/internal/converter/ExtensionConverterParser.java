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
package org.xwiki.extension.internal.converter;

/**
 * Helper to parse slash separated objects.
 * 
 * @version $Id$
 * @since 16.7.0RC1
 */
public class ExtensionConverterParser
{
    private final String value;

    private final int length;

    private int begin;

    private int end;

    private int elementCount;

    private StringBuilder builder;

    /**
     * @param value the value to escape
     * @return the escaped value
     */
    public static final String escape(String value)
    {
        if (value == null) {
            return value;
        }

        return value.replace("\\", "\\\\").replace("/", "\\/");
    }

    public static final String toString(Object... elements)
    {
        StringBuilder builder = new StringBuilder();

        int last = elements.length - 1;
        for (int i = 0; i < elements.length; ++i) {
            Object element = elements[i];

            // If the last element is not serialized we don't need the separator
            if (i > 0 && (i != last || element != null)) {
                builder.append('/');
            }

            if (element != null) {
                String elementString = element.toString();

                // No need to escape the last element
                if (i != last) {
                    elementString = ExtensionConverterParser.escape(elementString);
                }

                builder.append(elementString);
            }
        }

        return builder.toString();
    }

    /**
     * @param value the value to parse
     */
    public ExtensionConverterParser(String value)
    {
        this.value = value;
        this.length = value.length();
        this.begin = 0;
        this.end = 0;
        this.elementCount = 0;
    }

    /**
     * @param separator if true, stop at next separator, otherwise go to the end of the string
     * @return the new element
     */
    public String next(boolean separator)
    {
        if (this.elementCount == 0 && this.length == 0) {
            this.elementCount = 1;

            return "";
        } else if (this.end == this.length) {
            return null;
        }

        // Move the beginning if not the first element
        if (this.elementCount > 0) {
            this.begin = this.end + 1;
        }

        // Add a new element
        ++this.elementCount;

        // Reset the previous builder
        this.builder = null;

        // If no separator, then return the rest of the String
        if (!separator) {
            return this.value.substring(this.begin);
        }

        // Parse the string and stop at first separator or at the end
        boolean escaped = false;
        for (this.end = this.begin; this.end < this.value.length(); ++this.end) {
            char c = this.value.charAt(this.end);

            if (escaped) {
                escaped = false;

                // The character is escaped, add it to the buffer
                getBuilder().append(c);
            } else {
                if (c == '\\') {
                    escaped = true;
                } else if (c == '/') {
                    break;
                } else {
                    // Nothing special, add the character to the buffer
                    getBuilder().append(c);
                }
            }
        }

        return this.builder != null ? this.builder.toString() : "";
    }

    private StringBuilder getBuilder()
    {
        if (this.builder == null) {
            this.builder = new StringBuilder(this.length - this.begin);
        }

        return this.builder;
    }
}
