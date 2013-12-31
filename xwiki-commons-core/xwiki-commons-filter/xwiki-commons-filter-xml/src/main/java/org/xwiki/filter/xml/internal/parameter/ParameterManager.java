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

import javax.xml.stream.XMLStreamWriter;

import org.w3c.dom.Element;
import org.xwiki.component.annotation.Role;

/**
 * Parse and serialize object to and from XML.
 * 
 * @version $Id$
 * @since 5.2M1
 */
@Role
public interface ParameterManager
{
    /**
     * Write the passed object on the passed stream writer.
     * 
     * @param type the type of the element to serialize
     * @param object the object to convert
     * @param xmlStreamWriter the xml writer
     */
    void serialize(Type type, Object object, XMLStreamWriter xmlStreamWriter);

    /**
     * Convert the passed {@link Element} into an instance of the passed type.
     * 
     * @param type the type
     * @param rootElement the source to convert
     * @return the object
     * @throws ClassNotFoundException when failing to find the class of the property to create
     */
    Object unSerialize(Type type, Element rootElement) throws ClassNotFoundException;
}
