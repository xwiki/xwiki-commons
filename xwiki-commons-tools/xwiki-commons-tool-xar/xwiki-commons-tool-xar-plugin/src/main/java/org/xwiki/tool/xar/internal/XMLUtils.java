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
package org.xwiki.tool.xar.internal;

import org.dom4j.io.SAXReader;
import org.xml.sax.SAXException;

/**
 * XML Utility methods.
 *
 * @version $Id$
 * @since 14.10
 */
public final class XMLUtils
{
    private XMLUtils()
    {
        // Prevents being instantiated
    }

    /**
     * @return a safe XML Reader protected against XXE attacks (by forbidding the use of DOCTYPE).
     */
    public static SAXReader getSAXReader()
    {
        try {
            SAXReader reader = new SAXReader();
            // Note: Prevent XXE attacks by disabling completely DTDs. This is possible since XWiki XML documents don't
            // contain DOCTYPEs.
            reader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            return reader;
        } catch (SAXException e) {
            throw new RuntimeException("Failed to configure the XML parser to read XAR data", e);
        }
    }
}
