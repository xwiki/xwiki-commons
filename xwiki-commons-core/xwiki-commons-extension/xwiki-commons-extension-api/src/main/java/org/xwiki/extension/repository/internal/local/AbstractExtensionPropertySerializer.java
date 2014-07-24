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
package org.xwiki.extension.repository.internal.local;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Base class to help implement {@link ExtensionPropertySerializer}.
 *
 * @param <T> the type of the property value
 * @version $Id$
 */
public abstract class AbstractExtensionPropertySerializer<T> implements ExtensionPropertySerializer<T>
{
    /**
     * The simple type identifier. Used to recognize the type when unserializing.
     */
    private String type;

    /**
     * @param type the type identifier
     */
    public AbstractExtensionPropertySerializer(String type)
    {
        this.type = type;
    }

    /**
     * @return the type identifier
     */
    public String getType()
    {
        return this.type;
    }

    /**
     * @param document the document used to create new {@link Element}
     * @param elementName the name of the element to create
     * @return the new element
     */
    protected Element createRootElement(Document document, String elementName)
    {
        Element element = document.createElement(elementName);

        if (getType() != null) {
            element.setAttribute("type", getType());
        }

        return element;
    }

    @Override
    public Element toElement(Document document, String elementName, T elementValue)
    {
        Element element = createRootElement(document, elementName);

        element.setTextContent(elementValue.toString());

        return element;
    }
}
