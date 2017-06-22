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
package org.xwiki.properties.internal.converter;

import java.lang.reflect.Type;

import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.namespace.Namespace;
import org.xwiki.component.namespace.NamespaceUtils;
import org.xwiki.properties.converter.AbstractConverter;

/**
 * Converter that converts a value into an {@link Namespace} object.
 *
 * @version $Id$
 * @since 9.5
 */
@Component
@Singleton
public class NamespaceConverter extends AbstractConverter<Namespace>
{
    @Override
    protected Namespace convertToType(Type type, Object value)
    {
        Namespace namespace = null;
        if (value != null) {
            namespace = NamespaceUtils.toNamespace(value.toString());
        }

        return namespace;
    }

    @Override
    protected String convertToString(Namespace value)
    {
        return value != null ? value.serialize() : null;
    }
}
