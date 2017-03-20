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
package org.xwiki.extension.event;

import org.xwiki.extension.ExtensionId;
import org.xwiki.observation.event.BeginEvent;

/**
 * An event triggered when a extension is going to be uninstalled.
 * <p>
 * The event also send the following parameters:
 * </p>
 * <ul>
 * <li>source: the related {@link org.xwiki.extension.InstalledExtension} instance</li>
 * <li>data: null</li>
 * </ul>
 *
 * @see ExtensionUninstalledEvent
 * @see ExtensionUninstallFailedEvent
 * @version $Id$
 * @since 4.0M1
 */
public class ExtensionUninstallingEvent extends AbstractExtensionEvent implements BeginEvent
{
    /**
     * Matches all extensions.
     */
    public ExtensionUninstallingEvent()
    {
    }

    /**
     * Matches only the specified extension id or/and version.
     *
     * @param extensionId the extension identifier
     * @param namespace the namespace from which the extensions has been uninstalled
     */
    public ExtensionUninstallingEvent(ExtensionId extensionId, String namespace)
    {
        super(extensionId, namespace);
    }
}
