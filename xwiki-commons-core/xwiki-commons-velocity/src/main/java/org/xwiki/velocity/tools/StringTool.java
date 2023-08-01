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

import org.xwiki.text.StringUtils;

/**
 * Velocity Tool providing various helpers to manipulate strings.
 * 
 * @version $Id$
 * @since 15.7RC1
 */
public class StringTool extends StringUtils
{
    /**
     * "Undeprecate" {@link org.apache.commons.lang3.StringUtils#defaultString(String, String)} since it's not really
     * possible to use the recommended alternative in Velocity.
     *
     * @param str the String to check, may be null
     * @param nullDefault the default String to return if the input is {@code null}, may be null
     * @return the passed in String, or the default if it was {@code null}
     * @see org.apache.commons.lang3.StringUtils#defaultString(String, String)
     */
    @SuppressWarnings("deprecation")
    public static String defaultString(final String str, final String nullDefault)
    {
        return StringUtils.defaultString(str, nullDefault);
    }
}
