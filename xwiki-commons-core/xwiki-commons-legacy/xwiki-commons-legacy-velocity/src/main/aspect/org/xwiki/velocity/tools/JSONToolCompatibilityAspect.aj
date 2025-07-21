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

 /**
 * Add a backward compatibility layer to the {@link JSONTool} class.
 *
 * @version $Id$
 * @since 17.6.0RC1
 */
public privileged aspect JSONToolCompatibilityAspect
{
    /**
     * Parse a serialized JSON into a real JSON object. Only valid JSON strings can be parsed, and doesn't support
     * JSONP. If the argument is not valid JSON, then {@code null} is returned.
     * <p>
     * Note that the return type has changed in 17.6.0RC1 from {@link net.sf.json.JSON} to {@link Object} in order to
     * drop the dependency on json-lib which is not maintained anymore and requires commons-lang 2.x which is not
     * maintained either and has known security vulnerabilities. Most of the Velocity scripts using this method should
     * continue to work as before, but there can be breakage if they were relying on the return type being
     * {@link net.sf.json.JSON}. Best is to update those scripts to use {@link #fromString(String)} instead.
     *
     * @param json the string to parse, must be valid JSON
     * @return the {@link Object} resolved from the string (usually a Maps or and Lists), or {@code null} if the given
     *         string is not valid JSON; note that before 17.6.0RC1 the returned value was either a
     *         {@link net.sf.json.JSONObject} or a {@link net.sf.json.JSONArray} if the given string was a valid JSON
     * @since 5.2M1
     * @deprecated since 9.9RC1, use {@link #fromString(String)} instead
     */
    @Deprecated
    public Object JSONTool.parse(String json)
    {
        return fromString(json);
    }
}