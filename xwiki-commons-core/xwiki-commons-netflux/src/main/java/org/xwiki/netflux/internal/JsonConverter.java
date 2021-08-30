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
package org.xwiki.netflux.internal;

import java.util.List;

import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Provides conversion between a JSON string and a list of Java objects.
 * 
 * @version $Id$
 * @since 13.8RC1
 */
public class JsonConverter implements Decoder.Text<List<Object>>, Encoder.Text<List<Object>>
{
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void init(EndpointConfig config)
    {
    }

    @Override
    public void destroy()
    {
    }

    @Override
    public List<Object> decode(String json) throws DecodeException
    {
        try {
            return this.mapper.readValue(json,
                this.mapper.getTypeFactory().constructCollectionType(List.class, Object.class));
        } catch (JsonProcessingException e) {
            throw new DecodeException(json, "Failed to parse JSON message.", e);
        }
    }

    @Override
    public boolean willDecode(String json)
    {
        return true;
    }

    @Override
    public String encode(List<Object> list) throws EncodeException
    {
        try {
            return this.mapper.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            throw new EncodeException(list, "Failed to serialize JSON message.", e);
        }
    }
}
