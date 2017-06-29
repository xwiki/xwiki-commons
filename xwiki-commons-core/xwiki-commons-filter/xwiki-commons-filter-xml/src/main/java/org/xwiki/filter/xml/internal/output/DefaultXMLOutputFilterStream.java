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
package org.xwiki.filter.xml.internal.output;

import java.io.IOException;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;

import org.xwiki.filter.FilterException;
import org.xwiki.filter.output.BeanOutputFilterStream;
import org.xwiki.filter.xml.output.XMLOutputProperties;

/**
 * @param <P>
 * @version $Id$
 * @since 6.2M1
 */
public class DefaultXMLOutputFilterStream<P extends XMLOutputProperties, F> extends AbstractXMLOutputFilterStream<P>
    implements BeanOutputFilterStream<P>
{
    private final AbstractXMLBeanOutputFilterStreamFactory<P, F> factory;

    public DefaultXMLOutputFilterStream(AbstractXMLBeanOutputFilterStreamFactory<P, F> factory, P properties,
        XMLOutputFactory xmlFactory) throws FilterException, XMLStreamException, IOException
    {
        super(properties, xmlFactory);

        this.factory = factory;
    }

    @Override
    protected Object createFilter(P properties) throws XMLStreamException, FactoryConfigurationError, FilterException
    {
        return this.factory.createListener(this.result, properties);
    }

    @Override
    public void setProperties(P properties) throws FilterException
    {
        // Not needed
    }
}
