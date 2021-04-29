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
package org.xwiki.filter.internal.output;

import java.io.File;
import java.io.OutputStream;
import java.io.Writer;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.apache.commons.lang3.reflect.TypeUtils;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.filter.FilterException;
import org.xwiki.filter.output.DefaultFileOutputTarget;
import org.xwiki.filter.output.DefaultOutputStreamOutputTarget;
import org.xwiki.filter.output.DefaultWriterOutputTarget;
import org.xwiki.filter.output.OutputTarget;
import org.xwiki.filter.output.OutputTargetReferenceParser;
import org.xwiki.properties.converter.AbstractConverter;
import org.xwiki.properties.converter.ConversionException;

/**
 * @version $Id$
 * @since 6.2M1
 */
@Component
@Singleton
public class OutputTargetConverter extends AbstractConverter<OutputTarget>
{
    @Inject
    @Named("context")
    private Provider<ComponentManager> contextComponentManagerProvider;

    @Inject
    private OutputTargetReferenceParser parser;

    @Override
    protected <G extends OutputTarget> G convertToType(Type targetType, Object value)
    {
        if (value == null) {
            return null;
        }

        if (value instanceof OutputTarget) {
            return (G) value;
        }

        OutputTarget outputTarget;

        if (value instanceof String) {
            outputTarget = fromString(value.toString());
        } else if (value instanceof OutputStream) {
            outputTarget = new DefaultOutputStreamOutputTarget((OutputStream) value);
        } else if (value instanceof File) {
            outputTarget = new DefaultFileOutputTarget((File) value);
        } else if (value instanceof Writer) {
            outputTarget = new DefaultWriterOutputTarget((Writer) value);
        } else {
            ParameterizedType componentRole =
                TypeUtils.parameterize(org.xwiki.filter.output.OutputTargetConverter.class, value.getClass());

            ComponentManager componentManager = this.contextComponentManagerProvider.get();

            if (componentManager.hasComponent(componentRole)) {
                try {
                    org.xwiki.filter.output.OutputTargetConverter converter =
                        componentManager.getInstance(componentRole);

                    outputTarget = converter.convert(value);
                } catch (ComponentLookupException e) {
                    throw new ConversionException(
                        "Failed to get the output target converter component for type [" + value.getClass() + "]", e);
                }
            } else {
                // Fallback on the String logic
                outputTarget = fromString(value.toString());
            }
        }

        return (G) outputTarget;
    }

    private OutputTarget fromString(String target)
    {
        try {
            return this.parser.parse(target);
        } catch (FilterException e) {
            throw new ConversionException("Failed to parse the output target reference [" + target + "]", e);
        }
    }
}
