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
package org.xwiki.extension.maven.internal;

import org.apache.maven.model.Dependency;
import org.xwiki.extension.ExtensionDependency;

/**
 * An {@link ExtensionDependency} specific to Maven.
 * 
 * @version $Id$
 * @since 7.3M1
 */
public interface MavenExtensionDependency extends ExtensionDependency
{
    /**
     * @return the Maven dependency object
     */
    Dependency getMavenDependency();

    /**
     * @return the scope of dependency
     * @since 8.1M1
     */
    String getScope();

    /**
     * @return the type in case it's different from the default type of the extension
     * @since 12.4RC1
     */
    String getMavenType();
}
