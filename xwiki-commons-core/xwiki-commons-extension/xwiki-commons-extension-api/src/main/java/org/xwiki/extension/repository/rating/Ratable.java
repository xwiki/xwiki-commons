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
package org.xwiki.extension.repository.rating;

import org.xwiki.extension.ExtensionId;
import org.xwiki.extension.ResolveException;
import org.xwiki.extension.rating.ExtensionRating;
import org.xwiki.extension.version.Version;
import org.xwiki.stability.Unstable;

/**
 * A repository can implement it to provide rating capabilities.
 *
 * @version $Id$
 * @since 6.4M3
 */
@Unstable
public interface Ratable
{
    /**
     * @param extensionId the extension id
     * @return the total votes/average vote pair representing an extension's rating
     * @throws ResolveException error when trying to resolve extension for provided extensionId
     */
    ExtensionRating getRating(ExtensionId extensionId) throws ResolveException;

    /**
     * @param extensionId the extension id
     * @param extensionVersion the extension version
     * @return the total votes/average vote pair representing an extension's rating
     * @throws ResolveException error when trying to resolve extension for provided extensionId
     */
    ExtensionRating getRating(String extensionId, Version extensionVersion) throws ResolveException;

    /**
     * @param extensionId the extension id
     * @param extensionVersion the extension version
     * @return the total votes/average vote pair representing an extension's rating
     * @throws ResolveException error when trying to resolve extension for provided extensionId
     */
    ExtensionRating getRating(String extensionId, String extensionVersion) throws ResolveException;
}
