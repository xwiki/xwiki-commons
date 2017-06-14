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

package org.xwiki.extension.internal;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.extension.ExtensionManagerConfiguration;
import org.xwiki.extension.handler.ExtensionInitializer;
import org.xwiki.extension.repository.CoreExtensionRepository;
import org.xwiki.extension.repository.ExtensionRepositoryManager;
import org.xwiki.extension.repository.internal.core.DefaultCoreExtensionRepository;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.event.ApplicationStartedEvent;
import org.xwiki.observation.event.Event;

/**
 * An application started listener to initialize extensions.
 *
 * @version $Id$
 * @since 4.0M1
 */
@Component
@Singleton
@Named("ExtensionApplicationStartedListener")
public class ExtensionApplicationStartedListener implements EventListener
{
    /**
     * The list of events observed.
     */
    private static final List<Event> EVENTS = Collections.<Event>singletonList(new ApplicationStartedEvent());

    @Inject
    private Provider<CoreExtensionRepository> coreExtensionsProvider;

    @Inject
    private Provider<ExtensionManagerConfiguration> configuration;

    @Inject
    private Provider<ExtensionRepositoryManager> repositoryManagerProvider;

    /**
     * The extension initializer.
     * <p>
     * Use it only when receiving an {@link ApplicationStartedEvent} event.
     */
    @Inject
    private Provider<ExtensionInitializer> extensionInitializer;

    @Override
    public List<Event> getEvents()
    {
        return EVENTS;
    }

    @Override
    public String getName()
    {
        return "ExtensionApplicationStartedListener";
    }

    @Override
    public void onEvent(Event arg0, Object arg1, Object arg2)
    {
        // Make sure installed extensions are initialized (it might have already been done but it's OK since initialize
        // is automatically called only once)
        this.extensionInitializer.get();

        // Update core extension informations
        // Only if enabled and only if there is any remote extension repositories enabled
        CoreExtensionRepository coreExtensions = this.coreExtensionsProvider.get();
        if (coreExtensions instanceof DefaultCoreExtensionRepository && this.configuration.get().resolveCoreExtensions()
            && !this.repositoryManagerProvider.get().getRepositories().isEmpty()) {
            ((DefaultCoreExtensionRepository) coreExtensions).updateExtensions();
        }
    }
}
