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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Serialize and unserialize {@link Collection} properties.
 * 
 * @param <C> the type of the property value
 * @version $Id$
 */
public class CollectionExtensionPropertySerializer<C extends Collection> extends AbstractExtensionPropertySerializer<C>
{
    /**
     * The serializers by type id.
     */
    protected Map<String, ExtensionPropertySerializer> serializerById;

    /**
     * The serializers by class.
     */
    protected Map<Class< ? >, ExtensionPropertySerializer> serializerByClass;

    /**
     * @param type the type of the property
     * @param serializerById the serializers by type id
     * @param serializerByClass the serializers by class
     */
    protected CollectionExtensionPropertySerializer(String type,
        Map<String, ExtensionPropertySerializer> serializerById,
        Map<Class< ? >, ExtensionPropertySerializer> serializerByClass)
    {
        super(type);

        this.serializerById = serializerById;
        this.serializerByClass = serializerByClass;
    }

    /**
     * @param serializerById the serializers by type id
     * @param serializerByClass the serializers by class
     */
    public CollectionExtensionPropertySerializer(Map<String, ExtensionPropertySerializer> serializerById,
        Map<Class< ? >, ExtensionPropertySerializer> serializerByClass)
    {
        this("collection", serializerById, serializerByClass);
    }

    /**
     * @return a new collection
     */
    protected C createCollection()
    {
        return (C) new ArrayList();
    }

    /**
     * @param <T> the type of the expected value
     * @param element the element to unserialize
     * @param serializerById the serializers by type id
     * @return the unserialized property value
     */
    public static <T> T toValue(Element element, Map<String, ExtensionPropertySerializer> serializerById)
    {
        if (element != null) {
            String type = element.getAttribute("type");

            ExtensionPropertySerializer< ? > serializer = serializerById.get(type);

            if (serializer != null) {
                return (T) serializer.toValue(element);
            }
        }

        return null;
    }

    /**
     * @param valueClass the class of the value to serialize
     * @param serializerByClass the serializers by class
     * @return the serializer for the provided class
     */
    public static ExtensionPropertySerializer getSerializerByClass(Class< ? > valueClass,
        Map<Class< ? >, ExtensionPropertySerializer> serializerByClass)
    {
        for (Map.Entry<Class< ? >, ExtensionPropertySerializer> entry : serializerByClass.entrySet()) {
            if (entry.getKey().isAssignableFrom(valueClass)) {
                return entry.getValue();
            }
        }

        return null;
    }

    /**
     * @param value the value to serialize
     * @param document the document used to create new {@link Element}
     * @param elementName the name of the element to create
     * @param serializerByClass the serializers by class
     * @return the serialized property {@link Element}
     */
    public static Element toElement(Object value, Document document, String elementName,
        Map<Class< ? >, ExtensionPropertySerializer> serializerByClass)
    {
        if (value != null) {
            ExtensionPropertySerializer serializer = getSerializerByClass(value.getClass(), serializerByClass);

            if (serializer != null) {
                return serializer.toElement(document, elementName, value);
            }
        }

        return null;
    }

    @Override
    public C toValue(Element element)
    {
        C collection = createCollection();

        NodeList featuresNodes = element.getChildNodes();

        for (int i = 0; i < featuresNodes.getLength(); ++i) {
            Node node = featuresNodes.item(i);

            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Object value = toValue((Element) node, this.serializerById);

                if (value == null) {
                    return null;
                } else {
                    collection.add(value);
                }
            }
        }

        return collection;
    }

    @Override
    public Element toElement(Document document, String elementName, C elementValue)
    {
        Element element = createRootElement(document, elementName);

        for (Object subValue : elementValue) {
            Element subElement = toElement(subValue, document, elementName, this.serializerByClass);

            if (subElement == null) {
                return null;
            }

            element.appendChild(subElement);
        }

        return element;
    }
}
