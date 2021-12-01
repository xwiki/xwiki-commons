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

import java.util.concurrent.Callable;

import org.xwiki.component.annotation.Role;

/**
 * Allow executing some code in the context of the specified namespace.
 *
 * @version $Id$
 * @since 10.6RC1
 * @since 10.5
 * @since 9.11.6
 */
@Role
public interface NamespaceContextExecutor
{
    /**
     * Execute the passed {@link Callable} in the context of the specified namespace.
     *
     * @param namespace the namespace to use
     * @param callable the task to execute
     * @param <V> the result type of method {@code call}
     * @return computed result
     * @throws Exception if unable to compute a result
     */
    <V> V execute(Namespace namespace, Callable<V> callable) throws Exception;
}
