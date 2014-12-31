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

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.xwiki.extension.repository.rating.RatableExtensionRepository;

/**
 * Default implementation of ExtensionRating.
 *
 * @version $Id$
 * @since 6.2M2
 */
public class DefaultExtensionRating implements ExtensionRating
{
    /**
     * @see #getTotalVotes()
     */
    private int totalVotes;

    /**
     * @see #getAverageVote()
     */
    private float averageVote;

    /**
     * @see #getRepository()
     */
    private RatableExtensionRepository repository;

    /**
     * @param totalVotes the total number of votes
     * @param averageVote the average of all votes
     * @param repository the repository from which the rating comes from
     */
    public DefaultExtensionRating(int totalVotes, float averageVote, RatableExtensionRepository repository)
    {
        this.totalVotes = totalVotes;
        this.averageVote = averageVote;
        this.repository = repository;
    }

    @Override
    public int getTotalVotes()
    {
        return this.totalVotes;
    }

    @Override
    public float getAverageVote()
    {
        return this.averageVote;
    }

    @Override
    public RatableExtensionRepository getRepository()
    {
        return this.repository;
    }

    // Object

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this) {
            return true;
        }

        if (obj instanceof ExtensionRating) {
            ExtensionRating rating = (ExtensionRating) obj;
            return this.totalVotes == rating.getTotalVotes() && this.averageVote == rating.getAverageVote()
                && this.repository == rating.getRepository();
        } else {
            return false;
        }
    }

    @Override
    public int hashCode()
    {
        HashCodeBuilder builder = new HashCodeBuilder();

        builder.append(this.totalVotes);
        builder.append(this.averageVote);
        builder.append(this.repository);

        return builder.toHashCode();
    }
}
