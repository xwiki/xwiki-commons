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
import java.util.Objects;

import org.apache.commons.io.IOUtils;
import org.mockito.ArgumentMatcher;

/**
 * Match a StringReader parameter with a String.
 * 
 * @version $Id$
 * @since 9.0RC1
 */
public class StringReaderMatcher implements ArgumentMatcher<StringReader>
{
    private final String str;

    /**
     * @param str the expected {@link String}
     */
    public StringReaderMatcher(String str)
    {
        this.str = str;
    }

    @Override
    public boolean matches(StringReader argument)
    {
        if (argument == null) {
            return this.str == null;
        }

        try {
            // Remember the current position so that the code under test can still consume the reader, and so that
            // this matcher can be called more than once for the same reader (Mockito matches on each invocation).
            // The read ahead limit is unused by StringReader.
            argument.mark(0);
            String content = IOUtils.toString(argument);
            argument.reset();

            return Objects.equals(this.str, content);
        } catch (IOException e) {
            // A StringReader only fails when it's closed, which means the test itself is wrong.
            throw new UncheckedIOException("Failed to read the matched reader", e);
        }
    }
}
