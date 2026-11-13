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
package org.xwiki.xml.internal;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;
import org.xwiki.xml.XMLReaderFactory;

/**
 * Create secure XML readers: protected against entity expansion attacks and resolving external entities locally.
 *
 * @version $Id$
 * @since 1.7.1
 */
public class DefaultXMLReaderFactory implements XMLReaderFactory
{
    @Override
    public XMLReader createXMLReader() throws SAXException, ParserConfigurationException
    {
        SAXParserFactory parserFactory = SAXParserFactory.newInstance();

        // Use the standard SAX2 defaults (namespace support enabled, namespace prefixes not reported as attributes),
        // which is not what the JAXP factory does by default.
        parserFactory.setNamespaceAware(true);

        // Protect against entity expansion attacks (CWE-776).
        try {
            parserFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        } catch (SAXNotRecognizedException | SAXNotSupportedException e) {
            // All implementations are required to support the above feature, but for example
            // the Piccolo parser (piccolo.sourceforge.net) JAXPSAXParserFactory does not.
            // We let it go through, the security feature will just not be active.
        }

        XMLReader xmlReader = parserFactory.newSAXParser().getXMLReader();

        // Protect against XXE attacks except entity expansion (CWE-776). Allows resolving well-known entities
        // locally. See https://css4j.github.io/resolver.html for details.
        xmlReader.setEntityResolver(new LocalEntityResolver());

        return xmlReader;
    }
}
