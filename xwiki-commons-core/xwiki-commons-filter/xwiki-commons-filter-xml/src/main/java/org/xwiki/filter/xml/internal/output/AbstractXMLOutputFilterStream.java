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

import java.io.Closeable;
import java.io.IOException;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Result;
import javax.xml.transform.stax.StAXResult;

import org.xwiki.filter.FilterException;
import org.xwiki.filter.output.OutputFilterStream;
import org.xwiki.filter.output.OutputTarget;
import org.xwiki.filter.xml.output.ResultOutputTarget;
import org.xwiki.filter.xml.output.XMLOutputProperties;

/**
 * @param <P>
 * @version $Id$
 * @since 6.2M1
 */
public abstract class AbstractXMLOutputFilterStream<P extends XMLOutputProperties> implements OutputFilterStream
{
    protected final P properties;

    protected final Result result;

    protected Object filter;

    public AbstractXMLOutputFilterStream(P properties, XMLOutputFactory xmlFactory)
        throws FilterException, XMLStreamException, IOException
    {
        this.properties = properties;
        this.result = createResult(this.properties, xmlFactory);
    }

    protected Result createResult(P properties, XMLOutputFactory xmlfactory)
        throws FilterException, XMLStreamException, IOException
    {
        OutputTarget target = properties.getTarget();

        Result result;

        if (target instanceof ResultOutputTarget) {
            result = ((ResultOutputTarget) target).getResult();
        } else {
            XMLStreamWriter xmlStreamWriter = XMLOutputFilterStreamUtils.createXMLStreamWriter(xmlfactory, properties);

            result = new StAXResult(xmlStreamWriter);
        }

        return result;
    }

    @Override
    public Object getFilter() throws FilterException
    {
        if (this.filter == null) {
            try {
                this.filter = createFilter(this.properties);
            } catch (Exception e) {
                throw new FilterException("Failed to create filter", e);
            }
        }

        return this.filter;
    }

    protected abstract Object createFilter(P parameters)
        throws XMLStreamException, FactoryConfigurationError, FilterException;

    @Override
    public void close() throws IOException
    {
        if (this.filter instanceof Closeable) {
            ((Closeable) this.filter).close();
        }

        this.properties.getTarget().close();
    }
}
