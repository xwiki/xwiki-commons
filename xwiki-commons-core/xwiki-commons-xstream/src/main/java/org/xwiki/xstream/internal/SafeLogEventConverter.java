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
package org.xwiki.xstream.internal;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Marker;
import org.xwiki.logging.LogLevel;
import org.xwiki.logging.event.LogEvent;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * Filter {@link LogEvent} arguments allowed to be serialized.
 * 
 * @version $Id$
 * @since 11.9RC1
 */
public class SafeLogEventConverter extends SafeMessageConverter
{
    protected static final String FIELD_LEVEL = "level";

    protected static final String FIELD_TIMESTAMP = "timeStamp";

    /**
     * @param xstream the {@link com.thoughtworks.xstream.XStream} instance to use to isolate array element marshaling
     */
    public SafeLogEventConverter(SafeXStream xstream)
    {
        super(xstream);
    }

    @Override
    public boolean canConvert(Class type)
    {
        return type == LogEvent.class;
    }

    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context)
    {
        LogEvent log = (LogEvent) source;

        // Level
        XStreamUtils.serializeField(FIELD_LEVEL, LogLevel.class, log.getLevel(), writer, context, mapper());

        // TimeStamp
        writer.startNode(FIELD_TIMESTAMP);
        writer.setValue(String.valueOf(log.getTimeStamp()));
        writer.endNode();

        super.marshal(source, writer, context);
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context)
    {
        LogLevel level = null;
        Marker marker = null;
        String message = "";
        long timeStamp = -1;
        List<Object> arguments = new ArrayList<>();
        Throwable throwable = null;

        while (reader.hasMoreChildren()) {
            reader.moveDown();
            switch (reader.getNodeName()) {
                case FIELD_LEVEL:
                    level = read(LogLevel.class, reader, context);
                    break;
                case FIELD_MARKER:
                    marker = read(Marker.class, reader, context);
                    break;
                case FIELD_MESSAGE:
                    message = reader.getValue();
                    break;
                case FIELD_TIMESTAMP:
                    String timeStampString = reader.getValue();
                    timeStamp = NumberUtils.toLong(timeStampString, -1);
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

        return new LogEvent(marker, level, message, arguments.toArray(), throwable, timeStamp);
    }
}
