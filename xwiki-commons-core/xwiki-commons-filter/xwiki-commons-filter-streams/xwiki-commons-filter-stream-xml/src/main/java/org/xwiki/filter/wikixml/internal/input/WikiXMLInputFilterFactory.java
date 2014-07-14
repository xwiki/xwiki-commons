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
package org.xwiki.filter.wikixml.internal.input;

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
import org.xwiki.filter.output.OutputFilterStreamFactory;
import org.xwiki.filter.type.FilterStreamType;
import org.xwiki.filter.wikixml.input.WikiXMLInputProperties;
import org.xwiki.filter.wikixml.internal.output.WikiXMLOutputFilterFactory;
import org.xwiki.filter.xml.internal.input.AbstractXMLBeanInputFilterFactory;
import org.xwiki.filter.xml.parser.XMLParserFactory;

/**
 * A generic xml output filter implementation. This class can be used as a test bench to validate various
 * XMLInputStream wiki parsers.
 * 
 * @version $Id$
 * @since 6.2M1
 */
@Component
@Named("wiki+xml")
@Singleton
public class WikiXMLInputFilterFactory extends
    AbstractXMLBeanInputFilterFactory<WikiXMLInputProperties, Object>
{
    @Inject
    private XMLParserFactory parserFactory;

    @Inject
    private Provider<ComponentManager> contextComponentManager;

    /**
     * Default constructor.
     */
    public WikiXMLInputFilterFactory()
    {
        super(FilterStreamType.WIKI_XML);

        setName("Generic XML output stream");
        setDescription("Generates wiki events from generic XML file.");
    }

    @Override
    public Collection<Class< ? >> getFilterInterfaces() throws FilterException
    {
        List<OutputFilterStreamFactory> factories;
        try {
            factories = this.contextComponentManager.get().getInstanceList(OutputFilterStreamFactory.class);
        } catch (ComponentLookupException e) {
            throw new FilterException("Failed to lookup OutputFilterFactory components instances", e);
        }

        Set<Class< ? >> filters = new HashSet<Class< ? >>();

        filters.add(UnknownFilter.class);

        for (OutputFilterStreamFactory factory : factories) {
            if (factory.getClass() != WikiXMLOutputFilterFactory.class) {
                filters.addAll(factory.getFilterInterfaces());
            }
        }

        return filters;
    }

    @Override
    protected XMLEventWriter createXMLEventWriter(Object filter, WikiXMLInputProperties parameters)
    {
        return this.parserFactory.createXMLEventWriter(filter, null);
    }
}
