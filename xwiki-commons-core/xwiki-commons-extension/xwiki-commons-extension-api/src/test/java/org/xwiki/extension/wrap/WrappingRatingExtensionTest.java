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

import java.util.List;

import org.junit.jupiter.api.Test;
import org.xwiki.extension.AbstractRatingExtension;
import org.xwiki.extension.ExtensionId;
import org.xwiki.extension.ExtensionSupportPlans;
import org.xwiki.extension.RemoteExtension;
import org.xwiki.extension.rating.ExtensionRating;
import org.xwiki.extension.rating.RatingExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * Validate {@link WrappingRatingExtension} and extended wrappers.
 * 
 * @version $Id$
 */
class WrappingRatingExtensionTest
{
    public static class TestRatingExtension extends AbstractRatingExtension
    {
        public TestRatingExtension()
        {
            super(null, new ExtensionId("id", "version"), "type");
        }
    }

    private final TestRatingExtension ratingExtension = new TestRatingExtension();

    private final WrappingRatingExtension<RemoteExtension> wrapper =
        new WrappingRatingExtension<>(this.ratingExtension);

    @Test
    void isRecommended()
    {
        assertFalse(this.wrapper.isRecommended());

        this.ratingExtension.setRecommended(true);

        assertTrue(this.wrapper.isRecommended());

        this.ratingExtension.setRecommended(false);

        assertFalse(this.wrapper.isRecommended());

        this.wrapper.setOverwrite(RemoteExtension.FIELD_RECOMMENDED, true);

        assertTrue(this.wrapper.isRecommended());
    }

    @Test
    void getSupportPlans()
    {
        assertEquals(List.of(), this.wrapper.getSupportPlans().getSupportPlans());

        ExtensionSupportPlans supportPlans1 = mock();

        this.ratingExtension.setSupportPlans(supportPlans1);

        assertSame(supportPlans1, this.wrapper.getSupportPlans());

        ExtensionSupportPlans supportPlans2 = mock();

        this.wrapper.setOverwrite(RemoteExtension.FIELD_SUPPORT_PLANS, supportPlans2);

        assertSame(supportPlans2, this.wrapper.getSupportPlans());
    }

    @Test
    void getRating()
    {
        assertNull(this.wrapper.getRating());

        ExtensionRating rating1 = mock();

        this.ratingExtension.setRating(rating1);

        assertSame(rating1, this.wrapper.getRating());

        ExtensionRating rating2 = mock();

        this.wrapper.setOverwrite(RatingExtension.FIELD_RATING, rating2);

        assertSame(rating2, this.wrapper.getRating());
    }
}
