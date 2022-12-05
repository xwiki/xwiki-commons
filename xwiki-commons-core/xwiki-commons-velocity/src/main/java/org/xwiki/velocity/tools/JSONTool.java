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
package org.xwiki.velocity.tools;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.SerializableString;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.io.CharacterEscapes;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;

import net.sf.json.JSON;
import net.sf.json.JSONException;
import net.sf.json.JSONSerializer;

/**
 * Velocity tool to facilitate serialization of Java objects to the JSON format.
 *
 * @version $Id$
 * @since 4.0M2
 */
public class JSONTool
{
    private static final Logger LOGGER = LoggerFactory.getLogger(JSONTool.class);

    private static final CustomCharacterEscapes CHARACTER_ESCAPES = new CustomCharacterEscapes();

    /**
     * Serialize a Java object to the JSON format.
     * <p>
     * Examples:
     * <ul>
     * <li>numbers and boolean values: 23, 13.5, true, false</li>
     * <li>strings: "one\"two'three" (quotes included)</li>
     * <li>arrays and collections: [1, 2, 3]</li>
     * <li>maps: {"number": 23, "boolean": false, "string": "value"}</li>
     * <li>beans: {"enabled": true, "name": "XWiki"} for a bean that has #isEnabled() and #getName() getters</li>
     * </ul>
     * <p>
     * This also escapes "/" in the output and thus cannot close HTML tags or wiki macros.
     *
     * @param object the object to be serialized to the JSON format
     * @return the JSON-verified string representation of the given object
     */
    public String serialize(Object object)
    {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.getFactory().setCharacterEscapes(CHARACTER_ESCAPES);
            SimpleModule m = new SimpleModule("org.json.* serializer", new Version(1, 0, 0, "", "org.json", "json"));
            m.addSerializer(JSONObject.class, new JSONObjectSerializer());
            m.addSerializer(JSONArray.class, new JSONArraySerializer());
            mapper.registerModule(m);

            return mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            LOGGER.error("Failed to serialize object to JSON", e);
        }

        return null;
    }

    /**
     * Parse JSON {@link String} into an {@link Object}.
     * 
     * @param str the string to parse
     * @return the {@link Object} resolved from the string (usually a Maps or and Lists)
     * @since 9.9RC1
     */
    public Object fromString(String str)
    {
        if (StringUtils.isNotBlank(str)) {
            ObjectMapper objectMapper = new ObjectMapper();

            try {
                return objectMapper.readValue(str, Object.class);
            } catch (Exception e) {
                LOGGER.info("Failed to parse JSON [{}]: {}", StringUtils.abbreviate(str, 32),
                    ExceptionUtils.getRootCauseMessage(e));

                return null;
            }
        }

        return null;
    }

    class JSONObjectSerializer extends JsonSerializer<JSONObject>
    {
        @Override
        public void serialize(JSONObject value, JsonGenerator jgen, SerializerProvider provider) throws IOException
        {
            jgen.writeRawValue(value.toString());
        }
    }

    class JSONArraySerializer extends JsonSerializer<JSONArray>
    {
        @Override
        public void serialize(JSONArray value, JsonGenerator jgen, SerializerProvider provider) throws IOException
        {
            jgen.writeRawValue(value.toString());
        }
    }

    /**
     * Custom character escapes to also escape forward slash.
     * <p>
     * Inspired by <a href="https://stackoverflow.com/a/6826587/1293930">this answer on Stack Overflow</a>.
     *
     * @since 14.10.1
     * @since 15.0RC1
     */
    private static class CustomCharacterEscapes extends CharacterEscapes
    {
        private final int[] asciiEscapes;

        CustomCharacterEscapes()
        {
            this.asciiEscapes = standardAsciiEscapesForJSON();
            // Forward slash can be escaped by \/ according to the JSON specification.
            this.asciiEscapes['/'] = '/';
        }

        @Override
        public int[] getEscapeCodesForAscii()
        {
            return this.asciiEscapes;
        }

        @Override
        public SerializableString getEscapeSequence(int i)
        {
            return null;
        }
    }

    // Deprecated

    /**
     * Parse a serialized JSON into a real JSON object. Only valid JSON strings can be parsed, and doesn't support
     * JSONP. If the argument is not valid JSON, then {@code null} is returned.
     *
     * @param json the string to parse, must be valid JSON
     * @return the parsed JSON, either a {@link net.sf.json.JSONObject} or a {@link net.sf.json.JSONArray}, or
     *         {@code null} if the argument is not a valid JSON
     * @since 5.2M1
     * @deprecated since 9.9RC1, use {@link #fromString(String)} instead
     */
    @Deprecated
    public JSON parse(String json)
    {
        try {
            return JSONSerializer.toJSON(json);
        } catch (JSONException e) {
            LOGGER.warn("Tried to parse invalid JSON [{}]. Root error: [{}]", StringUtils.abbreviate(json, 32),
                ExceptionUtils.getRootCauseMessage(e));
            return null;
        }
    }
}
