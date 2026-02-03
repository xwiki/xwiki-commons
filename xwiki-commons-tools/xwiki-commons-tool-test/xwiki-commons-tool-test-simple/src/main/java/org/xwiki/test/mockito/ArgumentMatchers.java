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

import static org.mockito.ArgumentMatchers.argThat;

/**
 * Custom argument matchers for Mockito.
 * 
 * @version $Id$
 * @since 18.1.0RC1
 * @since 18.0.1
 * @since 17.10.4
 */
public final class ArgumentMatchers
{
    /**
     * This class has only static methods.
     */
    private ArgumentMatchers()
    {
    }

    /**
     * Matches arguments by comparing their {@link Object#toString()} values.
     * 
     * @param <T> the type of the matched argument
     * @param value the expected value
     * @return the matched argument
     */
    public static <T> T toStrEq(T value)
    {
        return argThat(new ToStringMatcher<T>(value));
    }
}
