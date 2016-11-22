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

import org.xwiki.extension.rating.ExtensionRating;
import org.xwiki.extension.rating.RatingExtension;

/**
 * Wrap a rating extension.
 *
 * @param <T> the extension type
 * @version $Id$
 * @since 6.4M3
 */
public class WrappingRatingExtension<T extends RatingExtension> extends WrappingRemoteExtension<T>
    implements RatingExtension
{
    /**
     * @param ratingExtension the wrapped rating extension
     */
    public WrappingRatingExtension(T ratingExtension)
    {
        super(ratingExtension);
    }

    // RatingExtension

    @Override
    public ExtensionRating getRating()
    {
        if (this.overwrites.containsKey(RatingExtension.FIELD_AVERAGE_VOTE)) {
            return (ExtensionRating) this.overwrites.get(RatingExtension.FIELD_AVERAGE_VOTE);
        }

        return getWrapped().getRating();
    }
}
