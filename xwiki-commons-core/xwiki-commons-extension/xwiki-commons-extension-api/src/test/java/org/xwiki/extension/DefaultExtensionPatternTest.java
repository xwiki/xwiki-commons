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
package org.xwiki.extension;

import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Validate {@link DefaultExtensionPattern}.
 * 
 * @version $Id$
 */
public class DefaultExtensionPatternTest
{
    @Test
    public void matchesString()
    {
        DefaultExtensionPattern pattern = new DefaultExtensionPattern("abcd");

        assertTrue(pattern.matches("abcd"));
        assertFalse(pattern.matches("abc"));
    }

    @Test
    public void matchesPattern()
    {
        DefaultExtensionPattern pattern = new DefaultExtensionPattern(Pattern.compile("ab.*d"));

        assertTrue(pattern.matches("abcd"));
        assertFalse(pattern.matches("abc"));
    }

    @Test
    public void equal()
    {
        assertEquals(new DefaultExtensionPattern(Pattern.compile("ab.*d")),
            new DefaultExtensionPattern(Pattern.compile("ab.*d")));

        assertNotEquals(new DefaultExtensionPattern(Pattern.compile("ab.*d")),
            new DefaultExtensionPattern(Pattern.compile("ab.*")));
    }
}
