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

import java.lang.reflect.Type;

import org.xwiki.component.annotation.Role;
import org.xwiki.stability.Unstable;

/**
 * Provides a way to access the HTML Displayer for the given type.
 *
 * @version $Id$
 * @since 10.10RC1
 */
@Role
@Unstable
public interface HTMLDisplayerManager
{
    /**
     * Retrieve the appropriate HTML Displayer based on the given type.
     *
     * @param <T> the type of the HTML Displayer
     * @param targetType the type used to retrieve the HTML Displayer
     * @return the HTML Displayer of type {@code <T>}
     * @throws HTMLDisplayerException if the HTML Displayer cannot be found
     */
    <T> HTMLDisplayer<T> getHTMLDisplayer(Type targetType) throws HTMLDisplayerException;
}
