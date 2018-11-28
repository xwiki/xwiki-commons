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
        return this.getHTMLDisplayer(targetType, null);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Example: if the target type is <code>A&lt;B&lt;C&gt;&gt;</code> with {@code hint} hint,
     * the following lookups will be made until a {@link HTMLDisplayer} component is found:
     * <ul>
     *     <li>Component: <code>HTMLDisplayer&lt;A&lt;B&lt;C&gt;&gt;&gt;</code>; Role: {@code hint}
     *     <li>Component: <code>HTMLDisplayer&lt;A&lt;B&gt;&gt;</code>; Role: {@code hint}
     *     <li>Component: <code>HTMLDisplayer&lt;A&gt;</code>; Role: {@code hint}
     *     <li>Component: {@code HTMLDisplayer}; Role: {@code hint}
     *     <li>Component: {@code HTMLDisplayer}; Role: default
     * </ul>
     */
    @Override
    public <T> HTMLDisplayer<T> getHTMLDisplayer(Type targetType, String roleHint) throws HTMLDisplayerException
    {
        try {
            HTMLDisplayer<T> component;
            ComponentManager componentManager = this.componentManagerProvider.get();

            Type type = targetType;
            Type displayerType = new DefaultParameterizedType(null, HTMLDisplayer.class, type);
            while (!componentManager.hasComponent(displayerType, roleHint) && type instanceof ParameterizedType) {
                type = ((ParameterizedType) type).getRawType();
                displayerType = new DefaultParameterizedType(null, HTMLDisplayer.class, type);
            }
            if (!componentManager.hasComponent(displayerType, roleHint)) {
                displayerType = HTMLDisplayer.class;
            }
            if (componentManager.hasComponent(displayerType, roleHint)) {
                component = componentManager.getInstance(displayerType, roleHint);
            } else {
                component = componentManager.getInstance(displayerType);
            }

            return component;
        } catch (ComponentLookupException e) {
            throw new HTMLDisplayerException(
                    "Failed to initialized the HTML displayer for target type [" + targetType + "] and role [" + String
                            .valueOf(roleHint) + "]", e);
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
