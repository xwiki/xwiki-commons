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
package org.xwiki.extension;

import java.util.regex.Pattern;

import org.xwiki.stability.Unstable;

/**
 * An extension exclusion.
 *
 * @version $Id$
 * @since 12.2
 */
@Unstable
public interface ExtensionPattern
{
    /**
     * @return a regular expression matching all the ids to exclude or null to match everything
     */
    Pattern getIdPattern();

    /**
     * @param extensionId the extension id to match
     * @return true if the passed extension is matched by the pattern
     */
    boolean matches(ExtensionId extensionId);

    /**
     * @param dependency the extension dependency to match
     * @return true if the passed extension dependency is matched by the pattern
     */
    boolean matches(ExtensionDependency dependency);
}
