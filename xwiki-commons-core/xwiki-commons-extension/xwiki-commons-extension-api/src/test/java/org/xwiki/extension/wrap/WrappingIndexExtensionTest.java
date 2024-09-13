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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.xwiki.extension.AbstractExtensionTest.TestExtension;
import org.xwiki.extension.AbstractRatingExtension;
import org.xwiki.extension.Extension;
import org.xwiki.extension.ExtensionId;
import org.xwiki.extension.ExtensionSupportPlans;
import org.xwiki.extension.RemoteExtension;
import org.xwiki.extension.index.IndexedExtension;
import org.xwiki.extension.rating.ExtensionRating;
import org.xwiki.extension.rating.RatingExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * Validate {@link WrappingIndexedExtension} and extended wrappers.
 * 
 * @version $Id$
 */
class WrappingIndexExtensionTest
{
    public static class TestIndexedExtension extends AbstractRatingExtension implements IndexedExtension
    {
        private final Map<String, Boolean> compatible = new HashMap<>();

        public TestIndexedExtension()
        {
            super(null, new ExtensionId("id", "version"), "type");
        }

        @Override
        public Boolean isCompatible(String namespace)
        {
            return this.compatible.get(namespace);
        }

        public void setCompatible(String namespace, Boolean compatible)
        {
            this.compatible.put(namespace, compatible);
        }
    }

    private final TestIndexedExtension indexedExtension = new TestIndexedExtension();

    private final TestExtension notIndexedExtension = new TestExtension();

    private final WrappingIndexedExtension<IndexedExtension> indexedWrapper =
        new WrappingIndexedExtension<>(this.indexedExtension);

    private final WrappingIndexedExtension<Extension> notIndexedWrapper =
        new WrappingIndexedExtension<>(this.notIndexedExtension);

    // RemoteExtension

    @Test
    void isRecommended()
    {
        assertFalse(this.indexedWrapper.isRecommended());

        this.indexedExtension.setRecommended(true);

        assertTrue(this.indexedWrapper.isRecommended());

        this.indexedExtension.setRecommended(false);

        assertFalse(this.indexedWrapper.isRecommended());

        this.indexedWrapper.setOverwrite(RemoteExtension.FIELD_RECOMMENDED, true);

        assertTrue(this.indexedWrapper.isRecommended());
    }

    @Test
    void isRecommendedNotRemoteExtension()
    {
        assertFalse(this.notIndexedWrapper.isRecommended());
    }

    @Test
    void getSupportPlans()
    {
        assertEquals(List.of(), this.indexedWrapper.getSupportPlans().getSupportPlans());

        ExtensionSupportPlans supportPlans1 = mock();

        this.indexedExtension.setSupportPlans(supportPlans1);

        assertSame(supportPlans1, this.indexedWrapper.getSupportPlans());

        ExtensionSupportPlans supportPlans2 = mock();

        this.indexedWrapper.setOverwrite(RemoteExtension.FIELD_SUPPORT_PLANS, supportPlans2);

        assertSame(supportPlans2, this.indexedWrapper.getSupportPlans());
    }

    @Test
    void getSupportPlansNotRemoteExtension()
    {
        assertEquals(List.of(), this.notIndexedWrapper.getSupportPlans().getSupportPlans());
    }

    // RatingExtension

    @Test
    void getRating()
    {
        assertNull(this.indexedWrapper.getRating());

        ExtensionRating rating1 = mock();

        this.indexedExtension.setRating(rating1);

        assertSame(rating1, this.indexedWrapper.getRating());

        ExtensionRating rating2 = mock();

        this.indexedWrapper.setOverwrite(RatingExtension.FIELD_RATING, rating2);

        assertSame(rating2, this.indexedWrapper.getRating());
    }

    @Test
    void getRatingNotRatingExtension()
    {
        assertNull(this.notIndexedWrapper.getRating());
    }

    // IndexedExtension

    @Test
    void isCompatible()
    {
        assertNull(this.indexedWrapper.isCompatible("namespace"));

        this.indexedExtension.setCompatible("namespace", Boolean.TRUE);

        assertEquals(Boolean.TRUE, this.indexedWrapper.isCompatible("namespace"));
    }

    @Test
    void isCompatibleNotIndexedExtension()
    {
        assertNull(this.notIndexedWrapper.isCompatible("namespace"));
    }
}
