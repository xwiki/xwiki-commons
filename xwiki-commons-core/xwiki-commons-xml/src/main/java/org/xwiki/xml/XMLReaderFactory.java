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
package org.xwiki.xml;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xwiki.component.annotation.Role;

/**
 * Factory to create secure {@link XMLReader}. This gives us a level of indirection versus using directly
 * {@link javax.xml.parsers.SAXParserFactory}, to configure the readers the same way everywhere (protection against
 * entity expansion and external entities, local resolution of the well-known DTDs).
 *
 * @version $Id$
 * @since 1.7.1
 */
@Role
public interface XMLReaderFactory
{
    /**
     * @return the optimised XML Reader instance
     * @throws SAXException in case of an error in the creation of the XML Reader instance
     * @throws ParserConfigurationException in case of an error in the creation of the XML Reader instance
     */
    XMLReader createXMLReader() throws SAXException, ParserConfigurationException;
}
