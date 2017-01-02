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
package org.xwiki.extension.internal.converter;

import java.lang.reflect.Type;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.extension.Extension;
import org.xwiki.properties.ConverterManager;
import org.xwiki.properties.converter.AbstractConverter;
import org.xwiki.properties.converter.ConversionException;
import org.xwiki.properties.converter.Converter;

/**
 * Convert object to Extension instance.
 * 
 * @version $Id$
 * @since 7.0M1
 */
@Component
@Singleton
public class ExtensionConverter extends AbstractConverter<Extension>
{
    @Inject
    private ConverterManager converter;

    @Override
    protected <G extends Extension> G convertToType(Type targetType, Object value)
    {
        if (value == null) {
            return null;
        }

        Converter<?> sourceConverter = this.converter.getConverter(value.getClass());
        if (sourceConverter != null) {
            return sourceConverter.convert(targetType, value);
        }

        throw new ConversionException(String.format("Unsupported target type [%s]", value.getClass()));
    }
}
