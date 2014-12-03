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
package org.xwiki.extension.repository.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Serialize and unserialize {@link Map} properties.
 *
 * @param <M> the type of the property value
 * @version $Id$
 */
public class StringKeyMapExtensionPropertySerializer<M extends Map> extends AbstractExtensionPropertySerializer<M>
{
    /**
     * The serializers by type id.
     */
    protected Map<String, ExtensionPropertySerializer> serializerById;

    /**
     * The serializers by class.
     */
    protected Map<Class<?>, ExtensionPropertySerializer> serializerByClass;

    /**
     * @param type the type of the property
     * @param serializerById the serializers by type id
     * @param serializerByClass the serializers by class
     */
    protected StringKeyMapExtensionPropertySerializer(String type,
        Map<String, ExtensionPropertySerializer> serializerById,
        Map<Class<?>, ExtensionPropertySerializer> serializerByClass)
    {
        super(type);

        this.serializerById = serializerById;
        this.serializerByClass = serializerByClass;
    }

    /**
     * @param serializerById the serializers by type id
     * @param serializerByClass the serializers by class
     */
    public StringKeyMapExtensionPropertySerializer(Map<String, ExtensionPropertySerializer> serializerById,
        Map<Class<?>, ExtensionPropertySerializer> serializerByClass)
    {
        this("strinkeygmap", serializerById, serializerByClass);
    }

    @Override
    public M toValue(Element element)
    {
        M map = (M) new HashMap();

        NodeList featuresNodes = element.getChildNodes();

        for (int i = 0; i < featuresNodes.getLength(); ++i) {
            Node node = featuresNodes.item(i);

            if (node.getNodeType() == Node.ELEMENT_NODE) {
                String key = node.getNodeName();
                Object value = CollectionExtensionPropertySerializer.toValue((Element) node, this.serializerById);

                map.put(key, value);
            }
        }

        return map;
    }

    @Override
    public Element toElement(Document document, String elementName, M elementValue)
    {
        Element element = createRootElement(document, elementName);

        Set<Map.Entry> set = elementValue.entrySet();
        for (Map.Entry entry : set) {
            if (entry.getKey() != null) {
                Element subElement =
                    CollectionExtensionPropertySerializer.toElement(entry.getValue(), document, entry.getKey()
                        .toString(), this.serializerByClass);

                if (subElement == null) {
                    return null;
                }

                element.appendChild(subElement);
            }
        }

        return element;
    }
}
