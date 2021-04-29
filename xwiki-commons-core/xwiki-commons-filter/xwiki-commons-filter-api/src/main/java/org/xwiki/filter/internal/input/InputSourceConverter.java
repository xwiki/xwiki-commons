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
package org.xwiki.filter.internal.input;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URL;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.apache.commons.lang3.reflect.TypeUtils;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.filter.FilterException;
import org.xwiki.filter.input.DefaultFileInputSource;
import org.xwiki.filter.input.DefaultInputStreamInputSource;
import org.xwiki.filter.input.DefaultReaderInputSource;
import org.xwiki.filter.input.DefaultURLInputSource;
import org.xwiki.filter.input.InputSource;
import org.xwiki.filter.input.InputSourceReferenceParser;
import org.xwiki.properties.converter.AbstractConverter;
import org.xwiki.properties.converter.ConversionException;

/**
 * @version $Id$
 * @since 6.2M1
 */
@Component
@Singleton
public class InputSourceConverter extends AbstractConverter<InputSource>
{
    @Inject
    @Named("context")
    private Provider<ComponentManager> contextComponentManagerProvider;

    @Inject
    private InputSourceReferenceParser parser;

    @Override
    protected <G extends InputSource> G convertToType(Type targetType, Object value)
    {
        if (value == null) {
            return null;
        }

        if (value instanceof InputSource) {
            return (G) value;
        }

        InputSource inputSource;

        if (value instanceof String) {
            inputSource = fromString(value.toString());
        } else if (value instanceof InputStream) {
            inputSource = new DefaultInputStreamInputSource((InputStream) value);
        } else if (value instanceof File) {
            inputSource = new DefaultFileInputSource((File) value);
        } else if (value instanceof Reader) {
            inputSource = new DefaultReaderInputSource((Reader) value);
        } else if (value instanceof URL) {
            inputSource = new DefaultURLInputSource((URL) value);
        } else {
            ParameterizedType componentRole =
                TypeUtils.parameterize(org.xwiki.filter.input.InputSourceConverter.class, value.getClass());

            ComponentManager componentManager = this.contextComponentManagerProvider.get();

            if (componentManager.hasComponent(componentRole)) {
                try {
                    org.xwiki.filter.input.InputSourceConverter converter = componentManager.getInstance(componentRole);

                    inputSource = converter.convert(value);
                } catch (ComponentLookupException e) {
                    throw new ConversionException(
                        "Failed to get the input source converter component for type [" + value.getClass() + "]", e);
                }
            } else {
                // Fallback on the String logic
                inputSource = fromString(value.toString());
            }
        }

        return (G) inputSource;
    }

    private InputSource fromString(String source)
    {
        try {
            return this.parser.parse(source);
        } catch (FilterException e) {
            throw new ConversionException("Failed to parse the inut source reference [" + source + "]", e);
        }
    }
}
