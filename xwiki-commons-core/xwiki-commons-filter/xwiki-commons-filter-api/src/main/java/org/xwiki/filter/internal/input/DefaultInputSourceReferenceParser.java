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
import java.net.URL;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.filter.FilterException;
import org.xwiki.filter.input.DefaultFileInputSource;
import org.xwiki.filter.input.DefaultURLInputSource;
import org.xwiki.filter.input.InputSource;
import org.xwiki.filter.input.InputSourceReferenceParser;
import org.xwiki.filter.input.StringInputSource;
import org.xwiki.properties.converter.ConversionException;

/**
 * The default implementation of {@link InputSourceReferenceParser} which find the right parser based on the prefix.
 * 
 * @version $Id$
 * @since 13.4RC1
 */
@Component
@Singleton
public class DefaultInputSourceReferenceParser implements InputSourceReferenceParser
{
    @Inject
    @Named("context")
    private Provider<ComponentManager> contextComponentManagerProvider;

    @Override
    public InputSource parse(String reference) throws FilterException
    {
        if (reference == null) {
            return null;
        }

        InputSource inputSource;

        int index = reference.indexOf(':');
        if (index > 0) {
            String prefix = reference.substring(0, index);
            String value = reference.substring(index + 1);

            if (prefix.equals("url")) {
                try {
                    inputSource = new DefaultURLInputSource(new URL(value));
                } catch (Exception e) {
                    throw new ConversionException("Failed to create input source for URL [" + reference + "]", e);
                }
            } else if (prefix.equals("file")) {
                inputSource = new DefaultFileInputSource(new File(value));
            } else if (prefix.equals("string")) {
                inputSource = new DefaultFileInputSource(new File(value));
            } else if (prefix.equals("resource")) {
                ClassLoader classloader = Thread.currentThread().getContextClassLoader();
                URL resource = classloader.getResource(value);
                inputSource = new DefaultURLInputSource(resource);
            } else {
                ComponentManager componentManager = this.contextComponentManagerProvider.get();

                if (componentManager.hasComponent(InputSourceReferenceParser.class, prefix)) {
                    InputSourceReferenceParser parser;
                    try {
                        parser = componentManager.getInstance(InputSourceReferenceParser.class, prefix);
                    } catch (ComponentLookupException e) {
                        throw new FilterException(
                            "Failed to get the input source reference parser component for prefix [" + prefix + "]", e);
                    }

                    inputSource = parser.parse(value);
                } else {
                    inputSource = new StringInputSource(reference);
                }
            }
        } else {
            inputSource = new StringInputSource(reference);
        }

        return inputSource;
    }

}
