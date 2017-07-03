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
package org.xwiki.filter.xml.internal.input;

import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;

import org.xwiki.filter.FilterException;
import org.xwiki.filter.input.AbstractBeanInputFilterStreamFactory;
import org.xwiki.filter.input.BeanInputFilterStream;
import org.xwiki.filter.type.FilterStreamType;
import org.xwiki.filter.xml.input.XMLInputProperties;

/**
 * @param <P>
 * @version $Id$
 * @since 6.2M1
 */
public abstract class AbstractXMLBeanInputFilterStreamFactory<P extends XMLInputProperties, F>
    extends AbstractBeanInputFilterStreamFactory<P, F>
{
    protected XMLInputFactory xmlFactory;

    public AbstractXMLBeanInputFilterStreamFactory(FilterStreamType type)
    {
        super(type);
    }

    @Override
    public BeanInputFilterStream<P> createInputFilterStream(P properties) throws FilterException
    {
        return new DefaultXMLInputFilterStream<P, F>(this, properties, this.xmlFactory);
    }

    protected abstract XMLEventWriter createXMLEventWriter(Object filter, P parameters);
}
