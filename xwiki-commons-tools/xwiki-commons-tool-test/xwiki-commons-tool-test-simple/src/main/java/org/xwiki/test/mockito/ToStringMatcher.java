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

import java.util.Objects;

import org.mockito.ArgumentMatcher;

/**
 * Matches arguments by comparing their {@link Object#toString()} values.
 * 
 * @version $Id$
 * @param <T> the type of the matched argument
 * @since 18.1.0RC1
 * @since 18.0.1
 * @since 17.10.4
 */
public class ToStringMatcher<T> implements ArgumentMatcher<T>
{
    private final T expectedValue;

    /**
     * @param expectedValue the expected value
     */
    public ToStringMatcher(T expectedValue)
    {
        this.expectedValue = expectedValue;
    }

    @Override
    public boolean matches(T actualValue)
    {
        String expectedString = Objects.toString(this.expectedValue, null);
        String actualString = Objects.toString(actualValue, null);
        return Objects.equals(expectedString, actualString);
    }
}
