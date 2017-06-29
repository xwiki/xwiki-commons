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
package org.xwiki.filter.xml.internal.parser;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.xerces.parsers.XMLParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import org.xwiki.component.util.ReflectionUtils;
import org.xwiki.filter.FilterDescriptor;
import org.xwiki.filter.FilterElementDescriptor;
import org.xwiki.filter.FilterElementParameterDescriptor;
import org.xwiki.filter.FilterEventParameters;
import org.xwiki.filter.UnknownFilter;
import org.xwiki.filter.xml.XMLConfiguration;
import org.xwiki.filter.xml.internal.XMLUtils;
import org.xwiki.filter.xml.internal.parameter.ParameterManager;
import org.xwiki.properties.ConverterManager;
import org.xwiki.properties.converter.ConversionException;
import org.xwiki.xml.Sax2Dom;

/**
 * Default implementation of {@link XMLParser}.
 *
 * @version $Id$
 * @since 5.2M1
 */
// TODO: move from ContentHandler to XMLEventConsumer or XMLEventWriter
public class DefaultXMLParser extends DefaultHandler implements ContentHandler
{
    /**
     * Logging helper object.
     */
    protected static final Logger LOGGER = LoggerFactory.getLogger(DefaultXMLParser.class);

    private ParameterManager parameterManager;

    private ConverterManager stringConverter;

    private FilterDescriptor filterDescriptor;

    private Object filter;

    private Deque<Block> blockStack = new LinkedList<>();

    private int elementDepth = 0;

    private StringBuilder content;

    private XMLConfiguration configuration;

    public static class Block
    {
        public String name;

        public FilterElementDescriptor filterElement;

        public boolean beginSent = false;

        public List<Object> parameters = new ArrayList<Object>();

        public FilterEventParameters namedParameters = new FilterEventParameters();

        public Sax2Dom parametersDOMBuilder;

        public int elementDepth;

        private Object[] parametersTable;

        public Block(String name, FilterElementDescriptor listenerElement, int elementDepth)
        {
            this.name = name;
            this.filterElement = listenerElement;
            this.elementDepth = elementDepth;
        }

        public boolean isContainer()
        {
            return this.filterElement == null || this.filterElement.getBeginMethod() != null;
        }

        public void setParameter(String name, Object value)
        {
            this.namedParameters.put(name, value);
        }

        public void setParameter(int index, Object value)
        {
            for (int i = this.parameters.size(); i <= index; ++i) {
                this.parameters.add(this.filterElement.getParameters()[i].getDefaultValue());
            }

            this.parameters.set(index, value);
            this.parametersTable = null;
        }

        public List<Object> getParametersList()
        {
            return this.parameters;
        }

        public Object[] getParametersTable()
        {
            if (this.parametersTable == null) {
                if (this.parameters.isEmpty()) {
                    this.parametersTable = ArrayUtils.EMPTY_OBJECT_ARRAY;
                }

                this.parametersTable = this.parameters.toArray();
            }

            return this.parametersTable;
        }

        public void fireBeginEvent(Object listener) throws SAXException
        {
            if (this.filterElement != null) {
                fireEvent(this.filterElement.getBeginMethod(), listener);
            } else if (listener instanceof UnknownFilter) {
                try {
                    ((UnknownFilter) listener).beginUnknwon(this.name, this.namedParameters);
                } catch (Exception e) {
                    throw new SAXException("Failed to invoke unknown event with name [" + this.name
                        + "] and parameters [" + this.namedParameters + "]", e);
                }
            }
            this.beginSent = true;
        }

        public void fireEndEvent(Object listener) throws SAXException
        {
            if (this.filterElement != null) {
                fireEvent(this.filterElement.getEndMethod(), listener);
            } else if (listener instanceof UnknownFilter) {
                try {
                    ((UnknownFilter) listener).endUnknwon(this.name, this.namedParameters);
                } catch (Exception e) {
                    throw new SAXException("Failed to invoke unknown event with name [" + this.name
                        + "] and parameters [" + this.namedParameters + "]", e);
                }
            }
        }

        public void fireOnEvent(Object listener) throws SAXException
        {
            if (this.filterElement != null) {
                fireEvent(this.filterElement.getOnMethod(), listener);
            } else if (listener instanceof UnknownFilter) {
                try {
                    ((UnknownFilter) listener).onUnknwon(this.name, this.namedParameters);
                } catch (Exception e) {
                    throw new SAXException("Failed to invoke unknown event with name [" + this.name
                        + "] and parameters [" + this.namedParameters + "]", e);
                }
            }
        }

        private void fireEvent(Method eventMethod, Object listener) throws SAXException
        {
            Object[] parameters = getParametersTable();
            Class<?>[] methodParameters = eventMethod.getParameterTypes();

            Object[] properParameters;
            // Missing parameters
            if (methodParameters.length > parameters.length) {
                properParameters = new Object[methodParameters.length];
                for (int i = 0; i < methodParameters.length; ++i) {
                    if (i < parameters.length) {
                        properParameters[i] = parameters[i];
                    } else {
                        properParameters[i] = this.filterElement.getParameters()[i].getDefaultValue();
                    }
                }
            } else {
                properParameters = parameters;
            }

            // Invalid primitive
            for (int i = 0; i < properParameters.length; ++i) {
                Object parameter = properParameters[i];

                if (parameter == null) {
                    Class<?> methodParameter = methodParameters[i];

                    if (methodParameter.isPrimitive()) {
                        properParameters[i] = XMLUtils.emptyValue(methodParameter);
                    }
                }
            }

            // Send event
            try {
                eventMethod.invoke(listener, properParameters);
            } catch (InvocationTargetException e) {
                throw new SAXException("Event [" + eventMethod + "] thrown exception",
                    e.getCause() instanceof Exception ? (Exception) e.getCause() : e);
            } catch (Exception e) {
                throw new SAXException("Failed to invoke event [" + eventMethod + "]", e);
            }
        }
    }

    public DefaultXMLParser(Object listener, FilterDescriptor listenerDescriptor, ConverterManager stringConverter,
        ParameterManager parameterManager, XMLConfiguration configuration)
    {
        this.filter = listener;
        this.filterDescriptor = listenerDescriptor;
        this.stringConverter = stringConverter;
        this.parameterManager = parameterManager;
        this.configuration = configuration != null ? configuration : new XMLConfiguration();
    }

    private boolean onBlockChild()
    {
        boolean result;

        if (!this.blockStack.isEmpty()) {
            Block currentBlock = this.blockStack.peek();

            return currentBlock.elementDepth == (this.elementDepth - 1);
        } else {
            result = false;
        }

        return result;
    }

    private boolean onBlockElement(String elementName)
    {
        boolean result;

        if (!this.blockStack.isEmpty()) {
            Block currentBlock = this.blockStack.peek();

            result = (this.elementDepth - currentBlock.elementDepth <= 1)
                && !this.configuration.getElementParameters().equals(elementName);
        } else {
            result = true;
        }

        return result;
    }

    private boolean onParametersElement(String elementName)
    {
        return onBlockChild() && this.configuration.getElementParameters().equals(elementName);
    }

    private int extractParameterIndex(String elementName)
    {
        Matcher matcher = XMLUtils.INDEX_PATTERN.matcher(elementName);
        matcher.find();

        return Integer.valueOf(matcher.group(1));
    }

    private boolean isReservedBlockAttribute(String attributeName)
    {
        return this.configuration.getAttributeBlockName().equals(attributeName)
            || this.configuration.getAttributeParameterName().equals(attributeName);
    }

    private void setParameter(Block block, String name, Object value, boolean attribute) throws SAXException
    {
        if (XMLUtils.INDEX_PATTERN.matcher(name).matches()) {
            int parameterIndex = extractParameterIndex(name);

            if (block.filterElement != null && block.filterElement.getParameters().length > parameterIndex) {
                FilterElementParameterDescriptor<?> filterParameter =
                    block.filterElement.getParameters()[parameterIndex];

                setParameter(block, filterParameter, value);
            } else {
                LOGGER.warn("Unknown element parameter [{}] (=[{}]) in block [{}] (available parameters are {})", name,
                    value, block.name, Arrays.asList(block.filterElement.getParameters()));

                block.setParameter(name, value);
            }
        } else if (!attribute || !isReservedBlockAttribute(name)) {
            if (block.filterElement != null) {
                FilterElementParameterDescriptor<?> filterParameter = block.filterElement.getParameter(name);

                if (filterParameter != null) {
                    setParameter(block, filterParameter, value);
                } else {
                    LOGGER.warn("Unknown element parameter [{}] (=[{}]) in block [{}] (available parameters are {})",
                        name, value, block.name, Arrays.asList(block.filterElement.getParameters()));

                    block.setParameter(name, value);
                }
            }
        }
    }

    private void setParameter(Block block, FilterElementParameterDescriptor<?> filterParameter, Object value)
        throws SAXException
    {
        Type type = filterParameter.getType();

        if (value instanceof Element) {
            try {
                block.setParameter(filterParameter.getIndex(), unserializeParameter(type, (Element) value));
            } catch (ClassNotFoundException e) {
                throw new SAXException("Failed to parse property", e);
            }
        } else if (value instanceof String) {
            String stringValue = (String) value;

            Class<?> typeClass = ReflectionUtils.getTypeClass(type);

            if (typeClass == String.class || typeClass == Object.class) {
                block.setParameter(filterParameter.getIndex(), stringValue);
            } else {
                try {
                    block.setParameter(filterParameter.getIndex(), this.stringConverter.convert(type, value));
                } catch (ConversionException e) {
                    if (stringValue.isEmpty()) {
                        block.setParameter(filterParameter.getIndex(), XMLUtils.emptyValue(typeClass));
                    }

                    LOGGER.warn("Unsuported conversion to type [{}] for value [{}]", type, value);
                }
            }
        } else {
            LOGGER.warn("Unsuported type [{}] for value [{}]", value.getClass(), value);
        }
    }

    private Object unserializeParameter(Type type, Element element) throws ClassNotFoundException
    {
        if (element.hasAttribute(this.configuration.getAttributeParameterType())) {
            String typeString = element.getAttribute(this.configuration.getAttributeParameterType());
            return this.parameterManager
                .unSerialize(Class.forName(typeString, true, Thread.currentThread().getContextClassLoader()), element);
        }

        return this.parameterManager.unSerialize(type, element);
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
    {
        Block currentBlock = this.blockStack.isEmpty() ? null : this.blockStack.peek();

        if (onBlockElement(qName)) {
            if (currentBlock != null) {
                // send previous event
                if (!currentBlock.beginSent) {
                    currentBlock.fireBeginEvent(this.filter);
                }
            }

            // push new event
            Block block = getBlock(qName, attributes);

            this.blockStack.push(block);

            if (!block.isContainer() && block.filterElement != null && block.filterElement.getParameters().length > 0) {
                this.content = new StringBuilder();
            }

            // Extract simple parameters from attributes
            for (int i = 0; i < attributes.getLength(); ++i) {
                String attributeName = attributes.getQName(i);

                setParameter(block, attributeName, attributes.getValue(i), true);
            }
        } else {
            if (onParametersElement(qName)) {
                // starting a new block parameter
                if (currentBlock.filterElement != null) {
                    try {
                        currentBlock.parametersDOMBuilder = new Sax2Dom();
                    } catch (ParserConfigurationException e) {
                        throw new SAXException("Failed to create new Sax2Dom handler", e);
                    }
                    currentBlock.parametersDOMBuilder.startDocument();
                }
            }

            if (currentBlock.parametersDOMBuilder != null) {
                currentBlock.parametersDOMBuilder.startElement(uri, localName, qName, attributes);
            }
        }

        ++this.elementDepth;
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException
    {
        --this.elementDepth;

        Block currentBlock = this.blockStack.isEmpty() ? null : this.blockStack.peek();

        if (onBlockElement(qName)) {
            Block block = this.blockStack.pop();

            // Flush pending begin event and send end event or send on event
            if (block.isContainer()) {
                if (!block.beginSent) {
                    block.fireBeginEvent(this.filter);
                }

                block.fireEndEvent(this.filter);
            } else {
                if (block.getParametersList().size() == 0
                    && this.filterDescriptor.getElement(qName).getParameters().length > 0) {
                    if (this.content != null && this.content.length() > 0) {
                        block.setParameter(0,
                            this.stringConverter.convert(
                                this.filterDescriptor.getElement(qName).getParameters()[0].getType(),
                                this.content.toString()));
                        this.content = null;
                    }
                }

                block.fireOnEvent(this.filter);
            }
        } else if (currentBlock.parametersDOMBuilder != null) {
            currentBlock.parametersDOMBuilder.endElement(uri, localName, qName);

            if (onParametersElement(qName)) {
                currentBlock.parametersDOMBuilder.endDocument();

                Element rootElement = currentBlock.parametersDOMBuilder.getRootElement();
                NodeList parameterNodes = rootElement.getChildNodes();
                for (int i = 0; i < parameterNodes.getLength(); ++i) {
                    Node parameterNode = parameterNodes.item(i);

                    if (parameterNode.getNodeType() == Node.ELEMENT_NODE) {
                        String nodeName = parameterNode.getLocalName();

                        setParameter(currentBlock, nodeName, parameterNode, true);
                    }
                }

                currentBlock.parametersDOMBuilder = null;
            }
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException
    {
        if (!this.blockStack.isEmpty() && this.blockStack.peek().parametersDOMBuilder != null) {
            this.blockStack.peek().parametersDOMBuilder.characters(ch, start, length);
        } else if (this.content != null) {
            this.content.append(ch, start, length);
        }
    }

    @Override
    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException
    {
        if (!this.blockStack.isEmpty() && this.blockStack.peek().parametersDOMBuilder != null) {
            this.blockStack.peek().parametersDOMBuilder.ignorableWhitespace(ch, start, length);
        }
    }

    @Override
    public void skippedEntity(String name) throws SAXException
    {
        if (!this.blockStack.isEmpty() && this.blockStack.peek().parametersDOMBuilder != null) {
            this.blockStack.peek().parametersDOMBuilder.skippedEntity(name);
        }
    }

    private Block getBlock(String qName, Attributes attributes)
    {
        String blockName;
        if (this.configuration.getElementBlock().equals(qName)) {
            blockName = attributes.getValue(this.configuration.getAttributeBlockName());
        } else {
            blockName = qName;
        }

        FilterElementDescriptor element = this.filterDescriptor.getElement(blockName);

        if (element == null) {
            LOGGER.warn("Uknown filter element [{}]", blockName);
        }

        return new Block(qName, element, this.elementDepth);
    }
}
