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
package org.xwiki.displayer;

import java.util.Map;

import org.xwiki.component.annotation.Role;
import org.xwiki.stability.Unstable;

/**
 * Generates the HTML used to display the values of a given type {@link T}.
 *
 * @param <T> the type of values that are being displayed as HTML
 * @version $Id$
 * @since 10.11RC1
 */
@Role
@Unstable
public interface HTMLDisplayer<T>
{
    /**
     * @param value the value to be displayed
     * @return the HTML that can be used to display the given value
     * @throws HTMLDisplayerException if an error occurs during the display
     */
    String display(T value) throws HTMLDisplayerException;

    /**
     * @param value the value to be displayed
     * @param parameters parameters used while generating the html. Could be the attributes of an input for instance.
     * @return the HTML that can be used to display the given value
     * @throws HTMLDisplayerException if an error occurs during the display
     */
    String display(T value, Map<String, String> parameters) throws HTMLDisplayerException;

    /**
     * @param value the value to be displayed
     * @param parameters parameters used while generating the html. Could be the attributes of an input for instance.
     * @param mode the display mode (view, edit, ...)
     * @return the HTML that can be used to display the given value
     * @throws HTMLDisplayerException if an error occurs during the display
     */
    String display(T value, Map<String, String> parameters, String mode) throws HTMLDisplayerException;
}
