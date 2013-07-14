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
package org.xwiki.filter.xml.parser;

import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;

import org.xml.sax.ContentHandler;
import org.xwiki.component.annotation.Role;
import org.xwiki.filter.xml.XMLConfiguration;

/**
 * Create XML parsers.
 * 
 * @version $Id$
 * @since 5.2M1
 */
@Role
public interface XMLParserFactory
{
    /**
     * @param source the source to read
     * @param filter the filter to send events to
     * @param configuration the configuration of the parser
     * @throws XMLStreamException when failing to create the parser
     */
    void parse(Source source, Object filter, XMLConfiguration configuration) throws XMLStreamException;

    /**
     * @param filter the filter to send events to
     * @param configuration the configuration of the parser
     * @return the parser as a {@link Result}.
     */
    Result createResult(Object filter, XMLConfiguration configuration);

    /**
     * @param filter the filter to send events to
     * @param configuration the configuration of the parser
     * @return the parser as a {@link ContentHandler}
     */
    ContentHandler createContentHandler(Object filter, XMLConfiguration configuration);
}
