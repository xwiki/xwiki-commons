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
package org.xwiki.velocity.tools;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link CSSIdentifierSerializer}.
 * 
 * @version $Id$
 */
public class CSSIdentifierSerializerTest
{
    private CSSIdentifierSerializer cssIdentifierSerializer = new CSSIdentifierSerializer();

    /**
     * NOTE: This test was adapted from Mathias Bynens' tests for CSS.escape polyfill, available under the MIT license.
     * 
     * @see <a href="https://drafts.csswg.org/cssom/#serialize-an-identifier">
     *       https://drafts.csswg.org/cssom/#serialize-an-identifier</a>
     * @see <a href="https://github.com/mathiasbynens/CSS.escape/blob/master/tests/tests.js">
     *       https://github.com/mathiasbynens/CSS.escape/blob/master/tests/tests.js</a>
     */
    @Test
    @DisplayName("Various CSS serialization tests")
    public void serialize()
    {
        assertCssThrowsException("\u0000");
        assertCssThrowsException("a\u0000");
        assertCssThrowsException("\u0000b");
        assertCssThrowsException("a\u0000b");

        assertNull(this.cssIdentifierSerializer.serialize(null));
        assertCss("", "");

        assertCss("\u0001\u0002\u001E\u001F", "\\1 \\2 \\1e \\1f ");

        assertCss("0a", "\\30 a");
        assertCss("1a", "\\31 a");
        assertCss("2a", "\\32 a");
        assertCss("3a", "\\33 a");
        assertCss("4a", "\\34 a");
        assertCss("5a", "\\35 a");
        assertCss("6a", "\\36 a");
        assertCss("7a", "\\37 a");
        assertCss("8a", "\\38 a");
        assertCss("9a", "\\39 a");

        assertCss("a0b", "a0b");
        assertCss("a1b", "a1b");
        assertCss("a2b", "a2b");
        assertCss("a3b", "a3b");
        assertCss("a4b", "a4b");
        assertCss("a5b", "a5b");
        assertCss("a6b", "a6b");
        assertCss("a7b", "a7b");
        assertCss("a8b", "a8b");
        assertCss("a9b", "a9b");

        assertCss("-0a", "-\\30 a");
        assertCss("-1a", "-\\31 a");
        assertCss("-2a", "-\\32 a");
        assertCss("-3a", "-\\33 a");
        assertCss("-4a", "-\\34 a");
        assertCss("-5a", "-\\35 a");
        assertCss("-6a", "-\\36 a");
        assertCss("-7a", "-\\37 a");
        assertCss("-8a", "-\\38 a");
        assertCss("-9a", "-\\39 a");

        assertCss("-", "\\-");
        assertCss("-a", "-a");
        assertCss("--", "--");
        assertCss("--a", "--a");

        assertCss("\u0080\u002D\u005F\u00A9", "\u0080\u002D\u005F\u00A9");
        assertCss("\u007F\u0080\u0081\u0082\u0083\u0084\u0085\u0086\u0087\u0088"
            + "\u0089\u008A\u008B\u008C\u008D\u008E\u008F\u0090\u0091\u0092"
            + "\u0093\u0094\u0095\u0096\u0097\u0098\u0099\u009A\u009B\u009C\u009D\u009E\u009F",
            "\\7f \u0080\u0081\u0082\u0083\u0084\u0085\u0086\u0087\u0088\u0089"
                + "\u008A\u008B\u008C\u008D\u008E\u008F\u0090\u0091\u0092\u0093"
                + "\u0094\u0095\u0096\u0097\u0098\u0099\u009A\u009B\u009C\u009D\u009E\u009F");
        assertCss("\u00A0\u00A1\u00A2", "\u00A0\u00A1\u00A2");
        assertCss("a0123456789b", "a0123456789b");
        assertCss("abcdefghijklmnopqrstuvwxyz", "abcdefghijklmnopqrstuvwxyz");
        assertCss("ABCDEFGHIJKLMNOPQRSTUVWXYZ", "ABCDEFGHIJKLMNOPQRSTUVWXYZ");

        assertCss("\u0020\u0021\u0078\u0079", "\\ \\!xy");

        // astral symbol (U+1D306 TETRAGRAM FOR CENTRE)
        assertCss("\uD834\uDF06", "\uD834\uDF06");
        // lone surrogates
        assertCss("\uDF06", "\uDF06");
        assertCss("\uD834", "\uD834");

        assertCss("a#b.c d[e=f]g{h:i;j}k", "a\\#b\\.c\\ d\\[e\\=f\\]g\\{h\\:i\\;j\\}k");
    }

    private void assertCss(String expected, String actual)
    {
        assertEquals(this.cssIdentifierSerializer.serialize(expected), actual);
    }

    private void assertCssThrowsException(String input)
    {
        Throwable exception = assertThrows(IllegalArgumentException.class, () -> {
            this.cssIdentifierSerializer.serialize(input);
        });
        assertEquals("Invalid character: the input contains U+0000.", exception.getMessage());
    }
}
