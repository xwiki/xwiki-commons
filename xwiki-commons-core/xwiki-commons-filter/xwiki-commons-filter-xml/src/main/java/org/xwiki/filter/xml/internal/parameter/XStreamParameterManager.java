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
package org.xwiki.filter.xml.internal.parameter;

import java.lang.reflect.Type;
import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.slf4j.Logger;
import org.w3c.dom.Element;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.component.util.ReflectionUtils;
import org.xwiki.filter.xml.internal.XMLUtils;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.DataHolder;
import com.thoughtworks.xstream.core.MapBackedDataHolder;
import com.thoughtworks.xstream.io.xml.DomReader;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import com.thoughtworks.xstream.io.xml.StaxWriter;

/**
 * XStream based implementation of {@link ParameterManager}.
 *
 * @version $Id$
 * @since 5.2M1
 */
@Component
@Singleton
public class XStreamParameterManager implements ParameterManager, Initializable
{
    /**
     * The name of the type field containing the default type of the parameter.
     */
    public static final String DDEFAULTTYPE_NAME = "defaultType";

    /**
     * The logger.
     */
    @Inject
    private Logger logger;

    /**
     * The XStream entry point.
     */
    private XStream xstream;

    /**
     * Used to create xml stream writers.
     */
    private StaxDriver staxDriver;

    @Override
    public void initialize() throws InitializationException
    {
        this.staxDriver = new StaxDriver();
        this.xstream = new ParameterXStream(this.staxDriver);
    }

    @Override
    public void serialize(Type type, Object object, XMLStreamWriter xmlStreamWriter)
    {
        Class<?> typeClass = ReflectionUtils.getTypeClass(type);
        if (typeClass != null && Objects.equals(XMLUtils.emptyValue(typeClass), object)) {
            return;
        }

        StaxWriter staxWriter;
        try {
            staxWriter = this.staxDriver.createStaxWriter(xmlStreamWriter, false);
        } catch (XMLStreamException e) {
            // Should never happen since that when sending start document event
            this.logger.error("Failed to create new instance of StaxWriter", e);

            return;
        }

        DataHolder dataHolder = new MapBackedDataHolder();
        if (type != Object.class) {
            dataHolder.put(DDEFAULTTYPE_NAME, type);
        }

        this.xstream.marshal(object, staxWriter, dataHolder);
    }

    @Override
    public Object unSerialize(Type type, Element rootElement) throws ClassNotFoundException
    {
        if (type != null && !rootElement.hasChildNodes()) {
            Object value = XMLUtils.emptyValue(ReflectionUtils.getTypeClass(type));
            if (value != null) {
                return value;
            }
        }

        DataHolder dataHolder = new MapBackedDataHolder();

        if (type == Object.class) {
            dataHolder.put(DDEFAULTTYPE_NAME, String.class);
        } else {
            dataHolder.put(DDEFAULTTYPE_NAME, type);
        }

        return this.xstream.unmarshal(new DomReader(rootElement), null, dataHolder);
    }
}
