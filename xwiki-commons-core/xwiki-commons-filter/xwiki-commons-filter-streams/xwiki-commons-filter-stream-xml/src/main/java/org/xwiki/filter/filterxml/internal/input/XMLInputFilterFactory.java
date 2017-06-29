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
package org.xwiki.filter.filterxml.internal.input;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.xml.stream.XMLEventWriter;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.filter.FilterException;
import org.xwiki.filter.UnknownFilter;
import org.xwiki.filter.filterxml.input.FilterXMLInputProperties;
import org.xwiki.filter.filterxml.internal.output.XMLOutputFilterFactory;
import org.xwiki.filter.output.OutputFilterStreamFactory;
import org.xwiki.filter.type.FilterStreamType;
import org.xwiki.filter.xml.internal.input.AbstractXMLBeanInputFilterStreamFactory;
import org.xwiki.filter.xml.parser.XMLParserFactory;

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
public class XMLInputFilterFactory extends AbstractXMLBeanInputFilterStreamFactory<FilterXMLInputProperties, Object>
{
    @Inject
    private XMLParserFactory parserFactory;

    @Inject
    private Provider<ComponentManager> contextComponentManager;

    /**
     * Default constructor.
     */
    public XMLInputFilterFactory()
    {
        super(FilterStreamType.FILTER_XML);

        setName("Generic XML input stream");
        setDescription("Generates wiki events from generic XML file.");
    }

    @Override
    public Collection<Class<?>> getFilterInterfaces() throws FilterException
    {
        List<OutputFilterStreamFactory> factories;
        try {
            factories = this.contextComponentManager.get().getInstanceList(OutputFilterStreamFactory.class);
        } catch (ComponentLookupException e) {
            throw new FilterException("Failed to lookup OutputFilterFactory components instances", e);
        }

        Set<Class<?>> filters = new HashSet<>();

        filters.add(UnknownFilter.class);

        for (OutputFilterStreamFactory factory : factories) {
            if (factory.getClass() != XMLOutputFilterFactory.class) {
                filters.addAll(factory.getFilterInterfaces());
            }
        }

        return filters;
    }

    @Override
    protected XMLEventWriter createXMLEventWriter(Object filter, FilterXMLInputProperties properties)
    {
        return this.parserFactory.createXMLEventWriter(filter, null);
    }
}
