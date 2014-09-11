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
package org.xwiki.filter;

import java.util.LinkedHashMap;

import org.xwiki.stability.Unstable;

/**
 * Custom parameters passed to a filter event.
 *
 * @version $Id$
 * @since 5.2M2
 */
@Unstable
public class FilterEventParameters extends LinkedHashMap<String, Object>
{
    /**
     * The name of the parameter containing the custom parameters.
     */
    public static final String NAME = "parameters";

    /**
     * The default value of a {@link FilterEventParameters} as {@link String}.
     */
    public static final String DEFAULT = "";

    /**
     * Empty instance.
     */
    public static final FilterEventParameters EMPTY = new FilterEventParameters();
}
