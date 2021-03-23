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
package org.xwiki.properties.converter;

/**
 * A <strong>ConversionException</strong> indicates that a call to <code>Converter.convert()</code> has failed to
 * complete successfully.
 * 
 * @version $Id$
 * @since 2.0M2
 */
public class ConversionException extends RuntimeException
{
    /**
     * Construct a new exception with the specified message.
     *
     * @param message The message describing this exception
     */
    public ConversionException(String message)
    {
        super(message);
    }

    /**
     * Construct a new exception with the specified message and root cause.
     *
     * @param message The message describing this exception
     * @param cause The root cause of this exception
     */
    public ConversionException(String message, Throwable cause)
    {
        super(message, cause);
    }

    /**
     * Construct a new exception with the specified root cause.
     *
     * @param cause The root cause of this exception
     */
    public ConversionException(Throwable cause)
    {
        super(cause);
    }
}
