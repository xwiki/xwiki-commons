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
package org.xwiki.logging.marker;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

/**
 * Test {@link TranslationMarker}.
 *
 * @version $Id$
 */
public class TranslationMarkerTest
{
    private TranslationMarker marker;

    @Before
    public void setUp()
    {
        this.marker = new TranslationMarker("translation.key");
    }

    @Test
    public void getTranslationKey()
    {
        Assert.assertEquals("translation.key", this.marker.getTranslationKey());
    }

    @Test
    public void getName()
    {
        Assert.assertEquals(TranslationMarker.NAME, this.marker.getName());
    }

    @Test
    public void add()
    {
        this.marker.add(MarkerFactory.getMarker("marker"));
        Assert.assertTrue(this.marker.hasChildren());
    }

    @Test
    public void remove()
    {
        Assert.assertFalse(this.marker.remove(null));
    }

    @Test
    public void hasChildren()
    {
        Assert.assertFalse(this.marker.hasChildren());
    }

    @Test
    public void hasReferences()
    {
        Assert.assertFalse(this.marker.hasReferences());
    }

    @Test
    public void iterator()
    {
        Assert.assertFalse(this.marker.iterator().hasNext());
    }

    @Test
    public void containsString()
    {
        Assert.assertFalse(this.marker.contains("name"));
        Assert.assertTrue(this.marker.contains(this.marker.getName()));
    }

    @Test
    public void containsMarker()
    {
        Assert.assertFalse(this.marker.contains(MarkerFactory.getMarker("name")));
        Assert.assertTrue(this.marker.contains(this.marker));
    }

    @Test
    public void testHashCode()
    {
        Marker equalsTMarker = new TranslationMarker("translation.key");
        Marker otherTMarker = new TranslationMarker("translation.otherkey");
        Marker otherMarker = MarkerFactory.getMarker("name");

        Assert.assertEquals(equalsTMarker.hashCode(), this.marker.hashCode());
        Assert.assertFalse(this.marker.hashCode() == otherTMarker.hashCode());
        Assert.assertFalse(this.marker.hashCode() == otherMarker.hashCode());
    }

    @Test
    public void testEquals()
    {
        Marker equalsTMarker = new TranslationMarker("translation.key");
        Marker otherTMarker = new TranslationMarker("translation.otherkey");
        Marker otherMarker = MarkerFactory.getMarker("name");

        Assert.assertEquals(equalsTMarker, this.marker);
        Assert.assertFalse(this.marker.equals(otherTMarker));
        Assert.assertFalse(this.marker.equals(otherMarker));
    }
}
