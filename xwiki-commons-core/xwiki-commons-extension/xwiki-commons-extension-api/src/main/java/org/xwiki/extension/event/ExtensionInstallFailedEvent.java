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
import org.xwiki.observation.event.EndEvent;

/**
 * An event triggered when a extension install has failed. Its job is mostly to provide a {@link EndEvent} to close the
 * previous {@link ExtensionInstallingEvent}.
 * <p>
 * The event also send the following parameters:
 * </p>
 * <ul>
 * <li>source: the related {@link org.xwiki.extension.LocalExtension} instance</li>
 * <li>data: null</li>
 * </ul>
 *
 * @see ExtensionInstalledEvent
 * @see ExtensionInstallingEvent
 * @version $Id$
 * @since 9.2RC1
 */
public class ExtensionInstallFailedEvent extends AbstractExtensionEvent implements EndEvent
{
    /**
     * Matches all extensions.
     */
    public ExtensionInstallFailedEvent()
    {
    }

    /**
     * Matches only the specified extension id or/and version.
     *
     * @param extensionId the extension identifier
     * @param namespace the namespace on which the event happened
     */
    public ExtensionInstallFailedEvent(ExtensionId extensionId, String namespace)
    {
        super(extensionId, namespace);
    }
}
