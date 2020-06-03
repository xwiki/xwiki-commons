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
package org.xwiki.job.internal;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.xwiki.cache.Cache;
import org.xwiki.cache.CacheException;
import org.xwiki.cache.CacheManager;
import org.xwiki.cache.config.LRUCacheConfiguration;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.event.ComponentDescriptorRemovedEvent;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.job.GroupedJobInitializer;
import org.xwiki.job.GroupedJobInitializerManager;
import org.xwiki.job.JobGroupPath;
import org.xwiki.job.JobManagerConfiguration;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.event.Event;

/**
 * A component dedicated to find the appropriate {@link GroupedJobInitializer} based on a {@link JobGroupPath}.
 *
 * @since 12.5RC1
 * @version $Id$
 */
@Component
@Singleton
public class DefaultGroupedJobInitializerManager implements GroupedJobInitializerManager, Initializable, EventListener
{
    @Inject
    @Named("context")
    private Provider<ComponentManager> componentManagerProvider;

    @Inject
    private Logger logger;

    @Inject
    private GroupedJobInitializer defaultGoupedJobInitializer;

    @Inject
    private CacheManager cacheManager;

    @Inject
    private JobManagerConfiguration configuration;

    private Cache<GroupedJobInitializer> cachedInitializers;

    @Override
    public void initialize() throws InitializationException
    {
        LRUCacheConfiguration cacheConfiguration = new LRUCacheConfiguration("job.grouped.initializer",
            this.configuration.getGroupedJobInitializerCacheSize());
        try {
            this.cachedInitializers = this.cacheManager.createNewCache(cacheConfiguration);
        } catch (CacheException e) {
            throw new InitializationException("Error while creating cache for GroupedJobInitializer", e);
        }
    }

    /**
     * Search a {@link GroupedJobInitializer} by checking if their ID matches the given {@link JobGroupPath}.
     * This methods fills the cache with initializer information as side effect.
     * This methods performs the following search:
     *   1. check if the initializer is already stored in the cache
     *   2. if not stored, iterates over all the {@link GroupedJobInitializer}, put them in cache, and check if it
     *      matches the path. If it matches stop iterating.
     * @param path the searched path.
     * @return an initializer matching the path or null if no initializer has been found.
     */
    private GroupedJobInitializer search(JobGroupPath path)
    {
        GroupedJobInitializer result = this.cachedInitializers.get(path.toString());
        if (result == null) {
            try {
                List<GroupedJobInitializer> initializers =
                    this.componentManagerProvider.get().getInstanceList(GroupedJobInitializer.class);
                for (GroupedJobInitializer initializer : initializers) {
                    if (initializer.getId() != null) {
                        this.cachedInitializers.set(initializer.getId().toString(), initializer);
                        if (path.equals(initializer.getId())) {
                            result = initializer;
                            break;
                        }
                    }
                }
            } catch (ComponentLookupException e) {
                this.logger.error("Error while loading GroupedJobInitializer component: [{}]",
                    ExceptionUtils.getRootCauseMessage(e));
            }
        }
        return result;
    }

    @Override
    public GroupedJobInitializer getGroupedJobInitializer(JobGroupPath jobGroupPath)
    {
        GroupedJobInitializer result = null;
        if (jobGroupPath != null) {

            JobGroupPath currentPath = jobGroupPath;

            // loop over the parents of the path and stop as soon as a result has been found
            // or if there is no more parent.
            while (currentPath != null && result == null) {
                result = search(currentPath);
                currentPath = currentPath.getParent();
            }
        }

        // if nothing has been found, return the default implementation.
        if (result == null) {
            result = this.defaultGoupedJobInitializer;
        }

        return result;
    }

    @Override
    public String getName()
    {
        return getClass().getSimpleName();
    }

    @Override
    public List<Event> getEvents()
    {
        return Collections.singletonList(new ComponentDescriptorRemovedEvent());
    }

    @Override
    public void onEvent(Event event, Object source, Object data)
    {
        ComponentDescriptorRemovedEvent componentDescriptorRemovedEvent = (ComponentDescriptorRemovedEvent) event;

        // Flush the cache in case a GroupedJobInitializer component has been removed
        if (componentDescriptorRemovedEvent.getRoleType() == GroupedJobInitializer.class) {
            this.cachedInitializers.removeAll();
        }
    }
}
