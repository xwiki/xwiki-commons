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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.util.DefaultParameterizedType;
import org.xwiki.displayer.HTMLDisplayer;
import org.xwiki.displayer.HTMLDisplayerException;
import org.xwiki.displayer.HTMLDisplayerManager;

/**
 * Default implementation for {@link org.xwiki.displayer.HTMLDisplayerManager}.
 *
 * @version $Id$
 * @since 10.11RC1
 */
@Component
@Singleton
public class DefaultDisplayerManager implements HTMLDisplayerManager
{
    /**
     * Use to find the proper {@link org.xwiki.displayer.HTMLDisplayer} component for the provided target type.
     */
    @Inject
    @Named("context")
    private Provider<ComponentManager> componentManagerProvider;

    @Override
    public <T> HTMLDisplayer<T> getHTMLDisplayer(Type targetType) throws HTMLDisplayerException
    {
        try {
            ComponentManager componentManager = this.componentManagerProvider.get();

            ParameterizedType converterType = new DefaultParameterizedType(null, HTMLDisplayer.class, targetType);

            return componentManager.getInstance(converterType);
        } catch (ComponentLookupException e) {
            throw new HTMLDisplayerException(
                    "Failed to retrieve the HTML displayer for target type [" + targetType + "]", e);
        }
    }

    @Override
    public <T> String display(Type targetType, T value) throws HTMLDisplayerException
    {
        return getHTMLDisplayer(targetType).display(value);
    }

    @Override
    public <T> String display(Type targetType, T value, Map<String, String> parameters) throws HTMLDisplayerException
    {
        return getHTMLDisplayer(targetType).display(value, parameters);
    }

    @Override
    public <T> String display(Type targetType, T value, Map<String, String> parameters, String mode)
            throws HTMLDisplayerException
    {
        return getHTMLDisplayer(targetType).display(value, parameters, mode);
    }
}
