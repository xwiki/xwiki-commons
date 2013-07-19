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

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONNull;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import net.sf.json.util.JSONUtils;

/**
 * Velocity tool to facilitate serialization of Java objects to the JSON format.
 * 
 * @version $Id$
 * @since 4.0M2
 */
public final class JSONTool
{
    /** Logging helper object. */
    private Logger logger = LoggerFactory.getLogger(JSONTool.class);

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
     * 
     * @param object the object to be serialized to the JSON format
     * @return the JSON-verified string representation of the given object
     */
    public String serialize(Object object)
    {
        JSON json = null;
        if (object == null) {
            json = JSONNull.getInstance();
        } else if (object instanceof String) {
            return JSONUtils.valueToString(object);
        } else if (JSONUtils.isBoolean(object)) {
            return object.toString();
        } else if (JSONUtils.isNumber(object)) {
            return JSONUtils.numberToString((Number) object);
        } else if (JSONUtils.isArray(object)) {
            json = JSONArray.fromObject(object);
        } else {
            json = JSONObject.fromObject(object);
        }

        return json.toString();
    }

    /**
     * Parse a serialized JSON into a real JSON object. Only valid JSON strings can be parsed, and doesn't support
     * JSONP. If the argument is not valid JSON, then {@code null} is returned.
     * 
     * @param json the string to parse, must be valid JSON
     * @return the parsed JSON, either a {@link net.sf.json.JSONObject} or a {@link net.sf.json.JSONArray}, or
     *         {@code null} if the argument is not a valid JSON
     * @since 5.2M1
     */
    public JSON parse(String json)
    {
        try {
            return JSONSerializer.toJSON(json);
        } catch (JSONException ex) {
            System.out.println(String.format("Tried to parse invalid JSON: [%s], exception was: %s",
                StringUtils.abbreviate(json, 32), ex.getMessage()));
            return null;
        }
    }
}
