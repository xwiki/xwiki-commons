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
package org.xwiki.filter.xml.serializer;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Result;

import org.xwiki.component.annotation.Role;
import org.xwiki.filter.xml.XMLConfiguration;

/**
 * Create XML serializers as listeners.
 *
 * @version $Id$
 * @since 5.2M1
 */
@Role
public interface XMLSerializerFactory
{
    /**
     * @param filterInterface the interface of the filter that will receive the events
     * @param xmlResult the StAX event writer
     * @param configuration the configuration of the serializer
     * @return the listener to send events to
     * @param <T> the type of the event listener
     * @throws FactoryConfigurationError if no instance of XMLOutputFactory can be found
     * @throws XMLStreamException if the passed result is not supported
     */
    <T> T createSerializer(Class<T> filterInterface, Result xmlResult, XMLConfiguration configuration)
        throws XMLStreamException, FactoryConfigurationError;

    /**
     * @param filterInterfaces the interfaces of the filter that will receive the events
     * @param xmlResult the StAX event writer
     * @param configuration the configuration of the serializer
     * @return the listener to send events to
     * @param <T> the type of the event listener
     * @throws FactoryConfigurationError if no instance of XMLOutputFactory can be found
     * @throws XMLStreamException if the passed result is not supported
     * @since 5.3M1
     */
    <T> T createSerializer(Class<?>[] filterInterfaces, Result xmlResult, XMLConfiguration configuration)
        throws XMLStreamException, FactoryConfigurationError;
}
