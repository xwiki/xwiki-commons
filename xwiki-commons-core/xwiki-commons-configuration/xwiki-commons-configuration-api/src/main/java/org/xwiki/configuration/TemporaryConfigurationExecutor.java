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
package org.xwiki.configuration;

import java.util.Map;
import java.util.concurrent.Callable;

import org.xwiki.component.annotation.Role;
import org.xwiki.stability.Unstable;

/**
 * Executes a {@link Callable} using a temporary configuration.
 * 
 * @version $Id$
 * @since 16.1.0RC1
 * @since 15.10.6
 */
@Role
@Unstable
public interface TemporaryConfigurationExecutor
{
    /**
     * Executes the passed {@link Callable} using the given temporary configuration.
     *
     * @param sourceHint indicates the configuration source that should receive the temporary configuration
     * @param temporaryConfiguration the temporary configuration to use while executing the passed {@link Callable}
     * @param callable the code to execute
     * @param <V> the type of value returned by the passed {@link Callable}
     * @return the value returned by the passed {@link Callable}
     * @throws Exception if the passed {@link Callable} throws an exception
     */
    <V> V call(String sourceHint, Map<String, Object> temporaryConfiguration, Callable<V> callable) throws Exception;
}
