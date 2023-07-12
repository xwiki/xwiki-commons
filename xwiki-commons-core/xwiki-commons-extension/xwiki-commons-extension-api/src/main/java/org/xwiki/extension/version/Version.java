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
package org.xwiki.extension.version;

import java.io.Serializable;

import org.xwiki.stability.Unstable;

/**
 * An extension version.
 *
 * @version $Id$
 * @since 4.0M1
 */
public interface Version extends Comparable<Version>, Serializable
{
    /**
     * The type of version.
     *
     * @version $Id$
     */
    enum Type
    {
        /**
         * A not released build.
         */
        SNAPSHOT,

        /**
         * A released but not stable version.
         */
        BETA,

        /**
         * A stable version.
         */
        STABLE
    }

    /**
     * @return the string representation of this version constraint
     */
    String getValue();

    /**
     * @return the type of version
     */
    Type getType();

    /**
     * Access the version as it was expressed in the source. For example the final version (when deployed or stored in
     * the XWiki local repository) of a Maven 1.0-SNAPSHOT extension will be of the form "1.0-20230712.163603-0" but in
     * some cases you might still need to access the source version.
     * 
     * @return the version as it was expressed in the source
     * @since 15.6RC1
     */
    @Unstable
    default Version getSourceVersion()
    {
        return this;
    }
}
