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
package org.xwiki.tool.xar;

import java.util.List;

import org.dom4j.DocumentException;
import org.junit.jupiter.api.Test;
import org.xwiki.tool.xar.internal.XWikiDocument;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Validate {@link XWikiDocument}.
 *
 * @version $Id$
 */
class XWikiDocumentTest
{
    private void assertReferenceFromNameSpace(String reference, String space, String name) throws DocumentException
    {
        XWikiDocument xdocument = new XWikiDocument();
        xdocument.fromXML("<xwikidoc><web>" + space + "</web><name>" + name + "</name></xwikidoc>");

        assertEquals(reference, xdocument.getReference());
    }

    @Test
    void objectPresent() throws DocumentException
    {
        XWikiDocument xdocument = new XWikiDocument();

        xdocument.fromXML("<xwikidoc><web>space</web><name>name</name><object></object></xwikidoc>");
        assertTrue(xdocument.isObjectPresent());

        xdocument.fromXML("<xwikidoc><web>space</web><name>name</name></xwikidoc>");
        assertFalse(xdocument.isObjectPresent());
    }

    @Test
    void wikiMacroIds() throws DocumentException
    {
        XWikiDocument xdocument = new XWikiDocument();

        xdocument.fromXML("<xwikidoc><web>space</web><name>name</name>"
            + "<object><className>XWiki.WikiMacroClass</className><property><id> macro1 </id></property></object>"
            + "<object><className>XWiki.StyleSheetExtension</className><property><id>notAMacro</id></property>"
            + "</object>"
            + "<object><className>XWiki.WikiMacroClass</className><property><id>macro2</id></property></object>"
            + "</xwikidoc>");
        assertEquals(List.of("macro1", "macro2"), xdocument.getWikiMacroIds());

        xdocument = new XWikiDocument();
        xdocument.fromXML("<xwikidoc><web>space</web><name>name</name></xwikidoc>");
        assertEquals(List.of(), xdocument.getWikiMacroIds());
    }

    @Test
    void createReference() throws DocumentException
    {
        assertReferenceFromNameSpace("Space.Page", "Space", "Page");

        // .

        assertReferenceFromNameSpace("Space.Page\\.with\\.dots", "Space", "Page.with.dots");

        assertReferenceFromNameSpace("Space\\.with\\.dots.Page\\.with\\.dots", "Space.with.dots", "Page.with.dots");

        // \

        assertReferenceFromNameSpace("Space.Page\\\\with\\\\backslashes", "Space", "Page\\with\\backslashes");

        assertReferenceFromNameSpace("Space\\\\with\\\\backslashes.Page\\\\with\\\\backslashes",
            "Space\\with\\backslashes", "Page\\with\\backslashes");
    }
}
