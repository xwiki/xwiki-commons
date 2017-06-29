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
package org.xwiki.filter.filterxml.internal.output;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Result;

import org.apache.commons.lang3.ArrayUtils;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.filter.FilterException;
import org.xwiki.filter.UnknownFilter;
import org.xwiki.filter.filterxml.internal.input.XMLInputFilterFactory;
import org.xwiki.filter.filterxml.output.FilterXMLOutputProperties;
import org.xwiki.filter.input.InputFilterStreamFactory;
import org.xwiki.filter.type.FilterStreamType;
import org.xwiki.filter.xml.internal.output.AbstractXMLBeanOutputFilterStreamFactory;
import org.xwiki.filter.xml.serializer.XMLSerializerFactory;

import com.ctc.wstx.api.WstxOutputProperties;

/**
 * A generic xml output filter implementation. This class can be used as a test bench to validate various XMLInputStream
 * wiki parsers.
 *
 * @version $Id$
 * @since 6.2M1
 */
@Component
@Named("filter+xml")
@Singleton
public class XMLOutputFilterFactory extends AbstractXMLBeanOutputFilterStreamFactory<FilterXMLOutputProperties, Object>
{
    @Inject
    private XMLSerializerFactory serializerFactory;

    @Inject
    private Provider<ComponentManager> contextComponentManager;

    /**
     * Default constructor.
     */
    public XMLOutputFilterFactory()
    {
        super(FilterStreamType.FILTER_XML);

        setName("Generic XML output stream");
        setDescription("Write generic XML from wiki events.");

        this.xmlFactory = XMLOutputFactory.newInstance();
        // Allow producing XML with several root elements (there is no constraint on events to have a single root
        // begin/end event)
        this.xmlFactory.setProperty(WstxOutputProperties.P_OUTPUT_VALIDATE_STRUCTURE, false);
    }

    @Override
    public Collection<Class<?>> getFilterInterfaces() throws FilterException
    {
        List<InputFilterStreamFactory> factories;
        try {
            factories = this.contextComponentManager.get().getInstanceList(InputFilterStreamFactory.class);
        } catch (ComponentLookupException e) {
            throw new FilterException("Failed to lookup InputFilterFactory components instances", e);
        }

        Set<Class<?>> filters = new HashSet<>();

        filters.add(UnknownFilter.class);

        for (InputFilterStreamFactory factory : factories) {
            if (factory.getClass() != XMLInputFilterFactory.class) {
                filters.addAll(factory.getFilterInterfaces());
            }
        }

        return filters;
    }

    @Override
    protected Object createListener(Result result, FilterXMLOutputProperties properties)
        throws XMLStreamException, FactoryConfigurationError, FilterException
    {
        return this.serializerFactory.createSerializer(getFilterInterfaces().toArray(ArrayUtils.EMPTY_CLASS_ARRAY),
            result, null);
    }
}
