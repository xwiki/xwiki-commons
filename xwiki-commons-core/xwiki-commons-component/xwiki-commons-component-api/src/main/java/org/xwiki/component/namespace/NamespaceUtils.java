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
package org.xwiki.component.namespace;

/**
 * Various namespaces utilities.
 *
 * @version $Id$
 * @since 8.0M1
 */
public final class NamespaceUtils
{
    /**
     * Utility class.
     */
    private NamespaceUtils()
    {
        // Utility class
    }

    /**
     * Extract prefix of the id used to find custom factory.
     *
     * @param id the identifier of the component manager to create
     * @return the prefix of the id or null if none is provided
     */
    public static String getPrefix(String id)
    {
        boolean escaped = false;
        StringBuilder typeBuilder = null;
        for (int i = 0; i < id.length(); ++i) {
            char c = id.charAt(i);
            if (escaped) {
                typeBuilder.append(c);
            } else {
                if (c == '\\') {
                    if (typeBuilder == null) {
                        typeBuilder = new StringBuilder();
                        typeBuilder.append(id.substring(0, i));
                    }
                } else if (c == ':') {
                    return id.substring(0, i);
                } else if (typeBuilder != null) {
                    typeBuilder.append(c);
                }
            }
        }

        return null;
    }
}
