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

import org.xwiki.extension.Extension;
import org.xwiki.extension.ExtensionSupportPlans;
import org.xwiki.extension.RemoteExtension;

/**
 * Wrap a remote extension.
 *
 * @param <T> the extension type
 * @version $Id$
 * @since 8.3RC1
 */
public class WrappingRemoteExtension<T extends Extension> extends WrappingExtension<T> implements RemoteExtension
{
    /**
     * @param extension the wrapped extension
     */
    public WrappingRemoteExtension(T extension)
    {
        super(extension);
    }

    /**
     * A default constructor allowing to set the wrapped object later.
     * 
     * @since 16.7.0RC1
     */
    protected WrappingRemoteExtension()
    {

    }

    // RemoteExtension

    @Override
    @Deprecated
    public boolean isRecommended()
    {
        if (this.overwrites.containsKey(RemoteExtension.FIELD_RECOMMENDED)) {
            return (boolean) this.overwrites.get(RemoteExtension.FIELD_RECOMMENDED);
        }

        if (getWrapped() instanceof RemoteExtension remoteExtension) {
            return remoteExtension.isRecommended();
        }

        // Fallback on the existence of at least one support plan
        return !getSupportPlans().getSupporters().isEmpty();
    }

    @Override
    public ExtensionSupportPlans getSupportPlans()
    {
        if (this.overwrites.containsKey(RemoteExtension.FIELD_SUPPORT_PLANS)) {
            return (ExtensionSupportPlans) this.overwrites.get(RemoteExtension.FIELD_SUPPORT_PLANS);
        }

        if (getWrapped() instanceof RemoteExtension remoteExtension) {
            return remoteExtension.getSupportPlans();
        }

        return ExtensionSupportPlans.EMPTY;
    }
}
