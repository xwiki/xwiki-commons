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

import java.io.StringReader;
import java.lang.reflect.Field;

import org.mockito.ArgumentMatcher;
import org.mockito.internal.matchers.Equality;

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
        Field field;
        try {
            field = StringReader.class.getDeclaredField("str");
        } catch (Exception e) {
            return false;
        }

        field.setAccessible(true);

        try {
            return Equality.areEqual(this.str, field.get(argument));
        } catch (Exception e) {
            return false;
        }
    }
}
