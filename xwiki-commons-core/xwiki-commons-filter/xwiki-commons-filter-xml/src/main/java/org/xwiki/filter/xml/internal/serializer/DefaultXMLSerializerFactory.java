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
package org.xwiki.filter.xml.internal.serializer;

import java.lang.reflect.Proxy;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Result;

import org.xwiki.component.annotation.Component;
import org.xwiki.filter.FilterDescriptorManager;
import org.xwiki.filter.xml.XMLConfiguration;
import org.xwiki.filter.xml.internal.parameter.ParameterManager;
import org.xwiki.filter.xml.serializer.XMLSerializerFactory;
import org.xwiki.properties.ConverterManager;

/**
 * Default implementation of {@link XMLSerializerFactory}.
 *
 * @version $Id$
 * @since 5.2M1
 */
@Component
@Singleton
public class DefaultXMLSerializerFactory implements XMLSerializerFactory
{
    /**
     * The parameter converter.
     */
    @Inject
    private ParameterManager parameterManager;

    /**
     * The events supported by the filter.
     */
    @Inject
    private FilterDescriptorManager descriptorManager;

    /**
     * Used to convert simple types.
     */
    @Inject
    private ConverterManager converter;

    @Override
    public <T> T createSerializer(Class<T> filterInterface, Result xmlResult, XMLConfiguration configuration)
        throws XMLStreamException, FactoryConfigurationError
    {
        return createSerializer(new Class<?>[] { filterInterface }, xmlResult, configuration);
    }

    @Override
    public <T> T createSerializer(Class<?>[] filterInterfaces, Result xmlResult, XMLConfiguration configuration)
        throws XMLStreamException, FactoryConfigurationError
    {
        DefaultXMLSerializer handler =
            new DefaultXMLSerializer(xmlResult, this.parameterManager,
                this.descriptorManager.getFilterDescriptor(filterInterfaces), this.converter, configuration);

        return (T) Proxy.newProxyInstance(filterInterfaces[0].getClassLoader(), filterInterfaces, handler);
    }
}
