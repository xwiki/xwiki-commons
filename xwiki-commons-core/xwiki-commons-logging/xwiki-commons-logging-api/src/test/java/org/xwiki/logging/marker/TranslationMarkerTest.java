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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test {@link TranslationMarker}.
 *
 * @version $Id$
 */
public class TranslationMarkerTest
{
    private TranslationMarker marker;

    @BeforeEach
    public void setUp()
    {
        this.marker = new TranslationMarker("translation.key");
    }

    @Test
    public void getTranslationKey()
    {
        assertEquals("translation.key", this.marker.getTranslationKey());
    }

    @Test
    public void getName()
    {
        assertEquals(TranslationMarker.NAME, this.marker.getName());
    }

    @Test
    public void add()
    {
        this.marker.add(MarkerFactory.getMarker("marker"));
        assertTrue(this.marker.hasChildren());
    }

    @Test
    public void remove()
    {
        assertFalse(this.marker.remove(null));
    }

    @Test
    public void hasChildren()
    {
        assertFalse(this.marker.hasChildren());
    }

    @Test
    public void hasReferences()
    {
        assertFalse(this.marker.hasReferences());
    }

    @Test
    public void iterator()
    {
        assertFalse(this.marker.iterator().hasNext());
    }

    @Test
    public void containsString()
    {
        assertFalse(this.marker.contains("name"));
        assertTrue(this.marker.contains(this.marker.getName()));
    }

    @Test
    public void containsMarker()
    {
        assertFalse(this.marker.contains(MarkerFactory.getMarker("name")));
        assertTrue(this.marker.contains(this.marker));
    }

    @Test
    public void testHashCode()
    {
        Marker equalsTMarker = new TranslationMarker("translation.key");
        Marker otherTMarker = new TranslationMarker("translation.otherkey");
        Marker otherMarker = MarkerFactory.getMarker("name");

        assertEquals(equalsTMarker.hashCode(), this.marker.hashCode());
        assertFalse(this.marker.hashCode() == otherTMarker.hashCode());
        assertFalse(this.marker.hashCode() == otherMarker.hashCode());
    }

    @Test
    public void testEquals()
    {
        Marker equalsTMarker = new TranslationMarker("translation.key");
        Marker otherTMarker = new TranslationMarker("translation.otherkey");
        Marker otherMarker = MarkerFactory.getMarker("name");

        assertEquals(equalsTMarker, this.marker);
        assertFalse(this.marker.equals(otherTMarker));
        assertFalse(this.marker.equals(otherMarker));
    }
}
