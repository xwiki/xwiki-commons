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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.descriptor.DefaultComponentDescriptor;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.manager.ComponentRepositoryException;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.component.util.DefaultParameterizedType;
import org.xwiki.observation.AbstractEventListener;
import org.xwiki.observation.event.Event;
import org.xwiki.properties.converter.Converter;

/**
 * Listener in charge of registering converter for the private types of very common collections like {@link List#of()},
 * {@link Collections#EMPTY_LIST}, etc.
 * 
 * @version $Id$
 * @since 17.5.0RC1
 */
@Component
@Named(ConverterRegistratorListener.NAME)
@Singleton
public class ConverterRegistratorListener extends AbstractEventListener implements Initializable
{
    /**
     * The name and role hint of the listener component.
     */
    public static final String NAME = "org.xwiki.properties.internal.converter.CollectionConverterRegistratorListener";

    @Inject
    private ComponentManager componentManager;

    @Inject
    // This component is actually associated to the type ArrayList, and not List
    @SuppressWarnings("checkstyle:IllegalType")
    private Converter<ArrayList> listConverter;

    @Inject
    // This component is actually associated to the type HashSet, and not Set
    @SuppressWarnings("checkstyle:IllegalType")
    private Converter<HashSet> setConverter;

    @Inject
    private Logger logger;

    /**
     * The default constructor.
     */
    public ConverterRegistratorListener()
    {
        super(NAME);
    }

    @Override
    public void initialize() throws InitializationException
    {
        // ListConverter
        registerListConverter(List.of().getClass());
        registerListConverter(List.of(1).getClass());
        registerListConverter(List.of(1, 2, 3).getClass());
        registerListConverter(Arrays.asList().getClass());
        registerListConverter(Collections.emptyList().getClass());
        registerListConverter(Collections.unmodifiableList(Collections.emptyList()).getClass());

        // SetConverter
        registerSetConverter(Set.of().getClass());
        registerSetConverter(Set.of(1).getClass());
        registerSetConverter(Set.of(1, 2, 3).getClass());
        registerSetConverter(Collections.emptySet().getClass());
        registerSetConverter(Collections.unmodifiableSet(Collections.emptySet()).getClass());
    }

    private void registerListConverter(Class<?> collectionClass)
    {
        // This component is actually associated to the type ArrayList, and not List
        @SuppressWarnings("checkstyle:IllegalType")
        DefaultComponentDescriptor<Converter<ArrayList>> componentDescriptor = new DefaultComponentDescriptor<>();
        componentDescriptor.setImplementation(ArrayListConverter.class);
        componentDescriptor.setRoleType(new DefaultParameterizedType(null, Converter.class, collectionClass));

        try {
            this.componentManager.registerComponent(componentDescriptor, this.listConverter);
        } catch (ComponentRepositoryException e) {
            this.logger.error("Failed to register a List converter for type [{}]", collectionClass);
        }
    }

    private void registerSetConverter(Class<?> collectionClass)
    {
        // This component is actually associated to the type HashSet, and not Set
        @SuppressWarnings("checkstyle:IllegalType")
        DefaultComponentDescriptor<Converter<HashSet>> componentDescriptor = new DefaultComponentDescriptor<>();
        componentDescriptor.setImplementation(HashSetConverter.class);
        componentDescriptor.setRoleType(new DefaultParameterizedType(null, Converter.class, collectionClass));

        try {
            this.componentManager.registerComponent(componentDescriptor, this.setConverter);
        } catch (ComponentRepositoryException e) {
            this.logger.error("Failed to register a Set converter for type [{}]", collectionClass);
        }
    }

    @Override
    public void onEvent(Event event, Object source, Object data)
    {
        // Nothing to do here, we implement Listener as a way to be triggered early
    }
}
