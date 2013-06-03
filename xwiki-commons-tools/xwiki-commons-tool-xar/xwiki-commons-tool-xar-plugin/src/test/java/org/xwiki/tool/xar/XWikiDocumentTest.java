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

import org.junit.Assert;
import org.junit.Test;

/**
 * Validate {@link XWikiDocument}.
 * 
 * @version $Id$
 */
public class XWikiDocumentTest
{
    // Tests

    @Test
    public void getFullName()
    {
        XWikiDocument xdocument = new XWikiDocument();

        xdocument.setSpace("Space");
        xdocument.setName("Page");

        Assert.assertEquals("Space.Page", xdocument.getFullName());

        // .

        xdocument.setName("Page.with.dots");

        Assert.assertEquals("Space.Page\\.with\\.dots", xdocument.getFullName());

        xdocument.setSpace("Space.with.dots");

        Assert.assertEquals("Space\\.with\\.dots.Page\\.with\\.dots", xdocument.getFullName());

        // \

        xdocument.setSpace("Space");
        xdocument.setName("Page\\with\\backslashes");

        Assert.assertEquals("Space.Page\\\\with\\\\backslashes", xdocument.getFullName());

        xdocument.setSpace("Space\\with\\backslashes");

        Assert.assertEquals("Space\\\\with\\\\backslashes.Page\\\\with\\\\backslashes", xdocument.getFullName());
    }
}
