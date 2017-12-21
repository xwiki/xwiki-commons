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

import org.xwiki.extension.rating.RatingExtension;
import org.xwiki.extension.repository.ExtensionRepository;

/**
 * Base class for {@link RatingExtension} implementations.
 *
 * @version $Id$
 * @since 8.3RC1
 */
public abstract class AbstractRemoteExtension extends AbstractExtension implements RemoteExtension
{
    /**
     * @see #isRecommended()
     */
    protected boolean recommended;

    /**
     * @param repository the repository where this extension comes from
     * @param id the extension identifier
     * @param type the extension type
     */
    public AbstractRemoteExtension(ExtensionRepository repository, ExtensionId id, String type)
    {
        super(repository, id, type);
    }

    /**
     * Create new extension descriptor by copying provided one.
     *
     * @param repository the repository where this extension comes from
     * @param extension the extension to copy
     */
    public AbstractRemoteExtension(ExtensionRepository repository, Extension extension)
    {
        super(repository, extension);
    }

    @Override
    public boolean isRecommended()
    {
        return this.recommended;
    }

    /**
     * @param recommended true if the extension is recommended
     * @see #isRecommended()
     */
    public void setRecommended(boolean recommended)
    {
        this.recommended = recommended;
    }

    @Override
    public <T> T get(String fieldName)
    {
        switch (fieldName.toLowerCase()) {
            case FIELD_RECOMMENDED:
                return (T) (Boolean) isRecommended();
            default:
                return super.get(fieldName);
        }
    }
}
