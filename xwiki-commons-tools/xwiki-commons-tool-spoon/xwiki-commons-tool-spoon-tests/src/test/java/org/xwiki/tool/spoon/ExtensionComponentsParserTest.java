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
package org.xwiki.tool.spoon;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.xwiki.tool.spoon.ExtensionComponentsParser.Component;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit test for {@link ExtensionComponentsParser}. The expected values follow the parsing done by the Extension
 * Manager ({@code AbstractModelConverter}) on the {@code xwiki.extension.components} Maven property.
 *
 * @version $Id$
 */
class ExtensionComponentsParserTest
{
    @Test
    void parseNullOrBlank()
    {
        assertEquals(List.of(), ExtensionComponentsParser.parse(null));
        assertEquals(List.of(), ExtensionComponentsParser.parse("  \n\t "));
    }

    @Test
    void parse()
    {
        assertEquals(List.of(
            new Component("org.xwiki.rendering.macro.Macro", "line"),
            new Component("org.xwiki.rendering.macro.Macro", "pipe"),
            new Component("org.xwiki.rendering.macro.Macro", "tab"),
            new Component("org.xwiki.rendering.macro.Macro", "default"),
            new Component("org.xwiki.rendering.macro.Macro", "default"),
            new Component("java.util.List<java.lang.String>", "generic"),
            new Component("my/role", "hint/with/slashes"),
            new Component("org.xwiki.rendering.macro.Macro", "quoted hint")),
            ExtensionComponentsParser.parse("""
                  org.xwiki.rendering.macro.Macro/line
                org.xwiki.rendering.macro.Macro/pipe|org.xwiki.rendering.macro.Macro/tab\t\
                org.xwiki.rendering.macro.Macro
                org.xwiki.rendering.macro.Macro/
                java.util.List< java.lang.String >/generic
                my\\/role/hint/with/slashes
                "org.xwiki.rendering.macro.Macro/quoted hint"
                """));
    }

    @Test
    void toMacroDeclaration()
    {
        assertEquals("org.xwiki.rendering.macro.Macro/foo", ExtensionComponentsParser.toMacroDeclaration("foo"));
        assertEquals("org.xwiki.rendering.macro.Macro/default", ExtensionComponentsParser.toMacroDeclaration(""));
    }
}
