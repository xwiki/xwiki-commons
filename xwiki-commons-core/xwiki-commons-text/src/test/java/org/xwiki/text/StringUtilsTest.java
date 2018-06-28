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
package org.xwiki.text;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Validate {@link StringUtils}.
 *
 * @version $Id$
 */
class StringUtilsTest
{
    @Test
    @DisplayName("Doubling of characters")
    void doubleChar()
    {
        assertNull(StringUtils.doubleChar(null, 'a'));
        assertEquals("", StringUtils.doubleChar("", 'a'));
        assertEquals("b", StringUtils.doubleChar("b", 'a'));
        assertEquals("aa", StringUtils.doubleChar("a", 'a'));
        assertEquals("aaaa", StringUtils.doubleChar("aa", 'a'));
        assertEquals("baabaa", StringUtils.doubleChar("baba", 'a'));
        assertEquals("aabaab", StringUtils.doubleChar("abab", 'a'));
    }

    @Test
    @DisplayName("Alphanumeric conversion")
    void convertToAlphaNumeric()
    {
        assertNull(StringUtils.convertToAlphaNumeric(null));
        assertEquals("", StringUtils.convertToAlphaNumeric(""));
        assertEquals("abc123", StringUtils.convertToAlphaNumeric("abc123"));
        assertEquals("MainWebHome", StringUtils.convertToAlphaNumeric("Main.WebHome"));
        assertEquals(
            "TheQuickBrownFoxjumpsoverthelazydog",
            StringUtils.convertToAlphaNumeric("The Quick Brown Fox jumps over the lazy dog")
        );
        assertEquals(
            "DesNoelouunzephyrhaimevetdeglaconswurmiensjedinedexquisrotisdebufaukiralaydagemurctera",
            StringUtils.convertToAlphaNumeric("Dès Noël où un zéphyr haï me vêt de glaçons würmiens je dîne d’exquis " +
                "rôtis de bœuf au kir à l’aÿ d’âge mûr & cætera !")
        );
    }
}
