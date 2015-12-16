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

import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.extension.ExtensionFeature;
import org.xwiki.extension.version.internal.DefaultVersionConstraint;
import org.xwiki.properties.converter.AbstractConverter;

/**
 * Convert an extension feature from a String to a {@link ExtensionFeature} object and the other way around.
 *
 * @version $Id$
 * @since 7.0M1
 */
@Component
@Singleton
public class ExtensionFeatureConverter extends AbstractConverter<ExtensionFeature>
{
    public static ExtensionFeature toExtensionFeature(Object value)
    {
        ExtensionFeature feature = null;

        if (value != null) {
            String valueString = value.toString();
            int index = valueString.lastIndexOf('/');
            String id = valueString;
            DefaultVersionConstraint version;
            if (index > 0 && index < (valueString.length() - 1)) {
                id = valueString.substring(0, index);
                version = new DefaultVersionConstraint(valueString.substring(index + 1));
            } else {
                id = valueString;
                version = null;
            }
            feature = new ExtensionFeature(id, version);
        }

        return feature;        
    }
    
    @Override
    protected ExtensionFeature convertToType(Type targetType, Object value)
    {
        return toExtensionFeature(value);
    }

    @Override
    protected String convertToString(ExtensionFeature value)
    {
        return value.toString();
    }
}
