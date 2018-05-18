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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

/**
 * Velocity tool to parse URL parts.
 * 
 * @version $Id$
 * @since 6.3M1
 */
public class URLTool
{
    /**
     * Parse a query string into a map of key-value pairs.
     * 
     * @param query query string to be parsed
     * @return a mapping of parameter names to values suitable e.g. to pass into {@link EscapeTool#url(Map)}
     */
    public Map<String, List<String>> parseQuery(String query)
    {
        Map<String, List<String>> queryParams = new LinkedHashMap<>();
        if (query != null) {
            for (NameValuePair params : URLEncodedUtils.parse(query, StandardCharsets.UTF_8)) {
                String name = params.getName();
                List<String> values = queryParams.get(name);
                if (values == null) {
                    values = new ArrayList<>();
                    queryParams.put(name, values);
                }
                values.add(params.getValue());
            }
        }
        return queryParams;
    }

    /**
     * Convert a map into a query string.
     * 
     * @param map the map to be converted
     * @return the converted query string
     * @since 10.4
     */

    public String getQueryString(Map<String, String> map)
    {
        String queryString = new String();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            queryString = add(queryString, entry.getKey(), entry.getValue());
        }

        return queryString;
    }

    /**
     * Add a parameter to a query string.
     * 
     * @param queryString the initial query string
     * @param name the name of the parameter
     * @param value the value of the parameter
     * @return the new query string
     * @since 10.4
     */
    public String add(String queryString, String name, String value)
    {
        StringBuilder stringBuilder = new StringBuilder(queryString);
        if (stringBuilder.length() > 0) {
            stringBuilder.append("&");
        }
        stringBuilder.append(name).append("=");
        if (!StringUtils.isEmpty(value)) {
            stringBuilder.append(encodeURLParameter(value));
        }

        return stringBuilder.toString();
    }

    private String encodeURLParameter(String value)
    {
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // Shouldn't happen.
            return null;
        }
    }
}
