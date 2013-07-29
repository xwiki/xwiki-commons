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
package org.xwiki.filter.json.internal.serializer;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.filter.FilterDescriptor;
import org.xwiki.filter.FilterElement;
import org.xwiki.filter.FilterElementParameter;
import org.xwiki.filter.json.JSONConfiguration;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Proxy called as an event filter to write JSON elements.
 * 
 * @version $Id$
 * @since 5.2M1
 */
@Component
@Singleton
public class DefaultJSONSerializer implements InvocationHandler
{
    private final FilterDescriptor descriptor;

    private final JSONConfiguration configuration;

    private Stack<Integer> index = new Stack<Integer>();

    private JsonGenerator generator;

    public DefaultJSONSerializer(Writer writer, FilterDescriptor descriptor, JSONConfiguration configuration)
        throws IOException
    {
        this.descriptor = descriptor;
        this.configuration = configuration != null ? configuration : new JSONConfiguration();

        JsonFactory jsonFactory = new JsonFactory();

        this.generator = jsonFactory.createGenerator(writer);
        this.generator.setCodec(new ObjectMapper());
        this.generator.useDefaultPrettyPrinter();
    }

    private String getBlockType(String eventName, String prefix)
    {
        String blockName = eventName.substring(prefix.length());
        blockName = Character.toLowerCase(blockName.charAt(0)) + blockName.substring(1);

        return blockName;
    }

    private void incrementIndex()
    {
        this.index.set(this.index.size() - 1, this.index.peek() + 1);
    }

    private void pushIndex()
    {
        this.index.push(0);
    }

    private void popIndex()
    {
        this.index.pop();
    }

    private int getIndex()
    {
        return this.index.size() > 0 ? this.index.peek() : 0;
    }

    private void beginEvent(String eventName, Object[] parameters) throws IOException
    {
        if (this.index.size() > 0 && getIndex() == 0) {
            this.generator.writeFieldName(this.configuration.getBlockChildrenField());
        }

        String blockType = getBlockType(eventName, "begin");

        FilterElement element = this.descriptor.getElements().get(blockType.toLowerCase());

        List<Object> elementParameters = parameters != null ? Arrays.asList(parameters) : null;

        // Print start element
        this.generator.writeStartObject();

        this.generator.writeStringField(this.configuration.getBlockTypeField(), blockType);

        // Write complex parameters
        writeParameters(elementParameters, element);

        pushIndex();
    }

    private void endEvent() throws IOException
    {
        if (this.index.size() > 0 && getIndex() > 1) {
            this.generator.writeEndArray();
        }

        popIndex();

        this.generator.writeEndObject();

        if (!this.index.isEmpty()) {
            incrementIndex();
        }

        this.generator.flush();
    }

    private void onEvent(String eventName, Object[] parameters) throws IOException
    {
        if (this.index.size() > 0 && getIndex() == 0) {
            this.generator.writeArrayFieldStart(this.configuration.getBlockChildrenField());
        }

        String blockType = getBlockType(eventName, "on");

        FilterElement element = this.descriptor.getElements().get(blockType.toLowerCase());

        List<Object> elementParameters = parameters != null ? Arrays.asList(parameters) : null;

        this.generator.writeStartObject();

        this.generator.writeStringField(this.configuration.getBlockTypeField(), blockType);

        writeParameters(elementParameters, element);

        pushIndex();
        endEvent();
    }

    private void writeParameters(List<Object> parameters, FilterElement descriptor) throws IOException
    {
        if (parameters != null && !parameters.isEmpty()) {
            for (int i = 0; i < parameters.size(); ++i) {
                Object value = parameters.get(i);

                if (value != null) {
                    FilterElementParameter filterParameter = descriptor.getParameters()[i];

                    String elementName;

                    if (filterParameter.getName() != null) {
                        elementName = filterParameter.getName();
                    } else {
                        elementName = this.configuration.getBlockParameterField() + filterParameter.getIndex();
                    }

                    this.generator.writeObjectField(elementName, parameters.get(i));
                }
            }
        }
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
    {
        Object result = null;

        if (method.getName().startsWith("begin")) {
            beginEvent(method.getName(), args);
        } else if (method.getName().startsWith("end")) {
            endEvent();
        } else if (method.getName().startsWith("on")) {
            onEvent(method.getName(), args);
        } else {
            throw new NoSuchMethodException(method.toGenericString());
        }

        return result;
    }
}
