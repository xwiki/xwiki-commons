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
package org.xwiki.test.mockito;

import java.io.IOException;
import java.io.StringReader;
import java.io.UncheckedIOException;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link StringReaderMatcher}.
 *
 * @version $Id$
 */
class StringReaderMatcherTest
{
    @Test
    void matchesSameContent()
    {
        assertTrue(new StringReaderMatcher("content").matches(new StringReader("content")));
    }

    @Test
    void matchesDifferentContent()
    {
        assertFalse(new StringReaderMatcher("content").matches(new StringReader("other")));
        assertFalse(new StringReaderMatcher("content").matches(new StringReader("")));
        assertFalse(new StringReaderMatcher("").matches(new StringReader("content")));
    }

    @Test
    void matchesEmptyContent()
    {
        assertTrue(new StringReaderMatcher("").matches(new StringReader("")));
    }

    @Test
    void matchesNullArgument()
    {
        assertFalse(new StringReaderMatcher("content").matches(null));
        assertTrue(new StringReaderMatcher(null).matches(null));
        assertFalse(new StringReaderMatcher(null).matches(new StringReader("content")));
    }

    @Test
    void readerIsStillReadableAfterMatch() throws IOException
    {
        StringReader reader = new StringReader("content");

        assertTrue(new StringReaderMatcher("content").matches(reader));

        assertEquals("content", IOUtils.toString(reader));
    }

    @Test
    void readerIsStillReadableAfterFailedMatch() throws IOException
    {
        StringReader reader = new StringReader("content");

        assertFalse(new StringReaderMatcher("other").matches(reader));

        assertEquals("content", IOUtils.toString(reader));
    }

    @Test
    void matchesSameReaderSeveralTimes()
    {
        StringReader reader = new StringReader("content");
        StringReaderMatcher matcher = new StringReaderMatcher("content");

        assertTrue(matcher.matches(reader));
        assertTrue(matcher.matches(reader));
    }

    @Test
    void matchesPartiallyConsumedReader() throws IOException
    {
        StringReader reader = new StringReader("content");
        assertEquals('c', reader.read());

        assertTrue(new StringReaderMatcher("ontent").matches(reader));
        assertEquals("ontent", IOUtils.toString(reader));
    }

    @Test
    void matchesClosedReader()
    {
        StringReader reader = new StringReader("content");
        reader.close();

        StringReaderMatcher matcher = new StringReaderMatcher("content");
        assertThrows(UncheckedIOException.class, () -> matcher.matches(reader));
    }
}
