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
package org.xwiki.javascript.importmap.internal.parser;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.xwiki.webjars.WebjarDescriptor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

/**
 * Parse the importmap declaration format.
 *
 * @version $Id$
 * @since 17.10.0RC1
 */
public class JavascriptImportmapParser
{
    private static final ObjectMapper OBJECT_MAPPER = JsonMapper.builder().build();

    /**
     * Parse an importmap declaration.
     *
     * @param importMapJSON the importmap declaration as a JSON string
     * @return the resolve mapping of importmap module id and their corresponding {@link WebjarDescriptor} definitions
     * @throws JavascriptImportmapException in case of issue when parsing the provided JSON string
     */
    public Map<String, WebjarDescriptor> parse(String importMapJSON) throws JavascriptImportmapException
    {
        Map<String, WebjarDescriptor> extensionImportMap;
        Object parsedJSON = null;
        try {
            parsedJSON = OBJECT_MAPPER.readValue(importMapJSON, Object.class);
        } catch (JsonProcessingException e) {
            throw new JavascriptImportmapException("Malformed importmap definition", e);
        }
        extensionImportMap = new LinkedHashMap<>();
        if (parsedJSON instanceof Map parsedJSONMap) {
            for (Map.Entry o : (Set<Map.Entry>) parsedJSONMap.entrySet()) {
                Object value = o.getValue();
                var key = String.valueOf(o.getKey());
                if (value instanceof Map valueMap) {
                    WebjarDescriptor webjarDescriptor;
                    try {
                        webjarDescriptor = new WebjarDescriptor(
                            (String) valueMap.get("webjarId"),
                            (String) valueMap.get("namespace"),
                            (String) valueMap.get("path"),
                            (Map<String, ?>) valueMap.get("params"));
                    } catch (NullPointerException | IllegalArgumentException e) {
                        throw new JavascriptImportmapException("Malformed value for key [%s]".formatted(key), e);
                    }
                    extensionImportMap.put(key, webjarDescriptor);
                } else {
                    extensionImportMap.put(key, parseValue(String.valueOf(value)));
                }
            }
        }
        return extensionImportMap;
    }

    private WebjarDescriptor parseValue(String value) throws JavascriptImportmapException
    {
        var separator = "/";
        if (!value.contains(separator)) {
            throw new JavascriptImportmapException("Invalid importmap value: %s".formatted(value));
        }
        var split = value.split(separator, 2);
        return new WebjarDescriptor(split[0], split[1]);
    }
}

