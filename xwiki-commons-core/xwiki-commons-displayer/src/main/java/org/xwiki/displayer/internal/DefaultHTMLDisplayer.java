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
package org.xwiki.displayer.internal;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.displayer.HTMLDisplayer;
import org.xwiki.displayer.HTMLDisplayerException;

/**
 * Default implementation of {@code HTMLDisplayer}.
 *
 * @version $Id$
 * @since 10.11RC1
 */
@Component
@Singleton
public class DefaultHTMLDisplayer implements HTMLDisplayer
{
    /**
     * {@inheritDoc}
     *
     * Displays the value with the 'view' mode.
     */
    @Override
    public String display(Type type, Object value)
    {
        return display(type, value, new HashMap<>());
    }

    /**
     * {@inheritDoc}
     *
     * Displays the value with the 'view' mode.
     */
    @Override
    public String display(Type type, Object value, Map parameters)
    {
        return display(type, value, parameters, "view");
    }

    @Override
    public String display(Type type, Object value, Map parameters, String mode)
    {
        if (value == null) {
            return "";
        } else {
            return value.toString();
        }
    }
}
