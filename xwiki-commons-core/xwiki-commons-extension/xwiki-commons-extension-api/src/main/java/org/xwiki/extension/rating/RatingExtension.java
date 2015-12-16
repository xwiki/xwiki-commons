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
package org.xwiki.extension.rating;

import org.xwiki.extension.Extension;

/**
 * Combine {@link Extension} and {@link Rating}.
 * 
 * @version $Id$
 * @since 6.4M3
 */
public interface RatingExtension extends Extension, Rating
{
    /**
     * @see #getRating()
     * @see ExtensionRating#getTotalVotes()
     * @since 7.2M1
     */
    String FIELD_TOTAL_VOTES = "nbvotes";

    /**
     * @see #getRating()
     * @see ExtensionRating#getAverageVote()
     * @since 7.1.2 - 7.2M1
     */
    String FIELD_AVERAGE_VOTE = "averagevote";
}
