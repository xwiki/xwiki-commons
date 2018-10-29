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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Marker;
import org.xwiki.job.annotation.Serializable;
import org.xwiki.logging.Message;
import org.xwiki.logging.event.LogEvent;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.core.util.HierarchicalStreams;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * Filter {@link LogEvent} arguments allowed to be serialized.
 * 
 * @version $Id$
 * @since 8.4RC1
 */
public class SafeMessageConverter extends SafeArrayConverter
{
    protected static final String FIELD_MESSAGE = "message";

    protected static final String FIELD_MARKER = "marker";

    protected static final String FIELD_ARGUMENTARRAY = "argumentArray";

    protected static final String FIELD_THROWABLE = "throwable";

    /**
     * @param xstream the {@link com.thoughtworks.xstream.XStream} instance to use to isolate array element marshaling
     */
    public SafeMessageConverter(SafeXStream xstream)
    {
        super(xstream);
    }

    @Override
    public boolean canConvert(Class type)
    {
        return type == Message.class;
    }

    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context)
    {
        Message message = (Message) source;

        // Message
        XStreamUtils.serializeField(FIELD_MESSAGE, String.class, message.getMessage(), writer, context, mapper());

        // Marker
        XStreamUtils.serializeField(FIELD_MARKER, Marker.class, message.getMarker(), writer, context, mapper());

        // Log arguments
        if (message.getArgumentArray() != null) {
            writer.startNode(FIELD_ARGUMENTARRAY);
            for (Object argument : message.getArgumentArray()) {
                if (isSerializable(argument)) {
                    writeCompleteItem(argument, context, writer);
                } else {
                    writeCompleteItem(argument.toString(), context, writer);
                }
            }
            writer.endNode();
        }

        // Throwable
        XStreamUtils.serializeField(FIELD_THROWABLE, Throwable.class, message.getThrowable(), writer, context,
            mapper());
    }

    protected boolean isSerializable(Object argument)
    {
        if (argument == null) {
            return true;
        }

        Serializable serializable = argument.getClass().getAnnotation(Serializable.class);
        if (serializable != null) {
            return serializable.value();
        } else {
            return argument instanceof java.io.Serializable;
        }

        // TODO: Add white list or is Serializable interface and @Serializable annotation enough ?
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context)
    {
        Marker marker = null;
        String message = "";
        List<Object> arguments = Collections.emptyList();
        Throwable throwable = null;

        while (reader.hasMoreChildren()) {
            reader.moveDown();
            switch (reader.getNodeName()) {
                case FIELD_MARKER:
                    marker = read(Marker.class, reader, context);
                    break;
                case FIELD_MESSAGE:
                    message = reader.getValue();
                    break;
                case FIELD_THROWABLE:
                    throwable = read(Throwable.class, reader, context);
                    break;
                case FIELD_ARGUMENTARRAY:
                    arguments = unmarshalArgumentArray(reader, context);
                    break;
                default:
                    break;
            }
            reader.moveUp();
        }

        return new Message(marker, message, arguments.toArray(), throwable);
    }

    protected List<Object> unmarshalArgumentArray(HierarchicalStreamReader reader, UnmarshallingContext context)
    {
        List<Object> arguments = new ArrayList<>();

        while (reader.hasMoreChildren()) {
            reader.moveDown();
            Object argument = readBareItem(reader, context, null);
            arguments.add(argument);
            reader.moveUp();
        }

        return arguments;
    }

    protected <T> T read(Class<T> defaultType, HierarchicalStreamReader reader, UnmarshallingContext context)
    {
        String classAttribute = HierarchicalStreams.readClassAttribute(reader, mapper());

        return (T) context.convertAnother(null,
            classAttribute != null ? mapper().realClass(classAttribute) : defaultType);
    }
}
