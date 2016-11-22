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
package org.xwiki.component.internal.namespace;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.namespace.NamespaceNotAllowedException;
import org.xwiki.component.namespace.NamespaceValidator;

/**
 * Default implementation of {@link NamespaceValidator}.
 * 
 * @version $Id$
 * @since 8.0M1
 */
@Component
@Singleton
public class DefaultNamespaceValidator implements NamespaceValidator
{
    private static final Pattern REGEXP_PATTERN = Pattern.compile("\\[(.*)\\]");

    private static final Set<String> ROOT_NAMESPACES = new HashSet<>();

    static {
        ROOT_NAMESPACES.add("{root}");
        ROOT_NAMESPACES.add("{}");
    };

    @Inject
    @Named("context")
    private Provider<ComponentManager> componentManager;

    @Override
    public boolean isAllowed(Collection<String> allowedNamespaces, String namespace)
    {
        // Null list means any namespace
        if (allowedNamespaces == null) {
            return true;
        }

        for (String allowedNamespace : allowedNamespaces) {
            if (isAllowed(allowedNamespace, namespace)) {
                return true;
            }
        }

        return false;
    }

    private boolean isAllowed(String allowedNamespace, String namespace)
    {
        // Check if it's the same namespace
        if (StringUtils.equals(allowedNamespace, namespace)) {
            return true;
        }

        // Check root namespaces aliases
        if (namespace == null) {
            return ROOT_NAMESPACES.contains(allowedNamespace);
        }

        // Check if it's a regexp
        if (allowedNamespace != null) {
            Matcher matcher = REGEXP_PATTERN.matcher(allowedNamespace);
            if (matcher.matches()) {
                String pattern = matcher.group(1);
                return namespace.matches(pattern);
            }
        }

        return false;
    }

    @Override
    public void checkAllowed(Collection<String> allowedNamespaces, String namespace) throws NamespaceNotAllowedException
    {
        if (!isAllowed(allowedNamespaces, namespace)) {
            throw new NamespaceNotAllowedException(
                "Allowed namespace list [" + allowedNamespaces + "] does not matches namespace [" + namespace + "]");
        }
    }
}
