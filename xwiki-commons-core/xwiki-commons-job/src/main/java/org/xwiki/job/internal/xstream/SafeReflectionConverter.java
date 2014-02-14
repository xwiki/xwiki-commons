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
package org.xwiki.job.internal.xstream;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.SingleValueConverter;
import com.thoughtworks.xstream.converters.reflection.ReflectionConverter;
import com.thoughtworks.xstream.converters.reflection.ReflectionProvider;
import com.thoughtworks.xstream.core.ReferencingMarshallingContext;
import com.thoughtworks.xstream.core.util.ArrayIterator;
import com.thoughtworks.xstream.io.ExtendedHierarchicalStreamWriterHelper;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;

/**
 * A {@link ReflectionConverter} which never fail whatever value is provided.
 * 
 * @version $Id$
 * @since 5.4M1
 */
public class SafeReflectionConverter extends ReflectionConverter
{
    /**
     * The logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SafeTreeMarshaller.class);

    private static class FieldInfo
    {
        final String fieldName;

        final Class type;

        final Class definedIn;

        final Object value;

        FieldInfo(String fieldName, Class type, Class definedIn, Object value)
        {
            this.fieldName = fieldName;
            this.type = type;
            this.definedIn = definedIn;
            this.value = value;
        }
    }

    private final XStream xstream;

    public SafeReflectionConverter(XStream xstream)
    {
        super(xstream.getMapper(), xstream.getReflectionProvider());

        this.xstream = xstream;
    }

    @Override
    protected void doMarshal(final Object source, final HierarchicalStreamWriter writer,
        final MarshallingContext context)
    {
        final List fields = new ArrayList();
        final Map defaultFieldDefinition = new HashMap();

        // Attributes might be preferred to child elements ...
        reflectionProvider.visitSerializableFields(source, new ReflectionProvider.Visitor()
        {
            final Set writtenAttributes = new HashSet();

            public void visit(String fieldName, Class type, Class definedIn, Object value)
            {
                if (!mapper.shouldSerializeMember(definedIn, fieldName)) {
                    return;
                }
                if (!defaultFieldDefinition.containsKey(fieldName)) {
                    Class lookupType = source.getClass();
                    // See XSTR-457 and OmitFieldsTest
                    if (definedIn != source.getClass() && !mapper.shouldSerializeMember(lookupType, fieldName)) {
                        lookupType = definedIn;
                    }
                    defaultFieldDefinition.put(fieldName, reflectionProvider.getField(lookupType, fieldName));
                }

                SingleValueConverter converter = mapper.getConverterFromItemType(fieldName, type, definedIn);
                if (converter != null) {
                    final String attribute = mapper.aliasForAttribute(mapper.serializedMember(definedIn, fieldName));
                    if (value != null) {
                        if (writtenAttributes.contains(fieldName)) { // TODO: use attribute
                            throw new ConversionException("Cannot write field with name '" + fieldName
                                + "' twice as attribute for object of type " + source.getClass().getName());
                        }
                        final String str = converter.toString(value);
                        if (str != null) {
                            writer.addAttribute(attribute, str);
                        }
                    }
                    writtenAttributes.add(fieldName); // TODO: use attribute
                } else {
                    fields.add(new FieldInfo(fieldName, type, definedIn, value));
                }
            }
        });

        new Object()
        {
            {
                for (Iterator fieldIter = fields.iterator(); fieldIter.hasNext();) {
                    FieldInfo info = (FieldInfo) fieldIter.next();
                    if (info.value != null) {
                        Mapper.ImplicitCollectionMapping mapping =
                            mapper.getImplicitCollectionDefForFieldName(source.getClass(), info.fieldName);
                        if (mapping != null) {
                            if (context instanceof ReferencingMarshallingContext) {
                                if (info.value != Collections.EMPTY_LIST && info.value != Collections.EMPTY_SET
                                    && info.value != Collections.EMPTY_MAP) {
                                    ReferencingMarshallingContext refContext = (ReferencingMarshallingContext) context;
                                    refContext.registerImplicit(info.value);
                                }
                            }
                            final boolean isCollection = info.value instanceof Collection;
                            final boolean isMap = info.value instanceof Map;
                            final boolean isEntry = isMap && mapping.getKeyFieldName() == null;
                            final boolean isArray = info.value.getClass().isArray();
                            for (Iterator iter =
                                isArray ? new ArrayIterator(info.value) : isCollection ? ((Collection) info.value)
                                    .iterator() : isEntry ? ((Map) info.value).entrySet().iterator()
                                    : ((Map) info.value).values().iterator(); iter.hasNext();) {
                                Object obj = iter.next();
                                final String itemName;
                                final Class itemType;
                                if (obj == null) {
                                    itemType = Object.class;
                                    itemName = mapper.serializedClass(null);
                                } else if (isEntry) {
                                    final String entryName =
                                        mapping.getItemFieldName() != null ? mapping.getItemFieldName() : mapper
                                            .serializedClass(Map.Entry.class);
                                    Map.Entry entry = (Map.Entry) obj;
                                    ExtendedHierarchicalStreamWriterHelper.startNode(writer, entryName,
                                        entry.getClass());
                                    writeItem(entry.getKey(), context, writer);
                                    writeItem(entry.getValue(), context, writer);
                                    writer.endNode();
                                    continue;
                                } else if (mapping.getItemFieldName() != null) {
                                    itemType = mapping.getItemType();
                                    itemName = mapping.getItemFieldName();
                                } else {
                                    itemType = obj.getClass();
                                    itemName = mapper.serializedClass(itemType);
                                }
                                safeWriteField(info.fieldName, itemName, itemType, info.definedIn, obj);
                            }
                        } else {
                            safeWriteField(info.fieldName, null, info.type, info.definedIn, info.value);
                        }
                    }
                }

            }

            void safeWriteField(String fieldName, String aliasName, Class fieldType, Class definedIn, Object newObj)
            {
                if (XStreamUtils.isSafeType(newObj)) {
                    writeField(fieldName, aliasName, fieldType, definedIn, newObj);
                } else {
                    try {
                        // Test the serialization
                        xstream.marshal(newObj, VoidWriter.WRITER, new DataHolderWrapper(context));

                        // Do the actual serialization
                        writeField(fieldName, aliasName, fieldType, definedIn, newObj);
                    } catch (Throwable e) {
                        LOGGER.debug("Failed to write field [" + fieldName + "], ignore", e);
                    }
                }
            }

            void writeField(String fieldName, String aliasName, Class fieldType, Class definedIn, Object newObj)
            {
                Class actualType = newObj != null ? newObj.getClass() : fieldType;
                ExtendedHierarchicalStreamWriterHelper.startNode(writer,
                    aliasName != null ? aliasName : mapper.serializedMember(source.getClass(), fieldName), actualType);

                if (newObj != null) {
                    Class defaultType = mapper.defaultImplementationOf(fieldType);
                    if (!actualType.equals(defaultType)) {
                        String serializedClassName = mapper.serializedClass(actualType);
                        if (!serializedClassName.equals(mapper.serializedClass(defaultType))) {
                            String attributeName = mapper.aliasForSystemAttribute("class");
                            if (attributeName != null) {
                                writer.addAttribute(attributeName, serializedClassName);
                            }
                        }
                    }

                    final Field defaultField = (Field) defaultFieldDefinition.get(fieldName);
                    if (defaultField.getDeclaringClass() != definedIn) {
                        String attributeName = mapper.aliasForSystemAttribute("defined-in");
                        if (attributeName != null) {
                            writer.addAttribute(attributeName, mapper.serializedClass(definedIn));
                        }
                    }

                    Field field = reflectionProvider.getField(definedIn, fieldName);
                    marshallField(context, newObj, field);
                }
                writer.endNode();
            }

            void writeItem(Object item, MarshallingContext context, HierarchicalStreamWriter writer)
            {
                if (item == null) {
                    String name = mapper.serializedClass(null);
                    ExtendedHierarchicalStreamWriterHelper.startNode(writer, name, Mapper.Null.class);
                    writer.endNode();
                } else {
                    String name = mapper.serializedClass(item.getClass());
                    ExtendedHierarchicalStreamWriterHelper.startNode(writer, name, item.getClass());
                    context.convertAnother(item);
                    writer.endNode();
                }
            }
        };
    }
}
