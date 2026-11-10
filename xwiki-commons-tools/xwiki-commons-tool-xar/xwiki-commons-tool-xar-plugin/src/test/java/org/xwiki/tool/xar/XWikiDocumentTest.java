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
import java.util.Map;

import org.dom4j.DocumentException;
import org.junit.jupiter.api.Test;
import org.xwiki.tool.xar.internal.XWikiDocument;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

    @Test
    void formatVersion() throws DocumentException
    {
        XWikiDocument xdocument = new XWikiDocument();

        xdocument.fromXML("<xwikidoc version=\"1.6\" reference=\"Space.Page\"/>");
        assertEquals("1.6", xdocument.getFormatVersion());

        xdocument.fromXML("<xwikidoc><web>Space</web><name>Page</name></xwikidoc>");
        assertEquals("1.0", xdocument.getFormatVersion());
    }

    @Test
    void requiredFormatVersions() throws DocumentException
    {
        XWikiDocument xdocument = new XWikiDocument();

        xdocument.fromXML("<xwikidoc reference=\"Space.Page\">"
            + "<attachment><revisions/></attachment>"
            + "<originalMetadataAuthor>xwiki:XWiki.Admin</originalMetadataAuthor>"
            + "<enforceRequiredRights>true</enforceRequiredRights>"
            + "<object><property type=\"String\"><name>value</name></property></object>"
            + "</xwikidoc>");
        assertEquals(Map.of("the <revisions> element of an attachment", "1.4",
            XWikiDocument.ORIGINAL_METADATA_AUTHOR_ELEMENT, "1.5", "the <enforceRequiredRights> element", "1.6",
            "the type attribute of an object property", "1.7"), xdocument.getRequiredFormatVersions());

        // An XClass property named like a versioned element, and an object property without a type, are not the
        // versioned elements.
        xdocument.fromXML("<xwikidoc reference=\"Space.Page\">"
            + "<class><enforceRequiredRights><name>enforceRequiredRights</name></enforceRequiredRights></class>"
            + "<object><property><name>value</name></property></object>"
            + "</xwikidoc>");
        assertEquals(Map.of(), xdocument.getRequiredFormatVersions());
    }

    @Test
    void isValidFormatVersion()
    {
        assertTrue(XWikiDocument.isValidFormatVersion("1.6"));
        assertTrue(XWikiDocument.isValidFormatVersion("1.10"));
        assertFalse(XWikiDocument.isValidFormatVersion("1.x"));
        assertFalse(XWikiDocument.isValidFormatVersion("1"));
        assertFalse(XWikiDocument.isValidFormatVersion("1.6 "));
        assertFalse(XWikiDocument.isValidFormatVersion("1.6.1"));
    }

    @Test
    void compareFormatVersions()
    {
        assertTrue(XWikiDocument.compareFormatVersions("1.2", "1.6") < 0);
        assertEquals(0, XWikiDocument.compareFormatVersions("1.6", "1.6"));
        // Compared numerically, not as strings.
        assertTrue(XWikiDocument.compareFormatVersions("1.10", "1.7") > 0);
        assertTrue(XWikiDocument.compareFormatVersions("2.0", "1.7") > 0);

        IllegalArgumentException exception =
            assertThrows(IllegalArgumentException.class, () -> XWikiDocument.compareFormatVersions("1.x", "1.6"));
        assertEquals("Invalid XAR format version [1.x], expected [<major>.<minor>]", exception.getMessage());
    }

    @Test
    void getHighestFormatVersion()
    {
        assertEquals("1.6", XWikiDocument.getHighestFormatVersion("1.2", List.of("1.4", "1.6", "1.5")));
        assertEquals("1.7", XWikiDocument.getHighestFormatVersion("1.7", List.of("1.6")));
        assertEquals("1.2", XWikiDocument.getHighestFormatVersion("1.2", List.of()));
    }
}
