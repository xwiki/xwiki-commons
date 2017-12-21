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

import org.xwiki.extension.repository.ExtensionRepositoryManager;

/**
 * An extension that come from a remote extensions repository.
 * <p>
 * A remote extension repository is any repository which is not one of the repository listed in
 * {@link ExtensionRepositoryManager}. It means it could be a Maven repository targeting some
 * {@code file:///my/repository/path} for example.
 * 
 * @version $Id$
 * @since 8.3RC1
 */
public interface RemoteExtension extends Extension
{
    /**
     * @see #isRecommended()
     */
    String FIELD_RECOMMENDED = "recommended";

    /**
     * Indicate if the extension is recommended by the repository where it come from.
     * <p>
     * What "recommended" exactly means depend on the repository giving this information.
     * <p>
     * For example on http://extensions.xwiki.org the meaning is the extension is known to be of good quality and still
     * officially supported by its author.
     * 
     * @return true if the extension is recommended
     */
    default boolean isRecommended()
    {
        return false;
    }
}
