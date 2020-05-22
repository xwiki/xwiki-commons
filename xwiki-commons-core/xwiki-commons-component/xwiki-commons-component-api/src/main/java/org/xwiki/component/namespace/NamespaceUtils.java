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

import java.util.HashSet;
import java.util.Set;

/**
 * Various namespaces utilities.
 *
 * @version $Id$
 * @since 8.0M1
 */
public final class NamespaceUtils
{
    private static final Set<String> ROOT_NAMESPACES = new HashSet<>();

    static {
        ROOT_NAMESPACES.add("{root}");
        ROOT_NAMESPACES.add("{}");
        ROOT_NAMESPACES.add("");
    };

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
     * @param namespaceString the namespace as a {@link String}
     * @return the type of the namespace or null if none is provided
     */
    public static String getPrefix(String namespaceString)
    {
        Namespace namespace = toNamespace(namespaceString);

        return namespace != null ? namespace.getType() : null;
    }

    /**
     * @param namespace the {@link String} namespace to parse
     * @return the parsed {@link Namespace} or null if null is passed
     * @since 9.0RC1
     */
    public static Namespace toNamespace(String namespace)
    {
        if (namespace == null) {
            return null;
        }

        if (ROOT_NAMESPACES.contains(namespace)) {
            return Namespace.ROOT;
        }

        boolean escaped = false;
        StringBuilder typeBuilder = null;
        for (int i = 0; i < namespace.length(); ++i) {
            char c = namespace.charAt(i);
            if (escaped) {
                typeBuilder.append(c);
                escaped = false;
            } else {
                if (c == '\\') {
                    if (typeBuilder == null) {
                        typeBuilder = new StringBuilder();
                        if (i > 0) {
                            typeBuilder.append(namespace, 0, i);
                        }
                    }
                    escaped = true;
                } else if (c == ':') {
                    String type;
                    if (typeBuilder != null) {
                        type = typeBuilder.toString();
                    } else {
                        type = namespace.substring(0, i);
                    }
                    return new Namespace(type, namespace.substring(i + 1));
                } else if (typeBuilder != null) {
                    typeBuilder.append(c);
                }
            }
        }

        return new Namespace(null, namespace);
    }
}
