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

import org.apache.commons.lang3.StringUtils;

/**
 * Implements the specification regarding CSS identifier serialization.
 * 
 * @version $Id$
 * @since 6.4.7, 7.1.4, 7.4M1
 * @see https://drafts.csswg.org/cssom/#serialize-an-identifier
 */
public class CSSIdentifierSerializer
{
    private static final int[][] NO_ESCAPE_RANGES = new int[][] {new int[] {0x0030, 0x0039},
        new int[] {0x0041, 0x005A}, new int[] {0x0061, 0x007A}};

    /**
     * Serialize a CSS identifier.
     * <p>
     * NOTE: This code was adapted from Mathias Bynens' CSS.escape polyfill, available under the MIT license.
     * 
     * @param identifier the identifier to serialize
     * @return the serialized identifier
     * @throws IllegalArgumentException if the input contains U+0000
     * @see https://drafts.csswg.org/cssom/#serialize-an-identifier
     * @see https://github.com/mathiasbynens/CSS.escape
     */
    public String serialize(String identifier)
    {
        if (StringUtils.isEmpty(identifier)) {
            return identifier;
        }

        int length = identifier.length();
        int index = -1;
        StringBuilder result = new StringBuilder();
        int firstCodeUnit = identifier.charAt(0);

        while (++index < length) {
            int codeUnit = identifier.charAt(index);
            // Note: there's no need to special-case astral symbols, surrogate pairs, or lone surrogates.

            // If the character is NULL (U+0000), throw an IllegalArgumentException exception and terminate these steps.
            if (codeUnit == 0x0000) {
                throw new IllegalArgumentException("Invalid character: the input contains U+0000.");

            } else if (shouldEscapeAsCodePoint(codeUnit, index, firstCodeUnit)) {
                // https://drafts.csswg.org/cssom/#escape-a-character-as-code-point
                result.append('\\').append(Integer.toHexString(codeUnit)).append(' ');

            } else if (index == 0 && length == 1 && codeUnit == 0x002D) {
                // If the character is the first character and is a '-' (U+002D), and there is no second character
                // https://drafts.csswg.org/cssom/#escape-a-character
                result.append('\\').append(identifier.charAt(index));

            } else if (shouldNotEscape(codeUnit)) {
                // the character itself
                result.append(identifier.charAt(index));

            } else {
                // Otherwise, the escaped character.
                // https://drafts.csswg.org/cssom/#escape-a-character
                result.append('\\').append(identifier.charAt(index));
            }
        }

        return result.toString();
    }

    /**
     * @see https://drafts.csswg.org/cssom/#escape-a-character-as-code-point
     */
    private boolean shouldEscapeAsCodePoint(int codeUnit, int index, int firstCodeUnit)
    {
        // If the character is in the range [\1-\1F] (U+0001 to U+001F) or is U+007F, [...]
        if (isInRange(codeUnit, 0x0001, 0x001F) || codeUnit == 0x007F) {
            return true;

        } else if (index == 0 && isInRange(codeUnit, 0x0030, 0x0039)) {
            // If the character is the first character and is in the range [0-9] (U+0030 to U+0039), [...]
            return true;

        } else {
            // If the character is the second character and is in the range [0-9] (U+0030 to U+0039) and the first
            // character is a '-' (U+002D), [...]
            return index == 1 && isInRange(codeUnit, 0x0030, 0x0039) && firstCodeUnit == 0x002D;
        }
    }

    private boolean shouldNotEscape(int codeUnit)
    {
        // If the character is not handled by one of the above rules and is greater than or equal to U+0080, is '-'
        // (U+002D) or '_' (U+005F), or is in one of the ranges [0-9] (U+0030 to U+0039), [A-Z] (U+0041 to U+005A),
        // or [a-z] (U+0061 to U+007A), [...]
        return codeUnit >= 0x0080 || codeUnit == 0x002D || codeUnit == 0x005F || isInRanges(codeUnit, NO_ESCAPE_RANGES);
    }

    private boolean isInRange(int codeUnit, int start, int end)
    {
        return codeUnit >= start && codeUnit <= end;
    }

    private boolean isInRanges(int codeUnit, int[][] ranges)
    {
        for (int[] range : ranges) {
            if (isInRange(codeUnit, range[0], range[1])) {
                return true;
            }
        }
        return false;
    }
}
