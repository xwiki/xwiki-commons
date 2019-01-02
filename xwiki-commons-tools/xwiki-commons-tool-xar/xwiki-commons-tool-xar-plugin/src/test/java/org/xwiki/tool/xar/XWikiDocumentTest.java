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

import org.dom4j.DocumentException;
import org.junit.Test;
import org.xwiki.tool.xar.internal.XWikiDocument;

import static org.junit.Assert.assertEquals;

/**
 * Validate {@link XWikiDocument}.
 * 
 * @version $Id$
 */
public class XWikiDocumentTest
{
    private void assertReferenceFromNameSpace(String reference, String space, String name) throws DocumentException
    {
        XWikiDocument xdocument = new XWikiDocument();
        xdocument.fromXML("<xwikidoc><web>" + space + "</web><name>" + name + "</name></xwikidoc>");

        assertEquals(reference, xdocument.getReference());
    }

    // Tests

    @Test
    public void createReference() throws DocumentException
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
