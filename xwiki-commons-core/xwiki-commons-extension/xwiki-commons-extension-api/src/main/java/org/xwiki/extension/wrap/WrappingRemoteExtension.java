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
package org.xwiki.extension.wrap;

import org.xwiki.extension.RemoteExtension;

/**
 * Wrap a remote extension.
 *
 * @param <T> the extension type
 * @version $Id$
 * @since 8.3RC1
 */
public class WrappingRemoteExtension<T extends RemoteExtension> extends WrappingExtension<T> implements RemoteExtension
{
    /**
     * @param remoteExtension the wrapped local extension
     */
    public WrappingRemoteExtension(T remoteExtension)
    {
        super(remoteExtension);
    }

    // RremoteExtension

    @Override
    public boolean isRecommended()
    {
        if (this.overwrites.containsKey(RemoteExtension.FIELD_RECOMMENDED)) {
            return (boolean) this.overwrites.get(RemoteExtension.FIELD_RECOMMENDED);
        }

        return getWrapped().isRecommended();
    }
}
