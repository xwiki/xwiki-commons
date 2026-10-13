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
package org.xwiki.tool.checkstyle.test.encoding;

public class TestClassWithInvisibleCharacters
{
    private static final String VISIBLE = "café ½ öüäăâîș 😀";

    private static final String ESCAPED = "a\u00A0b\u200Bc\u202Ed";

    private static final String NBSP = "a b";

    private static final String ZERO_WIDTH_SPACE = "a​b";

    // Trojan Source: ‮ reversed text
    private static final String CONTROL = "ab";
}
