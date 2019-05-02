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

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Result;

import org.xwiki.component.util.ReflectionUtils;
import org.xwiki.filter.FilterDescriptor;
import org.xwiki.filter.FilterElementDescriptor;
import org.xwiki.filter.FilterElementParameterDescriptor;
import org.xwiki.filter.internal.DefaultFilterDescriptorManager;
import org.xwiki.filter.xml.XMLConfiguration;
import org.xwiki.filter.xml.internal.XMLUtils;
import org.xwiki.filter.xml.internal.parameter.ParameterManager;
import org.xwiki.properties.ConverterManager;
import org.xwiki.xml.stax.StAXUtils;

import com.ctc.wstx.api.WstxOutputProperties;
import com.ctc.wstx.stax.WstxOutputFactory;

/**
 * Proxy called as an event filter to produce SAX events.
 *
 * @version $Id$
 * @since 5.2M1
 */
public class DefaultXMLSerializer implements InvocationHandler, Closeable
{
    /**
     * Force using Woodstox implementation which is thread safe.
     */
    private static final XMLOutputFactory XML_OUTPUT_FACTORY = new WstxOutputFactory();

    static {
        // Allow producing XML with several root elements (there is no constraint on events to have a single root
        // begin/end event)
        XML_OUTPUT_FACTORY.setProperty(WstxOutputProperties.P_OUTPUT_VALIDATE_STRUCTURE, false);
    }

    private static final Pattern VALID_ELEMENTNAME = Pattern.compile("[A-Za-z][A-Za-z0-9:_.-]*");

    private final XMLStreamWriter xmlStreamWriter;

    private final ParameterManager parameterManager;

    private final FilterDescriptor descriptor;

    private final ConverterManager converter;

    private final XMLConfiguration configuration;

    public DefaultXMLSerializer(Result result, ParameterManager parameterManager, FilterDescriptor descriptor,
        ConverterManager converter, XMLConfiguration configuration) throws XMLStreamException, FactoryConfigurationError
    {
        this.parameterManager = parameterManager;
        this.descriptor = descriptor;
        this.converter = converter;
        this.configuration = configuration != null ? configuration : new XMLConfiguration();

        this.xmlStreamWriter = StAXUtils.getXMLStreamWriter(XML_OUTPUT_FACTORY, result);
    }

    private boolean isValidBlockElementName(String blockName)
    {
        return VALID_ELEMENTNAME.matcher(blockName).matches()
            && !this.configuration.getElementParameters().equals(blockName);
    }

    private boolean isValidParameterElementName(String parameterName)
    {
        return VALID_ELEMENTNAME.matcher(parameterName).matches()
            && !XMLUtils.INDEX_PATTERN.matcher(parameterName).matches();
    }

    private boolean isValidParameterAttributeName(String parameterName)
    {
        return isValidParameterElementName(parameterName)
            && !this.configuration.getAttributeParameterName().equals(parameterName);
    }

    private String getBlockName(String eventName)
    {
        String blockName = Character.toLowerCase(eventName.charAt(0)) + eventName.substring(1);

        return blockName;
    }

    private void writeInlineParameters(List<Object> parameters, FilterElementDescriptor element)
        throws XMLStreamException
    {
        for (int i = 0; i < parameters.size(); ++i) {
            Object parameterValue = parameters.get(i);

            if (parameterValue != null) {
                FilterElementParameterDescriptor<?> filterParameter = element.getParameters()[i];

                if (!Objects.equals(filterParameter.getDefaultValue(), parameterValue)) {
                    Class<?> typeClass = ReflectionUtils.getTypeClass(filterParameter.getType());

                    String attributeName;

                    if (filterParameter.getName() != null) {
                        if (isValidParameterAttributeName(filterParameter.getName())) {
                            attributeName = filterParameter.getName();
                        } else {
                            attributeName = null;
                        }
                    } else {
                        attributeName = "_" + filterParameter.getIndex();
                    }

                    if (attributeName != null) {
                        if (parameterValue instanceof String) {
                            this.xmlStreamWriter.writeAttribute(attributeName, (String) parameterValue);

                            parameters.set(filterParameter.getIndex(), null);
                        } else if (XMLUtils.isSimpleType(typeClass)) {
                            this.xmlStreamWriter.writeAttribute(attributeName,
                                this.converter.<String>convert(String.class, parameterValue));

                            parameters.set(filterParameter.getIndex(), null);
                        } else if (Objects.equals(XMLUtils.emptyValue(typeClass), parameterValue)) {
                            this.xmlStreamWriter.writeAttribute(attributeName, "");

                            parameters.set(filterParameter.getIndex(), null);
                        }
                    }
                }
            }
        }
    }

    private void writeStartAttributes(String blockName, List<Object> parameters) throws XMLStreamException
    {
        if (!isValidBlockElementName(blockName)) {
            this.xmlStreamWriter.writeAttribute(this.configuration.getAttributeBlockName(), blockName);
        }

        if (parameters != null) {
            FilterElementDescriptor element = this.descriptor.getElement(blockName);

            writeInlineParameters(parameters, element);
        }
    }

    private void removeDefaultParameters(List<Object> parameters, FilterElementDescriptor descriptor)
    {
        if (parameters != null) {
            for (int i = 0; i < parameters.size(); ++i) {
                Object value = parameters.get(i);

                if (!shouldWriteParameter(value, descriptor.getParameters()[i])) {
                    parameters.set(i, null);
                }
            }
        }
    }

    private void beginEvent(Method method, Object[] parameters) throws XMLStreamException
    {
        String blockName = DefaultFilterDescriptorManager.getElementName(method, true);

        FilterElementDescriptor element = this.descriptor.getElement(blockName);

        List<Object> elementParameters = parameters != null ? Arrays.asList(parameters) : null;

        // Remove useless parameters
        removeDefaultParameters(elementParameters, element);

        // Get proper element name
        String elementName;
        if (isValidBlockElementName(blockName)) {
            elementName = blockName;
        } else {
            elementName = this.configuration.getElementBlock();
        }

        // Print start element
        this.xmlStreamWriter.writeStartElement(elementName);

        // Put as attributes parameters which are simple enough to not require full XML serialization
        writeStartAttributes(blockName, elementParameters);

        // Write complex parameters
        writeParameters(elementParameters, element);
    }

    private void endEvent() throws XMLStreamException
    {
        this.xmlStreamWriter.writeEndElement();
    }

    private void onEvent(Method method, Object[] parameters) throws XMLStreamException
    {
        String blockName = DefaultFilterDescriptorManager.getElementName(method, true);

        FilterElementDescriptor element = this.descriptor.getElement(blockName);

        List<Object> elementParameters = parameters != null ? Arrays.asList(parameters) : null;

        // Remove useless parameters
        removeDefaultParameters(elementParameters, element);

        // Get proper element name
        String elementName;
        if (isValidBlockElementName(blockName)) {
            elementName = blockName;
        } else {
            elementName = this.configuration.getElementBlock();
        }

        // Write start element
        this.xmlStreamWriter.writeStartElement(elementName);

        // Put as attributes parameters which are simple enough to not require full XML serialization
        if (elementParameters != null && elementParameters.size() > 1) {
            writeStartAttributes(blockName, Arrays.asList(parameters));
        }

        // Write complex parameters
        if (parameters != null && parameters.length == 1
            && XMLUtils.isSimpleType(element.getParameters()[0].getType())) {
            Object parameter = parameters[0];
            if (parameter != null && !Objects.equals(element.getParameters()[0].getDefaultValue(), parameter)) {
                String value = parameter.toString();
                this.xmlStreamWriter.writeCharacters(value);
            }
        } else {
            writeParameters(elementParameters, element);
        }

        // Write end element
        this.xmlStreamWriter.writeEndElement();
    }

    private boolean shouldWriteParameter(Object value, FilterElementParameterDescriptor<?> filterParameter)
    {
        boolean write;

        if (value != null && !Objects.equals(filterParameter.getDefaultValue(), value)) {
            write = true;

            Type type = filterParameter.getType();

            if (type instanceof Class) {
                Class<?> typeClass = (Class<?>) type;
                try {
                    if (typeClass.isPrimitive()) {
                        write = !XMLUtils.emptyValue(typeClass).equals(value);
                    }
                } catch (Exception e) {
                    // Should never happen
                }
            }
        } else {
            write = false;
        }

        return write;
    }

    private void writeParameters(List<Object> parameters, FilterElementDescriptor descriptor) throws XMLStreamException
    {
        if (parameters != null && !parameters.isEmpty()) {
            boolean writeContainer = false;

            for (Object parameter : parameters) {
                if (parameter != null) {
                    writeContainer = true;
                    break;
                }
            }

            if (writeContainer) {
                this.xmlStreamWriter.writeStartElement(this.configuration.getElementParameters());
            }

            for (int i = 0; i < parameters.size(); ++i) {
                Object parameterValue = parameters.get(i);

                FilterElementParameterDescriptor<?> filterParameter = descriptor.getParameters()[i];

                if (shouldWriteParameter(parameterValue, filterParameter)) {
                    String elementName;
                    String attributeName = null;
                    String attributeValue = null;

                    if (filterParameter.getName() != null) {
                        if (isValidParameterElementName(filterParameter.getName())) {
                            elementName = filterParameter.getName();
                        } else {
                            elementName = "_" + filterParameter.getIndex();
                            attributeName = this.configuration.getAttributeParameterName();
                            attributeValue = filterParameter.getName();
                        }
                    } else {
                        elementName = "_" + filterParameter.getIndex();
                    }

                    this.xmlStreamWriter.writeStartElement(elementName);

                    if (attributeName != null) {
                        this.xmlStreamWriter.writeAttribute(attributeName, attributeValue);
                    }
                    if (descriptor.getParameters()[i].getType() == Object.class
                        && parameterValue.getClass() != String.class) {
                        this.xmlStreamWriter.writeAttribute(this.configuration.getAttributeParameterType(),
                            parameterValue.getClass().getCanonicalName());
                    }

                    this.parameterManager.serialize(descriptor.getParameters()[i].getType(), parameterValue,
                        this.xmlStreamWriter);

                    this.xmlStreamWriter.writeEndElement();
                }
            }

            if (writeContainer) {
                this.xmlStreamWriter.writeEndElement();
            }
        }
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
    {
        Object result = null;

        if (method.getDeclaringClass() == Closeable.class) {
            close();
        } else {
            if (method.getName().startsWith("begin")) {
                beginEvent(method, args);
            } else if (method.getName().startsWith("end")) {
                endEvent();
            } else if (method.getName().startsWith("on")) {
                onEvent(method, args);
            } else {
                throw new NoSuchMethodException(method.toGenericString());
            }
        }

        return result;
    }

    @Override
    public void close() throws IOException
    {
        try {
            this.xmlStreamWriter.close();
        } catch (XMLStreamException e) {
            throw new IOException(e);
        }
    }
}
